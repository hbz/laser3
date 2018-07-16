
/* anonymise log entries - DSGVO */

UPDATE audit_log SET actor = 'anonymised' WHERE actor IN (SELECT DISTINCT username FROM user);

/* refactoring: replace link table with license.instanceOf */

UPDATE license, link SET license.lic_parent_lic_fk = link.link_from_lic_fk
  WHERE license.lic_id = link.link_to_lic_fk;

UPDATE license, link SET license.lic_is_slaved = 1
  WHERE license.lic_id = link.link_to_lic_fk
  AND link.link_is_slaved IS NOT NULL;
