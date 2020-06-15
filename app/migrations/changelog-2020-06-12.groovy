databaseChangeLog = {

	changeSet(author: "agalffy (modified)", id: "1591943606947-1") {
		grailsChange {
			change {
				sql.execute("update property_definition set pd_type = 'class java.math.BigDecimal' where pd_name = 'DFG funding amount'")
			}
		}
		rollback {}
	}

	changeSet(author: "agalffy (modified)", id: "1591943606947-2") {
		grailsChange {
			change {
				sql.execute("update subscription_custom_property set dec_value = int_value where type_id = (select pd_id from property_definition where pd_name = 'DFG funding amount');")
			}
		}
		rollback {}
	}

	changeSet(author: "agalffy (modified)", id: "1591943606947-3") {
		grailsChange {
			change {
				sql.execute("update subscription_custom_property set int_value = null where type_id = (select pd_id from property_definition where pd_name = 'DFG funding amount');")
			}
		}
		rollback {}
	}

	changeSet(author: "agalffy (modified)", id: "1591943606947-4") {
		grailsChange {
			change {
				sql.execute("update property_definition set pd_type = 'class java.lang.String' where pd_name = 'Specialised statistics / classification' and pd_tenant_fk is null")
			}
		}
		rollback {}
	}

	changeSet(author: "agalffy (modified)", id: "1591943606947-5") {
		grailsChange {
			change {
				sql.execute("update subscription_custom_property set string_value = int_value where type_id = (select pd_id from property_definition where pd_name = 'Specialised statistics / classification' and pd_tenant_fk is null);")
			}
		}
		rollback {}
	}

	changeSet(author: "agalffy (modified)", id: "1591943606947-6") {
		grailsChange {
			change {
				sql.execute("update subscription_custom_property set int_value = null where type_id = (select pd_id from property_definition where pd_name = 'Specialised statistics / classification' and pd_tenant_fk is null);")
			}
		}
		rollback {}
	}

}
