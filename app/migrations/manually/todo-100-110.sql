-- 2019-09-04 (gorm problem)

ALTER TABLE task ADD tsk_system_create_date timestamp;
UPDATE task SET tsk_system_create_date = tsk_create_date where task.tsk_system_create_date isnull;
ALTER TABLE task ALTER COLUMN tsk_system_create_date SET NOT NULL;

-- 2019-09-23 (add done-status to due date)
ALTER TABLE dashboard_due_date ADD COLUMN IF NOT EXISTS das_is_done boolean DEFAULT false;
ALTER TABLE dashboard_due_date ADD COLUMN IF NOT EXISTS das_is_hide boolean DEFAULT false;
