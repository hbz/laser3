databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1566992531378-1") {
		addColumn(schemaName: "public", tableName: "survey_config") {
			column(name: "surconf_is_subscription_survey_fix", type: "bool")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1566992531378-2") {
		addColumn(schemaName: "public", tableName: "survey_info") {
			column(name: "surin_is_subscription_survey", type: "bool")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1566992531378-3") {
		addColumn(schemaName: "public", tableName: "survey_result") {
			column(name: "surre_owner_comment", type: "varchar(255)")
		}
	}
}
