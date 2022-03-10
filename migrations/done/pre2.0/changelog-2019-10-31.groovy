databaseChangeLog = {

	changeSet(author: "kloberd (modified)", id: "1572516031732-1") {
		grailsChange {
			change {
				sql.execute("update subscription set sub_is_multi_year = true where sub_id in(select sub_id from subscription where DATE_PART('day', sub_end_date - sub_start_date) >= 724 and sub_end_date is not null)")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1572516031732-2") {
		addColumn(schemaName: "public", tableName: "identifier") {
			column(name: "id_lic_fk", type: "int8")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1572516031732-3") {
		addColumn(schemaName: "public", tableName: "identifier") {
			column(name: "id_note", type: "text")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1572516031732-4") {
		addColumn(schemaName: "public", tableName: "identifier") {
			column(name: "id_org_fk", type: "int8")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1572516031732-5") {
		addColumn(schemaName: "public", tableName: "identifier") {
			column(name: "id_pkg_fk", type: "int8")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1572516031732-6") {
		addColumn(schemaName: "public", tableName: "identifier") {
			column(name: "id_sub_fk", type: "int8")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1572516031732-7") {
		addColumn(schemaName: "public", tableName: "identifier") {
			column(name: "id_ti_fk", type: "int8")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1572516031732-8") {
		addColumn(schemaName: "public", tableName: "identifier") {
			column(name: "id_tipp_fk", type: "int8")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1572516031732-9") {
		addColumn(schemaName: "public", tableName: "identifier") {
			column(name: "id_cre_fk", type: "int8")
		}
	}
	changeSet(author: "kloberd (generated)", id: "1572516031732-10") {
		createIndex(indexName: "id_tipp_idx", schemaName: "public", tableName: "identifier") {
			column(name: "id_tipp_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1572516031732-11") {
		createIndex(indexName: "id_title_idx", schemaName: "public", tableName: "identifier") {
			column(name: "id_ti_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1572516031732-12") {
		dropForeignKeyConstraint(baseTableName: "creator", baseTableSchemaName: "public", constraintName: "fk3d4e802c8b454035")
	}

	changeSet(author: "kloberd (generated)", id: "1572516031732-13") {
		dropColumn(columnName: "cre_gnd_id_fk", tableName: "creator")
	}

	changeSet(author: "kloberd (generated)", id: "1572516031732-14") {
		addForeignKeyConstraint(baseColumnNames: "id_lic_fk", baseTableName: "identifier", baseTableSchemaName: "public", constraintName: "FK9F88ACA9F175B1E6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "lic_id", referencedTableName: "license", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1572516031732-15") {
		addForeignKeyConstraint(baseColumnNames: "id_org_fk", baseTableName: "identifier", baseTableSchemaName: "public", constraintName: "FK9F88ACA9274BF4EB", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1572516031732-16") {
		addForeignKeyConstraint(baseColumnNames: "id_pkg_fk", baseTableName: "identifier", baseTableSchemaName: "public", constraintName: "FK9F88ACA9BE5BC4E5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pkg_id", referencedTableName: "package", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1572516031732-17") {
		addForeignKeyConstraint(baseColumnNames: "id_sub_fk", baseTableName: "identifier", baseTableSchemaName: "public", constraintName: "FK9F88ACA971E1DCE2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sub_id", referencedTableName: "subscription", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1572516031732-18") {
		addForeignKeyConstraint(baseColumnNames: "id_ti_fk", baseTableName: "identifier", baseTableSchemaName: "public", constraintName: "FK9F88ACA96D99385B", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ti_id", referencedTableName: "title_instance", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1572516031732-19") {
		addForeignKeyConstraint(baseColumnNames: "id_tipp_fk", baseTableName: "identifier", baseTableSchemaName: "public", constraintName: "FK9F88ACA96EEC45F4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "tipp_id", referencedTableName: "title_instance_package_platform", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1572516031732-20") {
		addForeignKeyConstraint(baseColumnNames: "id_cre_fk", baseTableName: "identifier", baseTableSchemaName: "public", constraintName: "FK9F88ACA915F7CC81", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "cre_id", referencedTableName: "creator", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}
}
