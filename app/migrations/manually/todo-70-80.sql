
-- 2019-07-09
-- remove role ROLE_DATAMANAGER

DELETE FROM user_role WHERE role_id = (
    SELECT id FROM role WHERE authority = 'ROLE_DATAMANAGER'
);

DELETE FROM role WHERE authority = 'ROLE_DATAMANAGER';
