databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1685433290580-1") {
        createTable(tableName: "deleted_object") {
            column(autoIncrement: "true", name: "do_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "deleted_objectPK")
            }

            column(name: "do_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "do_ref_subscription_uid", type: "VARCHAR(255)")

            column(name: "do_old_date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "do_old_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "do_old_object_type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "do_date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "do_ref_title_wekb_id", type: "VARCHAR(255)")

            column(name: "do_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "do_ref_package_wekb_id", type: "VARCHAR(255)")

            column(name: "do_old_database_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "do_old_global_uid", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "galffy (generated)", id: "1685433290580-2") {
        createIndex(indexName: "do_old_database_id", tableName: "deleted_object") {
            column(name: "do_old_database_id")
        }
    }

    changeSet(author: "galffy (generated)", id: "1685433290580-3") {
        createIndex(indexName: "do_old_db_id_obj_idx", tableName: "deleted_object") {
            column(name: "do_old_object_type")

            column(name: "do_old_database_id")
        }
    }

    changeSet(author: "galffy (generated)", id: "1685433290580-4") {
        createIndex(indexName: "do_old_global_idx", tableName: "deleted_object") {
            column(name: "do_old_global_uid")
        }
    }

    changeSet(author: "galffy (generated)", id: "1685433290580-5") {
        createIndex(indexName: "do_old_object_type_idx", tableName: "deleted_object") {
            column(name: "do_old_object_type")
        }
    }

    changeSet(author: "galffy (generated)", id: "1685433290580-6") {
        createIndex(indexName: "do_ref_package_wekb_idx", tableName: "deleted_object") {
            column(name: "do_ref_package_wekb_id")
        }
    }

    changeSet(author: "galffy (generated)", id: "1685433290580-7") {
        createIndex(indexName: "do_ref_subscription_idx", tableName: "deleted_object") {
            column(name: "do_ref_subscription_uid")
        }
    }

    changeSet(author: "galffy (generated)", id: "1685433290580-8") {
        createIndex(indexName: "do_ref_title_wekb_idx", tableName: "deleted_object") {
            column(name: "do_ref_title_wekb_id")
        }
    }
}
