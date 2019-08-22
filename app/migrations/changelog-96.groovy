databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1566463428751-1") {
		addColumn(schemaName: "public", tableName: "survey_config") {
			column(name: "surconf_evaluation_finish", type: "bool")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1566463428751-2") {
		addColumn(schemaName: "public", tableName: "survey_result") {
			column(name: "surre_participant_comment", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1566463428751-3") {
		dropIndex(indexName: "rdv_entry_idx", tableName: "refdata_value")
	}
}
