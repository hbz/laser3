package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1685955764050-1") {
        createTable(tableName: "deleted_identifier") {
            column(autoIncrement: "true", name: "di_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "deleted_identifierPK")
            }

            column(name: "di_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "di_old_date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "di_old_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "di_date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "di_old_reference_object_type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "di_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "di_old_namespace", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "di_old_value", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "di_old_global_uid", type: "VARCHAR(255)")

            column(name: "di_old_database_id_pointer", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "di_old_database_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1685955764050-2") {
        createIndex(indexName: "di_old_database_id", tableName: "deleted_identifier") {
            column(name: "di_old_database_id")
        }
    }

    changeSet(author: "galffy (generated)", id: "1685955764050-3") {
        createIndex(indexName: "di_old_database_id_pointer", tableName: "deleted_identifier") {
            column(name: "di_old_database_id_pointer")
        }
    }

    changeSet(author: "galffy (generated)", id: "1685955764050-4") {
        createIndex(indexName: "di_old_global_idx", tableName: "deleted_identifier") {
            column(name: "di_old_global_uid")
        }
    }

    changeSet(author: "galffy (generated)", id: "1685955764050-5") {
        createIndex(indexName: "di_old_namespace_idx", tableName: "deleted_identifier") {
            column(name: "di_old_namespace")
        }
    }

    changeSet(author: "galffy (generated)", id: "1685955764050-6") {
        createIndex(indexName: "di_old_reference_object_type_idx", tableName: "deleted_identifier") {
            column(name: "di_old_reference_object_type")
        }
    }

    changeSet(author: "galffy (generated)", id: "1685955764050-7") {
        createIndex(indexName: "di_old_value_idx", tableName: "deleted_identifier") {
            column(name: "di_old_value")
        }
    }

    changeSet(author: "galffy (generated)", id: "1685955764050-8") {
        createIndex(indexName: "di_old_value_ns_idx", tableName: "deleted_identifier") {
            column(name: "di_old_namespace")

            column(name: "di_old_value")
        }
    }
}
