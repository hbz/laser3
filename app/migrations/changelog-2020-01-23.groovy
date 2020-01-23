databaseChangeLog = {

	changeSet(author: "kloberd (modified)", id: "tmp-x") {
		grailsChange {
			change {
				sql.execute("""
update property_definition set pd_name_de = i10n_value_de, pd_name_en = i10n_value_en
from i10n_translation
where pd_id = i10n_reference_id and i10n_reference_class = 'com.k_int.properties.PropertyDefinition' and i10n_reference_field = 'name';
""")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "tmp-xx") {
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

	changeSet(author: "kloberd (modified)", id: "tmp-xxx") {
		grailsChange {
			change {
				sql.execute("delete from i10n_translation where i10n_reference_class like 'com.k_int.properties.PropertyDefinition%' and i10n_reference_field = 'name';")
				sql.execute("delete from i10n_translation where i10n_reference_class like 'com.k_int.properties.PropertyDefinition%' and i10n_reference_field = 'expl';")
			}
			rollback {}
		}
	}
}