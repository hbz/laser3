databaseChangeLog = {

	changeSet(author: "kloberd (modified)", id: "1575963571978-1") {
		grailsChange {
			change {
				sql.execute("truncate table system_profiler restart identity")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1575963571978-2") {
		addColumn(schemaName: "public", tableName: "audit_config") {
			column(name: "auc_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1575963571978-3") {
		addColumn(schemaName: "public", tableName: "audit_config") {
			column(name: "auc_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1575963571978-4") {
		addColumn(schemaName: "public", tableName: "dashboard_due_date") {
			column(name: "das_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1575963571978-5") {
		addColumn(schemaName: "public", tableName: "i10n_translation") {
			column(name: "i10n_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1575963571978-6") {
		addColumn(schemaName: "public", tableName: "i10n_translation") {
			column(name: "i10n_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1575963571978-7") {
		addColumn(schemaName: "public", tableName: "issue_entitlement_coverage") {
			column(name: "ic_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1575963571978-8") {
		addColumn(schemaName: "public", tableName: "issue_entitlement_coverage") {
			column(name: "ic_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1575963571978-9") {
		addColumn(schemaName: "public", tableName: "price_item") {
			column(name: "pi_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1575963571978-10") {
		addColumn(schemaName: "public", tableName: "price_item") {
			column(name: "pi_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1575963571978-11") {
		addColumn(schemaName: "public", tableName: "property_definition") {
			column(name: "pd_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1575963571978-12") {
		addColumn(schemaName: "public", tableName: "property_definition") {
			column(name: "pd_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1575963571978-13") {
		addColumn(schemaName: "public", tableName: "property_definition_group") {
			column(name: "pdg_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1575963571978-14") {
		addColumn(schemaName: "public", tableName: "property_definition_group") {
			column(name: "pdg_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1575963571978-15") {
		addColumn(schemaName: "public", tableName: "property_definition_group_binding") {
			column(name: "pbg_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1575963571978-16") {
		addColumn(schemaName: "public", tableName: "property_definition_group_binding") {
			column(name: "pbg_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1575963571978-17") {
		addColumn(schemaName: "public", tableName: "property_definition_group_item") {
			column(name: "pde_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1575963571978-18") {
		addColumn(schemaName: "public", tableName: "property_definition_group_item") {
			column(name: "pde_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1575963571978-19") {
		addColumn(schemaName: "public", tableName: "tippcoverage") {
			column(name: "tc_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1575963571978-20") {
		addColumn(schemaName: "public", tableName: "tippcoverage") {
			column(name: "tc_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1575963571978-21") {
		addColumn(schemaName: "public", tableName: "user_org") {
			column(name: "uo_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1575963571978-22") {
		addColumn(schemaName: "public", tableName: "user_org") {
			column(name: "uo_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1575963571978-23") {
		addColumn(schemaName: "public", tableName: "user_role") {
			column(name: "ur_date_created", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1575963571978-24") {
		addColumn(schemaName: "public", tableName: "user_role") {
			column(name: "ur_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (modified)", id: "1575963571978-25") {
		grailsChange {
			change {
				sql.execute("""
alter table user_org alter column uo_date_created type timestamp using uo_date_created::timestamp;
update user_org set uo_date_created = (uo_date_created + '1 hour'::interval);
alter table user_org alter column uo_last_updated type timestamp using uo_last_updated::timestamp;
update user_org set uo_last_updated = (uo_last_updated + '1 hour'::interval);
alter table user_role alter column ur_date_created type timestamp using ur_date_created::timestamp;
update user_role set ur_date_created = (ur_date_created + '1 hour'::interval);
alter table user_role alter column ur_last_updated type timestamp using ur_last_updated::timestamp;
update user_role set ur_last_updated = (ur_last_updated + '1 hour'::interval);
""")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1575963571978-26") {
		grailsChange {
			change {
				sql.execute("""
alter table audit_config alter column auc_date_created type timestamp using auc_date_created::timestamp;
update audit_config set auc_date_created = (auc_date_created + '1 hour'::interval);
alter table audit_config alter column auc_last_updated type timestamp using auc_last_updated::timestamp;
update audit_config set auc_last_updated = (auc_last_updated + '1 hour'::interval);
alter table dashboard_due_date alter column das_date_created type timestamp using das_date_created::timestamp;
update dashboard_due_date set das_date_created = (das_date_created + '1 hour'::interval);
alter table i10n_translation alter column i10n_date_created type timestamp using i10n_date_created::timestamp;
update i10n_translation set i10n_date_created = (i10n_date_created + '1 hour'::interval);
alter table i10n_translation alter column i10n_last_updated type timestamp using i10n_last_updated::timestamp;
update i10n_translation set i10n_last_updated = (i10n_last_updated + '1 hour'::interval);
alter table issue_entitlement_coverage alter column ic_date_created type timestamp using ic_date_created::timestamp;
update issue_entitlement_coverage set ic_date_created = (ic_date_created + '1 hour'::interval);
alter table issue_entitlement_coverage alter column ic_last_updated type timestamp using ic_last_updated::timestamp;
update issue_entitlement_coverage set ic_last_updated = (ic_last_updated + '1 hour'::interval);
alter table tippcoverage alter column tc_date_created type timestamp using tc_date_created::timestamp;
update tippcoverage set tc_date_created = (tc_date_created + '1 hour'::interval);
alter table tippcoverage alter column tc_last_updated type timestamp using tc_last_updated::timestamp;
update tippcoverage set tc_last_updated = (tc_last_updated + '1 hour'::interval);
""")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1575963571978-27") {
		grailsChange {
			change {
				sql.execute("""
alter table price_item alter column pi_date_created type timestamp using pi_date_created::timestamp;
update price_item set pi_date_created = (pi_date_created + '1 hour'::interval);
alter table price_item alter column pi_last_updated type timestamp using pi_last_updated::timestamp;
update price_item set pi_last_updated = (pi_last_updated + '1 hour'::interval);
alter table property_definition_group_item alter column pde_date_created type timestamp using pde_date_created::timestamp;
update property_definition_group_item set pde_date_created = (pde_date_created + '1 hour'::interval);
alter table property_definition_group_item alter column pde_last_updated type timestamp using pde_last_updated::timestamp;
update property_definition_group_item set pde_last_updated = (pde_last_updated + '1 hour'::interval);
alter table property_definition alter column pd_date_created type timestamp using pd_date_created::timestamp;
update property_definition set pd_date_created = (pd_date_created + '1 hour'::interval);
alter table property_definition alter column pd_last_updated type timestamp using pd_last_updated::timestamp;
update property_definition set pd_last_updated = (pd_last_updated + '1 hour'::interval);
alter table property_definition_group alter column pdg_date_created type timestamp using pdg_date_created::timestamp;
update property_definition_group set pdg_date_created = (pdg_date_created + '1 hour'::interval);
alter table property_definition_group alter column pdg_last_updated type timestamp using pdg_last_updated::timestamp;
update property_definition_group set pdg_last_updated = (pdg_last_updated + '1 hour'::interval);
alter table property_definition_group_binding alter column pbg_date_created type timestamp using pbg_date_created::timestamp;
update property_definition_group_binding set pbg_date_created = (pbg_date_created + '1 hour'::interval);
alter table property_definition_group_binding alter column pbg_last_updated type timestamp using pbg_last_updated::timestamp;
update property_definition_group_binding set pbg_last_updated = (pbg_last_updated + '1 hour'::interval);
""")
			}
			rollback {}
		}
	}
}





