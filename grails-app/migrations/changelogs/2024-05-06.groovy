package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1714976179029-1") {
        createTable(tableName: "invoicing_vendor") {
            column(autoIncrement: "true", name: "iv_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "invoicing_vendorPK")
            }

            column(name: "iv_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "iv_date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "iv_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "iv_vendor_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "iv_provider_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-2") {
        createTable(tableName: "provider") {
            column(autoIncrement: "true", name: "prov_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "providerPK")
            }

            column(name: "prov_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "prov_gokb_id", type: "TEXT")

            column(name: "prov_individual_invoice_design", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "prov_date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "prov_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "prov_processing_of_compensation_payments", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "prov_sortname", type: "VARCHAR(255)")

            column(name: "prov_metadata_downloader_url", type: "TEXT")

            column(name: "prov_homepage", type: "VARCHAR(512)")

            column(name: "prov_last_updated_cascading", type: "TIMESTAMP WITHOUT TIME ZONE")

            column(name: "prov_retirement_date", type: "TIMESTAMP WITHOUT TIME ZONE")

            column(name: "prov_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "prov_guid", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "prov_kbart_downloader_url", type: "TEXT")

            column(name: "prov_status_rv_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "prov_management_of_credits", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "prov_paper_invoice", type: "BOOLEAN") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-3") {
        createTable(tableName: "provider_property") {
            column(autoIncrement: "true", name: "prp_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "provider_propertyPK")
            }

            column(name: "prp_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "prp_ref_value_rv_fk", type: "BIGINT")

            column(name: "prp_int_value", type: "INTEGER")

            column(name: "prp_date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "prp_date_value", type: "TIMESTAMP WITHOUT TIME ZONE")

            column(name: "prp_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE")

            column(name: "prp_dec_value", type: "numeric(19, 2)")

            column(name: "prp_owner_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "prp_is_public", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "prp_tenant_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "prp_string_value", type: "TEXT")

            column(name: "prp_last_updated_cascading", type: "TIMESTAMP WITHOUT TIME ZONE")

            column(name: "prp_type_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "prp_note", type: "TEXT")

            column(name: "prp_url_value", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-4") {
        createTable(tableName: "provider_role") {
            column(autoIncrement: "true", name: "pr_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "provider_rolePK")
            }

            column(name: "pr_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "pr_is_shared", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "pr_shared_from_fk", type: "BIGINT")

            column(name: "pr_date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "pr_subscription_fk", type: "BIGINT")

            column(name: "pr_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "pr_license_fk", type: "BIGINT")

            column(name: "pr_provider_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-5") {
        addColumn(tableName: "address") {
            column(name: "adr_provider_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-6") {
        addColumn(tableName: "alternative_name") {
            column(name: "altname_prov_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-7") {
        addColumn(tableName: "contact") {
            column(name: "ct_provider_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-8") {
        addColumn(tableName: "electronic_billing") {
            column(name: "eb_provider_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-9") {
        addColumn(tableName: "identifier") {
            column(name: "id_provider_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-10") {
        addColumn(tableName: "invoice_dispatch") {
            column(name: "idi_provider_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-11") {
        addColumn(tableName: "marker") {
            column(name: "mkr_prov_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-12") {
        addColumn(tableName: "package") {
            column(name: "pkg_provider_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-13") {
        addColumn(tableName: "platform") {
            column(name: "plat_provider_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-14") {
        addColumn(tableName: "person_role") {
            column(name: "pr_provider_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-15") {
        addUniqueConstraint(columnNames: "prov_gokb_id", constraintName: "UC_PROVIDERPROV_GOKB_ID_COL", tableName: "provider")
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-16") {
        addUniqueConstraint(columnNames: "prov_guid", constraintName: "UC_PROVIDERPROV_GUID_COL", tableName: "provider")
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-17") {
        createIndex(indexName: "adr_provider_idx", tableName: "address") {
            column(name: "adr_provider_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-18") {
        createIndex(indexName: "ct_provider_idx", tableName: "contact") {
            column(name: "ct_provider_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-19") {
        createIndex(indexName: "id_provider_idx", tableName: "identifier") {
            column(name: "id_provider_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-20") {
        createIndex(indexName: "plat_provider_idx", tableName: "platform") {
            column(name: "plat_provider_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-21") {
        createIndex(indexName: "pr_license_idx", tableName: "provider_role") {
            column(name: "pr_license_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-22") {
        createIndex(indexName: "pr_provider_idx", tableName: "provider_role") {
            column(name: "pr_provider_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-23") {
        createIndex(indexName: "pr_prs_prov_idx", tableName: "person_role") {
            column(name: "pr_provider_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-24") {
        createIndex(indexName: "pr_subscription_idx", tableName: "provider_role") {
            column(name: "pr_subscription_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-25") {
        createIndex(indexName: "prov_name_idx", tableName: "provider") {
            column(name: "prov_name")
        }
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-26") {
        createIndex(indexName: "prov_sortname_idx", tableName: "provider") {
            column(name: "prov_sortname")
        }
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-27") {
        createIndex(indexName: "prp_owner_idx", tableName: "provider_property") {
            column(name: "prp_owner_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-28") {
        createIndex(indexName: "prp_tenant_idx", tableName: "provider_property") {
            column(name: "prp_tenant_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-29") {
        createIndex(indexName: "prp_type_idx", tableName: "provider_property") {
            column(name: "prp_type_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-30") {
        addForeignKeyConstraint(baseColumnNames: "mkr_prov_fk", baseTableName: "marker", constraintName: "FK1job6ggry6elwwm3o53m7pav2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "prov_id", referencedTableName: "provider", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-31") {
        addForeignKeyConstraint(baseColumnNames: "eb_provider_fk", baseTableName: "electronic_billing", constraintName: "FK1vk8i4e79ub3kahcb47bu0r0v", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "prov_id", referencedTableName: "provider", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-32") {
        addForeignKeyConstraint(baseColumnNames: "plat_provider_fk", baseTableName: "platform", constraintName: "FK4quypd3wbxvsj4i8jdesemiu5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "prov_id", referencedTableName: "provider", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-33") {
        addForeignKeyConstraint(baseColumnNames: "pr_shared_from_fk", baseTableName: "provider_role", constraintName: "FK6sfevdfr88rtnyolhocvaoe4k", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pr_id", referencedTableName: "provider_role", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-34") {
        addForeignKeyConstraint(baseColumnNames: "pr_license_fk", baseTableName: "provider_role", constraintName: "FK761tdsf8il96pav54bh5i5vnh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "lic_id", referencedTableName: "license", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-35") {
        addForeignKeyConstraint(baseColumnNames: "iv_vendor_fk", baseTableName: "invoicing_vendor", constraintName: "FK7e14w8xslmtyaokls3hvhmote", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ven_id", referencedTableName: "vendor", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-36") {
        addForeignKeyConstraint(baseColumnNames: "idi_provider_fk", baseTableName: "invoice_dispatch", constraintName: "FK7slcudapbh50q2lcjvekb2sp6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "prov_id", referencedTableName: "provider", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-37") {
        addForeignKeyConstraint(baseColumnNames: "pr_provider_fk", baseTableName: "provider_role", constraintName: "FK9vts2h9vaeje8o3jgewmiyavy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "prov_id", referencedTableName: "provider", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-38") {
        addForeignKeyConstraint(baseColumnNames: "prp_type_fk", baseTableName: "provider_property", constraintName: "FKaaeggd4b5x92xopde9ra47xqc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pd_id", referencedTableName: "property_definition", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-39") {
        addForeignKeyConstraint(baseColumnNames: "prp_owner_fk", baseTableName: "provider_property", constraintName: "FKaiv2k7ptdwtuy15w67sc8wrde", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "prov_id", referencedTableName: "provider", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-40") {
        addForeignKeyConstraint(baseColumnNames: "altname_prov_fk", baseTableName: "alternative_name", constraintName: "FKbknr57e6cdj02y28j7oefm4hw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "prov_id", referencedTableName: "provider", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-41") {
        addForeignKeyConstraint(baseColumnNames: "adr_provider_fk", baseTableName: "address", constraintName: "FKe3vs0eudj8pd59yh2vn4onpmi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "prov_id", referencedTableName: "provider", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-42") {
        addForeignKeyConstraint(baseColumnNames: "prov_status_rv_fk", baseTableName: "provider", constraintName: "FKekpkw0ukm96s8j10o24dn00ml", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-43") {
        addForeignKeyConstraint(baseColumnNames: "prp_tenant_fk", baseTableName: "provider_property", constraintName: "FKfajalrqa9qgl4v65c3fw34syi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-44") {
        addForeignKeyConstraint(baseColumnNames: "id_provider_fk", baseTableName: "identifier", constraintName: "FKhsg0loeqkyp0sak34axod15n0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "prov_id", referencedTableName: "provider", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-45") {
        addForeignKeyConstraint(baseColumnNames: "pkg_provider_fk", baseTableName: "package", constraintName: "FKiewym40cy64prhwn9js4q8hej", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "prov_id", referencedTableName: "provider", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-46") {
        addForeignKeyConstraint(baseColumnNames: "iv_provider_fk", baseTableName: "invoicing_vendor", constraintName: "FKisrodfpp7vd3vf61di0pnhif8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "prov_id", referencedTableName: "provider", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-47") {
        addForeignKeyConstraint(baseColumnNames: "pr_provider_fk", baseTableName: "person_role", constraintName: "FKjd1gcrp05rin0hrhrjpnowy8n", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "prov_id", referencedTableName: "provider", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-48") {
        addForeignKeyConstraint(baseColumnNames: "prp_ref_value_rv_fk", baseTableName: "provider_property", constraintName: "FKjkeyxuh8v6js48kycyayc9mn8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-49") {
        addForeignKeyConstraint(baseColumnNames: "pr_subscription_fk", baseTableName: "provider_role", constraintName: "FKl3swwj03ee01dsp67ku809p03", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sub_id", referencedTableName: "subscription", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-50") {
        addForeignKeyConstraint(baseColumnNames: "ct_provider_fk", baseTableName: "contact", constraintName: "FKo2bsn6k0tah9k83lmhl2pvs3d", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "prov_id", referencedTableName: "provider", validate: "true")
    }


    changeSet(author: "galffy (generated)", id: "1714976179029-51") {
        addColumn(tableName: "alternative_name") {
            column(name: "altname_vendor_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-52") {
        addColumn(tableName: "doc_context") {
            column(name: "dc_prov_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-53") {
        addColumn(tableName: "doc_context") {
            column(name: "dc_ven_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-54") {
        createIndex(indexName: "doc_prov_idx", tableName: "doc_context") {
            column(name: "dc_prov_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-55") {
        createIndex(indexName: "doc_ven_idx", tableName: "doc_context") {
            column(name: "dc_ven_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-56") {
        addForeignKeyConstraint(baseColumnNames: "dc_prov_fk", baseTableName: "doc_context", constraintName: "FK5bymcjuptssm9gvykqhfpshg4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "prov_id", referencedTableName: "provider", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-57") {
        addForeignKeyConstraint(baseColumnNames: "altname_vendor_fk", baseTableName: "alternative_name", constraintName: "FK9hus4ep2yufw7o2ltlqg9c5ye", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ven_id", referencedTableName: "vendor", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-58") {
        addForeignKeyConstraint(baseColumnNames: "dc_ven_fk", baseTableName: "doc_context", constraintName: "FKmeu5b7kfkeidg1kv7g81iuy42", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ven_id", referencedTableName: "vendor", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-59") {
        addColumn(tableName: "task") {
            column(name: "tsk_prov_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-60") {
        addColumn(tableName: "task") {
            column(name: "tsk_ven_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-61") {
        addForeignKeyConstraint(baseColumnNames: "tsk_ven_fk", baseTableName: "task", constraintName: "FK8fwfwwg8d0v9hqj23rq57d874", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ven_id", referencedTableName: "vendor", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1714976179029-62") {
        addForeignKeyConstraint(baseColumnNames: "tsk_prov_fk", baseTableName: "task", constraintName: "FKd14mvwoi3v4tane6uja2l8u1w", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "prov_id", referencedTableName: "provider", validate: "true")
    }
}
