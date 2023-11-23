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

    changeSet(author: "klober (modified)", id: "1690522190841-5") {
        grailsChange {
            change {
                sql.execute("delete from system_event where date_part('Year', se_created) = 2022")

            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1690522190841-6") {
        grailsChange {
            change {
                sql.execute("delete from system_activity_profiler where date_part('Year', sap_date_created) = 2022")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1690522190841-7") {
        grailsChange {
            change {
                sql.execute("delete from system_profiler where sp_archive = '3.0'")
                sql.execute("delete from system_profiler where sp_archive = '3.1-RC'")
                sql.execute("delete from system_profiler where sp_archive = '3.2-RC'")
            }
            rollback {}
        }
    }

//    TODO: check; or remove IssueEntitlement.status (nullable:true) ?
//    changeSet(author: "klober (generated)", id: "1690522190841-8") {
//        dropNotNullConstraint(columnDataType: "bigint", columnName: "ie_status_rv_fk", tableName: "issue_entitlement")
//    }
}
