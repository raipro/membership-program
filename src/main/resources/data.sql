-- Seed data for the FirstClub Membership Program.
-- Runs after Hibernate creates the schema (spring.jpa.defer-datasource-initialization=true).
-- Column names match Hibernate's snake_case physical naming for the entities.

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
