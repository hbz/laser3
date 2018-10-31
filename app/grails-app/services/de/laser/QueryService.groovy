package de.laser

import com.k_int.kbplus.*
import static de.laser.helper.RDStore.*

class QueryService {
    def contextService

    def getDueSubscriptionCustomProperties(Org contextOrg, java.sql.Date fromDateValue, java.sql.Date toDateValue){
        def mySubsQuery = getMySubscriptionsQuery(contextOrg)
        def query = "SELECT distinct(prop) FROM SubscriptionCustomProperty as prop WHERE (dateValue >= :from and dateValue <= :to) " +
                "and owner in ( " + mySubsQuery.query + " )"
        def queryParams = [from:fromDateValue, to:toDateValue] << mySubsQuery.queryParams

        SubscriptionCustomProperty.executeQuery(query, queryParams)
    }

    def getDueLicenseCustomProperties(Org contextOrg, java.sql.Date fromDateValue, java.sql.Date toDateValue){
        def myLicensesQuery = getMyLicensesQuery(contextOrg)
        def query = "SELECT distinct(prop) FROM LicenseCustomProperty as prop WHERE (dateValue >= :from and dateValue <= :to) " +
                "and owner in ( " + myLicensesQuery.query + " )"
        def queryParams = [from:fromDateValue, to:toDateValue] << myLicensesQuery.queryParams

        LicenseCustomProperty.executeQuery(query, queryParams)
    }

    def getDueOrgPrivateProperties(Org contextOrg, java.sql.Date fromDateValue, java.sql.Date toDateValue) {
        def query = "SELECT distinct(prop) FROM OrgPrivateProperty as prop WHERE (dateValue >= :from and dateValue <= :to) " +
                "and exists (select pd from PropertyDefinition as pd where prop.type = pd AND pd.tenant = :myOrg) "
        def queryParams = [from:fromDateValue, to:toDateValue, myOrg:contextOrg]

        OrgPrivateProperty.executeQuery(query, queryParams)
    }

    def getDueSubscriptionPrivateProperties(Org contextOrg, java.sql.Date fromDateValue, java.sql.Date toDateValue){
        def mySubsQuery = getMySubscriptionsQuery(contextOrg)
        def query = "SELECT distinct(prop) FROM SubscriptionPrivateProperty as prop WHERE (dateValue >= :from and dateValue <= :to)" +
                "and exists (select pd from PropertyDefinition as pd where prop.type = pd AND pd.tenant = :myOrg) " +
                "and owner in ( " + mySubsQuery.query + " )"
        def queryParams = [from:fromDateValue, to:toDateValue, myOrg:contextOrg] << mySubsQuery.queryParams

        SubscriptionPrivateProperty.executeQuery(query, queryParams)
    }

    def getDueLicensePrivateProperties(Org contextOrg, java.sql.Date fromDateValue, java.sql.Date toDateValue){
        def myLicensesQuery = getMyLicensesQuery(contextOrg)
        def query = "SELECT distinct(prop) FROM LicensePrivateProperty as prop WHERE (dateValue >= :from and dateValue <= :to)" +
                "and exists (select pd from PropertyDefinition as pd where prop.type = pd AND pd.tenant = :myOrg) " +
                "and owner in ( " + myLicensesQuery.query + " )"
        def queryParams = [from:fromDateValue, to:toDateValue, myOrg:contextOrg] << myLicensesQuery.queryParams

        LicensePrivateProperty.executeQuery(query, queryParams)
    }

    private def getMySubscriptionsQuery(Org contextOrg){
        getDueSubscriptionsQuery(contextOrg, null, null, null, null)
    }

    def getDueSubscriptions(Org contextOrg, java.sql.Date endDateFrom, java.sql.Date endDateTo, java.sql.Date manualCancellationDateFrom, java.sql.Date manualCancellationDateTo) {
        def query = getDueSubscriptionsQuery(contextOrg, endDateFrom, endDateTo, manualCancellationDateFrom, manualCancellationDateTo)
        Subscription.executeQuery(query.query, query.queryParams)
    }

    private def getDueSubscriptionsQuery(Org contextOrg, java.sql.Date endDateFrom, java.sql.Date endDateTo, java.sql.Date manualCancellationDateFrom, java.sql.Date manualCancellationDateTo) {
        def result = [:]
        Org institution = contextOrg
        def base_qry
        def qry_params
        boolean isSubscriptionConsortia = ((OR_TYPE_CONSORTIUM in institution.getallOrgRoleType()))
        boolean isSubscriber = ! isSubscriptionConsortia

        if (isSubscriber) {
            base_qry = "from Subscription as s where ( "+
                    "exists ( select o from s.orgRelations as o where ( ( o.roleType = :roleType1 or o.roleType = :roleType2 ) AND o.org = :activeInst ) ) "+
                    "AND ( s.status.value != 'Deleted' ) "+
                    "AND ( "+
                    "( not exists ( select o from s.orgRelations as o where o.roleType = :scRoleType ) ) "+
                    "or "+
                    "( ( exists ( select o from s.orgRelations as o where o.roleType = :scRoleType ) ) AND ( s.instanceOf is not null) ) "+
                    ") "+
                    ")"
            qry_params = ['roleType1':OR_SUBSCRIBER, 'roleType2':OR_SUBSCRIBER_CONS, 'activeInst':institution, 'scRoleType':OR_SUBSCRIPTION_CONSORTIA]
        }

        if (isSubscriptionConsortia) {
            base_qry = " from Subscription as s where  ( ( exists ( select o from s.orgRelations as o where ( o.roleType = :roleType AND o.org = :activeInst ) ) ) ) AND ( s.instanceOf is null AND s.status.value != 'Deleted' ) "
            qry_params = ['roleType':OR_SUBSCRIPTION_CONSORTIA, 'activeInst':institution]
        }

        if (endDateFrom && endDateTo) {
            base_qry += " and (endDate >= :endFrom and endDate <= :endTo)"
            qry_params.put("endFrom", endDateFrom)
            qry_params.put("endTo", endDateTo)
        }

        if (manualCancellationDateFrom && manualCancellationDateTo){
            base_qry +=" or (manualCancellationDate >= :cancellFrom and manualCancellationDate <= :cancellTo) "
            qry_params.put("cancellFrom", manualCancellationDateFrom)
            qry_params.put("cancellTo", manualCancellationDateTo)
        }

        base_qry += " and status != :status "
        qry_params.put("status", SUBSCRIPTION_DELETED)

        result.query = "select s ${base_qry}"
        result.queryParams = qry_params
        result
    }

    private def getMyLicensesQuery(Org institution){
        def template_license_type = RefdataValue.getByValueAndCategory('Template', 'License Type')
        def result = [:]
        def base_qry
        def qry_params
        boolean isLicensingConsortium = ((OR_TYPE_CONSORTIUM in institution.getallOrgRoleType()))
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