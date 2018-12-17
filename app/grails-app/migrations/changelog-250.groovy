databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1544690147263-1") {
		createTable(tableName: "api_source") {
			column(autoIncrement: "true", name: "as_id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "api_sourcePK")
			}

			column(name: "as_version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "as_active", type: "bit")

			column(name: "as_apikey", type: "varchar(255)")

			column(name: "as_apisecret", type: "varchar(255)")

			column(name: "as_baseUrl", type: "varchar(255)")

			column(name: "as_creds", type: "varchar(255)")

			column(name: "as_dateCreated", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "as_fixToken", type: "varchar(255)")

			column(name: "as_identifier", type: "varchar(255)")

			column(name: "as_lastUpdated", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "as_lastUpdated_with_Api", type: "datetime")

			column(name: "as_name", type: "longtext")

			column(name: "as_principal", type: "varchar(255)")

			column(name: "as_typ", type: "varchar(255)")

			column(name: "as_variableToken", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1544690147263-2") {
		createTable(tableName: "dashboard_due_date") {
			column(autoIncrement: "true", name: "das_id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "dashboard_duePK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "das_attribut", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "das_date", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "datetime")

			column(name: "das_oid", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "das_responsible_org_fk", type: "bigint")

			column(name: "das_responsible_user_fk", type: "bigint")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1544690147263-3") {
		addColumn(tableName: "license_custom_property") {
			column(name: "url_value", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1544690147263-4") {
		addColumn(tableName: "license_private_property") {
			column(name: "url_value", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1544690147263-5") {
		addColumn(tableName: "org_custom_property") {
			column(name: "url_value", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1544690147263-6") {
		addColumn(tableName: "org_private_property") {
			column(name: "url_value", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1544690147263-7") {
		addColumn(tableName: "person_private_property") {
			column(name: "url_value", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1544690147263-8") {
		addColumn(tableName: "property_definition") {
			column(name: "pd_explanation", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1544690147263-9") {
		addColumn(tableName: "subscription_custom_property") {
			column(name: "url_value", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1544690147263-10") {
		addColumn(tableName: "subscription_private_property") {
			column(name: "url_value", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1544690147263-11") {
		addColumn(tableName: "system_admin_custom_property") {
			column(name: "url_value", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1544690147263-15") {
		dropIndex(indexName: "td_new_idx", tableName: "property_definition")
	}

	changeSet(author: "kloberd (generated)", id: "1544690147263-16") {
		createIndex(indexName: "FK2DE8A86442FFCFFB", tableName: "dashboard_due_date") {
			column(name: "das_responsible_user_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1544690147263-17") {
		createIndex(indexName: "FK2DE8A86474C97DF9", tableName: "dashboard_due_date") {
			column(name: "das_responsible_org_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1544690147263-18") {
		createIndex(indexName: "das_responsible_org_fk_idx", tableName: "dashboard_due_date") {
			column(name: "das_responsible_org_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1544690147263-19") {
		createIndex(indexName: "das_responsible_user_fk_idx", tableName: "dashboard_due_date") {
			column(name: "das_responsible_user_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1544690147263-20") {
		createIndex(indexName: "td_new_idx", tableName: "property_definition") {
			column(name: "pd_description")
			column(name: "pd_explanation")
			column(name: "pd_name")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1544690147263-12") {
		addForeignKeyConstraint(baseColumnNames: "das_responsible_org_fk", baseTableName: "dashboard_due_date", constraintName: "FK2DE8A86474C97DF9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1544690147263-13") {
		addForeignKeyConstraint(baseColumnNames: "das_responsible_user_fk", baseTableName: "dashboard_due_date", constraintName: "FK2DE8A86442FFCFFB", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
	}

	//changeSet(author: "kloberd (generated)", id: "1544690147263-14") {
	//	addForeignKeyConstraint(baseColumnNames: "grt_owner_fk", baseTableName: "global_record_tracker", constraintName: "FK808F5966F6287F86", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "gri_id", referencedTableName: "global_record_info", referencesUniqueColumn: "false")
	//}
}
