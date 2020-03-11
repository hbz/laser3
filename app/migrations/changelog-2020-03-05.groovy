databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1583394967979-1") {
		createTable(schemaName: "public", tableName: "system_announcement") {
			column(autoIncrement: "true", name: "sa_id", type: "int8") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "system_announPK")
			}

			column(name: "sa_version", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "sa_content", type: "text") {
				constraints(nullable: "false")
			}

			column(name: "sa_date_created", type: "timestamp")

			column(name: "sa_is_published", type: "bool") {
				constraints(nullable: "false")
			}

			column(name: "sa_last_publishing_date", type: "timestamp")

			column(name: "sa_last_updated", type: "timestamp")

			column(name: "sa_title", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "sa_user_fk", type: "int8") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1583394967979-7") {
		addForeignKeyConstraint(baseColumnNames: "sa_user_fk", baseTableName: "system_announcement", baseTableSchemaName: "public", constraintName: "FKE9DC8957AEDD557C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1583394967979-16") {
		addColumn(schemaName: "public", tableName: "system_announcement") {
			column(name: "sa_status", type: "text")
		}
	}
}
