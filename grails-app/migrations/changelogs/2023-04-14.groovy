package changelogs

databaseChangeLog = {

    changeSet(author: "djebeniani (generated)", id: "1681469132222-1") {
        grailsChange {
            change {
                sql.execute("delete from refdata_value where rdv_value = 'vpn'")
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1681469132222-2") {
        grailsChange {
            change {
                sql.execute("delete from org_access_point where oar_access_method_rv_fk = (select rdv_id from refdata_value where rdv_value = 'vpn')")
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1681469132222-3") {
        grailsChange {
            change {
                sql.execute("delete from access_point_data where apd_org_access_point_fk in (select oar_id from org_access_point where oar_access_method_rv_fk = (select rdv_id from refdata_value where rdv_value = 'vpn'))")
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1681469132222-4") {
        dropTable(tableName: "platform_access_method")
    }

    changeSet(author: "djebeniani (generated)", id: "1681469132222-5") {
        dropUniqueConstraint(constraintName: "platform_access_method_plat_guid_key", tableName: "platform_access_method")
    }

    changeSet(author: "djebeniani (generated)", id: "1681469132222-6") {
        dropForeignKeyConstraint(baseTableName: "platform_access_method", constraintName: "fke7e3f5508055d84b")
    }

    changeSet(author: "djebeniani (generated)", id: "1681469132222-7") {
        dropForeignKeyConstraint(baseTableName: "platform_access_method", constraintName: "fke7e3f55064d7720")
    }













}
