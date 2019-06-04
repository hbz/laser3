
-- erms-1256
-- 2019-05-08
-- removing ROLE_ORG_COM_EDITOR

--DELETE FROM user_role WHERE role_id = (
--  SELECT id FROM role WHERE authority = 'ROLE_ORG_COM_EDITOR'
--);
--DELETE FROM role WHERE authority = 'ROLE_ORG_COM_EDITOR';


-- erms-1297 (in connection with erms-1149 and erms-947)
-- 2019-05-07
-- execute before startup

--insert into public.refdata_value (rdv_hard_data,rdv_version,rdv_owner,rdv_value) values (true,1,(select rdc_id from refdata_category where rdc_description = 'Subscription Status'),'Status not defined');
--update subscription set sub_status_rv_fk = (select rdv_id from refdata_value where rdv_value = 'Status not defined' and rdv_owner = (select rdc_id from refdata_category where rdc_description = 'Subscription Status')) where sub_status_rv_fk is null;
--alter table subscription alter column sub_status_rv_fk set not null;


-- erms-1360
-- 2019-05-21
-- execute before startup

--insert into public.refdata_value (rdv_hard_data,rdv_version,rdv_owner,rdv_value) values (true,1,(select rdc_id from refdata_category where rdc_description = 'License Status'),'Status not defined');
--update license set lic_status_rv_fk = (select rdv_id from refdata_value where rdv_value = 'Status not defined' and rdv_owner = (select rdc_id from refdata_category where rdc_description = 'License Status')) where lic_status_rv_fk is null;
--alter table license alter column lic_status_rv_fk set not null;
--alter table license drop column lic_license_status_str;


-- 2019-05-22
-- refactoring roles and perms
-- execute before startup!

--update role set authority = 'ORG_BASIC_MEMBER' where authority = 'ORG_MEMBER';
--update role set authority = 'ORG_INST' where authority = 'ORG_BASIC';
--update role set authority = 'ORG_INST_COLLECTIVE' where authority = 'ORG_COLLECTIVE';

--update perm set code = 'org_basic_member' where code = 'org_member';
--update perm set code = 'org_inst' where code = 'org_basic';
--update perm set code = 'org_inst_collective' where code = 'org_collective';


-- erms-1214
-- 2019-06-04
-- removing ROLE_API_READER, ROLE_API_WRITER and ROLE_API_DATAMANAGER

--DELETE FROM user_role WHERE role_id = (SELECT id FROM role WHERE authority = 'ROLE_API_READER');
--DELETE FROM role WHERE authority = 'ROLE_API_READER';
--DELETE FROM user_role WHERE role_id = (SELECT id FROM role WHERE authority = 'ROLE_API_WRITER');
--DELETE FROM role WHERE authority = 'ROLE_API_WRITER';
--DELETE FROM user_role WHERE role_id = (SELECT id FROM role WHERE authority = 'ROLE_API_DATAMANAGER');
--DELETE FROM role WHERE authority = 'ROLE_API_DATAMANAGER';