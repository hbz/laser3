package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1737556199573-1") {
        grailsChange {
            change {
                sql.execute('ALTER TABLE IF EXISTS vendor RENAME ven_prequalification_vol TO ven_prequalification')
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1737556199573-2") {
        grailsChange {
            change {
                sql.execute('ALTER TABLE IF EXISTS vendor RENAME ven_prequalification_vol_info TO ven_prequalification_info')
            }
            rollback {}
        }
    }
}
