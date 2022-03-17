databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1566384977224-1") {
		createTable(schemaName: "public", tableName: "issue_entitlement_coverage") {
			column(autoIncrement: "true", name: "ic_id", type: "int8") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "issue_entitlePK")
			}

			column(name: "ic_version", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "ic_coverage_depth", type: "varchar(255)")

			column(name: "ic_coverage_note", type: "text")

			column(name: "ic_embargo", type: "varchar(255)")

			column(name: "ic_end_date", type: "timestamp")

			column(name: "ic_end_issue", type: "varchar(255)")

			column(name: "ic_end_volume", type: "varchar(255)")

			column(name: "ic_ie_fk", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "ic_start_date", type: "timestamp")

			column(name: "ic_start_issue", type: "varchar(255)")

			column(name: "ic_start_volume", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1566384977224-2") {
		createTable(schemaName: "public", tableName: "tippcoverage") {
			column(autoIncrement: "true", name: "tc_id", type: "int8") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "tippcoveragePK")
			}

			column(name: "tc_version", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "tc_coverage_depth", type: "varchar(255)")

			column(name: "tc_coverage_note", type: "text")

			column(name: "tc_embargo", type: "varchar(255)")

			column(name: "tc_end_date", type: "timestamp")

			column(name: "tc_end_issue", type: "varchar(255)")

			column(name: "tc_end_volume", type: "varchar(255)")

			column(name: "tc_start_date", type: "timestamp")

			column(name: "tc_start_issue", type: "varchar(255)")

			column(name: "tc_start_volume", type: "varchar(255)")

			column(name: "tc_tipp_fk", type: "int8") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1566384977224-3") {
		addColumn(schemaName: "public", tableName: "org") {
			column(name: "org_origin_edit_url", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1566384977224-4") {
		addColumn(schemaName: "public", tableName: "package") {
			column(name: "pkg_origin_edit_url", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1566384977224-5") {
		addColumn(schemaName: "public", tableName: "platform") {
			column(name: "plat_origin_edit_url", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1566384977224-6") {
		addColumn(schemaName: "public", tableName: "title_instance") {
			column(name: "ti_origin_edit_url", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1566384977224-7") {
		addColumn(schemaName: "public", tableName: "title_instance_package_platform") {
			column(name: "tipp_origin_edit_url", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (modified)", id: "1566384977224-8") {
		grailsChange {
			change {
				sql.execute(
						"insert into tippcoverage (tc_version,tc_start_date,tc_start_volume,tc_start_issue,tc_end_date,tc_end_volume,tc_end_issue,tc_coverage_depth,tc_coverage_note,tc_embargo,tc_tipp_fk) "
						+ " select tipp_version,tipp_start_date,tipp_start_volume,tipp_start_issue,tipp_end_date,tipp_end_volume,tipp_end_issue,tipp_coverage_depth,tipp_coverage_note,tipp_embargo,tipp_id "
						+ " from title_instance_package_platform"
				)

			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1566384977224-9") {
		grailsChange {
			change {
				sql.execute(
						"insert into issue_entitlement_coverage (ic_version,ic_start_date,ic_start_volume,ic_start_issue,ic_end_date,ic_end_volume,ic_end_issue,ic_coverage_depth,ic_coverage_note,ic_embargo,ic_ie_fk) "
						+ " select ie_version,ie_start_date,ie_start_volume,ie_start_issue,ie_end_date,ie_end_volume,ie_end_issue,ie_coverage_depth,ie_coverage_note,ie_embargo,ie_id "
						+ " from issue_entitlement"
				)
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1566384977224-10") {
		grailsChange {
			change {
				sql.execute("UPDATE org_settings SET os_key_enum = 'OAMONITOR_SERVER_ACCESS' WHERE os_key_enum = 'OA2020_SERVER_ACCESS'")
				sql.execute("UPDATE org_settings SET os_key_enum = 'NATSTAT_SERVER_ACCESS' WHERE os_key_enum = 'STATISTICS_SERVER_ACCESS'")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1566384977224-11") {
		dropForeignKeyConstraint(baseTableName: "title_instance_package_platform", baseTableSchemaName: "public", constraintName: "fke793fb8f31072c91")
	}

	changeSet(author: "kloberd (generated)", id: "1566384977224-12") {
		dropForeignKeyConstraint(baseTableName: "title_instance_package_platform", baseTableSchemaName: "public", constraintName: "fke793fb8f922ac05f")
	}

	changeSet(author: "kloberd (generated)", id: "1566384977224-15") {
		dropIndex(indexName: "ie_dates_idx", tableName: "issue_entitlement")
	}

	changeSet(author: "kloberd (generated)", id: "1566384977224-16") {
		dropIndex(indexName: "tipp_dates_idx", tableName: "title_instance_package_platform")
	}

	changeSet(author: "kloberd (generated)", id: "1566384977224-17") {
		createIndex(indexName: "ic_dates_idx", schemaName: "public", tableName: "issue_entitlement_coverage") {
			column(name: "ic_end_date")
			column(name: "ic_start_date")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1566384977224-18") {
		createIndex(indexName: "tc_dates_idx", schemaName: "public", tableName: "tippcoverage") {
			column(name: "tc_end_date")
			column(name: "tc_start_date")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1566384977224-19") {
		dropColumn(columnName: "ie_coverage_depth", tableName: "issue_entitlement")
	}

	changeSet(author: "kloberd (generated)", id: "1566384977224-20") {
		dropColumn(columnName: "ie_coverage_note", tableName: "issue_entitlement")
	}

	changeSet(author: "kloberd (generated)", id: "1566384977224-21") {
		dropColumn(columnName: "ie_embargo", tableName: "issue_entitlement")
	}

	changeSet(author: "kloberd (generated)", id: "1566384977224-22") {
		dropColumn(columnName: "ie_end_date", tableName: "issue_entitlement")
	}

	changeSet(author: "kloberd (generated)", id: "1566384977224-23") {
		dropColumn(columnName: "ie_end_issue", tableName: "issue_entitlement")
	}

	changeSet(author: "kloberd (generated)", id: "1566384977224-24") {
		dropColumn(columnName: "ie_end_volume", tableName: "issue_entitlement")
	}

	changeSet(author: "kloberd (generated)", id: "1566384977224-25") {
		dropColumn(columnName: "ie_start_date", tableName: "issue_entitlement")
	}

	changeSet(author: "kloberd (generated)", id: "1566384977224-26") {
		dropColumn(columnName: "ie_start_issue", tableName: "issue_entitlement")
	}

	changeSet(author: "kloberd (generated)", id: "1566384977224-27") {
		dropColumn(columnName: "ie_start_volume", tableName: "issue_entitlement")
	}

	changeSet(author: "kloberd (generated)", id: "1566384977224-28") {
		dropColumn(columnName: "master_tipp_id", tableName: "title_instance_package_platform")
	}

	changeSet(author: "kloberd (generated)", id: "1566384977224-29") {
		dropColumn(columnName: "tipp_coverage_depth", tableName: "title_instance_package_platform")
	}

	changeSet(author: "kloberd (generated)", id: "1566384977224-30") {
		dropColumn(columnName: "tipp_coverage_note", tableName: "title_instance_package_platform")
	}

	changeSet(author: "kloberd (generated)", id: "1566384977224-31") {
		dropColumn(columnName: "tipp_derived_from", tableName: "title_instance_package_platform")
	}

	changeSet(author: "kloberd (generated)", id: "1566384977224-32") {
		dropColumn(columnName: "tipp_embargo", tableName: "title_instance_package_platform")
	}

	changeSet(author: "kloberd (generated)", id: "1566384977224-33") {
		dropColumn(columnName: "tipp_end_date", tableName: "title_instance_package_platform")
	}

	changeSet(author: "kloberd (generated)", id: "1566384977224-34") {
		dropColumn(columnName: "tipp_end_issue", tableName: "title_instance_package_platform")
	}

	changeSet(author: "kloberd (generated)", id: "1566384977224-35") {
		dropColumn(columnName: "tipp_end_volume", tableName: "title_instance_package_platform")
	}

	changeSet(author: "kloberd (generated)", id: "1566384977224-36") {
		dropColumn(columnName: "tipp_start_date", tableName: "title_instance_package_platform")
	}

	changeSet(author: "kloberd (generated)", id: "1566384977224-37") {
		dropColumn(columnName: "tipp_start_issue", tableName: "title_instance_package_platform")
	}

	changeSet(author: "kloberd (generated)", id: "1566384977224-38") {
		dropColumn(columnName: "tipp_start_volume", tableName: "title_instance_package_platform")
	}

	changeSet(author: "kloberd (generated)", id: "1566384977224-13") {
		addForeignKeyConstraint(baseColumnNames: "ic_ie_fk", baseTableName: "issue_entitlement_coverage", baseTableSchemaName: "public", constraintName: "FK58936060E776F474", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ie_id", referencedTableName: "issue_entitlement", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1566384977224-14") {
		addForeignKeyConstraint(baseColumnNames: "tc_tipp_fk", baseTableName: "tippcoverage", baseTableSchemaName: "public", constraintName: "FK3766C41D59CFB348", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "tipp_id", referencedTableName: "title_instance_package_platform", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}
}
