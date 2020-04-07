package de.laser

import com.k_int.kbplus.Org
import com.k_int.kbplus.RefdataValue
import de.laser.helper.DateUtil
import de.laser.helper.RDStore
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap

import java.text.SimpleDateFormat

class SubscriptionsQueryService {
    def genericOIDService
    def propertyService
    def accessService

    List myInstitutionCurrentSubscriptionsBaseQuery(params, Org contextOrg) {

        def date_restriction
        SimpleDateFormat sdf = DateUtil.getSDF_NoTime()
        boolean filterSet = false
        if (params.validOn == null || params.validOn.trim() == '') {
            date_restriction = null
        } else {
            date_restriction = sdf.parse(params.validOn)
            filterSet = true
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

        RefdataValue role_sub            = RDStore.OR_SUBSCRIBER
        RefdataValue role_subCons        = RDStore.OR_SUBSCRIBER_CONS
        RefdataValue role_sub_consortia  = RDStore.OR_SUBSCRIPTION_CONSORTIA
        RefdataValue role_subColl        = RDStore.OR_SUBSCRIBER_COLLECTIVE
        RefdataValue role_sub_collective = RDStore.OR_SUBSCRIPTION_COLLECTIVE

        // ORG: def base_qry = " from Subscription as s where  ( ( exists ( select o from s.orgRelations as o where ( o.roleType IN (:roleTypes) AND o.org = :activeInst ) ) ) ) AND ( s.status.value != 'Deleted' ) "
        // ORG: def qry_params = ['roleTypes':roleTypes, 'activeInst':contextOrg]

        String base_qry
        Map qry_params

        if (! params.orgRole) {
            if (accessService.checkPerm(contextOrg,'ORG_CONSORTIUM')) {
                params.orgRole = 'Subscription Consortia'
            }
            else if(accessService.checkPerm(contextOrg,'ORG_INST_COLLECTIVE')) {
                params.orgRole = 'Subscription Collective'
            }
            else {
                params.orgRole = 'Subscriber'
            }
        }

        if (params.orgRole == 'Subscriber') {

            base_qry = "from Subscription as s where (exists ( select o from s.orgRelations as o where ( ( o.roleType = :roleType1 or o.roleType in (:roleType2) ) AND o.org = :activeInst ) ) AND (( not exists ( select o from s.orgRelations as o where o.roleType in (:scRoleType) ) ) or ( ( exists ( select o from s.orgRelations as o where o.roleType in (:scRoleType) ) ) AND ( s.instanceOf is not null) ) ) )"

            qry_params = ['roleType1':role_sub, 'roleType2':[role_subCons,role_subColl], 'activeInst':contextOrg, 'scRoleType':[role_sub_consortia,role_sub_collective]]
        }

        if (params.orgRole == 'Subscription Consortia') {
            if (params.actionName == 'manageMembers') {
                base_qry =  " from Subscription as s where ( exists ( select o from s.orgRelations as o where ( o.roleType = :roleType AND o.org = :activeInst ) ) ) " +
                            " AND s.instanceOf is not null "
                qry_params = ['roleType':role_sub_consortia, 'activeInst':contextOrg]
            } else {
                if (params.showParentsAndChildsSubs) {
                    base_qry =  " from Subscription as s where ( exists ( select o from s.orgRelations as o where ( o.roleType = :roleType AND o.org = :activeInst ) ) ) "
                    qry_params = ['roleType':role_sub_consortia, 'activeInst':contextOrg]
                } else {//nur Parents
                    base_qry =  " from Subscription as s where ( exists ( select o from s.orgRelations as o where ( o.roleType = :roleType AND o.org = :activeInst ) ) ) " +
                                " AND s.instanceOf is null "
                    qry_params = ['roleType':role_sub_consortia, 'activeInst':contextOrg]
                }
            }
        }
        else if (params.orgRole == 'Subscription Collective') {
            if (params.actionName == 'manageMembers') {
                base_qry =  " from Subscription as s where ( exists ( select o from s.orgRelations as o where ( o.roleType = :roleType AND o.org = :activeInst ) ) ) " +
                        " AND s.instanceOf is not null "
                qry_params = ['roleType':role_sub_collective, 'activeInst':contextOrg]
            } else {
                if (params.showParentsAndChildsSubs) {
                    base_qry =  " from Subscription as s where ( exists ( select o from s.orgRelations as o where ( o.roleType = :roleType AND o.org = :activeInst ) ) ) "
                    qry_params = ['roleType':role_sub_collective, 'activeInst':contextOrg]
                } else { //nur Parents
                    base_qry =  " from Subscription as s where ( exists ( select o from s.orgRelations as o where ( o.roleType = :roleType AND o.org = :activeInst ) ) ) " +
                            " AND ( s.instanceOf is null or exists ( select o2 from s.orgRelations as o2 where ( o2.roleType = :roleType2 AND o2.org = :activeInst ) ) )"
                    qry_params = ['roleType':role_sub_collective, 'roleType2': role_subCons, 'activeInst':contextOrg]
                }
            }
        }

        def consortia = params.consortia ? genericOIDService.resolveOID(params.consortia) : null
        if (consortia) {
            base_qry += " and exists ("
            base_qry += "    select cr from s.orgRelations as cr where cr.roleType = :rtSubCons"
            base_qry += "       and cr.org = :consortia"
            base_qry += " )"

            qry_params.put('rtSubCons', RDStore.OR_SUBSCRIPTION_CONSORTIA)
            qry_params.put('consortia', consortia)
        }


        if (params.identifier) {

            // globalUID based
            if (params.identifier.startsWith('org:')) {

                base_qry += "AND ( exists ( select idMatch from OrgRole as idMatch where idMatch.sub = s and idMatch.org.globalUID = :identifier ) ) "
            }
            else if (params.identifier.startsWith('license:')) {

                base_qry += "AND ( exists ( select idMatch from License as idMatch where s.owner = idMatch and idMatch.globalUID = :identifier ) ) "
            }
            else if (params.identifier.startsWith('subscription:')) {

                base_qry += "AND ( exists ( select idMatch from Subscription as idMatch where idMatch = s and idMatch.globalUID = :identifier ) ) "
            }
            else if (params.identifier.startsWith('package:')) {

                base_qry += "AND ( exists ( select idMatch from SubscriptionPackage as idMatch where idMatch.subscription = s and idMatch.pkg.globalUID = :identifier ) ) "
            }
            // identifier based
            else {
                String tmpBaseQuery1 = "( exists ( select ident from Identifier ident"
                String tmpBaseQuery2 = "and ident.value = :identifier ) )"

                base_qry += "AND ("
                base_qry += tmpBaseQuery1 + " where ident.sub = s.id " + tmpBaseQuery2 + " or "

                base_qry += tmpBaseQuery1 + ", License lic where ident.lic = lic.id and s.owner = lic " + tmpBaseQuery2 + " or "

                base_qry += tmpBaseQuery1 + ", SubscriptionPackage sp where ident.pkg = sp.pkg.id and sp.subscription = s " + tmpBaseQuery2 + " or "

                base_qry += tmpBaseQuery1 + ", TitleInstance ti, TitleInstancePackagePlatform tipp, IssueEntitlement ie " +
                        " where ident.ti = ti.id and tipp.title = ti.id and ie.tipp = tipp.id and ie.subscription = s.id " + tmpBaseQuery2  + " or "

                base_qry += tmpBaseQuery1 + ", OrgRole ro where ident.org = ro.org and ro.sub = s " + tmpBaseQuery2

                base_qry += ")"
            }

            qry_params.put('identifier', params.identifier.trim())
            filterSet = true
        }

        if (params.org) {
            base_qry += (" and  exists ( select orgR from OrgRole as orgR where orgR.sub = s and orgR.org = :org) ")
            qry_params.put('org', params.org)
            filterSet = true
        }

        if (params.provider) {
            base_qry += (" and  exists ( select orgR from OrgRole as orgR where orgR.sub = s and orgR.org.id = :provider) ")
            qry_params.put('provider', (params.provider as Long))
            filterSet = true
        }

        if (params.q?.length() > 0) {
            base_qry += (
                    " and ( genfunc_filter_matcher(s.name, :name_filter) = true " // filter by subscription
                            + " or exists ( select sp from SubscriptionPackage as sp where sp.subscription = s and genfunc_filter_matcher(sp.pkg.name, :name_filter) = true ) " // filter by pkg
                            + " or exists ( select lic from License as lic where s.owner = lic and genfunc_filter_matcher(lic.reference, :name_filter) = true ) " // filter by license
                            + " or exists ( select orgR from OrgRole as orgR where orgR.sub = s and ( "
                                + " genfunc_filter_matcher(orgR.org.name, :name_filter) = true "
                                + " or genfunc_filter_matcher(orgR.org.shortname, :name_filter) = true "
                                + " or genfunc_filter_matcher(orgR.org.sortname, :name_filter) = true "
                            + " ) ) " // filter by Anbieter, Konsortium, Agency
                        +  " ) "
            )
            qry_params.put('name_filter', "${params.q}")
            filterSet = true
        }
        // eval property filter

        if (params.filterPropDef) {
            def query = propertyService.evalFilterQuery(params, base_qry, 's', qry_params)
            base_qry = query.query
            qry_params = query.queryParams
            filterSet = true
        }

        if (date_restriction) {
            base_qry += " and s.startDate <= :date_restr and (s.endDate >= :date_restr or s.endDate is null)"
            qry_params.put('date_restr', date_restriction)
            filterSet = true
        }

        if (params.endDateFrom && params.endDateTo && params.manualCancellationDateFrom && params.manualCancellationDateTo) {
            base_qry += " and ((s.endDate >= :endFrom and s.endDate <= :endTo) OR (s.manualCancellationDate >= :cancellFrom and s.manualCancellationDate <= :cancellTo))"
            qry_params.put("endFrom", params.endDateFrom)
            qry_params.put("endTo", params.endDateTo)
            qry_params.put("cancellFrom", params.manualCancellationDateFrom)
            qry_params.put("cancellTo", params.manualCancellationDateTo)
            filterSet = true
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
                        subTypes = [params.subTypes instanceof Long ? params.subTypes : Long.parseLong(params.subTypes)]
                    }
                }
            }
            if (subTypes) {
                base_qry += " and s.type.id in (:subTypes) "
                qry_params.put('subTypes', subTypes)
            }
            filterSet = true
        }

        if (params.containsKey('subKinds')) {
            base_qry += " and s.kind.id in (:subKinds) "
            qry_params.put('subKinds', params.list('subKinds').collect { Long.parseLong(it) })
            filterSet = true
        }

        if (params.status) {

            if (params.status != 'FETCH_ALL') {
                base_qry += " and s.status.id = :status "
                qry_params.put('status', (params.status as Long))
                filterSet = true
            }
        }

        if (params.form) {
            base_qry += "and s.form.id = :form "
            qry_params.put('form', (params.form as Long))
            filterSet = true
        }

        if (params.resource) {
          base_qry += "and s.resource.id = :resource "
          qry_params.put('resource', (params.resource as Long))
            filterSet = true
        }

        if (params.isPublicForApi) {
            base_qry += "and s.isPublicForApi = :isPublicForApi "
            qry_params.put('isPublicForApi', (params.isPublicForApi == RDStore.YN_YES.id.toString()) ? true : false)
            filterSet = true
        }

        if (params.hasPerpetualAccess) {
            base_qry += "and s.hasPerpetualAccess = :hasPerpetualAccess "
            qry_params.put('hasPerpetualAccess', (params.hasPerpetualAccess == RDStore.YN_YES.id.toString()) ? true : false)
            filterSet = true
        }

        if (params.subRunTimeMultiYear || params.subRunTime) {

            if (params.subRunTimeMultiYear && !params.subRunTime) {
                base_qry += " and s.isMultiYear = :subRunTimeMultiYear "
                qry_params.put('subRunTimeMultiYear', true)
                filterSet = true
            }else if (!params.subRunTimeMultiYear && params.subRunTime){
                base_qry += " and s.isMultiYear = :subRunTimeMultiYear "
                qry_params.put('subRunTimeMultiYear', false)
                filterSet = true
            }
        }

        //ERMS-584: the symbol "§" means that the given sort parameter should not be considered in base query
        if ((params.sort != null) && (params.sort.length() > 0) && params.sort.indexOf("§") < 0) {
            base_qry += (params.sort=="s.name") ? " order by LOWER(${params.sort}) ${params.order}":" order by ${params.sort} ${params.order}"
        } else {
            base_qry += " order by lower(trim(s.name)) asc"
        }

        //log.debug("query: ${base_qry} && params: ${qry_params}")

        return [base_qry, qry_params, filterSet]
    }
}
