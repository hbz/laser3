
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

/*
  migrate existing OrgRole 'Licensee' to 'Licensee_Consortial'
  if License.instanceOf(parent_lic_fk) is NOT NULL and parent is NOT a template
*/

UPDATE org_role target SET target.or_roletype_fk =
  (SELECT rdv_id FROM refdata_value WHERE rdv_value = 'Licensee_Consortial')
WHERE target.or_id IN (SELECT * FROM (
     SELECT or_id FROM org_role
     WHERE org_role.or_roletype_fk = (SELECT rdv.rdv_id FROM refdata_value rdv
                                        JOIN refdata_category rdc
                                          ON rdv.rdv_owner = rdc.rdc_id
                                       WHERE rdc.rdc_description = 'Organisational Role' AND rdv.rdv_value = 'Licensee')
     AND or_lic_fk IN (SELECT lic.lic_id FROM license lic
                         JOIN license parent
                           ON lic.lic_parent_lic_fk = parent.lic_id
                        WHERE parent.lic_type_rv_fk != (SELECT rdv.rdv_id FROM refdata_value rdv
                                                         WHERE rdv.rdv_value = 'Template')
                      )
) source);

/*
  adding missing OrgRoles
  'Licensing Consortium' for child licenses with existing OrgRole 'Licensee_Consortial'
*/

INSERT INTO org_role (or_version, or_lic_fk, or_org_fk, or_roletype_fk)
  SELECT 0, childLic, orgConsortium, (SELECT rdv_id
                                      FROM refdata_value
                                      WHERE rdv_value = 'Licensing Consortium') orgConsRdv FROM
    (
      SELECT
        or1.or_org_fk          orgLicensee,
        or1.or_lic_fk          childLic,
        lic1.lic_parent_lic_fk parentLic,
        or2.or_org_fk          orgConsortium
      FROM org_role or1
        JOIN license lic1 ON or1.or_lic_fk = lic1.lic_id
        JOIN org_role or2 ON or2.or_lic_fk = lic1.lic_parent_lic_fk
      WHERE or1.or_roletype_fk = (SELECT rdv_id
                                  FROM refdata_value
                                  WHERE rdv_value = 'Licensee_Consortial')
            AND or2.or_roletype_fk = (SELECT rdv_id
                                      FROM refdata_value
                                      WHERE rdv_value = 'Licensing Consortium')
    ) source
;
