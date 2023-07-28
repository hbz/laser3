package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1690522190841-1") {
        addNotNullConstraint(columnDataType: "boolean", columnName: "sub_offer_accepted", tableName: "subscription", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1690522190841-2") {
        addNotNullConstraint(columnDataType: "boolean", columnName: "sub_offer_requested", tableName: "subscription", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1690522190841-3") {
        addNotNullConstraint(columnDataType: "boolean", columnName: "sub_participant_transfer_with_survey", tableName: "subscription", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1690522190841-4") {
        addNotNullConstraint(columnDataType: "boolean", columnName: "sub_renewal_sent", tableName: "subscription", validate: "true")
    }

//    TODO: check; or remove IssueEntitlement.status (nullable:true) ?
//    changeSet(author: "klober (generated)", id: "1690522190841-5") {
//        dropNotNullConstraint(columnDataType: "bigint", columnName: "ie_status_rv_fk", tableName: "issue_entitlement")
//    }
}
