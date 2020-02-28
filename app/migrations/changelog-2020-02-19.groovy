import com.k_int.kbplus.Platform
import com.k_int.kbplus.TitleInstance
import com.k_int.kbplus.TitleInstancePackagePlatform
import de.laser.YodaService
import com.k_int.kbplus.Package
import com.k_int.kbplus.Subscription
import grails.util.Holders

YodaService yodaService = Holders.grailsApplication.mainContext.getBean('yodaService')

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1582107705616-1") {
        addColumn(schemaName: "public", tableName: "subscription") {
            column(name: "sub_has_perpetual_access", type: "bool")
        }
    }

    changeSet(author: "galffy (modified)", id: "1582107705616-2") {
        grailsChange {
            change {
                sql.execute("update subscription set sub_has_perpetual_access=false where sub_has_perpetual_access is null;")
            }
            rollback {
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-3") {
        addNotNullConstraint(columnDataType: "bool", columnName: "sub_has_perpetual_access", tableName: "subscription")
    }

    changeSet(author: "galffy (hand-coded)", id: "1582107705616-4") {
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

    changeSet(author: "galffy (generated)", id: "1582107705616-5") {
        dropForeignKeyConstraint(baseTableName: "core_assertion", baseTableSchemaName: "public", constraintName: "fke48406625ad1eb60")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-6") {
        dropForeignKeyConstraint(baseTableName: "global_record_info", baseTableSchemaName: "public", constraintName: "fkb057c1402753393f")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-7") {
        dropForeignKeyConstraint(baseTableName: "global_record_info", baseTableSchemaName: "public", constraintName: "fkb057c140e1ae5394")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-8") {
        dropForeignKeyConstraint(baseTableName: "global_record_info", baseTableSchemaName: "public", constraintName: "fkb057c14074d2c985")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-9") {
        dropForeignKeyConstraint(baseTableName: "global_record_tracker", baseTableSchemaName: "public", constraintName: "fk808f5966f6287f86")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-10") {
        dropForeignKeyConstraint(baseTableName: "package", baseTableSchemaName: "public", constraintName: "fkcfe5344692580d5f")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-11") {
        dropForeignKeyConstraint(baseTableName: "platformtipp", baseTableSchemaName: "public", constraintName: "fk9544a2810252c57")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-12") {
        dropForeignKeyConstraint(baseTableName: "platformtipp", baseTableSchemaName: "public", constraintName: "fk9544a28c581dd6e")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-13") {
        dropForeignKeyConstraint(baseTableName: "title_instance", baseTableSchemaName: "public", constraintName: "fkacc69c334e5d16")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-14") {
        dropForeignKeyConstraint(baseTableName: "title_instance_package_platform", baseTableSchemaName: "public", constraintName: "fke793fb8f80f6588")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-15") {
        dropForeignKeyConstraint(baseTableName: "title_institution_provider", baseTableSchemaName: "public", constraintName: "fk89a2e01f35702557")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-16") {
        dropForeignKeyConstraint(baseTableName: "title_institution_provider", baseTableSchemaName: "public", constraintName: "fk89a2e01f97876ad4")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-17") {
        dropForeignKeyConstraint(baseTableName: "title_institution_provider", baseTableSchemaName: "public", constraintName: "fk89a2e01f47b4bd3f")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-18") {
        dropIndex(indexName: "tiinp_idx", tableName: "title_institution_provider")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-19") {
        dropTable(tableName: "core_assertion")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-20") {
        dropTable(tableName: "global_record_info")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-21") {
        dropTable(tableName: "global_record_tracker")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-22") {
        dropTable(tableName: "platformtipp")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-23") {
        dropTable(tableName: "title_institution_provider")
    }

    changeSet(author: "galffy (hand-coded)", id: "1582107705616-24") {
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

    changeSet(author: "galffy (hand-coded)", id: "1582107705616-25") {
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
                sql.execute("""update org_access_point_link set platform_id = 5 where platform_id = 27;""")
                //seach.ebscohost.com in EBSCOhost
                sql.execute("""update org_access_point_link set platform_id = 12 where platform_id in (30,59);""")
                //CareLit in CareLit Online
                sql.execute("""update org_access_point_link set platform_id = 98 where platform_id = 1;""")
                //Web of Science in Web of Science
                sql.execute("""update org_access_point_link set platform_id = 8 where platform_id = 62;""")
            }
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1582107705616-26") {
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

    changeSet(author: "galffy (hand-coded)", id: "1582107705616-27") {
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

    changeSet(author: "galffy (hand-coded)", id: "1582107705616-28") {
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

    changeSet(author: "galffy (hand-coded)", id: "1582107705616-29") {
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

    changeSet(author: "galffy (hand-coded)", id: "1582107705616-30") {
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

    changeSet(author: "galffy (generated)", id: "1582107705616-31") {
        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "org_name", tableName: "org")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-32") {
        modifyDataType(columnName: "pkg_gokb_id", newDataType: "varchar(511)", tableName: "package")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-33") {
        addNotNullConstraint(columnDataType: "varchar(511)", columnName: "pkg_gokb_id", tableName: "package")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-34") {
        modifyDataType(columnName: "plat_gokb_id", newDataType: "text", tableName: "platform")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-35") {
        addNotNullConstraint(columnDataType: "text", columnName: "plat_gokb_id", tableName: "platform")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-36") {
        modifyDataType(columnName: "ti_gokb_id", newDataType: "varchar(511)", tableName: "title_instance")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-37") {
        addNotNullConstraint(columnDataType: "varchar(511)", columnName: "ti_gokb_id", tableName: "title_instance")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-38") {
        addNotNullConstraint(columnDataType: "varchar(255)", columnName: "ti_guid", tableName: "title_instance")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-39") {
        modifyDataType(columnName: "tipp_gokb_id", newDataType: "varchar(511)", tableName: "title_instance_package_platform")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-40") {
        addNotNullConstraint(columnDataType: "varchar(511)", columnName: "tipp_gokb_id", tableName: "title_instance_package_platform")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-41") {
        dropIndex(indexName: "org_imp_id_idx", tableName: "org")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-42") {
        dropIndex(indexName: "pkg_imp_id_idx", tableName: "package")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-43") {
        dropIndex(indexName: "plat_imp_id_idx", tableName: "platform")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-44") {
        dropIndex(indexName: "sub_imp_id_idx", tableName: "subscription")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-45") {
        dropIndex(indexName: "ti_imp_id_idx", tableName: "title_instance")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-46") {
        dropIndex(indexName: "tipp_imp_id_idx", tableName: "title_instance_package_platform")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-47") {
        createIndex(indexName: "pkg_gokb_id_uniq_1582107700215", schemaName: "public", tableName: "package", unique: "true") {
            column(name: "pkg_gokb_id")
        }
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-48") {
        createIndex(indexName: "plat_gokb_id_uniq_1582107700219", schemaName: "public", tableName: "platform", unique: "true") {
            column(name: "plat_gokb_id")
        }
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-49") {
        createIndex(indexName: "ti_gokb_id_idx", schemaName: "public", tableName: "title_instance") {
            column(name: "ti_gokb_id")
        }
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-50") {
        createIndex(indexName: "ti_gokb_id_uniq_1582107700241", schemaName: "public", tableName: "title_instance", unique: "true") {
            column(name: "ti_gokb_id")
        }
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-51") {
        createIndex(indexName: "tipp_gokb_id_uniq_1582107700242", schemaName: "public", tableName: "title_instance_package_platform", unique: "true") {
            column(name: "tipp_gokb_id")
        }
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-52") {
        dropColumn(columnName: "imp_id", tableName: "license")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-53") {
        dropColumn(columnName: "org_imp_id", tableName: "org")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-54") {
        dropColumn(columnName: "pkg_identifier", tableName: "package")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-55") {
        dropColumn(columnName: "pkg_imp_id", tableName: "package")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-56") {
        dropColumn(columnName: "plat_imp_id", tableName: "platform")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-57") {
        dropColumn(columnName: "sub_imp_id", tableName: "subscription")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-58") {
        dropColumn(columnName: "ti_imp_id", tableName: "title_instance")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-59") {
        dropColumn(columnName: "tipp_imp_id", tableName: "title_instance_package_platform")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-60") {
        dropColumn(columnName: "tipp_sub_fk", tableName: "title_instance_package_platform")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-61") {
        addForeignKeyConstraint(baseColumnNames: "pkg_content_type_rv_fk", baseTableName: "package", baseTableSchemaName: "public", constraintName: "FKCFE534465251D5E5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
    }

    changeSet(author: "galffy (generated)", id: "1582107705616-62") {
        dropTable(tableName: "delete_me")
    }
}
