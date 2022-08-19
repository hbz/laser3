package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1660898634754-1") {
        addColumn(tableName: "wf_workflow") {
            column(name: "wfw_prototype_version", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "klober (generated)", id: "1660898634754-2") {
        addColumn(tableName: "wf_workflow_prototype") {
            column(name: "wfwp_prototype_version", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "klober (generated)", id: "1660898634754-3") {
        addColumn(tableName: "wf_workflow") {
            column(name: "wfw_prototype_last_updated", type: "timestamp") {
                constraints(nullable: "false")
            }
        }
    }
}
