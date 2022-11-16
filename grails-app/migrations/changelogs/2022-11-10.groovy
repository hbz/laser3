package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1668084149642-1") {
        grailsChange {
            change {
                sql.execute("delete from pending_change where pc_msg_token = 'pendingChange.message_TP04' and pc_oid is not null")
            }
            rollback {}
        }
    }
}
