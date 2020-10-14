-- erms-1147
-- 2019-04-11
-- execute before startup
-- alter table numbers rename to reader_number;
-- alter table reader_number drop column num_end_date;
-- alter table reader_number rename num_number to num_value;
-- alter table reader_number rename column num_start_date to num_due_date;
-- alter table reader_number rename column num_typ_rdv_fk to num_reference_group;
-- alter table reader_number drop constraint fk88c28e4a3026029e;
-- alter table reader_number alter column num_reference_group type varchar(255) using num_reference_group::varchar(255);
update reader_number set num_reference_group = 'Studenten' where num_reference_group ~ '^\d+(\.\d+)?$' AND CAST (num_reference_group AS BIGINT) = (select i10n_reference_id from i10n_translation where i10n_value_de = 'Studenten');
update reader_number set num_reference_group = 'wissenschaftliches Personal' where num_reference_group ~ '^\d+(\.\d+)?$' AND CAST (num_reference_group AS BIGINT) = (select i10n_reference_id from i10n_translation where i10n_value_de = 'wissenschaftliches Personal');
update reader_number set num_reference_group = 'Nutzer' where num_reference_group ~ '^\d+(\.\d+)?$' AND CAST (num_reference_group AS BIGINT) = (select i10n_reference_id from i10n_translation where i10n_value_de = 'Nutzer');
update reader_number set num_reference_group = 'Einwohner' where num_reference_group ~ '^\d+(\.\d+)?$' AND CAST (num_reference_group AS BIGINT) = (select i10n_reference_id from i10n_translation where i10n_value_de = 'Einwohner');