
-- erms-1256
-- 2019-05-08
-- removing ROLE_ORG_COM_EDITOR

DELETE FROM user_role WHERE role_id = (
  SELECT id FROM role WHERE authority = 'ROLE_ORG_COM_EDITOR'
);

DELETE FROM role WHERE authority = 'ROLE_ORG_COM_EDITOR';