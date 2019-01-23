
-- erms-886
ALTER TABLE public.property_definition ADD COLUMN IF NOT EXISTS pd_used_for_logic boolean DEFAULT false;

-- erms-930
-- execute before startup
UPDATE public.license SET lic_ref = 'Name fehlt',lic_sortable_ref = 'name fehlt' WHERE lic_ref is null