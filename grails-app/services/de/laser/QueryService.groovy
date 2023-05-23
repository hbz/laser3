package de.laser


import de.laser.auth.User
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.storage.RDStore
import de.laser.utils.SqlDateUtils
import de.laser.interfaces.CalculatedType
import de.laser.properties.*
import de.laser.survey.SurveyInfo
import grails.gorm.transactions.Transactional

import java.sql.Timestamp

/**
 * This service is a helper service for the due dates service processes
 */
@Transactional
class QueryService {
    SubscriptionsQueryService subscriptionsQueryService
    TaskService taskService

    /**
     * Gets the date from which the given user wishes to be informed
     * @param user the user whose setting should be retrieved
     * @param userSettingKey the setting key constant for task reminding
     * @return the starting date for queries
     */
    private java.sql.Date _computeInfoDate(User user, UserSetting.KEYS userSettingKey){
        int daysToBeInformedBeforeToday = user.getSetting(userSettingKey, UserSetting.DEFAULT_REMINDER_PERIOD)?.getValue() ?: 1
        java.sql.Date infoDate = daysToBeInformedBeforeToday? SqlDateUtils.getDateInNrOfDays(daysToBeInformedBeforeToday) : null
        infoDate
    }

    /**
     * Retrieves due objects according to the given user's settings
     * @param contextOrg the user's institution
     * @param contextUser the user whose due objects should be retrieved
     * @return a list of objects with upcoming due dates
     */
    List getDueObjectsCorrespondingUserSettings(Org contextOrg, User contextUser) {
        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
        ArrayList dueObjects = new ArrayList()

        if (contextUser.getSettingsValue(UserSetting.KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_ENDDATE)==RDStore.YN_YES || contextUser.getSettingsValue(UserSetting.KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_NOTICEPERIOD)==RDStore.YN_YES) {
            def endDateFrom =                (contextUser.getSettingsValue(UserSetting.KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_ENDDATE)==RDStore.YN_YES)? today : null
            def endDateTo =                  (contextUser.getSettingsValue(UserSetting.KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_ENDDATE)==RDStore.YN_YES)? _computeInfoDate(contextUser, UserSetting.KEYS.REMIND_PERIOD_FOR_SUBSCRIPTIONS_ENDDATE) : null
            def manualCancellationDateFrom = (contextUser.getSettingsValue(UserSetting.KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_NOTICEPERIOD)==RDStore.YN_YES)? today : null
            def manualCancellationDateTo =   (contextUser.getSettingsValue(UserSetting.KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_NOTICEPERIOD)==RDStore.YN_YES)? _computeInfoDate(contextUser, UserSetting.KEYS.REMIND_PERIOD_FOR_SUBSCRIPTIONS_NOTICEPERIOD) : null
            getDueSubscriptions(contextOrg, endDateFrom, endDateTo, manualCancellationDateFrom, manualCancellationDateTo).each{

                if ( ! (it._getCalculatedType() in [CalculatedType.TYPE_PARTICIPATION]) ) {
                    dueObjects << it
                }
            }
        }

        if (contextUser.getSettingsValue(UserSetting.KEYS.IS_REMIND_FOR_TASKS)==RDStore.YN_YES) {
            dueObjects.addAll( taskService.getTasksByResponsibles(
                    contextUser,
                    contextOrg,
                    [query:" and t.status = :open and t.endDate <= :endDate",
                     queryParams:[open: RDStore.TASK_STATUS_OPEN,
                                  endDate: _computeInfoDate(contextUser, UserSetting.KEYS.REMIND_PERIOD_FOR_TASKS)]]) )
        }

        if (contextUser.getSettingsValue(UserSetting.KEYS.IS_REMIND_FOR_SURVEYS_NOT_MANDATORY_ENDDATE)==RDStore.YN_YES) {

            dueObjects.addAll(SurveyInfo.executeQuery("SELECT distinct(surveyOrg.surveyConfig.surveyInfo) FROM SurveyOrg surveyOrg LEFT JOIN surveyOrg.surveyConfig surConfig LEFT JOIN surConfig.surveyInfo surInfo WHERE surveyOrg.org = :org AND surInfo.endDate <= :endDate AND surveyOrg.finishDate is NULL AND surInfo.status = :status AND surInfo.isMandatory = false",
                    [org: contextOrg,
                     endDate: _computeInfoDate(contextUser, UserSetting.KEYS.REMIND_PERIOD_FOR_SURVEYS_NOT_MANDATORY_ENDDATE),
                     status: RDStore.SURVEY_SURVEY_STARTED]))

            dueObjects.addAll(SurveyInfo.executeQuery("SELECT distinct(surInfo) FROM SurveyInfo surInfo WHERE surInfo.owner = :org AND surInfo.endDate <= :endDate AND surInfo.status = :status AND surInfo.isMandatory = false",
                    [org: contextOrg,
                     endDate: _computeInfoDate(contextUser, UserSetting.KEYS.REMIND_PERIOD_FOR_SURVEYS_NOT_MANDATORY_ENDDATE),
                     status: RDStore.SURVEY_SURVEY_STARTED]))

        }

        if (contextUser.getSettingsValue(UserSetting.KEYS.IS_REMIND_FOR_SURVEYS_MANDATORY_ENDDATE)==RDStore.YN_YES) {

            dueObjects.addAll(SurveyInfo.executeQuery("SELECT distinct(surveyOrg.surveyConfig.surveyInfo) FROM SurveyOrg surveyOrg LEFT JOIN surveyOrg.surveyConfig surConfig LEFT JOIN surConfig.surveyInfo surInfo WHERE surveyOrg.org = :org AND surInfo.endDate <= :endDate AND surveyOrg.finishDate is NULL AND surInfo.status = :status AND surInfo.isMandatory = true",
                    [org: contextOrg,
                     endDate: _computeInfoDate(contextUser, UserSetting.KEYS.REMIND_PERIOD_FOR_SURVEYS_MANDATORY_ENDDATE),
                     status: RDStore.SURVEY_SURVEY_STARTED]))

            dueObjects.addAll(SurveyInfo.executeQuery("SELECT distinct(surInfo) FROM SurveyInfo surInfo WHERE surInfo.owner = :org AND surInfo.endDate <= :endDate AND surInfo.status = :status AND surInfo.isMandatory = true",
                    [org: contextOrg,
                     endDate: _computeInfoDate(contextUser, UserSetting.KEYS.REMIND_PERIOD_FOR_SURVEYS_MANDATORY_ENDDATE),
                     status: RDStore.SURVEY_SURVEY_STARTED]))

        }

        if (contextUser.getSettingsValue(UserSetting.KEYS.IS_REMIND_FOR_LICENSE_CUSTOM_PROP)==RDStore.YN_YES) {
            getDueLicenseCustomProperties(contextOrg, today, _computeInfoDate(contextUser, UserSetting.KEYS.REMIND_PERIOD_FOR_LICENSE_CUSTOM_PROP)).each{

                if ( ! (it.owner._getCalculatedType() in [CalculatedType.TYPE_PARTICIPATION]) ) {
                    dueObjects << it
                }
            }
        }
        if (contextUser.getSettingsValue(UserSetting.KEYS.IS_REMIND_FOR_LIZENSE_PRIVATE_PROP)==RDStore.YN_YES) {
            dueObjects.addAll(getDueLicensePrivateProperties(contextOrg, today, _computeInfoDate(contextUser, UserSetting.KEYS.REMIND_PERIOD_FOR_LICENSE_PRIVATE_PROP)))
        }
        if (contextUser.getSettingsValue(UserSetting.KEYS.IS_REMIND_FOR_PERSON_PRIVATE_PROP)==RDStore.YN_YES) {
            dueObjects.addAll(PersonProperty.findAllByDateValueBetweenForOrgAndIsNotPulbic(today, _computeInfoDate(contextUser, UserSetting.KEYS.REMIND_PERIOD_FOR_PERSON_PRIVATE_PROP), contextOrg))
        }
        if (contextUser.getSettingsValue(UserSetting.KEYS.IS_REMIND_FOR_ORG_CUSTOM_PROP)==RDStore.YN_YES) {
            dueObjects.addAll(OrgProperty.findAllByDateValueBetweenAndTenantAndIsPublic(today, _computeInfoDate(contextUser, UserSetting.KEYS.REMIND_PERIOD_FOR_ORG_CUSTOM_PROP), contextOrg,true))
        }
        if (contextUser.getSettingsValue(UserSetting.KEYS.IS_REMIND_FOR_ORG_PRIVATE_PROP)==RDStore.YN_YES) {
            dueObjects.addAll(getDueOrgPrivateProperties(contextOrg, today, _computeInfoDate(contextUser, UserSetting.KEYS.REMIND_PERIOD_FOR_ORG_PRIVATE_PROP)))
        }
        if (contextUser.getSettingsValue(UserSetting.KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_CUSTOM_PROP)==RDStore.YN_YES) {
            getDueSubscriptionCustomProperties(contextOrg, today, _computeInfoDate(contextUser, UserSetting.KEYS.REMIND_PERIOD_FOR_SUBSCRIPTIONS_CUSTOM_PROP)).each{

                if ( ! (it.owner._getCalculatedType() in [CalculatedType.TYPE_PARTICIPATION]) ) {
                    dueObjects << it
                }
            }
        }
        if (contextUser.getSettingsValue(UserSetting.KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_PRIVATE_PROP)==RDStore.YN_YES) {
            dueObjects.addAll(getDueSubscriptionPrivateProperties(contextOrg, today, _computeInfoDate(contextUser, UserSetting.KEYS.REMIND_PERIOD_FOR_SUBSCRIPTIONS_PRIVATE_PROP)))
        }
        dueObjects = dueObjects.sort {
            (it instanceof AbstractPropertyWithCalculatedLastUpdated)?
                    it.dateValue : (((it instanceof Subscription || it instanceof License) && it.manualCancellationDate)? it.manualCancellationDate : it.endDate)?: Timestamp.valueOf("0001-1-1 00:00:00")
        }

        dueObjects
    }

    /**
     * Generates a due object query according to the given arguments
     * @param propertyClass the class of objects to retrieve
     * @param contextOrg the institution whose perspective is going to be taken
     * @param fromDateValue the time point from which objects should be retrieved
     * @param toDateValue the time point until objects should be retrieved
     * @param isPublic should only public objects being retrieved?
     * @return a map containing query and query parameters
     */
    private Map<String, Object> _getQuery(Class propertyClass, Org contextOrg, java.sql.Date fromDateValue, java.sql.Date toDateValue, boolean isPublic){
        Map<String, Object> result = [:]
        String query
        Map<String,Object> queryParams = [isPublic:isPublic, myOrg:contextOrg]
        if (toDateValue) {
            query = "SELECT distinct(prop) FROM " + propertyClass.simpleName + " as prop WHERE (dateValue >= :from and dateValue <= :to) AND prop.tenant = :myOrg AND prop.isPublic = :isPublic "
            queryParams << [from:fromDateValue, to:toDateValue]
        } else {
            query = "SELECT distinct(prop) FROM " + propertyClass.simpleName + " as prop WHERE dateValue >= :from AND prop.tenant = :myOrg AND prop.isPublic = :isPublic "
            queryParams << [from:fromDateValue]
        }
        if (!isPublic) {
            query += "and exists (select pd from PropertyDefinition as pd where prop.type = pd AND pd.tenant = :myOrg) "
        }
        if (SubscriptionProperty.class.equals(propertyClass)) {
            Map tmpQuery = _getMySubscriptionsQuery(contextOrg)
            queryParams << tmpQuery.queryParams
            query += "and owner in ( " + tmpQuery.query + " )"
        }else if (LicenseProperty.class.equals(propertyClass)){
            Map tmpQuery = _getMyLicensesQuery(contextOrg)
            queryParams << tmpQuery.queryParams
            query += "and owner in ( " + tmpQuery.query + " )"
        }
        result.query = query
        result.queryParams = queryParams
        result
    }

    /**
     * Retrieves due public subscription properties
     * @param contextOrg the institution whose properties should be accessed
     * @param fromDateValue from when should objects being retrieved?
     * @param toDateValue until when should objects being retrieved?
     * @return a list of upcoming due subscription properties
     */
    List<SubscriptionProperty> getDueSubscriptionCustomProperties(Org contextOrg, java.sql.Date fromDateValue, java.sql.Date toDateValue){
        Map query = _getQuery(SubscriptionProperty.class, contextOrg, fromDateValue, toDateValue, true)
        SubscriptionProperty.executeQuery(query.query, query.queryParams)
    }

    /**
     * Retrieves due public license properties
     * @param contextOrg the institution whose properties should be accessed
     * @param fromDateValue from when should objects being retrieved?
     * @param toDateValue until when should objects being retrieved?
     * @return a list of upcoming due license properties
     */
    List<LicenseProperty> getDueLicenseCustomProperties(Org contextOrg, java.sql.Date fromDateValue, java.sql.Date toDateValue){
        Map query = _getQuery(LicenseProperty.class, contextOrg, fromDateValue, toDateValue, true)
        LicenseProperty.executeQuery(query.query, query.queryParams)
    }

    /**
     * Retrieves due public organisation properties
     * @param contextOrg the institution whose properties should be accessed
     * @param fromDateValue from when should objects being retrieved?
     * @param toDateValue until when should objects being retrieved?
     * @return a list of upcoming due organisation properties
     */
    List<OrgProperty> getDueOrgPrivateProperties(Org contextOrg, java.sql.Date fromDateValue, java.sql.Date toDateValue) {
        Map query = _getQuery(OrgProperty.class, contextOrg, fromDateValue, toDateValue, false)
        OrgProperty.executeQuery(query.query, query.queryParams)
    }

    /**
     * Retrieves due private subscription properties
     * @param contextOrg the institution whose properties should be accessed
     * @param fromDateValue from when should objects being retrieved?
     * @param toDateValue until when should objects being retrieved?
     * @return a list of upcoming due private subscription properties
     */
    List<SubscriptionProperty> getDueSubscriptionPrivateProperties(Org contextOrg, java.sql.Date fromDateValue, java.sql.Date toDateValue){
        Map query = _getQuery(SubscriptionProperty.class, contextOrg, fromDateValue, toDateValue, false)
        SubscriptionProperty.executeQuery(query.query, query.queryParams)
    }

    /**
     * Retrieves due private license properties
     * @param contextOrg the institution whose properties should be accessed
     * @param fromDateValue from when should objects being retrieved?
     * @param toDateValue until when should objects being retrieved?
     * @return a list of upcoming due private license properties
     */
    List<LicenseProperty> getDueLicensePrivateProperties(Org contextOrg, java.sql.Date fromDateValue, java.sql.Date toDateValue){
        Map query = _getQuery(LicenseProperty.class, contextOrg, fromDateValue, toDateValue, false)
        LicenseProperty.executeQuery(query.query, query.queryParams)
    }

    /**
     * Generates a generic query for due subscriptions
     * @param contextOrg the institution whose subscriptions should be accessed
     * @return
     */
    private Map _getMySubscriptionsQuery(Org contextOrg){
        _getDueSubscriptionsQuery(contextOrg, null, null, null, null)
    }

    /**
     * Gets due subscriptions for the given institution
     * @param contextOrg the institution whose subscriptions should be accessed
     * @param endDateFrom the start time from which end dates should be considered
     * @param endDateTo the end time until which end dates should be considered
     * @param manualCancellationDateFrom the start time from which cancellation dates should be considered
     * @param manualCancellationDateTo the end time until which cancellation dates should be considered
     * @return a list of due subscriptions
     */
    List<Subscription> getDueSubscriptions(Org contextOrg, java.sql.Date endDateFrom, java.sql.Date endDateTo, java.sql.Date manualCancellationDateFrom, java.sql.Date manualCancellationDateTo) {
        Map query = _getDueSubscriptionsQuery(contextOrg, endDateFrom, endDateTo, manualCancellationDateFrom, manualCancellationDateTo)
        Subscription.executeQuery(query.query, query.queryParams)
    }

    /**
     * Generates a query for due subscriptions
     * @param contextOrg the institution whose subscriptions should be accessed
     * @param endDateFrom the start time from which end dates should be considered
     * @param endDateTo the end time until which end dates should be considered
     * @param manualCancellationDateFrom the start time from which cancellation dates should be considered
     * @param manualCancellationDateTo the end time until which cancellation dates should be considered
     * @return a map containing the query string and the parameters for the query
     */
    private Map<String, Object> _getDueSubscriptionsQuery(Org contextOrg, java.sql.Date endDateFrom, java.sql.Date endDateTo, java.sql.Date manualCancellationDateFrom, java.sql.Date manualCancellationDateTo) {
        Map queryParams = [:]
        queryParams.endDateFrom = endDateFrom
        queryParams.endDateTo = endDateTo
        queryParams.manualCancellationDateFrom = manualCancellationDateFrom
        queryParams.manualCancellationDateTo = manualCancellationDateTo
        queryParams.validOn = ""
        def base_qry
        def qry_params
        (base_qry, qry_params) = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(queryParams, contextOrg)
        Map<String, Object> result = [:]
        result.query = "select s " + base_qry
        result.queryParams = qry_params
        result
    }

    /**
     * Generates a query for due licenses
     * @param institution the institution whose licenses should be accessed
     * @return a map containing the query string and the query arguments
     */
    private Map<String, Object> _getMyLicensesQuery(Org institution){
        Map<String, Object> result = [:]
        def base_qry
        def qry_params
        boolean isLicensingConsortium = (institution?.isCustomerType_Consortium())
        boolean isLicensee = ! isLicensingConsortium

        if (isLicensee) {
            base_qry = """
from License as l where (
    exists ( select o from l.orgRelations as o where ( ( o.roleType = :roleType1 or o.roleType = :roleType2 ) AND o.org = :lic_org ) ) 
)
"""
            qry_params = [roleType1:RDStore.OR_LICENSEE, roleType2:RDStore.OR_LICENSEE_CONS, lic_org:institution]
        }

        if (isLicensingConsortium) {
            base_qry = """
from License as l where (
    exists ( select o from l.orgRelations as o where ( 
            ( o.roleType = :roleTypeC 
                AND o.org = :lic_org 
                AND NOT exists (
                    select o2 from l.orgRelations as o2 where o2.roleType = :roleTypeL
                )
            )
        ))
)
"""
            qry_params = [roleTypeC:RDStore.OR_LICENSING_CONSORTIUM, roleTypeL:RDStore.OR_LICENSEE_CONS, lic_org:institution]
        }

        result.query = "select l " + base_qry
        result.queryParams = qry_params

        result
    }
}
