
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
update refdata_value set rdv_value = 'Responsible Admin' where rdv_value = 'Responsible Contact';


-- ERMS-1428
-- 2019-06-26
-- Rename idns_non_unique to idns_unique

alter table identifier_namespace add idns_unique boolean; -- not null;
update identifier_namespace set idns_unique = false where idns_non_unique = true;
update identifier_namespace set idns_unique = true where (idns_non_unique = false or idns_non_unique is null);


-- ERMS-1298
-- 2019-06-25
-- merge refdata categories Entitlement Issue Status into TIPP Status
update issue_entitlement set ie_status_rv_fk = 367 where ie_status_rv_fk = 295 or ie_status_rv_fk = 808;
update issue_entitlement set ie_status_rv_fk = 369 where ie_status_rv_fk = 296;
delete from refdata_value where rdv_owner = 54;
delete from refdata_category where rdc_id = 54;