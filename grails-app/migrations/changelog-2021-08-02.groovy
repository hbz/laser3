databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1627897549314-1") {
        grailsChange {
            change {
                sql.execute("update property_definition set pd_multiple_occurrence = true where pd_name = 'Cost unit'")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1627897549314-2") {
        renameColumn(tableName: "wf_workflow", oldColumnName: "wfw_child_fk", newColumnName: "wfw_task_fk")
    }

    changeSet(author: "klober (modified)", id: "1627897549314-3") {
        renameColumn(tableName: "wf_workflow_prototype", oldColumnName: "wfwp_child_fk", newColumnName: "wfwp_task_fk")
    }
}