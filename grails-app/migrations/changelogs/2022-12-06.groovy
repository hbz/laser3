package changelogs

databaseChangeLog = {

    changeSet(author: "djebeniani (generated)", id: "1670359606968-1") {
        createTable(tableName: "survey_url") {
            column(autoIncrement: "true", name: "surur_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "survey_urlPK")
            }

            column(name: "surur_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "surur_date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "surur_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "surur_url", type: "VARCHAR(512)")

            column(name: "surur_url_comment", type: "TEXT")

            column(name: "surur_survey_config_fk", type: "BIGINT")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1670359606968-2") {
        addForeignKeyConstraint(baseColumnNames: "surur_survey_config_fk", baseTableName: "survey_url", constraintName: "FKbjuvr8nxkwgmr9q5sphun7bga", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "surconf_id", referencedTableName: "survey_config", validate: "true")
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1670359606968-3") {
        grailsChange {
            change {
                sql.executeInsert("insert into survey_url (surur_version, surur_date_created, surur_last_updated, surur_url, surur_url_comment, surur_survey_config_fk) (select 0, now(), now(), surconf_url, surconf_url_comment, surconf_id from survey_config where surconf_url is not null or surconf_url != '')")
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1670359606968-4") {
        grailsChange {
            change {
                sql.executeInsert("insert into survey_url (surur_version, surur_date_created, surur_last_updated, surur_url, surur_url_comment, surur_survey_config_fk) (select 0, now(), now(), surconf_url_2, surconf_url_comment_2, surconf_id from survey_config where surconf_url_2 is not null or surconf_url_2 != '')")
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1670359606968-5") {
        grailsChange {
            change {
                sql.executeInsert("insert into survey_url (surur_version, surur_date_created, surur_last_updated, surur_url, surur_url_comment, surur_survey_config_fk) (select 0, now(), now(), surconf_url_3, surconf_url_comment_3, surconf_id from survey_config where surconf_url_3 is not null or surconf_url_3 != '')")
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1670359606968-6") {
        dropColumn(columnName: "surconf_url", tableName: "survey_config")
    }

    changeSet(author: "djebeniani (generated)", id: "1670359606968-7") {
        dropColumn(columnName: "surconf_url_2", tableName: "survey_config")
    }

    changeSet(author: "djebeniani (generated)", id: "1670359606968-8") {
        dropColumn(columnName: "surconf_url_3", tableName: "survey_config")
    }

    changeSet(author: "djebeniani (generated)", id: "1670359606968-9") {
        dropColumn(columnName: "surconf_url_comment", tableName: "survey_config")
    }

    changeSet(author: "djebeniani (generated)", id: "1670359606968-10") {
        dropColumn(columnName: "surconf_url_comment_2", tableName: "survey_config")
    }

    changeSet(author: "djebeniani (generated)", id: "1670359606968-11") {
        dropColumn(columnName: "surconf_url_comment_3", tableName: "survey_config")
    }

    changeSet(author: "klober (generated)", id: "1670338391348-12") {
        dropForeignKeyConstraint(baseTableName: "doc_context", constraintName: "fk30eba9a858752a7e")
    }

    changeSet(author: "klober (generated)", id: "1670338391348-13") {
        dropColumn(columnName: "dc_rv_doctype_fk", tableName: "doc_context")
    }
}
