databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1622545058988-1") {
        addColumn(tableName: "reader_number") {
            column(name: "num_date_group_note", type: "text")
        }
    }
}
