package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1746009178569-1") {
        grailsChange {
            change {
                String query = "delete from links where l_id in (select l_id from links join subscription as s on sub_id = l_dest_sub_fk join license on lic_id = l_source_lic_fk where sub_parent_sub_fk is null and lic_parent_lic_fk is not null)"
                int count = sql.executeUpdate(query)
                String info = "${query}: ${count} deleted"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1746009178569-2") {
        grailsChange {
            change {
                String query = "delete from doc_context where dc_link_fk in " +
                        "(select l_id from links " +
                        "join org_role os on os.or_sub_fk = l_dest_sub_fk " +
                        "join org_role ol on ol.or_lic_fk = l_source_lic_fk " +
                        "where os.or_roletype_fk = (select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdv_value = 'Subscriber' and rdc_description = 'organisational.role') " +
                        "and ol.or_roletype_fk = (select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdv_value = 'Licensee_Consortial' and rdc_description = 'organisational.role'))"
                int count = sql.executeUpdate(query)
                String info = "${query}: ${count} deleted"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1746009178569-3") {
        grailsChange {
            change {
                String query = "delete from links where l_id in " +
                        "(select l_id from links " +
                        "join org_role os on os.or_sub_fk = l_dest_sub_fk " +
                        "join org_role ol on ol.or_lic_fk = l_source_lic_fk " +
                        "where os.or_roletype_fk = (select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdv_value = 'Subscriber' and rdc_description = 'organisational.role') " +
                        "and ol.or_roletype_fk = (select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdv_value = 'Licensee_Consortial' and rdc_description = 'organisational.role'))"
                int count = sql.executeUpdate(query)
                String info = "${query}: ${count} deleted"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

}
