databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1681375642877-1") {
        grailsChange {
            change {
                sql.execute("delete from pending_change where pc_msg_token = 'pendingChange.message_TP02'")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1681375642877-2") {
        grailsChange {
            change {
                sql.execute("delete from pending_change where pc_msg_token in ('pendingChange.message_TC01', 'pendingChange.message_TC02', 'pendingChange.message_TC03')")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1681375642877-3") {
        grailsChange {
            change {
                sql.execute("delete from pending_change where pc_msg_token in ('pendingChange.message_TR01', 'pendingChange.message_TR02', 'pendingChange.message_TR03')")
            }
            rollback {}
        }
    }

}
