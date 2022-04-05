databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1621414115224-1") {
        grailsChange {
            change {
                sql.execute("delete from survey_result where surre_type_fk in (select pd_id from property_definition where pd_tenant_fk in (select os_org_fk from org_setting where os_key_enum = 'CUSTOMER_TYPE' and os_role_fk = (select id from public.role where authority = 'ORG_INST') and pd_description = 'Survey Property'))")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1621414115224-2") {
        grailsChange {
            change {
                sql.execute("delete from property_definition where pd_tenant_fk in (select os_org_fk from org_setting where os_key_enum = 'CUSTOMER_TYPE' and os_role_fk = (select id from public.role where authority = 'ORG_INST') and pd_description = 'Survey Property')")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1621414115224-3") {
        grailsChange {
            change {
                sql.execute("delete from org_property where op_type_fk in (select pd_id from property_definition where pd_name_de = 'Rechnungs-Zeitpunkt')")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1621414115224-4") {
        grailsChange {
            change {
                sql.execute("delete from property_definition where pd_name_de = 'Rechnungs-Zeitpunkt'")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1621414115224-5") {
        grailsChange {
            change {
                sql.execute("update identifier_namespace set idns_ns = 'dbis_res_id' where idns_ns = 'DBIS Anker'")
            }
            rollback {}
        }
    }

}
