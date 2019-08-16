
-- 2019-08-01
-- migrate refdataValues (category='YN') to boolean

--ALTER TABLE license ALTER COLUMN lic_is_slaved DROP DEFAULT;
--ALTER TABLE license DROP CONSTRAINT fk9f08441e07d095a;
--ALTER TABLE license ALTER lic_is_slaved TYPE bool USING CASE WHEN lic_is_slaved=1 THEN TRUE ELSE FALSE END;

--ALTER TABLE license ALTER COLUMN lic_is_public_rdv_fk DROP DEFAULT;
--ALTER TABLE license DROP CONSTRAINT fk9f084413d2aceb;
--ALTER TABLE license ALTER lic_is_public_rdv_fk TYPE bool USING CASE WHEN lic_is_public_rdv_fk=1 THEN TRUE ELSE FALSE END;
--ALTER TABLE license RENAME lic_is_public_rdv_fk TO lic_is_public;

--ALTER TABLE subscription ALTER COLUMN sub_is_slaved DROP DEFAULT;
--ALTER TABLE subscription DROP CONSTRAINT fk1456591d2d814494;
--ALTER TABLE subscription ALTER sub_is_slaved TYPE bool USING CASE WHEN sub_is_slaved=1 THEN TRUE ELSE FALSE END;

--ALTER TABLE subscription ALTER COLUMN sub_is_public DROP DEFAULT;
--ALTER TABLE subscription DROP CONSTRAINT fk1456591d28e1dd90;
--ALTER TABLE subscription ALTER sub_is_public TYPE bool USING CASE WHEN sub_is_public=1 THEN TRUE ELSE FALSE END;

--ALTER TABLE package ALTER COLUMN pkg_is_public DROP DEFAULT;
--ALTER TABLE package DROP CONSTRAINT fkcfe53446f8dfd21c;
--ALTER TABLE package ALTER pkg_is_public TYPE bool USING CASE WHEN pkg_is_public=1 THEN TRUE ELSE FALSE END;

--ALTER TABLE person ALTER COLUMN prs_is_public_rv_fk DROP DEFAULT;
--ALTER TABLE person DROP CONSTRAINT fkc4e39b55750b1c62;
--ALTER TABLE person ALTER prs_is_public_rv_fk TYPE bool USING CASE WHEN prs_is_public_rv_fk=1 THEN TRUE ELSE FALSE END;
--ALTER TABLE person RENAME prs_is_public_rv_fk TO prs_is_public;


-- 2019-08-02
-- naming conventions

--ALTER TABLE refdata_category RENAME rdv_hard_data TO rdc_is_hard_data;
--ALTER TABLE refdata_value RENAME rdv_hard_data TO rdv_is_hard_data;

--ALTER TABLE identifier_namespace RENAME idns_hide TO idns_is_hidden;
--ALTER TABLE identifier_namespace RENAME idns_unique TO idns_is_unique;


-- 2019-08-06
-- migrate refdataValues (category='YN') to boolean

--ALTER TABLE property_definition_group_binding ALTER COLUMN pbg_visible_rv_fk DROP DEFAULT;
--ALTER TABLE property_definition_group_binding DROP CONSTRAINT fk3d279603de1d7a3a;
--ALTER TABLE property_definition_group_binding ALTER pbg_visible_rv_fk TYPE bool USING CASE WHEN pbg_visible_rv_fk=1 THEN TRUE ELSE FALSE END;
--ALTER TABLE property_definition_group_binding RENAME pbg_visible_rv_fk TO pbg_is_visible;

--ALTER TABLE property_definition_group_binding ALTER COLUMN pbg_is_viewable_rv_fk DROP DEFAULT;
--ALTER TABLE property_definition_group_binding DROP CONSTRAINT fk3d27960376a2fe3c;
--ALTER TABLE property_definition_group_binding ALTER pbg_is_viewable_rv_fk TYPE bool USING CASE WHEN pbg_is_viewable_rv_fk=1 THEN TRUE ELSE FALSE END;
--ALTER TABLE property_definition_group_binding RENAME pbg_is_viewable_rv_fk TO pbg_is_visible_for_cons_member;

--ALTER TABLE property_definition_group ALTER COLUMN pdg_visible_rv_fk DROP DEFAULT;
--ALTER TABLE property_definition_group DROP CONSTRAINT fked224dbda14135f8;
--ALTER TABLE property_definition_group ALTER pdg_visible_rv_fk TYPE bool USING CASE WHEN pdg_visible_rv_fk=1 THEN TRUE ELSE FALSE END;
--ALTER TABLE property_definition_group RENAME pdg_visible_rv_fk TO pdg_is_visible;


-- 2019-08-16
-- migrate org settings

UPDATE org_settings SET os_key_enum = 'OAMONITOR_SERVER_ACCESS' WHERE os_key_enum = 'OA2020_SERVER_ACCESS';



