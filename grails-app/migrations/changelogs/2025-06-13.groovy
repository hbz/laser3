package changelogs

databaseChangeLog = {

    changeSet(author: "djebeniani (generated)", id: "1749819913867-1") {
        addColumn(tableName: "survey_org") {
            column(name: "surorg_peppol_receiver_id", type: "varchar(255)")
        }
    }
}
