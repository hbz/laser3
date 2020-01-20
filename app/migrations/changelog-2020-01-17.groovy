databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1579264687321-1") {
		addColumn(schemaName: "public", tableName: "refdata_category") {
			column(name: "rdc_description_de", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1579264687321-2") {
		addColumn(schemaName: "public", tableName: "refdata_category") {
			column(name: "rdc_description_en", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1579264687321-3") {
		addColumn(schemaName: "public", tableName: "refdata_value") {
			column(name: "rdv_value_de", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1579264687321-4") {
		addColumn(schemaName: "public", tableName: "refdata_value") {
			column(name: "rdv_value_en", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1579264687321-5") {
		createIndex(indexName: "rdc_description_de_idx", schemaName: "public", tableName: "refdata_category") {
			column(name: "rdc_description_de")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1579264687321-6") {
		createIndex(indexName: "rdc_description_en_idx", schemaName: "public", tableName: "refdata_category") {
			column(name: "rdc_description_en")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1579264687321-7") {
		createIndex(indexName: "rdv_value_de_idx", schemaName: "public", tableName: "refdata_value") {
			column(name: "rdv_value_de")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1579264687321-8") {
		createIndex(indexName: "rdv_value_en_idx", schemaName: "public", tableName: "refdata_value") {
			column(name: "rdv_value_en")
		}
	}

	changeSet(author: "kloberd (modified)", id: "1579264687321-9") {
		grailsChange {
			change {
				sql.execute("""
update refdata_category set rdc_description_de = i10n_value_de, rdc_description_en = i10n_value_en
from i10n_translation
where rdc_id = i10n_reference_id and i10n_reference_class = 'com.k_int.kbplus.RefdataCategory' and i10n_reference_field = 'desc';
""")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1579264687321-10") {
		grailsChange {
			change {
				sql.execute("""
update refdata_value set rdv_value_de = i10n_value_de, rdv_value_en = i10n_value_en
from i10n_translation
where rdv_id = i10n_reference_id and i10n_reference_class = 'com.k_int.kbplus.RefdataValue' and i10n_reference_field = 'value';
""")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1579264687321-11") {
		grailsChange {
			change {
				sql.execute("delete from i10n_translation where i10n_reference_class like 'com.k_int.kbplus.RefdataCategory%' and i10n_reference_field = 'desc';")
				sql.execute("delete from i10n_translation where i10n_reference_class like 'com.k_int.kbplus.RefdataValue%' and i10n_reference_field = 'value';")
			}
			rollback {}
		}
	}

}