package changelogs

databaseChangeLog = {

    changeSet(author: "djebeniani (generated)", id: "1741258146941-1") {
        dropColumn(columnName: "surconf_scheduled_enddate", tableName: "survey_config")
    }

    changeSet(author: "djebeniani (generated)", id: "1741258146941-2") {
        dropColumn(columnName: "surconf_scheduled_startdate", tableName: "survey_config")
    }
}
