
/* anonymise log entries - DSGVO */

UPDATE audit_log SET actor = 'anonymised' WHERE actor IN (SELECT DISTINCT username FROM user);
