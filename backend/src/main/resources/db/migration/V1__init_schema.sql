CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    technician_code VARCHAR(50) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    nip VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_technician_code (technician_code),
    UNIQUE KEY uk_users_nip (nip),
    KEY idx_users_role (role),
    KEY idx_users_active (active),
    CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'TECHNICIAN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE work_lists (
    id BIGINT NOT NULL AUTO_INCREMENT,
    technician_id BIGINT NOT NULL,
    list_type VARCHAR(10) NOT NULL,
    initial_drop_count INT NOT NULL,
    reference_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    completion_date DATE NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    in_progress_owner_id BIGINT GENERATED ALWAYS AS (
        CASE WHEN status = 'IN_PROGRESS' THEN technician_id ELSE NULL END
    ) STORED,
    PRIMARY KEY (id),
    CONSTRAINT fk_work_lists_technician FOREIGN KEY (technician_id) REFERENCES users (id),
    CONSTRAINT chk_work_lists_list_type CHECK (list_type IN ('OBD', 'DPO')),
    CONSTRAINT chk_work_lists_status CHECK (status IN ('IN_PROGRESS', 'FINISHED')),
    CONSTRAINT chk_work_lists_initial_drop_count CHECK (initial_drop_count > 0),
    UNIQUE KEY uk_work_lists_one_in_progress (in_progress_owner_id),
    KEY idx_work_lists_technician_status (technician_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE daily_progress (
    id BIGINT NOT NULL AUTO_INCREMENT,
    work_list_id BIGINT NOT NULL,
    technician_id BIGINT NOT NULL,
    work_date DATE NOT NULL,
    completed_drops INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_daily_progress_work_list FOREIGN KEY (work_list_id) REFERENCES work_lists (id) ON DELETE CASCADE,
    CONSTRAINT fk_daily_progress_technician FOREIGN KEY (technician_id) REFERENCES users (id),
    CONSTRAINT chk_daily_progress_completed CHECK (completed_drops > 0),
    UNIQUE KEY uk_daily_progress_list_date (work_list_id, work_date),
    KEY idx_daily_progress_technician_date (technician_id, work_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE day_off_requests (
    id BIGINT NOT NULL AUTO_INCREMENT,
    technician_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    number_of_days INT NOT NULL,
    reason VARCHAR(500) NULL,
    status VARCHAR(20) NOT NULL,
    admin_comment VARCHAR(500) NULL,
    approved_by BIGINT NULL,
    created_at DATETIME(6) NOT NULL,
    decision_date DATE NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_day_off_technician FOREIGN KEY (technician_id) REFERENCES users (id),
    CONSTRAINT fk_day_off_approved_by FOREIGN KEY (approved_by) REFERENCES users (id),
    CONSTRAINT chk_day_off_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT chk_day_off_dates CHECK (start_date <= end_date),
    CONSTRAINT chk_day_off_number_of_days CHECK (number_of_days > 0),
    KEY idx_day_off_technician (technician_id),
    KEY idx_day_off_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
