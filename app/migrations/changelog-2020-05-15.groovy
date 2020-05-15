databaseChangeLog = {

	changeSet(author: "Moe (generated)", id: "1589541015267-1") {
		addColumn(schemaName: "public", tableName: "survey_config") {
			column(name: "surconf_create_title_groups", type: "bool") {
				//constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "Moe (modified)", id: "1589541015267-2") {
		grailsChange {
			change {
				sql.execute("UPDATE survey_config SET surconf_create_title_groups = false WHERE surconf_create_title_groups is null;")
			}
			rollback {}
		}
	}

	changeSet(author: "Moe (modified)", id: "1589541015267-3") {
		grailsChange {
			change {
				sql.execute("UPDATE org_settings SET os_role_fk = (select id from role where authority = 'ORG_CONSORTIUM') WHERE os_key_enum = 'CUSTOMER_TYPE' AND os_role_fk = (select id from role where authority = 'ORG_CONSORTIUM_SURVEY');")
			}
			rollback {}
		}
	}

	changeSet(author: "Moe (modified)", id: "1589541015267-4") {
		grailsChange {
			change {
				sql.execute("DELETE FROM perm_grant WHERE role_id = (select id from role where authority = 'ORG_CONSORTIUM_SURVEY');")
				sql.execute("DELETE FROM role WHERE authority = 'ORG_CONSORTIUM_SURVEY';")
			}
			rollback {}
		}
	}
}
