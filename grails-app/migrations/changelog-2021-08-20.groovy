databaseChangeLog = {

    changeSet(author: "galffy (modified)", id: "1629440898005-1") {
        addColumn(tableName: "issue_entitlement") {
            column(name: "ie_name", type: "text")
        }
    }

    changeSet(author: "galffy (modified)", id: "1629440898005-2") {
        addColumn(tableName: "issue_entitlement") {
            column(name: "ie_sortname", type: "text")
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1629440898005-3") {
        grailsChange {
            change {
                sql.execute('update issue_entitlement set ie_name = tipp_name from title_instance_package_platform where ie_tipp_fk = tipp_id and tipp_name is not null')
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1629440898005-4") {
        grailsChange {
            change {
                sql.execute('update issue_entitlement set ie_sortname = tipp_sort_name from title_instance_package_platform where ie_tipp_fk = tipp_id and tipp_sort_name is not null')
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1629440898005-5") {
        grailsChange {
            change {
                sql.execute("delete from refdata_value where rdv_value = 'type_0' and rdv_owner = (select rdc_id from refdata_category where rdc_description = 'workflow.condition.type')")
            }
            rollback {}
        }
    }
    changeSet(author: "klober (modified)", id: "1629440898005-6") {
        grailsChange {
            change {
                sql.execute('update wf_condition set wfc_type = 4 where wfc_type = 0 and wfc_file1 is null')
                sql.execute('update wf_condition set wfc_type = 7 where wfc_type = 0 and wfc_file1 is not null')
                sql.execute('update wf_condition_prototype set wfcp_type = 4 where wfcp_type = 0 and wfcp_file1 is null')
                sql.execute('update wf_condition_prototype set wfcp_type = 7 where wfcp_type = 0 and wfcp_file1 is not null')
            }
            rollback {}
        }
    }
}
