databaseChangeLog = {

    changeSet(author: "galffy (modified)", id: "1604478749938-1") {
        grailsChange {
            change {
                sql.execute("delete from subscription_property where sp_tenant_fk is null")
            }
            rollback{}
        }
    }

    changeSet(author: "galffy (modified)", id: "1604478749938-2") {
        addNotNullConstraint(columnDataType: "int8", columnName: "sp_tenant_fk", tableName: "subscription_property")
    }

    changeSet(author: "galffy (modified)", id: "1604478749938-3") {
        grailsChange {
            change {
                sql.execute("update license_property lp set lp_tenant_fk = pd_tenant_fk from property_definition where lp_type_fk = pd_id and lp_tenant_fk is null")
            }
            rollback{}
        }
    }

    changeSet(author: "galffy (modified)", id: "1604478749938-4") {
		addNotNullConstraint(columnDataType: "int8", columnName: "lp_tenant_fk", tableName: "license_property")
	}

    changeSet(author: "galffy (modified)", id: "1604478749938-5") {
        grailsChange {
            change {
                sql.execute("update person_property pp set pp_tenant_fk = pd_tenant_fk from property_definition where pp_type_fk = pd_id and pp_tenant_fk is null")
            }
            rollback{}
        }
    }

	changeSet(author: "galffy (modified)", id: "1604478749938-6") {
		addNotNullConstraint(columnDataType: "int8", columnName: "pp_tenant_fk", tableName: "person_property")
	}

    changeSet(author: "klober (generated)", id: "1604478749938-7") {
        addColumn(tableName: "user") {
            column(name: "image", type: "varchar(255)")
        }
    }

}
