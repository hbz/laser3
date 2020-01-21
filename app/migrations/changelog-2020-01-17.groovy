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
update refdata_category set rdc_description = 'authority' where rdc_description = 'Authority';
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

	changeSet(author: "kloberd (modified)", id: "1579264687321-13") {
		grailsChange {
			change {
				sql.execute("""
update refdata_category set rdc_description = 'fact.metric' where rdc_description = 'FactMetric';
update refdata_category set rdc_description = 'fact.type' where rdc_description = 'FactType';
update refdata_category set rdc_description = 'federal.state' where rdc_description = 'Federal State';
update refdata_category set rdc_description = 'funder.type' where rdc_description = 'Funder Type';
update refdata_category set rdc_description = 'gender' where rdc_description = 'Gender';
update refdata_category set rdc_description = 'ie.accept.status' where rdc_description = 'IE Accept Status';
update refdata_category set rdc_description = 'ie.access.status' where rdc_description = 'IE Access Status';
update refdata_category set rdc_description = 'ie.medium' where rdc_description = 'IEMedium';
update refdata_category set rdc_description = 'ill.code' where rdc_description = 'Ill code';
update refdata_category set rdc_description = 'indemnification' where rdc_description = 'Indemnification';
update refdata_category set rdc_description = 'invoicing' where rdc_description = 'Invoicing';
update refdata_category set rdc_description = 'ipv4.address.format' where rdc_description = 'IPv4 Address Format';
update refdata_category set rdc_description = 'ipv6.address.format' where rdc_description = 'IPv6 Address Format';
update refdata_category set rdc_description = 'language' where rdc_description = 'Language';
update refdata_category set rdc_description = 'library.network' where rdc_description = 'Library Network';
update refdata_category set rdc_description = 'license.category' where rdc_description = 'LicenseCategory';
update refdata_category set rdc_description = 'license.remote.access' where rdc_description = 'Lincense.RemoteAccess';
update refdata_category set rdc_description = 'library.type' where rdc_description = 'Library Type';
update refdata_category set rdc_description = 'license.status' where rdc_description = 'License Status';
update refdata_category set rdc_description = 'license.type' where rdc_description = 'License Type';
""")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1579264687321-14") {
		grailsChange {
			change {
				sql.execute("""
update refdata_category set rdc_description = 'license.arc.archival.copy.content' where rdc_description = 'License.Arc.ArchivalCopyContent';
update refdata_category set rdc_description = 'license.arc.archival.copy.cost' where rdc_description = 'License.Arc.ArchivalCopyCost';
update refdata_category set rdc_description = 'license.arc.archival.copy.time' where rdc_description = 'License.Arc.ArchivalCopyTime';
update refdata_category set rdc_description = 'license.arc.archival.copy.transmission.format' where rdc_description = 'License.Arc.ArchivalCopyTransmissionFormat';
update refdata_category set rdc_description = 'license.arc.authorized' where rdc_description = 'License.Arc.Authorized';
update refdata_category set rdc_description = 'license.arc.hosting.restriction' where rdc_description = 'License.Arc.HostingRestriction';
update refdata_category set rdc_description = 'license.arc.hosting.solution' where rdc_description = 'License.Arc.HostingSolution';
update refdata_category set rdc_description = 'license.arc.hosting.time' where rdc_description = 'License.Arc.HostingTime';
update refdata_category set rdc_description = 'license.arc.payment.note' where rdc_description = 'License.Arc.PaymentNote';
update refdata_category set rdc_description = 'license.arc.title.transfer.regulation' where rdc_description = 'License.Arc.TitletransferRegulation';
update refdata_category set rdc_description = 'license.oa.corresponding.author.identification' where rdc_description = 'License.OA.CorrespondingAuthorIdentification';
update refdata_category set rdc_description = 'license.oa.earc.version' where rdc_description = 'License.OA.eArcVersion';
update refdata_category set rdc_description = 'license.oa.license.to.publish' where rdc_description = 'License.OA.LicenseToPublish';
update refdata_category set rdc_description = 'license.oa.receiving.modalities' where rdc_description = 'License.OA.ReceivingModalities';
update refdata_category set rdc_description = 'license.oa.repository' where rdc_description = 'License.OA.Repository';
update refdata_category set rdc_description = 'license.oa.type' where rdc_description = 'License.OA.Type';
update refdata_category set rdc_description = 'license.remote.access2' where rdc_description = 'License.RemoteAccess';
update refdata_category set rdc_description = 'license.statistics.delivery' where rdc_description = 'License.Statistics.Delivery';
update refdata_category set rdc_description = 'license.statistics.format' where rdc_description = 'License.Statistics.Format';
update refdata_category set rdc_description = 'license.statistics.frequency' where rdc_description = 'License.Statistics.Frequency';
update refdata_category set rdc_description = 'license.statistics.standards' where rdc_description = 'License.Statistics.Standards';
update refdata_category set rdc_description = 'license.statistics.user.creds' where rdc_description = 'License.Statistics.UserCreds';
""")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1579264687321-15") {
		grailsChange {
			change {
				sql.execute("""
update refdata_category set rdc_description = 'link.type' where rdc_description = 'Link Type';
update refdata_category set rdc_description = 'mail.template.language' where rdc_description = 'MailTemplate Language';
update refdata_category set rdc_description = 'mail.template.type' where rdc_description = 'MailTemplate Type';
update refdata_category set rdc_description = 'number.type' where rdc_description = 'Number Type';
update refdata_category set rdc_description = 'organisational.role' where rdc_description = 'Organisational Role';
update refdata_category set rdc_description = 'org.sector' where rdc_description = 'OrgSector';
update refdata_category set rdc_description = 'org.status' where rdc_description = 'OrgStatus';
update refdata_category set rdc_description = 'org.type' where rdc_description = 'OrgRoleType';
""")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1579264687321-16") {
		grailsChange {
			change {
				sql.execute("""
update refdata_category set rdc_description = 'link.type' where rdc_description = 'Link Type';
update refdata_category set rdc_description = 'mail.template.language' where rdc_description = 'MailTemplate Language';
update refdata_category set rdc_description = 'mail.template.type' where rdc_description = 'MailTemplate Type';
update refdata_category set rdc_description = 'number.type' where rdc_description = 'Number Type';
update refdata_category set rdc_description = 'organisational.role' where rdc_description = 'Organisational Role';
update refdata_category set rdc_description = 'org.sector' where rdc_description = 'OrgSector';
update refdata_category set rdc_description = 'org.status' where rdc_description = 'OrgStatus';
update refdata_category set rdc_description = 'org.type' where rdc_description = 'OrgRoleType';
""")
			}
			rollback {}
		}
	}
	
}