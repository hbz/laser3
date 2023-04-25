databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1682412652800-1") {
        grailsChange {
            change {
                sql.executeUpdate("update global_record_source set grs_uri = 'https://wekb.hbz-nrw.de/api2/', grs_edit_uri = 'https://wekb.hbz-nrw.de/api2/' where grs_uri = 'https://wekb.hbz-nrw.de/api/'")
            }
            rollback {}
        }
    }

    //for QA datasets only!
    changeSet(author: "galffy (hand-coded)", id: "1682412652800-2") {
        grailsChange {
            change {
                sql.executeUpdate("update global_record_source set grs_uri = 'https://wekb-qa.hbz-nrw.de/api2/', grs_edit_uri = 'https://wekb-qa.hbz-nrw.de/api2/' where grs_uri = 'https://wekb-qa.hbz-nrw.de/api/'")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1682412652800-3") {
        grailsChange {
            change {
                sql.executeUpdate("update api_source set as_fix_token = '/api2' where as_fix_token like '/api%'")
            }
            rollback {}
        }
    }

}
