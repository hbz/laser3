databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1574863913007-1") {
		addColumn(schemaName: "public", tableName: "dashboard_due_date") {
			column(name: "das_attribute_name", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (modified)", id: "1574863913007-2") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE dashboard_due_date RENAME das_attribut TO das_attribute_value_de")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1574863913007-3") {
		addColumn(schemaName: "public", tableName: "dashboard_due_date") {
			column(name: "das_attribute_value_en", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (modified)", id: "1574863913007-4") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE dashboard_due_date RENAME version TO das_version")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1574863913007-5") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE dashboard_due_date RENAME last_updated TO das_last_updated")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1574863913007-6") {
		grailsChange {
			change {
				sql.execute("TRUNCATE TABLE dashboard_due_date")
				sql.execute("ALTER SEQUENCE dashboard_due_date_das_id_seq RESTART WITH 1")
				sql.execute("ALTER TABLE dashboard_due_date ALTER COLUMN das_last_updated TYPE TIMESTAMP WITH TIME ZONE")
			}
			rollback {}
		}
	}
}
