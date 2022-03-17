databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1546417253293-1") {
		modifyDataType(columnName: "i10n_value_de", newDataType: "text", tableName: "i10n_translation")
	}

	changeSet(author: "kloberd (generated)", id: "1546417253293-2") {
		modifyDataType(columnName: "i10n_value_en", newDataType: "text", tableName: "i10n_translation")
	}

	changeSet(author: "kloberd (generated)", id: "1546417253293-3") {
		modifyDataType(columnName: "i10n_value_fr", newDataType: "text", tableName: "i10n_translation")
	}

	changeSet(author: "kloberd (generated)", id: "1546417253293-4") {
		modifyDataType(columnName: "pd_description", newDataType: "text", tableName: "property_definition")
	}

	changeSet(author: "kloberd (generated)", id: "1546417253293-5") {
		modifyDataType(columnName: "pd_explanation", newDataType: "text", tableName: "property_definition")
	}
}
