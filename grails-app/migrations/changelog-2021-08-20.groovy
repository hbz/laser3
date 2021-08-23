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

}
