databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1622112089455-1") {
        addColumn(tableName: "identifier") {
            column(name: "id_instance_of_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1622112089455-2") {
        addForeignKeyConstraint(baseColumnNames: "id_instance_of_fk", baseTableName: "identifier", constraintName: "FK7scf66303sr5sli99funtcsej", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id_id", referencedTableName: "identifier")
    }

}
