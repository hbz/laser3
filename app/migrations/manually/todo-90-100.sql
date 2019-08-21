-- 2019-08-21
-- change column mappings, set default for user.date_created

ALTER TABLE public.org RENAME date_created  TO org_date_created;
ALTER TABLE public.org RENAME last_updated  TO org_last_updated;