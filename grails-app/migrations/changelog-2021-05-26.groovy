databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1622018225727-1") {
        addColumn(tableName: "contact") {
            column(name: "ct_language_rv_fk", type: "int8")
        }
    }

}