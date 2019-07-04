
-- 2019-06-07
-- Change surconf_pickAndChoose to surconf_pickandchoose

-- ALTER TABLE public.survey_config RENAME COLUMN "surconf_pickAndChoose" TO surconf_pickandchoose;


-- 2019-06-07
-- Deleting orphaned links for licenses and subscriptions

--SELECT * FROM links
--WHERE l_object = 'com.k_int.kbplus.License'
--  AND ( (l_source_fk not in (SELECT lic_id FROM license))
--    OR (l_destination_fk not in (SELECT lic_id FROM license))
--    );

--SELECT * FROM links
--WHERE l_object = 'com.k_int.kbplus.Subscription'
--  AND ( (l_source_fk not in (SELECT sub_id FROM subscription))
--    OR (l_destination_fk not in (SELECT sub_id FROM subscription))
--    );


-- ERMS-1103/ERMS-1181
-- 2019-06-13
-- New column sub_is_administrative for public.subscription

--alter table subscription add column sub_is_administrative bool not null default false;


-- ERMS-1412
-- 2019-06-13
-- Drop column createdBy/lastUpdatedBy

--alter table cost_item drop column last_updated_by_id;
--alter table cost_item drop column created_by_id;


-- Rename refdata value
-- ERMS-1418
-- 2019-06-25

--update refdata_value set rdv_value = 'Responsible Admin' where rdv_value = 'Responsible Contact';


-- ERMS-1428
-- 2019-06-26
-- Rename idns_non_unique to idns_unique

--alter table identifier_namespace add idns_unique boolean; -- not null;
--update identifier_namespace set idns_unique = false where idns_non_unique = true;
--update identifier_namespace set idns_unique = true where (idns_non_unique = false or idns_non_unique is null);


-- ERMS-884
-- 2019-06-27
-- Change data type of sums from double precision to numeric
-- suspended UFN
--alter table cost_item alter column ci_cost_in_local_currency type numeric;
--alter table cost_item alter column ci_cost_in_billing_currency type numeric;
--alter table cost_item alter column ci_currency_rate type numeric;

-- ERMS-1298
-- 2019-06-25
-- merge refdata categories Entitlement Issue Status into TIPP Status
--OLD: update issue_entitlement set ie_status_rv_fk = 367 where ie_status_rv_fk = 295 or ie_status_rv_fk = 808;
--OLD: update issue_entitlement set ie_status_rv_fk = 369 where ie_status_rv_fk = 296;
--OLD: delete from refdata_value where rdv_owner = 54;
--OLD: delete from refdata_category where rdc_id = 54;

-- update issue_entitlement set ie_status_rv_fk = (
--     SELECT v.rdv_id FROM refdata_value v JOIN refdata_category c ON (v.rdv_owner = c.rdc_id) WHERE c.rdc_description = 'TIPP Status' and v.rdv_value = 'Current' -- 369
-- ) where ie_status_rv_fk in (
--     SELECT v.rdv_id FROM refdata_value v JOIN refdata_category c ON (v.rdv_owner = c.rdc_id) WHERE c.rdc_description = 'Entitlement Issue Status' and (
--                 v.rdv_value = 'Live' or v.rdv_value = 'Current'-- 296 or 808
--         )
-- );
--
-- update issue_entitlement set ie_status_rv_fk = (
--     SELECT v.rdv_id FROM refdata_value v JOIN refdata_category c ON (v.rdv_owner = c.rdc_id) WHERE c.rdc_description = 'TIPP Status' and v.rdv_value = 'Deleted' -- 369
-- ) where ie_status_rv_fk = (
--     SELECT v.rdv_id FROM refdata_value v JOIN refdata_category c ON (v.rdv_owner = c.rdc_id) WHERE c.rdc_description = 'Entitlement Issue Status' and v.rdv_value = 'Deleted' -- 296
-- );
--
-- delete from refdata_value where rdv_owner = (
--     SELECT rdc_id FROM refdata_category WHERE rdc_description = 'Entitlement Issue Status'
-- );
--
-- delete from refdata_category where rdc_id = (
--     SELECT rdc_id FROM refdata_category WHERE rdc_description = 'Entitlement Issue Status'
-- );
