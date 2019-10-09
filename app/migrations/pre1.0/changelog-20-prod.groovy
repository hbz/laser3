databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1549436371014-1") {
		modifyDataType(columnName: "ciec_id", newDataType: "int8", tableName: "cost_item_element_configuration")
	}

	changeSet(author: "kloberd (generated)", id: "1549436371014-2") {
		modifyDataType(columnName: "l_id", newDataType: "int8", tableName: "links")
	}

	changeSet(author: "kloberd (generated)", id: "1549436371014-3") {
		modifyDataType(columnName: "pd_used_for_logic", newDataType: "bool", tableName: "property_definition")
	}

	changeSet(author: "kloberd (generated)", id: "1549436371014-4") {
		addNotNullConstraint(columnDataType: "bool", columnName: "pd_used_for_logic", tableName: "property_definition")
	}

	changeSet(author: "kloberd (generated)", id: "1549436371014-5") {
		modifyDataType(columnName: "se_id", newDataType: "int8", tableName: "system_event")
	}
}
