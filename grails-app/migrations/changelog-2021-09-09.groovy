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
}
