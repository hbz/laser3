databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1548329193723-1") {
		modifyDataType(columnName: "ciec_id", newDataType: "int8", tableName: "cost_item_element_configuration")
	}

	changeSet(author: "kloberd (generated)", id: "1548329193723-2") {
		modifyDataType(columnName: "l_id", newDataType: "int8", tableName: "links")
	}

	changeSet(author: "kloberd (generated)", id: "1548329193723-3") {
		modifyDataType(columnName: "pd_used_for_logic", newDataType: "bool", tableName: "property_definition")
	}

}
