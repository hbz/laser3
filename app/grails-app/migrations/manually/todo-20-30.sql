
-- erms-1037
-- 2019-02-26
-- execute before startup / local dev environment only
-- ALTER TABLE public.refdata_category DROP COLUMN rdv_soft_data;
-- ALTER TABLE public.refdata_value DROP COLUMN rdv_soft_data;

-- erms-1055
-- 2019-03-05
-- execute before startup / local dev environment only
-- ALTER TABLE public.property_definition DROP COLUMN pd_soft_data;

ALTER TABLE public.property_definition DROP COLUMN pd_soft_data;

-- erms-1072
-- 2019-03-08
-- execute before startup
update public.refdata_value set rdv_order = 0 where rdv_value = 'only for creator';
update public.refdata_value set rdv_order = 10, rdv_value = 'only for uploader context organisation' where rdv_value = 'only for author organisation';
update public.refdata_value set rdv_order = 20, rdv_value = 'only for uploader context and target organisation' where rdv_value = 'only for author and target organisation';
insert into public.refdata_value values(rdv_owner = 124, rdv_order = 30, rdv_value = 'only for consortia members', rdv_hard_data = true);
update public.refdata_value set rdv_order = 40 where rdv_id = 'everyone';