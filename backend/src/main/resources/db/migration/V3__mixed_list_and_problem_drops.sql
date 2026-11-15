ALTER TABLE work_lists
    ADD COLUMN problem_drop_count INT NOT NULL DEFAULT 0,
    ADD COLUMN problem_note VARCHAR(500) NULL;

ALTER TABLE work_lists
    DROP CHECK chk_work_lists_list_type;

ALTER TABLE work_lists
    ADD CONSTRAINT chk_work_lists_list_type CHECK (list_type IN ('OBD', 'DPO', 'OBD_DPO'));

ALTER TABLE work_lists
    ADD CONSTRAINT chk_work_lists_problem_drop_count CHECK (problem_drop_count >= 0);
