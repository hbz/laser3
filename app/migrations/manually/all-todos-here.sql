-- add all migrations (for local and/or remote environments) here
-- add all migrations (for local and/or remote environments) here
-- add all migrations (for local and/or remote environments) here

-- yyyy-mm-dd
-- <short description>

-----------------------------------------------------------------------------------------------------------------------

-- 2020-02-18
-- Add new Column to SurveyInfo
alter table survey_info add surin_is_mandatory boolean;
update survey_info set surin_is_mandatory = true where surin_is_mandatory is null and surin_is_subscription_survey = true;


-- 2020-02-19
-- ERMS-2119
-- changesets in changelog-2020-02-19.groovy

-- ALTER TABLE subscription ADD COLUMN sub_has_perpetual_access boolean NOT NULL DEFAULT false;

-- 2020-03-02
-- missing statement
-- changesets in changelog-2020-03-02.groovy

alter table cost_item drop column ci_include_in_subscr;