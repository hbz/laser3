databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1583394967979-1") {
		createTable(schemaName: "public", tableName: "system_announcement") {
			column(autoIncrement: "true", name: "sa_id", type: "int8") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "system_announPK")
			}

			column(name: "sa_version", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "sa_content", type: "text") {
				constraints(nullable: "false")
			}

			column(name: "sa_date_created", type: "timestamp")

			column(name: "sa_is_published", type: "bool") {
				constraints(nullable: "false")
			}

			column(name: "sa_last_publishing_date", type: "timestamp")

			column(name: "sa_last_updated", type: "timestamp")

			column(name: "sa_title", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "sa_user_fk", type: "int8") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1583394967979-2") {
		addColumn(schemaName: "public", tableName: "survey_info") {
			column(name: "surin_is_mandatory", type: "bool")
		}
	}

	changeSet(author: "kloberd (modified)", id: "1583394967979-3") {
		grailsChange {
			change {
				sql.execute("update survey_info set surin_is_mandatory = true where surin_is_mandatory is null and surin_is_subscription_survey = true;")
				sql.execute("update survey_info set surin_is_mandatory = false where surin_is_mandatory is null;")
			}
			rollback {
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1583394967979-4") {
		addNotNullConstraint(columnDataType: "bool", columnName: "surin_is_mandatory", tableName: "survey_info")
	}

	changeSet(author: "kloberd (generated)", id: "1583394967979-5") {
		dropForeignKeyConstraint(baseTableName: "survey_result", baseTableSchemaName: "public", constraintName: "fk92ea04a2289bab1")
	}

	changeSet(author: "kloberd (generated)", id: "1583394967979-6") {
		dropColumn(columnName: "surre_user_fk", tableName: "survey_result")
	}

	changeSet(author: "kloberd (generated)", id: "1583394967979-7") {
		addForeignKeyConstraint(baseColumnNames: "sa_user_fk", baseTableName: "system_announcement", baseTableSchemaName: "public", constraintName: "FKE9DC8957AEDD557C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1583394967979-8") {
		createTable(schemaName: "public", tableName: "org_subject_group") {
			column(autoIncrement: "true", name: "osg_id", type: "int8") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "org_subject_gPK")
			}

			column(name: "version", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "osg_date_created", type: "timestamp")

			column(name: "osg_last_updated", type: "timestamp")

			column(name: "osg_org", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "osg_subject_group", type: "int8") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1583394967979-9") {
		addColumn(schemaName: "public", tableName: "org") {
			column(name: "org_funder_hsk_type_rv_fk", type: "int8")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1583394967979-10") {
		addColumn(schemaName: "public", tableName: "org") {
			column(name: "org_legal_patronname", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1583394967979-11") {
		addColumn(schemaName: "public", tableName: "org") {
			column(name: "org_region_rv_fk", type: "int8")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1583394967979-12") {
		addForeignKeyConstraint(baseColumnNames: "org_funder_hsk_type_rv_fk", baseTableName: "org", baseTableSchemaName: "public", constraintName: "FK1AEE4E01687D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1583394967979-13") {
		addForeignKeyConstraint(baseColumnNames: "org_region_rv_fk", baseTableName: "org", baseTableSchemaName: "public", constraintName: "FK1AEE4DF8FB2C1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1583394967979-14") {
		addForeignKeyConstraint(baseColumnNames: "osg_org", baseTableName: "org_subject_group", baseTableSchemaName: "public", constraintName: "FK7F97BF9117DD94EF", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1583394967979-15") {
		addForeignKeyConstraint(baseColumnNames: "osg_subject_group", baseTableName: "org_subject_group", baseTableSchemaName: "public", constraintName: "FK7F97BF9158CB3321", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1583394967979-16") {
		addColumn(schemaName: "public", tableName: "system_announcement") {
			column(name: "sa_status", type: "text")
		}
	}
}
