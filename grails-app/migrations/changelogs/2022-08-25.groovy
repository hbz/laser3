databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1661411937279-1") {
        addColumn(tableName: "platform") {
            column(name: "plat_c4_last_run", type: "timestamp")
        }
    }

    changeSet(author: "galffy (generated)", id: "1661411937279-2") {
        addColumn(tableName: "platform") {
            column(name: "plat_c5_last_run", type: "timestamp")
        }
    }
}
