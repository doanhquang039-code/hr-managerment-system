CREATE TABLE IF NOT EXISTS collaboration_group_posts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    group_id INT NOT NULL,
    author_id INT NOT NULL,
    content VARCHAR(500) NOT NULL,
    type VARCHAR(30) NOT NULL DEFAULT 'UPDATE',
    pinned BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_collaboration_group_posts_group
        FOREIGN KEY (group_id) REFERENCES collaboration_group(id) ON DELETE CASCADE,
    CONSTRAINT fk_collaboration_group_posts_author
        FOREIGN KEY (author_id) REFERENCES user(id) ON DELETE CASCADE
);
