databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1571820746491-1") {
		addColumn(schemaName: "public", tableName: "org_access_point") {
			column(name: "class", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1571820746491-2") {
		addColumn(schemaName: "public", tableName: "org_access_point") {
			column(name: "oar_entity_id", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1571820746491-3") {
		addColumn(schemaName: "public", tableName: "org_access_point") {
			column(name: "oar_url", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1571820746491-4") {
		addColumn(schemaName: "public", tableName: "org_access_point") {
			column(name: "url", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1571820746491-5") {
		addNotNullConstraint(columnDataType: "bool", columnName: "sub_is_multi_year", tableName: "subscription")
	}

	changeSet(author: "kloberd (generated)", id: "1571820746491-6") {
		addNotNullConstraint(columnDataType: "timestamp", columnName: "date_created", tableName: "user")
	}

	changeSet(author: "kloberd (generated)", id: "1571820746491-7") {
		addNotNullConstraint(columnDataType: "timestamp", columnName: "last_updated", tableName: "user")
	}

	changeSet(author: "kloberd (generated)", id: "1571820746491-8") {
		dropColumn(columnName: "org_origin_edit_url", tableName: "org")
	}

	changeSet(author: "kloberd (generated)", id: "1571820746491-9") {
		dropColumn(columnName: "pkg_origin_edit_url", tableName: "package")
	}

	changeSet(author: "kloberd (generated)", id: "1571820746491-10") {
		dropColumn(columnName: "plat_origin_edit_url", tableName: "platform")
	}

	changeSet(author: "kloberd (generated)", id: "1571820746491-11") {
		dropColumn(columnName: "ti_origin_edit_url", tableName: "title_instance")
	}

	changeSet(author: "kloberd (generated)", id: "1571820746491-12") {
		dropColumn(columnName: "tipp_origin_edit_url", tableName: "title_instance_package_platform")
	}

	changeSet(author: "kloberd (modified)", id: "1571820746491-13") {
		grailsChange {
			change {
				sql.execute("DELETE FROM identifier_occurrence where io_canonical_id in (select id_id from identifier left join identifier_namespace xx on identifier.id_ns_fk = xx.idns_id where xx.idns_ns in ('originEditUrl', 'originediturl'))")
				sql.execute("DELETE FROM identifier where id_ns_fk = (select idns_id from identifier_namespace where idns_ns in ('originEditUrl', 'originediturl'))")
				sql.execute("DELETE FROM identifier_namespace where idns_ns in ('originEditUrl', 'originediturl')")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1571820746491-14") {
		grailsChange {
			change {
				sql.execute("update org_access_point set class = 'com.k_int.kbplus.OrgAccessPoint' where class is null")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1571820746491-15") {
		grailsChange {
			change {
				sql.execute("update identifier_namespace set idns_is_hidden = false where idns_is_hidden is null")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1571820746491-16") {
		addNotNullConstraint(columnDataType: "bool", columnName: "idns_is_hidden", tableName: "identifier_namespace")
	}

	changeSet(author: "kloberd (generated)", id: "1571820746491-17") {
		addNotNullConstraint(columnDataType: "int8", columnName: "version", tableName: "price_item")
	}

	changeSet(author: "kloberd (generated)", id: "1571820746491-18") {
		addNotNullConstraint(columnDataType: "bool", columnName: "surconf_evaluation_finish", tableName: "survey_config")
	}

	changeSet(author: "kloberd (generated)", id: "1571820746491-19") {
		addNotNullConstraint(columnDataType: "bool", columnName: "surconf_is_subscription_survey_fix", tableName: "survey_config")
	}

	changeSet(author: "kloberd (generated)", id: "1571820746491-20") {
		addNotNullConstraint(columnDataType: "bool", columnName: "surin_is_subscription_survey", tableName: "survey_info")
	}

	changeSet(author: "kloberd (generated)", id: "1571820746491-21") {
		addNotNullConstraint(columnDataType: "bool", columnName: "surre_is_required", tableName: "survey_result")
	}

	changeSet(author: "kloberd (generated)", id: "1571820746491-22") {
		addNotNullConstraint(columnDataType: "varchar(255)", columnName: "class", tableName: "org_access_point")
	}
}
