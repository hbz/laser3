databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1620971273062-1") {
        grailsChange {
            change {
                sql.execute("delete from links where l_id in ( select l_id from links join license l on l_source_lic_fk = lic_id join subscription on l_dest_sub_fk = sub_id where sub_parent_sub_fk is not null and lic_parent_lic_fk is null and exists ( select or_id from org_role where or_lic_fk = l_source_lic_fk and or_roletype_fk = (select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdv_value = 'Licensing Consortium' and rdc_description = 'organisational.role') ) );")
            }
            rollback {

            }
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1620971273062-2") {
        grailsChange {
            change {
                sql.execute("DELETE FROM links WHERE l_link_type_rv_fk = (select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdv_value = 'license' and rdc_description = 'link.type') and l_source_lic_fk is null")
            }
            rollback {

            }
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1620971273062-3") {
        grailsChange {
            change {
                sql.execute("delete from org_role where or_id in (select or_id from org_role join license on or_lic_fk = lic_id where or_roletype_fk = (select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdv_value = 'Licensee_Consortial' and rdc_description = 'organisational.role') and lic_parent_lic_fk is null)")
            }
            rollback {

            }
        }
    }

}
