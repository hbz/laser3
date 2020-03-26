databaseChangeLog = {

	changeSet(author: "klober (generated)", id: "1585205594968-1") {
		dropForeignKeyConstraint(baseTableName: "cost_item_element_configuration", baseTableSchemaName: "public", constraintName: "fk2d8a6879b47e66a0")
	}

	changeSet(author: "klober (generated)", id: "1585205594968-2") {
		dropForeignKeyConstraint(baseTableName: "cost_item_element_configuration", baseTableSchemaName: "public", constraintName: "fk2d8a68793096044a")
	}

	changeSet(author: "klober (generated)", id: "1585205594968-3") {
		dropForeignKeyConstraint(baseTableName: "links", baseTableSchemaName: "public", constraintName: "fk6234fb9b47e66a0")
	}

	changeSet(author: "klober (generated)", id: "1585205594968-4") {
		dropForeignKeyConstraint(baseTableName: "links", baseTableSchemaName: "public", constraintName: "fk6234fb93096044a")
	}

	changeSet(author: "klober (generated)", id: "1585205594968-5") {
		dropColumn(columnName: "created_by_id", tableName: "cost_item_element_configuration")
	}

	changeSet(author: "klober (generated)", id: "1585205594968-6") {
		dropColumn(columnName: "last_updated_by_id", tableName: "cost_item_element_configuration")
	}

	changeSet(author: "klober (generated)", id: "1585205594968-7") {
		dropColumn(columnName: "created_by_id", tableName: "links")
	}

	changeSet(author: "klober (generated)", id: "1585205594968-8") {
		dropColumn(columnName: "last_updated_by_id", tableName: "links")
	}
}
