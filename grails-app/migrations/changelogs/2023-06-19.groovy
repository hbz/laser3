package changelogs

databaseChangeLog = {

    changeSet(author: "djebeniani (generated)", id: "1687207898506-1") {
        addColumn(tableName: "subscription") {
            column(name: "sub_offer_accepted", type: "boolean")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1687207898506-2") {
        addColumn(tableName: "subscription") {
            column(name: "sub_offer_note", type: "varchar(255)")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1687207898506-3") {
        addColumn(tableName: "subscription") {
            column(name: "sub_offer_requested", type: "boolean")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1687207898506-4") {
        addColumn(tableName: "subscription") {
            column(name: "sub_offer_requested_date", type: "timestamp")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1687207898506-5") {
        addColumn(tableName: "subscription") {
            column(name: "sub_participant_transfer_with_survey", type: "boolean")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1687207898506-6") {
        addColumn(tableName: "subscription") {
            column(name: "sub_price_increase_info", type: "varchar(255)")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1687207898506-7") {
        addColumn(tableName: "subscription") {
            column(name: "sub_renewal_sent", type: "boolean")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1687207898506-8") {
        addColumn(tableName: "subscription") {
            column(name: "sub_renewal_sent_date", type: "timestamp")
        }
    }

    changeSet(author: "djebeniani (modified)", id: "1687207898506-9") {
        grailsChange {
            change {
                sql.execute("UPDATE subscription set sub_participant_transfer_with_survey = false WHERE sub_participant_transfer_with_survey is null")
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (modified)", id: "1687207898506-10") {
        grailsChange {
            change {
                sql.execute("UPDATE subscription set sub_offer_accepted = false WHERE sub_offer_accepted is null")
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (modified)", id: "1687207898506-11") {
        grailsChange {
            change {
                sql.execute("UPDATE subscription set sub_offer_requested = false WHERE sub_offer_requested is null")
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (modified)", id: "1687207898506-12") {
        grailsChange {
            change {
                sql.execute("UPDATE subscription set sub_renewal_sent = false WHERE sub_renewal_sent is null")
            }
            rollback {}
        }
    }

}
