-- NOTE: collaboration_group_roles table was already created in V29.
-- This migration only seeds additional default roles if missing.

INSERT IGNORE INTO collaboration_group_roles (group_id, role)
SELECT id, 'ADMIN' FROM collaboration_group WHERE name = 'HR Collaboration Group';
INSERT IGNORE INTO collaboration_group_roles (group_id, role)
SELECT id, 'MANAGER' FROM collaboration_group WHERE name = 'HR Collaboration Group';
INSERT IGNORE INTO collaboration_group_roles (group_id, role)
SELECT id, 'HIRING' FROM collaboration_group WHERE name = 'HR Collaboration Group';
INSERT IGNORE INTO collaboration_group_roles (group_id, role)
SELECT id, 'USER' FROM collaboration_group WHERE name = 'HR Collaboration Group';
