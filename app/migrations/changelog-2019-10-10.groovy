databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1570696933657-1") {
		createTable(schemaName: "public", tableName: "platform_custom_property") {
			column(autoIncrement: "true", name: "id", type: "int8") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "platform_custPK")
			}

			column(name: "version", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "date_value", type: "timestamp")

			column(name: "dec_value", type: "numeric(19, 2)")

			column(name: "int_value", type: "int4")

			column(name: "note", type: "text")

			column(name: "owner_id", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "ref_value_id", type: "int8")

			column(name: "string_value", type: "text")

			column(name: "type_id", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "url_value", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570696933657-2") {
		addColumn(schemaName: "public", tableName: "api_source") {
			column(name: "as_editUrl", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570696933657-3") {
		addColumn(schemaName: "public", tableName: "global_record_source") {
			column(name: "grs_edit_uri", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570696933657-4") {
		createIndex(indexName: "pcp_owner_idx", schemaName: "public", tableName: "platform_custom_property") {
			column(name: "owner_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1570696933657-5") {
		dropColumn(columnName: "apikey", tableName: "user")
	}

	changeSet(author: "kloberd (generated)", id: "1570696933657-6") {
		dropColumn(columnName: "apisecret", tableName: "user")
	}

	changeSet(author: "kloberd (generated)", id: "1570696933657-7") {
		dropTable(tableName: "dataload_file_instance")
	}

	changeSet(author: "kloberd (generated)", id: "1570696933657-8") {
		dropTable(tableName: "dataload_file_type")
	}

	changeSet(author: "kloberd (generated)", id: "1570696933657-9") {
		addForeignKeyConstraint(baseColumnNames: "owner_id", baseTableName: "platform_custom_property", baseTableSchemaName: "public", constraintName: "FK41914B1728A77A17", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "plat_id", referencedTableName: "platform", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1570696933657-10") {
		addForeignKeyConstraint(baseColumnNames: "ref_value_id", baseTableName: "platform_custom_property", baseTableSchemaName: "public", constraintName: "FK41914B172992A286", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1570696933657-11") {
		addForeignKeyConstraint(baseColumnNames: "type_id", baseTableName: "platform_custom_property", baseTableSchemaName: "public", constraintName: "FK41914B17638A6383", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pd_id", referencedTableName: "property_definition", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (modified)", id: "1570696933657-12") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE api_source RENAME as_baseurl TO as_base_url")
				sql.execute("ALTER TABLE api_source RENAME as_datecreated TO as_date_created")
				sql.execute('ALTER TABLE api_source RENAME "as_editUrl" TO as_edit_url')
				sql.execute("ALTER TABLE api_source RENAME as_fixtoken TO as_fix_token")
				sql.execute("ALTER TABLE api_source RENAME as_lastupdated TO as_last_updated")
				sql.execute("ALTER TABLE api_source RENAME as_lastupdated_with_api TO as_last_updated_with_api")
				sql.execute("ALTER TABLE api_source RENAME as_variabletoken TO as_variable_token")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1570696933657-13") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE cost_item RENAME ci_subpkg_fk TO ci_sub_pkg_fk")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1570696933657-14") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE creator RENAME cre_datecreated TO cre_date_created")
				sql.execute("ALTER TABLE creator RENAME cre_lastupdated TO cre_last_updated")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1570696933657-15") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE creator_title RENAME ct_datecreated TO ct_date_created")
				sql.execute("ALTER TABLE creator_title RENAME ct_lastupdated TO ct_last_updated")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1570696933657-16") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE doc RENAME doc_mimetype TO doc_mime_type")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1570696933657-17") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE folder_item RENAME fi_datecreated TO fi_date_created")
				sql.execute("ALTER TABLE folder_item RENAME fi_lastupdated TO fi_last_updated")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1570696933657-18") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE reader_number RENAME num_create_date TO num_date_created")
				sql.execute("ALTER TABLE reader_number RENAME num_lastupdate_date TO num_last_updated")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1570696933657-19") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE system_message RENAME sm_datecreated TO sm_date_created")
				sql.execute("ALTER TABLE system_message RENAME sm_lastupdated TO sm_last_updated")
				sql.execute("ALTER TABLE system_message RENAME sm_shownow TO sm_show_now")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1570696933657-20") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE user_folder RENAME uf_datecreated TO uf_date_created")
				sql.execute("ALTER TABLE user_folder RENAME uf_lastupdated TO uf_last_updated")
			}
			rollback {}
		}
	}
}
