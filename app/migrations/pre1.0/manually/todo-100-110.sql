-- 2019-09-04 (gorm problem) -> changelog-100
-- ALTER TABLE task ADD tsk_system_create_date timestamp;
-- UPDATE task SET tsk_system_create_date = tsk_create_date where task.tsk_system_create_date isnull;
-- ALTER TABLE task ALTER COLUMN tsk_system_create_date SET NOT NULL;

-- 2019-09-23 (add done-status to due date) -> changelog-100
-- ALTER TABLE dashboard_due_date ADD COLUMN IF NOT EXISTS das_is_done boolean DEFAULT false;
-- ALTER TABLE dashboard_due_date ADD COLUMN IF NOT EXISTS das_is_hide boolean DEFAULT false;

-- 2019-09-24 (Set accept status Fixed to all IEs with status current ) changelog-100
-- UPDATE issue_entitlement SET ie_accept_status_rv_fk = (SELECT rdv.rdv_id FROM refdata_value rdv
-- JOIN refdata_category rdc ON rdv.rdv_owner = rdc.rdc_id
-- WHERE rdv.rdv_value = 'Fixed' AND rdc.rdc_description = 'IE Accept Status') where
-- ie_id IN (SELECT ie_id FROM issue_entitlement JOIN refdata_value rv ON issue_entitlement.ie_status_rv_fk = rv.rdv_id
-- WHERE rdv_value = 'Current')

--- 2019-09-16 -> changelog-100
--- ERMS-1490
--- correct price item column mappigs

--ALTER TABLE price_item RENAME pi_local_currency_rv_fk  TO pi_list_currency_rv_fk;
--ALTER TABLE price_item RENAME local_currency_id  TO pi_local_currency_rv_fk;

-- ALTER TABLE price_item ALTER COLUMN version DROP NOT NULL;
-- ALTER TABLE price_item ALTER COLUMN pi_list_currency_rv_fk DROP NOT NULL;
-- ALTER TABLE price_item ALTER COLUMN pi_list_price DROP NOT NULL;
-- ALTER TABLE price_item ALTER COLUMN pi_local_currency_rv_fk DROP NOT NULL;
-- ALTER TABLE price_item ALTER COLUMN pi_local_price DROP NOT NULL;
-- ALTER TABLE price_item ALTER COLUMN pi_price_date DROP NOT NULL;

-- 2019-09-30 -> changelog-101
-- add coloums to surveyResult
-- alter table survey_result add surre_is_required boolean;
-- UPDATE survey_result set surre_is_required = false where surre_is_required is null;
