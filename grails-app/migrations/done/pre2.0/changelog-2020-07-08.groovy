databaseChangeLog = {

	changeSet(author: "galffy (generated)", id: "1594186314951-1") {
		dropForeignKeyConstraint(baseTableName: "person_private_property", baseTableSchemaName: "public", constraintName: "fk99dfa8bb56aba4d2")
	}

	changeSet(author: "galffy (generated)", id: "1594186314951-2") {
		dropForeignKeyConstraint(baseTableName: "person_private_property", baseTableSchemaName: "public", constraintName: "fk99dfa8bbd23a4c5e")
	}

	changeSet(author: "galffy (generated)", id: "1594186314951-3") {
		dropForeignKeyConstraint(baseTableName: "person_private_property", baseTableSchemaName: "public", constraintName: "fk99dfa8bb2992a286")
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-4") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE person_private_property RENAME ppp_id  TO pp_id;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-5") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE person_private_property RENAME ppp_version TO pp_version;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-6") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE person_private_property RENAME date_value  TO pp_date_value;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-7") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE person_private_property RENAME dec_value  TO pp_dec_value;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-8") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE person_private_property RENAME int_value  TO pp_int_value;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-9") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE person_private_property RENAME note  TO pp_note;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-10") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE person_private_property RENAME ppp_owner_fk  TO pp_owner_fk;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-11") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE person_private_property RENAME ref_value_id  TO pp_ref_value_rv_fk;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-12") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE person_private_property RENAME string_value  TO pp_string_value;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-13") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE person_private_property RENAME ppp_type_fk  TO pp_type_fk;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-14") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE person_private_property RENAME url_value  TO pp_url_value;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-15") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE person_private_property RENAME ppp_date_created  TO pp_date_created;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-16") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE person_private_property RENAME ppp_last_updated  TO pp_last_updated;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-17") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE person_private_property RENAME last_updated_cascading  TO pp_last_updated_cascading;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-18") {
		addColumn(schemaName: "public", tableName: "person_private_property") {
			column(name: "pp_is_public", type: "bool")
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-19") {
		grailsChange {
			change {
				sql.execute("update person_private_property set pp_is_public = false where pp_is_public is null;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-20") {
		addNotNullConstraint(columnDataType: "bool", columnName: "pp_is_public", tableName: "person_private_property")
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-21") {
		addColumn(schemaName: "public", tableName: "person_private_property") {
			column(name: "pp_tenant_fk", type: "int8")
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-22") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE person_private_property RENAME TO person_property;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (generated)", id: "1594186314951-23") {
		createIndex(indexName: "pp_owner_idx", schemaName: "public", tableName: "person_property") {
			column(name: "pp_owner_fk")
		}
	}

	changeSet(author: "galffy (generated)", id: "1594186314951-24") {
		createIndex(indexName: "pp_tenant_fk", schemaName: "public", tableName: "person_property") {
			column(name: "pp_tenant_fk")
		}
	}

	changeSet(author: "galffy (generated)", id: "1594186314951-25") {
		createIndex(indexName: "pp_type_idx", schemaName: "public", tableName: "person_property") {
			column(name: "pp_type_fk")
		}
	}

	changeSet(author: "galffy (generated)", id: "1594186314951-26") {
		addForeignKeyConstraint(baseColumnNames: "pp_owner_fk", baseTableName: "person_property", baseTableSchemaName: "public", constraintName: "FK6890637FF8F34B42", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "prs_id", referencedTableName: "person", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "galffy (generated)", id: "1594186314951-27") {
		addForeignKeyConstraint(baseColumnNames: "pp_ref_value_rv_fk", baseTableName: "person_property", baseTableSchemaName: "public", constraintName: "FK6890637FB9B3B418", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "galffy (generated)", id: "1594186314951-28") {
		addForeignKeyConstraint(baseColumnNames: "pp_tenant_fk", baseTableName: "person_property", baseTableSchemaName: "public", constraintName: "FK6890637F188C9A42", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "galffy (generated)", id: "1594186314951-29") {
		addForeignKeyConstraint(baseColumnNames: "pp_type_fk", baseTableName: "person_property", baseTableSchemaName: "public", constraintName: "FK6890637FAE2C17EE", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pd_id", referencedTableName: "property_definition", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "galffy (generated)", id: "1594186314951-30") {
		dropForeignKeyConstraint(baseTableName: "platform_custom_property", baseTableSchemaName: "public", constraintName: "fk41914b1728a77a17")
	}

	changeSet(author: "galffy (generated)", id: "1594186314951-31") {
		dropForeignKeyConstraint(baseTableName: "platform_custom_property", baseTableSchemaName: "public", constraintName: "fk41914b172992a286")
	}

	changeSet(author: "galffy (generated)", id: "1594186314951-32") {
		dropForeignKeyConstraint(baseTableName: "platform_custom_property", baseTableSchemaName: "public", constraintName: "fk41914b17638a6383")
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-33") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE platform_custom_property RENAME id  TO plp_id;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-34") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE platform_custom_property RENAME version  TO plp_version;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-35") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE platform_custom_property RENAME date_value  TO plp_date_value;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-36") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE platform_custom_property RENAME dec_value  TO plp_dec_value;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-37") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE platform_custom_property RENAME int_value  TO plp_int_value;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-38") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE platform_custom_property RENAME note  TO plp_note;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-39") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE platform_custom_property RENAME owner_id  TO plp_owner_fk;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-40") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE platform_custom_property RENAME ref_value_id  TO plp_ref_value_rv_fk;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-41") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE platform_custom_property RENAME string_value  TO plp_string_value;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-42") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE platform_custom_property RENAME type_id  TO plp_type_fk;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-43") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE platform_custom_property RENAME url_value  TO plp_url_value;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-44") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE platform_custom_property RENAME pcp_date_created  TO plp_date_created;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-45") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE platform_custom_property RENAME pcp_last_updated  TO plp_last_updated;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-46") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE platform_custom_property RENAME last_updated_cascading  TO plp_last_updated_cascading;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-47") {
		addColumn(schemaName: "public", tableName: "platform_custom_property") {
			column(name: "plp_is_public", type: "text")
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-48") {
		grailsChange {
			change {
				sql.execute("update platform_custom_property set plp_is_public = true where plp_is_public is null;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-49") {
		addNotNullConstraint(columnDataType: "bool", columnName: "plp_is_public", tableName: "platform_custom_property")
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-50") {
		addColumn(schemaName: "public", tableName: "platform_custom_property") {
			column(name: "plp_tenant_fk", type: "int8")
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-51") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE platform_custom_property RENAME TO platform_property;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (generated)", id: "1594186314951-52") {
		createIndex(indexName: "plp_owner_idx", schemaName: "public", tableName: "platform_property") {
			column(name: "plp_owner_fk")
		}
	}

	changeSet(author: "galffy (generated)", id: "1594186314951-53") {
		createIndex(indexName: "plp_tenant_idx", schemaName: "public", tableName: "platform_property") {
			column(name: "plp_tenant_fk")
		}
	}

	changeSet(author: "galffy (generated)", id: "1594186314951-54") {
		createIndex(indexName: "plp_type_idx", schemaName: "public", tableName: "platform_property") {
			column(name: "plp_type_fk")
		}
	}

	changeSet(author: "galffy (generated)", id: "1594186314951-55") {
		addForeignKeyConstraint(baseColumnNames: "plp_owner_fk", baseTableName: "platform_property", baseTableSchemaName: "public", constraintName: "FK520878213C61EBAC", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "plat_id", referencedTableName: "platform", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "galffy (generated)", id: "1594186314951-56") {
		addForeignKeyConstraint(baseColumnNames: "plp_ref_value_rv_fk", baseTableName: "platform_property", baseTableSchemaName: "public", constraintName: "FK52087821A1F6F00C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "galffy (generated)", id: "1594186314951-57") {
		addForeignKeyConstraint(baseColumnNames: "plp_tenant_fk", baseTableName: "platform_property", baseTableSchemaName: "public", constraintName: "FK520878215318DD36", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "galffy (generated)", id: "1594186314951-58") {
		addForeignKeyConstraint(baseColumnNames: "plp_type_fk", baseTableName: "platform_property", baseTableSchemaName: "public", constraintName: "FK5208782111487E2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pd_id", referencedTableName: "property_definition", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-59") {
		addColumn(schemaName: "public", tableName: "survey_result") {
			column(name: "surre_tenant_fk", type: "int8")
		}
	}

	changeSet(author: "galffy (generated)", id: "1594186314951-60") {
		addForeignKeyConstraint(baseColumnNames: "surre_tenant_fk", baseTableName: "survey_result", baseTableSchemaName: "public", constraintName: "FK92EA04A2EF851217", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-61") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE survey_result RENAME date_value  TO surre_date_value;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-62") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE survey_result RENAME dec_value  TO surre_dec_value;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-63") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE survey_result RENAME int_value  TO surre_int_value;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-64") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE survey_result RENAME note  TO surre_note;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-65") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE survey_result RENAME ref_value_id  TO surre_ref_value_rv_fk;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-66") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE survey_result RENAME string_value  TO surre_string_value;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-67") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE survey_result RENAME url_value  TO surre_url_value;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-68") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE survey_result RENAME last_updated_cascading  TO surre_last_updated_cascading;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-69") {
		addColumn(schemaName: "public", tableName: "survey_result") {
			column(name: "surre_is_public", type: "bool")
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-70") {
		grailsChange {
			change {
				sql.execute("update survey_result set surre_is_public = true where surre_is_public is null;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594186314951-71") {
		addNotNullConstraint(columnDataType: "bool", columnName: "surre_is_public", tableName: "survey_result")
	}

	changeSet(author: "Moe (generated)", id: "1594186314951-72") {
		addColumn(schemaName: "public", tableName: "subscription") {
			column(name: "sub_comment", type: "text")
		}
	}
}
