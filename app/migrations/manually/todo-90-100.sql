-- 2019-08-21
-- change column mappings, set default for user.date_created

ALTER TABLE public.org RENAME date_created TO org_date_created;
ALTER TABLE public.org RENAME last_updated TO org_last_updated;

ALTER TABLE public."user" ADD COLUMN date_created timestamp without time zone;
ALTER TABLE public."user" ADD COLUMN last_updated timestamp without time zone;

-- July 18th was the last date when the QA database has been reset
UPDATE public."user" SET date_created = '2019-07-18 00:00:00.0',last_updated = '2019-07-18 00:00:00.0' where date_created is null;

ALTER TABLE public."user" ALTER COLUMN date_created set not null;
ALTER TABLE public."user" ALTER COLUMN date_created set default now();
alter table public."user" alter column last_updated set not null;
alter table public."user" alter column last_updated set default now();