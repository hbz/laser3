
-- Change surconf_pickAndChoose to surconf_pickandchoose
-- 2019-06-07

-- ALTER TABLE public.survey_config RENAME COLUMN "surconf_pickAndChoose" TO surconf_pickandchoose;


-- Deleting orphaned links for licenses and subscriptions
-- 2019-06-07

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

-- New column sub_is_administrative for public.subscription
-- ERMS-1103/ERMS-1181
-- 2019-06-13
alter table subscription add column sub_is_administrative bool not null default false;

-- Drop column createdBy/lastUpdatedBy
-- ERMS-1412
-- 2019-06-13
alter table cost_item drop column last_updated_by_id;
alter table cost_item drop column created_by_id;