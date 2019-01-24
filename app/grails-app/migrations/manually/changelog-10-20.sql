
-- erms-886
-- 2019-01-15
-- dbCreate = "update" problem; workaround until changelog-20.groovy
-- ALTER TABLE public.property_definition ADD COLUMN IF NOT EXISTS pd_used_for_logic boolean DEFAULT false;

-- erms-930
-- 2019-01-23
-- execute before startup / before applying changelog-20.groovy
-- changeSet: 1548252520602-24
-- UPDATE public.license SET lic_ref = 'Name fehlt', lic_sortable_ref = 'name fehlt' WHERE lic_ref IS null;

-- 2019-01-23
-- execute before startup / after applying changelog-20.groovy
-- changeSet: 1548252520602-25
-- UPDATE property_definition SET pd_used_for_logic = false WHERE pd_used_for_logic IS null;