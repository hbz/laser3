package changelogs

import de.laser.GlobalService
import de.laser.IssueEntitlement
import de.laser.storage.RDStore
import groovy.sql.GroovyRowResult
import groovy.sql.Sql

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1688988863186-1") {
        grailsChange {
            change {
                Sql storageSql = GlobalService.obtainStorageSqlConnection()
                List<GroovyRowResult> delIEs = sql.rows("select ie_id as \"dbId\", " +
                        "ie_version as version, " +
                        "'"+ IssueEntitlement.class.name+"' as \"objectType\"," +
                        "(select sub_guid from subscription where ie_subscription_fk = sub_id) as \"subGuid\", " +
                        "coalesce(ie_date_created, now()) as \"oldDateCreated\", " +
                        "coalesce(ie_last_updated, ie_date_created, now()) as \"oldLastUpdated\", " +
                        "(select tipp_gokb_id from title_instance_package_platform where ie_tipp_fk = tipp_id) as \"titleWekbId\"," +
                        "(select tipp_name from title_instance_package_platform where ie_tipp_fk = tipp_id) as name," +
                        "(select pkg_gokb_id from title_instance_package_platform join package on tipp_pkg_fk = pkg_id where ie_tipp_fk = tipp_id) as \"pkgWekbId\"" +
                        "from issue_entitlement where ie_status_rv_fk = :removed", [removed: RDStore.TIPP_STATUS_REMOVED.id])
                delIEs.each { GroovyRowResult row ->
                    storageSql.withBatch(200000, 'insert into deleted_object (do_version, do_ref_subscription_uid, do_old_date_created, do_old_last_updated, do_old_object_type, do_date_created, do_ref_title_wekb_id, do_last_updated, do_ref_package_wekb_id, do_old_database_id, do_old_name) values (:version, :subGuid, :oldDateCreated, :oldLastUpdated, :objectType, now(), :titleWekbId, now(), :pkgWekbId, :dbId, :name)') {stmt ->
                        stmt.addBatch(row)
                    }
                }
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1688988863186-2") {
        createTable(tableName: "issue_entitlement_cleared") {
            column(autoIncrement: "true", name: "ie_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "issue_entitlementPK")
            }

            column(name: "ie_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "ie_date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "ie_perpetual_access_by_sub_fk", type: "BIGINT")

            column(name: "ie_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE")

            column(name: "ie_notes", type: "TEXT")

            column(name: "ie_access_start_date", type: "TIMESTAMP WITHOUT TIME ZONE")

            column(name: "ie_access_end_date", type: "TIMESTAMP WITHOUT TIME ZONE")

            column(name: "ie_tipp_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "ie_subscription_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "ie_guid", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "galffy (modified)", id: "1688988863186-3") {
        dropUniqueConstraint(constraintName: "issue_entitlement_ie_guid_key", tableName: "issue_entitlement")
    }

    changeSet(author: "galffy (modified)", id: "1688988863186-4") {
        addUniqueConstraint(columnNames: "ie_guid", constraintName: "issue_entitlement_ie_guid_key", tableName: "issue_entitlement_cleared")
    }

    changeSet(author: "galffy (hand-coded)", id: "1688988863186-5") {
        grailsChange {
            change {
                Sql sql = GlobalService.obtainSqlConnection()
                sql.executeInsert('insert into issue_entitlement_cleared select ie_id, ie_version, ie_date_created, ie_perpetual_access_by_sub_fk, ie_last_updated, ie_notes, ie_access_start_date, ie_access_end_date, ie_tipp_fk, ie_subscription_fk, ie_guid from issue_entitlement where ie_status_rv_fk != :removed', [removed: RDStore.TIPP_STATUS_REMOVED.id])
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1688988863186-6") {
        dropIndex(indexName: "ie_perpetual_access_by_sub_idx", tableName: "issue_entitlement")
    }

    changeSet(author: "galffy (modified)", id: "1688988863186-7") {
        dropIndex(indexName: "ie_sub_idx", tableName: "issue_entitlement")
    }

    changeSet(author: "galffy (modified)", id: "1688988863186-8") {
        dropIndex(indexName: "ie_sub_tipp_idx", tableName: "issue_entitlement")
    }

    changeSet(author: "galffy (modified)", id: "1688988863186-9") {
        dropIndex(indexName: "ie_tipp_idx", tableName: "issue_entitlement")
    }

    changeSet(author: "galffy (modified)", id: "1688988863186-10") {
        createIndex(indexName: "ie_perpetual_access_by_sub_idx", tableName: "issue_entitlement_cleared") {
            column(name: "ie_perpetual_access_by_sub_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1688988863186-11") {
        createIndex(indexName: "ie_sub_idx", tableName: "issue_entitlement_cleared") {
            column(name: "ie_subscription_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1688988863186-12") {
        createIndex(indexName: "ie_sub_tipp_idx", tableName: "issue_entitlement_cleared") {
            column(name: "ie_tipp_fk")

            column(name: "ie_subscription_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1688988863186-13") {
        createIndex(indexName: "ie_tipp_idx", tableName: "issue_entitlement_cleared") {
            column(name: "ie_tipp_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1688988863186-14") {
        dropForeignKeyConstraint(baseTableName: "permanent_title", constraintName: "FKdgopfa6r885u5oy3l9k9pm2j6")
    }

    changeSet(author: "galffy (modified)", id: "1688988863186-15") {
        dropForeignKeyConstraint(baseTableName: "issue_entitlement", constraintName: "FKgnwnhaj7fnllowveafnqpxwuk")
    }

    changeSet(author: "galffy (modified)", id: "1688988863186-16") {
        dropForeignKeyConstraint(baseTableName: "issue_entitlement", constraintName: "fk2d45f6c72f4a207")
    }

    changeSet(author: "galffy (modified)", id: "1688988863186-17") {
        dropForeignKeyConstraint(baseTableName: "issue_entitlement", constraintName: "fk2d45f6c7330b4f5")
    }

    changeSet(author: "galffy (modified)", id: "1688988863186-18") {
        dropForeignKeyConstraint(baseTableName: "issue_entitlement", constraintName: "fk2d45f6c775f8181e")
    }

    changeSet(author: "galffy (modified)", id: "1688988863186-19") {
        dropForeignKeyConstraint(baseTableName: "issue_entitlement_coverage", constraintName: "fk58936060e776f474")
    }

    changeSet(author: "galffy (modified)", id: "1688988863186-20") {
        dropForeignKeyConstraint(baseTableName: "issue_entitlement_group_item", constraintName: "fk82e6a14b252b85a5")
    }

    changeSet(author: "galffy (modified)", id: "1688988863186-21") {
        dropForeignKeyConstraint(baseTableName: "price_item", constraintName: "fka8c4e849fc049213")
    }

    changeSet(author: "galffy (modified)", id: "1688988863186-22") {
        dropForeignKeyConstraint(baseTableName: "cost_item", constraintName: "fkefe45c455c9f1829")
    }

    changeSet(author: "galffy (modified)", id: "1688988863186-23") {
        addForeignKeyConstraint(baseColumnNames: "ie_tipp_fk", baseTableName: "issue_entitlement_cleared", constraintName: "FKa9mk7rtlbvb2g12r0i3evmjgq", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "tipp_id", referencedTableName: "title_instance_package_platform", validate: "true")
    }

    changeSet(author: "galffy (hand-coded)", id: "1688988863186-24") {
        grailsChange {
            change {
                sql.execute('delete from permanent_title where pt_ie_fk in (select ie_id from issue_entitlement where ie_status_rv_fk = :removed)',[removed: RDStore.TIPP_STATUS_REMOVED.id])
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1688988863186-25") {
        addForeignKeyConstraint(baseColumnNames: "pt_ie_fk", baseTableName: "permanent_title", constraintName: "FKdgopfa6r885u5oy3l9k9pm2j6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ie_id", referencedTableName: "issue_entitlement_cleared", validate: "true")
    }

    changeSet(author: "galffy (hand-coded)", id: "1688988863186-26") {
        grailsChange {
            change {
                sql.execute('delete from price_item where pi_ie_fk in (select ie_id from issue_entitlement where ie_status_rv_fk = :removed)',[removed: RDStore.TIPP_STATUS_REMOVED.id])
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1688988863186-27") {
        addForeignKeyConstraint(baseColumnNames: "pi_ie_fk", baseTableName: "price_item", constraintName: "FKg2mtnhss5mxf7kox7e4vv748y", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ie_id", referencedTableName: "issue_entitlement_cleared", validate: "true")
    }

    changeSet(author: "galffy (modified)", id: "1688988863186-28") {
        addForeignKeyConstraint(baseColumnNames: "ie_subscription_fk", baseTableName: "issue_entitlement_cleared", constraintName: "FKglfidxueej4do626xvq7hpbcv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sub_id", referencedTableName: "subscription", validate: "true")
    }

    changeSet(author: "galffy (modified)", id: "1688988863186-29") {
        addForeignKeyConstraint(baseColumnNames: "ie_perpetual_access_by_sub_fk", baseTableName: "issue_entitlement_cleared", constraintName: "FKgnwnhaj7fnllowveafnqpxwuk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sub_id", referencedTableName: "subscription", validate: "true")
    }

    changeSet(author: "galffy (hand-coded)", id: "1688988863186-30") {
        grailsChange {
            change {
                sql.execute('delete from issue_entitlement_group_item where igi_ie_fk in (select ie_id from issue_entitlement where ie_status_rv_fk = :removed)',[removed: RDStore.TIPP_STATUS_REMOVED.id])
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1688988863186-31") {
        addForeignKeyConstraint(baseColumnNames: "igi_ie_fk", baseTableName: "issue_entitlement_group_item", constraintName: "FKmqpyva7bou6eivbq4k8mypt37", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ie_id", referencedTableName: "issue_entitlement_cleared", validate: "true")
    }

    changeSet(author: "galffy (hand-coded)", id: "1688988863186-32") {
        grailsChange {
            change {
                sql.execute('update cost_item set ci_e_fk = null where ci_e_fk in (select ie_id from issue_entitlement where ie_status_rv_fk = :removed)',[removed: RDStore.TIPP_STATUS_REMOVED.id])
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1688988863186-33") {
        addForeignKeyConstraint(baseColumnNames: "ci_e_fk", baseTableName: "cost_item", constraintName: "FKq46jogu1754qx1kxh5vbrahqp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ie_id", referencedTableName: "issue_entitlement_cleared", validate: "true")
    }

    changeSet(author: "galffy (hand-coded)", id: "1688988863186-34") {
        grailsChange {
            change {
                sql.execute('delete from issue_entitlement_coverage where ic_ie_fk in (select ie_id from issue_entitlement where ie_status_rv_fk = :removed)',[removed: RDStore.TIPP_STATUS_REMOVED.id])
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1688988863186-35") {
        addForeignKeyConstraint(baseColumnNames: "ic_ie_fk", baseTableName: "issue_entitlement_coverage", constraintName: "FKtbi3rlfhm442exhufymkomru6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ie_id", referencedTableName: "issue_entitlement_cleared", validate: "true")
    }

    changeSet(author: "galffy (modified)", id: "1688988863186-36") {
        dropTable(tableName: "issue_entitlement")
    }

    changeSet(author: "galffy (hand-coded)", id: "1688988863186-37") {
        grailsChange {
            change {
                sql.execute('alter table issue_entitlement_cleared rename to issue_entitlement')
            }
            rollback {}
        }
    }
}
