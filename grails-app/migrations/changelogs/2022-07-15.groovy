package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1657862108623-1") {
        addNotNullConstraint(columnDataType: "boolean", columnName: "surlin_both_direction", tableName: "survey_links", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1657862108623-2") {
        addNotNullConstraint(columnDataType: "boolean", columnName: "org_inserted_itself", tableName: "survey_org", validate: "true")
    }
}
