databaseChangeLog = {

    changeSet(author: "djebeniani (generated)", id: "1631214032993-1") {
        addColumn(tableName: "survey_config") {
            column(name: "surconf_pac_perpetualaccess", type: "boolean") {
            }
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1631214032993-2") {
        dropColumn(columnName: "surconf_create_title_groups", tableName: "survey_config")
    }

    changeSet(author: "djebeniani (modified)", id: "1631214032993-3") {
        grailsChange {
            change {
                sql.execute('update survey_config set surconf_pac_perpetualaccess = false where surconf_pac_perpetualaccess is null')
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1631214032993-4") {
        addColumn(tableName: "subscription") {
            column(name: "sub_is_automatic_renew_annually", type: "boolean") {
            }
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1631214032993-5") {
        grailsChange {
            change {
                sql.execute('update subscription set sub_is_automatic_renew_annually = false where sub_is_automatic_renew_annually is null')
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1631214032993-6") {
        addColumn(tableName: "issue_entitlement") {
            column(name: "ie_has_perpetual_access", type: "boolean") {
            }
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1631214032993-7") {
        grailsChange {
            change {
                sql.execute('update issue_entitlement set ie_has_perpetual_access = false where ie_has_perpetual_access is null')
            }
            rollback {}
        }
    }

}
