package changelogs

import de.laser.RefdataCategory

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1662985584165-1") {
        dropForeignKeyConstraint(baseTableName: "issue_entitlement", constraintName: "fk2d45f6c71268c999")
    }

    changeSet(author: "klober (generated)", id: "1662985584165-2") {
        dropColumn(columnName: "core_status_id", tableName: "issue_entitlement")
    }

    changeSet(author: "klober (modified)", id: "1662985584165-3") {
        grailsChange {
            change {
                RefdataCategory cs = RefdataCategory.findByDesc('core.status')
                if (cs) {
                    sql.execute('delete from refdata_value where rdv_owner = ' + cs.id)
                    sql.execute('delete from refdata_category where rdc_id = ' + cs.id)
                }
            }
            rollback {}
        }
    }
}
