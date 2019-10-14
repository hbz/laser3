
-- 2019-08-01
-- migrate refdataValues (category='YN') to boolean

--ALTER TABLE license ALTER COLUMN lic_is_slaved DROP DEFAULT;
--ALTER TABLE license DROP CONSTRAINT fk9f08441e07d095a;
--ALTER TABLE license ALTER lic_is_slaved TYPE bool USING CASE WHEN lic_is_slaved=1 THEN TRUE ELSE FALSE END;

--ALTER TABLE license ALTER COLUMN lic_is_public_rdv_fk DROP DEFAULT;
--ALTER TABLE license DROP CONSTRAINT fk9f084413d2aceb;
--ALTER TABLE license ALTER lic_is_public_rdv_fk TYPE bool USING CASE WHEN lic_is_public_rdv_fk=1 THEN TRUE ELSE FALSE END;
--ALTER TABLE license RENAME lic_is_public_rdv_fk TO lic_is_public;

--ALTER TABLE subscription ALTER COLUMN sub_is_slaved DROP DEFAULT;
--ALTER TABLE subscription DROP CONSTRAINT fk1456591d2d814494;
--ALTER TABLE subscription ALTER sub_is_slaved TYPE bool USING CASE WHEN sub_is_slaved=1 THEN TRUE ELSE FALSE END;

--ALTER TABLE subscription ALTER COLUMN sub_is_public DROP DEFAULT;
--ALTER TABLE subscription DROP CONSTRAINT fk1456591d28e1dd90;
--ALTER TABLE subscription ALTER sub_is_public TYPE bool USING CASE WHEN sub_is_public=1 THEN TRUE ELSE FALSE END;

--ALTER TABLE package ALTER COLUMN pkg_is_public DROP DEFAULT;
--ALTER TABLE package DROP CONSTRAINT fkcfe53446f8dfd21c;
--ALTER TABLE package ALTER pkg_is_public TYPE bool USING CASE WHEN pkg_is_public=1 THEN TRUE ELSE FALSE END;

--ALTER TABLE person ALTER COLUMN prs_is_public_rv_fk DROP DEFAULT;
--ALTER TABLE person DROP CONSTRAINT fkc4e39b55750b1c62;
--ALTER TABLE person ALTER prs_is_public_rv_fk TYPE bool USING CASE WHEN prs_is_public_rv_fk=1 THEN TRUE ELSE FALSE END;
--ALTER TABLE person RENAME prs_is_public_rv_fk TO prs_is_public;


-- 2019-08-02
-- naming conventions

--ALTER TABLE refdata_category RENAME rdv_hard_data TO rdc_is_hard_data;
--ALTER TABLE refdata_value RENAME rdv_hard_data TO rdv_is_hard_data;

--ALTER TABLE identifier_namespace RENAME idns_hide TO idns_is_hidden;
--ALTER TABLE identifier_namespace RENAME idns_unique TO idns_is_unique;


-- 2019-08-06
-- migrate refdataValues (category='YN') to boolean

--ALTER TABLE property_definition_group_binding ALTER COLUMN pbg_visible_rv_fk DROP DEFAULT;
--ALTER TABLE property_definition_group_binding DROP CONSTRAINT fk3d279603de1d7a3a;
--ALTER TABLE property_definition_group_binding ALTER pbg_visible_rv_fk TYPE bool USING CASE WHEN pbg_visible_rv_fk=1 THEN TRUE ELSE FALSE END;
--ALTER TABLE property_definition_group_binding RENAME pbg_visible_rv_fk TO pbg_is_visible;

--ALTER TABLE property_definition_group_binding ALTER COLUMN pbg_is_viewable_rv_fk DROP DEFAULT;
--ALTER TABLE property_definition_group_binding DROP CONSTRAINT fk3d27960376a2fe3c;
--ALTER TABLE property_definition_group_binding ALTER pbg_is_viewable_rv_fk TYPE bool USING CASE WHEN pbg_is_viewable_rv_fk=1 THEN TRUE ELSE FALSE END;
--ALTER TABLE property_definition_group_binding RENAME pbg_is_viewable_rv_fk TO pbg_is_visible_for_cons_member;

--ALTER TABLE property_definition_group ALTER COLUMN pdg_visible_rv_fk DROP DEFAULT;
--ALTER TABLE property_definition_group DROP CONSTRAINT fked224dbda14135f8;
--ALTER TABLE property_definition_group ALTER pdg_visible_rv_fk TYPE bool USING CASE WHEN pdg_visible_rv_fk=1 THEN TRUE ELSE FALSE END;
--ALTER TABLE property_definition_group RENAME pdg_visible_rv_fk TO pdg_is_visible;


-- 2019-08-14
-- migrate coverage dates from main TIPP/IssueEntitlement object into deployed TIPPCoverage/IssueEntitlementCoverage objects

--insert into tippcoverage (tc_version,tc_start_date,tc_start_volume,tc_start_issue,tc_end_date,tc_end_volume,tc_end_issue,tc_coverage_depth,tc_coverage_note,tc_embargo,tc_tipp_fk)
--select tipp_version,tipp_start_date,tipp_start_volume,tipp_start_issue,tipp_end_date,tipp_end_volume,tipp_end_issue,tipp_coverage_depth,tipp_coverage_note,tipp_embargo,tipp_id from title_instance_package_platform;
--alter table title_instance_package_platform drop column tipp_start_date;
--alter table title_instance_package_platform drop column tipp_start_volume;
--alter table title_instance_package_platform drop column tipp_start_issue;
--alter table title_instance_package_platform drop column tipp_end_date;
--alter table title_instance_package_platform drop column tipp_end_volume;
--alter table title_instance_package_platform drop column tipp_end_issue;
--alter table title_instance_package_platform drop column tipp_coverage_depth;
--alter table title_instance_package_platform drop column tipp_coverage_note;
--alter table title_instance_package_platform drop column tipp_embargo;

--insert into issue_entitlement_coverage (ic_version,ic_start_date,ic_start_volume,ic_start_issue,ic_end_date,ic_end_volume,ic_end_issue,ic_coverage_depth,ic_coverage_note,ic_embargo,ic_ie_fk)
--select ie_version,ie_start_date,ie_start_volume,ie_start_issue,ie_end_date,ie_end_volume,ie_end_issue,ie_coverage_depth,ie_coverage_note,ie_embargo,ie_id from issue_entitlement;
--alter table issue_entitlement drop column ie_start_date;
--alter table issue_entitlement drop column ie_start_volume;
--alter table issue_entitlement drop column ie_start_issue;
--alter table issue_entitlement drop column ie_end_date;
--alter table issue_entitlement drop column ie_end_volume;
--alter table issue_entitlement drop column ie_end_issue;
--alter table issue_entitlement drop column ie_coverage_depth;
--alter table issue_entitlement drop column ie_coverage_note;
--alter table issue_entitlement drop column ie_embargo;


-- 2019-08-16
-- migrate org settings

--UPDATE org_settings SET os_key_enum = 'OAMONITOR_SERVER_ACCESS' WHERE os_key_enum = 'OA2020_SERVER_ACCESS';
--UPDATE org_settings SET os_key_enum = 'NATSTAT_SERVER_ACCESS' WHERE os_key_enum = 'STATISTICS_SERVER_ACCESS';


-- 2019-08-20
-- ERMS-1615
-- remove subscription type collective (AFTER yoda-triggered migration)

-- DELETE FROM refdata_value WHERE rdv_value = 'Collective Subscription';


-- 2019-08-22
-- set by all surveyConfigs evaluationFinish to false

-- ALTER TABLE public.survey_config ADD surconf_evaluation_finish boolean DEFAULT false NULL;
-- UPDATE survey_config SET surconf_evaluation_finish = false WHERE surconf_evaluation_finish is null;


-- 2019-08-21
-- change column mappings

--ALTER TABLE public.org RENAME date_created TO org_date_created;
--ALTER TABLE public.org RENAME last_updated TO org_last_updated;

-- set default for user.date_created
-- July 18th was the last date when the QA database has been reset

-- ALTER TABLE public.user ADD date_created timestamp DEFAULT '2019-07-18 00:00:00.0' not null;
-- ALTER TABLE public.user ADD last_updated timestamp DEFAULT '2019-07-18 00:00:00.0' not null;
-- UPDATE public."user" SET date_created = '2019-01-01 00:00:00.0',last_updated = '2019-01-01 00:00:00.0' where date_created is null;

-- 2019-08-28
-- new coloumn for survey (gorm auto-creation fail)

--ALTER TABLE public.survey_config ADD surconf_is_subscription_survey_fix boolean DEFAULT false NULL;
--ALTER TABLE public.survey_info ADD surin_is_subscription_survey boolean NULL;

--UPDATE survey_info SET surin_is_subscription_survey = true;
--UPDATE public.survey_config SET surconf_is_subscription_survey_fix = true where surconf_sub_fk is not null;
--UPDATE survey_config SET surconf_is_subscription_survey_fix = false WHERE surconf_sub_fk IS NULLL;

-- 2019-09-04
-- typo in key and translations

--update i10n_translation set i10n_value_de = 'Wissenschaftliche Spezialbibliothek' where i10n_value_de = 'Wissenschafltiche Spezialbibliothek';
--update i10n_translation set i10n_value_en = 'Wissenschaftliche Spezialbibliothek' where i10n_value_en = 'Wissenschafltiche Spezialbibliothek';
--update refdata_value set rdv_value = 'Wissenschaftliche Spezialbibliothek' where rdv_value = 'Wissenschafltiche Spezialbibliothek';

-- 2019-09-09
-- update survey results with new global survey property

update survey_result set surre_type_fk = (select surpro_id from survey_property where surpro_name = 'Participation') where surre_type_fk = (select surpro_id from survey_property where surpro_name = 'Continue to license');
update survey_config set surconf_surprop_fk = (select surpro_id from survey_property where surpro_name = 'Participation') where surconf_surprop_fk = (select surpro_id from survey_property where surpro_name = 'Continue to license');
update survey_config_properties set surconpro_survey_property_fk = (select surpro_id from survey_property where surpro_name = 'Participation') where surconpro_survey_property_fk = (select surpro_id from survey_property where surpro_name = 'Continue to license');

-- 2019-09-11
-- update survey results with new global survey property

update survey_result set surre_type_fk = (select surpro_id from survey_property where surpro_name = 'Access choice') where surre_type_fk = (select surpro_id from survey_property where surpro_name = 'Zugangswahl');
update survey_config set surconf_surprop_fk = (select surpro_id from survey_property where surpro_name = 'Access choice') where surconf_surprop_fk = (select surpro_id from survey_property where surpro_name = 'Zugangswahl');
update survey_config_properties set surconpro_survey_property_fk = (select surpro_id from survey_property where surpro_name = 'Access choice') where surconpro_survey_property_fk = (select surpro_id from survey_property where surpro_name = 'Zugangswahl');

update survey_result set surre_type_fk = (select surpro_id from survey_property where surpro_name = 'Category A-F') where surre_type_fk = (select surpro_id from survey_property where surpro_name = 'Kategorie A-F');
update survey_config set surconf_surprop_fk = (select surpro_id from survey_property where surpro_name = 'Category A-F') where surconf_surprop_fk = (select surpro_id from survey_property where surpro_name = 'Kategorie A-F');
update survey_config_properties set surconpro_survey_property_fk = (select surpro_id from survey_property where surpro_name = 'Category A-F') where surconpro_survey_property_fk = (select surpro_id from survey_property where surpro_name = 'Kategorie A-F');

update survey_result set surre_type_fk = (select surpro_id from survey_property where surpro_name = 'Multi-year term 2 years') where surre_type_fk = (select surpro_id from survey_property where surpro_name = 'Mehrjahreslaufzeit 2 Jahre');
update survey_config set surconf_surprop_fk = (select surpro_id from survey_property where surpro_name = 'Multi-year term 2 years') where surconf_surprop_fk = (select surpro_id from survey_property where surpro_name = 'Mehrjahreslaufzeit 2 Jahre');
update survey_config_properties set surconpro_survey_property_fk = (select surpro_id from survey_property where surpro_name = 'Multi-year term 2 years') where surconpro_survey_property_fk = (select surpro_id from survey_property where surpro_name = 'Mehrjahreslaufzeit 2 Jahre');

update survey_result set surre_type_fk = (select surpro_id from survey_property where surpro_name = 'Multi-year term 3 years') where surre_type_fk = (select surpro_id from survey_property where surpro_name = 'Mehrjahreslaufzeit 3 Jahre');
update survey_config set surconf_surprop_fk = (select surpro_id from survey_property where surpro_name = 'Multi-year term 3 years') where surconf_surprop_fk = (select surpro_id from survey_property where surpro_name = 'Mehrjahreslaufzeit 3 Jahre');
update survey_config_properties set surconpro_survey_property_fk = (select surpro_id from survey_property where surpro_name = 'Multi-year term 3 years') where surconpro_survey_property_fk = (select surpro_id from survey_property where surpro_name = 'Mehrjahreslaufzeit 3 Jahre');


-- START OF ERMS-1666 --
-- Kontrolle vorher: zu löschende Einträge
SELECT * FROM Person AS p
                left join person_role pr on p.prs_id = pr_prs_fk
WHERE p.prs_is_public = true
  and pr isnull;


DELETE FROM contact AS c
WHERE c.ct_prs_fk in (SELECT p2.prs_id FROM Person AS p2
                                           left join person_role pr on p2.prs_id = pr_prs_fk
                   WHERE p2.prs_is_public = true
                     and pr isnull);
DELETE FROM address AS a
WHERE a.adr_prs_fk in (SELECT p2.prs_id FROM Person AS p2
                                           left join person_role pr on p2.prs_id = pr_prs_fk
                   WHERE p2.prs_is_public = true
                     and pr isnull);
DELETE FROM Person AS p
WHERE p.prs_id in (SELECT p2.prs_id FROM Person AS p2
                                  left join person_role pr on p2.prs_id = pr_prs_fk
               WHERE p2.prs_is_public = true
                 and pr isnull);

-- Kontrolle nachher: Liste müsste nun LEER sein!
SELECT * FROM Person AS p
                left join person_role pr on p.prs_id = pr_prs_fk
WHERE p.prs_is_public = true
  and pr isnull;
-- END ERMS-1666 --

--- 2019-09-12
--- Update and delete SurveyProperty
update survey_result set surre_type_fk = (select surpro_id from survey_property where surpro_name = 'Participation') where surre_type_fk = (select surpro_id from survey_property where surpro_name = 'Teilnahme');
update survey_config set surconf_surprop_fk = (select surpro_id from survey_property where surpro_name = 'Participation') where surconf_surprop_fk = (select surpro_id from survey_property where surpro_name = 'Teilnahme');
update survey_config_properties set surconpro_survey_property_fk = (select surpro_id from survey_property where surpro_name = 'Participation') where surconpro_survey_property_fk = (select surpro_id from survey_property where surpro_name = 'Teilnahme');

DELETE FROM survey_property where surpro_name = 'Continue to license';
DELETE FROM survey_property where surpro_name = 'Teilnahme';


