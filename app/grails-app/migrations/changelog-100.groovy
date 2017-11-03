databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1508320874725-1") {
		addColumn(tableName: "license_private_property") {
			column(name: "lpp_id", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-2") {
		addColumn(tableName: "license_private_property") {
			column(name: "lpp_owner_fk", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-3") {
		addColumn(tableName: "license_private_property") {
			column(name: "lpp_type_fk", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-4") {
		addColumn(tableName: "license_private_property") {
			column(name: "lpp_version", type: "bigint") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-5") {
		addColumn(tableName: "property_definition") {
			column(name: "pd_mandatory", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-6") {
		addColumn(tableName: "property_definition") {
			column(name: "pd_multiple_occurrence", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-7") {
		addColumn(tableName: "property_definition") {
			column(name: "pd_soft_data", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-8") {
		addColumn(tableName: "property_definition") {
			column(name: "pd_tenant_fk", type: "bigint")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-9") {
		addColumn(tableName: "refdata_category") {
			column(name: "rdv_soft_data", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-10") {
		addColumn(tableName: "refdata_value") {
			column(name: "rdv_soft_data", type: "bit") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-11") {
		addNotNullConstraint(columnDataType: "bigint", columnName: "adr_type_rv_fk", tableName: "address")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-12") {
		addNotNullConstraint(columnDataType: "bigint", columnName: "ct_type_rv_fk", tableName: "contact")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-13") {
		modifyDataType(columnName: "gri_desc", newDataType: "longtext", tableName: "global_record_info")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-14") {
		modifyDataType(columnName: "gri_name", newDataType: "longtext", tableName: "global_record_info")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-15") {
		modifyDataType(columnName: "grs_name", newDataType: "longtext", tableName: "global_record_source")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-16") {
		modifyDataType(columnName: "grt_name", newDataType: "longtext", tableName: "global_record_tracker")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-17") {
		modifyDataType(columnName: "last_updated", newDataType: "datetime", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-18") {
		dropNotNullConstraint(columnDataType: "datetime", columnName: "last_updated", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-19") {
		modifyDataType(columnName: "string_value", newDataType: "varchar(255)", tableName: "license_custom_property")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-20") {
		dropNotNullConstraint(columnDataType: "varchar(255)", columnName: "org_name", tableName: "org")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-21") {
		modifyDataType(columnName: "last_updated", newDataType: "datetime", tableName: "subscription")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-22") {
		dropNotNullConstraint(columnDataType: "datetime", columnName: "last_updated", tableName: "subscription")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-23") {
		modifyDataType(columnName: "sort_title", newDataType: "longtext", tableName: "title_instance")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-24") {
		modifyDataType(columnName: "ti_key_title", newDataType: "longtext", tableName: "title_instance")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-25") {
		modifyDataType(columnName: "ti_norm_title", newDataType: "longtext", tableName: "title_instance")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-26") {
		modifyDataType(columnName: "ti_title", newDataType: "longtext", tableName: "title_instance")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-27") {
		modifyDataType(columnName: "tipp_host_platform_url", newDataType: "longtext", tableName: "title_instance_package_platform")
	}

	changeSet(author: "kloberd (modified)", id: "1508320874725-28") {
        //ORG: addPrimaryKey(columnNames: "lpp_id", constraintName: "license_privaPK", tableName: "license_private_property")
        dropColumn(columnName: "id", tableName: "license_private_property")
	}

	changeSet(author: "kloberd (modified)", id: "1508320874725-29") {
        //ORG: dropPrimaryKey(tableName: "license_private_property")
		addPrimaryKey(columnNames: "lpp_id", constraintName: "license_privaPK", tableName: "license_private_property")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-30") {
		dropForeignKeyConstraint(baseTableName: "cluster", baseTableSchemaName: "KBPlus", constraintName: "FK33FB11FA69E7EA2E")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-31") {
		dropForeignKeyConstraint(baseTableName: "doc", baseTableSchemaName: "KBPlus", constraintName: "FK18538F206CBF4")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-32") {
		dropForeignKeyConstraint(baseTableName: "fact", baseTableSchemaName: "KBPlus", constraintName: "FK2FD66C5CC2FB63")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-33") {
		dropForeignKeyConstraint(baseTableName: "fact", baseTableSchemaName: "KBPlus", constraintName: "FK2FD66CD2A25EFB")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-34") {
		dropForeignKeyConstraint(baseTableName: "fact", baseTableSchemaName: "KBPlus", constraintName: "FK2FD66C467CFA43")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-35") {
		dropForeignKeyConstraint(baseTableName: "fact", baseTableSchemaName: "KBPlus", constraintName: "FK2FD66C4CB39BA6")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-36") {
		dropForeignKeyConstraint(baseTableName: "fact", baseTableSchemaName: "KBPlus", constraintName: "FK2FD66C40C7D5B5")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-37") {
		dropForeignKeyConstraint(baseTableName: "global_record_tracker", baseTableSchemaName: "KBPlus", constraintName: "FK808F5966D92AE946")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-38") {
		dropForeignKeyConstraint(baseTableName: "license", baseTableSchemaName: "KBPlus", constraintName: "FK9F08441B6A1F9E4")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-39") {
		dropForeignKeyConstraint(baseTableName: "license", baseTableSchemaName: "KBPlus", constraintName: "FK9F08441595820F7")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-40") {
		dropForeignKeyConstraint(baseTableName: "license", baseTableSchemaName: "KBPlus", constraintName: "FK9F0844177A5D483")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-41") {
		dropForeignKeyConstraint(baseTableName: "license", baseTableSchemaName: "KBPlus", constraintName: "FK9F084416C7CF136")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-42") {
		dropForeignKeyConstraint(baseTableName: "license", baseTableSchemaName: "KBPlus", constraintName: "FK9F084414B2F78C0")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-43") {
		dropForeignKeyConstraint(baseTableName: "license", baseTableSchemaName: "KBPlus", constraintName: "FK9F0844119B9B694")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-44") {
		dropForeignKeyConstraint(baseTableName: "license", baseTableSchemaName: "KBPlus", constraintName: "FK9F08441D4BECC91")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-45") {
		dropForeignKeyConstraint(baseTableName: "license", baseTableSchemaName: "KBPlus", constraintName: "FK9F084414C1CBBFB")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-46") {
		dropForeignKeyConstraint(baseTableName: "license", baseTableSchemaName: "KBPlus", constraintName: "FK9F08441D77ABF2C")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-47") {
		dropForeignKeyConstraint(baseTableName: "license", baseTableSchemaName: "KBPlus", constraintName: "FK9F08441DBC1FD3A")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-48") {
		dropForeignKeyConstraint(baseTableName: "license", baseTableSchemaName: "KBPlus", constraintName: "FK9F0844197334194")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-49") {
		dropForeignKeyConstraint(baseTableName: "license_private_property", baseTableSchemaName: "KBPlus", constraintName: "FKF9BC354F90DECB4B")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-50") {
		dropForeignKeyConstraint(baseTableName: "license_private_property", baseTableSchemaName: "KBPlus", constraintName: "FKF9BC354FEF8511C1")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-51") {
		dropForeignKeyConstraint(baseTableName: "license_private_property", baseTableSchemaName: "KBPlus", constraintName: "FKF9BC354F638A6383")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-52") {
		dropForeignKeyConstraint(baseTableName: "onixpl_license_text", baseTableSchemaName: "KBPlus", constraintName: "FKF1E88AC0F8B5EA69")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-53") {
		dropForeignKeyConstraint(baseTableName: "onixpl_license_text", baseTableSchemaName: "KBPlus", constraintName: "FKF1E88AC0EF8C4AB4")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-54") {
		dropForeignKeyConstraint(baseTableName: "onixpl_usage_term", baseTableSchemaName: "KBPlus", constraintName: "FK11797DDF2F1DD172")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-55") {
		dropForeignKeyConstraint(baseTableName: "onixpl_usage_term", baseTableSchemaName: "KBPlus", constraintName: "FK11797DDFE9F8A801")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-56") {
		dropForeignKeyConstraint(baseTableName: "onixpl_usage_term", baseTableSchemaName: "KBPlus", constraintName: "FK11797DDFCF47BC89")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-57") {
		dropForeignKeyConstraint(baseTableName: "onixpl_usage_term_license_text", baseTableSchemaName: "KBPlus", constraintName: "FKC989A8CB55D5F69B")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-58") {
		dropForeignKeyConstraint(baseTableName: "onixpl_usage_term_license_text", baseTableSchemaName: "KBPlus", constraintName: "FKC989A8CB3313FD03")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-59") {
		dropForeignKeyConstraint(baseTableName: "onixpl_usage_term_refdata_value", baseTableSchemaName: "KBPlus", constraintName: "FKB744770F7B0024B0")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-60") {
		dropForeignKeyConstraint(baseTableName: "onixpl_usage_term_refdata_value", baseTableSchemaName: "KBPlus", constraintName: "FKB744770FE27A4895")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-61") {
		dropForeignKeyConstraint(baseTableName: "onixpl_usage_term_refdata_value", baseTableSchemaName: "KBPlus", constraintName: "FKB744770FAAD0839C")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-62") {
		dropForeignKeyConstraint(baseTableName: "org_cluster", baseTableSchemaName: "KBPlus", constraintName: "FKBFF1439FB7B3B52")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-63") {
		dropForeignKeyConstraint(baseTableName: "org_cluster", baseTableSchemaName: "KBPlus", constraintName: "FKBFF1439F39056212")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-64") {
		dropForeignKeyConstraint(baseTableName: "org_private_property", baseTableSchemaName: "KBPlus", constraintName: "FKDFC7450C835C6C31")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-65") {
		dropForeignKeyConstraint(baseTableName: "org_title_instance", baseTableSchemaName: "KBPlus", constraintName: "FK41AF6157CC172760")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-66") {
		dropForeignKeyConstraint(baseTableName: "org_title_instance", baseTableSchemaName: "KBPlus", constraintName: "FK41AF6157F0E2D5FD")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-67") {
		dropForeignKeyConstraint(baseTableName: "person", baseTableSchemaName: "KBPlus", constraintName: "FKC4E39B5526759820")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-68") {
		dropForeignKeyConstraint(baseTableName: "person", baseTableSchemaName: "KBPlus", constraintName: "FKC4E39B555406FA95")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-69") {
		dropForeignKeyConstraint(baseTableName: "person", baseTableSchemaName: "KBPlus", constraintName: "FKC4E39B552F08D4E6")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-70") {
		dropForeignKeyConstraint(baseTableName: "person_private_property", baseTableSchemaName: "KBPlus", constraintName: "FK99DFA8BB71DF72B2")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-71") {
		dropForeignKeyConstraint(baseTableName: "private_property_rule", baseTableSchemaName: "KBPlus", constraintName: "FK6DAE656AC258E067")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-72") {
		dropForeignKeyConstraint(baseTableName: "private_property_rule", baseTableSchemaName: "KBPlus", constraintName: "FK6DAE656ACB238934")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-77") {
		dropIndex(indexName: "fact_uid_idx", tableName: "fact")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-78") {
		dropIndex(indexName: "oplt_el_id_idx", tableName: "onixpl_license_text")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-79") {
		dropIndex(indexName: "oput_entry_idx", tableName: "onixpl_usage_term")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-80") {
		dropIndex(indexName: "opul_entry_idx", tableName: "onixpl_usage_term_license_text")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-81") {
		dropIndex(indexName: "td_name_idx", tableName: "property_definition")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-82") {
		createIndex(indexName: "FK808F5966F6287F86", tableName: "global_record_tracker") {
			column(name: "grt_owner_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-83") {
		createIndex(indexName: "fact_uid_uniq_1508320874102", tableName: "kbplus_fact", unique: "true") {
			column(name: "fact_uid")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-84") {
		createIndex(indexName: "FKF9BC354F7EA7815A", tableName: "license_private_property") {
			column(name: "lpp_type_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-85") {
		createIndex(indexName: "FKF9BC354FD9657268", tableName: "license_private_property") {
			column(name: "lpp_owner_fk")
		}
	}

	changeSet(author: "kloberd (modified)", id: "1508320874725-86") {
        modifyDataType(columnName: "org_imp_id", newDataType: "varchar(255)", tableName: "org") // ADDED

		createIndex(indexName: "org_imp_id_idx", tableName: "org") {
			column(name: "org_imp_id")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-87") {
		createIndex(indexName: "FKF00C25FD1DD43936", tableName: "property_definition") {
			column(name: "pd_tenant_fk")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-88") {
		createIndex(indexName: "td_new_idx", tableName: "property_definition") {
			column(name: "pd_description")

			column(name: "pd_name")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-89") {
		createIndex(indexName: "tfmr_url_uniq_1508320874129", tableName: "transformer", unique: "true") {
			column(name: "tfmr_url")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-90") {
		dropColumn(columnName: "cl_owner_fk", tableName: "cluster")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-91") {
		dropColumn(columnName: "doc_migrated", tableName: "doc")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-92") {
		dropColumn(columnName: "owner_id", tableName: "global_record_tracker")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-93") {
		dropColumn(columnName: "lic_alumni_access_rdv_fk", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-94") {
		dropColumn(columnName: "lic_concurrent_user_count", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-95") {
		dropColumn(columnName: "lic_concurrent_users_rdv_fk", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-96") {
		dropColumn(columnName: "lic_coursepack_rdv_fk", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-97") {
		dropColumn(columnName: "lic_enterprise_rdv_fk", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-98") {
		dropColumn(columnName: "lic_ill_rdv_fk", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-99") {
		dropColumn(columnName: "lic_license_type_str", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-100") {
		dropColumn(columnName: "lic_multisite_access_rdv_fk", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-101") {
		dropColumn(columnName: "lic_partners_access_rdv_fk", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-102") {
		dropColumn(columnName: "lic_pca_rdv_fk", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-103") {
		dropColumn(columnName: "lic_remote_access_rdv_fk", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-104") {
		dropColumn(columnName: "lic_vle_rdv_fk", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-105") {
		dropColumn(columnName: "lic_walkin_access_rdv_fk", tableName: "license")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-106") {
		dropColumn(columnName: "date", tableName: "license_custom_property")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-107") {
		dropColumn(columnName: "date", tableName: "license_private_property")
	}

	changeSet(author: "kloberd (modified)", id: "1508320874725-108") {
		//ORG: dropColumn(columnName: "id", tableName: "license_private_property")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-109") {
		dropColumn(columnName: "owner_id", tableName: "license_private_property")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-110") {
		dropColumn(columnName: "tenant_fk", tableName: "license_private_property")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-111") {
		dropColumn(columnName: "type_id", tableName: "license_private_property")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-112") {
		dropColumn(columnName: "version", tableName: "license_private_property")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-113") {
		dropColumn(columnName: "org_address", tableName: "org")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-114") {
		dropColumn(columnName: "date", tableName: "org_custom_property")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-115") {
		dropColumn(columnName: "date", tableName: "org_private_property")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-116") {
		dropColumn(columnName: "opp_tenant_fk", tableName: "org_private_property")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-117") {
		dropColumn(columnName: "prs_is_public_rdv_fk", tableName: "person")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-118") {
		dropColumn(columnName: "prs_org_fk", tableName: "person")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-119") {
		dropColumn(columnName: "prs_owner_fk", tableName: "person")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-120") {
		dropColumn(columnName: "date", tableName: "person_private_property")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-121") {
		dropColumn(columnName: "ppp_tenant_fk", tableName: "person_private_property")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-122") {
		dropColumn(columnName: "date", tableName: "subscription_custom_property")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-123") {
		dropColumn(columnName: "date", tableName: "system_admin_custom_property")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-124") {
		dropTable(tableName: "fact")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-125") {
		dropTable(tableName: "onixpl_license_text")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-126") {
		dropTable(tableName: "onixpl_usage_term")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-127") {
		dropTable(tableName: "onixpl_usage_term_license_text")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-128") {
		dropTable(tableName: "onixpl_usage_term_refdata_value")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-129") {
		dropTable(tableName: "org_bck")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-130") {
		dropTable(tableName: "org_cluster")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-131") {
		dropTable(tableName: "org_title_instance")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-132") {
		dropTable(tableName: "private_property_rule")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-133") {
		dropTable(tableName: "type_definition")
	}

	changeSet(author: "kloberd (modified)", id: "1508320874725-73") {
		//ORG: addForeignKeyConstraint(baseColumnNames: "grt_owner_fk", baseTableName: "global_record_tracker", constraintName: "FK808F5966F6287F86", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "gri_id", referencedTableName: "global_record_info", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-74") {
		addForeignKeyConstraint(baseColumnNames: "lpp_owner_fk", baseTableName: "license_private_property", constraintName: "FKF9BC354FD9657268", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "lic_id", referencedTableName: "license", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-75") {
		addForeignKeyConstraint(baseColumnNames: "lpp_type_fk", baseTableName: "license_private_property", constraintName: "FKF9BC354F7EA7815A", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pd_id", referencedTableName: "property_definition", referencesUniqueColumn: "false")
	}

	changeSet(author: "kloberd (generated)", id: "1508320874725-76") {
		addForeignKeyConstraint(baseColumnNames: "pd_tenant_fk", baseTableName: "property_definition", constraintName: "FKF00C25FD1DD43936", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", referencesUniqueColumn: "false")
	}
}
