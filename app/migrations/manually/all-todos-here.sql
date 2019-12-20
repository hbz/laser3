-- 2019-10-09
-- remove table columns for local environments
-- changesets in changelog-2019-10-10.groovy

--alter table "user" drop column apikey;
--alter table "user" drop column apisecret;

-- 2019-10-10
-- fixed gorm mappings for local environments
-- changesets in changelog-2019-10-10.groovy

-- ALTER TABLE api_source RENAME as_baseurl TO as_base_url;
-- ALTER TABLE api_source RENAME as_datecreated TO as_date_created;
-- ALTER TABLE api_source RENAME "as_editUrl" TO as_edit_url;
-- ALTER TABLE api_source RENAME as_fixtoken TO as_fix_token;
-- ALTER TABLE api_source RENAME as_lastupdated TO as_last_updated;
-- ALTER TABLE api_source RENAME as_lastupdated_with_api TO as_last_updated_with_api;
-- ALTER TABLE api_source RENAME as_variabletoken TO as_variable_token;
-- ALTER TABLE cost_item RENAME ci_subpkg_fk TO ci_sub_pkg_fk;
-- ALTER TABLE creator RENAME cre_datecreated TO cre_date_created;
-- ALTER TABLE creator RENAME cre_lastupdated TO cre_last_updated;
-- ALTER TABLE creator_title RENAME ct_datecreated TO ct_date_created;
-- ALTER TABLE creator_title RENAME ct_lastupdated TO ct_last_updated;
-- ALTER TABLE doc RENAME doc_mimetype TO doc_mime_type;
-- ALTER TABLE folder_item RENAME fi_datecreated TO fi_date_created;
-- ALTER TABLE folder_item RENAME fi_lastupdated TO fi_last_updated;
-- ALTER TABLE reader_number RENAME num_create_date TO num_date_created;
-- ALTER TABLE reader_number RENAME num_lastupdate_date TO num_last_updated;
-- ALTER TABLE system_message RENAME sm_datecreated TO sm_date_created;
-- ALTER TABLE system_message RENAME sm_lastupdated TO sm_last_updated;
-- ALTER TABLE system_message RENAME sm_shownow TO sm_show_now;
-- ALTER TABLE user_folder RENAME uf_datecreated TO uf_date_created;
-- ALTER TABLE user_folder RENAME uf_lastupdated TO uf_last_updated;

-- 2019-10-18
-- changesets in changelog-2019-10-21.groovy
-----ALTER TABLE subscription ADD sub_is_multi_year boolean;
--UPDATE subscription set sub_is_multi_year = FALSE;

-- 2019-10-22
-- changesets in changelog-2019-10-23.groovy
-- ERMS-1785: purge originEditUrl as it is never used
-----ALTER TABLE package DROP COLUMN pkg_origin_edit_url;
-----ALTER TABLE title_instance_package_platform DROP COLUMN tipp_origin_edit_url;
-----ALTER TABLE title_instance DROP COLUMN ti_origin_edit_url;
-----ALTER TABLE platform DROP COLUMN plat_origin_edit_url;
-----ALTER TABLE org DROP COLUMN org_origin_edit_url;
--DELETE FROM identifier_occurrence where io_canonical_id in (select id_id from identifier left join identifier_namespace "in" on identifier.id_ns_fk = "in".idns_id where "in".idns_ns in ('originEditUrl','originediturl'));
--DELETE FROM identifier where id_ns_fk = (select idns_id from identifier_namespace where idns_ns in ('originEditUrl','originediturl'));
--DELETE FROM identifier_namespace where idns_ns in ('originEditUrl','originediturl');

-- 2019-10-22 (mbeh)
--  new column class in org_access_point is initially null
-- need to set to  com.k_int.kbplus.OrgAccessPoint for all existing rows
-- see pull request for Update access point management - ad7500ef0534c4b414e5e7cb0c9acc1acd4f8283"
--update org_access_point set class = 'com.k_int.kbplus.OrgAccessPoint' where class is null;

-- 2019-10-23
-- need to refetch usage data delete contents of tables
-- DELETE FROM stats_triple_cursor;
-- DELETE FROM fact;
-- changesets in changelog-2019-10-24.groovy
-- execute before startup / local dev environment only
-- changed Fact.supplier without mapping from Org to Platform!
-- changesets in changelog-2019-10-24.groovy
-----ALTER TABLE fact DROP COLUMN supplier_id;

-- 2019-10-25
-- Set sub_is_multi_year on all subscription where the periode more than 724 days
-- changesets in changelog-2019-10-31.groovy
-- update subscription set sub_is_multi_year = true where sub_id in(select sub_id from subscription where DATE_PART('day', sub_end_date - sub_start_date) >= 724 and sub_end_date is not null);

-- 2019-11-14
-- Change for SurveyProperty  reference_field
-- changesets in changelog-2019-10-31.groovy
---update i10n_translation set i10n_reference_field = 'expl' where i10n_reference_field = 'explain';

-- 2019-11-18
-- Delete deprecated user settings
-- changesets in changelog-2019-10-31.groovy
-- delete from user_settings where us_key_enum like 'DASHBOARD_REMINDER_PERIOD';

-- 2019-11-21
-- Refactoring PendingChanges.(changeDoc -> payload)
-- changesets in changelog-2019-11-21.groovy
--ALTER TABLE pending_change RENAME pc_change_doc TO pc_payload;

-- 2019-11-21
-- Rename Column
-- changesets in changelog-2019-11-21.groovy
--alter table dashboard_due_date RENAME das_is_hide TO das_is_hidden;

-- 2019-11-21
-- Rename Columns
alter table dashboard_due_date RENAME das_attribut TO das_attribute_value_de;
alter table dashboard_due_date RENAME version TO das_version;
alter table dashboard_due_date RENAME last_updated TO das_last_updated;
alter table dashboard_due_date add column if not exists das_attribute_value_en varchar(255);
alter table dashboard_due_date add column if not exists das_attribute_name varchar(255);
-- changesets in changelog-2019-11-27.groovy
--alter table dashboard_due_date RENAME das_attribut TO das_attribute_value_de;
--alter table dashboard_due_date RENAME version TO das_version;
--alter table dashboard_due_date RENAME last_updated TO das_last_updated;
--alter table dashboard_due_date add column if not exists das_attribute_value_en varchar(255);
--alter table dashboard_due_date add column if not exists das_attribute_name varchar(255);

-- 2019-11-22
-- Fill new columns with values
TRUNCATE TABLE dashboard_due_date;
ALTER SEQUENCE dashboard_due_date_das_id_seq RESTART WITH 1;
ALTER TABLE dashboard_due_date ALTER COLUMN das_last_updated TYPE TIMESTAMP WITH TIME ZONE;
update dashboard_due_date set das_attribute_name = 'manualCancellationDate' where das_attribute_name isnull and das_attribute_value_de = 'Kündigungsdatum' and das_oid like 'com.k_int.kbplus.Subscription:%';
update dashboard_due_date set das_attribute_name = 'endDate' where  das_attribute_name isnull and das_attribute_value_de = 'Enddatum' and das_oid like 'com.k_int.kbplus.Subscription:%';
update dashboard_due_date set das_attribute_name = 'manualCancellationDate' where  das_attribute_name isnull and das_attribute_value_de = 'Kündigungsdatum' and das_oid like 'com.k_int.kbplus.Subscription:%';
update dashboard_due_date set das_attribute_name = 'endDate' where  das_attribute_name isnull and das_oid like 'com.k_int.kbplus.Task:%';
update dashboard_due_date set das_attribute_name = 'endDate' where  das_attribute_name isnull and das_oid like 'com.k_int.kbplus.SurveyInfo:%';
update dashboard_due_date set das_attribute_name = 'type.name' where  das_attribute_name isnull and das_oid like '%Property%';

update dashboard_due_date set das_attribute_value_en = 'termination date' where das_attribute_value_en isnull and das_attribute_name = 'manualCancellationDate' ;
update dashboard_due_date set das_attribute_value_en = 'due date' where  das_attribute_value_en isnull and das_oid like 'com.k_int.kbplus.Task:%';
update dashboard_due_date set das_attribute_value_en = 'endDate' where  das_attribute_value_en isnull and (das_oid like 'com.k_int.kbplus.Subscription:%' or das_oid like 'com.k_int.kbplus.SurveyInfo:%');
-- changesets in changelog-2019-11-27.groovy
--TRUNCATE TABLE dashboard_due_date;
--ALTER SEQUENCE dashboard_due_date_das_id_seq RESTART WITH 1;
--ALTER TABLE dashboard_due_date ALTER COLUMN das_last_updated TYPE TIMESTAMP WITH TIME ZONE;

-- 2019-12-19
-- ERMS-1992: translations provided for subscription custom properties
-- changesets in changelog-2019-12-20.groovy
update property_definition set pd_name = 'GASCO display name' where pd_name = 'GASCO-Anzeigename' and pd_description ='Subscription Property';
update property_definition set pd_name = 'GASCO negotiator name' where pd_name = 'GASCO-Verhandlername' and pd_description ='Subscription Property';
update property_definition set pd_name = 'GASCO information link' where pd_name = 'GASCO-Information-Link' and pd_description ='Subscription Property';
update property_definition set pd_name = 'EZB tagging (yellow)' where pd_name = 'EZB Gelbschaltung' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Pricing advantage by licensing of another product' where pd_name = 'Preisvorteil durch weitere Produktteilnahme' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Product dependency' where pd_name = 'Produktabhängigkeit' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Open country-wide' where pd_name = 'Bundesweit offen' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Billing done by provider' where pd_name = 'Rechnungsstellung durch Anbieter' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Due date for volume discount' where pd_name = 'Mengenrabatt Stichtag' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Time span for testing' where pd_name = 'Testzeitraum' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Joining during the period' where pd_name = 'Unterjähriger Einstieg' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Newcomer discount' where pd_name = 'Neueinsteigerrabatt' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Time of billing' where pd_name = 'Rechnungszeitpunkt' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Payment target' where pd_name = 'Zahlungsziel' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Price rounded' where pd_name = 'Preis gerundet' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Partial payment' where pd_name = 'Teilzahlung' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Statistic' where pd_name = 'Statistik' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Statistic access' where pd_name = 'Statistikzugang' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Statistics Link' where pd_name = 'StatisticsLink' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Admin Access' where pd_name = 'AdminAccess' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Admin Link' where pd_name = 'AdminLink' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Private institutions' where pd_name = 'Private Einrichtungen' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Perennial term' where pd_name = 'Mehrjahreslaufzeit' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Perennial term checked' where pd_name = 'Mehrjahreslaufzeit ausgewählt' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Discount' where pd_name = 'Rabatt' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Scale of discount' where pd_name = 'Rabattstaffel' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Calculation of discount' where pd_name = 'Rabatt Zählung' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Term of notice' where pd_name = 'Kündigungsfrist' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Additional software necessary?' where pd_name = 'Zusätzliche Software erforderlich?' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Price increase' where pd_name = 'Preissteigerung' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Price depending on' where pd_name = 'Preis abhängig von' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Cancellation rate' where pd_name = 'Abbestellquote' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Order number in purchasing system' where pd_name = 'Bestellnummer im Erwerbungssystem' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Credentials for users (per journal)' where pd_name = 'Zugangskennungen für Nutzer (pro Zeitschrift)' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Tax exemption' where pd_name = 'TaxExemption' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Subscription number of editor' where pd_name = 'Subscriptionsnummer vom Verlag' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Subscription number of provider' where pd_name = 'Subskriptionsnummer des Lieferanten' and pd_description ='Subscription Property';
update property_definition set pd_name = 'DBIS entry' where pd_name = 'DBIS-Eintrag' and pd_description ='Subscription Property';
update property_definition set pd_name = 'DBIS link' where pd_name = 'DBIS-Link' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Cancellation reason' where pd_name = 'Abbestellgrund' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Hosting fee' where pd_name = 'Hosting-Gebühr' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Pick&Choose package' where pd_name = 'Pick&Choose-Paket' and pd_description ='Subscription Property';
update property_definition set pd_name = 'PDA/EBS model' where pd_name = 'PDA/EBS-Programm' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Specialised statistics / classification' where pd_name = 'Fachstatistik / Klassifikation' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Perpetual access' where pd_name = 'Archivzugriff' and pd_description ='Subscription Property';
update property_definition set pd_name = 'Restricted user group' where pd_name = 'Eingeschränkter Benutzerkreis' and pd_description ='Subscription Property';
update property_definition set pd_name = 'SFX entry' where pd_name = 'SFX-Eintrag' and pd_description ='Subscription Property';