package changelogs

databaseChangeLog = {

    changeSet(author: "klober (hand-coded)", id: "1750780914926-1") {
        grailsChange {
            change {
                sql.executeUpdate("alter table system_announcement rename to service_message")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (hand-coded)", id: "1750780914926-2") {
        grailsChange {
            change {
                sql.executeUpdate("update refdata_value set rdv_value='Service Messages', rdv_value_de='Service-Meldungen', rdv_value_en='Service Messages' where rdv_value='Announcements' and rdv_is_hard_data is true")
            }
            rollback {}
        }
    }
}
