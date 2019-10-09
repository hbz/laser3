-- 2019-10-09
-- remove table columns for local environments
-- no changeset generated yet <--

alter table "user" drop column apikey;
alter table "user" drop column apisecret;