databaseChangeLog = {

	changeSet(author: "Moe (generated)", id: "1589541015267-1") {
		addColumn(schemaName: "public", tableName: "survey_config") {
			column(name: "surconf_create_title_groups", type: "bool") {
				//constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "Moe (modified)", id: "1589541015267-2") {
		grailsChange {
			change {
				sql.execute("UPDATE survey_config SET surconf_create_title_groups = false WHERE surconf_create_title_groups is null;")
			}
			rollback {}
		}
	}
}
