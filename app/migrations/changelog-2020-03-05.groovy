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

	changeSet(author: "kloberd (generated)", id: "1583394967979-3") {
		grailsChange {
			change {
				sql.execute("update survey_info set surin_is_mandatory = true where surin_is_mandatory is null and surin_is_subscription_survey = true;")
				sql.execute("update survey_info set surin_is_mandatory = false where surin_is_mandatory is null;")
			}
			rollback {
			}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1583394967979-4") {
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

}
