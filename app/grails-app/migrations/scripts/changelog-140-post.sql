
/* remove legacy roles and uer_roles */

DELETE FROM user_role WHERE role_id in (SELECT id FROM role WHERE authority in ('KBPLUS_EDITOR', 'ROLE_EDITOR'));
DELETE FROM role WHERE authority in ('KBPLUS_EDITOR', 'ROLE_EDITOR');

/* remove user based roles in global context */

DELETE FROM user_role WHERE role_id in (SELECT id FROM role WHERE authority in ('INST_USER', 'INST_ADM'));

/* remove legacy person roles; now splitted into function and responsibility */

DELETE FROM refdata_value WHERE rdv_owner IN (SELECT rdc_id FROM refdata_category WHERE rdc_description = 'Person Role');
DELETE FROM refdata_category WHERE rdc_description = 'Person Role';