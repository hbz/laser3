
-- erms-1037
-- 2019-02-26
-- execute before startup / local dev environment only
-- ALTER TABLE public.refdata_category DROP COLUMN rdv_soft_data;
-- ALTER TABLE public.refdata_value DROP COLUMN rdv_soft_data;

-- erms-1055
-- 2019-03-05
-- execute before startup / local dev environment only
-- ALTER TABLE public.property_definition DROP COLUMN pd_soft_data;

-- ALTER TABLE public.property_definition DROP COLUMN pd_soft_data;

-- erms-1072
-- 2019-03-08
-- execute before startup
update public.refdata_value set rdv_order = 0 where rdv_value = 'only for creator';
update public.refdata_value set rdv_order = 10 where rdv_value = 'only for author organisation';
update public.refdata_value set rdv_order = 20 where rdv_value = 'only for author and target organisation';
update public.refdata_value set rdv_order = 30 where rdv_value = 'only for consortia members';
update public.refdata_value set rdv_order = 40 where rdv_value = 'everyone';