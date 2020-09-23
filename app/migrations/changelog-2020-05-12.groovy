databaseChangeLog = {

	changeSet(author: "klober (generated)", id: "1589272928554-1") {
		dropForeignKeyConstraint(baseTableName: "license", baseTableSchemaName: "public", constraintName: "fk9f08441f5a55c6c")
	}

	changeSet(author: "klober (generated)", id: "1589272928554-2") {
		dropForeignKeyConstraint(baseTableName: "onixpl_license", baseTableSchemaName: "public", constraintName: "fk620852cc4d1ae0db")
	}

	changeSet(author: "klober (generated)", id: "1589272928554-3") {
		dropColumn(columnName: "lic_opl_fk", tableName: "license")
	}

	changeSet(author: "klober (generated)", id: "1589272928554-4") {
		dropTable(tableName: "onixpl_license")
	}

	changeSet(author: "klober (generated)", id: "1589272928554-5") {
		addColumn(schemaName: "public", tableName: "platform") {
			column(name: "plat_last_updated_cascading", type: "timestamp")
		}
	}

	changeSet(author: "klober (generated)", id: "1589272928554-6") {
		addColumn(schemaName: "public", tableName: "title_instance") {
			column(name: "ti_last_updated_cascading", type: "timestamp")
		}
	}

	changeSet(author: "klober (generated)", id: "1589272928554-7") {
		addColumn(schemaName: "public", tableName: "license") {
			column(name: "lic_last_updated_cascading", type: "timestamp")
		}
	}

	changeSet(author: "klober (generated)", id: "1589272928554-8") {
		addColumn(schemaName: "public", tableName: "org") {
			column(name: "org_last_updated_cascading", type: "timestamp")
		}
	}

	changeSet(author: "klober (generated)", id: "1589272928554-9") {
		addColumn(schemaName: "public", tableName: "package") {
			column(name: "pkg_last_updated_cascading", type: "timestamp")
		}
	}

	changeSet(author: "klober (generated)", id: "1589272928554-10") {
		addColumn(schemaName: "public", tableName: "person") {
			column(name: "prs_last_updated_cascading", type: "timestamp")
		}
	}

	changeSet(author: "klober (modified)", id: "1589272928554-11") {
		grailsChange {
			change {
				sql.execute("alter table license alter column lic_last_updated_cascading type timestamp using lic_last_updated_cascading::timestamp")
				sql.execute("alter table org alter column org_last_updated_cascading type timestamp using org_last_updated_cascading::timestamp")
				sql.execute("alter table package alter column pkg_last_updated_cascading type timestamp using pkg_last_updated_cascading::timestamp")
				sql.execute("alter table title_instance alter column ti_last_updated_cascading type timestamp using ti_last_updated_cascading::timestamp")
				sql.execute("alter table person alter column prs_last_updated_cascading type timestamp using prs_last_updated_cascading::timestamp")
				sql.execute("alter table platform alter column plat_last_updated_cascading type timestamp using plat_last_updated_cascading::timestamp")
			}
		}
	}

	// step 2

	changeSet(author: "klober (generated)", id: "1589272928554-12") {
		addColumn(schemaName: "public", tableName: "license_custom_property") {
			column(name: "last_updated_cascading", type: "timestamp")
		}
	}

	changeSet(author: "klober (generated)", id: "1589272928554-13") {
		addColumn(schemaName: "public", tableName: "license_private_property") {
			column(name: "last_updated_cascading", type: "timestamp")
		}
	}

	changeSet(author: "klober (generated)", id: "1589272928554-14") {
		addColumn(schemaName: "public", tableName: "org_custom_property") {
			column(name: "last_updated_cascading", type: "timestamp")
		}
	}

	changeSet(author: "klober (generated)", id: "1589272928554-15") {
		addColumn(schemaName: "public", tableName: "org_private_property") {
			column(name: "last_updated_cascading", type: "timestamp")
		}
	}

	changeSet(author: "klober (generated)", id: "1589272928554-16") {
		addColumn(schemaName: "public", tableName: "person_private_property") {
			column(name: "last_updated_cascading", type: "timestamp")
		}
	}

	changeSet(author: "klober (generated)", id: "1589272928554-17") {
		addColumn(schemaName: "public", tableName: "platform_custom_property") {
			column(name: "last_updated_cascading", type: "timestamp")
		}
	}

	changeSet(author: "klober (generated)", id: "1589272928554-18") {
		addColumn(schemaName: "public", tableName: "subscription_custom_property") {
			column(name: "last_updated_cascading", type: "timestamp")
		}
	}

	changeSet(author: "klober (generated)", id: "1589272928554-19") {
		addColumn(schemaName: "public", tableName: "subscription_private_property") {
			column(name: "last_updated_cascading", type: "timestamp")
		}
	}

	changeSet(author: "klober (generated)", id: "1589272928554-20") {
		addColumn(schemaName: "public", tableName: "survey_result") {
			column(name: "last_updated_cascading", type: "timestamp")
		}
	}

	changeSet(author: "klober (modified)", id: "1589272928554-21") {
		grailsChange {
			change {
				sql.execute("alter table license_custom_property alter column last_updated_cascading type timestamp using last_updated_cascading::timestamp")
				sql.execute("alter table license_private_property alter column last_updated_cascading type timestamp using last_updated_cascading::timestamp")
				sql.execute("alter table org_custom_property alter column last_updated_cascading type timestamp using last_updated_cascading::timestamp")
				sql.execute("alter table org_private_property alter column last_updated_cascading type timestamp using last_updated_cascading::timestamp")
				sql.execute("alter table platform_custom_property alter column last_updated_cascading type timestamp using last_updated_cascading::timestamp")
				sql.execute("alter table subscription_custom_property alter column last_updated_cascading type timestamp using last_updated_cascading::timestamp")
				sql.execute("alter table survey_result alter column last_updated_cascading type timestamp using last_updated_cascading::timestamp")
				sql.execute("alter table person_private_property alter column last_updated_cascading type timestamp using last_updated_cascading::timestamp")
				sql.execute("alter table subscription_private_property alter column last_updated_cascading type timestamp using last_updated_cascading::timestamp")
			}
		}
	}

	changeSet(author: "klober (generated)", id: "1589272928554-22") {
		addColumn(schemaName: "public", tableName: "platform_custom_property") {
			column(name: "pcp_date_created", type: "timestamp")
		}
	}

	changeSet(author: "klober (generated)", id: "1589272928554-23") {
		addColumn(schemaName: "public", tableName: "platform_custom_property") {
			column(name: "pcp_last_updated", type: "timestamp")
		}
	}

	changeSet(author: "klober (modified)", id: "1589272928554-24") {
		grailsChange {
			change {
				sql.execute("alter table platform_custom_property alter column pcp_date_created type timestamp using pcp_date_created::timestamp")
				sql.execute("alter table platform_custom_property alter column pcp_last_updated type timestamp using pcp_last_updated::timestamp")
			}
		}
	}
}
