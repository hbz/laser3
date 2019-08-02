
-- 2019-08-01
-- migrate refdataValues (category='YN') to boolean

ALTER TABLE license ALTER COLUMN lic_is_slaved DROP DEFAULT;
ALTER TABLE license DROP CONSTRAINT fk9f08441e07d095a;
ALTER TABLE license ALTER lic_is_slaved TYPE bool USING CASE WHEN lic_is_slaved=1 THEN TRUE ELSE FALSE END;

ALTER TABLE license ALTER COLUMN lic_is_public_rdv_fk DROP DEFAULT;
ALTER TABLE license DROP CONSTRAINT fk9f084413d2aceb;
ALTER TABLE license RENAME lic_is_public_rdv_fk TO lic_is_public;
ALTER TABLE license ALTER lic_is_public_rv_fk TYPE bool USING CASE WHEN lic_is_public_rv_fk=1 THEN TRUE ELSE FALSE END;

ALTER TABLE subscription ALTER COLUMN sub_is_slaved DROP DEFAULT;
ALTER TABLE subscription DROP CONSTRAINT fk1456591d2d814494;
ALTER TABLE subscription ALTER sub_is_slaved TYPE bool USING CASE WHEN sub_is_slaved=1 THEN TRUE ELSE FALSE END;

ALTER TABLE subscription ALTER COLUMN sub_is_public DROP DEFAULT;
ALTER TABLE subscription DROP CONSTRAINT fk1456591d28e1dd90;
ALTER TABLE subscription ALTER sub_is_public TYPE bool USING CASE WHEN sub_is_public=1 THEN TRUE ELSE FALSE END;

ALTER TABLE package ALTER COLUMN pkg_is_public DROP DEFAULT;
ALTER TABLE package DROP CONSTRAINT fkcfe53446f8dfd21c;
ALTER TABLE package ALTER pkg_is_public TYPE bool USING CASE WHEN pkg_is_public=1 THEN TRUE ELSE FALSE END;

ALTER TABLE person ALTER COLUMN prs_is_public_rv_fk DROP DEFAULT;
ALTER TABLE person DROP CONSTRAINT fkc4e39b55750b1c62;
ALTER TABLE person RENAME prs_is_public_rv_fk TO prs_is_public;
ALTER TABLE person ALTER prs_is_public_rv_fk TYPE bool USING CASE WHEN prs_is_public_rv_fk=1 THEN TRUE ELSE FALSE END;




