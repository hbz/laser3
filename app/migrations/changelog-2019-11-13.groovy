databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1573628349804-1") {
		createTable(schemaName: "public", tableName: "mail_template") {
			column(autoIncrement: "true", name: "mt_id", type: "int8") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "mail_templatePK")
			}

			column(name: "mt_version", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "mt_date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "mt_language_rv_fk", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "mt_last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "mt_name", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "mt_owner_org_fk", type: "int8")

			column(name: "mt_sent_by_system", type: "bool") {
				constraints(nullable: "false")
			}

			column(name: "mt_subject", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "mt_text", type: "text")

			column(name: "mt_type_rv_fk", type: "int8") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1573628349804-2") {
		addForeignKeyConstraint(baseColumnNames: "mt_language_rv_fk", baseTableName: "mail_template", baseTableSchemaName: "public", constraintName: "FK4F827602E490B442", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1573628349804-3") {
		addForeignKeyConstraint(baseColumnNames: "mt_owner_org_fk", baseTableName: "mail_template", baseTableSchemaName: "public", constraintName: "FK4F82760225FA17EB", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1573628349804-4") {
		addForeignKeyConstraint(baseColumnNames: "mt_type_rv_fk", baseTableName: "mail_template", baseTableSchemaName: "public", constraintName: "FK4F82760225B4AA4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}
}
