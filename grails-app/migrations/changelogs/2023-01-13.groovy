package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1673609493242-1") {
        grailsChange {
            change {
                sql.execute('update subscription_package set sp_date_created = sp_last_updated where sp_last_updated is not null and sp_date_created is null')
            }
            rollback {}
        }
    }
}
