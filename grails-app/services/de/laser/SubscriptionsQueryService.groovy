package de.laser


import de.laser.utils.DateUtils
import de.laser.storage.RDStore
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap

import java.text.SimpleDateFormat
import java.time.Year

/**
 * This service generates compley subscription queries
 */
@Transactional
class SubscriptionsQueryService {
    AccessService accessService
    GenericOIDService genericOIDService
    PropertyService propertyService

    /**
     *
     * @param params
     * @param contextOrg
     * @param joinQuery an eventual join if further tables need to be accessed by an optional filter
     * @return the base query data in structure:
     * <ol start="0">
     *     <li>base_qry the query string</li>
     *     <li>qry_params the query parameters</li>
     *     <li>filterSet the flag for the export whether a filter has been applied</li>
     * </ol>
     */
    List myInstitutionCurrentSubscriptionsBaseQuery(params, Org contextOrg, String joinQuery = "") {

        def date_restriction
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        boolean filterSet = false
        if (params.validOn == null || params.validOn.trim() == '') {
            date_restriction = null
        } else {
            date_restriction = sdf.parse(params.validOn)
            filterSet = true
        }

        RefdataValue role_sub            = RDStore.OR_SUBSCRIBER
        RefdataValue role_subCons        = RDStore.OR_SUBSCRIBER_CONS
        RefdataValue role_sub_consortia  = RDStore.OR_SUBSCRIPTION_CONSORTIA

        // ORG: def base_qry = " from Subscription as s where  ( ( exists ( select o from s.orgRelations as o where ( o.roleType IN (:roleTypes) AND o.org = :activeInst ) ) ) ) AND ( s.status.value != 'Deleted' ) "
        // ORG: def qry_params = ['roleTypes':roleTypes, 'activeInst':contextOrg]

        String base_qry = ''
        Map qry_params = [:]

        if (! params.orgRole) {
            if (accessService.checkPerm(contextOrg,'ORG_CONSORTIUM')) {
                params.orgRole = 'Subscription Consortia'
            }
            else {
                params.orgRole = 'Subscriber'
            }
        }

        if (params.orgRole == 'Subscriber') {

            base_qry = " from Subscription as s ${joinQuery} where (exists ( select o from s.orgRelations as o where ( ( o.roleType = :roleType1 or o.roleType in (:roleType2) ) AND o.org = :activeInst ) ) AND (( not exists ( select o from s.orgRelations as o where o.roleType in (:scRoleType) ) ) or ( ( exists ( select o from s.orgRelations as o where o.roleType in (:scRoleType) ) ) AND ( s.instanceOf is not null) ) ) )"

            qry_params << ['roleType1':role_sub, 'roleType2':[role_subCons], 'activeInst':contextOrg, 'scRoleType':[role_sub_consortia]]
        }

        if (params.orgRole == 'Subscription Consortia') {
            if (params.actionName == 'manageMembers') {
                base_qry =  " from Subscription as s ${joinQuery} where ( exists ( select o from s.orgRelations as o where ( o.roleType = :roleType AND o.org = :activeInst ) ) ) " +
                            " AND s.instanceOf is not null "
                qry_params << ['roleType':role_sub_consortia, 'activeInst':contextOrg]
            } else {
                if (params.showParentsAndChildsSubs) {
                    base_qry =  " from Subscription as s ${joinQuery} where ( exists ( select o from s.orgRelations as o where ( o.roleType = :roleType AND o.org = :activeInst ) ) ) "
                    qry_params << ['roleType':role_sub_consortia, 'activeInst':contextOrg]
                } else {//nur Parents
                    base_qry =  " from Subscription as s ${joinQuery} where ( exists ( select o from s.orgRelations as o where ( o.roleType = :roleType AND o.org = :activeInst ) ) ) " +
                                " AND s.instanceOf is null "
                    qry_params << ['roleType':role_sub_consortia, 'activeInst':contextOrg]
                }
            }
        }

        if(params.license) {
            base_qry += " and exists (select l from Links l where l.sourceLicense = :lic and l.linkType = :linkType and l.destinationSubscription = s)"
            qry_params.put('lic',License.get(params.license))
            qry_params.put('linkType',RDStore.LINKTYPE_LICENSE)
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

                base_qry += "AND ( exists ( select idMatch.id from OrgRole as idMatch where idMatch.sub = s and idMatch.org.globalUID = :identifier ) ) "
            }
            else if (params.identifier.startsWith('license:')) {

                base_qry += "AND ( exists ( select idMatch.id from Links li join li.sourceLicense idMatch where li.destinationSubscription = s and li.linkType = :linkType and idMatch.globalUID = :identifier ) ) "
                qry_params.put('linkType',RDStore.LINKTYPE_LICENSE)
            }
            else if (params.identifier.startsWith('subscription:')) {

                base_qry += "AND ( exists ( select idMatch.id from Subscription as idMatch where idMatch = s and idMatch.globalUID = :identifier ) ) "
            }
            else if (params.identifier.startsWith('package:')) {

                base_qry += "AND ( exists ( select idMatch.id from SubscriptionPackage as idMatch where idMatch.subscription = s and idMatch.pkg.globalUID = :identifier ) ) "
            }
            // identifier based
            else {
                base_qry += "AND ( exists ( select ident.id from Identifier ident"

                base_qry += " where ( ident.sub.id = s.id or "

                //base_qry += "ident.lic.id in (select li.sourceLicense.id from Links li where li.destinationSubscription.id = s.id and li.linkType = :linkType) "
                base_qry += "ident.lic.id in (select li.sourceLicense.id from Links li where li.destinationSubscription.id = s.id and li.linkType = :linkType) or "
                qry_params.put('linkType', RDStore.LINKTYPE_LICENSE)

                //base_qry += "ident.pkg.id in (select sp.pkg.id from SubscriptionPackage sp where sp.subscription.id = s.id) "
                base_qry += "ident.pkg.id in (select sp.pkg.id from SubscriptionPackage sp where sp.subscription.id = s.id) or "

                //base_qry += "ident.tipp.id in (select ie.tipp.id from IssueEntitlement ie where ie.subscription.id = s.id) "
                base_qry += "ident.tipp.id in (select ie.tipp.id from IssueEntitlement ie where ie.subscription.id = s.id) or "

                base_qry += "ident.org.id in (select ro.org.id from OrgRole ro where ro.sub.id = s.id) "

                base_qry += ") and ident.value = :identifier ) )"
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

        if (params.providers && params.providers != "") {
            base_qry += (" and  exists ( select orgR from OrgRole as orgR where orgR.sub = s and orgR.org.id in (:providers)) ")
            if (params instanceof GrailsParameterMap) {
                qry_params.put('providers', (params.list('providers').collect { Long.parseLong(it) }))
            } else {
                if (params.providers instanceof List<String>) {
                    qry_params.put('providers', (params.providers.collect { Long.parseLong(it) }))
                } else {
                    if (params.providers instanceof List<Long>) {
                        qry_params.put('providers', (params.providers))
                    }
                }
            }

            filterSet = true
        }

        if (params.q?.length() > 0) {
            base_qry += (
                    " and ( genfunc_filter_matcher(s.name, :name_filter) = true " + // filter by subscription
                            " or exists ( select sp from SubscriptionPackage as sp where sp.subscription = s and genfunc_filter_matcher(sp.pkg.name, :name_filter) = true ) " + // filter by pkg
                            " or exists ( select li.sourceLicense from Links li where li.destinationSubscription = s and li.linkType = :linkType and genfunc_filter_matcher(li.sourceLicense.reference, :name_filter) = true ) " + // filter by license
                            " or exists ( select orgR from OrgRole as orgR where orgR.sub = s and" +
                            "   orgR.roleType in (:subRoleTypes) and ( " +
                                " genfunc_filter_matcher(orgR.org.name, :name_filter) = true " +
                                " or genfunc_filter_matcher(orgR.org.shortname, :name_filter) = true " +
                                " or genfunc_filter_matcher(orgR.org.sortname, :name_filter) = true " +
                            " ) ) " + // filter by Anbieter, Konsortium, Agency
                         " ) "
            )
            qry_params.put('name_filter', params.q)
            qry_params.put('linkType', RDStore.LINKTYPE_LICENSE)
            qry_params.put('subRoleTypes', [RDStore.OR_AGENCY, RDStore.OR_PROVIDER])
            if(params.orgRole != "Subscription Consortia")
                qry_params.subRoleTypes.add(RDStore.OR_SUBSCRIPTION_CONSORTIA)
            filterSet = true
        }
        // eval property filter

        if (params.filterPropDef) {
            Map<String, Object> query = propertyService.evalFilterQuery(params, base_qry, 's', qry_params)
            base_qry = query.query
            qry_params.putAll(query.queryParams)
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
                if(params.status instanceof List || params.status instanceof String[]){
                    base_qry += " and (s.status.id in (:status) "
                    qry_params.put('status', params.status.collect { it instanceof Long ? it : Long.parseLong(it) })
                    filterSet = true
                }else {
                    base_qry += " and (s.status.id = :status "
                    qry_params.put('status', (params.status as Long))
                    filterSet = true
                }
            }
            if(RDStore.SUBSCRIPTION_CURRENT.id in qry_params.status) {
                /*
                needs to be dealt separately, must not be and-linked
                */
                if (params.hasPerpetualAccess) {
                    if(params.hasPerpetualAccess == RDStore.YN_YES.id.toString()) {
                        base_qry += "or s.hasPerpetualAccess = :hasPerpetualAccess) "
                        qry_params.put('hasPerpetualAccess', true)
                    }
                    else if(params.hasPerpetualAccess == RDStore.YN_NO.id.toString()) {
                        base_qry += "and s.hasPerpetualAccess = :hasPerpetualAccess) "
                        qry_params.put('hasPerpetualAccess', false)
                    }
                    filterSet = true
                }
                else base_qry += ")" //opened in line 268 or 272
            }
            else if(params.status != 'FETCH_ALL') base_qry += ")" //opened in line 268 or 272
        }
        if (!(RDStore.SUBSCRIPTION_CURRENT.id.toString() in params.status) && params.hasPerpetualAccess) {
            base_qry += " and s.hasPerpetualAccess = :hasPerpetualAccess "
            qry_params.put('hasPerpetualAccess', (params.hasPerpetualAccess == RDStore.YN_YES.id.toString()) ? true : false)
            filterSet = true
        }


        if (params.form) {
            base_qry += " and s.form.id in (:form) "
            qry_params.put('form', params.list("form").collect { Long.parseLong(it) })
            filterSet = true
        }

        if (params.resource) {
          base_qry += " and s.resource.id in (:resources) "
          qry_params.put('resources', params.list("resource").collect { Long.parseLong(it) })
            filterSet = true
        }

        if (params.isPublicForApi) {
            base_qry += " and s.isPublicForApi = :isPublicForApi "
            qry_params.put('isPublicForApi', (params.isPublicForApi == RDStore.YN_YES.id.toString()) ? true : false)
            filterSet = true
        }

        if (params.hasPublishComponent) {
            base_qry += " and s.hasPublishComponent = :hasPublishComponent "
            qry_params.put('hasPublishComponent', (params.hasPublishComponent == RDStore.YN_YES.id.toString()) ? true : false)
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

        if (params.referenceYears) {
            base_qry += " and s.referenceYear in (:referenceYears) "
            Set<Year> referenceYears = []
            params.list('referenceYears').each { String referenceYear ->
                referenceYears << Year.parse(referenceYear)
            }
            qry_params.put('referenceYears', referenceYears)
            filterSet = true
        }

        if ((params.sort != null) && (params.sort.length() > 0)) {
            if(params.sort != "providerAgency")
                base_qry += (params.sort=="s.name") ? " order by LOWER(${params.sort}) ${params.order}":" order by ${params.sort} ${params.order}"
        } else {
            base_qry += " order by lower(trim(s.name)) asc, s.startDate, s.endDate, s.instanceOf desc"
            if(joinQuery)
                base_qry += ", so.org.sortname asc"
        }

        //log.debug("query: ${base_qry} && params: ${qry_params}")

        return [base_qry, qry_params, filterSet]
    }
}
