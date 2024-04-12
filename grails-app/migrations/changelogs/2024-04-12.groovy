package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1712929543410-1") {
        createTable(tableName: "electronic_billing") {
            column(autoIncrement: "true", name: "eb_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "electronic_billingPK")
            }

            column(name: "eb_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "eb_invoicing_format_rv_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "eb_vendor_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1712929543410-2") {
        createTable(tableName: "electronic_delivery_delay_notification") {
            column(autoIncrement: "true", name: "eddn_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "electronic_delivery_delay_notificationPK")
            }

            column(name: "eddn_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "eddn_delay_notification_rv_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "eddn_vendor_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1712929543410-3") {
        createTable(tableName: "invoice_dispatch") {
            column(autoIncrement: "true", name: "idi_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "invoice_dispatchPK")
            }

            column(name: "idi_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "idi_invoice_dispatch_rv_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "idi_vendor_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1712929543410-4") {
        createTable(tableName: "library_system") {
            column(autoIncrement: "true", name: "ls_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "library_systemPK")
            }

            column(name: "ls_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "ls_library_system_rv_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "ls_vendor_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1712929543410-5") {
        addColumn(tableName: "vendor") {
            column(name: "vel_edi_orders", type: "boolean") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1712929543410-6") {
        addColumn(tableName: "vendor") {
            column(name: "ven_management_of_credits", type: "boolean") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1712929543410-7") {
        addColumn(tableName: "vendor") {
            column(name: "ven_activation_new_releases", type: "boolean") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1712929543410-8") {
        addColumn(tableName: "vendor") {
            column(name: "ven_exchange_individual_titles", type: "boolean") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1712929543410-9") {
        addColumn(tableName: "vendor") {
            column(name: "ven_forwarding_usage_statistics_from_publisher", type: "boolean") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1712929543410-10") {
        addColumn(tableName: "vendor") {
            column(name: "ven_homepage", type: "varchar(255)")
        }
    }

    changeSet(author: "galffy (generated)", id: "1712929543410-11") {
        addColumn(tableName: "vendor") {
            column(name: "ven_individual_invoice_design", type: "boolean") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1712929543410-12") {
        addColumn(tableName: "vendor") {
            column(name: "ven_paper_invoice", type: "boolean") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1712929543410-13") {
        addColumn(tableName: "vendor") {
            column(name: "ven_prequalification_vol", type: "boolean") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1712929543410-14") {
        addColumn(tableName: "vendor") {
            column(name: "ven_prequalification_vol_info", type: "varchar(255)")
        }
    }

    changeSet(author: "galffy (generated)", id: "1712929543410-15") {
        addColumn(tableName: "vendor") {
            column(name: "ven_processing_of_compensation_payments", type: "boolean") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1712929543410-16") {
        addColumn(tableName: "vendor") {
            column(name: "ven_research_platform_ebooks", type: "varchar(255)")
        }
    }

    changeSet(author: "galffy (generated)", id: "1712929543410-17") {
        addColumn(tableName: "vendor") {
            column(name: "ven_shipping_metadata", type: "boolean") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1712929543410-18") {
        addColumn(tableName: "vendor") {
            column(name: "ven_technical_support", type: "boolean") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1712929543410-19") {
        addColumn(tableName: "vendor") {
            column(name: "ven_web_shop_orders", type: "boolean") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1712929543410-20") {
        addColumn(tableName: "vendor") {
            column(name: "ven_xml_orders", type: "boolean") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1712929543410-21") {
        addForeignKeyConstraint(baseColumnNames: "eb_invoicing_format_rv_fk", baseTableName: "electronic_billing", constraintName: "FKaenf5ts5rj5je2thdf5au12s6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1712929543410-22") {
        addForeignKeyConstraint(baseColumnNames: "idi_vendor_fk", baseTableName: "invoice_dispatch", constraintName: "FKbqpxl0a200ru1qfdx39au85ox", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ven_id", referencedTableName: "vendor", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1712929543410-23") {
        addForeignKeyConstraint(baseColumnNames: "eddn_delay_notification_rv_fk", baseTableName: "electronic_delivery_delay_notification", constraintName: "FKdrdoisim3samro3ex9xfrsjw0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1712929543410-24") {
        addForeignKeyConstraint(baseColumnNames: "ls_library_system_rv_fk", baseTableName: "library_system", constraintName: "FKe0uigyrsaf6rip0bstoolq38p", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1712929543410-25") {
        addForeignKeyConstraint(baseColumnNames: "ls_vendor_fk", baseTableName: "library_system", constraintName: "FKf55rvw9dmyc8qyyr5plji4ayk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ven_id", referencedTableName: "vendor", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1712929543410-26") {
        addForeignKeyConstraint(baseColumnNames: "eb_vendor_fk", baseTableName: "electronic_billing", constraintName: "FKi4yi8quqdo5tqe4v2db0dwg12", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ven_id", referencedTableName: "vendor", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1712929543410-27") {
        addForeignKeyConstraint(baseColumnNames: "idi_invoice_dispatch_rv_fk", baseTableName: "invoice_dispatch", constraintName: "FKlysh0tqxgdjv0eay4vbv2vwbo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1712929543410-28") {
        addForeignKeyConstraint(baseColumnNames: "eddn_vendor_fk", baseTableName: "electronic_delivery_delay_notification", constraintName: "FKt6o3vmvc2qhedtcjd0cnkt9v9", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ven_id", referencedTableName: "vendor", validate: "true")
    }
}
