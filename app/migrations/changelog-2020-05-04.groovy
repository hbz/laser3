databaseChangeLog = {

	changeSet(author: "klober (generated)", id: "1588575540803-1") {
		addColumn(schemaName: "public", tableName: "identifier") {
			column(name: "id_last_updated_cascading", type: "timestamp")
		}
	}

	changeSet(author: "klober (generated)", id: "1588575540803-2") {
		addColumn(schemaName: "public", tableName: "identifier_namespace") {
			column(name: "idns_last_updated_cascading", type: "timestamp")
		}
	}

	changeSet(author: "klober (generated)", id: "1588575540803-3") {
		addColumn(schemaName: "public", tableName: "subscription") {
			column(name: "sub_last_updated_cascading", type: "timestamp")
		}
	}

	changeSet(author: "klober (modified)", id: "1588575540803-4") {
		grailsChange {
			change {
				sql.execute("alter table identifier_namespace alter column idns_last_updated_cascading type timestamp using idns_last_updated_cascading::timestamp")
				sql.execute("alter table identifier alter column id_last_updated_cascading type timestamp using id_last_updated_cascading::timestamp")
				sql.execute("alter table subscription alter column sub_last_updated_cascading type timestamp using sub_last_updated_cascading::timestamp")
			}
		}
	}
}

