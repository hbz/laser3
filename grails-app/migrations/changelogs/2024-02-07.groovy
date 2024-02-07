package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1707298648291-1") {
        addColumn(tableName: "platform") {
            column(name: "plat_natstat_supplier_id", type: "text")
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1707298648291-2") {
        grailsChange {
            change {
                sql.execute("update platform set plat_natstat_supplier_id = (select plp_string_value from platform_property join property_definition on plp_type_fk = pd_id where plp_owner_fk = plat_id and pd_name = 'NatStat Supplier ID' order by pd_last_updated desc limit 1)")
                int deleted = sql.getUpdateCount()
                confirm("update platform set plat_natstat_supplier_id = (select plp_string_value from platform_property join property_definition on plp_type_fk = pd_id where plp_owner_fk = plat_id and pd_name = 'NatStat Supplier ID' order by pd_last_updated desc limit 1): ${deleted}")
                changeSet.setComments("update platform set plat_natstat_supplier_id = (select plp_string_value from platform_property join property_definition on plp_type_fk = pd_id where plp_owner_fk = plat_id and pd_name = 'NatStat Supplier ID' order by pd_last_updated desc limit 1): ${deleted}")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1707298648291-3") {
        grailsChange {
            change {
                sql.execute("delete from platform_property where plp_type_fk = (select pd_id from property_definition where pd_name = 'NatStat Supplier ID')")
                int deleted = sql.getUpdateCount()
                confirm("delete from platform_property where plp_type_fk = (select pd_id from property_definition where pd_name = 'NatStat Supplier ID'): ${deleted}")
                changeSet.setComments("delete from platform_property where plp_type_fk = (select pd_id from property_definition where pd_name = 'NatStat Supplier ID'): ${deleted}")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1707298648291-4") {
        grailsChange {
            change {
                sql.execute("delete from property_definition_group_binding where pdgb_property_definition_group_fk in (select pde_id from property_definition_group_item join property_definition on pde_property_definition_fk = pd_id where pd_name = 'NatStat Supplier ID')")
                int deleted = sql.getUpdateCount()
                confirm("delete from property_definition_group_binding where pdgb_property_definition_group_fk in (select pde_id from property_definition_group_item join property_definition on pde_property_definition_fk = pd_id where pd_name = 'NatStat Supplier ID'): ${deleted}")
                changeSet.setComments("delete from property_definition_group_binding where pdgb_property_definition_group_fk in (select pde_id from property_definition_group_item join property_definition on pde_property_definition_fk = pd_id where pd_name = 'NatStat Supplier ID'): ${deleted}")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1707298648291-5") {
        grailsChange {
            change {
                sql.execute("delete from property_definition_group_item where pde_property_definition_fk = (select pd_id from property_definition where pd_name = 'NatStat Supplier ID')")
                int deleted = sql.getUpdateCount()
                confirm("delete from property_definition_group_item where pde_property_definition_fk = (select pd_id from property_definition where pd_name = 'NatStat Supplier ID'): ${deleted}")
                changeSet.setComments("delete from property_definition_group_item where pde_property_definition_fk = (select pd_id from property_definition where pd_name = 'NatStat Supplier ID'): ${deleted}")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1707298648291-7") {
        grailsChange {
            change {
                sql.execute("delete from property_definition where pd_name = 'NatStat Supplier ID'")
                int deleted = sql.getUpdateCount()
                confirm("delete from property_definition where pd_name = 'NatStat Supplier ID': ${deleted}")
                changeSet.setComments("delete from property_definition where pd_name = 'NatStat Supplier ID': ${deleted}")
            }
            rollback {}
        }
    }
}
