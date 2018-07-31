databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1533039768856-1") {
		createTable(tableName: "system_ticket") {
			column(autoIncrement: "true", name: "sti_id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "system_ticketPK")
			}

			column(name: "sti_version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "sti_user_fk", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "sti_category_rv_fk", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "sti_created", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "sti_described", type: "longtext") {
				constraints(nullable: "false")
			}

			column(name: "sti_expected", type: "longtext") {
				constraints(nullable: "false")
			}

			column(name: "sti_info", type: "longtext") {
				constraints(nullable: "false")
			}

			column(name: "sti_jira", type: "varchar(255)")

			column(name: "sti_modified", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "sti_meta", type: "longtext") {
				constraints(nullable: "false")
			}

			column(name: "sti_status_rv_fk", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "sti_title", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1533039768856-5") {
		createIndex(indexName: "FK830EF31C2171A6C7", tableName: "system_ticket") {
			column(name: "sti_category_rv_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1533039768856-6") {
		createIndex(indexName: "FK830EF31C28068B56", tableName: "system_ticket") {
			column(name: "sti_user_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1533039768856-7") {
		createIndex(indexName: "FK830EF31CBB60047B", tableName: "system_ticket") {
			column(name: "sti_status_rv_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1533039768856-2") {
		addForeignKeyConstraint(baseColumnNames: "sti_category_rv_fk", baseTableName: "system_ticket", constraintName: "FK830EF31C2171A6C7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1533039768856-3") {
		addForeignKeyConstraint(baseColumnNames: "sti_status_rv_fk", baseTableName: "system_ticket", constraintName: "FK830EF31CBB60047B", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1533039768856-4") {
		addForeignKeyConstraint(baseColumnNames: "sti_user_fk", baseTableName: "system_ticket", constraintName: "FK830EF31C28068B56", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencesUniqueColumn: "false")
	}
}
