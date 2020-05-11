-- add all migrations (for local and/or remote environments) here
-- add all migrations (for local and/or remote environments) here
-- add all migrations (for local and/or remote environments) here

-- yyyy-mm-dd
-- <short description>
-- changesets in changelog-yyyy-mm-dd.groovy

--ERMS-2407
update org set org_region_rv_fk = null where org_region_rv_fk in (
    select rdv_id
    from refdata_value
    where rdv_owner = (select rdc_id from refdata_category where rdc_description = 'regions.de')
);

delete from refdata_value where rdv_owner = (select rdc_id from refdata_category where rdc_description = 'regions.de');

update refdata_value
    set rdv_owner = (select rdc_id from refdata_category where rdc_description = 'regions.de')
    where rdv_owner = (select rdc_id from refdata_category where rdc_description = 'federal.state');

delete from refdata_category where rdc_description = 'federal.state';

ALTER TABLE address RENAME COLUMN adr_state_rv_fk TO adr_region_rv_fk;