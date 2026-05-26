CREATE TABLE IF NOT EXISTS collaboration_group (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME,
    updated_at DATETIME
);

CREATE TABLE IF NOT EXISTS collaboration_group_members (
    group_id INT NOT NULL,
    user_id INT NOT NULL,
    PRIMARY KEY (group_id, user_id),
    CONSTRAINT fk_collaboration_group_members_group
        FOREIGN KEY (group_id) REFERENCES collaboration_group(id) ON DELETE CASCADE,
    CONSTRAINT fk_collaboration_group_members_user
        FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS collaboration_group_roles (
    group_id INT NOT NULL,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (group_id, role),
    CONSTRAINT fk_collaboration_group_roles_group
        FOREIGN KEY (group_id) REFERENCES collaboration_group(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS collaboration_group_features (
    group_id INT NOT NULL,
    feature VARCHAR(50) NOT NULL,
    PRIMARY KEY (group_id, feature),
    CONSTRAINT fk_collaboration_group_features_group
        FOREIGN KEY (group_id) REFERENCES collaboration_group(id) ON DELETE CASCADE
);

INSERT INTO collaboration_group (name, description, active, created_at)
SELECT 'HR Collaboration Group', 'Shared group for selected users across all roles.', TRUE, NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM collaboration_group WHERE name = 'HR Collaboration Group'
);

INSERT IGNORE INTO collaboration_group_features (group_id, feature)
SELECT id, 'DASHBOARD' FROM collaboration_group WHERE name = 'HR Collaboration Group';
INSERT IGNORE INTO collaboration_group_features (group_id, feature)
SELECT id, 'MEMBERS' FROM collaboration_group WHERE name = 'HR Collaboration Group';
INSERT IGNORE INTO collaboration_group_features (group_id, feature)
SELECT id, 'NOTES' FROM collaboration_group WHERE name = 'HR Collaboration Group';

INSERT IGNORE INTO collaboration_group_roles (group_id, role)
SELECT id, 'ADMIN' FROM collaboration_group WHERE name = 'HR Collaboration Group';
INSERT IGNORE INTO collaboration_group_roles (group_id, role)
SELECT id, 'MANAGER' FROM collaboration_group WHERE name = 'HR Collaboration Group';
INSERT IGNORE INTO collaboration_group_roles (group_id, role)
SELECT id, 'HIRING' FROM collaboration_group WHERE name = 'HR Collaboration Group';
INSERT IGNORE INTO collaboration_group_roles (group_id, role)
SELECT id, 'USER' FROM collaboration_group WHERE name = 'HR Collaboration Group';
