
-- erms-1037
-- 2019-02-26
-- execute before startup / after applying changelog-30.groovy
ALTER TABLE public.refdata_category DROP COLUMN rdc_soft_data;
ALTER TABLE public.refdata_value DROP COLUMN rdv_soft_data;

