databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1620118196310-1") {
        grailsChange {
            change {
                sql.execute("delete from subscription_property where sp_type_fk in (select pd_id from property_definition join property_definition_group_item on pde_property_definition_fk = pd_id where pd_tenant_fk is not null);")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1620118196310-2") {
        grailsChange {
            change {
                sql.execute("delete from license_property where lp_type_fk in (select pd_id from property_definition join property_definition_group_item on pde_property_definition_fk = pd_id where pd_tenant_fk is not null);")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1620118196310-3") {
        grailsChange {
            change {
                sql.execute("delete from org_property where op_type_fk in (select pd_id from property_definition join property_definition_group_item on pde_property_definition_fk = pd_id where pd_tenant_fk is not null);")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1620118196310-4") {
        grailsChange {
            change {
                sql.execute("delete from property_definition_group_item using property_definition where pde_property_definition_fk = pd_id and pd_tenant_fk is not null;")
            }
            rollback {}
        }
    }

}
