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
                String query = "update platform set plat_natstat_supplier_id = (select plp_string_value from platform_property join property_definition on plp_type_fk = pd_id where plp_owner_fk = plat_id and pd_name = 'NatStat Supplier ID' and plp_string_value is not null order by pd_last_updated desc limit 1)"
                sql.execute(query)
                int updated = sql.getUpdateCount()
                confirm("${query}: ${updated}")
                changeSet.setComments("${query}: ${updated}")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1707298648291-3") {
        grailsChange {
            change {
                String query = "delete from platform_property where plp_type_fk = (select pd_id from property_definition where pd_name = 'NatStat Supplier ID')"
                sql.execute(query)
                int deleted = sql.getUpdateCount()
                confirm("${query}: ${deleted}")
                changeSet.setComments("${query}: ${deleted}")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1707298648291-4") {
        grailsChange {
            change {
                String query = "delete from property_definition_group_binding where pdgb_property_definition_group_fk in (select pde_id from property_definition_group_item join property_definition on pde_property_definition_fk = pd_id where pd_name = 'NatStat Supplier ID')"
                sql.execute(query)
                int deleted = sql.getUpdateCount()
                confirm("${query}: ${deleted}")
                changeSet.setComments("${query}: ${deleted}")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1707298648291-5") {
        grailsChange {
            change {
                String query = "delete from property_definition_group_item where pde_property_definition_fk = (select pd_id from property_definition where pd_name = 'NatStat Supplier ID')"
                sql.execute(query)
                int deleted = sql.getUpdateCount()
                confirm("${query}: ${deleted}")
                changeSet.setComments("${query}: ${deleted}")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1707298648291-7") {
        grailsChange {
            change {
                String query = "delete from property_definition where pd_name = 'NatStat Supplier ID'"
                sql.execute(query)
                int deleted = sql.getUpdateCount()
                confirm("${query}: ${deleted}")
                changeSet.setComments("${query}: ${deleted}")
            }
            rollback {}
        }
    }
}
