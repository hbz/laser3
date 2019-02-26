
-- erms-1037
-- 2019-02-26
-- execute before startup / local dev environment only
ALTER TABLE public.refdata_category DROP COLUMN rdc_soft_data;
ALTER TABLE public.refdata_value DROP COLUMN rdv_soft_data;

