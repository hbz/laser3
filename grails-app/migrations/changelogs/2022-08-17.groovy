package changelogs

import de.laser.RefdataCategory

databaseChangeLog = {

    changeSet(author: "klober (modified)", id: "1660730198939-1") {
        grailsChange {
            change {
                sql.execute('delete from wf_workflow')
                sql.execute('delete from wf_task')
                sql.execute('delete from wf_condition')
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1660730198939-2") {
        grailsChange {
            change {
                sql.execute('delete from wf_workflow_prototype')
                sql.execute('delete from wf_task_prototype')
                sql.execute('delete from wf_condition_prototype')
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1660730198939-3") {
        grailsChange {
            change {
                RefdataCategory wct = RefdataCategory.findByDesc('workflow.condition.type')
                if (wct) {
                    sql.execute('delete from refdata_value where rdv_owner = ' + wct.id)
                }
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1660730198939-4") {
        dropColumn(columnName: "wfc_type", tableName: "wf_condition")
    }

    changeSet(author: "klober (modified)", id: "1660730198939-5") {
        dropColumn(columnName: "wfcp_type", tableName: "wf_condition_prototype")
    }

    changeSet(author: "klober (generated)", id: "1660730198939-6") {
        addColumn(tableName: "wf_condition") {
            column(name: "wfc_checkbox3", type: "boolean") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "klober (generated)", id: "1660730198939-7") {
        addColumn(tableName: "wf_condition") {
            column(name: "wfc_checkbox3_is_trigger", type: "boolean") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "klober (generated)", id: "1660730198939-8") {
        addColumn(tableName: "wf_condition") {
            column(name: "wfc_checkbox3_title", type: "varchar(255)")
        }
    }

    changeSet(author: "klober (generated)", id: "1660730198939-9") {
        addColumn(tableName: "wf_condition") {
            column(name: "wfc_checkbox4", type: "boolean") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "klober (generated)", id: "1660730198939-10") {
        addColumn(tableName: "wf_condition") {
            column(name: "wfc_checkbox4_is_trigger", type: "boolean") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "klober (generated)", id: "1660730198939-11") {
        addColumn(tableName: "wf_condition") {
            column(name: "wfc_checkbox4_title", type: "varchar(255)")
        }
    }

    changeSet(author: "klober (generated)", id: "1660730198939-12") {
        addColumn(tableName: "wf_condition") {
            column(name: "wfc_date3", type: "timestamp")
        }
    }

    changeSet(author: "klober (generated)", id: "1660730198939-13") {
        addColumn(tableName: "wf_condition") {
            column(name: "wfc_date3_title", type: "varchar(255)")
        }
    }

    changeSet(author: "klober (generated)", id: "1660730198939-14") {
        addColumn(tableName: "wf_condition") {
            column(name: "wfc_date4", type: "timestamp")
        }
    }

    changeSet(author: "klober (generated)", id: "1660730198939-15") {
        addColumn(tableName: "wf_condition") {
            column(name: "wfc_date4_title", type: "varchar(255)")
        }
    }

    changeSet(author: "klober (generated)", id: "1660730198939-16") {
        addColumn(tableName: "wf_condition_prototype") {
            column(name: "wfcp_checkbox3", type: "boolean") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "klober (generated)", id: "1660730198939-17") {
        addColumn(tableName: "wf_condition_prototype") {
            column(name: "wfcp_checkbox3_is_trigger", type: "boolean") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "klober (generated)", id: "1660730198939-18") {
        addColumn(tableName: "wf_condition_prototype") {
            column(name: "wfcp_checkbox3_title", type: "varchar(255)")
        }
    }

    changeSet(author: "klober (generated)", id: "1660730198939-19") {
        addColumn(tableName: "wf_condition_prototype") {
            column(name: "wfcp_checkbox4", type: "boolean") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "klober (generated)", id: "1660730198939-20") {
        addColumn(tableName: "wf_condition_prototype") {
            column(name: "wfcp_checkbox4_is_trigger", type: "boolean") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "klober (generated)", id: "1660730198939-21") {
        addColumn(tableName: "wf_condition_prototype") {
            column(name: "wfcp_checkbox4_title", type: "varchar(255)")
        }
    }

    changeSet(author: "klober (generated)", id: "1660730198939-22") {
        addColumn(tableName: "wf_condition_prototype") {
            column(name: "wfcp_date3", type: "timestamp")
        }
    }

    changeSet(author: "klober (generated)", id: "1660730198939-23") {
        addColumn(tableName: "wf_condition_prototype") {
            column(name: "wfcp_date3_title", type: "varchar(255)")
        }
    }

    changeSet(author: "klober (generated)", id: "1660730198939-24") {
        addColumn(tableName: "wf_condition_prototype") {
            column(name: "wfcp_date4", type: "timestamp")
        }
    }

    changeSet(author: "klober (generated)", id: "1660730198939-25") {
        addColumn(tableName: "wf_condition_prototype") {
            column(name: "wfcp_date4_title", type: "varchar(255)")
        }
    }

    changeSet(author: "klober (modified)", id: "1660730198939-26") {
        addColumn(tableName: "wf_condition") {
            column(name: "wfc_type", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "klober (modified)", id: "1660730198939-27") {
        addColumn(tableName: "wf_condition_prototype") {
            column(name: "wfcp_type", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "klober (generated)", id: "1660730198939-28") {
        addColumn(tableName: "wf_condition") {
            column(name: "wfc_file2", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1660730198939-29") {
        addColumn(tableName: "wf_condition") {
            column(name: "wfc_file2_title", type: "varchar(255)")
        }
    }

    changeSet(author: "klober (generated)", id: "1660730198939-30") {
        addColumn(tableName: "wf_condition_prototype") {
            column(name: "wfcp_file2", type: "int8")
        }
    }

    changeSet(author: "klober (generated)", id: "1660730198939-31") {
        addColumn(tableName: "wf_condition_prototype") {
            column(name: "wfcp_file2_title", type: "varchar(255)")
        }
    }

    changeSet(author: "klober (generated)", id: "1660730198939-32") {
        addForeignKeyConstraint(baseColumnNames: "wfc_file2", baseTableName: "wf_condition", constraintName: "FKid9rpybbo1rflva40ram4wqeb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "dc_id", referencedTableName: "doc_context", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1660730198939-33") {
        addForeignKeyConstraint(baseColumnNames: "wfcp_file2", baseTableName: "wf_condition_prototype", constraintName: "FKnb4lb6iehcsxmu6axkvxv0sh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "dc_id", referencedTableName: "doc_context", validate: "true")
    }
}
