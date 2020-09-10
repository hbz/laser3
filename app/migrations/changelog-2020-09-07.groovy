databaseChangeLog = {

	changeSet(author: "kloberd (modified)", id: "1599469610364-1") {
		grailsChange {
			change {
				sql.execute("update ftcontrol set active = false where active is null")
			}
			rollback {}
		}
	}

	changeSet(author: "klober (generated)", id: "1599469610364-2") {
		addNotNullConstraint(columnDataType: "bool", columnName: "active", tableName: "ftcontrol")
	}

	changeSet(author: "klober (generated)", id: "1599469610364-3") {
		addNotNullConstraint(columnDataType: "bool", columnName: "surconf_create_title_groups", tableName: "survey_config")
	}

	changeSet(author: "klober (generated)", id: "1599469610364-4") {
		dropNotNullConstraint(columnDataType: "timestamp", columnName: "surre_date_created", tableName: "survey_result")
	}

	changeSet(author: "klober (generated)", id: "1599469610364-5") {
		dropNotNullConstraint(columnDataType: "timestamp", columnName: "surre_last_updated", tableName: "survey_result")
	}

	changeSet(author: "klober (generated)", id: "1599469610364-6") {
		addNotNullConstraint(columnDataType: "varchar(255)", columnName: "sm_type", tableName: "system_message")
	}

	changeSet(author: "klober (generated)", id: "1599469610364-7") {
		dropForeignKeyConstraint(baseTableName: "creator_title", baseTableSchemaName: "public", constraintName: "fk68b13485ad5ae355")
	}

	changeSet(author: "klober (generated)", id: "1599469610364-8") {
		dropForeignKeyConstraint(baseTableName: "creator_title", baseTableSchemaName: "public", constraintName: "fk68b134855e791dd6")
	}

	changeSet(author: "klober (generated)", id: "1599469610364-9") {
		dropForeignKeyConstraint(baseTableName: "creator_title", baseTableSchemaName: "public", constraintName: "fk68b134855b4619ca")
	}

	changeSet(author: "klober (generated)", id: "1599469610364-10") {
		dropForeignKeyConstraint(baseTableName: "identifier", baseTableSchemaName: "public", constraintName: "fk9f88aca915f7cc81")
	}

	changeSet(author: "klober (generated)", id: "1599469610364-11") {
		addForeignKeyConstraint(baseColumnNames: "lp_tenant_fk", baseTableName: "license_property", baseTableSchemaName: "public", constraintName: "FKD33CC413F9C604C6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "klober (generated)", id: "1599469610364-12") {
		dropColumn(columnName: "id_cre_fk", tableName: "identifier")
	}

	changeSet(author: "klober (generated)", id: "1599469610364-13") {
		dropSequence(schemaName: "public", sequenceName: "annotation_id_seq")
	}

	changeSet(author: "klober (generated)", id: "1599469610364-14") {
		dropTable(tableName: "annotation")
	}

	changeSet(author: "klober (generated)", id: "1599469610364-15") {
		dropSequence(schemaName: "public", sequenceName: "creator_cre_id_seq")
	}

	changeSet(author: "klober (generated)", id: "1599469610364-16") {
		dropTable(tableName: "creator")
	}

	changeSet(author: "klober (generated)", id: "1599469610364-17") {
		dropSequence(schemaName: "public", sequenceName: "creator_title_ct_id_seq")
	}

	changeSet(author: "klober (generated)", id: "1599469610364-18") {
		dropTable(tableName: "creator_title")
	}

	changeSet(author: "klober (generated)", id: "1599469610364-19") {
		dropColumn(columnName: "doc_blob_content", tableName: "doc")
  }
  
	changeSet(author: "djebeniani (generated)", id: "1599469610364-20") {
		modifyDataType(columnName: "surre_comment", newDataType: "text", tableName: "survey_result")
	}

	changeSet(author: "djebeniani (generated)", id: "1599469610364-21") {
		modifyDataType(columnName: "surre_owner_comment", newDataType: "text", tableName: "survey_result")
	}

	changeSet(author: "djebeniani (generated)", id: "1599469610364-22") {
		modifyDataType(columnName: "surre_participant_comment", newDataType: "text", tableName: "survey_result")
	}

	changeSet(author: "klober (modified)", id: "1599469610364-23") {
		grailsChange {
			change {
				sql.execute("update org_access_point set class = replace(class, 'com.k_int.kbplus.', 'de.laser.') where class is not null")
			}
			rollback {}
		}
	}

	changeSet(author: "klober (modified)", id: "1599469610364-24") {
		grailsChange {
			change {
				sql.execute("update i10n_translation set i10n_reference_class = replace(i10n_reference_class, 'com.k_int.kbplus.Survey', 'de.laser.Survey') where i10n_reference_class is not null")
			}
			rollback {}
		}
	}
}
