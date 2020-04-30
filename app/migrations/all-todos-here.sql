-- add all migrations (for local and/or remote environments) here
-- add all migrations (for local and/or remote environments) here
-- add all migrations (for local and/or remote environments) here

-- yyyy-mm-dd
-- <short description>
-- changesets in changelog-yyyy-mm-dd.groovy

-- 2020-04-16
-- ERMS-2382
-- issue entitlement tipp_fk and subscription_fk set not null
-- changesets in changelog-2020-04-16.groovy
alter table issue_entitlement alter column ie_tipp_fk set not null;
alter table issue_entitlement alter column ie_subscription_fk set not null;

-- 2020-04-20
-- ERMS-1914

insert into due_date_object (
    ddo_version,
    ddo_attribute_name,
    ddo_attribute_value_de,
    ddo_attribute_value_en,
    ddo_date,
    ddo_oid,
    ddo_date_created,
    ddo_last_updated,
    ddo_is_done)
select distinct
    das_version,
    das_attribute_name,
    das_attribute_value_de,
    das_attribute_value_en,
    das_date,
    das_oid,
    das_date_created,
    das_last_updated,
    das_is_done
from dashboard_due_date;

update dashboard_due_date ddd set das_ddobj_fk = ddo_id
from dashboard_due_date, due_date_object
where
    due_date_object.ddo_oid = ddd.das_oid and
    due_date_object.ddo_attribute_name = ddd.das_attribute_name;

-- Überprüfung der Daten:
select das_ddobj_fk, ddo_id, das_attribute_name, ddo_attribute_name, das_oid, ddo_oid
from dashboard_due_date, due_date_object
where ddo_oid = das_oid and
      ddo_attribute_name = das_attribute_name;

ALTER TABLE dashboard_due_date DROP COLUMN das_attribute_name;
ALTER TABLE dashboard_due_date DROP COLUMN das_attribute_value_de;
ALTER TABLE dashboard_due_date DROP COLUMN das_attribute_value_en;
ALTER TABLE dashboard_due_date DROP COLUMN das_date;
ALTER TABLE dashboard_due_date DROP COLUMN das_is_done;
ALTER TABLE dashboard_due_date DROP COLUMN das_oid;

