databaseChangeLog = {

    /*changeSet(author: "galffy (hand-coded)", id: "1617885051000-1") {
        grailsChange {
            change {
                sql.execute("ALTER TABLE subscription ALTER COLUMN sub_has_perpetual_access DROP NOT NULL;")
            }
            rollback {}
        }
    }*/

    /*changeSet(author: "galffy (hand-coded)", id: "1617885051000-2") {
        grailsChange {
            change {
                sql.execute("ALTER TABLE subscription ALTER sub_has_perpetual_access SET DEFAULT null;")
            }
            rollback {}
        }
    }*/

    /*changeSet(author: "galffy (hand-coded)", id: "1617885051000-3") {
        grailsChange {
            change {
                sql.execute("ALTER TABLE subscription ALTER sub_has_perpetual_access TYPE INTEGER USING CASE WHEN sub_has_perpetual_access = true THEN 1 ELSE null END;")
            }
            rollback {}
        }
    }*/

    /*changeSet(author: "galffy (hand-coded)", id: "1617885051000-4") {
        grailsChange {
            change {
                sql.execute("ALTER TABLE subscription RENAME sub_has_perpetual_access TO sub_has_perpetual_access_rv_fk;")
            }
            rollback {}
        }
    }*/

    /*changeSet(author: "galffy (generated)", id: "1617885051000-5") {
        addForeignKeyConstraint(baseColumnNames: "sub_has_perpetual_access_rv_fk", baseTableName: "subscription", constraintName: "FKbb09hlijj99er1oag0g77gu4l", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }*/

    changeSet(author: "galffy (modified)", id: "1617885051000-6") {
        addColumn(tableName: "subscription") {
            column(name: "sub_has_publish_component", type: "boolean")
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1617885051000-7") {
        grailsChange {
            change {
                sql.execute("update subscription set sub_has_publish_component = false")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1617885051000-8") {
        grailsChange {
            change {
                sql.execute("ALTER TABLE subscription ALTER COLUMN sub_has_publish_component SET DEFAULT False")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1617885051000-9") {
        addNotNullConstraint(columnDataType: "bool", columnName: "sub_has_publish_component", tableName: "subscription")
    }
}
