
/* remove legacy roles and uer_roles */

DELETE FROM user_role WHERE role_id in (SELECT id FROM role WHERE authority in ('KBPLUS_EDITOR', 'ROLE_EDITOR'));
DELETE FROM role WHERE authority in ('KBPLUS_EDITOR', 'ROLE_EDITOR');

/* remove user based roles in global context */

DELETE FROM user_role WHERE role_id in (SELECT id FROM role WHERE authority in ('INST_USER', 'INST_ADM'));