databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1524742653846-1") {
		createTable(tableName: "creator") {
			column(autoIncrement: "true", name: "cre_id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "creatorPK")
			}

			column(name: "cre_version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "cre_dateCreated", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "cre_firstname", type: "varchar(255)")

			column(name: "cre_guid", type: "varchar(255)")

			column(name: "cre_gnd_id_fk", type: "bigint")

			column(name: "cre_lastUpdated", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "cre_lastname", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "cre_middlename", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1524742653846-2") {
		createTable(tableName: "creator_title") {
			column(autoIncrement: "true", name: "ct_id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "creator_titlePK")
			}

			column(name: "ct_version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "ct_creator_fk", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "ct_dateCreated", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "ct_lastUpdated", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "ct_role_rv_fk", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "ct_title_fk", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1524742653846-3") {
		addColumn(tableName: "address") {
			column(name: "adr_addition_first", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1524742653846-4") {
		addColumn(tableName: "address") {
			column(name: "adr_addition_second", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1524742653846-5") {
		addColumn(tableName: "address") {
			column(name: "adr_name", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1524742653846-6") {
		addColumn(tableName: "address") {
			column(name: "adr_pob_city", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1524742653846-7") {
		addColumn(tableName: "address") {
			column(name: "adr_pob_zipcode", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1524742653846-8") {
		addColumn(tableName: "cost_item") {
			column(name: "ci_cost_title", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1524742653846-9") {
		addColumn(tableName: "cost_item") {
			column(name: "ci_guid", type: "varchar(255)") {
				constraints(unique: "true")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1524742653846-10") {
		addColumn(tableName: "org") {
			column(name: "org_url_gov", type: "varchar(512)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1524742653846-11") {
		addColumn(tableName: "title_instance") {
			column(name: "bk_dateFirstInPrint", type: "datetime")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1524742653846-12") {
		addColumn(tableName: "title_instance") {
			column(name: "bk_dateFirstOnline", type: "datetime")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1524742653846-13") {
		addColumn(tableName: "title_instance") {
			column(name: "bk_summaryOfContent", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1524742653846-14") {
		addColumn(tableName: "title_instance") {
			column(name: "bk_volume", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1524742653846-15") {
		addColumn(tableName: "title_instance") {
			column(name: "class", type: "varchar(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1524742653846-16") {
		dropNotNullConstraint(columnDataType: "varchar(255)", columnName: "prs_first_name", tableName: "person")
	}

	changeSet(author: "kloberd (generated)", id: "1524742653846-22") {
		createIndex(indexName: "ci_guid_uniq_1524742653158", tableName: "cost_item", unique: "true") {
			column(name: "ci_guid")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1524742653846-23") {
		createIndex(indexName: "FK3D4E802C8B454035", tableName: "creator") {
			column(name: "cre_gnd_id_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1524742653846-24") {
		createIndex(indexName: "cre_guid_uniq_1524742653159", tableName: "creator", unique: "true") {
			column(name: "cre_guid")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1524742653846-25") {
		createIndex(indexName: "FK68B134855B4619CA", tableName: "creator_title") {
			column(name: "ct_title_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1524742653846-26") {
		createIndex(indexName: "FK68B134855E791DD6", tableName: "creator_title") {
			column(name: "ct_role_rv_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1524742653846-27") {
		createIndex(indexName: "FK68B13485AD5AE355", tableName: "creator_title") {
			column(name: "ct_creator_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1524742653846-17") {
		addForeignKeyConstraint(baseColumnNames: "cre_gnd_id_fk", baseTableName: "creator", constraintName: "FK3D4E802C8B454035", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "io_id", referencedTableName: "identifier_occurrence", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1524742653846-18") {
		addForeignKeyConstraint(baseColumnNames: "ct_creator_fk", baseTableName: "creator_title", constraintName: "FK68B13485AD5AE355", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "cre_id", referencedTableName: "creator", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1524742653846-19") {
		addForeignKeyConstraint(baseColumnNames: "ct_role_rv_fk", baseTableName: "creator_title", constraintName: "FK68B134855E791DD6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1524742653846-20") {
		addForeignKeyConstraint(baseColumnNames: "ct_title_fk", baseTableName: "creator_title", constraintName: "FK68B134855B4619CA", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ti_id", referencedTableName: "title_instance", referencesUniqueColumn: "false")
	}

	//changeSet(author: "kloberd (generated)", id: "1524742653846-21") {
	//	addForeignKeyConstraint(baseColumnNames: "grt_owner_fk", baseTableName: "global_record_tracker", constraintName: "FK808F5966F6287F86", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "gri_id", referencedTableName: "global_record_info", referencesUniqueColumn: "false")
	//}
}
