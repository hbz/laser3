
-- erms-1074
-- 2019-03-11
-- clean up data / local dev environment only
UPDATE combo SET combo_type_rv_fk = (
  SELECT rdv.rdv_id from refdata_value rdv join refdata_category rdc on rdv.rdv_owner = rdc.rdc_id where rdv.rdv_value = 'Consortium' and rdc.rdc_description = 'Combo Type'
);
where combo_type_rv_fk = (
  SELECT rdv.rdv_id from refdata_value rdv join refdata_category rdc on rdv.rdv_owner = rdc.rdc_id where rdv.rdv_value = 'Consortium' and rdc.rdc_description = 'OrgType'
);

-- erms-1072
-- 2019-03-08
-- execute after bootstrap loc()-calls
update public.refdata_value set rdv_order = 0 where rdv_value = 'only for creator';
update public.refdata_value set rdv_order = 10 where rdv_value = 'only for author organisation';
update public.refdata_value set rdv_order = 20 where rdv_value = 'only for author and target organisation';
update public.refdata_value set rdv_order = 30 where rdv_value = 'only for consortia members';
update public.refdata_value set rdv_order = 40 where rdv_value = 'everyone';

-- erms-1081
-- 2019-03-08
-- clean up data
-- DELETE FROM public.setting WHERE set_name = 'AutoApproveMemberships';
-- DELETE FROM public.user_org WHERE status = 4; -- STATUS_CANCELLED
-- UPDATE public.user_org SET status = 1 WHERE status = 3; -- change STATUS_AUTO_APPROVED to STATUS_APPROVED

-- 2019-03-26
-- renaming joinTable / execute before start
-- ALTER TABLE public.org_roletype RENAME TO org_type;