CREATE OR REPLACE FUNCTION PROPERTY_MIGRATION()
    RETURNS void
    LANGUAGE plpgsql
    AS $$

DECLARE
    VERSION CONSTANT NUMERIC = 1;

  old_prop_def  property_definition%ROWTYPE;
  old_rdc       refdata_category%ROWTYPE;

  old_rdv1      refdata_value%ROWTYPE;
  old_rdv2      refdata_value%ROWTYPE;
  old_rdv3      refdata_value%ROWTYPE;
  old_rdv4      refdata_value%ROWTYPE;
  old_rdv5      refdata_value%ROWTYPE;
  old_rdv6      refdata_value%ROWTYPE;

  new_prop_def  property_definition%ROWTYPE;
  new_rdc       refdata_category%ROWTYPE;

  new_rdv1      refdata_value%ROWTYPE;
  new_rdv2      refdata_value%ROWTYPE;
  new_rdv3      refdata_value%ROWTYPE;
  new_rdv4      refdata_value%ROWTYPE;
  new_rdv5      refdata_value%ROWTYPE;
  new_rdv6      refdata_value%ROWTYPE;

  todo RECORD;

BEGIN
  /* -- old refdata category -- */
  select into strict old_rdc * from public.refdata_category where rdc_description = 'YNO';
  /* -- old property definition -- */
  select into strict old_prop_def * from public.property_definition where pd_name = 'Walk In Access' and pd_rdc = old_rdc.rdc_description;
  /* -- old refdata values -- */
  select into strict old_rdv1 * from public.refdata_value where rdv_value = 'Yes' and rdv_owner = old_rdc.rdc_id;
  select into strict old_rdv2 * from public.refdata_value where rdv_value = 'No' and rdv_owner = old_rdc.rdc_id;
  select into strict old_rdv3 * from public.refdata_value where rdv_value = 'Other' and rdv_owner = old_rdc.rdc_id;
  select into strict old_rdv4 * from public.refdata_value where rdv_value = 'Not applicable' and rdv_owner = old_rdc.rdc_id;
  select into strict old_rdv5 * from public.refdata_value where rdv_value = 'Unknown' and rdv_owner = old_rdc.rdc_id;
  select into strict old_rdv6 * from public.refdata_value where rdv_value = 'Planed' and rdv_owner = old_rdc.rdc_id;

  /* -- new refdata category -- */
  select into strict new_rdc * from public.refdata_category where rdc_description = 'Permissions';
  /* -- new property definition -- */
  select into strict new_prop_def * from public.property_definition where pd_name = 'Walk-in Access' and pd_rdc = new_rdc.rdc_description;
  /* -- new refdata values -- */
  select into strict new_rdv1 * from public.refdata_value where rdv_value = 'Permitted (explicit)' and rdv_owner = new_rdc.rdc_id;
  select into strict new_rdv2 * from public.refdata_value where rdv_value = 'Prohibited (explicit)' and rdv_owner = new_rdc.rdc_id;
  select into strict new_rdv3 * from public.refdata_value where rdv_value = 'Unknown' and rdv_owner = new_rdc.rdc_id;
  select into strict new_rdv4 * from public.refdata_value where rdv_value = 'Not applicable' and rdv_owner = new_rdc.rdc_id;
  select into strict new_rdv5 * from public.refdata_value where rdv_value = 'Unknown' and rdv_owner = new_rdc.rdc_id;
  select into strict new_rdv6 null;

  raise notice 'FROM:  %, %', old_prop_def, old_rdc;
  raise notice 'TO:    %, %', new_prop_def, new_rdc;
  raise notice '-----------------------------------------------';
  raise notice 'FROM:  %  TO:  %', old_rdv1, new_rdv1;
  raise notice 'FROM:  %  TO:  %', old_rdv2, new_rdv2;
  raise notice 'FROM:  %  TO:  %', old_rdv3, new_rdv3;
  raise notice 'FROM:  %  TO:  %', old_rdv4, new_rdv4;
  raise notice 'FROM:  %  TO:  %', old_rdv5, new_rdv5;
  raise notice 'FROM:  %  TO:  %', old_rdv6, new_rdv6;
  raise notice '-----------------------------------------------';

  FOR todo IN (select * from public.license_custom_property where type_id = old_prop_def.pd_id) LOOP
    raise notice 'migrate property_type: % => %', todo.type_id, new_prop_def.pd_id;
    execute 'update public.license_custom_property set type_id = $1 where id = $2' using new_prop_def.pd_id, todo.id;

    if todo.ref_value_id = old_rdv1.rdv_id THEN
      raise notice 'migrate type 1 => %', todo;
      execute 'update public.license_custom_property set ref_value_id = $1 where id = $2' using new_rdv1.rdv_id, todo.id;

    elseif todo.ref_value_id = old_rdv2.rdv_id THEN
      raise notice 'migrate type 2 => %', todo;
      execute 'update public.license_custom_property set ref_value_id = $1 where id = $2' using new_rdv2.rdv_id, todo.id;

    elseif todo.ref_value_id = old_rdv3.rdv_id THEN
      raise notice 'migrate type 3 => %', todo;
      execute 'update public.license_custom_property set ref_value_id = $1 where id = $2' using new_rdv3.rdv_id, todo.id;

    elseif todo.ref_value_id = old_rdv4.rdv_id THEN
      raise notice 'migrate type 4 => %', todo;
      execute 'update public.license_custom_property set ref_value_id = $1 where id = $2' using new_rdv4.rdv_id, todo.id;

    elseif todo.ref_value_id = old_rdv5.rdv_id THEN
      raise notice 'migrate type 5 => %', todo;
      execute 'update public.license_custom_property set ref_value_id = $1 where id = $2' using new_rdv5.rdv_id, todo.id;

    elseif todo.ref_value_id = old_rdv6.rdv_id THEN
      raise notice 'migrate type 6 => %', todo;
      execute 'update public.license_custom_property set ref_value_id = $1 where id = $2' using new_rdv6.rdv_id, todo.id;

    else
      raise notice 'null value  ====> %', todo;
    end if;
  END LOOP;

END;
$$;
