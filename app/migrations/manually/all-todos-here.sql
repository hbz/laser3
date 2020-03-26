-- add all migrations (for local and/or remote environments) here
-- add all migrations (for local and/or remote environments) here
-- add all migrations (for local and/or remote environments) here

-- yyyy-mm-dd
-- <short description>
-- changesets in changelog-yyyy-mm-dd.groovy

-- 2019-12-06
-- ERMS-1929
-- removing deprecated field impId, move ti_type_rv_fk to ti_medium_rv_fk
-- changesets in changelog-2020-03-26.groovy
--alter table package drop column pkg_identifier;
--alter table org drop column org_imp_id;
--alter table package drop column pkg_imp_id;
--alter table platform drop column plat_imp_id;
--alter table subscription drop column sub_imp_id;
--alter table title_instance drop column ti_imp_id;
--alter table title_instance_package_platform drop column tipp_imp_id;
--alter table title_instance rename ti_type_rv_fk to ti_medium_rv_fk;
--update refdata_value set rdv_value = 'Book' where rdv_value = 'EBook';
--update refdata_category set rdc_description = 'title.medium' where rdc_description = 'title.type';

-- 2019-12-10
-- ERMS-1901 (ERMS-1500)
-- org.name set not null with default "Name fehlt"
-- changesets in changelog-2020-03-26.groovy
--update org set org_name = 'Name fehlt!' where org_name is null;
--alter table org alter column org_name set default 'Name fehlt!';
--alter table org alter column org_name set not null;

-- 2020-01-23
-- ERMS-1901 (ERMS-1948): cleanup - set null values to generic null value, set gokbId as unique and not null, delete erroneous coverage data from ebooks and databases, delete column package_type_rv_fk
-- changesets in changelog-2020-03-26.groovy

-- delete from issue_entitlement_coverage where ic_ie_fk in (select ie_id from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id join title_instance ti on tipp_ti_fk = ti_id where class not like '%JournalInstance%');
-- ALTER TABLE title_instance ALTER COLUMN ti_gokb_id TYPE character varying(511);
-- alter table title_instance alter column ti_gokb_id set not null;
-- alter table title_instance add constraint unique_ti_gokb_id unique (ti_gokb_id);
-- update title_instance_package_platform set tipp_gokb_id = concat('generic.null.value.',tipp_id) where tipp_gokb_id is null;
-- alter table title_instance_package_platform alter column tipp_gokb_id type character varying(511);
-- alter table title_instance_package_platform alter column tipp_gokb_id set not null;
-- alter table title_instance_package_platform ADD CONSTRAINT unique_tipp_gokb_id UNIQUE (tipp_gokb_id);
-- update package set pkg_gokb_id = concat('generic.null.value',pkg_id) where pkg_gokb_id is null;
-- alter table package alter column pkg_gokb_id type character varying(511);
-- alter table package alter column pkg_gokb_id set not null;
-- alter table package ADD CONSTRAINT unique_pkg_gokb_id UNIQUE (pkg_gokb_id);
-- update platform set plat_gokb_id = concat('generic.null.value',plat_id) where plat_gokb_id is null;
-- alter table platform alter column plat_gokb_id type character varying(511);
-- alter table platform alter column plat_gokb_id set not null;
-- alter table platform ADD CONSTRAINT unique_plat_gokb_id UNIQUE (plat_gokb_id);

-- 2020-02-14
-- ERMS-1901 (ERMS-1957)
-- manually set platform and package data to correct one, drop tables title_institution_platform and core_assertion, drop legacy tables
-- changesets in changelog-2020-03-26.groovy

-- update org_access_point_link set platform_id = 5 where platform_id = 27;
-- update org_access_point_link set platform_id = 12 where platform_id in (30,59);
-- update org_access_point_link set platform_id = 98 where platform_id = 1;
-- update org_access_point_link set platform_id = 8 where platform_id = 62;
-- drop table core_assertion;
-- drop table title_institution_provider;
-- drop table global_record_info;
-- drop table global_record_tracker;

-- 2020-02-18
-- Add new Column to SurveyInfo
-- changesets in changelog-2020-03-05.groovy
-- alter table survey_info add surin_is_mandatory boolean;
-- update survey_info set surin_is_mandatory = true where surin_is_mandatory is null and surin_is_subscription_survey = true;

-- 2020-02-19
-- ERMS-2119
-- changesets in changelog-2020-03-26.groovy
-- ALTER TABLE subscription ADD COLUMN sub_has_perpetual_access boolean NOT NULL DEFAULT false;

-- 2020-02-27
-- ERMS-2107
-- changesets in changelog-2020-03-20.groovy
--update cost_item set ci_status_rv_fk = (select rdv_id from refdata_value where rdv_value = 'generic.null.value') where ci_status_rv_fk is null;
--ALTER TABLE public.cost_item ALTER COLUMN ci_status_rv_fk SET NOT NULL;

-- 2020-03-02
-- missing statement
-- changesets in changelog-2020-03-02.groovy

-- alter table cost_item drop column ci_include_in_subscr;

-- 2020-03-09
-- ERMS-2145
-- changesets in changelog-2020-03-09.groovy
-- alter table subscription add sub_kind_rv_fk bigint;

-- changesets in changelog-2020-03-10.groovy
-- UPDATE subscription SET sub_kind_rv_fk = (SELECT rdv_id FROM refdata_value WHERE
-- rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.kind')
--                                                                            AND rdv_value = 'Alliance Licence')
-- WHERE sub_type_rv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.type')
-- AND rdv_value = 'Alliance Licence');
-- UPDATE subscription SET sub_kind_rv_fk = (SELECT rdv_id FROM refdata_value WHERE
--        rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.kind')
--                                                                             AND rdv_value = 'National Licence')
-- WHERE sub_type_rv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.type')
--                                                           AND rdv_value = 'National Licence');
-- UPDATE subscription SET sub_kind_rv_fk = (SELECT rdv_id FROM refdata_value WHERE
--        rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.kind')
--                                                                             AND rdv_value = 'Consortial Licence')
-- WHERE sub_type_rv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.type')
--                                                         AND rdv_value = 'Consortial Licence');
-- UPDATE subscription SET sub_type_rv_fk = (SELECT rdv_id FROM refdata_value WHERE
--        rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.type')
--                                                                             AND rdv_value = 'Consortial Licence')
-- WHERE sub_type_rv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.type')
--                                                         AND rdv_value = 'National Licence');
-- UPDATE subscription SET sub_type_rv_fk = (SELECT rdv_id FROM refdata_value WHERE
--        rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.type')
--                                                                             AND rdv_value = 'Consortial Licence')
-- WHERE sub_type_rv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.type')
--                                                           AND rdv_value = 'Alliance Licence');
-- DELETE FROM refdata_value WHERE
--        rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.type')
--                            AND rdv_value = 'Alliance Licence';

-- DELETE FROM refdata_value WHERE
--        rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.type')
--                           AND rdv_value = 'National Licence';

-- 2020-03-09
-- ERMS-2159
-- changesets in changelog-2020-03-10.groovy
-- DELETE FROM refdata_value WHERE
--        rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.status')
--                           AND rdv_value = 'ExpiredPerennial';
-- DELETE FROM refdata_value WHERE
--        rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.status')
--                           AND rdv_value = 'IntendedPerennial';

-- 2020-03-12
-- bugfix correct camelcase
-- changesets in changelog-2020-03-13.groovy
-- update refdata_category set rdc_description = 'subjectgroup' where rdc_description = 'subjectGroup';
