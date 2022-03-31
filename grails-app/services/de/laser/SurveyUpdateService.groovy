package de.laser


import de.laser.helper.RDStore
import de.laser.interfaces.AbstractLockableService
import de.laser.system.SystemEvent
import grails.gorm.transactions.Transactional

/**
 * This service handles the automatic update of surveys
 */
@Transactional
class SurveyUpdateService extends AbstractLockableService {

    SurveyService surveyService

    /**
     * Constructor method
     */
    @javax.annotation.PostConstruct
    void init() {
        log.debug("Initialised SurveyUpdateService...")
    }

    /**
     * Cronjob-triggered.
     * Runs through all surveys having status "Ready" and "Started" and checks their dates:
     * - if state = ready, then check if start date is reached, if so: update to started, else do nothing
     * - else if state = started, then check if end date is reached, if so: update to completed, else do nothing
     * @return true if the execution was successful, false otherwise
     */
    boolean surveyCheck() {
        if(!running) {
            running = true
            Date currentDate = new Date()

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
