
/* anonymise log entries - DSGVO */

UPDATE audit_log SET actor = 'anonymised' WHERE actor IN (SELECT DISTINCT username FROM user);


/* refactoring: replace link table with license.instanceOf */

UPDATE license, link SET license.lic_parent_lic_fk = link.link_from_lic_fk
  WHERE license.lic_id = link.link_to_lic_fk;

UPDATE license, link SET license.lic_is_slaved = 1
  WHERE license.lic_id = link.link_to_lic_fk
  AND link.link_is_slaved IS NOT NULL;


/* migrate existing Org.Consortium <- (OrgRole: Licensee) -> License to (OrgRole: Licensing Consortium) */

UPDATE org_role target SET target.or_roletype_fk =
  (SELECT rdv_id FROM refdata_value WHERE rdv_value = 'Licensing Consortium')
WHERE target.or_id IN (SELECT * FROM (
    SELECT
      ogr.or_id orgRoleId
    FROM org_role ogr
      JOIN org o on ogr.or_org_fk = o.org_id
      JOIN refdata_value oRdv on o.org_type_rv_fk = oRdv.rdv_id
      JOIN refdata_value ogrRdv on ogr.or_roletype_fk = ogrRdv.rdv_id
      JOIN refdata_category ogrRdvRdc on ogrRdv.rdv_owner = ogrRdvRdc.rdc_id
    WHERE
      ogr.or_lic_fk IS NOT NULL
      AND oRdv.rdv_value = 'Consortium'
      AND ogrRdv.rdv_value = 'Licensee'
      AND ogrRdvRdc.rdc_description = 'Organisational Role'
) source);
