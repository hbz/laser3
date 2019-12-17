-- add all migrations (for local and/or remote environments) here
-- add all migrations (for local and/or remote environments) here
-- add all migrations (for local and/or remote environments) here

-- yyyy-mm-dd
-- <short description>

-- 2019-11-25 / 2019-11-28
-- Delete deprecated package identifier (we use gokbId instead), move TitleInstance.type to TitleInstance.medium
alter table package drop column pkg_identifier;
ALTER TABLE title_instance RENAME ti_type_rv_fk  TO ti_medium_rv_fk;
ALTER TABLE public.title_instance ALTER COLUMN ti_gokb_id TYPE character varying(255);

-- 2019-12-06
-- ERMS-1929
-- removing deprecated field impId, move ti_type_rv_fk to ti_medium_rv_fk
alter table org drop column org_imp_id;
alter table package drop column pkg_imp_id;
alter table platform drop column plat_imp_id;
alter table subscription drop column sub_imp_id;
alter table title_instance drop column ti_imp_id;
alter table title_instance_package_platform drop column tipp_imp_id;
alter table title_instance rename ti_type_rv_fk to ti_medium_rk_fk;
update refdata_value set rdv_value = 'Book' where rdv_value = 'EBook';
update refdata_category set rdc_description = 'Title Medium' where rdc_description = 'Title Type';

-- 2019-12-10
-- ERMS-1901 (ERMS-1500)
-- org.name set not null with default "Name fehlt"
update org set org_name = 'Name fehlt!' where org_name is null;
alter table org alter column org_name set default 'Name fehlt!';
alter table org alter column org_name set not null;

