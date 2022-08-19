package changelogs

import de.laser.RefdataCategory

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1660128579837-1") {
        dropColumn(columnName: "ie_reason", tableName: "issue_entitlement")
    }

    changeSet(author: "klober (modified)", id: "1660128579837-2") {
        grailsChange {
            change {
                ['User', 'UsedResource', 'usage.status'].each{
                    RefdataCategory rdc = RefdataCategory.findByDesc(it)
                    if (rdc) {
                        println '|--> deleting refdata with/and category: ' + it
                        sql.execute('delete from refdata_value where rdv_owner = ' + rdc.id)
                        sql.execute('delete from refdata_category where rdc_id = ' + rdc.id)
                    }
                }
            }
            rollback {}
        }
    }
}