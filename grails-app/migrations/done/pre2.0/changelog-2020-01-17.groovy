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
""")
				sql.execute("""
update property_definition set pd_rdc = 'access.choice.remote' where pd_rdc = 'Access choice remote';
update property_definition set pd_rdc = 'access.method' where pd_rdc = 'Access Method';
update property_definition set pd_rdc = 'access.method.ip' where pd_rdc = 'Access Method IP';
update property_definition set pd_rdc = 'access.point.type' where pd_rdc = 'Access Point Type';
update property_definition set pd_rdc = 'address.type' where pd_rdc = 'AddressType';
update property_definition set pd_rdc = 'authority' where pd_rdc = 'Authority';
update property_definition set pd_rdc = 'category.a.f' where pd_rdc = 'Category A-F';
update property_definition set pd_rdc = 'cluster.role' where pd_rdc = 'Cluster Role';
update property_definition set pd_rdc = 'cluster.type' where pd_rdc = 'ClusterType';
update property_definition set pd_rdc = 'combo.status' where pd_rdc = 'Combo Status';
update property_definition set pd_rdc = 'combo.type' where pd_rdc = 'Combo Type';
update property_definition set pd_rdc = 'concurrent.access' where pd_rdc = 'ConcurrentAccess';
update property_definition set pd_rdc = 'confidentiality' where pd_rdc = 'Confidentiality';
update property_definition set pd_rdc = 'contact.content.type' where pd_rdc = 'ContactContentType';
update property_definition set pd_rdc = 'contact.type' where pd_rdc = 'ContactType';
""")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1579264687321-13") {
		grailsChange {
			change {
				sql.execute("""
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
				sql.execute("""
update property_definition set pd_rdc = 'core.status' where pd_rdc = 'CoreStatus';
update property_definition set pd_rdc = 'cost.configuration' where pd_rdc = 'Cost configuration';
update property_definition set pd_rdc = 'cost.item.type' where pd_rdc = 'CostItem.Type';
update property_definition set pd_rdc = 'cost.item.category' where pd_rdc = 'CostItemCategory';
update property_definition set pd_rdc = 'cost.item.element' where pd_rdc = 'CostItemElement';
update property_definition set pd_rdc = 'cost.item.status' where pd_rdc = 'CostItemStatus';
update property_definition set pd_rdc = 'country' where pd_rdc = 'Country';
update property_definition set pd_rdc = 'creator.type' where pd_rdc = 'CreatorType';
update property_definition set pd_rdc = 'currency' where pd_rdc = 'Currency';
update property_definition set pd_rdc = 'customer.identifier.type' where pd_rdc = 'CustomerIdentifier.Type';
update property_definition set pd_rdc = 'document.context.status' where pd_rdc = 'Document Context Status';
update property_definition set pd_rdc = 'document.type' where pd_rdc = 'Document Type';
update property_definition set pd_rdc = 'existence' where pd_rdc = 'Existence';
""")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1579264687321-14") {
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
				sql.execute("""
update property_definition set pd_rdc = 'fact.metric' where pd_rdc = 'FactMetric';
update property_definition set pd_rdc = 'fact.type' where pd_rdc = 'FactType';
update property_definition set pd_rdc = 'federal.state' where pd_rdc = 'Federal State';
update property_definition set pd_rdc = 'funder.type' where pd_rdc = 'Funder Type';
update property_definition set pd_rdc = 'gender' where pd_rdc = 'Gender';
update property_definition set pd_rdc = 'ie.accept.status' where pd_rdc = 'IE Accept Status';
update property_definition set pd_rdc = 'ie.access.status' where pd_rdc = 'IE Access Status';
update property_definition set pd_rdc = 'ie.medium' where pd_rdc = 'IEMedium';
update property_definition set pd_rdc = 'ill.code' where pd_rdc = 'Ill code';
update property_definition set pd_rdc = 'indemnification' where pd_rdc = 'Indemnification';
update property_definition set pd_rdc = 'invoicing' where pd_rdc = 'Invoicing';
update property_definition set pd_rdc = 'ipv4.address.format' where pd_rdc = 'IPv4 Address Format';
update property_definition set pd_rdc = 'ipv6.address.format' where pd_rdc = 'IPv6 Address Format';
update property_definition set pd_rdc = 'language' where pd_rdc = 'Language';
update property_definition set pd_rdc = 'library.network' where pd_rdc = 'Library Network';
update property_definition set pd_rdc = 'license.category' where pd_rdc = 'LicenseCategory';
update property_definition set pd_rdc = 'license.remote.access' where pd_rdc = 'Lincense.RemoteAccess';
update property_definition set pd_rdc = 'library.type' where pd_rdc = 'Library Type';
update property_definition set pd_rdc = 'license.status' where pd_rdc = 'License Status';
update property_definition set pd_rdc = 'license.type' where pd_rdc = 'License Type';
""")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1579264687321-15") {
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
update refdata_category set rdc_description = 'license.arc.title.transfer.regulation' where rdc_description = 'License.Arc.TitleTransferRegulation';
""")
				sql.execute("""
update property_definition set pd_rdc = 'license.arc.archival.copy.content' where pd_rdc = 'License.Arc.ArchivalCopyContent';
update property_definition set pd_rdc = 'license.arc.archival.copy.cost' where pd_rdc = 'License.Arc.ArchivalCopyCost';
update property_definition set pd_rdc = 'license.arc.archival.copy.time' where pd_rdc = 'License.Arc.ArchivalCopyTime';
update property_definition set pd_rdc = 'license.arc.archival.copy.transmission.format' where pd_rdc = 'License.Arc.ArchivalCopyTransmissionFormat';
update property_definition set pd_rdc = 'license.arc.authorized' where pd_rdc = 'License.Arc.Authorized';
update property_definition set pd_rdc = 'license.arc.hosting.restriction' where pd_rdc = 'License.Arc.HostingRestriction';
update property_definition set pd_rdc = 'license.arc.hosting.solution' where pd_rdc = 'License.Arc.HostingSolution';
update property_definition set pd_rdc = 'license.arc.hosting.time' where pd_rdc = 'License.Arc.HostingTime';
update property_definition set pd_rdc = 'license.arc.payment.note' where pd_rdc = 'License.Arc.PaymentNote';
update property_definition set pd_rdc = 'license.arc.title.transfer.regulation' where pd_rdc = 'License.Arc.TitletransferRegulation';
update property_definition set pd_rdc = 'license.arc.title.transfer.regulation' where pd_rdc = 'License.Arc.TitleTransferRegulation';
""")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1579264687321-16") {
		grailsChange {
			change {
				sql.execute("""
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
				sql.execute("""
update property_definition set pd_rdc = 'license.oa.corresponding.author.identification' where pd_rdc = 'License.OA.CorrespondingAuthorIdentification';
update property_definition set pd_rdc = 'license.oa.earc.version' where pd_rdc = 'License.OA.eArcVersion';
update property_definition set pd_rdc = 'license.oa.license.to.publish' where pd_rdc = 'License.OA.LicenseToPublish';
update property_definition set pd_rdc = 'license.oa.receiving.modalities' where pd_rdc = 'License.OA.ReceivingModalities';
update property_definition set pd_rdc = 'license.oa.repository' where pd_rdc = 'License.OA.Repository';
update property_definition set pd_rdc = 'license.oa.type' where pd_rdc = 'License.OA.Type';
update property_definition set pd_rdc = 'license.remote.access2' where pd_rdc = 'License.RemoteAccess';
update property_definition set pd_rdc = 'license.statistics.delivery' where pd_rdc = 'License.Statistics.Delivery';
update property_definition set pd_rdc = 'license.statistics.format' where pd_rdc = 'License.Statistics.Format';
update property_definition set pd_rdc = 'license.statistics.frequency' where pd_rdc = 'License.Statistics.Frequency';
update property_definition set pd_rdc = 'license.statistics.standards' where pd_rdc = 'License.Statistics.Standards';
update property_definition set pd_rdc = 'license.statistics.user.creds' where pd_rdc = 'License.Statistics.UserCreds';
""")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1579264687321-17") {
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
				sql.execute("""
update property_definition set pd_rdc = 'link.type' where pd_rdc = 'Link Type';
update property_definition set pd_rdc = 'mail.template.language' where pd_rdc = 'MailTemplate Language';
update property_definition set pd_rdc = 'mail.template.type' where pd_rdc = 'MailTemplate Type';
update property_definition set pd_rdc = 'number.type' where pd_rdc = 'Number Type';
update property_definition set pd_rdc = 'organisational.role' where pd_rdc = 'Organisational Role';
update property_definition set pd_rdc = 'org.sector' where pd_rdc = 'OrgSector';
update property_definition set pd_rdc = 'org.status' where pd_rdc = 'OrgStatus';
update property_definition set pd_rdc = 'org.type' where pd_rdc = 'OrgRoleType';
""")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1579264687321-18") {
		grailsChange {
			change {
				sql.execute("""
update refdata_category set rdc_description = 'package.status' where rdc_description = 'Package Status';
update refdata_category set rdc_description = 'package.breakable' where rdc_description = 'Package.Breakable';
update refdata_category set rdc_description = 'package.consistent' where rdc_description = 'Package.Consistent';
update refdata_category set rdc_description = 'package.fixed' where rdc_description = 'Package.Fixed';
update refdata_category set rdc_description = 'package.list.status' where rdc_description = 'Package.ListStatus';
update refdata_category set rdc_description = 'package.scope' where rdc_description = 'Package.Scope';
update refdata_category set rdc_description = 'package.type' where rdc_description = 'Package.Type';
update refdata_category set rdc_description = 'pending.change.status' where rdc_description = 'PendingChangeStatus';
update refdata_category set rdc_description = 'permissions' where rdc_description = 'Permissions';
update refdata_category set rdc_description = 'person.contact.type' where rdc_description = 'Person Contact Type';
update refdata_category set rdc_description = 'person.function' where rdc_description = 'Person Function';
update refdata_category set rdc_description = 'person.position' where rdc_description = 'Person Position';
update refdata_category set rdc_description = 'person.responsibility' where rdc_description = 'Person Responsibility';
update refdata_category set rdc_description = 'platform.status' where rdc_description = 'Platform Status';
update refdata_category set rdc_description = 'reminder.method' where rdc_description = 'ReminderMethod';
update refdata_category set rdc_description = 'reminder.trigger' where rdc_description = 'ReminderTrigger';
update refdata_category set rdc_description = 'reminder.unit' where rdc_description = 'ReminderUnit';
""")
				sql.execute("""
update property_definition set pd_rdc = 'package.status' where pd_rdc = 'Package Status';
update property_definition set pd_rdc = 'package.breakable' where pd_rdc = 'Package.Breakable';
update property_definition set pd_rdc = 'package.consistent' where pd_rdc = 'Package.Consistent';
update property_definition set pd_rdc = 'package.fixed' where pd_rdc = 'Package.Fixed';
update property_definition set pd_rdc = 'package.list.status' where pd_rdc = 'Package.ListStatus';
update property_definition set pd_rdc = 'package.scope' where pd_rdc = 'Package.Scope';
update property_definition set pd_rdc = 'package.type' where pd_rdc = 'Package.Type';
update property_definition set pd_rdc = 'pending.change.status' where pd_rdc = 'PendingChangeStatus';
update property_definition set pd_rdc = 'permissions' where pd_rdc = 'Permissions';
update property_definition set pd_rdc = 'person.contact.type' where pd_rdc = 'Person Contact Type';
update property_definition set pd_rdc = 'person.function' where pd_rdc = 'Person Function';
update property_definition set pd_rdc = 'person.position' where pd_rdc = 'Person Position';
update property_definition set pd_rdc = 'person.responsibility' where pd_rdc = 'Person Responsibility';
update property_definition set pd_rdc = 'platform.status' where pd_rdc = 'Platform Status';
update property_definition set pd_rdc = 'reminder.method' where pd_rdc = 'ReminderMethod';
update property_definition set pd_rdc = 'reminder.trigger' where pd_rdc = 'ReminderTrigger';
update property_definition set pd_rdc = 'reminder.unit' where pd_rdc = 'ReminderUnit';
""")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1579264687321-19") {
		grailsChange {
			change {
				sql.execute("""
update refdata_category set rdc_description = 'semester' where rdc_description = 'Semester';
update refdata_category set rdc_description = 'share.configuration' where rdc_description = 'Share Configuration';
update refdata_category set rdc_description = 'sim.user.number' where rdc_description = 'Sim-User Number';
update refdata_category set rdc_description = 'subscription.form' where rdc_description = 'Subscription Form';
update refdata_category set rdc_description = 'subscription.resource' where rdc_description = 'Subscription Resource';
update refdata_category set rdc_description = 'subscription.status' where rdc_description = 'Subscription Status';
update refdata_category set rdc_description = 'subscription.type' where rdc_description = 'Subscription Type';
update refdata_category set rdc_description = 'survey.status' where rdc_description = 'Survey Status';
update refdata_category set rdc_description = 'survey.type' where rdc_description = 'Survey Type';
""")
sql.execute("""
update property_definition set pd_rdc = 'semester' where pd_rdc = 'Semester';
update property_definition set pd_rdc = 'share.configuration' where pd_rdc = 'Share Configuration';
update property_definition set pd_rdc = 'sim.user.number' where pd_rdc = 'Sim-User Number';
update property_definition set pd_rdc = 'subscription.form' where pd_rdc = 'Subscription Form';
update property_definition set pd_rdc = 'subscription.resource' where pd_rdc = 'Subscription Resource';
update property_definition set pd_rdc = 'subscription.status' where pd_rdc = 'Subscription Status';
update property_definition set pd_rdc = 'subscription.type' where pd_rdc = 'Subscription Type';
update property_definition set pd_rdc = 'survey.status' where pd_rdc = 'Survey Status';
update property_definition set pd_rdc = 'survey.type' where pd_rdc = 'Survey Type';
""")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1579264687321-20") {
		grailsChange {
			change {
				sql.execute("""
update refdata_category set rdc_description = 'task.priority' where rdc_description = 'Task Priority';
update refdata_category set rdc_description = 'task.status' where rdc_description = 'Task Status';
update refdata_category set rdc_description = 'tax.type' where rdc_description = 'TaxType';
update refdata_category set rdc_description = 'termination.condition' where rdc_description = 'Termination Condition';
update refdata_category set rdc_description = 'ticket.category' where rdc_description = 'Ticket.Category';
update refdata_category set rdc_description = 'ticket.status' where rdc_description = 'Ticket.Status';
update refdata_category set rdc_description = 'tipp.access.status' where rdc_description = 'TIPP Access Status';
update refdata_category set rdc_description = 'tipp.status' where rdc_description = 'TIPP Status';
update refdata_category set rdc_description = 'tipp.status.reason' where rdc_description = 'Tipp.StatusReason';
update refdata_category set rdc_description = 'title.type' where rdc_description = 'Title Type';
update refdata_category set rdc_description = 'tipp.delayed.oa' where rdc_description = 'TitleInstancePackagePlatform.DelayedOA';
update refdata_category set rdc_description = 'tipp.hybrid.oa' where rdc_description = 'TitleInstancePackagePlatform.HybridOA';
update refdata_category set rdc_description = 'tipp.payment.type' where rdc_description = 'TitleInstancePackagePlatform.PaymentType';
update refdata_category set rdc_description = 'title.status' where rdc_description = 'TitleInstanceStatus';
update refdata_category set rdc_description = 'transform.format' where rdc_description = 'Transform Format';
update refdata_category set rdc_description = 'transform.type' where rdc_description = 'Transform Type';
""")
				sql.execute("""
update property_definition set pd_rdc = 'task.priority' where pd_rdc = 'Task Priority';
update property_definition set pd_rdc = 'task.status' where pd_rdc = 'Task Status';
update property_definition set pd_rdc = 'tax.type' where pd_rdc = 'TaxType';
update property_definition set pd_rdc = 'termination.condition' where pd_rdc = 'Termination Condition';
update property_definition set pd_rdc = 'ticket.category' where pd_rdc = 'Ticket.Category';
update property_definition set pd_rdc = 'ticket.status' where pd_rdc = 'Ticket.Status';
update property_definition set pd_rdc = 'tipp.access.status' where pd_rdc = 'TIPP Access Status';
update property_definition set pd_rdc = 'tipp.status' where pd_rdc = 'TIPP Status';
update property_definition set pd_rdc = 'tipp.status.reason' where pd_rdc = 'Tipp.StatusReason';
update property_definition set pd_rdc = 'title.type' where pd_rdc = 'Title Type';
update property_definition set pd_rdc = 'tipp.delayed.oa' where pd_rdc = 'TitleInstancePackagePlatform.DelayedOA';
update property_definition set pd_rdc = 'tipp.hybrid.oa' where pd_rdc = 'TitleInstancePackagePlatform.HybridOA';
update property_definition set pd_rdc = 'tipp.payment.type' where pd_rdc = 'TitleInstancePackagePlatform.PaymentType';
update property_definition set pd_rdc = 'title.status' where pd_rdc = 'TitleInstanceStatus';
update property_definition set pd_rdc = 'transform.format' where pd_rdc = 'Transform Format';
update property_definition set pd_rdc = 'transform.type' where pd_rdc = 'Transform Type';
""")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1579264687321-21") {
		grailsChange {
			change {
				sql.execute("""
update refdata_category set rdc_description = 'usage.status' where rdc_description = 'UsageStatus';
update refdata_category set rdc_description = 'user.setting.dashboard.tab' where rdc_description = 'User.Settings.Dashboard.Tab';
update refdata_category set rdc_description = 'user.setting.theme' where rdc_description = 'User.Settings.Theme';
update refdata_category set rdc_description = 'y.n' where rdc_description = 'YN';
update refdata_category set rdc_description = 'y.n.o' where rdc_description = 'YNO';
update refdata_category set rdc_description = 'y.n.u' where rdc_description = 'YNU';
""")
				sql.execute("""
update property_definition set pd_rdc = 'usage.status' where pd_rdc = 'UsageStatus';
update property_definition set pd_rdc = 'user.setting.dashboard.tab' where pd_rdc = 'User.Settings.Dashboard.Tab';
update property_definition set pd_rdc = 'user.setting.theme' where pd_rdc = 'User.Settings.Theme';
update property_definition set pd_rdc = 'y.n' where pd_rdc = 'YN';
update property_definition set pd_rdc = 'y.n.o' where pd_rdc = 'YNO';
update property_definition set pd_rdc = 'y.n.u' where pd_rdc = 'YNU';
""")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1579264687321-22") {
		grailsChange {
			change {
				sql.execute("""
update survey_property set surpro_refdata_category = 'access.choice.remote' where surpro_refdata_category = 'Access choice remote';
update survey_property set surpro_refdata_category = 'access.method' where surpro_refdata_category = 'Access Method';
update survey_property set surpro_refdata_category = 'access.method.ip' where surpro_refdata_category = 'Access Method IP';
update survey_property set surpro_refdata_category = 'access.point.type' where surpro_refdata_category = 'Access Point Type';
update survey_property set surpro_refdata_category = 'address.type' where surpro_refdata_category = 'AddressType';
update survey_property set surpro_refdata_category = 'authority' where surpro_refdata_category = 'Authority';
update survey_property set surpro_refdata_category = 'category.a.f' where surpro_refdata_category = 'Category A-F';
update survey_property set surpro_refdata_category = 'cluster.role' where surpro_refdata_category = 'Cluster Role';
update survey_property set surpro_refdata_category = 'cluster.type' where surpro_refdata_category = 'ClusterType';
update survey_property set surpro_refdata_category = 'combo.status' where surpro_refdata_category = 'Combo Status';
update survey_property set surpro_refdata_category = 'combo.type' where surpro_refdata_category = 'Combo Type';
update survey_property set surpro_refdata_category = 'concurrent.access' where surpro_refdata_category = 'ConcurrentAccess';
update survey_property set surpro_refdata_category = 'confidentiality' where surpro_refdata_category = 'Confidentiality';
update survey_property set surpro_refdata_category = 'contact.content.type' where surpro_refdata_category = 'ContactContentType';
update survey_property set surpro_refdata_category = 'contact.type' where surpro_refdata_category = 'ContactType';
update survey_property set surpro_refdata_category = 'core.status' where surpro_refdata_category = 'CoreStatus';
update survey_property set surpro_refdata_category = 'cost.configuration' where surpro_refdata_category = 'Cost configuration';
update survey_property set surpro_refdata_category = 'cost.item.type' where surpro_refdata_category = 'CostItem.Type';
update survey_property set surpro_refdata_category = 'cost.item.category' where surpro_refdata_category = 'CostItemCategory';
update survey_property set surpro_refdata_category = 'cost.item.element' where surpro_refdata_category = 'CostItemElement';
update survey_property set surpro_refdata_category = 'cost.item.status' where surpro_refdata_category = 'CostItemStatus';
update survey_property set surpro_refdata_category = 'country' where surpro_refdata_category = 'Country';
update survey_property set surpro_refdata_category = 'creator.type' where surpro_refdata_category = 'CreatorType';
update survey_property set surpro_refdata_category = 'currency' where surpro_refdata_category = 'Currency';
update survey_property set surpro_refdata_category = 'customer.identifier.type' where surpro_refdata_category = 'CustomerIdentifier.Type';
update survey_property set surpro_refdata_category = 'document.context.status' where surpro_refdata_category = 'Document Context Status';
update survey_property set surpro_refdata_category = 'document.type' where surpro_refdata_category = 'Document Type';
update survey_property set surpro_refdata_category = 'existence' where surpro_refdata_category = 'Existence';
update survey_property set surpro_refdata_category = 'fact.metric' where surpro_refdata_category = 'FactMetric';
update survey_property set surpro_refdata_category = 'fact.type' where surpro_refdata_category = 'FactType';
update survey_property set surpro_refdata_category = 'federal.state' where surpro_refdata_category = 'Federal State';
update survey_property set surpro_refdata_category = 'funder.type' where surpro_refdata_category = 'Funder Type';
update survey_property set surpro_refdata_category = 'gender' where surpro_refdata_category = 'Gender';
update survey_property set surpro_refdata_category = 'ie.accept.status' where surpro_refdata_category = 'IE Accept Status';
update survey_property set surpro_refdata_category = 'ie.access.status' where surpro_refdata_category = 'IE Access Status';
update survey_property set surpro_refdata_category = 'ie.medium' where surpro_refdata_category = 'IEMedium';
update survey_property set surpro_refdata_category = 'ill.code' where surpro_refdata_category = 'Ill code';
update survey_property set surpro_refdata_category = 'indemnification' where surpro_refdata_category = 'Indemnification';
update survey_property set surpro_refdata_category = 'invoicing' where surpro_refdata_category = 'Invoicing';
update survey_property set surpro_refdata_category = 'ipv4.address.format' where surpro_refdata_category = 'IPv4 Address Format';
update survey_property set surpro_refdata_category = 'ipv6.address.format' where surpro_refdata_category = 'IPv6 Address Format';
update survey_property set surpro_refdata_category = 'language' where surpro_refdata_category = 'Language';
update survey_property set surpro_refdata_category = 'library.network' where surpro_refdata_category = 'Library Network';
update survey_property set surpro_refdata_category = 'license.category' where surpro_refdata_category = 'LicenseCategory';
update survey_property set surpro_refdata_category = 'license.remote.access' where surpro_refdata_category = 'Lincense.RemoteAccess';
update survey_property set surpro_refdata_category = 'library.type' where surpro_refdata_category = 'Library Type';
update survey_property set surpro_refdata_category = 'license.status' where surpro_refdata_category = 'License Status';
update survey_property set surpro_refdata_category = 'license.type' where surpro_refdata_category = 'License Type';
update survey_property set surpro_refdata_category = 'license.arc.archival.copy.content' where surpro_refdata_category = 'License.Arc.ArchivalCopyContent';
update survey_property set surpro_refdata_category = 'license.arc.archival.copy.cost' where surpro_refdata_category = 'License.Arc.ArchivalCopyCost';
update survey_property set surpro_refdata_category = 'license.arc.archival.copy.time' where surpro_refdata_category = 'License.Arc.ArchivalCopyTime';
update survey_property set surpro_refdata_category = 'license.arc.archival.copy.transmission.format' where surpro_refdata_category = 'License.Arc.ArchivalCopyTransmissionFormat';
update survey_property set surpro_refdata_category = 'license.arc.authorized' where surpro_refdata_category = 'License.Arc.Authorized';
update survey_property set surpro_refdata_category = 'license.arc.hosting.restriction' where surpro_refdata_category = 'License.Arc.HostingRestriction';
update survey_property set surpro_refdata_category = 'license.arc.hosting.solution' where surpro_refdata_category = 'License.Arc.HostingSolution';
update survey_property set surpro_refdata_category = 'license.arc.hosting.time' where surpro_refdata_category = 'License.Arc.HostingTime';
update survey_property set surpro_refdata_category = 'license.arc.payment.note' where surpro_refdata_category = 'License.Arc.PaymentNote';
update survey_property set surpro_refdata_category = 'license.arc.title.transfer.regulation' where surpro_refdata_category = 'License.Arc.TitletransferRegulation';
update survey_property set surpro_refdata_category = 'license.arc.title.transfer.regulation' where surpro_refdata_category = 'License.Arc.TitleTransferRegulation';
update survey_property set surpro_refdata_category = 'license.oa.corresponding.author.identification' where surpro_refdata_category = 'License.OA.CorrespondingAuthorIdentification';
update survey_property set surpro_refdata_category = 'license.oa.earc.version' where surpro_refdata_category = 'License.OA.eArcVersion';
update survey_property set surpro_refdata_category = 'license.oa.license.to.publish' where surpro_refdata_category = 'License.OA.LicenseToPublish';
update survey_property set surpro_refdata_category = 'license.oa.receiving.modalities' where surpro_refdata_category = 'License.OA.ReceivingModalities';
update survey_property set surpro_refdata_category = 'license.oa.repository' where surpro_refdata_category = 'License.OA.Repository';
update survey_property set surpro_refdata_category = 'license.oa.type' where surpro_refdata_category = 'License.OA.Type';
update survey_property set surpro_refdata_category = 'license.remote.access2' where surpro_refdata_category = 'License.RemoteAccess';
update survey_property set surpro_refdata_category = 'license.statistics.delivery' where surpro_refdata_category = 'License.Statistics.Delivery';
update survey_property set surpro_refdata_category = 'license.statistics.format' where surpro_refdata_category = 'License.Statistics.Format';
update survey_property set surpro_refdata_category = 'license.statistics.frequency' where surpro_refdata_category = 'License.Statistics.Frequency';
update survey_property set surpro_refdata_category = 'license.statistics.standards' where surpro_refdata_category = 'License.Statistics.Standards';
update survey_property set surpro_refdata_category = 'license.statistics.user.creds' where surpro_refdata_category = 'License.Statistics.UserCreds';
update survey_property set surpro_refdata_category = 'link.type' where surpro_refdata_category = 'Link Type';
update survey_property set surpro_refdata_category = 'mail.template.language' where surpro_refdata_category = 'MailTemplate Language';
update survey_property set surpro_refdata_category = 'mail.template.type' where surpro_refdata_category = 'MailTemplate Type';
update survey_property set surpro_refdata_category = 'number.type' where surpro_refdata_category = 'Number Type';
update survey_property set surpro_refdata_category = 'organisational.role' where surpro_refdata_category = 'Organisational Role';
update survey_property set surpro_refdata_category = 'org.sector' where surpro_refdata_category = 'OrgSector';
update survey_property set surpro_refdata_category = 'org.status' where surpro_refdata_category = 'OrgStatus';
update survey_property set surpro_refdata_category = 'org.type' where surpro_refdata_category = 'OrgRoleType';
update survey_property set surpro_refdata_category = 'package.status' where surpro_refdata_category = 'Package Status';
update survey_property set surpro_refdata_category = 'package.breakable' where surpro_refdata_category = 'Package.Breakable';
update survey_property set surpro_refdata_category = 'package.consistent' where surpro_refdata_category = 'Package.Consistent';
update survey_property set surpro_refdata_category = 'package.fixed' where surpro_refdata_category = 'Package.Fixed';
update survey_property set surpro_refdata_category = 'package.list.status' where surpro_refdata_category = 'Package.ListStatus';
update survey_property set surpro_refdata_category = 'package.scope' where surpro_refdata_category = 'Package.Scope';
update survey_property set surpro_refdata_category = 'package.type' where surpro_refdata_category = 'Package.Type';
update survey_property set surpro_refdata_category = 'pending.change.status' where surpro_refdata_category = 'PendingChangeStatus';
update survey_property set surpro_refdata_category = 'permissions' where surpro_refdata_category = 'Permissions';
update survey_property set surpro_refdata_category = 'person.contact.type' where surpro_refdata_category = 'Person Contact Type';
update survey_property set surpro_refdata_category = 'person.function' where surpro_refdata_category = 'Person Function';
update survey_property set surpro_refdata_category = 'person.position' where surpro_refdata_category = 'Person Position';
update survey_property set surpro_refdata_category = 'person.responsibility' where surpro_refdata_category = 'Person Responsibility';
update survey_property set surpro_refdata_category = 'platform.status' where surpro_refdata_category = 'Platform Status';
update survey_property set surpro_refdata_category = 'reminder.method' where surpro_refdata_category = 'ReminderMethod';
update survey_property set surpro_refdata_category = 'reminder.trigger' where surpro_refdata_category = 'ReminderTrigger';
update survey_property set surpro_refdata_category = 'reminder.unit' where surpro_refdata_category = 'ReminderUnit';
update survey_property set surpro_refdata_category = 'semester' where surpro_refdata_category = 'Semester';
update survey_property set surpro_refdata_category = 'share.configuration' where surpro_refdata_category = 'Share Configuration';
update survey_property set surpro_refdata_category = 'sim.user.number' where surpro_refdata_category = 'Sim-User Number';
update survey_property set surpro_refdata_category = 'subscription.form' where surpro_refdata_category = 'Subscription Form';
update survey_property set surpro_refdata_category = 'subscription.resource' where surpro_refdata_category = 'Subscription Resource';
update survey_property set surpro_refdata_category = 'subscription.status' where surpro_refdata_category = 'Subscription Status';
update survey_property set surpro_refdata_category = 'subscription.type' where surpro_refdata_category = 'Subscription Type';
update survey_property set surpro_refdata_category = 'survey.status' where surpro_refdata_category = 'Survey Status';
update survey_property set surpro_refdata_category = 'survey.type' where surpro_refdata_category = 'Survey Type';
update survey_property set surpro_refdata_category = 'task.priority' where surpro_refdata_category = 'Task Priority';
update survey_property set surpro_refdata_category = 'task.status' where surpro_refdata_category = 'Task Status';
update survey_property set surpro_refdata_category = 'tax.type' where surpro_refdata_category = 'TaxType';
update survey_property set surpro_refdata_category = 'termination.condition' where surpro_refdata_category = 'Termination Condition';
update survey_property set surpro_refdata_category = 'ticket.category' where surpro_refdata_category = 'Ticket.Category';
update survey_property set surpro_refdata_category = 'ticket.status' where surpro_refdata_category = 'Ticket.Status';
update survey_property set surpro_refdata_category = 'tipp.access.status' where surpro_refdata_category = 'TIPP Access Status';
update survey_property set surpro_refdata_category = 'tipp.status' where surpro_refdata_category = 'TIPP Status';
update survey_property set surpro_refdata_category = 'tipp.status.reason' where surpro_refdata_category = 'Tipp.StatusReason';
update survey_property set surpro_refdata_category = 'title.type' where surpro_refdata_category = 'Title Type';
update survey_property set surpro_refdata_category = 'tipp.delayed.oa' where surpro_refdata_category = 'TitleInstancePackagePlatform.DelayedOA';
update survey_property set surpro_refdata_category = 'tipp.hybrid.oa' where surpro_refdata_category = 'TitleInstancePackagePlatform.HybridOA';
update survey_property set surpro_refdata_category = 'tipp.payment.type' where surpro_refdata_category = 'TitleInstancePackagePlatform.PaymentType';
update survey_property set surpro_refdata_category = 'title.status' where surpro_refdata_category = 'TitleInstanceStatus';
update survey_property set surpro_refdata_category = 'transform.format' where surpro_refdata_category = 'Transform Format';
update survey_property set surpro_refdata_category = 'transform.type' where surpro_refdata_category = 'Transform Type';
update survey_property set surpro_refdata_category = 'usage.status' where surpro_refdata_category = 'UsageStatus';
update survey_property set surpro_refdata_category = 'user.setting.dashboard.tab' where surpro_refdata_category = 'User.Settings.Dashboard.Tab';
update survey_property set surpro_refdata_category = 'user.setting.theme' where surpro_refdata_category = 'User.Settings.Theme';
update survey_property set surpro_refdata_category = 'y.n' where surpro_refdata_category = 'YN';
update survey_property set surpro_refdata_category = 'y.n.o' where surpro_refdata_category = 'YNO';
update survey_property set surpro_refdata_category = 'y.n.u' where surpro_refdata_category = 'YNU';
""")
			}
			rollback {}
		}
	}

}