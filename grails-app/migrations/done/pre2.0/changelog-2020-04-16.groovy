databaseChangeLog = {

	changeSet(author: "agalffy (generated)", id: "1587022243462-1") {
		addNotNullConstraint(columnDataType: "int8", columnName: "ie_subscription_fk", tableName: "issue_entitlement")
	}

	changeSet(author: "agalffy (generated)", id: "1587022243462-2") {
		addNotNullConstraint(columnDataType: "int8", columnName: "ie_tipp_fk", tableName: "issue_entitlement")
	}
}
