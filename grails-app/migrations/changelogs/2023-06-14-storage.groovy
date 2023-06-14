databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1686720364944-1") {
        addColumn(tableName: "deleted_object") {
            column(name: "do_old_calculated_type", type: "varchar(255)")
        }
    }

    changeSet(author: "galffy (generated)", id: "1686720364944-2") {
        addColumn(tableName: "deleted_object") {
            column(name: "do_old_end_date", type: "timestamp")
        }
    }

    changeSet(author: "galffy (generated)", id: "1686720364944-3") {
        addColumn(tableName: "deleted_object") {
            column(name: "do_old_name", type: "text")
        }
    }

    changeSet(author: "galffy (generated)", id: "1686720364944-4") {
        addColumn(tableName: "deleted_object") {
            column(name: "do_old_start_date", type: "timestamp")
        }
    }
}
