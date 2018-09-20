databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1531983068809-1") {
		createTable(tableName: "access_point_data") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "access_point_PK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "apd_data", type: "varchar(255)")

			column(name: "apd_datatype", type: "varchar(255)")

			column(name: "apd_guid", type: "varchar(255)")

			column(name: "apd_org_access_point_fk", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-2") {
		createTable(tableName: "org_access_point") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "org_access_poPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "oar_access_method_rv_fk", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "oar_guid", type: "varchar(255)")

			column(name: "last_updated", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "oar_name", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "oar_org_fk", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-3") {
		createTable(tableName: "org_access_point_link") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "org_access_poPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "active", type: "bit") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "globaluid", type: "varchar(255)")

			column(name: "last_updated", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "license_id", type: "bigint")

			column(name: "oap_id", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "platform_id", type: "bigint")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-4") {
		createTable(tableName: "platform_access_method") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "platform_accePK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "pam_access_method_rv_fk", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "plat_guid", type: "varchar(255)")

			column(name: "last_updated", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "pam_platf_fk", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "pam_valid_from", type: "datetime")

			column(name: "pam_valid_to", type: "datetime")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-5") {
		addColumn(tableName: "budget_code") {
			column(name: "bc_description", type: "varchar(2048)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-6") {
		addColumn(tableName: "elasticsearch_source") {
			column(name: "ess_url", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-7") {
		addColumn(tableName: "license") {
			column(name: "lic_is_slaved", type: "bigint")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-8") {
		addColumn(tableName: "license") {
			column(name: "lic_parent_lic_fk", type: "bigint")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-9") {
		modifyDataType(columnName: "string_value", newDataType: "longtext", tableName: "license_custom_property")
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-10") {
		modifyDataType(columnName: "string_value", newDataType: "longtext", tableName: "license_private_property")
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-11") {
		modifyDataType(columnName: "string_value", newDataType: "longtext", tableName: "org_custom_property")
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-12") {
		modifyDataType(columnName: "note", newDataType: "longtext", tableName: "org_private_property")
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-13") {
		modifyDataType(columnName: "string_value", newDataType: "longtext", tableName: "org_private_property")
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-14") {
		modifyDataType(columnName: "note", newDataType: "longtext", tableName: "person_private_property")
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-15") {
		modifyDataType(columnName: "string_value", newDataType: "longtext", tableName: "person_private_property")
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-16") {
		modifyDataType(columnName: "string_value", newDataType: "longtext", tableName: "subscription_custom_property")
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-17") {
		modifyDataType(columnName: "note", newDataType: "longtext", tableName: "subscription_private_property")
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-18") {
		modifyDataType(columnName: "string_value", newDataType: "longtext", tableName: "subscription_private_property")
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-19") {
		modifyDataType(columnName: "string_value", newDataType: "longtext", tableName: "system_admin_custom_property")
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-31") {
		createIndex(indexName: "FKCE1A66B4A6D6B88F", tableName: "access_point_data") {
			column(name: "apd_org_access_point_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-32") {
		createIndex(indexName: "apd_guid_uniq_1531983068033", tableName: "access_point_data", unique: "true") {
			column(name: "apd_guid")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-33") {
		createIndex(indexName: "unique_bc_value", tableName: "budget_code", unique: "true") {
			column(name: "bc_owner_fk")

			column(name: "bc_value")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-34") {
		createIndex(indexName: "FK9F0844164F7F97E", tableName: "license") {
			column(name: "lic_parent_lic_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-35") {
		createIndex(indexName: "FK9F08441E07D095A", tableName: "license") {
			column(name: "lic_is_slaved")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-36") {
		createIndex(indexName: "FKC105E0701F50FD86", tableName: "org_access_point") {
			column(name: "oar_org_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-37") {
		createIndex(indexName: "FKC105E07075210A8F", tableName: "org_access_point") {
			column(name: "oar_access_method_rv_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-38") {
		createIndex(indexName: "oar_guid_uniq_1531983068087", tableName: "org_access_point", unique: "true") {
			column(name: "oar_guid")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-39") {
		createIndex(indexName: "unique_oar_name", tableName: "org_access_point", unique: "true") {
			column(name: "oar_org_fk")

			column(name: "oar_name")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-40") {
		createIndex(indexName: "FK1C324E6910252C57", tableName: "org_access_point_link") {
			column(name: "platform_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-41") {
		createIndex(indexName: "FK1C324E6946CE42E1", tableName: "org_access_point_link") {
			column(name: "oap_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-42") {
		createIndex(indexName: "FK1C324E69D9ED26FD", tableName: "org_access_point_link") {
			column(name: "license_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-43") {
		createIndex(indexName: "globaluid_uniq_1531983068088", tableName: "org_access_point_link", unique: "true") {
			column(name: "globaluid")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-44") {
		createIndex(indexName: "FKE7E3F55064D7720", tableName: "platform_access_method") {
			column(name: "pam_platf_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-45") {
		createIndex(indexName: "FKE7E3F5508055D84B", tableName: "platform_access_method") {
			column(name: "pam_access_method_rv_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-46") {
		createIndex(indexName: "plat_guid_uniq_1531983068096", tableName: "platform_access_method", unique: "true") {
			column(name: "plat_guid")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-20") {
		addForeignKeyConstraint(baseColumnNames: "apd_org_access_point_fk", baseTableName: "access_point_data", constraintName: "FKCE1A66B4A6D6B88F", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "org_access_point", referencesUniqueColumn: "false")
	}

	//changeSet(author: "kloberd (generated)", id: "1531983068809-21") {
	//	addForeignKeyConstraint(baseColumnNames: "grt_owner_fk", baseTableName: "global_record_tracker", constraintName: "FK808F5966F6287F86", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "gri_id", referencedTableName: "global_record_info", referencesUniqueColumn: "false")
	//}

	changeSet(author: "kloberd (generated)", id: "1531983068809-22") {
		addForeignKeyConstraint(baseColumnNames: "lic_is_slaved", baseTableName: "license", constraintName: "FK9F08441E07D095A", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-23") {
		addForeignKeyConstraint(baseColumnNames: "lic_parent_lic_fk", baseTableName: "license", constraintName: "FK9F0844164F7F97E", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "lic_id", referencedTableName: "license", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-24") {
		addForeignKeyConstraint(baseColumnNames: "oar_access_method_rv_fk", baseTableName: "org_access_point", constraintName: "FKC105E07075210A8F", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-25") {
		addForeignKeyConstraint(baseColumnNames: "oar_org_fk", baseTableName: "org_access_point", constraintName: "FKC105E0701F50FD86", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-26") {
		addForeignKeyConstraint(baseColumnNames: "license_id", baseTableName: "org_access_point_link", constraintName: "FK1C324E69D9ED26FD", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "lic_id", referencedTableName: "license", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-27") {
		addForeignKeyConstraint(baseColumnNames: "oap_id", baseTableName: "org_access_point_link", constraintName: "FK1C324E6946CE42E1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "org_access_point", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-28") {
		addForeignKeyConstraint(baseColumnNames: "platform_id", baseTableName: "org_access_point_link", constraintName: "FK1C324E6910252C57", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "plat_id", referencedTableName: "platform", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-29") {
		addForeignKeyConstraint(baseColumnNames: "pam_access_method_rv_fk", baseTableName: "platform_access_method", constraintName: "FKE7E3F5508055D84B", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1531983068809-30") {
		addForeignKeyConstraint(baseColumnNames: "pam_platf_fk", baseTableName: "platform_access_method", constraintName: "FKE7E3F55064D7720", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "plat_id", referencedTableName: "platform", referencesUniqueColumn: "false")
	}
}
