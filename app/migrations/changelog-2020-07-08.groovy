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
}
