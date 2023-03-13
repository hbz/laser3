package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1676626201994-1") {
        addColumn(tableName: "counter4report") {
            column(name: "c4r_database_name", type: "text")
        }
    }

    changeSet(author: "galffy (generated)", id: "1676626201994-2") {
        addColumn(tableName: "counter5report") {
            column(name: "c5r_database_name", type: "text")
        }
    }
}
