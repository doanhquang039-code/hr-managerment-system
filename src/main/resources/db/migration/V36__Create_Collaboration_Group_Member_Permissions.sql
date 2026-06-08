CREATE TABLE IF NOT EXISTS collaboration_group_member_permissions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    group_id INT NOT NULL,
    user_id INT NOT NULL,
    feature VARCHAR(50) NOT NULL,
    CONSTRAINT fk_collaboration_group_member_permissions_group
        FOREIGN KEY (group_id) REFERENCES collaboration_group(id) ON DELETE CASCADE,
    CONSTRAINT fk_collaboration_group_member_permissions_user
        FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT uk_collaboration_group_member_feature UNIQUE (group_id, user_id, feature)
);
