package de.laser

import com.k_int.kbplus.*
import com.k_int.kbplus.abstract_domain.AbstractProperty
import com.k_int.kbplus.auth.User
import de.laser.helper.SqlDateUtils

import static com.k_int.kbplus.UserSettings.KEYS.*
import static com.k_int.kbplus.UserSettings.DEFAULT_REMINDER_PERIOD
import static de.laser.helper.RDStore.*

class QueryService {
    def subscriptionsQueryService
    def taskService

    private computeInfoDate(User user, UserSettings.KEYS userSettingsKey){
        int daysToBeInformedBeforeToday = user.getSetting(userSettingsKey, DEFAULT_REMINDER_PERIOD)?.getValue()
        java.sql.Date infoDate = daysToBeInformedBeforeToday? SqlDateUtils.getDateInNrOfDays(daysToBeInformedBeforeToday) : null
        infoDate
    }


    def getDueObjectsCorrespondingUserSettings(Org contextOrg, User contextUser) {
        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
        ArrayList dueObjects = new ArrayList()

        if (contextUser.getSettingsValue(IS_REMIND_FOR_SUBSCRIPTIONS_ENDDATE)==YN_YES || contextUser.getSettingsValue(IS_REMIND_FOR_SUBSCRIPTIONS_NOTICEPERIOD)==YN_YES) {
            def endDateFrom =                (contextUser.getSettingsValue(IS_REMIND_FOR_SUBSCRIPTIONS_ENDDATE)==YN_YES)? today : null
            def endDateTo =                  (contextUser.getSettingsValue(IS_REMIND_FOR_SUBSCRIPTIONS_ENDDATE)==YN_YES)? computeInfoDate(contextUser, REMIND_PERIOD_FOR_SUBSCRIPTIONS_ENDDATE) : null
            def manualCancellationDateFrom = (contextUser.getSettingsValue(IS_REMIND_FOR_SUBSCRIPTIONS_NOTICEPERIOD)==YN_YES)? today : null
            def manualCancellationDateTo =   (contextUser.getSettingsValue(IS_REMIND_FOR_SUBSCRIPTIONS_NOTICEPERIOD)==YN_YES)? computeInfoDate(contextUser, REMIND_PERIOD_FOR_SUBSCRIPTIONS_NOTICEPERIOD) : null
            dueObjects.addAll(getDueSubscriptions(contextOrg, endDateFrom, endDateTo, manualCancellationDateFrom, manualCancellationDateTo))
        }

        if (contextUser.getSettingsValue(IS_REMIND_FOR_TASKS)==YN_YES) {
            dueObjects.addAll( taskService.getTasksByResponsibles(
                    contextUser,
                    contextOrg,
                    [query:" and status = :open and endDate <= :endDate",
                     queryParams:[open: RefdataValue.getByValueAndCategory('Open', 'Task Status'),
                                  endDate: computeInfoDate(contextUser, REMIND_PERIOD_FOR_TASKS)]]) )
        }

        if (contextUser.getSettingsValue(IS_REMIND_FOR_SURVEYS_ENDDATE)==YN_YES) {

            def surveys = SurveyInfo.executeQuery("SELECT sr.surveyConfig.surveyInfo FROM SurveyResult sr LEFT JOIN sr.surveyConfig surConfig LEFT JOIN surConfig.surveyInfo surInfo WHERE sr.participant = :org AND surInfo.endDate <= :endDate AND sr.finishDate is NULL ",
                    [org: contextOrg,
                     endDate: computeInfoDate(contextUser, REMIND_PERIOD_FOR_SURVEYS_ENDDATE)])

            surveys = surveys?.unique { a, b -> a?.id <=> b?.id }

            dueObjects.addAll(surveys)
        }

        if (contextUser.getSettingsValue(IS_REMIND_FOR_LICENSE_CUSTOM_PROP)==YN_YES) {
            dueObjects.addAll(getDueLicenseCustomProperties(contextOrg, today, computeInfoDate(contextUser, REMIND_PERIOD_FOR_LICENSE_CUSTOM_PROP)))
        }
        if (contextUser.getSettingsValue(IS_REMIND_FOR_LIZENSE_PRIVATE_PROP)==YN_YES) {
            dueObjects.addAll(getDueLicensePrivateProperties(contextOrg, today, computeInfoDate(contextUser, REMIND_PERIOD_FOR_LICENSE_PRIVATE_PROP)))
        }
        if (contextUser.getSettingsValue(IS_REMIND_FOR_PERSON_PRIVATE_PROP)==YN_YES) {
            dueObjects.addAll(PersonPrivateProperty.findAllByDateValueBetweenForOrgAndIsNotPulbic(today, computeInfoDate(contextUser, REMIND_PERIOD_FOR_PERSON_PRIVATE_PROP), contextOrg))
        }
        if (contextUser.getSettingsValue(IS_REMIND_FOR_ORG_CUSTOM_PROP)==YN_YES) {
            dueObjects.addAll(OrgCustomProperty.findAllByDateValueBetween(today, computeInfoDate(contextUser, REMIND_PERIOD_FOR_ORG_CUSTOM_PROP)))
        }
        if (contextUser.getSettingsValue(IS_REMIND_FOR_ORG_PRIVATE_PROP)==YN_YES) {
            dueObjects.addAll(getDueOrgPrivateProperties(contextOrg, today, computeInfoDate(contextUser, REMIND_PERIOD_FOR_ORG_PRIVATE_PROP)))
        }
        if (contextUser.getSettingsValue(IS_REMIND_FOR_SUBSCRIPTIONS_CUSTOM_PROP)==YN_YES) {
            dueObjects.addAll(getDueSubscriptionCustomProperties(contextOrg, today, computeInfoDate(contextUser, REMIND_PERIOD_FOR_SUBSCRIPTIONS_CUSTOM_PROP)))
        }
        if (contextUser.getSettingsValue(IS_REMIND_FOR_SUBSCRIPTIONS_PRIVATE_PROP)==YN_YES) {
            dueObjects.addAll(getDueSubscriptionPrivateProperties(contextOrg, today, computeInfoDate(contextUser, REMIND_PERIOD_FOR_SUBSCRIPTIONS_PRIVATE_PROP)))
        }
        dueObjects = dueObjects.sort {
            (it instanceof AbstractProperty)?
                    it.dateValue : (((it instanceof Subscription || it instanceof License) && it.manualCancellationDate)? it.manualCancellationDate : it.endDate)?: java.sql.Timestamp.valueOf("0001-1-1 00:00:00")
        }

        dueObjects
    }

    private def getQuery(Class propertyClass, Org contextOrg, java.sql.Date fromDateValue, java.sql.Date toDateValue){
        def result = [:]
        def query
        def queryParams
        if (toDateValue) {
            query = "SELECT distinct(prop) FROM " + propertyClass.simpleName + " as prop WHERE (dateValue >= :from and dateValue <= :to) "
            queryParams = [from:fromDateValue, to:toDateValue]
        } else {
            query = "SELECT distinct(prop) FROM " + propertyClass.simpleName + " as prop WHERE dateValue >= :from "
            queryParams = [from:fromDateValue]
        }
        if (propertyClass.simpleName.toLowerCase().contains("private")) {
            queryParams << [myOrg:contextOrg]
            query += "and exists (select pd from PropertyDefinition as pd where prop.type = pd AND pd.tenant = :myOrg) "
        }
        if (SubscriptionCustomProperty.class.equals(propertyClass) || SubscriptionPrivateProperty.class.equals(propertyClass)) {
            def tmpQuery = getMySubscriptionsQuery(contextOrg)
            queryParams << tmpQuery.queryParams
            query += "and owner in ( " + tmpQuery.query + " )"
        }else if (LicenseCustomProperty.class.equals(propertyClass) || LicensePrivateProperty.class.equals(propertyClass)){
            def tmpQuery = getMyLicensesQuery(contextOrg)
            queryParams << tmpQuery.queryParams
            query += "and owner in ( " + tmpQuery.query + " )"
        }
        result.query = query
        result.queryParams = queryParams
        result
    }

    def getDueSubscriptionCustomProperties(Org contextOrg, java.sql.Date fromDateValue, java.sql.Date toDateValue){
        def query = getQuery(SubscriptionCustomProperty.class, contextOrg, fromDateValue, toDateValue)
        SubscriptionCustomProperty.executeQuery(query.query, query.queryParams)
    }

    def getDueLicenseCustomProperties(Org contextOrg, java.sql.Date fromDateValue, java.sql.Date toDateValue){
        def query = getQuery(LicenseCustomProperty.class, contextOrg, fromDateValue, toDateValue)
        LicenseCustomProperty.executeQuery(query.query, query.queryParams)
    }

    def getDueOrgPrivateProperties(Org contextOrg, java.sql.Date fromDateValue, java.sql.Date toDateValue) {
        def query = getQuery(OrgPrivateProperty.class, contextOrg, fromDateValue, toDateValue)
        OrgPrivateProperty.executeQuery(query.query, query.queryParams)
    }

    def getDueSubscriptionPrivateProperties(Org contextOrg, java.sql.Date fromDateValue, java.sql.Date toDateValue){
        def query = getQuery(SubscriptionPrivateProperty.class, contextOrg, fromDateValue, toDateValue)
        SubscriptionPrivateProperty.executeQuery(query.query, query.queryParams)
    }

    def getDueLicensePrivateProperties(Org contextOrg, java.sql.Date fromDateValue, java.sql.Date toDateValue){
        def query = getQuery(LicensePrivateProperty.class, contextOrg, fromDateValue, toDateValue)
        LicensePrivateProperty.executeQuery(query.query, query.queryParams)
    }

    private def getMySubscriptionsQuery(Org contextOrg){
        getDueSubscriptionsQuery(contextOrg, null, null, null, null)
    }

    def getDueSubscriptions(Org contextOrg, java.sql.Date endDateFrom, java.sql.Date endDateTo, java.sql.Date manualCancellationDateFrom, java.sql.Date manualCancellationDateTo) {
        def query = getDueSubscriptionsQuery(contextOrg, endDateFrom, endDateTo, manualCancellationDateFrom, manualCancellationDateTo)
        Subscription.executeQuery(query.query, query.queryParams)
    }

    private def getDueSubscriptionsQuery(Org contextOrg, java.sql.Date endDateFrom, java.sql.Date endDateTo, java.sql.Date manualCancellationDateFrom, java.sql.Date manualCancellationDateTo) {
        def queryParams = [:]
        queryParams.endDateFrom = endDateFrom
        queryParams.endDateTo = endDateTo
        queryParams.manualCancellationDateFrom = manualCancellationDateFrom
        queryParams.manualCancellationDateTo = manualCancellationDateTo
        queryParams.validOn = ""
        def base_qry
        def qry_params
        (base_qry, qry_params) = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(queryParams, contextOrg)
        def result = [:]
        result.query = "select s ${base_qry}"
        result.queryParams = qry_params
        result
    }

    private def getMyLicensesQuery(Org institution){
        def template_license_type = RefdataValue.getByValueAndCategory('Template', 'License Type')
        def result = [:]
        def base_qry
        def qry_params
        boolean isLicensingConsortium = (institution?.hasPerm("ORG_CONSORTIUM"))
        boolean isLicensee = ! isLicensingConsortium

        if (isLicensee) {
            base_qry = """
from License as l where (
    exists ( select o from l.orgLinks as o where ( ( o.roleType = :roleType1 or o.roleType = :roleType2 ) AND o.org = :lic_org ) ) 
    AND ( l.status != :deleted OR l.status = null )
    AND ( l.type != :template )
)
"""
            qry_params = [roleType1:OR_LICENSEE, roleType2:OR_LICENSEE_CONS, lic_org:institution, deleted:LICENSE_DELETED, template: template_license_type]
        }

        if (isLicensingConsortium) {
            base_qry = """
from License as l where (
    exists ( select o from l.orgLinks as o where ( 
            ( o.roleType = :roleTypeC 
                AND o.org = :lic_org 
                AND NOT exists (
                    select o2 from l.orgLinks as o2 where o2.roleType = :roleTypeL
                )
            )
        )) 
    AND ( l.status != :deleted OR l.status = null )
    AND ( l.type != :template )
)
"""
            qry_params = [roleTypeC:OR_LICENSING_CONSORTIUM, roleTypeL:OR_LICENSEE_CONS, lic_org:institution, deleted:LICENSE_DELETED, template:template_license_type]
        }

        result.query = "select l ${base_qry}"
        result.queryParams = qry_params

        result
    }
}