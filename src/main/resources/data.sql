-- Seed data for the FirstClub Membership Program.
-- Runs after Hibernate creates the schema (spring.jpa.defer-datasource-initialization=true).
-- Column names match Hibernate's snake_case physical naming for the entities.

-- ---------------------------------------------------------------------------
-- Demo users (stub identities) so subscriptions can be created in the demo.
-- ---------------------------------------------------------------------------
INSERT INTO user_account (id, external_id, email, cohort, created_at) VALUES
  (1, 'ext-1001', 'alice@example.com', 'REGULAR', CURRENT_TIMESTAMP),
  (2, 'ext-1002', 'bob@example.com',   'PREMIUM', CURRENT_TIMESTAMP),
  (3, 'ext-1003', 'carol@example.com', 'REGULAR', CURRENT_TIMESTAMP),
  -- Users 4-6 anchor seeded past-due subscriptions for the maintenance-sweep demo.
  (4, 'ext-1004', 'dave@example.com',  'REGULAR', CURRENT_TIMESTAMP),
  (5, 'ext-1005', 'erin@example.com',  'REGULAR', CURRENT_TIMESTAMP),
  (6, 'ext-1006', 'frank@example.com', 'REGULAR', CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------------------
-- Plans (billing axis): cadence + duration. `price` is a reference "from" price.
-- ---------------------------------------------------------------------------
INSERT INTO membership_plan (id, code, name, billing_period, duration_days, price, currency, active, created_at) VALUES
  (1, 'MONTHLY',   'Monthly',   'MONTHLY',    30,   99.00, 'INR', TRUE, CURRENT_TIMESTAMP),
  (2, 'QUARTERLY', 'Quarterly', 'QUARTERLY',  90,  269.00, 'INR', TRUE, CURRENT_TIMESTAMP),
  (3, 'YEARLY',    'Yearly',    'YEARLY',    365,  999.00, 'INR', TRUE, CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------------------
-- Tiers (benefit axis): rank defines upgrade/downgrade order (higher = premium).
-- ---------------------------------------------------------------------------
INSERT INTO membership_tier (id, code, name, tier_rank, description, active, created_at) VALUES
  (1, 'SILVER',   'Silver',   1, 'Entry tier: free delivery on eligible orders.',                 TRUE, CURRENT_TIMESTAMP),
  (2, 'GOLD',     'Gold',     2, 'Mid tier: higher discounts and early access to sales.',         TRUE, CURRENT_TIMESTAMP),
  (3, 'PLATINUM', 'Platinum', 3, 'Top tier: maximum discounts, faster delivery, priority support.', TRUE, CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------------------
-- Plan x Tier price matrix (authoritative subscription price).
-- ---------------------------------------------------------------------------
INSERT INTO plan_tier_price (id, plan_id, tier_id, price, currency, active) VALUES
  -- Monthly
  (1, 1, 1,   99.00, 'INR', TRUE),
  (2, 1, 2,  199.00, 'INR', TRUE),
  (3, 1, 3,  399.00, 'INR', TRUE),
  -- Quarterly
  (4, 2, 1,  269.00, 'INR', TRUE),
  (5, 2, 2,  539.00, 'INR', TRUE),
  (6, 2, 3, 1079.00, 'INR', TRUE),
  -- Yearly
  (7, 3, 1,  999.00, 'INR', TRUE),
  (8, 3, 2, 1999.00, 'INR', TRUE),
  (9, 3, 3, 3999.00, 'INR', TRUE);

-- ---------------------------------------------------------------------------
-- Benefit catalog (master list of perk types).
-- ---------------------------------------------------------------------------
INSERT INTO benefit (id, code, type, description, active, created_at) VALUES
  (1, 'FREE_DELIVERY',    'FREE_DELIVERY',       'Free delivery on eligible orders',          TRUE, CURRENT_TIMESTAMP),
  (2, 'EXTRA_DISCOUNT',   'PERCENTAGE_DISCOUNT', 'Extra discount on selected items',          TRUE, CURRENT_TIMESTAMP),
  (3, 'EARLY_ACCESS',     'EARLY_ACCESS',        'Early access to sales and exclusive deals', TRUE, CURRENT_TIMESTAMP),
  (4, 'PRIORITY_SUPPORT', 'PRIORITY_SUPPORT',    'Priority customer support',                 TRUE, CURRENT_TIMESTAMP);

-- ---------------------------------------------------------------------------
-- Tier -> benefit links with per-tier metadata. Same benefit, different config
-- per tier (e.g. free-delivery threshold, discount %) demonstrates configurability.
-- ---------------------------------------------------------------------------
INSERT INTO tier_benefit (id, tier_id, benefit_id, benefit_metadata) VALUES
  -- Silver: free delivery above a threshold only
  (1, 1, 1, '{"minOrderValue": 500}'),
  -- Gold: unconditional free delivery, 5% discount, 12h early access
  (2, 2, 1, '{"minOrderValue": 0}'),
  (3, 2, 2, '{"percentage": 5, "appliesTo": "SELECTED_ITEMS"}'),
  (4, 2, 3, '{"earlyAccessHours": 12}'),
  -- Platinum: free delivery, 10% on everything, 24h early access, phone support
  (5, 3, 1, '{"minOrderValue": 0}'),
  (6, 3, 2, '{"percentage": 10, "appliesTo": "ALL_ITEMS"}'),
  (7, 3, 3, '{"earlyAccessHours": 24}'),
  (8, 3, 4, '{"channel": "PHONE", "slaHours": 4}');

-- ---------------------------------------------------------------------------
-- Tier eligibility criteria. Silver (base) has none → always eligible.
-- Multiple criteria on a tier qualify on ANY one (OR): e.g. Gold via 5+ orders
-- OR 3000+ spend OR PREMIUM cohort. threshold = inclusive minimum.
-- ---------------------------------------------------------------------------
INSERT INTO tier_criterion (id, tier_id, type, threshold, criterion_metadata) VALUES
  -- Gold (rank 2)
  (1, 2, 'ORDER_COUNT',   5,    NULL),
  (2, 2, 'MONTHLY_SPEND', 3000, NULL),
  (3, 2, 'COHORT',        NULL, '{"cohorts": ["PREMIUM"]}'),
  -- Platinum (rank 3)
  (4, 3, 'ORDER_COUNT',   15,    NULL),
  (5, 3, 'MONTHLY_SPEND', 10000, NULL);

-- ---------------------------------------------------------------------------
-- Past-due ACTIVE subscriptions (end_date in the past) to demo the maintenance sweep:
--   100: user 4, auto-renew, Silver  -> renews, keeps Silver (still eligible)
--   101: user 5, no auto-renew, Silver -> expires
--   102: user 6, auto-renew, Gold    -> renews but DOWNGRADES to Silver (no longer eligible)
-- High ids avoid colliding with API-created subscriptions (which start at 1).
-- ---------------------------------------------------------------------------
INSERT INTO subscription
  (id, user_id, plan_id, tier_id, status, start_date, end_date, price, currency, auto_renew, active_user_key, idempotency_key, version, created_at, updated_at) VALUES
  (100, 4, 1, 1, 'ACTIVE', DATE '2020-01-01', DATE '2020-01-31',  99.00, 'INR', TRUE,  4, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (101, 5, 1, 1, 'ACTIVE', DATE '2020-01-01', DATE '2020-01-31',  99.00, 'INR', FALSE, 5, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (102, 6, 1, 2, 'ACTIVE', DATE '2020-01-01', DATE '2020-01-31', 199.00, 'INR', TRUE,  6, NULL, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
