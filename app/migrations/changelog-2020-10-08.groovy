databaseChangeLog = {

	changeSet(author: "djebeniani (generated)", id: "1602168910023-1") {
		addColumn(schemaName: "public", tableName: "org") {
			column(name: "org_e_invoice", type: "bool") {
				constraints(nullable: "true")
			}
		}
	}

	changeSet(author: "djebeniani (generated)", id: "1602168910023-2") {
		addColumn(schemaName: "public", tableName: "org") {
			column(name: "org_e_invoice_portal_fk", type: "int8")
		}
	}

	changeSet(author: "djebeniani (generated)", id: "1602168910023-3") {
		addForeignKeyConstraint(baseColumnNames: "org_e_invoice_portal_fk", baseTableName: "org", baseTableSchemaName: "public", constraintName: "FK1AEE46FE5DD55", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "djebeniani (modifed)", id: "1602168910023-4") {
		grailsChange {
			change {
				sql.execute("update org set org_e_invoice = false where org_e_invoice is null")
			}
			rollback {}
		}
	}
}
