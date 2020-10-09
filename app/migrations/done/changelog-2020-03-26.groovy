import de.laser.Platform
import de.laser.titles.TitleInstance
import de.laser.TitleInstancePackagePlatform
import de.laser.YodaService
import de.laser.Package
import de.laser.Subscription
import grails.util.Holders

YodaService yodaService = Holders.grailsApplication.mainContext.getBean('yodaService')

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1585205594968-1") {
        dropForeignKeyConstraint(baseTableName: "cost_item_element_configuration", baseTableSchemaName: "public", constraintName: "fk2d8a6879b47e66a0")
    }

    changeSet(author: "klober (generated)", id: "1585205594968-2") {
        dropForeignKeyConstraint(baseTableName: "cost_item_element_configuration", baseTableSchemaName: "public", constraintName: "fk2d8a68793096044a")
    }

    changeSet(author: "klober (generated)", id: "1585205594968-3") {
        dropForeignKeyConstraint(baseTableName: "links", baseTableSchemaName: "public", constraintName: "fk6234fb9b47e66a0")
    }

    changeSet(author: "klober (generated)", id: "1585205594968-4") {
        dropForeignKeyConstraint(baseTableName: "links", baseTableSchemaName: "public", constraintName: "fk6234fb93096044a")
    }

    changeSet(author: "klober (generated)", id: "1585205594968-5") {
        dropColumn(columnName: "created_by_id", tableName: "cost_item_element_configuration")
    }

    changeSet(author: "klober (generated)", id: "1585205594968-6") {
        dropColumn(columnName: "last_updated_by_id", tableName: "cost_item_element_configuration")
    }

    changeSet(author: "klober (generated)", id: "1585205594968-7") {
        dropColumn(columnName: "created_by_id", tableName: "links")
    }

    changeSet(author: "klober (generated)", id: "1585205594968-8") {
        dropColumn(columnName: "last_updated_by_id", tableName: "links")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-9") {
        createTable(schemaName: "public", tableName: "pending_change_configuration") {
            column(autoIncrement: "true", name: "id", type: "int8") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "pending_changPK")
            }

            column(name: "version", type: "int8") {
                constraints(nullable: "false")
            }

            column(name: "pcc_setting_key_enum", type: "varchar(255)") {
                constraints(nullable: "false")
            }

            column(name: "pcc_setting_value_rv_fk", type: "int8")

            column(name: "pcc_sp_fk", type: "int8") {
                constraints(nullable: "false")
            }

            column(name: "pcc_with_information", type: "bool") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1585205594968-10") {
        grailsChange {
            change {
                sql.execute("""
                insert into refdata_category (rdc_version,rdc_description,rdc_is_hard_data) values (0,'pending.change.configuration.setting',false);
                insert into refdata_value (rdv_version,rdv_owner,rdv_is_hard_data,rdv_value) values (0,(select rdc_id from refdata_category where rdc_description = 'pending.change.configuration.setting'),false,'Accept'),
                (0,(select rdc_id from refdata_category where rdc_description = 'pending.change.configuration.setting'),false,'Prompt'),(0,(select rdc_id from refdata_category where rdc_description = 'pending.change.configuration.setting'),false,'Reject'),
                (0,(select rdc_id from refdata_category where rdc_description = 'pending.change.status'),false,'Superseded');
                """)
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-11") {
        addColumn(schemaName: "public", tableName: "pending_change") {
            column(name: "pc_new_value", type: "text")
        }
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-12") {
        addColumn(schemaName: "public", tableName: "pending_change") {
            column(name: "pc_old_value", type: "text")
        }
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-13") {
        addColumn(schemaName: "public", tableName: "pending_change") {
            column(name: "pc_target_property", type: "text")
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1585205594968-14") {
        grailsChange {
            change {
                //this changeset is HIGHLY EXPLOSIVE, TEST IT EXTENSIVELY BEFORE USE!!!!!!

                //2019-12-06
                //ERMS-1929
                //removing deprecated field impId, move ti_type_rv_fk to ti_medium_rv_fk
                /*
                alter table package drop column pkg_identifier;
                alter table org drop column org_imp_id;
                alter table package drop column pkg_imp_id;
                alter table platform drop column plat_imp_id;
                alter table subscription drop column sub_imp_id;
                alter table title_instance drop column ti_imp_id;
                alter table title_instance_package_platform drop column tipp_imp_id;
                alter table title_instance_package_platform drop column tipp_sub_fk;
                 */
                sql.execute("""
                alter table package rename pkg_type_rv_fk to pkg_content_type_rv_fk;
                alter table title_instance rename ti_type_rv_fk to ti_medium_rv_fk;
                update refdata_value set rdv_value = 'Book' where rdv_value = 'EBook';
                update refdata_category set rdc_description = 'title.medium' where rdc_description = 'title.type';""")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-15") {
        dropForeignKeyConstraint(baseTableName: "core_assertion", baseTableSchemaName: "public", constraintName: "fke48406625ad1eb60")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-16") {
        dropForeignKeyConstraint(baseTableName: "global_record_info", baseTableSchemaName: "public", constraintName: "fkb057c1402753393f")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-17") {
        dropForeignKeyConstraint(baseTableName: "global_record_info", baseTableSchemaName: "public", constraintName: "fkb057c140e1ae5394")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-18") {
        dropForeignKeyConstraint(baseTableName: "global_record_info", baseTableSchemaName: "public", constraintName: "fkb057c14074d2c985")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-19") {
        dropForeignKeyConstraint(baseTableName: "global_record_tracker", baseTableSchemaName: "public", constraintName: "fk808f5966f6287f86")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-20") {
        dropForeignKeyConstraint(baseTableName: "package", baseTableSchemaName: "public", constraintName: "fkcfe5344692580d5f")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-21") {
        dropForeignKeyConstraint(baseTableName: "platformtipp", baseTableSchemaName: "public", constraintName: "fk9544a2810252c57")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-22") {
        dropForeignKeyConstraint(baseTableName: "platformtipp", baseTableSchemaName: "public", constraintName: "fk9544a28c581dd6e")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-23") {
        dropForeignKeyConstraint(baseTableName: "title_instance", baseTableSchemaName: "public", constraintName: "fkacc69c334e5d16")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-24") {
        dropForeignKeyConstraint(baseTableName: "title_instance_package_platform", baseTableSchemaName: "public", constraintName: "fke793fb8f80f6588")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-25") {
        dropForeignKeyConstraint(baseTableName: "title_institution_provider", baseTableSchemaName: "public", constraintName: "fk89a2e01f35702557")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-26") {
        dropForeignKeyConstraint(baseTableName: "title_institution_provider", baseTableSchemaName: "public", constraintName: "fk89a2e01f97876ad4")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-27") {
        dropForeignKeyConstraint(baseTableName: "title_institution_provider", baseTableSchemaName: "public", constraintName: "fk89a2e01f47b4bd3f")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-28") {
        dropIndex(indexName: "tiinp_idx", tableName: "title_institution_provider")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-29") {
        dropTable(tableName: "core_assertion")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-30") {
        dropTable(tableName: "global_record_info")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-31") {
        dropTable(tableName: "global_record_tracker")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-32") {
        dropTable(tableName: "platformtipp")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-33") {
        dropTable(tableName: "title_institution_provider")
    }

    changeSet(author: "galffy (hand-coded)", id: "1585205594968-34") {
        grailsChange {
            change {
                //2019-12-10
                //ERMS-1901 (ERMS-1500)
                //org.name set not null with default "Name fehlt"
                /*
                alter table org alter column org_name set default 'Name fehlt!';
                alter table org alter column org_name set not null;
                 */
                sql.execute("""update org set org_name = 'Name fehlt!' where org_name is null;""")
            }
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1585205594968-35") {
        grailsChange {
            change {
                //2020-02-14
                //ERMS-1901 (ERMS-1957)
                //manually set platform and package data to correct one, drop tables title_institution_platform and core_assertion
                /*
                sql.execute("""drop table core_assertion;""")
                sql.execute("""drop table title_institution_provider;""")
                sql.execute("""drop table global_record_info;""")
                sql.execute("""drop table global_record_tracker;""")
                 */
                //www.degruyter.de in De Gruyter Online
                sql.execute("""update org_access_point_link set platform_id = (select plat_id from platform where plat_guid = 'platform:ac3de90c-cb91-485b-bd13-654424f1a5d1') where platform_id = (select plat_id from platform where plat_guid = 'platform:3768fcd5-2349-411f-8857-10c780cdc488');""")
                //seach.ebscohost.com in EBSCOhost
                sql.execute("""update org_access_point_link set platform_id = (select plat_id from platform where plat_guid = 'platform:3fb4b530-7361-48c0-9aa8-e05b8c43a4d1') where platform_id in (select plat_id from platform where plat_guid in ('platform:b1f175a3-90cb-4d80-9b31-eca99de8c14a','platform:be4001c8-2dda-4ace-8447-e8652ca683c0'));""")
                //CareLit in CareLit Online
                sql.execute("""update org_access_point_link set platform_id = (select plat_id from platform where plat_guid = 'platform:e9ee362f-fd01-4fa4-a456-5e3b52aa2fb5') where platform_id = (select plat_id from platform where plat_guid = 'platform:ddb03c2a-7732-4413-ad51-3f598a9d0db2');""")
                //Web of Science in Web of Science
                sql.execute("""update org_access_point_link set platform_id = (select plat_id from platform where plat_guid = 'platform:75239d58-f7e7-43ce-851b-cf2ee4051e45') where platform_id = (select plat_id from platform where plat_guid = 'platform:6013c248-50c8-4ccb-9375-4a79dbc9e495');""")
            }
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1585205594968-36") {
        grailsChange {
            change {
                try {
                    println("Starting with Packages")
                    Package.withNewSession {
                        Map packageDuplicates = yodaService.listDuplicatePackages()
                        List<Long> toDelete = []
                        toDelete.addAll(packageDuplicates.pkgDupsWithoutTipps.collect { dup -> dup.id })
                        packageDuplicates.pkgDupsWithTipps.each { dup ->
                            List<Subscription> concernedSubs = Subscription.executeQuery('select distinct(ie.subscription) from IssueEntitlement ie join ie.tipp tipp where tipp.pkg = :pkg', [pkg: dup])
                            if (!concernedSubs)
                                toDelete << dup.id
                        }
                        yodaService.executePackageCleanup(toDelete)
                    }
                }
                catch (Exception e) {
                    e.printStackTrace()
                }
            }
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1585205594968-37") {
        grailsChange {
            change {
                try {
                    println("to TitleInstances")
                    TitleInstance.withNewSession {
                        yodaService.executeTiCleanup(yodaService.listDuplicateTitles())
                    }
                }
                catch (Exception e) {
                    e.printStackTrace()
                }
            }
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1585205594968-38") {
        grailsChange {
            change {
                try {
                    println("to TIPPs")
                    TitleInstancePackagePlatform.withNewSession {
                        yodaService.executeTIPPCleanup(yodaService.listDeletedTIPPs())
                    }
                }
                catch (Exception e) {
                    e.printStackTrace()
                }
            }
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1585205594968-39") {
        grailsChange {
            change {
                try {
                    println("to Platforms")
                    Platform.withNewSession {
                        yodaService.executePlatformCleanup(yodaService.listPlatformDuplicates())
                    }
                }
                catch (Exception e) {
                    e.printStackTrace()
                }
            }
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1585205594968-40") {
        grailsChange {
            change {
                //2020-01-23
                //ERMS-1901 (ERMS-1948)
                //cleanup - set gokbId as unique and not null, delete erroneous coverage data from ebooks and databases, delete column package_type_rv_fk
                /*
                ALTER TABLE title_instance ALTER COLUMN ti_gokb_id TYPE character varying(511);
                alter table title_instance alter column ti_gokb_id set not null;
                alter table title_instance add constraint unique_ti_gokb_id unique (ti_gokb_id);
                alter table title_instance_package_platform alter column tipp_gokb_id type character varying(511);
                alter table title_instance_package_platform alter column tipp_gokb_id set not null;
                alter table title_instance_package_platform ADD CONSTRAINT unique_tipp_gokb_id UNIQUE (tipp_gokb_id);
                alter table package alter column pkg_gokb_id type character varying(511);
                alter table package alter column pkg_gokb_id set not null;
                alter table package ADD CONSTRAINT unique_pkg_gokb_id UNIQUE (pkg_gokb_id);
                alter table platform alter column plat_gokb_id type character varying(511);
                alter table platform alter column plat_gokb_id set not null;
                alter table platform ADD CONSTRAINT unique_plat_gokb_id UNIQUE (plat_gokb_id);
                 */
                sql.execute("""delete from issue_entitlement_coverage where ic_ie_fk in (select ie_id from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id join title_instance ti on tipp_ti_fk = ti_id where class not like '%JournalInstance%');
                update title_instance_package_platform set tipp_gokb_id = concat('generic.null.value.',tipp_id) where tipp_gokb_id is null;
                update package set pkg_gokb_id = concat('generic.null.value',pkg_id) where pkg_gokb_id is null;""")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-41") {
        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "org_name", tableName: "org")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-42") {
        modifyDataType(columnName: "pkg_gokb_id", newDataType: "varchar(511)", tableName: "package")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-43") {
        addNotNullConstraint(columnDataType: "varchar(511)", columnName: "pkg_gokb_id", tableName: "package")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-44") {
        modifyDataType(columnName: "plat_gokb_id", newDataType: "text", tableName: "platform")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-45") {
        addNotNullConstraint(columnDataType: "text", columnName: "plat_gokb_id", tableName: "platform")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-46") {
        modifyDataType(columnName: "ti_gokb_id", newDataType: "varchar(511)", tableName: "title_instance")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-47") {
        addNotNullConstraint(columnDataType: "varchar(511)", columnName: "ti_gokb_id", tableName: "title_instance")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-48") {
        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "ti_guid", tableName: "title_instance")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-49") {
        modifyDataType(columnName: "tipp_gokb_id", newDataType: "varchar(511)", tableName: "title_instance_package_platform")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-50") {
        addNotNullConstraint(columnDataType: "varchar(511)", columnName: "tipp_gokb_id", tableName: "title_instance_package_platform")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-51") {
        dropIndex(indexName: "org_imp_id_idx", tableName: "org")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-52") {
        dropIndex(indexName: "pkg_imp_id_idx", tableName: "package")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-53") {
        dropIndex(indexName: "plat_imp_id_idx", tableName: "platform")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-54") {
        dropIndex(indexName: "sub_imp_id_idx", tableName: "subscription")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-55") {
        dropIndex(indexName: "ti_imp_id_idx", tableName: "title_instance")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-56") {
        dropIndex(indexName: "tipp_imp_id_idx", tableName: "title_instance_package_platform")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-57") {
        createIndex(indexName: "pkg_gokb_id_uniq_1582107700215", schemaName: "public", tableName: "package", unique: "true") {
            column(name: "pkg_gokb_id")
        }
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-58") {
        createIndex(indexName: "plat_gokb_id_uniq_1582107700219", schemaName: "public", tableName: "platform", unique: "true") {
            column(name: "plat_gokb_id")
        }
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-59") {
        createIndex(indexName: "ti_gokb_id_idx", schemaName: "public", tableName: "title_instance") {
            column(name: "ti_gokb_id")
        }
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-60") {
        createIndex(indexName: "ti_gokb_id_uniq_1582107700241", schemaName: "public", tableName: "title_instance", unique: "true") {
            column(name: "ti_gokb_id")
        }
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-61") {
        createIndex(indexName: "tipp_gokb_id_uniq_1582107700242", schemaName: "public", tableName: "title_instance_package_platform", unique: "true") {
            column(name: "tipp_gokb_id")
        }
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-62") {
        dropColumn(columnName: "imp_id", tableName: "license")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-63") {
        dropColumn(columnName: "org_imp_id", tableName: "org")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-64") {
        dropColumn(columnName: "pkg_identifier", tableName: "package")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-65") {
        dropColumn(columnName: "pkg_imp_id", tableName: "package")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-66") {
        dropColumn(columnName: "plat_imp_id", tableName: "platform")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-67") {
        dropColumn(columnName: "sub_imp_id", tableName: "subscription")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-68") {
        dropColumn(columnName: "ti_imp_id", tableName: "title_instance")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-69") {
        dropColumn(columnName: "tipp_imp_id", tableName: "title_instance_package_platform")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-70") {
        dropColumn(columnName: "tipp_sub_fk", tableName: "title_instance_package_platform")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-71") {
        addForeignKeyConstraint(baseColumnNames: "pkg_content_type_rv_fk", baseTableName: "package", baseTableSchemaName: "public", constraintName: "FKCFE534465251D5E5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-72") {
        addForeignKeyConstraint(baseColumnNames: "pcc_setting_value_rv_fk", baseTableName: "pending_change_configuration", baseTableSchemaName: "public", constraintName: "FK30076C4F74CFA8A5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
    }

    changeSet(author: "galffy (generated)", id: "1585205594968-73") {
        addForeignKeyConstraint(baseColumnNames: "pcc_sp_fk", baseTableName: "pending_change_configuration", baseTableSchemaName: "public", constraintName: "FK30076C4F3315F684", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sp_id", referencedTableName: "subscription_package", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
    }
}
