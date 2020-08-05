
-- erms-886
-- 2019-01-15
-- dbCreate = "update" problem; workaround until changelog-20.groovy
-- ALTER TABLE public.property_definition ADD COLUMN IF NOT EXISTS pd_used_for_logic boolean DEFAULT false;

-- erms-930
-- 2019-01-23
-- execute before startup / before applying changelog-20.groovy
-- UPDATE public.license SET lic_ref = 'Name fehlt', lic_sortable_ref = 'name fehlt' WHERE lic_ref IS null;

-- 2019-01-23
-- execute before startup / after applying changelog-20.groovy
-- UPDATE property_definition SET pd_used_for_logic = false WHERE pd_used_for_logic IS null;

-- erms-934
-- 2019-01-24
-- execute before startup / after applying changelog-20.groovy
UPDATE i10n_translation SET i10n_value_fr = null WHERE i10n_reference_field = 'expl' AND i10n_value_fr = 'null';
UPDATE i10n_translation SET i10n_value_en = null WHERE i10n_reference_field = 'expl' AND i10n_value_en = 'null';
UPDATE i10n_translation SET i10n_value_de = null WHERE i10n_reference_field = 'expl' AND i10n_value_de = 'null';