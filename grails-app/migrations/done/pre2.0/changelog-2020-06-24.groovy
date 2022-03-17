databaseChangeLog = {

	changeSet(author: "Moe (generated)", id: "1592994520543-1") {
		addColumn(schemaName: "public", tableName: "ftcontrol") {
			column(name: "active", type: "bool") {
				constraints(nullable: "true")
			}
		}
	}
}
