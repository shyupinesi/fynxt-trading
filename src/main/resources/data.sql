-- ============================================================
-- FYNXT Trading Platform - Seed Data
-- ============================================================

-- Sample traders
MERGE INTO traders (id, name) KEY(id)
VALUES ('T001', 'Alice'),
       ('T002', 'Bob'),
       ('T003', 'Charlie');
