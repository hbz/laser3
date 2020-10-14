databaseChangeLog = {

	changeSet(author: "kloberd (modified)", id: "1556107615066-1") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE public.numbers RENAME TO reader_number")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-2") {
		addColumn(schemaName: "public", tableName: "doc_context") {
			column(name: "dc_survey_config_fk", type: "int8")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-3") {
		addColumn(schemaName: "public", tableName: "org_settings") {
			column(name: "os_role_fk", type: "int8")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-4") {
		addColumn(schemaName: "public", tableName: "platform") {
			column(name: "plat_org_fk", type: "int8")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-6") {
		addColumn(schemaName: "public", tableName: "stats_triple_cursor") {
			column(name: "avail_to", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-7") {
		addColumn(schemaName: "public", tableName: "survey_config") {
			column(name: "surconf_config_order", type: "int4") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-8") {
		addColumn(schemaName: "public", tableName: "survey_config") {
			column(name: "surconf_pickandchoose", type: "bool") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-9") {
		addColumn(schemaName: "public", tableName: "survey_result") {
			column(name: "date_value", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-10") {
		addColumn(schemaName: "public", tableName: "survey_result") {
			column(name: "dec_value", type: "numeric(19, 2)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-11") {
		addColumn(schemaName: "public", tableName: "survey_result") {
			column(name: "int_value", type: "int4")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-12") {
		addColumn(schemaName: "public", tableName: "survey_result") {
			column(name: "note", type: "text")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-13") {
		addColumn(schemaName: "public", tableName: "survey_result") {
			column(name: "ref_value_id", type: "int8")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-14") {
		addColumn(schemaName: "public", tableName: "survey_result") {
			column(name: "string_value", type: "text")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-15") {
		addColumn(schemaName: "public", tableName: "survey_result") {
			column(name: "surre_comment", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-16") {
		addColumn(schemaName: "public", tableName: "survey_result") {
			column(name: "surre_date_created", type: "timestamp") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-17") {
		addColumn(schemaName: "public", tableName: "survey_result") {
			column(name: "surre_end_date", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-18") {
		addColumn(schemaName: "public", tableName: "survey_result") {
			column(name: "surre_finish_date", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-19") {
		addColumn(schemaName: "public", tableName: "survey_result") {
			column(name: "surre_id", type: "int8") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-20") {
		addColumn(schemaName: "public", tableName: "survey_result") {
			column(name: "surre_last_updated", type: "timestamp") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-21") {
		addColumn(schemaName: "public", tableName: "survey_result") {
			column(name: "surre_owner_fk", type: "int8") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-22") {
		addColumn(schemaName: "public", tableName: "survey_result") {
			column(name: "surre_participant_fk", type: "int8") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-23") {
		addColumn(schemaName: "public", tableName: "survey_result") {
			column(name: "surre_result_values", type: "bytea")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-24") {
		addColumn(schemaName: "public", tableName: "survey_result") {
			column(name: "surre_start_date", type: "timestamp")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-25") {
		addColumn(schemaName: "public", tableName: "survey_result") {
			column(name: "surre_survey_config_fk", type: "int8") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-26") {
		addColumn(schemaName: "public", tableName: "survey_result") {
			column(name: "surre_type_fk", type: "int8") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-27") {
		addColumn(schemaName: "public", tableName: "survey_result") {
			column(name: "surre_user_fk", type: "int8")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-28") {
		addColumn(schemaName: "public", tableName: "survey_result") {
			column(name: "surre_version", type: "int8") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-29") {
		addColumn(schemaName: "public", tableName: "survey_result") {
			column(name: "url_value", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-30") {
		modifyDataType(columnName: "os_id", newDataType: "int8", tableName: "org_settings")
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-31") {
		modifyDataType(columnName: "surconf_id", newDataType: "int8", tableName: "survey_config")
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-32") {
		modifyDataType(columnName: "surconpro_id", newDataType: "int8", tableName: "survey_config_properties")
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-33") {
		modifyDataType(columnName: "surin_id", newDataType: "int8", tableName: "survey_info")
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-34") {
		modifyDataType(columnName: "surpro_id", newDataType: "int8", tableName: "survey_property")
	}

    changeSet(author: "kloberd (generated)", id: "1556107615066-35") {
        dropPrimaryKey(constraintName: "survey_resultPK", tableName: "survey_result")
    }

    changeSet(author: "kloberd (generated)", id: "1556107615066-36") {
        addPrimaryKey(columnNames: "surre_id", constraintName: "survey_resultPK", schemaName: "public", tableName: "survey_result")
    }

	changeSet(author: "kloberd (generated)", id: "1556107615066-37") {
		dropForeignKeyConstraint(baseTableName: "reader_number", baseTableSchemaName: "public", constraintName: "fk88c28e4ab8fa5820")
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-38") {
		dropForeignKeyConstraint(baseTableName: "reader_number", baseTableSchemaName: "public", constraintName: "fk88c28e4a3026029e")
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-39") {
		dropForeignKeyConstraint(baseTableName: "org_perm_share", baseTableSchemaName: "public", constraintName: "fk4eeb620b17b140a3")
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-40") {
		dropForeignKeyConstraint(baseTableName: "org_perm_share", baseTableSchemaName: "public", constraintName: "fk4eeb620bbfe25067")
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-41") {
		dropForeignKeyConstraint(baseTableName: "survey_config_doc", baseTableSchemaName: "public", constraintName: "fka30538c0ee3591d")
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-42") {
		dropForeignKeyConstraint(baseTableName: "survey_config_doc", baseTableSchemaName: "public", constraintName: "fka30538c05c88a960")
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-54") {
		dropColumn(columnName: "have_up_to", tableName: "stats_triple_cursor")
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-55") {
		dropColumn(columnName: "surconf_config_order_fk", tableName: "survey_config")
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-56") {
		dropColumn(columnName: "date_created", tableName: "survey_result")
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-57") {
		dropColumn(columnName: "id", tableName: "survey_result")
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-58") {
		dropColumn(columnName: "last_updated", tableName: "survey_result")
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-59") {
		dropColumn(columnName: "version", tableName: "survey_result")
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-60") {
		dropTable(tableName: "org_perm_share")
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-61") {
		dropTable(tableName: "survey_config_doc")
	}

    changeSet(author: "kloberd (modified)", id: "1556107615066-62") {
        dropColumn(columnName: "num_end_date", tableName: "reader_number")
    }

    changeSet(author: "kloberd (modified)", id: "1556107615066-63") {
        grailsChange {
            change {
                sql.execute("ALTER TABLE public.reader_number RENAME COLUMN num_number TO num_value")
            }
            rollback {}
        }
    }

    changeSet(author: "kloberd (modified)", id: "1556107615066-64") {
        grailsChange {
            change {
                sql.execute("ALTER TABLE public.reader_number RENAME COLUMN num_start_date TO num_due_date")
            }
            rollback {}
        }
    }

    changeSet(author: "kloberd (modified)", id: "1556107615066-65") {
        grailsChange {
            change {
                sql.execute("ALTER TABLE public.reader_number RENAME COLUMN num_typ_rdv_fk TO num_reference_group")
            }
            rollback {}
        }
    }

    changeSet(author: "kloberd (modified)", id: "1556107615066-66") {
        modifyDataType(columnName: "num_reference_group", newDataType: "varchar(255)", tableName: "reader_number")
    }

    changeSet(author: "kloberd (generated)", id: "1556107615066-67") {
        addColumn(schemaName: "public", tableName: "reader_number") {
            column(name: "num_semester_rv_fk", type: "int8")
        }
    }

    changeSet(author: "kloberd (generated)", id: "1556107615066-68") {
        modifyDataType(columnName: "num_due_date", newDataType: "timestamp", tableName: "reader_number")
    }

    changeSet(author: "kloberd (generated)", id: "1556107615066-69") {
        dropNotNullConstraint(columnDataType: "timestamp", columnName: "num_due_date", tableName: "reader_number")
    }

	changeSet(author: "kloberd (generated)", id: "1556107615066-43") {
		addForeignKeyConstraint(baseColumnNames: "dc_survey_config_fk", baseTableName: "doc_context", baseTableSchemaName: "public", constraintName: "FK30EBA9A8B98DE436", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "surconf_id", referencedTableName: "survey_config", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-44") {
		addForeignKeyConstraint(baseColumnNames: "os_role_fk", baseTableName: "org_settings", baseTableSchemaName: "public", constraintName: "FK4852BF1EA55C9B32", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "role", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-45") {
		addForeignKeyConstraint(baseColumnNames: "plat_org_fk", baseTableName: "platform", baseTableSchemaName: "public", constraintName: "FK6FBD687336453857", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-46") {
		addForeignKeyConstraint(baseColumnNames: "num_org_fk", baseTableName: "reader_number", baseTableSchemaName: "public", constraintName: "FK3BD5AE45B8FA5820", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-47") {
		addForeignKeyConstraint(baseColumnNames: "num_semester_rv_fk", baseTableName: "reader_number", baseTableSchemaName: "public", constraintName: "FK3BD5AE4563A35263", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-48") {
		addForeignKeyConstraint(baseColumnNames: "ref_value_id", baseTableName: "survey_result", baseTableSchemaName: "public", constraintName: "FK92EA04A22992A286", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-49") {
		addForeignKeyConstraint(baseColumnNames: "surre_owner_fk", baseTableName: "survey_result", baseTableSchemaName: "public", constraintName: "FK92EA04A25AEB7B54", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-50") {
		addForeignKeyConstraint(baseColumnNames: "surre_participant_fk", baseTableName: "survey_result", baseTableSchemaName: "public", constraintName: "FK92EA04A23FFB11D4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-51") {
		addForeignKeyConstraint(baseColumnNames: "surre_survey_config_fk", baseTableName: "survey_result", baseTableSchemaName: "public", constraintName: "FK92EA04A2D80F80B2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "surconf_id", referencedTableName: "survey_config", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-52") {
		addForeignKeyConstraint(baseColumnNames: "surre_type_fk", baseTableName: "survey_result", baseTableSchemaName: "public", constraintName: "FK92EA04A27E23DD3A", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "surpro_id", referencedTableName: "survey_property", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1556107615066-53") {
		addForeignKeyConstraint(baseColumnNames: "surre_user_fk", baseTableName: "survey_result", baseTableSchemaName: "public", constraintName: "FK92EA04A2289BAB1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}
}
