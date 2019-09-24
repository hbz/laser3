-- 2019-09-04 (gorm problem)

ALTER TABLE task ADD tsk_system_create_date timestamp;
UPDATE task SET tsk_system_create_date = tsk_create_date where task.tsk_system_create_date isnull;
ALTER TABLE task ALTER COLUMN tsk_system_create_date SET NOT NULL;

-- 2019-09-23 (add done-status to due date)
ALTER TABLE dashboard_due_date ADD COLUMN IF NOT EXISTS das_is_done boolean DEFAULT false;
ALTER TABLE dashboard_due_date ADD COLUMN IF NOT EXISTS das_is_hide boolean DEFAULT false;

-- 2019-09-24 (Set accept status Fixed to all IEs with status current )
UPDATE issue_entitlement SET ie_accept_status_rv_fk = (SELECT rdv.rdv_id FROM refdata_value rdv
    JOIN refdata_category rdc ON rdv.rdv_owner = rdc.rdc_id
WHERE rdv.rdv_value = 'Fixed' AND rdc.rdc_description = 'IE Accept Status') where
ie_id IN (SELECT ie_id FROM issue_entitlement JOIN refdata_value rv ON issue_entitlement.ie_status_rv_fk = rv.rdv_id
WHERE rdv_value = 'Current')