databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1558613037606-1") {
		createTable(schemaName: "public", tableName: "survey_org") {
			column(autoIncrement: "true", name: "surorg_id", type: "int8") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "survey_orgPK")
			}

			column(name: "surorg_version", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "surorg_date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "surorg_last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "surorg_org_fk", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "surorg_pricecomment", type: "text")

			column(name: "surorg_surveyconfig_fk", type: "int8") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1558613037606-2") {
		addColumn(schemaName: "public", tableName: "cost_item") {
			column(name: "ci_copy_base", type: "int8")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1558613037606-3") {
		addColumn(schemaName: "public", tableName: "cost_item") {
			column(name: "ci_surorg_fk", type: "int8")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1558613037606-4") {
		addColumn(schemaName: "public", tableName: "pending_change") {
			column(name: "pc_ci_fk", type: "int8")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1558613037606-5") {
		addColumn(schemaName: "public", tableName: "survey_info") {
			column(name: "surin_status_rv_fk", type: "int8") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1558613037606-6") {
		addColumn(schemaName: "public", tableName: "survey_config") {
			column(name: "surconf_config_finish", type: "bool") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1558613037606-7") {
		grailsChange {
			change {
				sql.execute("DELETE FROM user_role WHERE role_id = (SELECT id FROM role WHERE authority = 'ROLE_ORG_COM_EDITOR')")
				sql.execute("DELETE FROM role WHERE authority = 'ROLE_ORG_COM_EDITOR'")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1558613037606-8") {
		modifyDataType(columnName: "surin_start_date", newDataType: "timestamp", tableName: "survey_info")
	}

	changeSet(author: "kloberd (generated)", id: "1558613037606-9") {
		dropNotNullConstraint(columnDataType: "timestamp", columnName: "surin_start_date", tableName: "survey_info")
	}

	changeSet(author: "kloberd (generated)", id: "1558613037606-10") {
		dropForeignKeyConstraint(baseTableName: "survey_info", baseTableSchemaName: "public", constraintName: "fk234cfe737c3cda39")
	}

	changeSet(author: "kloberd (generated)", id: "1558613037606-17") {
		dropColumn(columnName: "lic_license_status_str", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1558613037606-18") {
		dropColumn(columnName: "surconf_org_ids", tableName: "survey_config")
	}

	changeSet(author: "kloberd (generated)", id: "1558613037606-19") {
		dropColumn(columnName: "survey_configs_idx", tableName: "survey_config")
	}

	changeSet(author: "kloberd (generated)", id: "1558613037606-20") {
		dropColumn(columnName: "status_id", tableName: "survey_info")
	}

	changeSet(author: "kloberd (generated)", id: "1558613037606-11") {
		addForeignKeyConstraint(baseColumnNames: "ci_copy_base", baseTableName: "cost_item", baseTableSchemaName: "public", constraintName: "FKEFE45C4548EB14BF", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ci_id", referencedTableName: "cost_item", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1558613037606-12") {
		addForeignKeyConstraint(baseColumnNames: "ci_surorg_fk", baseTableName: "cost_item", baseTableSchemaName: "public", constraintName: "FKEFE45C45EF66BF04", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "surorg_id", referencedTableName: "survey_org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1558613037606-13") {
		addForeignKeyConstraint(baseColumnNames: "pc_ci_fk", baseTableName: "pending_change", baseTableSchemaName: "public", constraintName: "FK65CBDF58CBC095EF", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ci_id", referencedTableName: "cost_item", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1558613037606-14") {
		addForeignKeyConstraint(baseColumnNames: "surin_status_rv_fk", baseTableName: "survey_info", baseTableSchemaName: "public", constraintName: "FK234CFE7391E0904E", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1558613037606-15") {
		addForeignKeyConstraint(baseColumnNames: "surorg_org_fk", baseTableName: "survey_org", baseTableSchemaName: "public", constraintName: "FKD7D9487F78138952", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1558613037606-16") {
		addForeignKeyConstraint(baseColumnNames: "surorg_surveyconfig_fk", baseTableName: "survey_org", baseTableSchemaName: "public", constraintName: "FKD7D9487F389C6156", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "surconf_id", referencedTableName: "survey_config", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (modified)", id: "1558613037606-21") {
		grailsChange {
			change {
				sql.execute("insert into public.refdata_value (rdv_hard_data,rdv_version,rdv_owner,rdv_value) values (true,1,(select rdc_id from refdata_category where rdc_description = 'License Status'),'Status not defined')")
				sql.execute("insert into public.refdata_value (rdv_hard_data,rdv_version,rdv_owner,rdv_value) values (true,1,(select rdc_id from refdata_category where rdc_description = 'Subscription Status'),'Status not defined')")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1558613037606-22") {
		grailsChange {
			change {
				sql.execute("update license set lic_status_rv_fk = (select rdv_id from refdata_value where rdv_value = 'Status not defined' and rdv_owner = (select rdc_id from refdata_category where rdc_description = 'License Status')) where lic_status_rv_fk is null")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1558613037606-23") {
		grailsChange {
			change {
				sql.execute("update subscription set sub_status_rv_fk = (select rdv_id from refdata_value where rdv_value = 'Status not defined' and rdv_owner = (select rdc_id from refdata_category where rdc_description = 'Subscription Status')) where sub_status_rv_fk is null")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1558613037606-24") {
		addNotNullConstraint(columnDataType: "int8", columnName: "lic_status_rv_fk", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1558613037606-25") {
		addNotNullConstraint(columnDataType: "int8", columnName: "sub_status_rv_fk", tableName: "subscription")
	}

	changeSet(author: "kloberd (modified)", id: "1558613037606-26") {
		grailsChange {
			change {
				sql.execute("update role set authority = 'ORG_BASIC_MEMBER' where authority = 'ORG_MEMBER'")
				sql.execute("update role set authority = 'ORG_INST' where authority = 'ORG_BASIC'")
				sql.execute("update role set authority = 'ORG_INST_COLLECTIVE' where authority = 'ORG_COLLECTIVE'")

				sql.execute("update perm set code = 'org_basic_member' where code = 'org_member'")
				sql.execute("update perm set code = 'org_inst' where code = 'org_basic'")
				sql.execute("update perm set code = 'org_inst_collective' where code = 'org_collective'")
			}
			rollback {}
		}
	}

}
