CREATE TABLE IF NOT EXISTS collaboration_group_tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    group_id INT NOT NULL,
    created_by INT NOT NULL,
    assignee_id INT NULL,
    title VARCHAR(160) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'TODO',
    due_date DATE NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL,
    CONSTRAINT fk_collaboration_group_tasks_group
        FOREIGN KEY (group_id) REFERENCES collaboration_group(id) ON DELETE CASCADE,
    CONSTRAINT fk_collaboration_group_tasks_creator
        FOREIGN KEY (created_by) REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT fk_collaboration_group_tasks_assignee
        FOREIGN KEY (assignee_id) REFERENCES user(id) ON DELETE SET NULL
);
