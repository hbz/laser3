databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1579869872860-1") {
		addColumn(schemaName: "public", tableName: "property_definition") {
			column(name: "pd_explanation_de", type: "text")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1579869872860-2") {
		addColumn(schemaName: "public", tableName: "property_definition") {
			column(name: "pd_explanation_en", type: "text")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1579869872860-3") {
		addColumn(schemaName: "public", tableName: "property_definition") {
			column(name: "pd_name_de", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1579869872860-4") {
		addColumn(schemaName: "public", tableName: "property_definition") {
			column(name: "pd_name_en", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (modified)", id: "1579869872860-5") {
		grailsChange {
			change {
				sql.execute("""
update property_definition set pd_name_de = pd_name where pd_name is not null;
update property_definition set pd_name_en = pd_name where pd_name is not null;
""")
				sql.execute("""
update property_definition set pd_name_de = i10n_value_de, pd_name_en = i10n_value_en
from i10n_translation
where pd_id = i10n_reference_id and i10n_reference_class = 'com.k_int.properties.PropertyDefinition' and i10n_reference_field = 'name';
""")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1579869872860-6") {
		grailsChange {
			change {
				sql.execute("""
update property_definition set pd_explanation_de = pd_explanation where pd_explanation is not null;
update property_definition set pd_explanation_en = pd_explanation where pd_explanation is not null;
""")
				sql.execute("""
update property_definition set pd_explanation_de = i10n_value_de, pd_explanation_en = i10n_value_en
from i10n_translation
where pd_id = i10n_reference_id and i10n_reference_class = 'com.k_int.properties.PropertyDefinition' and i10n_reference_field = 'expl';
""")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1579869872860-7") {
		grailsChange {
			change {
				sql.execute("delete from i10n_translation where i10n_reference_class like 'com.k_int.properties.PropertyDefinition%' and i10n_reference_field = 'name';")
				sql.execute("delete from i10n_translation where i10n_reference_class like 'com.k_int.properties.PropertyDefinition%' and i10n_reference_field = 'expl';")
				sql.execute("delete from i10n_translation where i10n_reference_class like 'com.k_int.properties.PropertyDefinition%' and i10n_reference_field = 'descr';")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1579869872860-8") {
		addColumn(schemaName: "public", tableName: "refdata_value") {
			column(name: "rdv_explanation_de", type: "text")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1579869872860-9") {
		addColumn(schemaName: "public", tableName: "refdata_value") {
			column(name: "rdv_explanation_en", type: "text")
		}
	}

	changeSet(author: "kloberd (modified)", id: "1579869872860-10") {
		grailsChange {
			change {
				sql.execute("""
update refdata_value set rdv_explanation_de = i10n_value_de, rdv_explanation_en = i10n_value_en
from i10n_translation
where rdv_id = i10n_reference_id and i10n_reference_class = 'com.k_int.kbplus.RefdataValue' and i10n_reference_field = 'expl';
""")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1579869872860-11") {
		grailsChange {
			change {
				sql.execute("delete from i10n_translation where i10n_reference_class like 'com.k_int.kbplus.RefdataValue%' and i10n_reference_field = 'expl';")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1579869872860-12") {
		dropColumn(columnName: "pd_explanation", tableName: "property_definition")
	}
}
