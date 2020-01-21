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

	changeSet(author: "kloberd (modified)", id: "1579264687321-12") {
		grailsChange {
			change {
				sql.execute("""
update refdata_category set rdc_description = 'access.choice.remote' where rdc_description = 'Access choice remote';
update refdata_category set rdc_description = 'access.method' where rdc_description = 'Access Method';
update refdata_category set rdc_description = 'access.method.ip' where rdc_description = 'Access Method IP';
update refdata_category set rdc_description = 'access.point.type' where rdc_description = 'Access Point Type';
update refdata_category set rdc_description = 'address.type' where rdc_description = 'AddressType';
update refdata_category set rdc_description = 'category.a.f' where rdc_description = 'Category A-F';
update refdata_category set rdc_description = 'cluster.role' where rdc_description = 'Cluster Role';
update refdata_category set rdc_description = 'cluster.type' where rdc_description = 'ClusterType';
update refdata_category set rdc_description = 'combo.status' where rdc_description = 'Combo Status';
update refdata_category set rdc_description = 'combo.type' where rdc_description = 'Combo Type';
update refdata_category set rdc_description = 'concurrent.access' where rdc_description = 'ConcurrentAccess';
update refdata_category set rdc_description = 'confidentiality' where rdc_description = 'Confidentiality';
update refdata_category set rdc_description = 'contact.content.type' where rdc_description = 'ContactContentType';
update refdata_category set rdc_description = 'contact.type' where rdc_description = 'ContactType';
update refdata_category set rdc_description = 'core.status' where rdc_description = 'CoreStatus';
update refdata_category set rdc_description = 'cost.configuration' where rdc_description = 'Cost configuration';
update refdata_category set rdc_description = 'cost.item.type' where rdc_description = 'CostItem.Type';
update refdata_category set rdc_description = 'cost.item.category' where rdc_description = 'CostItemCategory';
update refdata_category set rdc_description = 'cost.item.element' where rdc_description = 'CostItemElement';
update refdata_category set rdc_description = 'cost.item.status' where rdc_description = 'CostItemStatus';
update refdata_category set rdc_description = 'country' where rdc_description = 'Country';
update refdata_category set rdc_description = 'creator.type' where rdc_description = 'CreatorType';
update refdata_category set rdc_description = 'currency' where rdc_description = 'Currency';
update refdata_category set rdc_description = 'customer.identifier.type' where rdc_description = 'CustomerIdentifier.Type';
update refdata_category set rdc_description = 'document.context.status' where rdc_description = 'Document Context Status';
update refdata_category set rdc_description = 'document.type' where rdc_description = 'Document Type';
update refdata_category set rdc_description = 'existence' where rdc_description = 'Existence';
""")
			}
			rollback {}
		}
	}
	
}