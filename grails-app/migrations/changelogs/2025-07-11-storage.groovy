package changelogs

databaseChangeLog = {

    changeSet(author: "klober (mofified)", id: "1752222272091-1") {
        grailsChange {
            change {
                sql.executeUpdate("alter table deleted_object rename column do_old_global_uid to do_old_laser_id")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (mofified)", id: "1752222272091-2") {
        grailsChange {
            change {
                sql.executeUpdate("alter table del_combo rename column delc_acc_org_guid to delc_acc_org_laser_id")
            }
            rollback {}
        }
    }
}
