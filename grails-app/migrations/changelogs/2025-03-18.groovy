package changelogs

databaseChangeLog = {

    changeSet(author: "djebeniani (generated)", id: "1742285197922-1") {
        addNotNullConstraint(columnDataType: "boolean", columnName: "adr_preferred_for_survey", tableName: "address", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1742285197922-2") {
        addNotNullConstraint(columnDataType: "boolean", columnName: "surprere_billing_person", tableName: "survey_person_result", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1742285197922-3") {
        addNotNullConstraint(columnDataType: "boolean", columnName: "surprere_survey_person", tableName: "survey_person_result", validate: "true")
    }

    changeSet(author: "djebeniani (generated)", id: "1742285197922-4") {
        addColumn(tableName: "person") {
            column(name: "prs_preferred_billing_person", type: "boolean") {
            }
        }
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1742285197922-5") {
        grailsChange {
            change {
                String query = "update person set prs_preferred_billing_person = false;"
                sql.execute(query)
                String info = "${query}: ${sql.getUpdateCount()}"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1742285197922-6") {
        addColumn(tableName: "person") {
            column(name: "prs_preferred_survey_person", type: "boolean") {
            }
        }
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1742285197922-7") {
        grailsChange {
            change {
                String query = "update person set prs_preferred_survey_person = false;"
                sql.execute(query)
                String info = "${query}: ${sql.getUpdateCount()}"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1742285197922-8") {
        dropColumn(columnName: "prs_preferred_for_survey", tableName: "person")
    }
}
