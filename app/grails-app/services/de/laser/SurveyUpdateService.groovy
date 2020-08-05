package de.laser


import com.k_int.kbplus.SurveyInfo
import de.laser.helper.RDStore
import de.laser.interfaces.AbstractLockableService
import grails.transaction.Transactional

@Transactional
class SurveyUpdateService extends AbstractLockableService {

    EscapeService escapeService
    SurveyService surveyService


    @javax.annotation.PostConstruct
    void init() {
        log.debug("Initialised SurveyUpdateService...")
    }

    boolean surveyCheck() {
        if(!running) {
            running = true
            def currentDate = new Date(System.currentTimeMillis())

            Map<String,Object> updatedObjs = [:]

            // Ready -> Started
            List readySurveysIds = SurveyInfo.findAllByStatusAndStartDateLessThanEquals(RDStore.SURVEY_READY, currentDate).collect {it.id}

            log.info("surveyCheck (Ready to Started) readySurveysIds: " + readySurveysIds)

            if (readySurveysIds) {

                updatedObjs << ["readyToStarted (${readySurveysIds.size()})" : readySurveysIds]

                SurveyInfo.executeUpdate(
                        'UPDATE SurveyInfo survey SET survey.status =:status WHERE survey.id in (:ids)',
                        [status: RDStore.SURVEY_SURVEY_STARTED, ids: readySurveysIds]
                )
                //
                surveyService.emailsToSurveyUsers(readySurveysIds)

            }

            // Started -> Completed

            Set<Long> startedSurveyIds = SurveyInfo.executeQuery('select sur.id from SurveyInfo sur where sur.status = :status and sur.startDate < :currentDate and sur.endDate != null and sur.endDate < :currentDate',
                    [status: RDStore.SURVEY_SURVEY_STARTED,currentDate: currentDate])

            log.info("surveyCheck (Started to Completed) startedSurveyIds: " + startedSurveyIds)

            if (startedSurveyIds) {
                updatedObjs << ["StartedToCompleted (${startedSurveyIds.size()})" : startedSurveyIds]

                SurveyInfo.executeUpdate(
                        'UPDATE SurveyInfo survey SET survey.status =:status WHERE survey.id in (:ids)',
                        [status: RDStore.SURVEY_SURVEY_COMPLETED, ids: startedSurveyIds]
                )
            }

            def oldStartedSurveyIds = SurveyInfo.where {
                (status == RDStore.SURVEY_SURVEY_STARTED) && (startDate < currentDate) && (endDate != null && endDate < currentDate)
            }.collect { it.id }

            if (oldStartedSurveyIds) {
                updatedObjs << ["old_StartedToCompleted (${oldStartedSurveyIds.size()})": oldStartedSurveyIds]
            }

            SystemEvent.createEvent('SURVEY_UPDATE_SERVICE_PROCESSING', updatedObjs)
            running = false
            return true
        }
        else {
            log.warn("Surveys already checked ... not starting again.")
            return false
        }
    }
}
