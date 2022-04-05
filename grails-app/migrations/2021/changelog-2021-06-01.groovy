import de.laser.SurveyOrg
import de.laser.SurveyResult
import de.laser.auth.User
import de.laser.auth.UserOrg
import de.laser.system.SystemEvent

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1622545058988-1") {
        addColumn(tableName: "reader_number") {
            column(name: "num_date_group_note", type: "text")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1622545058988-2") {
        grailsChange {
            change {
                List<SurveyOrg> surveyOrgList = SurveyOrg.findAllByFinishDateIsNull()
                List idList = []
                surveyOrgList.each {SurveyOrg surveyOrg ->
                    List<SurveyResult> surveyResults = SurveyResult.findAllByParticipantAndSurveyConfig(surveyOrg.org, surveyOrg.surveyConfig)

                    if(surveyResults.size() > 0 && !surveyResults.finishDate.contains(null)){
                        surveyOrg.finishDate = surveyResults.finishDate[0]
                        println(surveyResults.finishDate[0])
                        idList << surveyOrg.id
                    }
                }

                if (idList) {
                    SystemEvent.createEvent('DBM_SCRIPT_INFO', ['changeset': '1622545058988-2', 'count': idList.size(), 'ids': idList])
                }
            }
        }
    }
}
