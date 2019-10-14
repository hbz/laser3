-- 2019-10-09
-- remove table columns for local environments
-- changesets in changelog-2019-10-10.groovy

alter table "user" drop column apikey;
alter table "user" drop column apisecret;

-- 2019-10-10
-- fixed gorm mappings for local environments
-- changesets in changelog-2019-10-10.groovy

ALTER TABLE api_source RENAME as_baseurl TO as_base_url;
ALTER TABLE api_source RENAME as_datecreated TO as_date_created;
ALTER TABLE api_source RENAME "as_editUrl" TO as_edit_url;
ALTER TABLE api_source RENAME as_fixtoken TO as_fix_token;
ALTER TABLE api_source RENAME as_lastupdated TO as_last_updated;
ALTER TABLE api_source RENAME as_lastupdated_with_api TO as_last_updated_with_api;
ALTER TABLE api_source RENAME as_variabletoken TO as_variable_token;
ALTER TABLE cost_item RENAME ci_subpkg_fk TO ci_sub_pkg_fk;
ALTER TABLE creator RENAME cre_datecreated TO cre_date_created;
ALTER TABLE creator RENAME cre_lastupdated TO cre_last_updated;
ALTER TABLE creator_title RENAME ct_datecreated TO ct_date_created;
ALTER TABLE creator_title RENAME ct_lastupdated TO ct_last_updated;
ALTER TABLE doc RENAME doc_mimetype TO doc_mime_type;
ALTER TABLE folder_item RENAME fi_datecreated TO fi_date_created;
ALTER TABLE folder_item RENAME fi_lastupdated TO fi_last_updated;
ALTER TABLE reader_number RENAME num_create_date TO num_date_created;
ALTER TABLE reader_number RENAME num_lastupdate_date TO num_last_updated;
ALTER TABLE system_message RENAME sm_datecreated TO sm_date_created;
ALTER TABLE system_message RENAME sm_lastupdated TO sm_last_updated;
ALTER TABLE system_message RENAME sm_shownow TO sm_show_now;
ALTER TABLE user_folder RENAME uf_datecreated TO uf_date_created;
ALTER TABLE user_folder RENAME uf_lastupdated TO uf_last_updated;