CREATE OR REPLACE FUNCTION org_merge_test(old_key bigint, new_key bigint)
    RETURNS void
    LANGUAGE plpgsql
    AS $$

-- execute manually on db concerned with select org_merge_test(old id, new id)

declare 
version constant numeric = 1;

oprow1 record;
oprow2 record;
oprow3 record;
oprow4 record;
oprow5 record;
oprow6 record;
orcount int;
orrow1 record;
orrow2 record;
orrow21 record;
orrow3 record;
orrow4 record;
osrow1 record;
osrow2 record;
osrow3 record;
osrow4 record;
prow1 record;
prrow1 record;
prrow2 record;
prrow3 record;
altname character varying;
newor bigint;
idcount bigint;
idrow1 record;
oldorgdata record;

BEGIN
	for altname in select altname_name from alternative_name where altname_org_fk = new_key
		loop
		delete from alternative_name where altname_name = altname and altname_org_fk = old_key;
		end loop;
	update alternative_name set altname_org_fk = new_key where altname_org_fk = old_key;
	for altname in select org_name from org where org_id = old_key
		loop
		insert into alternative_name (altname_version, altname_name, altname_org_fk) values (0, altname, new_key);
		end loop;
	select * into oldorgdata from org where org_id = old_key;
	update org set org_url = oldorgdata.org_url where org_id = new_key and org_url is null;
    delete from contact WHERE ct_org_fk = old_key and ct_content in (select ct_content from contact where ct_org_fk = new_key);
	update contact set ct_org_fk = new_key where ct_org_fk = old_key;
	delete from doc_context WHERE dc_org_fk = old_key and dc_doc_fk in (select dc_doc_fk from doc_context where dc_org_fk = new_key);
	update doc_context set dc_org_fk = new_key where dc_org_fk = old_key;
	update doc_context set dc_target_org_fk = new_key where dc_target_org_fk = old_key;
	select count(id_id) into idcount from identifier where id_org_fk = old_key;
	if(idcount > 0) then
		for idrow1 in select id_value, id_ns_fk from identifier where id_org_fk = new_key
			loop
			delete from identifier where id_org_fk = old_key and id_value = idrow1.id_value and id_ns_fk = idrow1.id_ns_fk;
			end loop;
	end if;
	--delete from identifier WHERE id_org_fk = old_key and id_value in (select id_value from identifier where id_org_fk = new_key);
	update identifier set id_org_fk = new_key where id_org_fk = old_key;
	for oprow1 in select op_string_value, op_type_fk from org_property join property_definition on op_type_fk = pd_id where op_owner_fk = new_key and pd_type = 'java.lang.String'
		loop
		delete from org_property where op_string_value = oprow1.op_string_value and op_type_fk = oprow1.op_type_fk and op_owner_fk = old_key;
		end loop;
	for oprow2 in select op_date_value, op_type_fk from org_property join property_definition on op_type_fk = pd_id where op_owner_fk = new_key and pd_type = 'java.util.Date'
		loop
		delete from org_property where op_date_value = oprow2.op_date_value and op_type_fk = oprow2.op_type_fk and op_owner_fk = old_key;
		end loop;
	for oprow3 in select op_dec_value, op_type_fk from org_property join property_definition on op_type_fk = pd_id where op_owner_fk = new_key and pd_type = 'java.math.BigDecimal'
		loop
		delete from org_property where op_dec_value = oprow3.op_dec_value and op_type_fk = oprow3.op_type_fk and op_owner_fk = old_key;
		end loop;
	for oprow4 in select op_int_value, op_type_fk from org_property join property_definition on op_type_fk = pd_id where op_owner_fk = new_key and pd_type = 'java.lang.Integer'
		loop
		delete from org_property where op_int_value = oprow4.op_int_value and op_type_fk = oprow4.op_type_fk and op_owner_fk = old_key;
		end loop;
	for oprow5 in select op_ref_value_rv_fk, op_type_fk from org_property join property_definition on op_type_fk = pd_id where op_owner_fk = new_key and pd_type = 'de.laser.RefdataValue'
		loop
		delete from org_property where op_ref_value_rv_fk = oprow5.op_ref_value_rv_fk and op_type_fk = oprow5.op_type_fk and op_owner_fk = old_key;
		end loop;
	for oprow6 in select op_url_value, op_type_fk from org_property join property_definition on op_type_fk = pd_id where op_owner_fk = new_key and pd_type = 'java.net.URL'
		loop
		delete from org_property where op_url_value = oprow6.op_url_value and op_type_fk = oprow6.op_type_fk and op_owner_fk = old_key;
		end loop;
	update org_property set op_owner_fk = new_key where op_owner_fk = old_key;
	select count(or_id) into orcount from org_role where or_org_fk = old_key;
	if(orcount > 0) then
		for orrow1 in select or_id, or_lic_fk from org_role where or_org_fk = new_key and or_lic_fk is not null
			loop
			delete from org_role where or_shared_from_fk = orrow1.or_id; --aiming one certain org_role object
			update org_role set or_shared_from_fk = orrow1.or_id where or_lic_fk in (select lic_id from license where lic_parent_lic_fk = orrow1.or_lic_fk) and or_org_fk = old_key;
			update org_role set or_is_shared = true where or_id = orrow1.or_id and exists (select or_id from org_role where or_shared_from_fk = orrow1.or_id);
			delete from org_role where or_lic_fk = orrow1.or_lic_fk and or_org_fk = old_key;
			end loop;
		for orrow2 in select or_id, or_sub_fk from org_role where or_org_fk = new_key and or_sub_fk is not null
			loop
			delete from org_role where or_shared_from_fk = orrow2.or_id; --aiming one certain org_role object
			update org_role set or_shared_from_fk = orrow2.or_id where or_sub_fk in (select sub_id from subscription where sub_parent_sub_fk = orrow2.or_sub_fk) and or_org_fk = old_key;
			update org_role set or_is_shared = true where or_id = orrow2.or_id and exists (select or_id from org_role where or_shared_from_fk = orrow2.or_id);
			delete from org_role where or_sub_fk = orrow2.or_sub_fk and or_org_fk = old_key;
			end loop;
		for orrow3 in select or_tipp_fk from org_role where or_org_fk = new_key and or_tipp_fk is not null
			loop
			delete from org_role where or_tipp_fk = orrow3.or_tipp_fk and or_org_fk = old_key;
			end loop;
		for orrow4 in select or_pkg_fk from org_role where or_org_fk = new_key and or_pkg_fk is not null
			loop
			delete from org_role where or_pkg_fk = orrow4.or_pkg_fk and or_org_fk = old_key;
			end loop;
		update org_role set or_org_fk = new_key where or_org_fk = old_key;
	end if;
	for osrow1 in select os_key_enum, os_string_value from org_setting where os_org_fk = new_key and os_string_value is not null
		loop
		delete from org_setting where os_string_value = osrow1.os_string_value and os_key_enum = osrow1.os_key_enum and os_org_fk = old_key;
		end loop;
	for osrow2 in select os_key_enum, os_rv_fk from org_setting where os_org_fk = new_key and os_rv_fk is not null
		loop
		delete from org_setting where os_rv_fk = osrow2.os_rv_fk and os_key_enum = osrow2.os_key_enum and os_org_fk = old_key;
		end loop;
	for osrow3 in select os_key_enum, os_role_fk from org_setting where os_org_fk = new_key and os_role_fk is not null
		loop
		delete from org_setting where os_role_fk = osrow3.os_role_fk and os_key_enum = osrow3.os_key_enum and os_org_fk = old_key;
		end loop;
	for osrow4 in select os_key_enum from org_setting where os_org_fk = new_key and os_string_value is null and os_rv_fk is null and os_role_fk is null
		loop
		delete from org_setting where os_key_enum = osrow4.os_key_enum and os_string_value is null and os_rv_fk is null and os_role_fk is null and os_org_fk = old_key;
		end loop;
	update org_setting set os_org_fk = new_key where os_org_fk = old_key;
	delete from task WHERE tsk_org_fk = old_key and tsk_creator_fk in (select tsk_creator_fk from task where tsk_org_fk = new_key);
	update task set tsk_org_fk = new_key where tsk_org_fk = old_key;
	delete from org_type where org_id = old_key;
	for prrow1 in select pr_function_type_rv_fk, pr_prs_fk from person_role where pr_org_fk = new_key and pr_function_type_rv_fk is not null
		loop
		delete from person_role where pr_org_fk = old_key and pr_prs_fk = prrow1.pr_prs_fk and pr_function_type_rv_fk = prrow1.pr_function_type_rv_fk;
		end loop;
	for prrow2 in select pr_responsibility_type_rv_fk, pr_prs_fk from person_role where pr_org_fk = new_key and pr_responsibility_type_rv_fk is not null
		loop
		delete from person_role where pr_org_fk = old_key and pr_prs_fk = prrow2.pr_prs_fk and pr_responsibility_type_rv_fk = prrow2.pr_responsibility_type_rv_fk;
		end loop;
	for prrow3 in select pr_position_type_rv_fk, pr_prs_fk from person_role where pr_org_fk = new_key and pr_position_type_rv_fk is not null
		loop
		delete from person_role where pr_org_fk = old_key and pr_prs_fk = prrow3.pr_prs_fk and pr_position_type_rv_fk = prrow3.pr_position_type_rv_fk;
		end loop;
	update person_role set pr_org_fk = new_key where pr_org_fk = old_key;
	-- prs_last_name must be always set whereas prs_first_name may be null, e.g. "Technischer Support"
	for prow1 in select prs_first_name, prs_last_name from person where prs_tenant_fk = new_key and prs_last_name is not null
		loop
		delete from person where prs_tenant_fk = old_key and prs_first_name = prow1.prs_first_name and prs_last_name = prow1.prs_last_name;
		end loop;
	update person set prs_tenant_fk = new_key where prs_tenant_fk = old_key;
	update platform set plat_org_fk = new_key where plat_org_fk = old_key; --otherwise, we would have platform doublets
	delete from org where org_id = old_key;
	raise notice 'done';
END;
$$