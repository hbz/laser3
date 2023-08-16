package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1692096951814-1") {
        grailsChange {
            change {
                sql.execute("delete from platform_property where plp_type_fk in (select pd_id from property_definition where pd_tenant_fk is null and pd_description = 'Platform Property' and pd_name != 'NatStat Supplier ID')")
                int deleted = sql.getUpdateCount()
                confirm("delete from platform_property where plp_type_fk in (select pd_id from property_definition where pd_tenant_fk is null and pd_description = 'Platform Property' and pd_name != 'NatStat Supplier ID'): ${deleted}")
                changeSet.setComments("delete from platform_property where plp_type_fk in (select pd_id from property_definition where pd_tenant_fk is null and pd_description = 'Platform Property' and pd_name != 'NatStat Supplier ID'): ${deleted}")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1692096951814-2") {
        grailsChange {
            change {
                sql.execute("delete from property_definition_group_binding where pdgb_property_definition_group_fk in (select pde_id from property_definition_group_item where pde_property_definition_fk in (select pd_id from property_definition where  pd_tenant_fk is null and pd_description = 'Platform Property' and pd_name != 'NatStat Supplier ID'))")
                int deleted = sql.getUpdateCount()
                confirm("delete from property_definition_group_binding where pdgb_property_definition_group_fk in (select pde_id from property_definition_group_item where pde_property_definition_fk in (select pd_id from property_definition where  pd_tenant_fk is null and pd_description = 'Platform Property' and pd_name != 'NatStat Supplier ID')): ${deleted}")
                changeSet.setComments("delete from property_definition_group_binding where pdgb_property_definition_group_fk in (select pde_id from property_definition_group_item where pde_property_definition_fk in (select pd_id from property_definition where  pd_tenant_fk is null and pd_description = 'Platform Property' and pd_name != 'NatStat Supplier ID')): ${deleted}")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1692096951814-3") {
        grailsChange {
            change {
                sql.execute("delete from property_definition_group_item where pde_property_definition_fk in (select pd_id from property_definition where  pd_tenant_fk is null and pd_description = 'Platform Property' and pd_name != 'NatStat Supplier ID')")
                int deleted = sql.getUpdateCount()
                confirm("delete from property_definition_group_item where pde_property_definition_fk in (select pd_id from property_definition where  pd_tenant_fk is null and pd_description = 'Platform Property' and pd_name != 'NatStat Supplier ID'): ${deleted}")
                changeSet.setComments("delete from property_definition_group_item where pde_property_definition_fk in (select pd_id from property_definition where  pd_tenant_fk is null and pd_description = 'Platform Property' and pd_name != 'NatStat Supplier ID'): ${deleted}")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1692096951814-4") {
        grailsChange {
            change {
                sql.execute("delete from property_definition_group where pdg_owner_type = 'Platform Property'")
                int deleted = sql.getUpdateCount()
                confirm("delete from property_definition_group where pdg_owner_type = 'Platform Property': ${deleted}")
                changeSet.setComments("delete from property_definition_group where pdg_owner_type = 'Platform Property': ${deleted}")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1692096951814-5") {
        grailsChange {
            change {
                sql.execute("delete from property_definition where pd_tenant_fk is null and pd_description = 'Platform Property' and pd_name != 'NatStat Supplier ID'")
                int deleted = sql.getUpdateCount()
                confirm("delete from property_definition where pd_tenant_fk is null and pd_description = 'Platform Property' and pd_name != 'NatStat Supplier ID': ${deleted}")
                changeSet.setComments("delete from property_definition where pd_tenant_fk is null and pd_description = 'Platform Property' and pd_name != 'NatStat Supplier ID': ${deleted}")
            }
            rollback {}
        }
    }

}
