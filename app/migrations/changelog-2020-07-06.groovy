databaseChangeLog = {

	changeSet(author: "galffy (generated)", id: "1594019505211-1") {
		dropForeignKeyConstraint(baseTableName: "license_custom_property", baseTableSchemaName: "public", constraintName: "fke8df0ae52992a286")
	}

	changeSet(author: "galffy (generated)", id: "1594019505211-2") {
		dropForeignKeyConstraint(baseTableName: "license_custom_property", baseTableSchemaName: "public", constraintName: "fke8df0ae5638a6383")
	}

	changeSet(author: "galffy (modified)", id: "1594019505211-3") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE license_custom_property RENAME id  TO lp_id;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594019505211-4") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE license_custom_property RENAME version  TO lp_version;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594019505211-5") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE license_custom_property RENAME date_value  TO lp_date_value;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594019505211-6") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE license_custom_property RENAME dec_value  TO lp_dec_value;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594019505211-7") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE license_custom_property RENAME instance_of_id  TO lp_instance_of_fk;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594019505211-8") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE license_custom_property RENAME int_value  TO lp_int_value;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594019505211-9") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE license_custom_property RENAME note  TO lp_note;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594019505211-10") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE license_custom_property RENAME paragraph  TO lp_paragraph;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594019505211-11") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE license_custom_property RENAME owner_id  TO lp_owner_fk;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594019505211-12") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE license_custom_property RENAME ref_value_id  TO lp_ref_value_rv_fk;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594019505211-13") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE license_custom_property RENAME string_value  TO lp_string_value;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594019505211-14") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE license_custom_property RENAME type_id  TO lp_type_fk;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594019505211-15") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE license_custom_property RENAME url_value  TO lp_url_value;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594019505211-16") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE license_custom_property RENAME lcp_date_created  TO lp_date_created;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594019505211-17") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE license_custom_property RENAME lcp_last_updated  TO lp_last_updated;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594019505211-18") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE license_custom_property RENAME last_updated_cascading  TO lp_last_updated_cascading;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594019505211-19") {
		addColumn(schemaName: "public", tableName: "license_custom_property") {
			column(name: "lp_is_public", type: "bool")
		}
	}

	changeSet(author: "galffy (modified)", id: "1594019505211-20") {
		grailsChange {
			change {
				sql.execute("update license_custom_property set lp_is_public = false where lp_is_public is null;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594019505211-21") {
		grailsChange {
			change {
				sql.execute("alter table subscription_property alter column sp_is_public type boolean using sp_is_public::boolean;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594019505211-22") {
		addNotNullConstraint(columnDataType: "bool", columnName: "lp_is_public", tableName: "license_custom_property")
	}

	changeSet(author: "galffy (modified)", id: "1594019505211-23") {
		addColumn(schemaName: "public", tableName: "license_custom_property") {
			column(name: "lp_tenant_fk", type: "int8")
		}
	}

	changeSet(author: "galffy (modified)", id: "1594019505211-24") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE license_custom_property RENAME TO license_property;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594019505211-25") {
		grailsChange {
			change {
				sql.execute("insert into license_property (lp_version, lp_date_value, lp_dec_value, lp_int_value, lp_note, lp_paragraph, lp_owner_fk, lp_ref_value_rv_fk, lp_string_value, lp_type_fk, lp_url_value, lp_tenant_fk, lp_is_public, lp_date_created, lp_last_updated, lp_last_updated_cascading) " +
						"select lpp.lpp_version, lpp.date_value, lpp.dec_value, lpp.int_value, lpp.note, lpp.paragraph, lpp.lpp_owner_fk, lpp.ref_value_id, lpp.string_value, lpp.lpp_type_fk, lpp.url_value, pd.pd_tenant_fk, false, lpp.lpp_date_created, lpp.lpp_last_updated, lpp.last_updated_cascading from license_private_property as lpp join property_definition pd on lpp.lpp_type_fk = pd.pd_id;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594019505211-26") {
		grailsChange {
			change {
				sql.execute("update license_property set lp_tenant_fk = (select or_org_fk from org_role where or_lic_fk = lp_owner_fk and or_roletype_fk in (select rdv_id from refdata_value left join refdata_category on rdv_owner = rdc_id where rdc_description = 'organisational.role' and rdv_value in ('Licensee','Licensing Consortium'))), lp_is_public = true where lp_tenant_fk is null and (select pd_tenant_fk from property_definition where pd_id = lp_type_fk) is null;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1594019505211-27") {
		grailsChange {
			change {
				sql.execute("update audit_config set auc_reference_class = 'com.k_int.kbplus.LicenseProperty' where auc_reference_class = 'com.k_int.kbplus.LicenseCustomProperty';")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (generated)", id: "1594019505211-28") {
		createIndex(indexName: "lp_tenant_idx", schemaName: "public", tableName: "license_property") {
			column(name: "lp_tenant_fk")
		}
	}

	changeSet(author: "galffy (generated)", id: "1594019505211-29") {
		createIndex(indexName: "lp_type_idx", schemaName: "public", tableName: "license_property") {
			column(name: "lp_type_fk")
		}
	}

	changeSet(author: "galffy (generated)", id: "1594019505211-30") {
		createIndex(indexName: "lp_owner_idx", schemaName: "public", tableName: "license_property") {
			column(name: "lp_owner_fk")
		}
	}

	changeSet(author: "galffy (generated)", id: "1594019505211-31") {
		addForeignKeyConstraint(baseColumnNames: "lp_instance_of_fk", baseTableName: "license_property", baseTableSchemaName: "public", constraintName: "FKD33CC413CFC87F72", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "lp_id", referencedTableName: "license_property", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "galffy (generated)", id: "1594019505211-32") {
		addForeignKeyConstraint(baseColumnNames: "lp_owner_fk", baseTableName: "license_property", baseTableSchemaName: "public", constraintName: "FKD33CC41390DECB4B", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "lic_id", referencedTableName: "license", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "galffy (generated)", id: "1594019505211-33") {
		addForeignKeyConstraint(baseColumnNames: "lp_ref_value_rv_fk", baseTableName: "license_property", baseTableSchemaName: "public", constraintName: "FKD33CC4132992A286", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "galffy (generated)", id: "1594019505211-34") {
		addForeignKeyConstraint(baseColumnNames: "lp_type_fk", baseTableName: "license_property", baseTableSchemaName: "public", constraintName: "FKD33CC413638A6383", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pd_id", referencedTableName: "property_definition", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "galffy (generated)", id: "1594019505211-35") {
		dropTable(tableName: "license_private_property")
	}

	changeSet(author: "klober (modified)", id: "1594019505211-36") {
		grailsChange {
			change {
				sql.execute(""" update change_notification_queue_item
            set cnqi_change_document = replace(cnqi_change_document, 'de.laser.domain.', 'de.laser.')
            where cnqi_change_document like '%de.laser.domain.%'; """)
			}
			rollback {}
		}
	}

	changeSet(author: "klober (modified)", id: "1594019505211-37") {
      grailsChange {
          change {
            sql.execute(""" update pending_change
               set pc_payload = replace(pc_payload, 'de.laser.domain.', 'de.laser.')
               where pc_payload like '%de.laser.domain.%'; """)
           }
           rollback {}
      }
  }
}
