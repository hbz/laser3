package de.laser

import de.laser.UserSetting
import de.laser.auth.User
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.helper.RDStore
import de.laser.helper.SqlDateUtils
import de.laser.interfaces.CalculatedType
import de.laser.properties.*
import grails.gorm.transactions.Transactional

import java.sql.Timestamp

@Transactional
class QueryService {
    def subscriptionsQueryService
    def taskService

    private java.sql.Date computeInfoDate(User user, UserSetting.KEYS userSettingKey){
        int daysToBeInformedBeforeToday = user.getSetting(userSettingKey, UserSetting.DEFAULT_REMINDER_PERIOD)?.getValue() ?: 1
        java.sql.Date infoDate = daysToBeInformedBeforeToday? SqlDateUtils.getDateInNrOfDays(daysToBeInformedBeforeToday) : null
        infoDate
    }


    List getDueObjectsCorrespondingUserSettings(Org contextOrg, User contextUser) {
        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
        ArrayList dueObjects = new ArrayList()

        if (contextUser.getSettingsValue(UserSetting.KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_ENDDATE)==RDStore.YN_YES || contextUser.getSettingsValue(UserSetting.KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_NOTICEPERIOD)==RDStore.YN_YES) {
            def endDateFrom =                (contextUser.getSettingsValue(UserSetting.KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_ENDDATE)==RDStore.YN_YES)? today : null
            def endDateTo =                  (contextUser.getSettingsValue(UserSetting.KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_ENDDATE)==RDStore.YN_YES)? computeInfoDate(contextUser, UserSetting.KEYS.REMIND_PERIOD_FOR_SUBSCRIPTIONS_ENDDATE) : null
            def manualCancellationDateFrom = (contextUser.getSettingsValue(UserSetting.KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_NOTICEPERIOD)==RDStore.YN_YES)? today : null
            def manualCancellationDateTo =   (contextUser.getSettingsValue(UserSetting.KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_NOTICEPERIOD)==RDStore.YN_YES)? computeInfoDate(contextUser, UserSetting.KEYS.REMIND_PERIOD_FOR_SUBSCRIPTIONS_NOTICEPERIOD) : null
            getDueSubscriptions(contextOrg, endDateFrom, endDateTo, manualCancellationDateFrom, manualCancellationDateTo).each{
                boolean isConsortialOrCollective = it._getCalculatedType() in [CalculatedType.TYPE_PARTICIPATION, CalculatedType.TYPE_PARTICIPATION_AS_COLLECTIVE]
                if ( ! isConsortialOrCollective ) {
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
                                  endDate: computeInfoDate(contextUser, UserSetting.KEYS.REMIND_PERIOD_FOR_TASKS)]]) )
        }

        if (contextUser.getSettingsValue(UserSetting.KEYS.IS_REMIND_FOR_SURVEYS_NOT_MANDATORY_ENDDATE)==RDStore.YN_YES) {

            dueObjects.addAll(SurveyInfo.executeQuery("SELECT distinct(sr.surveyConfig.surveyInfo) FROM SurveyResult sr LEFT JOIN sr.surveyConfig surConfig LEFT JOIN surConfig.surveyInfo surInfo WHERE sr.participant = :org AND surInfo.endDate <= :endDate AND sr.finishDate is NULL AND surInfo.status = :status AND surInfo.isMandatory = false",
                    [org: contextOrg,
                     endDate: computeInfoDate(contextUser, UserSetting.KEYS.REMIND_PERIOD_FOR_SURVEYS_NOT_MANDATORY_ENDDATE),
                     status: RDStore.SURVEY_SURVEY_STARTED]))

            dueObjects.addAll(SurveyInfo.executeQuery("SELECT distinct(surInfo) FROM SurveyInfo surInfo WHERE surInfo.owner = :org AND surInfo.endDate <= :endDate AND surInfo.status = :status AND surInfo.isMandatory = false",
                    [org: contextOrg,
                     endDate: computeInfoDate(contextUser, UserSetting.KEYS.REMIND_PERIOD_FOR_SURVEYS_NOT_MANDATORY_ENDDATE),
                     status: RDStore.SURVEY_SURVEY_STARTED]))

        }

        if (contextUser.getSettingsValue(UserSetting.KEYS.IS_REMIND_FOR_SURVEYS_MANDATORY_ENDDATE)==RDStore.YN_YES) {

            dueObjects.addAll(SurveyInfo.executeQuery("SELECT distinct(sr.surveyConfig.surveyInfo) FROM SurveyResult sr LEFT JOIN sr.surveyConfig surConfig LEFT JOIN surConfig.surveyInfo surInfo WHERE sr.participant = :org AND surInfo.endDate <= :endDate AND sr.finishDate is NULL AND surInfo.status = :status AND surInfo.isMandatory = true",
                    [org: contextOrg,
                     endDate: computeInfoDate(contextUser, UserSetting.KEYS.REMIND_PERIOD_FOR_SURVEYS_MANDATORY_ENDDATE),
                     status: RDStore.SURVEY_SURVEY_STARTED]))

            dueObjects.addAll(SurveyInfo.executeQuery("SELECT distinct(surInfo) FROM SurveyInfo surInfo WHERE surInfo.owner = :org AND surInfo.endDate <= :endDate AND surInfo.status = :status AND surInfo.isMandatory = true",
                    [org: contextOrg,
                     endDate: computeInfoDate(contextUser, UserSetting.KEYS.REMIND_PERIOD_FOR_SURVEYS_MANDATORY_ENDDATE),
                     status: RDStore.SURVEY_SURVEY_STARTED]))

        }

        if (contextUser.getSettingsValue(UserSetting.KEYS.IS_REMIND_FOR_LICENSE_CUSTOM_PROP)==RDStore.YN_YES) {
            getDueLicenseCustomProperties(contextOrg, today, computeInfoDate(contextUser, UserSetting.KEYS.REMIND_PERIOD_FOR_LICENSE_CUSTOM_PROP)).each{

                boolean isConsortialOrCollective = it.owner._getCalculatedType() in [CalculatedType.TYPE_PARTICIPATION, CalculatedType.TYPE_PARTICIPATION_AS_COLLECTIVE]
                if ( ! isConsortialOrCollective ) {
                    dueObjects << it
                }
            }
        }
        if (contextUser.getSettingsValue(UserSetting.KEYS.IS_REMIND_FOR_LIZENSE_PRIVATE_PROP)==RDStore.YN_YES) {
            dueObjects.addAll(getDueLicensePrivateProperties(contextOrg, today, computeInfoDate(contextUser, UserSetting.KEYS.REMIND_PERIOD_FOR_LICENSE_PRIVATE_PROP)))
        }
        if (contextUser.getSettingsValue(UserSetting.KEYS.IS_REMIND_FOR_PERSON_PRIVATE_PROP)==RDStore.YN_YES) {
            dueObjects.addAll(PersonProperty.findAllByDateValueBetweenForOrgAndIsNotPulbic(today, computeInfoDate(contextUser, UserSetting.KEYS.REMIND_PERIOD_FOR_PERSON_PRIVATE_PROP), contextOrg))
        }
        if (contextUser.getSettingsValue(UserSetting.KEYS.IS_REMIND_FOR_ORG_CUSTOM_PROP)==RDStore.YN_YES) {
            dueObjects.addAll(OrgProperty.findAllByDateValueBetweenAndTenantAndIsPublic(today, computeInfoDate(contextUser, UserSetting.KEYS.REMIND_PERIOD_FOR_ORG_CUSTOM_PROP), contextOrg,true))
        }
        if (contextUser.getSettingsValue(UserSetting.KEYS.IS_REMIND_FOR_ORG_PRIVATE_PROP)==RDStore.YN_YES) {
            dueObjects.addAll(getDueOrgPrivateProperties(contextOrg, today, computeInfoDate(contextUser, UserSetting.KEYS.REMIND_PERIOD_FOR_ORG_PRIVATE_PROP)))
        }
        if (contextUser.getSettingsValue(UserSetting.KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_CUSTOM_PROP)==RDStore.YN_YES) {
            getDueSubscriptionCustomProperties(contextOrg, today, computeInfoDate(contextUser, UserSetting.KEYS.REMIND_PERIOD_FOR_SUBSCRIPTIONS_CUSTOM_PROP)).each{

                boolean isConsortialOrCollective = it.owner._getCalculatedType() in [CalculatedType.TYPE_PARTICIPATION, CalculatedType.TYPE_PARTICIPATION_AS_COLLECTIVE]

                if ( ! isConsortialOrCollective ) {
                    dueObjects << it
                }
            }
        }
        if (contextUser.getSettingsValue(UserSetting.KEYS.IS_REMIND_FOR_SUBSCRIPTIONS_PRIVATE_PROP)==RDStore.YN_YES) {
            dueObjects.addAll(getDueSubscriptionPrivateProperties(contextOrg, today, computeInfoDate(contextUser, UserSetting.KEYS.REMIND_PERIOD_FOR_SUBSCRIPTIONS_PRIVATE_PROP)))
        }
        dueObjects = dueObjects.sort {
            (it instanceof AbstractPropertyWithCalculatedLastUpdated)?
                    it.dateValue : (((it instanceof Subscription || it instanceof License) && it.manualCancellationDate)? it.manualCancellationDate : it.endDate)?: Timestamp.valueOf("0001-1-1 00:00:00")
        }

        dueObjects
    }

    private Map<String, Object> getQuery(Class propertyClass, Org contextOrg, java.sql.Date fromDateValue, java.sql.Date toDateValue, boolean isPublic){
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
            def tmpQuery = getMySubscriptionsQuery(contextOrg)
            queryParams << tmpQuery.queryParams
            query += "and owner in ( " + tmpQuery.query + " )"
        }else if (LicenseProperty.class.equals(propertyClass)){
            def tmpQuery = getMyLicensesQuery(contextOrg)
            queryParams << tmpQuery.queryParams
            query += "and owner in ( " + tmpQuery.query + " )"
        }
        result.query = query
        result.queryParams = queryParams
        result
    }

    List<SubscriptionProperty> getDueSubscriptionCustomProperties(Org contextOrg, java.sql.Date fromDateValue, java.sql.Date toDateValue){
        def query = getQuery(SubscriptionProperty.class, contextOrg, fromDateValue, toDateValue, true)
        SubscriptionProperty.executeQuery(query.query, query.queryParams)
    }

    List<LicenseProperty> getDueLicenseCustomProperties(Org contextOrg, java.sql.Date fromDateValue, java.sql.Date toDateValue){
        def query = getQuery(LicenseProperty.class, contextOrg, fromDateValue, toDateValue, true)
        LicenseProperty.executeQuery(query.query, query.queryParams)
    }

    List<OrgProperty> getDueOrgPrivateProperties(Org contextOrg, java.sql.Date fromDateValue, java.sql.Date toDateValue) {
        def query = getQuery(OrgProperty.class, contextOrg, fromDateValue, toDateValue, false)
        OrgProperty.executeQuery(query.query, query.queryParams)
    }

    List<SubscriptionProperty> getDueSubscriptionPrivateProperties(Org contextOrg, java.sql.Date fromDateValue, java.sql.Date toDateValue){
        def query = getQuery(SubscriptionProperty.class, contextOrg, fromDateValue, toDateValue, false)
        SubscriptionProperty.executeQuery(query.query, query.queryParams)
    }

    List<LicenseProperty> getDueLicensePrivateProperties(Org contextOrg, java.sql.Date fromDateValue, java.sql.Date toDateValue){
        def query = getQuery(LicenseProperty.class, contextOrg, fromDateValue, toDateValue, false)
        LicenseProperty.executeQuery(query.query, query.queryParams)
    }

    private Map getMySubscriptionsQuery(Org contextOrg){
        getDueSubscriptionsQuery(contextOrg, null, null, null, null)
    }

    List<Subscription> getDueSubscriptions(Org contextOrg, java.sql.Date endDateFrom, java.sql.Date endDateTo, java.sql.Date manualCancellationDateFrom, java.sql.Date manualCancellationDateTo) {
        def query = getDueSubscriptionsQuery(contextOrg, endDateFrom, endDateTo, manualCancellationDateFrom, manualCancellationDateTo)
        Subscription.executeQuery(query.query, query.queryParams)
    }

    private Map<String, Object> getDueSubscriptionsQuery(Org contextOrg, java.sql.Date endDateFrom, java.sql.Date endDateTo, java.sql.Date manualCancellationDateFrom, java.sql.Date manualCancellationDateTo) {
        def queryParams = [:]
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

    private Map<String, Object> getMyLicensesQuery(Org institution){
        Map<String, Object> result = [:]
        def base_qry
        def qry_params
        boolean isLicensingConsortium = (institution?.hasPerm("ORG_CONSORTIUM"))
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
