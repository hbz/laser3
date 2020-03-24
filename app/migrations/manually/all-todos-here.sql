-- add all migrations (for local and/or remote environments) here
-- add all migrations (for local and/or remote environments) here
-- add all migrations (for local and/or remote environments) here

-- yyyy-mm-dd
-- <short description>
-- changesets in changelog-yyyy-mm-dd.groovy

-----------------------------------------------------------------------------------------------------------------------

-- 2020-02-18
-- Add new Column to SurveyInfo
-- changesets in changelog-2020-03-05.groovy
-- alter table survey_info add surin_is_mandatory boolean;
-- update survey_info set surin_is_mandatory = true where surin_is_mandatory is null and surin_is_subscription_survey = true;


-- 2020-02-19
-- ERMS-2119
-- changesets in changelog-2020-02-19.groovy
-- ALTER TABLE subscription ADD COLUMN sub_has_perpetual_access boolean NOT NULL DEFAULT false;

-- 2020-02-27
-- ERMS-2107
-- changesets in changelog-2020-03-20.groovy
--update cost_item set ci_status_rv_fk = (select rdv_id from refdata_value where rdv_value = 'generic.null.value') where ci_status_rv_fk is null;
--ALTER TABLE public.cost_item ALTER COLUMN ci_status_rv_fk SET NOT NULL;

-- 2020-03-02
-- missing statement
-- changesets in changelog-2020-03-02.groovy

alter table cost_item drop column ci_include_in_subscr;

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
