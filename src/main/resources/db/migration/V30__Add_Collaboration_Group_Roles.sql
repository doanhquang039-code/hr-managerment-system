CREATE TABLE IF NOT EXISTS collaboration_group_roles (
    group_id INT NOT NULL,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (group_id, role),
    CONSTRAINT fk_collaboration_group_roles_group
        FOREIGN KEY (group_id) REFERENCES collaboration_group(id) ON DELETE CASCADE
);

INSERT IGNORE INTO collaboration_group_roles (group_id, role)
SELECT id, 'ADMIN' FROM collaboration_group WHERE name = 'HR Collaboration Group';
INSERT IGNORE INTO collaboration_group_roles (group_id, role)
SELECT id, 'MANAGER' FROM collaboration_group WHERE name = 'HR Collaboration Group';
INSERT IGNORE INTO collaboration_group_roles (group_id, role)
SELECT id, 'HIRING' FROM collaboration_group WHERE name = 'HR Collaboration Group';
INSERT IGNORE INTO collaboration_group_roles (group_id, role)
SELECT id, 'USER' FROM collaboration_group WHERE name = 'HR Collaboration Group';
