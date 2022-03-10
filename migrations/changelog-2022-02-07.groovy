databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1644233076671-1") {
        dropNotNullConstraint(columnDataType: "bigint", columnName: "c4r_title_fk", tableName: "counter4report")
    }

}
