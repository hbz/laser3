
-- alter varying(1) to boolean

alter table cost_item
    alter column ci_is_viewable type BOOLEAN using ci_is_viewable::boolean;
alter table cost_item
    alter column ci_include_in_subscr type BOOLEAN using ci_include_in_subscr::boolean;
alter table cost_item
    alter column ci_final_cost_rounding type BOOLEAN using ci_final_cost_rounding::boolean;

alter table doc_context
    alter column dc_is_global type BOOLEAN using dc_is_global::boolean;

alter table elasticsearch_source
    alter column ess_active type BOOLEAN using ess_active::boolean;
alter table elasticsearch_source
    alter column ess_laser_es type BOOLEAN using ess_laser_es::boolean;
alter table elasticsearch_source
    alter column ess_gokb_es type BOOLEAN using ess_gokb_es::boolean;

alter table global_record_source
    alter column grs_active type BOOLEAN using grs_active::boolean;

alter table global_record_tracker
    alter column grt_auto_pkg_update type BOOLEAN using grt_auto_pkg_update::boolean;
alter table global_record_tracker
    alter column grt_auto_tipp_add type BOOLEAN using grt_auto_tipp_add::boolean;
alter table global_record_tracker
    alter column grt_auto_tipp_del type BOOLEAN using grt_auto_tipp_del::boolean;
alter table global_record_tracker
    alter column grt_auto_tipp_update type BOOLEAN using grt_auto_tipp_update::boolean;

alter table identifier_namespace
    alter column idns_hide type BOOLEAN using idns_hide::boolean;
alter table identifier_namespace
    alter column idns_non_unique type BOOLEAN using idns_non_unique::boolean;

alter table org_access_point_link
    alter column active type BOOLEAN using active::boolean;

alter table package
    alter column auto_accept type BOOLEAN using auto_accept::boolean;

alter table property_definition
    alter column pd_mandatory type BOOLEAN using pd_mandatory::boolean;
alter table property_definition
    alter column pd_multiple_occurrence type BOOLEAN using pd_multiple_occurrence::boolean;
alter table property_definition
    alter column pd_soft_data type BOOLEAN using pd_soft_data::boolean;
alter table property_definition
    alter column pd_hard_data type BOOLEAN using pd_hard_data::boolean;

alter table refdata_category
    alter column rdv_soft_data type BOOLEAN using rdv_soft_data::boolean;
alter table refdata_category
    alter column rdv_hard_data type BOOLEAN using rdv_hard_data::boolean;

alter table refdata_value
    alter column rdv_soft_data type BOOLEAN using rdv_soft_data::boolean;
alter table refdata_value
    alter column rdv_hard_data type BOOLEAN using rdv_hard_data::boolean;

alter table reminder
    alter column active type BOOLEAN using active::boolean;

alter table system_message
    alter column sm_shownow type BOOLEAN using sm_shownow::boolean;

alter table user_profile
    alter column enabled type BOOLEAN using enabled::boolean;
alter table user_profile
    alter column account_expired type BOOLEAN using account_expired::boolean;
alter table user_profile
    alter column account_locked type BOOLEAN using account_locked::boolean;
alter table user_profile
    alter column password_expired type BOOLEAN using password_expired::boolean;


-- fixing sequences
-- https://wiki.postgresql.org/wiki/Fixing_Sequences