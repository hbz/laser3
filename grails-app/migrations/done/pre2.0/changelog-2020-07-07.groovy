databaseChangeLog = {

	changeSet(author: "galffy (generated)", id: "1594122495562-1") {
		dropForeignKeyConstraint(baseTableName: "org_custom_property", baseTableSchemaName: "public", constraintName: "fkd7848f88c115df6e")
	}

	changeSet(author: "galffy (generated)", id: "1594122495562-2") {
		dropForeignKeyConstraint(baseTableName: "org_custom_property", baseTableSchemaName: "public", constraintName: "fkd7848f882992a286")
	}

	changeSet(author: "galffy (generated)", id: "1594122495562-3") {
		dropForeignKeyConstraint(baseTableName: "org_custom_property", baseTableSchemaName: "public", constraintName: "fkd7848f88638a6383")
	}

	changeSet(author: "galffy (generated)", id: "1594122495562-4") {
		dropForeignKeyConstraint(baseTableName: "org_private_property", baseTableSchemaName: "public", constraintName: "fkdfc7450c20b176a8")
	}

	changeSet(author: "galffy (generated)", id: "1594122495562-5") {
		dropForeignKeyConstraint(baseTableName: "org_private_property", baseTableSchemaName: "public", constraintName: "fkdfc7450c3d55999d")
	}

	changeSet(author: "galffy (generated)", id: "1594122495562-6") {
		dropForeignKeyConstraint(baseTableName: "org_private_property", baseTableSchemaName: "public", constraintName: "fkdfc7450c2992a286")
	}

	changeSet(author: "galffy (modified)", id: "1594122495562-7") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE org_custom_property RENAME id  TO op_id;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594122495562-8") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE org_custom_property RENAME version  TO op_version;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594122495562-9") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE org_custom_property RENAME date_value  TO op_date_value;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594122495562-10") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE org_custom_property RENAME dec_value  TO op_dec_value;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594122495562-11") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE org_custom_property RENAME int_value  TO op_int_value;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594122495562-12") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE org_custom_property RENAME note  TO op_note;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594122495562-13") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE org_custom_property RENAME owner_id  TO op_owner_fk;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594122495562-14") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE org_custom_property RENAME ref_value_id  TO op_ref_value_rv_fk;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594122495562-15") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE org_custom_property RENAME string_value  TO op_string_value;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594122495562-16") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE org_custom_property RENAME type_id  TO op_type_fk;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594122495562-17") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE org_custom_property RENAME url_value  TO op_url_value;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594122495562-18") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE org_custom_property RENAME ocp_date_created  TO op_date_created;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594122495562-19") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE org_custom_property RENAME ocp_last_updated  TO op_last_updated;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594122495562-20") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE org_custom_property RENAME last_updated_cascading  TO op_last_updated_cascading;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594122495562-21") {
		addColumn(schemaName: "public", tableName: "org_custom_property") {
			column(name: "op_is_public", type: "bool")
		}
	}

	changeSet(author: "galffy (modified)", id: "1594122495562-22") {
		grailsChange {
			change {
				sql.execute("update org_custom_property set op_is_public = false where op_is_public is null;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594122495562-23") {
		addNotNullConstraint(columnDataType: "bool", columnName: "op_is_public", tableName: "org_custom_property")
	}

	changeSet(author: "galffy (modified)", id: "1594122495562-24") {
		addColumn(schemaName: "public", tableName: "org_custom_property") {
			column(name: "op_tenant_fk", type: "int8")
		}
	}

	changeSet(author: "galffy (modified)", id: "1594122495562-25") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE org_custom_property RENAME TO org_property;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (generated)", id: "1594122495562-26") {
		createIndex(indexName: "op_tenant_idx", schemaName: "public", tableName: "org_property") {
			column(name: "op_tenant_fk")
		}
	}

	changeSet(author: "galffy (generated)", id: "1594122495562-27") {
		createIndex(indexName: "op_type_idx", schemaName: "public", tableName: "org_property") {
			column(name: "op_type_fk")
		}
	}

	changeSet(author: "galffy (generated)", id: "1594122495562-28") {
		createIndex(indexName: "op_owner_idx", schemaName: "public", tableName: "org_property") {
			column(name: "op_owner_fk")
		}
	}

	changeSet(author: "galffy (generated)", id: "1594122495562-29") {
		addForeignKeyConstraint(baseColumnNames: "op_owner_fk", baseTableName: "org_property", baseTableSchemaName: "public", constraintName: "FK6B42A6B7CCF35F8D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "galffy (generated)", id: "1594122495562-30") {
		addForeignKeyConstraint(baseColumnNames: "op_ref_value_rv_fk", baseTableName: "org_property", baseTableSchemaName: "public", constraintName: "FK6B42A6B7DACAFCD2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "galffy (generated)", id: "1594122495562-31") {
		addForeignKeyConstraint(baseColumnNames: "op_tenant_fk", baseTableName: "org_property", baseTableSchemaName: "public", constraintName: "FK6B42A6B72FA18A5F", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "galffy (generated)", id: "1594122495562-32") {
		addForeignKeyConstraint(baseColumnNames: "op_type_fk", baseTableName: "org_property", baseTableSchemaName: "public", constraintName: "FK6B42A6B7638A6383", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pd_id", referencedTableName: "property_definition", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "galffy (modified)", id: "1594122495562-33") {
		grailsChange {
			change {
				sql.execute("insert into org_property (op_version, op_date_value, op_dec_value, op_int_value, op_note, op_owner_fk, op_ref_value_rv_fk, op_string_value, op_type_fk, op_url_value, op_tenant_fk, op_is_public, op_date_created, op_last_updated, op_last_updated_cascading) " +
						"select opp.opp_version, opp.date_value, opp.dec_value, opp.int_value, opp.note, opp.opp_owner_fk, opp.ref_value_id, opp.string_value, opp.opp_type_fk, opp.url_value, pd.pd_tenant_fk, false, opp.opp_date_created, opp.opp_last_updated, opp.last_updated_cascading from org_private_property as opp join property_definition pd on opp.opp_type_fk = pd.pd_id;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (generated)", id: "1594122495562-34") {
		addForeignKeyConstraint(baseColumnNames: "op_owner_fk", baseTableName: "org_property", baseTableSchemaName: "public", constraintName: "FKB79DE0D035C60FB6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "galffy (generated)", id: "1594122495562-35") {
		addForeignKeyConstraint(baseColumnNames: "op_ref_value_rv_fk", baseTableName: "org_property", baseTableSchemaName: "public", constraintName: "FKB79DE0D0F521D1F9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "galffy (generated)", id: "1594122495562-36") {
		addForeignKeyConstraint(baseColumnNames: "op_tenant_fk", baseTableName: "org_property", baseTableSchemaName: "public", constraintName: "FKB79DE0D010DAF4E3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "galffy (generated)", id: "1594122495562-37") {
		addForeignKeyConstraint(baseColumnNames: "op_type_fk", baseTableName: "org_property", baseTableSchemaName: "public", constraintName: "FKB79DE0D0B9E2A6CF", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pd_id", referencedTableName: "property_definition", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "galffy (generated)", id: "1594122495562-38") {
		dropTable(tableName: "org_private_property")
	}
}
