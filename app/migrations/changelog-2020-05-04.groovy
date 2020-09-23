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

	changeSet(author: "klober (generated)", id: "1588575540803-5") {
		createTable(schemaName: "public", tableName: "due_date_object") {
			column(autoIncrement: "true", name: "ddo_id", type: "int8") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "due_date_objePK")
			}

			column(name: "ddo_version", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "ddo_attribute_name", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "ddo_attribute_value_de", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "ddo_attribute_value_en", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "ddo_date", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "ddo_date_created", type: "timestamp")

			column(name: "ddo_is_done", type: "bool") {
				constraints(nullable: "false")
			}

			column(name: "ddo_last_updated", type: "timestamp")

			column(name: "ddo_oid", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "klober (generated)", id: "1588575540803-6") {
		addColumn(schemaName: "public", tableName: "dashboard_due_date") {
			column(name: "das_ddobj_fk", type: "int8")
		}
	}

	changeSet(author: "klober (generated)", id: "1588575540803-7") {
		addForeignKeyConstraint(baseColumnNames: "das_ddobj_fk", baseTableName: "dashboard_due_date", baseTableSchemaName: "public", constraintName: "FK2DE8A864927DC6C7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ddo_id", referencedTableName: "due_date_object", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "klober (modified)", id: "1588575540803-8") {
		grailsChange {
			change {
				sql.execute("alter table due_date_object alter column ddo_date type timestamp using ddo_date::timestamp")
				sql.execute("alter table due_date_object alter column ddo_date_created type timestamp using ddo_date_created::timestamp")
				sql.execute("alter table due_date_object alter column ddo_last_updated type timestamp using ddo_last_updated::timestamp")
			}
		}
	}

	changeSet(author: "klober (modified)", id: "1588575540803-9") {
		grailsChange {
			change {
				sql.execute("""
	insert into due_date_object (
			ddo_version,
			ddo_attribute_name,
			ddo_attribute_value_de,
			ddo_attribute_value_en,
			ddo_date,
			ddo_oid,
			ddo_date_created,
			ddo_last_updated,
			ddo_is_done)
	select distinct
	das_version,
	das_attribute_name,
	das_attribute_value_de,
	das_attribute_value_en,
	das_date,
	das_oid,
	das_date_created,
	das_last_updated,
	das_is_done
	from dashboard_due_date
""")
			}
		}
	}

	changeSet(author: "klober (modified)", id: "1588575540803-10") {
		grailsChange {
			change {
				sql.execute("""
	update dashboard_due_date ddd set das_ddobj_fk = ddo_id
	from dashboard_due_date, due_date_object
	where
	due_date_object.ddo_oid = ddd.das_oid and
	due_date_object.ddo_attribute_name = ddd.das_attribute_name
""")
			}
		}
	}

	changeSet(author: "klober (generated)", id: "1588575540803-11") {
		dropColumn(columnName: "das_attribute_name", tableName: "dashboard_due_date")
	}

	changeSet(author: "klober (generated)", id: "1588575540803-12") {
		dropColumn(columnName: "das_attribute_value_de", tableName: "dashboard_due_date")
	}

	changeSet(author: "klober (generated)", id: "1588575540803-13") {
		dropColumn(columnName: "das_attribute_value_en", tableName: "dashboard_due_date")
	}

	changeSet(author: "klober (generated)", id: "1588575540803-14") {
		dropColumn(columnName: "das_date", tableName: "dashboard_due_date")
	}

	changeSet(author: "klober (generated)", id: "1588575540803-15") {
		dropColumn(columnName: "das_is_done", tableName: "dashboard_due_date")
	}

	changeSet(author: "klober (generated)", id: "1588575540803-16") {
		dropColumn(columnName: "das_oid", tableName: "dashboard_due_date")
	}
}

