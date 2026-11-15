CREATE TABLE admin_notifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    type VARCHAR(40) NOT NULL,
    title VARCHAR(150) NOT NULL,
    message VARCHAR(500) NOT NULL,
    technician_id BIGINT NULL,
    work_list_id BIGINT NULL,
    read_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_admin_notifications_technician FOREIGN KEY (technician_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_admin_notifications_work_list FOREIGN KEY (work_list_id) REFERENCES work_lists (id) ON DELETE SET NULL,
    CONSTRAINT chk_admin_notifications_type CHECK (type IN ('LIST_CREATED')),
    KEY idx_admin_notifications_created (created_at),
    KEY idx_admin_notifications_unread (read_at, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
