databaseChangeLog = {

    changeSet(author: "agalffy (modified)", id: "1612793672241-1") {
        grailsChange {
            change {
                sql.execute('alter table title_history_event rename event_date to the_event_date')
            }
            rollback {}
        }
    }

    changeSet(author: "agalffy (generated)", id: "1612793672241-2") {
        addColumn(tableName: "title_history_event") {
            column(name: "the_from", type: "text")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1612793672241-3") {
        addColumn(tableName: "title_history_event") {
            column(name: "the_tipp_fk", type: "int8")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1612793672241-4") {
        addColumn(tableName: "title_history_event") {
            column(name: "the_to", type: "text")
        }
    }

}
