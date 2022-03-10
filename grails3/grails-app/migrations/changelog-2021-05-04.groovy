databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1620118196310-1") {
        grailsChange {
            change {
                sql.execute("delete from subscription_property where sp_type_fk in (select pd_id from property_definition join property_definition_group_item on pde_property_definition_fk = pd_id where pd_tenant_fk is not null);")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1620118196310-2") {
        grailsChange {
            change {
                sql.execute("delete from license_property where lp_type_fk in (select pd_id from property_definition join property_definition_group_item on pde_property_definition_fk = pd_id where pd_tenant_fk is not null);")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1620118196310-3") {
        grailsChange {
            change {
                sql.execute("delete from org_property where op_type_fk in (select pd_id from property_definition join property_definition_group_item on pde_property_definition_fk = pd_id where pd_tenant_fk is not null);")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1620118196310-4") {
        grailsChange {
            change {
                sql.execute("delete from property_definition_group_item using property_definition where pde_property_definition_fk = pd_id and pd_tenant_fk is not null;")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (generated)", id: "1620118196310-5") {
        createTable(tableName: "dewey_decimal_classification") {
            column(autoIncrement: "true", name: "ddc_id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "dewey_decimal_classificationPK")
            }

            column(name: "ddc_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "ddc_tipp_fk", type: "BIGINT")

            column(name: "ddc_date_created", type: "TIMESTAMP WITHOUT TIME ZONE")

            column(name: "ddc_last_updated_cascading", type: "TIMESTAMP WITHOUT TIME ZONE")

            column(name: "ddc_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE")

            column(name: "ddc_rv_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "ddc_pkg_fk", type: "BIGINT")
        }
    }

    changeSet(author: "galffy (generated)", id: "1620118196310-6") {
        addForeignKeyConstraint(baseColumnNames: "ddc_rv_fk", baseTableName: "dewey_decimal_classification", constraintName: "FK2qu63m65t7uv3lwlc1lyn1gno", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "galffy (generated)", id: "1620118196310-7") {
        addForeignKeyConstraint(baseColumnNames: "ddc_pkg_fk", baseTableName: "dewey_decimal_classification", constraintName: "FK7jf0u2n5lfegx9y76f43ruvas", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pkg_id", referencedTableName: "package")
    }

    changeSet(author: "galffy (generated)", id: "1620118196310-8") {
        addForeignKeyConstraint(baseColumnNames: "ddc_tipp_fk", baseTableName: "dewey_decimal_classification", constraintName: "FKotxyn634jqtw9x4jxv6kicr4h", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "tipp_id", referencedTableName: "title_instance_package_platform")
    }

}
