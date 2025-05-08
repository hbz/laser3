package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1746534094963-1") {
        grailsChange {
            change {
                String headAccessServices = "select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdv_value = 'Head Access Services' and rdc_description = 'person.position'"
                String erwerbungsleitung = "select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdv_value = 'Erwerbungsleitung' and rdc_description = 'person.position'"
                String query = "update person_role set pr_position_type_rv_fk = (${headAccessServices}) where pr_position_type_rv_fk = (${erwerbungsleitung})"
                int count = sql.executeUpdate(query)
                String info = "${query}: ${count} updated"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1746534094963-2") {
        grailsChange {
            change {
                String erwerbungsleitung = "select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdv_value = 'Erwerbungsleitung' and rdc_description = 'person.position'"
                String query = "delete from refdata_value where rdv_id = (${erwerbungsleitung})"
                int count = sql.executeUpdate(query)
                String info = "${query}: ${count} deleted"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

    changeSet(author: "klober (generated)", id: "1746534094963-3") {
        dropColumn(columnName: "lp_int_value", tableName: "license_property")
    }

    changeSet(author: "klober (generated)", id: "1746534094963-4") {
        dropColumn(columnName: "op_int_value", tableName: "org_property")
    }

    changeSet(author: "klober (generated)", id: "1746534094963-5") {
        dropColumn(columnName: "plp_int_value", tableName: "platform_property")
    }

    changeSet(author: "klober (generated)", id: "1746534094963-6") {
        dropColumn(columnName: "pp_int_value", tableName: "person_property")
    }

    changeSet(author: "klober (generated)", id: "1746534094963-7") {
        dropColumn(columnName: "prp_int_value", tableName: "provider_property")
    }

    changeSet(author: "klober (generated)", id: "1746534094963-8") {
        dropColumn(columnName: "sp_int_value", tableName: "subscription_property")
    }

    changeSet(author: "klober (generated)", id: "1746534094963-9") {
        dropColumn(columnName: "surre_int_value", tableName: "survey_result")
    }

    changeSet(author: "klober (generated)", id: "1746534094963-10") {
        dropColumn(columnName: "vp_int_value", tableName: "vendor_property")
    }
}
