
-- 2019-07-09
-- remove role ROLE_DATAMANAGER

--DELETE FROM user_role WHERE role_id = (
--    SELECT id FROM role WHERE authority = 'ROLE_DATAMANAGER'
--);

--DELETE FROM role WHERE authority = 'ROLE_DATAMANAGER';


-- 2019-07-11
-- remove license and subscription status

-- DELETE FROM refdata_value WHERE rdv_id IN (
--     SELECT rdv.rdv_id
--     FROM refdata_value rdv
--              JOIN refdata_category rdc on rdv.rdv_owner = rdc.rdc_id
--     WHERE rdv.rdv_value = 'Deleted'
--       AND rdc.rdc_description = 'Subscription Status'
-- );
--
-- DELETE FROM refdata_value WHERE rdv_id IN (
--     SELECT rdv.rdv_id
--     FROM refdata_value rdv
--              JOIN refdata_category rdc on rdv.rdv_owner = rdc.rdc_id
--     WHERE rdv.rdv_value = 'Deleted'
--       AND rdc.rdc_description = 'License Status'
-- );
