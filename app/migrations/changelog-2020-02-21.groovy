databaseChangeLog = {

	changeSet(author: "djebeniani (generated)", id: "1582287348866-1") {
		addColumn(schemaName: "public", tableName: "survey_info") {
			column(name: "surin_is_mandatory", type: "bool") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "djebeniani (generated)", id: "1582287348866-2") {
		grailsChange {
			change {
				sql.execute("update survey_info set surin_is_mandatory = true where surin_is_mandatory is null and surin_is_subscription_survey = true;")
			}
			rollback {
			}
		}
	}

	changeSet(author: "djebeniani (generated)", id: "1582287348866-3") {
		dropForeignKeyConstraint(baseTableName: "survey_result", baseTableSchemaName: "public", constraintName: "fk92ea04a2289bab1")
	}


}
