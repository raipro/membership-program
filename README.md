# FirstClub Membership Program

Backend for a subscription-based **Membership Program** with tiered, configurable benefits and
criteria-driven tier eligibility — integrated with a shopping/checkout journey.

Built with **Java 17 · Spring Boot 3.3 · Spring Data JPA · MapStruct**, running on **H2** (zero-setup
demo) or **PostgreSQL** (prod).

---

## Core concept: two orthogonal axes

| Axis | What it is | Examples |
|------|-----------|----------|
| **Plan** | Billing cadence + duration + price | Monthly / Quarterly / Yearly |
| **Tier** | A configurable bundle of benefits + eligibility criteria | Silver / Gold / Platinum |

A **Subscription** binds a user to one **(Plan, Tier)** pair for a term. Price comes from a
**Plan × Tier matrix**, so Gold-Yearly can cost more than Silver-Yearly.

### Subscribed tier vs. earned tier (the eligibility gate)

The spec uses "tier" two ways — a *paid* tier you subscribe to, and an *earned* tier from behavior.
They're reconciled with a **hard eligibility gate**:

- Tier **evaluation** computes the highest tier a user qualifies for (`eligibleTier`) from their
  order count / monthly spend / cohort, and stores it as an **authorization ceiling**.
- A user may **subscribe / upgrade / renew** only to a tier at or below that ceiling — an unearned
  tier is rejected (HTTP 409).
- The **subscribed** tier drives billing *and* benefits. Earning a tier never auto-charges; the base
  tier (Silver) is always available.

---

## Quick start

```bash
# Requires JDK 17. (If your default `mvn` uses a newer JDK, point JAVA_HOME at 17.)
mvn spring-boot:run
```

- API base: `http://localhost:8080/api/v1`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- OpenAPI spec: `http://localhost:8080/v3/api-docs`
- H2 console: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:membership`, user `sa`)
- Health: `http://localhost:8080/actuator/health`

The H2 profile is active by default and seeds plans, tiers, the price matrix, benefits, criteria,
demo users, and a few past-due subscriptions for the maintenance demo.

### PostgreSQL

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=postgres
# Configurable via DB_URL / DB_USERNAME / DB_PASSWORD env vars.
```

---

## API reference

| Method | Path | Purpose |
|--------|------|---------|
| GET  | `/api/v1/plans` | List active plans |
| GET  | `/api/v1/plans/{id}` | Get a plan |
| GET  | `/api/v1/plans/pricing` | Full Plan × Tier price matrix |
| GET  | `/api/v1/tiers` | List active tiers |
| GET  | `/api/v1/tiers/{id}` | Tier detail with resolved benefits |
| GET  | `/api/v1/tiers/{id}/benefits` | Resolved, configured benefits for a tier |
| POST | `/api/v1/subscriptions` | Subscribe to a (plan, tier) |
| GET  | `/api/v1/users/{id}/subscription` | Current active subscription |
| POST | `/api/v1/subscriptions/{id}/upgrade` | Upgrade tier (gated) |
| POST | `/api/v1/subscriptions/{id}/downgrade` | Downgrade tier |
| POST | `/api/v1/subscriptions/{id}/cancel` | Cancel |
| POST | `/api/v1/users/{id}/orders` | Record a (simulated) order signal |
| GET  | `/api/v1/users/{id}/orders` | Current-period order rollup |
| POST | `/api/v1/users/{id}/tier/evaluate` | Re-evaluate eligible tier |
| GET  | `/api/v1/users/{id}/tier/eligibility` | Current eligibility ceiling |
| POST | `/api/v1/admin/subscriptions/sweep` | Run expiry/renewal sweep on demand |
| POST | `/api/v1/admin/tiers/reevaluate` | Re-evaluate all users on demand |

---

## Demo walkthrough

```bash
BASE=http://localhost:8080/api/v1

# 1. Browse the catalog + pricing matrix
curl -s $BASE/plans/pricing | jq
curl -s $BASE/tiers/3 | jq          # Platinum with its benefits

# 2. Subscribe a new user to the base tier (Silver is always available)
curl -s -X POST $BASE/subscriptions -H 'Content-Type: application/json' \
  -d '{"userId":1,"planId":1,"tierId":1}' | jq

# 3. Gate in action: user 1 cannot jump to Gold (not earned yet)
curl -s -X POST $BASE/subscriptions/1/upgrade -H 'Content-Type: application/json' \
  -d '{"tierId":2}' | jq           # 409 "GOLD is not unlocked yet"

# 4a. Cohort fast-track: user 2 is PREMIUM → evaluation unlocks Gold
curl -s -X POST $BASE/users/2/tier/evaluate | jq
curl -s -X POST $BASE/subscriptions -H 'Content-Type: application/json' \
  -d '{"userId":2,"planId":2,"tierId":2}' | jq   # 201, Gold

# 4b. Spend path: 3×5000 orders unlock Platinum for user 1
for i in 1 2 3; do curl -s -X POST $BASE/users/1/orders \
  -H 'Content-Type: application/json' -d '{"amount":5000}' >/dev/null; done
curl -s -X POST $BASE/users/1/tier/evaluate | jq  # eligibleTier = PLATINUM
curl -s -X POST $BASE/subscriptions/1/upgrade -H 'Content-Type: application/json' \
  -d '{"tierId":3}' | jq           # now allowed → Platinum

# 5. Maintenance sweep (seeded past-due subs 100/101/102)
curl -s -X POST $BASE/admin/subscriptions/sweep | jq   # {processed:3, renewed:2, expired:1}
curl -s $BASE/users/6/subscription | jq   # auto-renewed but DOWNGRADED to Silver (no longer eligible)
curl -s $BASE/users/5/subscription        # 404 — expired (auto-renew off)
```

---

## Architecture

Modular Spring Boot monolith, package-by-feature, layered `Controller → Service → Repository`.
Two pluggable **Strategy** seams provide the extensibility the spec grades on:

- **`BenefitHandler`** — one per benefit type, interprets per-tier JSON metadata into a resolved
  benefit. New perk = new `@Component`, auto-registered.
- **`CriterionEvaluator`** — one per criterion type (order count / spend / cohort), decides if a user
  satisfies a tier's rule. New criterion = new `@Component`, auto-registered.

Other notable pieces: a consumer-owned **`TierEligibilityService`** gate (Task 4 stub → Task 6
evaluation-backed impl via `@Primary`); rich-domain `Subscription` with guarded state transitions;
scheduled maintenance (expiry/renewal + re-evaluation) with admin triggers; domain events
(`Renewed`/`Expired`/`TierChanged`) decoupling lifecycle side effects.

### Data model

`membership_plan`, `membership_tier`, `plan_tier_price`, `benefit`, `tier_benefit`,
`tier_criterion`, `user_account`, `subscription`, `user_order_stats`, `user_tier_status`.
Schema is generated by Hibernate `ddl-auto` and seeded by `data.sql` (H2/Postgres-portable).

---

## Concurrency

- **Optimistic locking** (`@Version`) on `subscription` — concurrent tier changes fail with 409
  `CONCURRENT_MODIFICATION`.
- **One active subscription per user** — enforced at the DB via a nullable-unique `active_user_key`
  (= userId while ACTIVE), race-proof even past the friendly app-layer pre-check.
- **Idempotent subscribe** — an `idempotencyKey` makes retries return the original subscription.

---

## Testing

```bash
mvn test
```

76 tests: unit tests for the benefit handlers, criterion evaluators, strict metadata accessor, and
the subscription state machine; plus end-to-end integration tests (`@SpringBootTest` + MockMvc over
H2) for the catalog, subscribe/cancel/idempotency/gate, tier evaluation, and the maintenance sweep.

---

## Notable design decisions

- **Plan × Tier price matrix** as the single source of subscription price.
- **Tiers/benefits/criteria are data** (configurable per tier), not code — add a tier or tune a perk
  without redeploying.
- **Eligibility gate is a hard authorization ceiling**; earned tiers never auto-bill.
- **Renewal re-checks eligibility**: keeps the tier if still earned, else auto-downgrades to the
  highest qualified tier — never an upcharge.
- Scope kept to core: no proration and no audit-history table (no payment system in scope).
