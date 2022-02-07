databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1644233076671-1") {
        dropNotNullConstraint(columnDataType: "clob", columnName: "c4r_publisher", tableName: "counter4report")
    }

    changeSet(author: "galffy (generated)", id: "1644233076671-2") {
        dropNotNullConstraint(columnDataType: "bigint", columnName: "c4r_title_fk", tableName: "counter4report")
    }

}
