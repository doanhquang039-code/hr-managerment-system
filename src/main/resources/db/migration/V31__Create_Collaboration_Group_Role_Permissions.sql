CREATE TABLE IF NOT EXISTS collaboration_group_role_permissions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    group_id INT NOT NULL,
    role VARCHAR(50) NOT NULL,
    feature VARCHAR(50) NOT NULL,
    CONSTRAINT fk_collaboration_group_role_permissions_group
        FOREIGN KEY (group_id) REFERENCES collaboration_group(id) ON DELETE CASCADE,
    CONSTRAINT uk_collaboration_group_role_feature UNIQUE (group_id, role, feature)
);

INSERT IGNORE INTO collaboration_group_role_permissions (group_id, role, feature)
SELECT r.group_id, r.role, f.feature
FROM collaboration_group_roles r
JOIN collaboration_group_features f ON f.group_id = r.group_id;
