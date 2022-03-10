databaseChangeLog = {

	changeSet(author: "galffy (generated)", id: "1593768046016-1") {
		dropForeignKeyConstraint(baseTableName: "subscription_custom_property", baseTableSchemaName: "public", constraintName: "fk8717a7c18bc51d79")
	}

	changeSet(author: "galffy (generated)", id: "1593768046016-2") {
		dropForeignKeyConstraint(baseTableName: "subscription_custom_property", baseTableSchemaName: "public", constraintName: "fk8717a7c14b06441")
	}

	changeSet(author: "galffy (generated)", id: "1593768046016-3") {
		dropForeignKeyConstraint(baseTableName: "subscription_custom_property", baseTableSchemaName: "public", constraintName: "fk8717a7c12992a286")
	}

	changeSet(author: "galffy (generated)", id: "1593768046016-4") {
		dropForeignKeyConstraint(baseTableName: "subscription_custom_property", baseTableSchemaName: "public", constraintName: "fk8717a7c1638a6383")
	}

	changeSet(author: "galffy (modified)", id: "1593768046016-5") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE subscription_custom_property RENAME id  TO sp_id;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1593768046016-6") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE subscription_custom_property RENAME version  TO sp_version;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1593768046016-7") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE subscription_custom_property RENAME date_value  TO sp_date_value;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1593768046016-8") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE subscription_custom_property RENAME dec_value  TO sp_dec_value;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1593768046016-9") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE subscription_custom_property RENAME instance_of_id  TO sp_instance_of_fk;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1593768046016-10") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE subscription_custom_property RENAME int_value  TO sp_int_value;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1593768046016-11") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE subscription_custom_property RENAME note  TO sp_note;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1593768046016-12") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE subscription_custom_property RENAME owner_id  TO sp_owner_fk;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1593768046016-13") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE subscription_custom_property RENAME ref_value_id  TO sp_ref_value_rv_fk;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1593768046016-14") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE subscription_custom_property RENAME string_value  TO sp_string_value;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1593768046016-15") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE subscription_custom_property RENAME type_id  TO sp_type_fk;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1593768046016-16") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE subscription_custom_property RENAME url_value  TO sp_url_value;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1593768046016-17") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE subscription_custom_property RENAME scp_date_created  TO sp_date_created;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1593768046016-18") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE subscription_custom_property RENAME scp_last_updated  TO sp_last_updated;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1593768046016-19") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE subscription_custom_property RENAME last_updated_cascading  TO sp_last_updated_cascading;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1593768046016-20") {
		addColumn(schemaName: "public", tableName: "subscription_custom_property") {
			column(name: "sp_is_public", type: "text")
		}
	}

	changeSet(author: "galffy (modified)", id: "1593768046016-21") {
		grailsChange {
			change {
				sql.execute("update subscription_custom_property set sp_is_public = false where sp_is_public is null;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1593768046016-22") {
		addNotNullConstraint(columnDataType: "bool", columnName: "sp_is_public", tableName: "subscription_custom_property")
	}

	changeSet(author: "galffy (modified)", id: "1593768046016-23") {
		addColumn(schemaName: "public", tableName: "subscription_custom_property") {
			column(name: "sp_tenant_fk", type: "int8")
		}
	}

	changeSet(author: "galffy (modified)", id: "1593768046016-24") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE subscription_custom_property RENAME TO subscription_property;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (generated)", id: "1593768046016-25") {
		createIndex(indexName: "sp_tenant_idx", schemaName: "public", tableName: "subscription_property") {
			column(name: "sp_tenant_fk")
		}
	}

	changeSet(author: "galffy (generated)", id: "1593768046016-26") {
		createIndex(indexName: "sp_type_idx", schemaName: "public", tableName: "subscription_property") {
			column(name: "sp_type_fk")
		}
	}

	changeSet(author: "galffy (generated)", id: "1593768046016-27") {
		addForeignKeyConstraint(baseColumnNames: "sp_instance_of_fk", baseTableName: "subscription_property", baseTableSchemaName: "public", constraintName: "FK6B42A6B742D0FC34", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sp_id", referencedTableName: "subscription_property", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "galffy (generated)", id: "1593768046016-28") {
		addForeignKeyConstraint(baseColumnNames: "sp_owner_fk", baseTableName: "subscription_property", baseTableSchemaName: "public", constraintName: "FK6B42A6B7CCF35F8D", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sub_id", referencedTableName: "subscription", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "galffy (generated)", id: "1593768046016-29") {
		addForeignKeyConstraint(baseColumnNames: "sp_ref_value_rv_fk", baseTableName: "subscription_property", baseTableSchemaName: "public", constraintName: "FK6B42A6B7DACAFCD2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "galffy (generated)", id: "1593768046016-30") {
		addForeignKeyConstraint(baseColumnNames: "sp_tenant_fk", baseTableName: "subscription_property", baseTableSchemaName: "public", constraintName: "FK6B42A6B72FA18A5F", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "galffy (generated)", id: "1593768046016-31") {
		addForeignKeyConstraint(baseColumnNames: "sp_type_fk", baseTableName: "subscription_property", baseTableSchemaName: "public", constraintName: "FK6B42A6B7638A6383", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pd_id", referencedTableName: "property_definition", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "galffy (modified)", id: "1593768046016-32") {
		grailsChange {
			change {
				sql.execute("insert into subscription_property (sp_version, sp_date_value, sp_dec_value, sp_int_value, sp_note, sp_owner_fk, sp_ref_value_rv_fk, sp_string_value, sp_type_fk, sp_url_value, sp_tenant_fk, sp_is_public, sp_date_created, sp_last_updated, sp_last_updated_cascading) " +
						"select spp.spp_version, spp.date_value, spp.dec_value, spp.int_value, spp.note, spp.spp_owner_fk, spp.ref_value_id, spp.string_value, spp.spp_type_fk, spp.url_value, pd.pd_tenant_fk, false, spp.spp_date_created, spp.spp_last_updated, spp.last_updated_cascading from subscription_private_property as spp join property_definition pd on spp.spp_type_fk = pd.pd_id;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1593768046016-33") {
		grailsChange {
			change {
				sql.execute("update subscription_property set sp_tenant_fk = (select or_org_fk from org_role where or_sub_fk = sp_owner_fk and or_roletype_fk in (select rdv_id from refdata_value left join refdata_category on rdv_owner = rdc_id where rdc_description = 'organisational.role' and rdv_value in ('Subscriber','Subscription Consortia'))), sp_is_public = true where sp_tenant_fk is null and (select pd_tenant_fk from property_definition where pd_id = sp_type_fk) is null;")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1593768046016-34") {
		grailsChange {
			change {
				sql.execute("update audit_config set auc_reference_class = 'com.k_int.kbplus.SubscriptionProperty' where auc_reference_class = 'com.k_int.kbplus.SubscriptionCustomProperty';")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (generated)", id: "1593774690657-35") {
		dropForeignKeyConstraint(baseTableName: "subscription_private_property", baseTableSchemaName: "public", constraintName: "fk229733f32992a286")
	}

	changeSet(author: "galffy (generated)", id: "1593774690657-36") {
		dropForeignKeyConstraint(baseTableName: "subscription_private_property", baseTableSchemaName: "public", constraintName: "fk229733f3831290f7")
	}

	changeSet(author: "galffy (generated)", id: "1593774690657-37") {
		dropForeignKeyConstraint(baseTableName: "subscription_private_property", baseTableSchemaName: "public", constraintName: "fk229733f390e864a1")
	}

	changeSet(author: "galffy (generated)", id: "1593774690657-38") {
		dropTable(tableName: "subscription_private_property")
	}
}
