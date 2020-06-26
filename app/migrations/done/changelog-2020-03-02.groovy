databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1583133448395-1") {
		addNotNullConstraint(columnDataType: "int8", columnName: "cid_customer_fk", tableName: "customer_identifier")
	}

	changeSet(author: "kloberd (generated)", id: "1583133448395-2") {
		addNotNullConstraint(columnDataType: "bool", columnName: "cid_is_public", tableName: "customer_identifier")
	}

	changeSet(author: "kloberd (generated)", id: "1583133448395-3") {
		addNotNullConstraint(columnDataType: "varchar(255)", columnName: "das_attribute_name", tableName: "dashboard_due_date")
	}

	changeSet(author: "kloberd (generated)", id: "1583133448395-4") {
		addNotNullConstraint(columnDataType: "bool", columnName: "grt_auto_pkg_update", tableName: "global_record_tracker")
	}

	changeSet(author: "kloberd (generated)", id: "1583133448395-5") {
		addNotNullConstraint(columnDataType: "bool", columnName: "grt_auto_tipp_add", tableName: "global_record_tracker")
	}

	changeSet(author: "kloberd (generated)", id: "1583133448395-6") {
		addNotNullConstraint(columnDataType: "bool", columnName: "grt_auto_tipp_del", tableName: "global_record_tracker")
	}

	changeSet(author: "kloberd (generated)", id: "1583133448395-7") {
		addNotNullConstraint(columnDataType: "bool", columnName: "grt_auto_tipp_update", tableName: "global_record_tracker")
	}

	changeSet(author: "kloberd (generated)", id: "1583133448395-8") {
		addNotNullConstraint(columnDataType: "bool", columnName: "or_is_shared", tableName: "org_role")
	}

	changeSet(author: "kloberd (generated)", id: "1583133448395-9") {
		addNotNullConstraint(columnDataType: "int8", columnName: "identifier_type_id", tableName: "stats_triple_cursor")
	}

	changeSet(author: "kloberd (generated)", id: "1583133448395-10") {
		createIndex(indexName: "td_new_idx", schemaName: "public", tableName: "property_definition") {
			column(name: "pd_description")
			column(name: "pd_name")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1583133448395-11") {
		dropColumn(columnName: "ci_include_in_subscr", tableName: "cost_item")
	}

//	changeSet(author: "kloberd (generated)", id: "1583133448395-12") {
//		dropColumn(columnName: "surin_is_mandatory", tableName: "survey_info")
//	}
}
