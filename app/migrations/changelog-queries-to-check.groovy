
// -- changelog-20

changeSet(author: "kloberd (manually)", id: "1548252520602-10") {
	grailsChange {
		change {
			sql.execute("UPDATE public.license SET lic_ref = 'Name fehlt', lic_sortable_ref = 'name fehlt' WHERE lic_ref IS null")
		}
		confirm 'Updated Table Data'
	}
}
// select lic_ref from license where lic_ref is null = EMPTY = GOOD

changeSet(author: "kloberd (manually)", id: "1548252520602-25") {
	grailsChange {
		change {
			sql.execute("UPDATE public.property_definition SET pd_used_for_logic = false WHERE pd_used_for_logic IS null")
		}
		confirm 'Updated Table Data'
	}
}
//select pd_used_for_logic from property_definition where pd_used_for_logic is null = EMPTY = GOOD

// -- changelog-40

changeSet(author: "kloberd (modified)", id: "1553857368060-18") {
	grailsChange {
		change {
			sql.execute("DELETE FROM public.user_org WHERE status = 4")
			sql.execute("UPDATE public.user_org SET status = 1 WHERE status = 3")
		}
		rollback {}
	}
}
//select * from user_org where status = 4 = EMPTY = GOOD

// -- changelog-60

changeSet(author: "kloberd (modified)", id: "1558613037606-22") {
	grailsChange {
		change {
			sql.execute("update license set lic_status_rv_fk = (select rdv_id from refdata_value where rdv_value = 'Status not defined' and rdv_owner = (select rdc_id from refdata_category where rdc_description = 'License Status')) where lic_status_rv_fk is null")
		}
		rollback {}
	}
}
//select lic_status_rv_fk from license where lic_status_rv_fk is null = EMPTY = GOOD

changeSet(author: "kloberd (modified)", id: "1558613037606-23") {
	grailsChange {
		change {
			sql.execute("update subscription set sub_status_rv_fk = (select rdv_id from refdata_value where rdv_value = 'Status not defined' and rdv_owner = (select rdc_id from refdata_category where rdc_description = 'Subscription Status')) where sub_status_rv_fk is null")
		}
		rollback {}
	}
}
//select sub_status_rv_fk from subscription where sub_status_rv_fk is null = EMPTY = GOOD

changeSet(author: "kloberd (modified)", id: "1558613037606-26") {
	grailsChange {
		change {
			sql.execute("update role set authority = 'ORG_BASIC_MEMBER' where authority = 'ORG_MEMBER'")
			sql.execute("update role set authority = 'ORG_INST' where authority = 'ORG_BASIC'")
			sql.execute("update role set authority = 'ORG_INST_COLLECTIVE' where authority = 'ORG_COLLECTIVE'")

			sql.execute("update perm set code = 'org_basic_member' where code = 'org_member'")
			sql.execute("update perm set code = 'org_inst' where code = 'org_basic'")
			sql.execute("update perm set code = 'org_inst_collective' where code = 'org_collective'")
		}
		rollback {}
	}
}
// GOOD


// -- changelog-70

changeSet(author: "kloberd (modified)", id: "1561453819381-14") {
	grailsChange {
		change {
			sql.execute("UPDATE subscription SET sub_is_administrative = false WHERE sub_is_administrative IS NULL")
		}
		rollback {}
	}
}
//select sub_is_administrative from subscription where sub_is_administrative is null = EMPTY = GOOD

// -- changelog-75

changeSet(author: "kloberd (modified)", id: "1561617411135-3") {
	grailsChange {
		change {
			sql.execute("update identifier_namespace set idns_unique = false where idns_non_unique = true")
			sql.execute("update identifier_namespace set idns_unique = true where (idns_non_unique = false or idns_non_unique is null)")
		}
		rollback {}
	}
}
// Keine Daten

changeSet(author: "kloberd (modified)", id: "1561617411135-6") {
	grailsChange {
		change {
			sql.execute("UPDATE refdata_value SET rdv_value = 'Responsible Admin' WHERE rdv_value = 'Responsible Contact'")
		}
		rollback {}
	}
}
// select rdv_value from refdata_value WHERE rdv_value = 'Responsible Contact' = EMPTY = GOOD

// -- changelog-76

changeSet(author: "kloberd (modified)", id: "1562147170838-1") {
	grailsChange {
		change {
			sql.execute("""
update issue_entitlement set ie_status_rv_fk = (
	SELECT v.rdv_id FROM refdata_value v JOIN refdata_category c ON (v.rdv_owner = c.rdc_id) WHERE c.rdc_description = 'TIPP Status' and v.rdv_value = 'Current'
) where ie_status_rv_fk in (
	SELECT v.rdv_id FROM refdata_value v JOIN refdata_category c ON (v.rdv_owner = c.rdc_id) WHERE c.rdc_description = 'Entitlement Issue Status' and (
		v.rdv_value = 'Live' or v.rdv_value = 'Current'
	)
)
""")
		}
		rollback {}
	}
}

//select ie_status_rv_fk from issue_entitlement where ie_status_rv_fk in (
//    SELECT v.rdv_id
//    FROM refdata_value v
//             JOIN refdata_category c ON (v.rdv_owner = c.rdc_id)
//    WHERE c.rdc_description = 'Entitlement Issue Status'
//      and (
//        v.rdv_value = 'Live' or v.rdv_value = 'Current'
//        )) = EMPTY = GOOD

changeSet(author: "kloberd (modified)", id: "1562147170838-2") {
	grailsChange {
		change {
			sql.execute("""
update issue_entitlement set ie_status_rv_fk = (
	SELECT v.rdv_id FROM refdata_value v JOIN refdata_category c ON (v.rdv_owner = c.rdc_id) WHERE c.rdc_description = 'TIPP Status' and v.rdv_value = 'Deleted'
) where ie_status_rv_fk = (
	SELECT v.rdv_id FROM refdata_value v JOIN refdata_category c ON (v.rdv_owner = c.rdc_id) WHERE c.rdc_description = 'Entitlement Issue Status' and v.rdv_value = 'Deleted'
)
""")
		}
		rollback {}
	}
}
//select ie_status_rv_fk from issue_entitlement  where ie_status_rv_fk = (
//    SELECT v.rdv_id FROM refdata_value v JOIN refdata_category c ON (v.rdv_owner = c.rdc_id) WHERE c.rdc_description = 'Entitlement Issue Status' and v.rdv_value = 'Deleted')
//    = EMPTY = GOOD

// -- changelog-80

changeSet(author: "kloberd (modified)", id: "1563450092883-3") {
	grailsChange {
		change {
			sql.execute("""
UPDATE combo SET combo_type_rv_fk = (
		SELECT rv.rdv_id FROM refdata_value rv JOIN refdata_category rc ON rv.rdv_owner = rc.rdc_id
        	WHERE rc.rdc_description = 'Combo Type' AND rv.rdv_value = 'Consortium'
) WHERE combo_type_rv_fk ISNULL
""")
		}
		rollback {}
	}
}
//select combo_type_rv_fk from combo  WHERE combo_type_rv_fk ISNULL = EMPTY = GOOD

// -- changelog-90

changeSet(author: "kloberd (modified)", id: "1565864968228-13") {
	grailsChange {
		change {
			sql.execute(
					"UPDATE license SET lic_is_slaved_tmp = ( " +
							"CASE WHEN lic_is_slaved = (SELECT rdv_id FROM refdata_value WHERE rdv_value = 'Yes' ORDER BY rdv_id LIMIT(1)) " +
							"THEN TRUE ELSE FALSE END )"
			)
		}
		rollback {}
	}
}

changeSet(author: "kloberd (modified)", id: "1565864968228-15") {
	grailsChange {
		change {
			sql.execute(
					"UPDATE package SET pkg_is_public_tmp = ( " +
							"CASE WHEN pkg_is_public = (SELECT rdv_id FROM refdata_value WHERE rdv_value = 'Yes' ORDER BY rdv_id LIMIT(1)) " +
							"THEN TRUE ELSE FALSE END )"
			)
		}
		rollback {}
	}
}

changeSet(author: "kloberd (modified)", id: "1565864968228-19") {
	grailsChange {
		change {
			sql.execute(
					"UPDATE subscription SET sub_is_public_tmp = ( " +
							"CASE WHEN sub_is_public = (SELECT rdv_id FROM refdata_value WHERE rdv_value = 'Yes' ORDER BY rdv_id LIMIT(1)) " +
							"THEN TRUE ELSE FALSE END )"
			)
		}
		rollback {}
	}
}

changeSet(author: "kloberd (modified)", id: "1565864968228-21") {
	grailsChange {
		change {
			sql.execute(
					"UPDATE subscription SET sub_is_slaved_tmp = ( " +
							"CASE WHEN sub_is_slaved = (SELECT rdv_id FROM refdata_value WHERE rdv_value = 'Yes' ORDER BY rdv_id LIMIT(1)) " +
							"THEN TRUE ELSE FALSE END )"
			)
		}
		rollback {}
	}
}

changeSet(author: "kloberd (modified)", id: "1565864968228-67") {
	grailsChange {
		change {
			sql.execute(
					"UPDATE property_definition_group_binding SET pbg_is_visible = ( " +
							"CASE WHEN pbg_visible_rv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_value = 'Yes' ORDER BY rdv_id LIMIT(1)) " +
							"THEN TRUE ELSE FALSE END)"
			)
		}
		rollback {}
	}
}

changeSet(author: "kloberd (modified)", id: "1565864968228-68") {
	grailsChange {
		change {
			sql.execute(
					"UPDATE property_definition_group_binding SET pbg_is_visible_for_cons_member = ( " +
							"CASE WHEN pbg_is_viewable_rv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_value = 'Yes' ORDER BY rdv_id LIMIT(1)) " +
							"THEN TRUE ELSE FALSE END)"
			)
		}
		rollback {}
	}
}

changeSet(author: "kloberd (modified)", id: "1565864968228-69") {
	grailsChange {
		change {
			sql.execute(
					"UPDATE license SET lic_is_public = ( " +
							"CASE WHEN lic_is_public_rdv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_value = 'Yes' ORDER BY rdv_id LIMIT(1)) " +
							"THEN TRUE ELSE FALSE END )"
			)
		}
		rollback {}
	}
}

changeSet(author: "kloberd (modified)", id: "1565864968228-70") {
	grailsChange {
		change {
			sql.execute(
					"UPDATE person SET prs_is_public = ( " +
							"CASE WHEN prs_is_public_rv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_value = 'Yes' ORDER BY rdv_id LIMIT(1)) " +
							"THEN TRUE ELSE FALSE END)"
			)
		}
		rollback {}
	}
}

changeSet(author: "kloberd (modified)", id: "1565864968228-71") {
	grailsChange {
		change {
			sql.execute(
					"UPDATE property_definition_group SET pdg_is_visible = ( " +
							"CASE WHEN pdg_visible_rv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_value = 'Yes' ORDER BY rdv_id LIMIT(1)) " +
							"THEN TRUE ELSE FALSE END)"
			)
		}
		rollback {}
	}
}

// -- changelog-95

changeSet(author: "kloberd (modified)", id: "1566384977224-10") {
	grailsChange {
		change {
			sql.execute("UPDATE org_settings SET os_key_enum = 'OAMONITOR_SERVER_ACCESS' WHERE os_key_enum = 'OA2020_SERVER_ACCESS'")
			sql.execute("UPDATE org_settings SET os_key_enum = 'NATSTAT_SERVER_ACCESS' WHERE os_key_enum = 'STATISTICS_SERVER_ACCESS'")
		}
		rollback {}
	}
}
//select os_key_enum from org_settings  WHERE os_key_enum = 'OA2020_SERVER_ACCESS' = EMPTY = GOOD
//select os_key_enum from org_settings  WHERE os_key_enum = 'STATISTICS_SERVER_ACCESS' = EMPTY = GOOD

// -- changelog-97

changeSet(author: "kloberd (modified)", id: "1566540202427-2") {
	grailsChange {
		change {
			sql.execute("UPDATE survey_config SET surconf_evaluation_finish = false WHERE surconf_evaluation_finish is null")
		}
		rollback {}
	}
}
// select surconf_evaluation_finish from survey_config  WHERE surconf_evaluation_finish is null = EMPTY = GOOD


// -- changelog-98

changeSet(author: "kloberd (modified)", id: "1566992531378-4") {
	grailsChange {
		change {
			sql.execute("UPDATE survey_info SET surin_is_subscription_survey = true")
		}
		rollback {}
	}
}

//select surin_is_subscription_survey from survey_info  WHERE surin_is_subscription_survey is null = EMPTY = GOOD

changeSet(author: "kloberd (modified)", id: "1566992531378-5") {
	grailsChange {
		change {
			sql.execute("UPDATE survey_config SET surconf_is_subscription_survey_fix = true WHERE surconf_sub_fk IS NOT NULL")
			sql.execute("UPDATE survey_config SET surconf_is_subscription_survey_fix = false WHERE surconf_sub_fk IS NULL")
		}
		rollback {}
	}
}

//select surconf_is_subscription_survey_fix from survey_config  WHERE surconf_sub_fk IS NOT NULL = GOOD
//select surconf_is_subscription_survey_fix from survey_config  WHERE surconf_sub_fk IS NULL = EMPTY = GOOD

changeSet(author: "kloberd (modified)", id: "1566992531378-6") {
	grailsChange {
		change {
			sql.execute("update i10n_translation set i10n_value_de = 'Wissenschaftliche Spezialbibliothek' where i10n_value_de = 'Wissenschafltiche Spezialbibliothek'")
			sql.execute("update i10n_translation set i10n_value_en = 'Wissenschaftliche Spezialbibliothek' where i10n_value_en = 'Wissenschafltiche Spezialbibliothek'")
			sql.execute("update refdata_value set rdv_value = 'Wissenschaftliche Spezialbibliothek' where rdv_value = 'Wissenschafltiche Spezialbibliothek'")
		}
		rollback {}
	}
}

// -- changelog-100

changeSet(author: "djebeniani (modified)", id: "1569389997414-25") {
	grailsChange {
		change {
			sql.execute("UPDATE task SET tsk_system_create_date = tsk_create_date where tsk_system_create_date IS NULL")
			sql.execute("ALTER TABLE task ALTER COLUMN tsk_system_create_date SET NOT NULL")
		}
		rollback {}
	}
}
// select tsk_system_create_date from task where tsk_system_create_date IS NULL = EMPTY = GOOD

changeSet(author: "djebeniani (modified)", id: "1569389997414-26") {
	grailsChange {
		change {
			sql.execute("UPDATE issue_entitlement SET ie_accept_status_rv_fk = (SELECT rdv.rdv_id FROM refdata_value rdv\n" +
					"    JOIN refdata_category rdc ON rdv.rdv_owner = rdc.rdc_id\n" +
					"WHERE rdv.rdv_value = 'Fixed' AND rdc.rdc_description = 'IE Accept Status') where\n" +
					"ie_id IN (SELECT ie_id FROM issue_entitlement JOIN refdata_value rv ON issue_entitlement.ie_status_rv_fk = rv.rdv_id\n" +
					"WHERE rdv_value = 'Current')")
		}
		rollback {}
	}
}

//select ie_accept_status_rv_fk from issue_entitlement
//where ie_id IN
//      (SELECT ie_id FROM issue_entitlement JOIN refdata_value rv ON issue_entitlement.ie_status_rv_fk = rv.rdv_id
//					WHERE rdv_value = 'Current') = NOT EMPTY = NOT GOOD

changeSet(author: "djebeniani (modified)", id: "1569389997414-27") {
	grailsChange {
		change {
			sql.execute("UPDATE dashboard_due_date SET das_is_hide = false where das_is_hide IS NULL")
			sql.execute("ALTER TABLE dashboard_due_date ALTER COLUMN das_is_hide SET NOT NULL")
			sql.execute("UPDATE dashboard_due_date SET das_is_done = false where das_is_done IS NULL")
			sql.execute("ALTER TABLE dashboard_due_date ALTER COLUMN das_is_done SET NOT NULL")
		}
		rollback {}
	}
}
//select das_is_hide from dashboard_due_date where das_is_hide IS NULL = EMPTY = GOOD
//select das_is_done from dashboard_due_date where das_is_done IS NULL = EMPTY = GOOD

// -- changelog-101

changeSet(author: "kloberd (modified)", id: "1569915031100-3") {
	grailsChange {
		change {
			sql.execute("UPDATE survey_result SET surre_is_required = false WHERE surre_is_required IS NULL")
		}
		rollback {}
	}
}
//select surre_is_required from survey_result WHERE surre_is_required IS NULL = EMPTY = GOOD

//ALL QUERYS
/*select lic_ref from license where lic_ref is null;
select pd_used_for_logic from property_definition where pd_used_for_logic is null;
select * from user_org where status = 4;
select lic_status_rv_fk from license where lic_status_rv_fk is null;
select sub_status_rv_fk from subscription where sub_status_rv_fk is null;
select sub_is_administrative from subscription where sub_is_administrative is null;
select rdv_value from refdata_value WHERE rdv_value = 'Responsible Contact';
select ie_status_rv_fk from issue_entitlement where ie_status_rv_fk in (
		SELECT v.rdv_id
FROM refdata_value v
JOIN refdata_category c ON (v.rdv_owner = c.rdc_id)
WHERE c.rdc_description = 'Entitlement Issue Status'
and (
		v.rdv_value = 'Live' or v.rdv_value = 'Current'
));
select ie_status_rv_fk from issue_entitlement  where ie_status_rv_fk = (SELECT v.rdv_id FROM refdata_value v JOIN refdata_category c ON (v.rdv_owner = c.rdc_id) WHERE c.rdc_description = 'Entitlement Issue Status' and v.rdv_value = 'Deleted');
select combo_type_rv_fk from combo  WHERE combo_type_rv_fk ISNULL;
select os_key_enum from org_settings  WHERE os_key_enum = 'OA2020_SERVER_ACCESS';
select os_key_enum from org_settings  WHERE os_key_enum = 'STATISTICS_SERVER_ACCESS';
select surconf_evaluation_finish from survey_config  WHERE surconf_evaluation_finish is null;
select surin_is_subscription_survey from survey_info  WHERE surin_is_subscription_survey is null;
select surconf_is_subscription_survey_fix from survey_config  WHERE surconf_sub_fk IS NOT NULL;
select surconf_is_subscription_survey_fix from survey_config  WHERE surconf_sub_fk IS NULL;
select tsk_system_create_date from task where tsk_system_create_date IS NULL;
select ie_accept_status_rv_fk from issue_entitlement where ie_id IN (SELECT ie_id FROM issue_entitlement JOIN refdata_value rv ON issue_entitlement.ie_status_rv_fk = rv.rdv_id WHERE rdv_value = 'Current');
select das_is_hide from dashboard_due_date where das_is_hide IS NULL;
select das_is_done from dashboard_due_date where das_is_done IS NULL;
select surre_is_required from survey_result WHERE surre_is_required IS NULL;*/

