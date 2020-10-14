databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1575618606564-1") {
		addColumn(schemaName: "public", tableName: "survey_config") {
			column(name: "surconf_transfer_workflow", type: "text")
		}
	}

	changeSet(author: "kloberd (modified)", id: "1575618606564-2") {
		grailsChange {
			change {
				sql.execute("update property_definition set pd_name='Sim-User Number', pd_type='class com.k_int.kbplus.RefdataValue', pd_rdc='Sim-User Number' where pd_name='Simuser Zahl'")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1575618606564-3") {
		grailsChange {
			change {
				sql.execute("update survey_property set surpro_refdata_category='Category A-F', surpro_type='class com.k_int.kbplus.RefdataValue' where surpro_name='Category A-F'")
				sql.execute("update survey_property set surpro_refdata_category='Access choice remote', surpro_name='Access choice remote', surpro_type='class com.k_int.kbplus.RefdataValue' where surpro_name='Access choice'")
			}
			rollback {}
		}
	}
}
