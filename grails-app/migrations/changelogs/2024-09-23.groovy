package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1727102531671-1") {
        createIndex(indexName: "id_value_idx", tableName: "identifier") {
            column(name: "id_value")
        }
    }

    changeSet(author: "galffy (generated)", id: "1727102531671-2") {
        createIndex(indexName: "tipp_first_author_idx", tableName: "title_instance_package_platform") {
            column(name: "tipp_first_author")
        }
    }

    changeSet(author: "galffy (generated)", id: "1727102531671-3") {
        createIndex(indexName: "tipp_first_editor_idx", tableName: "title_instance_package_platform") {
            column(name: "tipp_first_editor")
        }
    }

    changeSet(author: "galffy (generated)", id: "1727102531671-4") {
        createIndex(indexName: "tipp_name_idx", tableName: "title_instance_package_platform") {
            column(name: "tipp_name")
        }
    }

    changeSet(author: "galffy (generated)", id: "1727102531671-5") {
        createIndex(indexName: "tipp_sort_name_idx", tableName: "title_instance_package_platform") {
            column(name: "tipp_sort_name")
        }
    }

    changeSet(author: "klober (generated)", id: "1727092155564-6") {
        dropColumn(columnName: "dc_domain", tableName: "doc_context")
    }

    changeSet(author: "klober (generated)", id: "1727092155564-7") {
        addColumn(tableName: "task") {
            column(name: "tsk_tipp_fk", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1727092155564-8") {
        addForeignKeyConstraint(baseColumnNames: "tsk_tipp_fk", baseTableName: "task", constraintName: "FKbncag6buf8esq1khwm4du5xws", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "tipp_id", referencedTableName: "title_instance_package_platform", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1727092155564-9") {
        createIndex(indexName: "tsk_tipp_idx", tableName: "task") {
            column(name: "tsk_tipp_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1727092155564-10") {
        dropIndex(indexName: "das_responsible_user_fk_idx", tableName: "dashboard_due_date")
    }

    changeSet(author: "klober (generated)", id: "1727092155564-11") {
        createIndex(indexName: "das_responsible_user_idx", tableName: "dashboard_due_date") {
            column(name: "das_responsible_user_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1727092155564-12") {
        dropIndex(indexName: "das_responsible_org_fk_idx", tableName: "dashboard_due_date")
    }

    changeSet(author: "klober (generated)", id: "1727092155564-13") {
        createIndex(indexName: "das_responsible_org_idx", tableName: "dashboard_due_date") {
            column(name: "das_responsible_org_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1727092155564-14") {
        dropColumn(columnName: "dc_is_global", tableName: "doc_context")
    }

    changeSet(author: "klober (generated)", id: "1727092155564-15") {
        dropIndex(indexName: "pp_tenant_fk", tableName: "person_property")
    }

    changeSet(author: "klober (generated)", id: "1727092155564-16") {
        createIndex(indexName: "pp_tenant_idx", tableName: "person_property") {
            column(name: "pp_tenant_fk")
        }
    }

    changeSet(author: "klober (modified)", id: "1727092155564-17") {
        grailsChange {
            change {
                sql.execute("update marker set mkr_type_enum = 'UNKOWN' where mkr_type_enum = 'TIPP_CHANGES'")
                String info = "update marker set mkr_type_enum = 'UNKOWN': ${sql.getUpdateCount()}"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

    changeSet(author: "klober (generated)", id: "1727092155564-18") {
        dropColumn(columnName: "doc_migrated", tableName: "doc")
    }
}