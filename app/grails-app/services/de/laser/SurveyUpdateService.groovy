package de.laser


import com.k_int.kbplus.SurveyInfo
import de.laser.helper.RDStore
import de.laser.interfaces.AbstractLockableService
import grails.transaction.Transactional

@Transactional
class SurveyUpdateService extends AbstractLockableService {

    boolean surveyCheck() {
        if(!running) {
            running = true
            def currentDate = new Date(System.currentTimeMillis())

            // Ready -> Started
            def readySurveysIds = SurveyInfo.where {
                status == RDStore.SURVEY_READY && startDate < currentDate
            }.collect { it -> it.id }

            log.info("surveyCheck readySurveysIds: " + readySurveysIds)

            if (readySurveysIds) {

                SurveyInfo.executeUpdate(
                        'UPDATE SurveyInfo survey SET survey.status =:status WHERE survey.id in (:ids)',
                        [status: RDStore.SURVEY_SURVEY_STARTED, ids: readySurveysIds]
                )
            }

            // Started -> Completed

            def startedSurveyIds = SurveyInfo.where {
                status == RDStore.SURVEY_SURVEY_STARTED && startDate < currentDate && (endDate != null && endDate < currentDate)
            }.collect { it -> it.id }

            log.info("surveyCheck startedSurveyIds: " + startedSurveyIds)

            if (startedSurveyIds) {

                SurveyInfo.executeUpdate(
                        'UPDATE SurveyInfo survey SET survey.status =:status WHERE survey.id in (:ids)',
                        [status: RDStore.SURVEY_SURVEY_COMPLETED, ids: startedSurveyIds]
                )
            }
            running = false
            return true
        }
        else {
            log.warn("Surveys already checked ... not starting again.")
            return false
        }
    }
}
