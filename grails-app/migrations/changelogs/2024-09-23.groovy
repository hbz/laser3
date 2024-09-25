package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (modified)", id: "1727102531671-1") {
        grailsChange {
            change {
                sql.execute('CREATE INDEX tipp_first_author_idx ON public.title_instance_package_platform (lower(tipp_first_author));')
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1727102531671-2") {
        grailsChange {
            change {
                sql.execute('CREATE INDEX tipp_first_editor_idx ON public.title_instance_package_platform (lower(tipp_first_editor));')
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1727102531671-3") {
        grailsChange {
            change {
                sql.execute('CREATE INDEX tipp_name_idx ON public.title_instance_package_platform (lower(tipp_name));')
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1727102531671-4") {
        grailsChange {
            change {
                sql.execute('CREATE INDEX tipp_sort_name_idx ON public.title_instance_package_platform (lower(tipp_sort_name));')
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1727102531671-5") {
        grailsChange {
            change {
                sql.execute('CREATE INDEX id_value_idx ON public.identifier (lower(id_value));')
            }
            rollback {}
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
}