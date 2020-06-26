databaseChangeLog = {

	changeSet(author: "klober (generated)", id: "1582026274560-1") {
		addColumn(schemaName: "public", tableName: "license") {
			column(name: "lic_is_public_for_api", type: "bool")
		}
	}

	changeSet(author: "klober (generated)", id: "1582026274560-2") {
		addColumn(schemaName: "public", tableName: "subscription") {
			column(name: "sub_is_public_for_api", type: "bool")
		}
	}

	changeSet(author: "kloberd (modified)", id: "1582026274560-3") {
		grailsChange {
			change {
				sql.execute("update license set lic_is_public_for_api=false where lic_is_public_for_api is null;")
				sql.execute("update subscription set sub_is_public_for_api=false where sub_is_public_for_api is null;")
			}
			rollback {
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1582026274560-4") {
		addNotNullConstraint(columnDataType: "bool", columnName: "lic_is_public_for_api", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1582026274560-5") {
		addNotNullConstraint(columnDataType: "bool", columnName: "sub_is_public_for_api", tableName: "subscription")
	}

	changeSet(author: "kloberd (generated)", id: "1582026274560-6") {
		dropColumn(columnName: "lic_is_public", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1582026274560-7") {
		dropColumn(columnName: "sub_is_public", tableName: "subscription")
	}

	changeSet(author: "kloberd (modified)", id: "1582026274560-8") {
		grailsChange {
			change {
				sql.execute("update api_source set as_active = false where as_active is null;")
			}
			rollback {
			}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1582026274560-9") {
		grailsChange {
			change {
				sql.execute("update cost_item set ci_final_cost_rounding = false where ci_final_cost_rounding is null;")
				sql.execute("update cost_item set ci_include_in_subscr = false where ci_include_in_subscr is null;")
				sql.execute("update cost_item set ci_is_viewable = false where ci_is_viewable is null;")
			}
			rollback {
			}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1582026274560-10") {
		grailsChange {
			change {
				sql.execute("update doc_context set dc_is_global = false where dc_is_global is null;")
				sql.execute("update doc_context set dc_is_shared = false where dc_is_shared is null;")
			}
			rollback {
			}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1582026274560-11") {
		grailsChange {
			change {
				sql.execute("update elasticsearch_source set ess_active = false where ess_active is null;")
				sql.execute("update elasticsearch_source set ess_gokb_es = false where ess_gokb_es is null;")
				sql.execute("update elasticsearch_source set ess_laser_es = false where ess_laser_es is null;")
			}
			rollback {
			}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1582026274560-12") {
		grailsChange {
			change {
				sql.execute("update global_record_source set grs_active = false where grs_active is null;")
			}
			rollback {
			}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1582026274560-13") {
		grailsChange {
			change {
				sql.execute("update global_record_tracker set grt_auto_pkg_update = false where grt_auto_pkg_update is null;")
				sql.execute("update global_record_tracker set grt_auto_tipp_add = false where grt_auto_tipp_add is null;")
				sql.execute("update global_record_tracker set grt_auto_tipp_del = false where grt_auto_tipp_del is null;")
				sql.execute("update global_record_tracker set grt_auto_tipp_update = false where grt_auto_tipp_update is null;")
			}
			rollback {
			}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1582026274560-14") {
		grailsChange {
			change {
				sql.execute("update org_role set or_is_shared = false where or_is_shared is null;")
			}
			rollback {
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1582026274560-15") {
		addNotNullConstraint(columnDataType: "bool", columnName: "as_active", tableName: "api_source")
	}

	changeSet(author: "kloberd (generated)", id: "1582026274560-16") {
		addNotNullConstraint(columnDataType: "bool", columnName: "ci_final_cost_rounding", tableName: "cost_item")
	}

	changeSet(author: "kloberd (generated)", id: "1582026274560-17") {
		addNotNullConstraint(columnDataType: "bool", columnName: "ci_include_in_subscr", tableName: "cost_item")
	}

	changeSet(author: "kloberd (generated)", id: "1582026274560-18") {
		addNotNullConstraint(columnDataType: "bool", columnName: "ci_is_viewable", tableName: "cost_item")
	}

	changeSet(author: "kloberd (generated)", id: "1582026274560-19") {
		addNotNullConstraint(columnDataType: "varchar(255)", columnName: "das_attribute_value_en", tableName: "dashboard_due_date")
	}

	changeSet(author: "kloberd (generated)", id: "1582026274560-20") {
		addNotNullConstraint(columnDataType: "bool", columnName: "dc_is_global", tableName: "doc_context")
	}

	changeSet(author: "kloberd (generated)", id: "1582026274560-21") {
		addNotNullConstraint(columnDataType: "bool", columnName: "dc_is_shared", tableName: "doc_context")
	}

	changeSet(author: "kloberd (generated)", id: "1582026274560-22") {
		addNotNullConstraint(columnDataType: "bool", columnName: "ess_active", tableName: "elasticsearch_source")
	}

	changeSet(author: "kloberd (generated)", id: "1582026274560-23") {
		addNotNullConstraint(columnDataType: "bool", columnName: "ess_gokb_es", tableName: "elasticsearch_source")
	}

	changeSet(author: "kloberd (generated)", id: "1582026274560-24") {
		addNotNullConstraint(columnDataType: "bool", columnName: "ess_laser_es", tableName: "elasticsearch_source")
	}

	changeSet(author: "kloberd (generated)", id: "1582026274560-25") {
		addNotNullConstraint(columnDataType: "bool", columnName: "grs_active", tableName: "global_record_source")
	}
}