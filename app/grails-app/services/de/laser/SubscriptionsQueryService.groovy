package de.laser

import com.k_int.kbplus.Combo
import com.k_int.kbplus.Org
import com.k_int.kbplus.OrgCustomProperty
import com.k_int.kbplus.RefdataValue
import com.k_int.kbplus.Subscription
import com.k_int.properties.PropertyDefinition
import de.laser.helper.DateUtil
import de.laser.helper.RDStore
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap

import javax.naming.Context


class SubscriptionsQueryService {
    def propertyService

    def myInstitutionCurrentSubscriptionsBaseQuery(def params, Org contextOrg) {

        def date_restriction
        def sdf = new DateUtil().getSimpleDateFormat_NoTime()

        if (params.validOn == null || params.validOn.trim() == '') {
            date_restriction = null
        } else {
            date_restriction = sdf.parse(params.validOn)
        }

        /*
        def dateBeforeFilter = null;
        def dateBeforeFilterVal = null;
        if(params.dateBeforeFilter && params.dateBeforeVal){
            if(params.dateBeforeFilter == "renewalDate"){
                dateBeforeFilter = " and s.manualRenewalDate < :date_before"
                dateBeforeFilterVal =sdf.parse(params.dateBeforeVal)
            }else if (params.dateBeforeFilter == "endDate"){
                dateBeforeFilter = " and s.endDate < :date_before"
                dateBeforeFilterVal =sdf.parse(params.dateBeforeVal)
            }else{
              result.remove('dateBeforeFilterVal')
              result.remove('dateBeforeFilter')
            }

        }
        */

        def role_sub            = RDStore.OR_SUBSCRIBER
        def role_subCons        = RDStore.OR_SUBSCRIBER_CONS
        def role_sub_consortia  = RDStore.OR_SUBSCRIPTION_CONSORTIA

        // ORG: def base_qry = " from Subscription as s where  ( ( exists ( select o from s.orgRelations as o where ( o.roleType IN (:roleTypes) AND o.org = :activeInst ) ) ) ) AND ( s.status.value != 'Deleted' ) "
        // ORG: def qry_params = ['roleTypes':roleTypes, 'activeInst':contextOrg]

        def base_qry
        def qry_params

        if (! params.orgRole) {
            if ((RDStore.OR_TYPE_CONSORTIUM?.id in contextOrg?.getallOrgRoleTypeIds())) {
                params.orgRole = 'Subscription Consortia'
            }
            else {
                params.orgRole = 'Subscriber'
            }
        }

        if (params.orgRole == 'Subscriber') {

            base_qry = """
from Subscription as s where (
    exists ( select o from s.orgRelations as o where ( ( o.roleType = :roleType1 or o.roleType = :roleType2 ) AND o.org = :activeInst ) ) 
    AND (
        ( not exists ( select o from s.orgRelations as o where o.roleType = :scRoleType ) )
        or
        ( ( exists ( select o from s.orgRelations as o where o.roleType = :scRoleType ) ) AND ( s.instanceOf is not null) )
    )
)
"""

            qry_params = ['roleType1':role_sub, 'roleType2':role_subCons, 'activeInst':contextOrg, 'scRoleType':role_sub_consortia]
        }

        if (params.orgRole == 'Subscription Consortia') {
            if (params.actionName == 'manageConsortia') {
                base_qry = " from Subscription as s where  ( ( exists ( select o from s.orgRelations as o where ( o.roleType = :roleType AND o.org = :activeInst ) ) ) ) AND ( s.instanceOf is not null AND s.status.value != 'Deleted' ) "
                qry_params = ['roleType':role_sub_consortia, 'activeInst':contextOrg]
            } else {
                base_qry = " from Subscription as s where  ( ( exists ( select o from s.orgRelations as o where ( o.roleType = :roleType AND o.org = :activeInst ) ) ) ) AND ( s.instanceOf is null AND s.status.value != 'Deleted' ) "
                qry_params = ['roleType':role_sub_consortia, 'activeInst':contextOrg]
            }
        }

        if (params.org) {
            base_qry += (" and  exists ( select orgR from OrgRole as orgR where orgR.sub = s and orgR.org = :org) ")
            qry_params.put('org', params.org)
        }

        if (params.q?.length() > 0) {
            base_qry += (
                    " and ( lower(s.name) like :name_filter " // filter by subscription
                            + " or exists ( select sp from SubscriptionPackage as sp where sp.subscription = s and ( lower(sp.pkg.name) like :name_filter ) ) " // filter by pkg
                            + " or exists ( select lic from License as lic where s.owner = lic and ( lower(lic.reference) like :name_filter ) ) " // filter by license
                            + " or exists ( select orgR from OrgRole as orgR where orgR.sub = s and ( lower(orgR.org.name) like :name_filter"
                            + " or lower(orgR.org.shortname) like :name_filter or lower(orgR.org.sortname) like :name_filter) ) " // filter by Anbieter, Konsortium, Agency
                            +  " ) "
            )

            qry_params.put('name_filter', "%${params.q.trim().toLowerCase()}%");
        }

        // eval property filter

        if (params.filterPropDef) {
            def query = propertyService.evalFilterQuery(params, base_qry, 's', qry_params)
            base_qry = query.query
            qry_params = query.queryParams
        }

        if (date_restriction) {
            base_qry += " and s.startDate <= :date_restr and (s.endDate >= :date_restr or s.endDate is null)"
            qry_params.put('date_restr', date_restriction)
        }

        if (params.endDateFrom && params.endDateTo && params.manualCancellationDateFrom && params.manualCancellationDateTo) {
            base_qry += " and ((s.endDate >= :endFrom and s.endDate <= :endTo) OR (s.manualCancellationDate >= :cancellFrom and s.manualCancellationDate <= :cancellTo))"
            qry_params.put("endFrom", params.endDateFrom)
            qry_params.put("endTo", params.endDateTo)
            qry_params.put("cancellFrom", params.manualCancellationDateFrom)
            qry_params.put("cancellTo", params.manualCancellationDateTo)
        } else {
            if (params.endDateFrom && params.endDateTo) {
                base_qry += " and (s.endDate >= :endFrom and s.endDate <= :endTo)"
                qry_params.put("endFrom", params.endDateFrom)
                qry_params.put("endTo", params.endDateTo)
            } else if (params.manualCancellationDateFrom && params.manualCancellationDateTo) {
                base_qry += " and (s.manualCancellationDate >= :cancellFrom and s.manualCancellationDate <= :cancellTo)"
                qry_params.put("cancellFrom", params.manualCancellationDateFrom)
                qry_params.put("cancellTo", params.manualCancellationDateTo)
            }
        }

        /* if(dateBeforeFilter ){
            base_qry += dateBeforeFilter
            qry_params.put('date_before', dateBeforeFilterVal)
        } */

        def subTypes = []
        if (params.containsKey('subTypes')) {
            if (params instanceof GrailsParameterMap) {
                params.list('subTypes').each{
                    subTypes.add(Long.parseLong(it))
                }
            } else { //TODO refactoring this bugfix
                if (params.subTypes instanceof List<String>) {
                    params.subTypes.each{
                        subTypes.add(Long.parseLong(it))
                    }
                } else {
                    if (params.subTypes instanceof List<Long>) {
                        subTypes = params.subTypes
                    } else {
                        subTypes = [params.subTypes instanceof Long ?: Long.parseLong(params.subTypes)]
                    }
                }
            }
            if (subTypes) {
                base_qry += " and s.type.id in (:subTypes) "
                qry_params.put('subTypes', subTypes)
            }
        }

        if (params.status) {

            if ((params.status as Long) == RefdataValue.getByValueAndCategory('subscription.status.no.status.set.but.null','filter.fake.values').id) {
                base_qry += " AND s.status is null "
            }
            else {
                base_qry += " and s.status.id = :status "
                qry_params.put('status', (params.status as Long))
            }

        } else {
            base_qry += " AND ( s.status.value != 'Deleted' ) "
        }


        if (params.form) {
            base_qry += "and s.form.id = :form "
            qry_params.put('form', (params.form as Long))
        }

        if (params.resource) {
          base_qry += "and s.resource.id = :resource "
          qry_params.put('resource', (params.resource as Long))
        }

        //ERMS-584: the symbol "§" means that the given sort parameter should not be considered in base query
        if ((params.sort != null) && (params.sort.length() > 0) && params.sort.indexOf("§") < 0) {
            base_qry += (params.sort=="s.name") ? " order by LOWER(${params.sort}) ${params.order}":" order by ${params.sort} ${params.order}"
        } else {
            base_qry += " order by lower(trim(s.name)) asc"
        }

        //log.debug("query: ${base_qry} && params: ${qry_params}")

        return [base_qry, qry_params]
    }
}
