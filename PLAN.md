# FirstClub Membership Program — Execution Plan

> Backend system for a subscription-based Membership Program with tiered benefits.
> Stack: Java + Spring Boot + Maven + Spring Data JPA, PostgreSQL (prod) / H2 (demo).
> Schema: Hibernate `ddl-auto` + `data.sql` seed (no migration tool — kept simple for the demo).
> Status: **Plan under review — no application code written yet.**

---

## 0. Core Modeling Insight

Plans and Tiers are two **orthogonal axes**:

- **Plan** = billing cadence + price *(the "how long / how much")* — Monthly, Quarterly, Yearly.
- **Tier** = a configurable bundle of benefits + eligibility criteria *(the "what you get")* — Silver, Gold, Platinum.
- **Subscription** binds a user to one **(Plan, Tier)** pair for a time window.
- **Price** is resolved from a **Plan × Tier matrix** (decided below), so a Gold-Yearly can cost more than a Silver-Yearly.

---

## 1. High-Level Architecture

Modular Spring Boot monolith, package-by-feature, layered inside each module. Tier evaluation and
benefits are pluggable (Strategy pattern) to satisfy the extensibility / "configurable" requirement.

```
┌─────────────────────────────────────────────────────────────┐
│                      REST API Layer                           │
│  PlanController · TierController · SubscriptionController ·    │
│  BenefitController · (CheckoutController – benefit preview)   │
└───────────────────────────┬───────────────────────────────────┘
                            │ DTOs (request/response, never expose entities)
┌───────────────────────────▼───────────────────────────────────┐
│                     Service / Domain Layer                    │
│                                                               │
│  ┌─────────────────┐  ┌──────────────────┐  ┌──────────────┐ │
│  │ SubscriptionSvc │  │ TierEvaluationSvc │  │ BenefitSvc   │ │
│  │ subscribe/      │  │  ┌─────────────┐  │  │ resolve      │ │
│  │ upgrade/        │  │  │TierCriterion│  │  │ benefits for │ │
│  │ downgrade/      │  │  │  Strategy   │◄─┼──┤ a tier       │ │
│  │ cancel          │  │  │ (pluggable) │  │  └──────────────┘ │
│  └────────┬────────┘  │  └─────────────┘  │                  │
│           │           │  OrderCount /     │                  │
│           │           │  MonthlySpend /   │                  │
│           │           │  Cohort criteria  │                  │
│           │           └──────────┬────────┘                  │
│   Domain state machine            │  consumes order signals  │
│   (ACTIVE→CANCELLED→EXPIRED)       │                          │
└───────────┬────────────────────────┬─────────────────────────┘
            │                        │
┌───────────▼────────────────────────▼─────────────────────────┐
│                    Repository Layer (Spring Data JPA)         │
└───────────────────────────┬───────────────────────────────────┘
                            │
                  ┌─────────▼─────────┐
                  │  PostgreSQL / H2  │   ddl-auto + data.sql
                  └───────────────────┘

Cross-cutting: Scheduled job (expiry sweep + tier re-evaluation),
Domain events (subscription created/upgraded), GlobalExceptionHandler,
Bean Validation, optimistic locking for concurrency.

Schema strategy: Hibernate `ddl-auto` generates tables from entities;
`data.sql` seeds plans/tiers/benefits/criteria/pricing. The one-ACTIVE-
per-user guard is enforced at the app layer (+ `@Version`) since a partial
unique index isn't auto-generated.
```

### Key design decisions

- **Strategy pattern for tier criteria** — each rule (`OrderCountCriterion`, `MonthlySpendCriterion`,
  `CohortCriterion`) implements a `TierCriterion` interface. New criteria added without touching
  existing code (Open/Closed).
- **Benefits as data, not code** — benefits are configurable rows linked to tiers, so "each tier
  unlocks additional perks – should be configurable" is satisfied by DB config, not redeployment.
- **Concurrency** — optimistic locking (`@Version`) on the subscription, idempotent subscribe,
  partial unique constraint guaranteeing one ACTIVE subscription per user.
- **Event-driven tier re-evaluation** — order events / scheduled jobs trigger re-evaluation,
  decoupling the membership module from the order domain.

---

## 2. Database ER Design (PostgreSQL / H2 compatible)

```
 membership_plan        membership_tier        benefit
 ─────────────          ───────────────        ───────
 id (PK)                id (PK)                id (PK)
 code (UQ)  M/Q/Y       code (UQ)              code (UQ)
 name                   name   Silver/Gold/..  type (enum)
 billing_period (enum)  rank (int, ordering)   description
 duration_days          description            created_at
 price (numeric)*       active (bool)
 currency               created_at
 active (bool)
 created_at

       tier_benefit (join + config)          tier_criterion
       ────────────────────────────          ──────────────
       id (PK)                               id (PK)
       tier_id (FK→tier)                     tier_id (FK→tier)
       benefit_id (FK→benefit)               type (enum: ORDER_COUNT /
       benefit_metadata (text)                 MONTHLY_SPEND / COHORT)
       UQ(tier_id, benefit_id)               threshold (numeric)
                                             config_json (text)

 plan_tier_price (pricing source of truth)
 ─────────────────────────────────────────
 id (PK)
 plan_id (FK→membership_plan)
 tier_id (FK→membership_tier)
 price (numeric)
 currency
 active (bool)
 UQ(plan_id, tier_id)

 user_account (minimal/stub)        subscription
 ──────────────────────────        ────────────
 id (PK)                           id (PK)
 external_id (UQ)                  user_id (FK→user_account)
 email                             plan_id (FK→plan)
 cohort                            tier_id (FK→tier)
 created_at                        status (enum: ACTIVE/CANCELLED/EXPIRED)
                                   start_date / end_date
                                   auto_renew (bool)
                                   version (optimistic lock)
                                   created_at / updated_at
                                   ⮕ one ACTIVE per user (app-layer guard)

 subscription_history (audit)              user_order_stats (read model)
 ───────────────────────────              ─────────────────────────────
 id (PK)                                  user_id (FK)
 subscription_id (FK)                     period (year-month)
 action (SUBSCRIBE/UPGRADE/               order_count
   DOWNGRADE/CANCEL/RENEW/EXPIRE)         total_value
 from_tier_id / to_tier_id                updated_at
 from_plan_id / to_plan_id                UQ(user_id, period)
 created_at

 user_tier_status (eligibility ceiling, computed by Task 6 evaluation)
 ─────────────────────────────────────────────────────────────────────
 user_id (FK, UQ)
 eligible_tier_id (FK→tier)   highest tier the user currently qualifies for
 evaluated_at                  when the ceiling was last computed
```

\* `membership_plan.price` is a list/base reference; the **authoritative subscription price** is
`plan_tier_price` for the chosen (plan, tier) pair.

### Relationships

- `subscription` → exactly one `plan` and one `tier`. One ACTIVE per user enforced at the app layer
  (guard + `@Version` optimistic lock), since `ddl-auto` won't generate a partial unique index.
- `tier` ↔ `benefit` many-to-many through `tier_benefit`, with per-tier `benefit_metadata`
  (stored as `text` JSON for H2/Postgres portability; e.g. discount % = 10 for Gold, 15 for Platinum).
- `tier` → many `tier_criterion` (eligibility rules).
- `plan_tier_price` → the single source of truth for what subscribing costs.
- `subscription_history` → full audit trail of upgrade/downgrade/cancel.
- `user_order_stats` → denormalized read model fed by order signals; what tier criteria evaluate
  against (avoids synchronous calls to a foreign order service).

---

## 3. Phased Task Breakdown

Each task is independently codeable, reviewable, and leaves the app compiling/runnable.

| Task | Scope | Deliverable / Review checkpoint |
|------|-------|--------------------------------|
| **Task 1 — Project skeleton** | Spring Boot + Maven setup, dependencies (Web, JPA, Validation, H2, Postgres, Lombok), profiles (`h2`/`postgres`), `ddl-auto` + `data.sql` config, package structure, `GlobalExceptionHandler`, base error DTO. | App boots, `/actuator/health` green. |
| **Task 2 — Plan & Tier catalog** | Entities `MembershipPlan`, `MembershipTier`, `plan_tier_price`, repos, `data.sql` seed data, read APIs: `GET /plans`, `GET /tiers`, `GET /tiers/{id}`, `GET /plans/pricing` (full priceable matrix). | "Get membership plans and tiers" requirement done. |
| **Task 3 — Benefits & configurability** | `Benefit`, `TierBenefit` (with `benefit_metadata`), `BenefitService` to resolve effective benefits for a tier, `GET /tiers/{id}/benefits`. Strategy seam for benefit types (delivery, discount, exclusive deals, priority support). | Configurable perks per tier demonstrated. |
| **Task 4 — Subscription core** | `Subscription` entity + status state machine, `SubscriptionService`: **subscribe (plan+tier)** with price resolved from `plan_tier_price`, **track current membership & expiry**, `POST /subscriptions`, `GET /users/{id}/subscription`. Introduce `TierEligibilityService` seam (initial impl: only base tier eligible) so subscribe is gated from day one; Task 6 swaps in the real criteria impl. Concurrency: `@Version` + one-active-per-user guard + idempotency. | Core subscribe + tracking working, gate seam in place. |
| **Task 5 — Lifecycle: upgrade / downgrade / cancel** | Tier upgrade & downgrade **gated by eligibility ceiling** (reject `rank > eligibleTier.rank`), cancel, endpoints `POST /subscriptions/{id}/upgrade\|downgrade\|cancel`. Tier change is immediate, updates the price snapshot, no mid-cycle money movement. (Proration + `subscription_history` audit dropped — not core; see decisions.) | Full user-action set complete, gate-enforced. |
| **Task 6 — Tier evaluation engine** | `TierCriterion` strategy interface + `OrderCountCriterion`, `MonthlySpendCriterion`, `CohortCriterion`. `TierEvaluationService` computes `eligibleTier` (authorization ceiling) from `user_order_stats`, persists to `user_tier_status`. Base tier ungated. Endpoint to ingest/simulate order stats (`POST /users/{id}/orders`) + `POST /users/{id}/tier/evaluate` + `GET /users/{id}/tier/eligibility`. | Eligibility gate that powers Task 5 — the standout abstraction. |
| **Task 7 — Scheduled jobs & expiry** | `@Scheduled` sweep: expire past-due subscriptions, auto-renew, periodic tier re-evaluation. Domain events for upgrade/expire. | Expiry/renewal automation. |
| **Task 8 — Hardening & demo** | Integration tests (`@SpringBootTest` + MockMvc over H2) for catalog, subscribe/cancel/idempotency/gate, evaluation, sweep; README with curl walkthrough; OpenAPI/Swagger UI. (Concurrency/parallel-subscribe test skipped per scope decision.) | Demo-able, evaluation criteria met. |

---

## 4. Locked Decisions

- **Schema management:** Hibernate `ddl-auto` + `data.sql` seed — no migration tool (Flyway dropped
  to keep the demo lean). JSON config columns stored as `text` for H2/Postgres portability.
- **Pricing model:** **Plan × Tier matrix** (`plan_tier_price` table) — authoritative source of
  subscription price.
- **Proration: dropped (not core).** No payment/billing system is in scope, so there is no money to
  prorate. Upgrade/downgrade changes the tier immediately, updates the price snapshot to the new
  tier's price (correct for renewal), keeps the end date, and moves no money mid-cycle. No
  `ProrationPolicy` abstraction (YAGNI).
- **`subscription_history` audit: dropped (not core).** Out of scope for the core requirements.
- **Order signals:** no real order service exists, so a small endpoint pushes/simulates
  `user_order_stats` to make tier evaluation demoable end-to-end.
- **Tier semantics — criteria are a hard eligibility gate.** The spec uses "tier" two ways: a
  *paid* tier you subscribe to (commerce) and an *earned* tier from behavior (loyalty). Reconciled:
  - Tier evaluation (Task 6) computes **`eligibleTier`** = the highest-rank tier whose criteria
    (order count / monthly spend / cohort) are all satisfied. Stored in
    `user_tier_status.eligible_tier_id`.
  - `eligibleTier` is an **authorization ceiling**: a user may subscribe / upgrade / renew only to a
    tier with `rank ≤ eligibleTier.rank`. Requesting an unearned tier is **rejected**
    (`BusinessRuleException`, HTTP 409) — not silently downgraded.
  - `subscription.tier_id` = the **subscribed** tier (≤ ceiling) → drives **both billing and
    benefits**. Changes only via explicit upgrade/downgrade.
  - **Eligibility is checked at upgrade / downgrade / renewal — never mid-term.** Once earned and
    bought, the tier is held for the paid term even if activity later dips.
  - **Base tier (Silver, rank 1) is ungated** — always eligible, so new users can always subscribe.
    Only Gold/Platinum carry criteria rows.
  - **At renewal (Task 7):** re-check eligibility; if no longer eligible for the current tier,
    auto-renew at the **highest tier still qualified for** (a downgrade if needed) — never
    auto-upgrade, never a surprise upcharge.

---

## 5. Evaluation Criteria Mapping (from problem statement)

| Criterion | Where addressed |
|-----------|-----------------|
| Abstractions created | Strategy seams (`TierCriterion`, benefit resolution, `ProrationPolicy`); domain state machine. |
| Entity design | Section 2 — normalized, Flyway-managed, Plan/Tier orthogonality, configurable benefits-as-data. |
| Extensibility | New criteria/benefits/plans via config or new strategy classes, no edits to existing code (OCP). |
| Modularity | Package-by-feature, layered Controller → Service → Repository. |
| Java best practices | DTO boundaries, Bean Validation, global exception handling, immutability where sensible. |
| Concurrency (bonus) | `@Version` optimistic locking, idempotent subscribe, app-layer one-active-per-user guard, parallel-subscribe test. |
| Running / demo-able | Task 8 — Swagger UI, seed data, curl walkthrough, H2 profile. |
```