package de.laser


import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.utils.DateUtils
import de.laser.storage.RDStore
import de.laser.properties.PropertyDefinition
import de.laser.survey.SurveyConfig
import de.laser.utils.LocaleUtils
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource
import groovy.sql.Sql

import java.sql.Connection
import java.sql.Timestamp
import java.text.DateFormat
import java.text.SimpleDateFormat

/**
 * This service handles generic query creating with inclusive filter processing
 */
@Transactional
class FilterService {

    private static final Long FAKE_CONSTRAINT_ORGID_WITHOUT_HITS = new Long(-1)

    ContextService contextService
    GenericOIDService genericOIDService
    MessageSource messageSource
    PropertyService propertyService

    /**
     * Processes organisation filters and generates a query to fetch organisations
     * @param params the filter parameter map
     * @return the map containing the query and the prepared query parameters
     */
    Map<String, Object> getOrgQuery(GrailsParameterMap params) {
        Map<String, Object> result = [:]
        ArrayList<String> query = []
        Map<String, Object> queryParams = [:]

        if (params.orgNameContains?.length() > 0) {
            query << "((genfunc_filter_matcher(o.name, :orgNameContains) = true or genfunc_filter_matcher(o.sortname, :orgNameContains) = true) or exists(select alt.id from AlternativeName alt where alt.org = o and genfunc_filter_matcher(alt.name, :orgNameContains) = true) )"
             queryParams << [orgNameContains : "${params.orgNameContains}"]
        }
        if (params.orgType) {
            if (params.orgType instanceof List) {
                query << " exists (select roletype from o.orgType as roletype where roletype.id in (:orgType) )"
                queryParams << [orgType: params.orgType]
            } else if (params.orgType.length() > 0) {
                query << " exists (select roletype from o.orgType as roletype where roletype.id = :orgType )"
                queryParams << [orgType: Long.parseLong(params.orgType)]
            }
        }
        if (params.orgStatus) {
            List selectedStatus = listReaderWrapper(params, 'orgStatus').collect { key -> key instanceof String ? Long.parseLong(key) : key }
            query << "o.status.id in (:orgStatus)"
            queryParams.orgStatus = selectedStatus
        }
        else {
            query << "o.status != :orgStatus"
            queryParams.orgStatus = RDStore.ORG_STATUS_REMOVED
        }
        if (params.orgRole?.length() > 0) {
            query << " exists (select ogr from o.links as ogr where ogr.roleType.id = :orgRole )"
             queryParams << [orgRole : Long.parseLong(params.orgRole)]
        }
        if (params.orgSector?.length() > 0) {
            query << "o.sector.id = :orgSector"
             queryParams << [orgSector : Long.parseLong(params.orgSector)]
        }
        if (params.orgIdentifier?.length() > 0) {
            query << " ( exists (select ident from Identifier ident join ident.org ioorg " +
                     " where ioorg = o and genfunc_filter_matcher(ident.value, :orgIdentifier) = true ) or " +
                     " ( exists ( select ci from CustomerIdentifier ci where ci.customer = o and genfunc_filter_matcher(ci.value, :orgIdentifier) = true ) ) " +
                     " ) "
            queryParams << [orgIdentifier: params.orgIdentifier]
        }
        if (params.identifierNamespace?.size() > 0) {
            query << " ( exists ( select ident from Identifier ident join ident.org ioorg where ioorg = o and ident.ns in (:namespaces) and ident.value != null and ident.value != '' and ident.value != '${IdentifierNamespace.UNKNOWN}' ) )"
            queryParams << [namespaces: []]
            params.list('identifierNamespace').each { String idnsKey ->
                queryParams.namespaces << IdentifierNamespace.get(idnsKey)
            }
        }
        if (params.customerIDNamespace?.size() > 0) {
            List<String> customerIDClause = []
            List<String> fields = params.list('customerIDNamespace')
            fields.each { String field ->
                customerIDClause << "id.${field} != null"
            }
            query << " ( exists ( select customerID from CustomerIdentifier customerID where customerID.customer = o and ( ${customerIDClause.join(' or ')} ) ) ) "
        }

        if (params.region?.size() > 0) {
            query << "o.region.id in (:region)"
            List<String> selRegions = params.list("region")
            List<Long> regions = []
            selRegions.each { String sel ->
                regions << Long.parseLong(sel)
            }
            queryParams << [region : regions]
        }

        if (params.subjectGroup?.size() > 0) {
            query << "exists (select osg from OrgSubjectGroup as osg where osg.org.id = o.id and osg.subjectGroup.id in (:subjectGroup))"
            queryParams << [subjectGroup : params.list("subjectGroup").collect {Long.parseLong(it)}]
        }

        if (params.libraryNetwork?.size() > 0) {
            query << "o.libraryNetwork.id in (:libraryNetwork)"
            List<String> selLibraryNetworks = params.list("libraryNetwork")
            List<Long> libraryNetworks = []
            selLibraryNetworks.each { String sel ->
                libraryNetworks << Long.parseLong(sel)
            }
            queryParams << [libraryNetwork : libraryNetworks]
        }
        if (params.libraryType?.size() > 0) {
            query << "o.libraryType.id in (:libraryType)"
            List<String> selLibraryTypes = params.list("libraryType")
            List<Long> libraryTypes = []
            selLibraryTypes.each { String sel ->
                libraryTypes << Long.parseLong(sel)
            }
            queryParams << [libraryType : libraryTypes]
        }
        if (params.country?.size() > 0) {
            query << "o.country.id in (:country)"
            List<String> selCountries = params.list("country")
            List<Long> countries = []
            selCountries.each { String sel ->
                countries << Long.parseLong(sel)
            }
            queryParams << [country : countries]
        }

        if (params.customerType?.length() > 0) {
            query << "exists (select oss from OrgSetting as oss where oss.org.id = o.id and oss.key = :customerTypeKey and oss.roleValue.id = :customerType)"
            queryParams << [customerType : Long.parseLong(params.customerType)]
            queryParams << [customerTypeKey : OrgSetting.KEYS.CUSTOMER_TYPE]
        }

        if (params.isLegallyObliged in ['yes', 'no']) {
            query << "o.legallyObligedBy " + (params.isLegallyObliged == 'yes' ? "is not null" : "is null")
        }

        if (params.legallyObligedBy?.length() > 0) {
            query << "o.legallyObligedBy.id in (:legallyObligedBy)"
            queryParams << [legallyObligedBy: listReaderWrapper(params, 'legallyObligedBy').collect { key -> Long.parseLong(key) }]
        }

        if (params.platform?.length() > 0) {
            query << "exists (select plat.id from Platform plat where plat.org = o and genfunc_filter_matcher(plat.name, :platform) = true)"
            queryParams << [platform: params.platform]
        }

        if (params.privateContact?.length() > 0) {
            query << "exists (select p.id from o.prsLinks op join op.prs p where p.tenant = :ctx and (genfunc_filter_matcher(p.first_name, :contact) = true or genfunc_filter_matcher(p.middle_name, :contact) = true or genfunc_filter_matcher(p.last_name, :contact) = true))"
            queryParams << [ctx: contextService.getOrg()]
            queryParams << [contact: params.privateContact]
        }

        // hack: applying filter on org subset
        if (params.containsKey("constraint_orgIds") && params.constraint_orgIds?.size() < 1) {
            query << "o.id = :emptyConstraintOrgIds"
             queryParams << [emptyConstraintOrgIds : FAKE_CONSTRAINT_ORGID_WITHOUT_HITS]
        }else if (params.constraint_orgIds?.size() > 0) {
            query << "o.id in ( :constraint_orgIds )"
             queryParams << [constraint_orgIds : params.constraint_orgIds]
        }

        String defaultOrder = " order by " + (params.sort ?: " LOWER(o.sortname)") + " " + (params.order ?: "asc")

        if (query.size() > 0) {
            result.query = "from Org o where " + query.join(" and ") + defaultOrder
        } else {
            result.query = "from Org o " + defaultOrder
        }
        result.queryParams = queryParams

        log.debug(result.toMapString())
        result
    }

    /**
     * Processes institution filters and generates a query to fetch institution; in addition, institutions
     * linked by combo to the given institution are fetched
     * @param params the filter parameter map
     * @param org the consortium to which the institutions are linked
     * @return the map containing the query and the prepared query parameters
     */
    Map<String, Object> getOrgComboQuery(GrailsParameterMap params, Org org) {
        Map<String, Object> result = [:]
        ArrayList<String> query = ["(o.status is null or o.status != :orgStatus)"]
        Map<String, Object> queryParams = ["orgStatus" : RDStore.ORG_STATUS_DELETED]

        // ERMS-1592, ERMS-1596
        if (params.orgNameContains?.length() > 0) {
            query << "(genfunc_filter_matcher(o.name, :orgNameContains1) = true or genfunc_filter_matcher(o.sortname, :orgNameContains2) = true) "
             queryParams << [orgNameContains1 : "${params.orgNameContains}"]
             queryParams << [orgNameContains2 : "${params.orgNameContains}"]
        }
        if (params.orgType?.length() > 0) {
            query << " exists (select roletype from o.orgType as roletype where roletype.id = :orgType )"
             queryParams << [orgType : Long.parseLong(params.orgType)]
        }
        if (params.orgSector?.length() > 0) {
            query << "o.sector.id = :orgSector"
             queryParams << [orgSector : Long.parseLong(params.orgSector)]
        }
        if (params.region?.size() > 0) {
            query << "o.region.id in (:region)"
            List<String> selRegions = params.list("region")
            List<Long> regions = []
            selRegions.each { String sel ->
                regions << Long.parseLong(sel)
            }
            queryParams << [region : regions]
        }
        if (params.country?.size() > 0) {
            query << "o.country.id in (:country)"
            List<String> selCountries = params.list("country")
            List<Long> countries = []
            selCountries.each { String sel ->
                countries << Long.parseLong(sel)
            }
            queryParams << [country : countries]
        }

        if (params.subjectGroup?.size() > 0) {
            query << "exists (select osg from OrgSubjectGroup as osg where osg.org.id = o.id and osg.subjectGroup.id in (:subjectGroup))"
            queryParams << [subjectGroup : params.list("subjectGroup").collect {Long.parseLong(it)}]
        }

        if (params.libraryNetwork?.size() > 0) {
            query << "o.libraryNetwork.id in (:libraryNetwork)"
            List<String> selLibraryNetworks = params.list("libraryNetwork")
            List<Long> libraryNetworks = []
            selLibraryNetworks.each { String sel ->
                libraryNetworks << Long.parseLong(sel)
            }
            queryParams << [libraryNetwork : libraryNetworks]
        }

        if (params.libraryType?.size() > 0) {
            query << "o.libraryType.id in (:libraryType)"
            List<String> selLibraryTypes = params.list("libraryType")
            List<Long> libraryTypes = []
            selLibraryTypes.each { String sel ->
                libraryTypes << Long.parseLong(sel)
            }
            queryParams << [libraryType : libraryTypes]
        }

        if (params.subStatus || params.subValidOn || params.subPerpetual) {
            String subQuery = "exists (select oo.id from OrgRole oo join oo.sub sub join sub.orgRelations ooCons where oo.org.id = o.id and oo.roleType in (:subscrRoles) and ooCons.org = :context and ooCons.roleType = :consType"
            if(params.invertDirection) {
                subQuery = "exists (select oo.id from OrgRole oo join oo.sub sub join sub.orgRelations ooCons where oo.org = :context and oo.roleType in (:subscrRoles) and ooCons.org.id = o.id and ooCons.roleType = :consType"
                queryParams << [subscrRoles: [RDStore.OR_SUBSCRIBER_CONS], consType: RDStore.OR_SUBSCRIPTION_CONSORTIA, context: contextService.getOrg()]
            }
            else
                queryParams << [subscrRoles: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN], consType: RDStore.OR_SUBSCRIPTION_CONSORTIA, context: contextService.getOrg()]
            if (params.subStatus) {
                subQuery +=  " and (sub.status = :subStatus" // ( closed in line 213; needed to prevent consortia members without any subscriptions because or would lift up the other restrictions)
                RefdataValue subStatus = RefdataValue.get(params.subStatus)
                queryParams << [subStatus: subStatus]
                if (!params.subValidOn && params.subPerpetual && subStatus == RDStore.SUBSCRIPTION_CURRENT)
                    subQuery += " or sub.hasPerpetualAccess = true"
                subQuery += ")"
            }
            if (params.subValidOn || params.subPerpetual) {
                if (params.subValidOn && !params.subPerpetual) {
                    subQuery += " and (sub.startDate <= :validOn or sub.startDate is null) and (sub.endDate >= :validOn or sub.endDate is null)"
                    queryParams << [validOn: DateUtils.parseDateGeneric(params.subValidOn)]
                }
                else if (params.subValidOn && params.subPerpetual) {
                    subQuery += " and ((sub.startDate <= :validOn or sub.startDate is null) and (sub.endDate >= :validOn or sub.endDate is null or sub.hasPerpetualAccess = true))"
                    queryParams << [validOn: DateUtils.parseDateGeneric(params.subValidOn)]
                }
            }
            query << subQuery+")"
        }

        if(params.sub && (params.hasSubscription &&  !params.hasNotSubscription) || (!params.hasSubscription && params.hasNotSubscription)) {
            String subQuery = ""

            if(params.hasNotSubscription) {
                subQuery = " not"
            }

            subQuery = subQuery + " exists (select oo.id from OrgRole oo join oo.sub sub join sub.orgRelations ooCons where oo.org.id = o.id and oo.roleType in (:subscrRoles) and ooCons.org = :context and ooCons.roleType = :consType and sub.instanceOf = :sub)"
            queryParams << [subscrRoles: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN], consType: RDStore.OR_SUBSCRIPTION_CONSORTIA, context: contextService.getOrg(), sub: params.sub]
            query << subQuery
        }

        if (params.filterPvd && params.filterPvd != "" && params.list('filterPvd')) {
            String subQuery = " exists (select oo.id from OrgRole oo join oo.sub sub join sub.orgRelations ooCons where oo.org.id = o.id and oo.roleType in (:subscrRoles) and ooCons.org = :context and ooCons.roleType = :consType and exists (select orgRole from OrgRole orgRole where orgRole.sub = sub and orgRole.org.id in (:filterPvd)) "
            queryParams << [subscrRoles: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN], consType: RDStore.OR_SUBSCRIPTION_CONSORTIA, context: contextService.getOrg(), filterPvd : params.list('filterPvd').collect { Long.parseLong(it) }]

            if (params.subStatus) {
                subQuery +=  " and (sub.status = :subStatus" // ( closed in line 213; needed to prevent consortia members without any subscriptions because or would lift up the other restrictions)
                RefdataValue subStatus = RefdataValue.get(params.subStatus)
                queryParams << [subStatus: subStatus]
                if (!params.subValidOn && params.subPerpetual && subStatus == RDStore.SUBSCRIPTION_CURRENT)
                    subQuery += " or sub.hasPerpetualAccess = true"
                subQuery += ")"
            }
            if (params.subValidOn || params.subPerpetual) {
                if (params.subValidOn && !params.subPerpetual) {
                    subQuery += " and (sub.startDate <= :validOn or sub.startDate is null) and (sub.endDate >= :validOn or sub.endDate is null)"
                    queryParams << [validOn: DateUtils.parseDateGeneric(params.subValidOn)]
                }
                else if (params.subValidOn && params.subPerpetual) {
                    subQuery += " and ((sub.startDate <= :validOn or sub.startDate is null) and (sub.endDate >= :validOn or sub.endDate is null or sub.hasPerpetualAccess = true))"
                    queryParams << [validOn: DateUtils.parseDateGeneric(params.subValidOn)]
                }
            }

            query << subQuery+")"
            params.filterSet = true
        }

        if (params.customerType?.length() > 0) {
            query << "exists (select oss from OrgSetting as oss where oss.id = o.id and oss.key = :customerTypeKey and oss.roleValue.id = :customerType)"
            queryParams << [customerType : Long.parseLong(params.customerType)]
            queryParams << [customerTypeKey : OrgSetting.KEYS.CUSTOMER_TYPE]
        }

        if (params.orgIdentifier?.length() > 0) {
            query << " exists (select io from Identifier io join io.org ioorg " +
                    " where ioorg = o and genfunc_filter_matcher(io.value, :orgIdentifier) = true) "
            queryParams << [orgIdentifier: params.orgIdentifier]
        }
        if (params.identifierNamespace?.size() > 0) {
            query << " ( exists ( select ident from Identifier ident join ident.org ioorg where ioorg = o and ident.ns in (:namespaces) and ident.value != null and ident.value != '' and ident.value != '${IdentifierNamespace.UNKNOWN}' ) )"
            queryParams << [namespaces: []]
            params.list('identifierNamespace').each { String idnsKey ->
                queryParams.namespaces << IdentifierNamespace.get(idnsKey)
            }
        }
        if (params.customerIDNamespace?.size() > 0) {
            List<String> customerIDClause = []
            List<String> fields = params.list('customerIDNamespace')
            fields.each { String field ->
                customerIDClause << "id.${field} != null"
            }
            query << " ( exists ( select customerID from CustomerIdentifier customerID where customerID.customer = o and ( ${customerIDClause.join(' or ')} ) ) ) "
        }

        if (params.filterPropDef?.size() > 0) {
            def psq = propertyService.evalFilterQuery(params, '', 'o', queryParams)
            query << psq.query.split(' and', 2)[1]
            queryParams << psq.queryParams
        }

         queryParams << [org : org]
         queryParams << [comboType : params.comboType]

        String defaultOrder = " order by " + (params.sort && (!params.consSort && !params.ownSort && !params.subscrSort) ? params.sort : " LOWER(o.sortname)") + " " + (params.order && (!params.consSort && !params.ownSort && !params.subscrSort) ? params.order : "asc")

        String direction = "c.fromOrg = o and c.toOrg = :org"
        if(params.invertDirection)
            direction = "c.fromOrg = :org and c.toOrg = o"
        if (query.size() > 0) {
            result.query = "select o from Org as o, Combo as c where " + query.join(" and ") + " and "+direction+" and c.type.value = :comboType " + defaultOrder
        } else {
            result.query = "select o from Org as o, Combo as c where "+direction+" and c.type.value = :comboType " + defaultOrder
        }
        result.queryParams = queryParams
        result
    }

    /**
     * Processes the task filter parameters and gets the task query with the prepared filter values
     * @param params the filter query parameter map
     * @param sdFormat the date format to use to parse dates
     * @return the map containing the query and the prepared query parameters
     */
    Map<String, Object> getTaskQuery(Map params, DateFormat sdFormat) {
        Map<String, Object> result = [:]
        def query = []
        Map<String, Object> queryParams = [:]

        // ERMS-1592, ERMS-1596
        if (params.taskName) {
            query << "genfunc_filter_matcher(t.title, :taskName) = true "
            queryParams << [taskName : "${params.taskName}"]
        }
        if (params.taskStatus) {
            if (params.taskStatus == 'not done') {
                query << "t.status.id != :rdvDone"
                queryParams << [rdvDone : RDStore.TASK_STATUS_DONE?.id]
            }
            else {
                query << "t.status.id = :statusId"
                queryParams << [statusId : Long.parseLong(params.taskStatus)]
            }
        }
        if (params.endDateFrom && sdFormat) {
            query << "t.endDate >= :endDateFrom"
            queryParams << [endDateFrom : sdFormat.parse(params.endDateFrom)]
        }
        if (params.endDateTo && sdFormat) {
            query << "t.endDate <= :endDateTo"
            queryParams << [endDateTo : sdFormat.parse(params.endDateTo)]
        }

        String defaultOrder = " order by " + (params.sort ?: "t.endDate") + " " + (params.order ?: "desc")

        if (query.size() > 0) {
            query = " and " + query.join(" and ") + defaultOrder
        } else {
            query = defaultOrder
        }
        result.query = query
        result.queryParams = queryParams

        result
    }

    /**
     * Kept as reference.
     * Processes the given filter parameters and generates a query to retrieve documents
     * @deprecated should be moved to the external document service
     * @param params the filter parameter map
     * @return the map containing the query and the prepared query parameters
     */
    @Deprecated
    Map<String,Object> getDocumentQuery(Map params) {
        Map result = [:]
        List query = []
        Map<String,Object> queryParams = [:]
        if(params.docTitle) {
            query << "lower(d.title) like lower(:title)"
            queryParams << [title:"%${params.docTitle}%"]
        }
        if(params.docFilename) {
            query << "lower(d.filename) like lower(:filename)"
            queryParams << [filename:"%${params.docFilename}%"]
        }
        if(params.docType) {
            query << "d.type = :type"
            queryParams << [type: RefdataValue.get(params.docType)]
        }
        if(params.docTarget) {
            List targetOIDs = params.docTarget.split(",")
            Set targetQuery = []
            List subs = [], orgs = [], licenses = [], pkgs = []
            targetOIDs.each { t ->
                //sorry, I hate def, but here, I cannot avoid using it ...
                def target = genericOIDService.resolveOID(t)
                switch(target.class.name) {
                    case Subscription.class.name: targetQuery << "dc.subscription in (:subs)"
                        subs << target
                        break
                    case Org.class.name: targetQuery << "dc.org in (:orgs)"
                        orgs << target
                        break
                    case License.class.name: targetQuery << "dc.license in (:licenses)"
                        licenses << target
                        break
                    case Package.class.name: targetQuery << "dc.pkg in (:pkgs)"
                        pkgs << target
                        break
                    default: log.debug(target.class.name)
                        break
                }
            }
            if(subs)
                queryParams.subs = subs
            if(orgs)
                queryParams.orgs = orgs
            if(licenses)
                queryParams.licenses = licenses
            if(pkgs)
                queryParams.pkgs = pkgs
            if(targetQuery)
                query << "(${targetQuery.join(" or ")})"
        }
        if(params.noTarget) {
            query << "dc.org = :context"
            queryParams << [context:contextService.getOrg()]
        }
        if(query.size() > 0)
            result.query = " and "+query.join(" and ")
        else result.query = ""
        result.queryParams = queryParams
        result
    }

    /**
     * Processes the given filter parameters and generates a survey query
     * @param params the filter parameter map
     * @param sdFormat the format to use for date parsing
     * @param contextOrg the context institution whose perspective should be taken
     * @return the map containing the query and the prepared query parameters
     */
    Map<String,Object> getSurveyConfigQueryConsortia(GrailsParameterMap params, DateFormat sdFormat, Org contextOrg) {
        Map result = [:]
        Map<String,Object> queryParams = [:]
        String query

        query = "from SurveyInfo surInfo left join surInfo.surveyConfigs surConfig where surInfo.owner = :contextOrg "
        queryParams << [contextOrg : contextOrg]

        def date_restriction
        if ((params.validOn == null || params.validOn.trim() == '') && sdFormat) {
            date_restriction = null
        } else {
            date_restriction = sdFormat.parse(params.validOn)
        }

        if (date_restriction) {
            query += " and surInfo.startDate <= :date_restr and (surInfo.endDate >= :date_restr or surInfo.endDate is null)"
            queryParams.put('date_restr', date_restriction)
            params.filterSet = true
        }

        if (params.validOnYear) {
            if (params.validOnYear && params.validOnYear != "" && params.list('validOnYear')) {
                if('all' in params.list('validOnYear')) {
                    params.filterSet = true
                    params.validOnYear = ['all']
                }else{
                    query += " and Year(surInfo.startDate) in (:validOnYear) "
                    queryParams << [validOnYear : params.list('validOnYear').collect { Integer.parseInt(it) }]
                    params.filterSet = true
                }
            }
        }

        if(params.name) {
            query += " and (genfunc_filter_matcher(surInfo.name, :name) = true or exists ( select surC from SurveyConfig as surC where surC.surveyInfo = surC and (genfunc_filter_matcher(surC.subscription.name, :name) = true))) "
            queryParams << [name:"${params.name}"]
            params.filterSet = true
        }

        if(params.status) {
            query += " and surInfo.status = :status"
            queryParams << [status: RefdataValue.get(params.status)]
            params.filterSet = true
        }
        if(params.type) {
            query += " and surInfo.type = :type"
            queryParams << [type: RefdataValue.get(params.type)]
            params.filterSet = true
        }
        if (params.startDate && sdFormat) {
            query += " and surInfo.startDate >= :startDate"
            queryParams << [startDate : sdFormat.parse(params.startDate)]
            params.filterSet = true
        }
        if (params.endDate && sdFormat) {
            query += " and surInfo.endDate <= :endDate"
            queryParams << [endDate : sdFormat.parse(params.endDate)]
            params.filterSet = true
        }

        if (params.mandatory || params.noMandatory) {

            if (params.mandatory && !params.noMandatory) {
                query += " and surInfo.isMandatory = :mandatory"
                queryParams << [mandatory: true]
            }else if (!params.mandatory && params.noMandatory){
                query += " and surInfo.isMandatory = :mandatory"
                queryParams << [mandatory: false]
            }
            params.filterSet = true
        }

        if(params.ids) {
            query += " and surInfo.id in (:ids)"
            queryParams << [ids: params.list('ids').collect{Long.parseLong(it)}]
            params.filterSet = true
        }

        if(params.checkSubSurveyUseForTransfer) {
            query += " and surConfig.subSurveyUseForTransfer = :checkSubSurveyUseForTransfer"
            queryParams << [checkSubSurveyUseForTransfer: true]
            params.filterSet = true
        }

        if (params.provider) {
            query += " and exists (select orgRole from OrgRole orgRole where orgRole.sub = surConfig.subscription and orgRole.org = :provider)"
            queryParams << [provider : Org.get(params.provider)]
            params.filterSet = true
        }

        if (params.list('filterSub')) {
            query += " and surConfig.subscription.name in (:subs) "
            queryParams << [subs : params.list('filterSub')]
            params.filterSet = true
        }

        if (params.filterStatus && params.filterStatus != "" && params.list('filterStatus')) {
            query += " and surInfo.status.id in (:filterStatus) "
            queryParams << [filterStatus : params.list('filterStatus').collect { Long.parseLong(it) }]
            params.filterSet = true
        }

        if (params.filterPvd && params.filterPvd != "" && params.list('filterPvd')) {
            query += " and exists (select orgRole from OrgRole orgRole where orgRole.sub = surConfig.subscription and orgRole.org.id in (:filterPvd))"
            queryParams << [filterPvd : params.list('filterPvd').collect { Long.parseLong(it) }]
            params.filterSet = true
        }

        if (params.participant) {
            query += " and exists (select surResult from SurveyResult as surResult where surResult.surveyConfig = surConfig and participant = :participant)"
            queryParams << [participant : params.participant]
        }

        if (params.filterPropDef?.size() > 0) {
            def psq = propertyService.evalFilterQuery(params, query, 'surConfig', queryParams)
            query = psq.query
            queryParams = psq.queryParams
            params.filterSet = true
        }

        if(params.tab == "created"){
            query += " and (surInfo.status in (:status))"
            queryParams << [status: [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY]]
        }

        if(params.tab == "active"){
            query += " and surInfo.status = :status"
            queryParams << [status: RDStore.SURVEY_SURVEY_STARTED]
        }

        if(params.tab == "finish"){
            query += " and surInfo.status = :status"
            queryParams << [status: RDStore.SURVEY_SURVEY_COMPLETED]
        }

        if(params.tab == "inEvaluation"){
            query += " and surInfo.status = :status"
            queryParams << [status: RDStore.SURVEY_IN_EVALUATION]
        }

        if(params.tab == "completed"){
            query += " and surInfo.status = :status"
            queryParams << [status: RDStore.SURVEY_COMPLETED]
        }

        if ((params.sort != null) && (params.sort.length() > 0)) {
            query += " order by ${params.sort} ${params.order ?: "asc"}"
        } else {
            query += " order by surInfo.endDate ASC, LOWER(surInfo.name) "
        }

        result.query = query
        result.queryParams = queryParams
        result
    }

    /**
     * @deprecated replaced by {@link #getParticipantSurveyQuery_New(grails.web.servlet.mvc.GrailsParameterMap, java.text.DateFormat, de.laser.Org)}
     */
    @Deprecated
    Map<String,Object> getParticipantSurveyQuery(Map params, DateFormat sdFormat, Org org) {
        Map result = [:]
        List query = []
        Map<String,Object> queryParams = [:]
        if(params.name) {
            query << " (genfunc_filter_matcher(surInfo.name, :name) = true or exists ( select surC from SurveyConfig as surC where surC.surveyInfo = surC and (genfunc_filter_matcher(surC.subscription.name, :name) = true))) "
            queryParams << [name:"${params.name}"]
        }
        if(params.status) {
            query << "surResult.surveyConfig.surveyInfo.status = :status"
            queryParams << [status: RefdataValue.get(params.status)]
        }
        if(params.type) {
            query << "surResult.surveyConfig.surveyInfo.type = :type"
            queryParams << [type: RefdataValue.get(params.type)]
        }

        if(params.owner) {
            query << "surInfo.owner = :owner"
            queryParams << [owner: params.owner instanceof Org ?: Org.get(params.owner) ]
        }

        if (params.currentDate) {

            params.currentDate = (params.currentDate instanceof Date) ? params.currentDate : sdFormat.parse(params.currentDate)

            query << "surResult.surveyConfig.surveyInfo.startDate <= :startDate and (surResult.surveyConfig.surveyInfo.endDate >= :endDate or surResult.surveyConfig.surveyInfo.endDate is null)"

            queryParams << [startDate : params.currentDate]
            queryParams << [endDate : params.currentDate]

            query << "surResult.surveyConfig.surveyInfo.status = :status"
            queryParams << [status: RDStore.SURVEY_SURVEY_STARTED]

        }

        if (params.startDate && sdFormat && !params.currentDate) {

            params.startDate = (params.startDate instanceof Date) ? params.startDate : sdFormat.parse(params.startDate)

            query << "surveyconfig.surveyInfo.startDate >= :startDate"
            queryParams << [startDate : params.startDate]
        }
        if (params.endDate && sdFormat && !params.currentDate) {

            params.endDate = params.endDate instanceof Date ? params.endDate : sdFormat.parse(params.endDate)

            query << "(surResult.surveyConfig.surveyInfo.endDate <= :endDate or surResult.surveyConfig.surveyInfo.endDate is null)"
            queryParams << [endDate : params.endDate]
        }



        if(params.tab == "new"){
            query << "(surResult.surveyConfig.surveyInfo.status = :status and surResult.id in (select sr.id from SurveyResult sr where sr.surveyConfig  = surveyConfig and sr.dateCreated = sr.lastUpdated and surOrg.finishDate is null))"
            queryParams << [status: RDStore.SURVEY_SURVEY_STARTED]
        }

        if(params.tab == "processed"){
            query << "(surResult.surveyConfig.surveyInfo.status = :status and surResult.id in (select sr.id from SurveyResult sr where sr.surveyConfig  = surveyConfig and sr.dateCreated < sr.lastUpdated and surOrg.finishDate is null))"
            queryParams << [status: RDStore.SURVEY_SURVEY_STARTED]
        }

        if(params.tab == "finish"){
            query << "(surOrg.finishDate is not null)"
        }

        if(params.tab == "notFinish"){
            query << "((surInfo.status in (:status)) and surOrg.finishDate is null)"
            queryParams << [status: [RDStore.SURVEY_SURVEY_COMPLETED, RDStore.SURVEY_IN_EVALUATION, RDStore.SURVEY_COMPLETED]]
        }

        if(params.consortiaOrg) {
            query << "surResult.owner = :owner"
            queryParams << [owner: params.consortiaOrg]
        }


        String defaultOrder = " order by " + (params.sort ?: " LOWER(surResult.surveyConfig.surveyInfo.name)") + " " + (params.order ?: "asc")

        if (query.size() > 0) {
            result.query = "from SurveyInfo surInfo left join surInfo.surveyConfigs surConfig left join surConfig.orgs surOrg left join surConfig.propertySet surResult  where (surOrg.org = :org)  and " + query.join(" and ") + defaultOrder
        } else {
            result.query = "from SurveyInfo surInfo left join surInfo.surveyConfigs surConfig left join surConfig.orgs surOrg left join surConfig.propertySet surResult where (surOrg.org = :org) " + defaultOrder
        }
        queryParams << [org : org]


        result.queryParams = queryParams
        result
    }

    /**
     * Processes the given filter parameters and generates a query to retrieve surveys from the participant's point of view
     * @param params the filter parameter map
     * @param sdFormat the format to use to parse dates
     * @param org the context institution
     * @return the map containing the query and the prepared query parameters
     */
    Map<String,Object> getParticipantSurveyQuery_New(GrailsParameterMap params, DateFormat sdFormat, Org org) {
        Map result = [:]
        List query = []
        Map<String,Object> queryParams = [:]

        def date_restriction
        if ((params.validOn == null || params.validOn.trim() == '') && sdFormat) {
            date_restriction = null
        } else {
            date_restriction = sdFormat.parse(params.validOn)
        }

        if (date_restriction) {
            query += " surInfo.startDate <= :date_restr and (surInfo.endDate >= :date_restr or surInfo.endDate is null)"
            queryParams.put('date_restr', date_restriction)
            params.filterSet = true
        }

        if (params.validOnYear) {
            if (params.validOnYear && params.validOnYear != "" && params.list('validOnYear')) {
                if('all' in params.list('validOnYear')) {
                    params.filterSet = true
                    params.validOnYear = ['all']
                }else{
                    query += " Year(surInfo.startDate) in (:validOnYear) "
                    queryParams << [validOnYear : params.list('validOnYear').collect { Integer.parseInt(it) }]
                    params.filterSet = true
                }
            }
        }

        if(params.name) {
            query << " (genfunc_filter_matcher(surInfo.name, :name) = true or exists ( select surC from SurveyConfig as surC where surC.surveyInfo = surC and (genfunc_filter_matcher(surC.subscription.name, :name) = true))) "
            queryParams << [name:"${params.name}"]
        }
        if(params.status) {
            query << "surInfo.status = :status"
            queryParams << [status: RefdataValue.get(params.status)]
        }
        if(params.type) {
            query << "surInfo.type = :type"
            queryParams << [type: RefdataValue.get(params.type)]
        }

        if(params.owner) {
            query << "surInfo.owner = :owner"
            queryParams << [owner: params.owner instanceof Org ? params.owner : Org.get(params.owner) ]
        }

        if (params.mandatory || params.noMandatory) {

            if (params.mandatory && !params.noMandatory) {
                query << "surInfo.isMandatory = :mandatory"
                queryParams << [mandatory: true]
            }else if (!params.mandatory && params.noMandatory){
                query << "surInfo.isMandatory = :mandatory"
                queryParams << [mandatory: false]
            }
            params.filterSet = true
        }

        if(params.checkSubSurveyUseForTransfer) {
            query << "surConfig.subSurveyUseForTransfer = :checkSubSurveyUseForTransfer"
            queryParams << [checkSubSurveyUseForTransfer: true]
            params.filterSet = true
        }

        if (params.list('filterSub')) {
            query << " surConfig.subscription.name in (:subs) "
            queryParams << [subs : params.list('filterSub')]
            params.filterSet = true
        }

        if (params.filterPvd && params.filterPvd != "" && params.list('filterPvd')) {
            query << "exists (select orgRole from OrgRole orgRole where orgRole.sub = surConfig.subscription and orgRole.org.id in (:filterPvd))"
            queryParams << [filterPvd : params.list('filterPvd').collect { Long.parseLong(it) }]
            params.filterSet = true
        }

        if (params.currentDate) {

            params.currentDate = (params.currentDate instanceof Date) ? params.currentDate : sdFormat.parse(params.currentDate)

            query << "surInfo.startDate <= :startDate and (surInfo.endDate >= :endDate or surInfo.endDate is null)"

            queryParams << [startDate : params.currentDate]
            queryParams << [endDate : params.currentDate]

            query << "surInfo.status = :status"
            queryParams << [status: RDStore.SURVEY_SURVEY_STARTED]

        }

        if (params.startDate && sdFormat && !params.currentDate) {

            params.startDate = (params.startDate instanceof Date) ? params.startDate : sdFormat.parse(params.startDate)

            query << "surInfo.startDate >= :startDate"
            queryParams << [startDate : params.startDate]
        }
        if (params.endDate && sdFormat && !params.currentDate) {

            params.endDate = params.endDate instanceof Date ? params.endDate : sdFormat.parse(params.endDate)

            query << "(surInfo.endDate <= :endDate or surInfo.endDate is null)"
            queryParams << [endDate : params.endDate]
        }

        if(params.tab == "new"){
            query << "((surOrg.org = :org and surOrg.finishDate is null and surConfig.pickAndChoose = true and surConfig.surveyInfo.status = :status and" +
                    " exists (select surResult from SurveyResult surResult where surResult.surveyConfig = surConfig and surConfig.surveyInfo.status = :status and surResult.dateCreated = surResult.lastUpdated and surOrg.finishDate is null and surConfig.pickAndChoose = true and surResult.participant = :org)) " +
                    "or (surOrg.org = :org and exists (select surResult from SurveyResult surResult where surResult.surveyConfig = surConfig and surConfig.surveyInfo.status = :status and surResult.dateCreated = surResult.lastUpdated and surOrg.finishDate is null and surConfig.pickAndChoose = false and surResult.participant = :org)))"
            queryParams << [status: RDStore.SURVEY_SURVEY_STARTED]
            queryParams << [org : org]
        }

        if(params.tab == "processed"){
            query << "(surInfo.status = :status and surOrg.org = :org and exists (select surResult from SurveyResult surResult where surResult.surveyConfig = surConfig and surResult.dateCreated != surResult.lastUpdated and surOrg.finishDate is null and surResult.participant = :org))"
            queryParams << [status: RDStore.SURVEY_SURVEY_STARTED]
            queryParams << [org : org]
        }

        if(params.tab == "finish"){
            query << "surOrg.org = :org and surOrg.finishDate is not null"
            queryParams << [org : org]
        }

        if(params.tab == "notFinish"){
            query << "surConfig.subSurveyUseForTransfer = false and (surInfo.status in (:status) and surOrg.finishDate is null and surOrg.org = :org)"
            queryParams << [status: [RDStore.SURVEY_SURVEY_COMPLETED, RDStore.SURVEY_IN_EVALUATION, RDStore.SURVEY_COMPLETED]]
            queryParams << [org : org]
        }

        if(params.tab == "termination"){
            query << "surConfig.subSurveyUseForTransfer = true and (surInfo.status in (:status) and surOrg.finishDate is null and surOrg.org = :org)"
            queryParams << [status: [RDStore.SURVEY_SURVEY_COMPLETED, RDStore.SURVEY_IN_EVALUATION, RDStore.SURVEY_COMPLETED]]
            queryParams << [org : org]
        }

        if(params.consortiaOrg) {
            query << "surInfo.owner = :owner"
            queryParams << [owner: params.consortiaOrg]
        }


        String defaultOrder = " order by " + (params.sort ?: " surInfo.endDate ASC, LOWER(surInfo.name) ") + " " + (params.order ?: "asc")

        if (query.size() > 0) {
            result.query = "from SurveyInfo surInfo left join surInfo.surveyConfigs surConfig left join surConfig.orgs surOrg where " + query.join(" and ") + defaultOrder
        } else {
            result.query = "from SurveyInfo surInfo left join surInfo.surveyConfigs surConfig left join surConfig.orgs surOrg " + defaultOrder
        }

        if (params.filterPropDef?.size() > 0) {
            def psq = propertyService.evalFilterQuery(params, result.query, 'surConfig', queryParams)
            result.query = psq.query
            queryParams = psq.queryParams
            params.filterSet = true
        }

        result.queryParams = queryParams
        result
    }

    /**
     * Processes the given filter parameters and generates a query to retrieve the given survey's participants
     * @param params the filter parameter map
     * @param surveyConfig the survey whose participants should be retrieved
     * @return the map containing the query and the prepared query parameters
     */
    Map<String,Object> getSurveyOrgQuery(GrailsParameterMap params, SurveyConfig surveyConfig) {
        Map result = [:]
        String base_qry = "from SurveyOrg as surveyOrg where surveyOrg.surveyConfig = :surveyConfig "
        Map<String,Object> queryParams = [surveyConfig: surveyConfig]

        if (params.orgNameContains?.length() > 0) {
            base_qry += " and (genfunc_filter_matcher(surveyOrg.org.name, :orgNameContains1) = true or genfunc_filter_matcher(surveyOrg.org.sortname, :orgNameContains2) = true) "
            queryParams << [orgNameContains1 : "${params.orgNameContains}"]
            queryParams << [orgNameContains2 : "${params.orgNameContains}"]
        }
        if (params.orgType?.length() > 0) {
            base_qry += " and exists (select roletype from surveyOrg.org.orgType as roletype where roletype.id = :orgType )"
            queryParams << [orgType : Long.parseLong(params.orgType)]
        }
        if (params.orgSector?.length() > 0) {
            base_qry += " and surveyOrg.org.sector.id = :orgSector"
            queryParams << [orgSector : Long.parseLong(params.orgSector)]
        }
        if (params.region?.size() > 0) {
            base_qry += " and surveyOrg.org.region.id in (:region)"
            List<String> selRegions = params.list("region")
            List<Long> regions = []
            selRegions.each { String sel ->
                regions << Long.parseLong(sel)
            }
            queryParams << [region : regions]
        }
        if (params.country?.size() > 0) {
            base_qry += " and surveyOrg.org.country.id in (:country)"
            List<String> selCountries = params.list("country")
            List<Long> countries = []
            selCountries.each { String sel ->
                countries << Long.parseLong(sel)
            }
            queryParams << [country : countries]
        }
        if (params.subjectGroup?.size() > 0) {
            base_qry +=  " and exists (select osg from OrgSubjectGroup as osg where osg.org.id = surveyOrg.org.id and osg.subjectGroup.id in (:subjectGroup))"
            queryParams << [subjectGroup : params.list("subjectGroup").collect {Long.parseLong(it)}]
        }

        if (params.libraryNetwork?.size() > 0) {
            base_qry += " and surveyOrg.org.libraryNetwork.id in (:libraryNetwork)"
            List<String> selLibraryNetworks = params.list("libraryNetwork")
            List<Long> libraryNetworks = []
            selLibraryNetworks.each { String sel ->
                libraryNetworks << Long.parseLong(sel)
            }
            queryParams << [libraryNetwork : libraryNetworks]
        }

        if (params.libraryType?.size() > 0) {
            base_qry += " and surveyOrg.org.libraryType.id in (:libraryType)"
            List<String> selLibraryTypes = params.list("libraryType")
            List<Long> libraryTypes = []
            selLibraryTypes.each { String sel ->
                libraryTypes << Long.parseLong(sel)
            }
            queryParams << [libraryType : libraryTypes]
        }

        if (params.customerType?.length() > 0) {
            base_qry += " and exists (select oss from OrgSetting as oss where oss.id = surveyOrg.org.id and oss.key = :customerTypeKey and oss.roleValue.id = :customerType)"
            queryParams << [customerType : Long.parseLong(params.customerType)]
            queryParams << [customerTypeKey : OrgSetting.KEYS.CUSTOMER_TYPE]
        }

        if (params.orgIdentifier?.length() > 0) {
            base_qry += " and exists (select ident from Identifier io join io.org ioorg " +
                    " where ioorg = surveyOrg.org and LOWER(ident.value) like LOWER(:orgIdentifier)) "
            queryParams << [orgIdentifier: "%${params.orgIdentifier}%"]
        }

        if (params.participant) {
            base_qry += " and surveyOrg.org = :participant)"
            queryParams << [participant : params.participant]
        }

        if(params.owner) {
            base_qry += " and surveyOrg.surveyConfig.surveyInfo.owner = :owner"
            queryParams << [owner: params.owner instanceof Org ?: Org.get(params.owner) ]
        }
        if(params.consortiaOrg) {
            base_qry += " and surveyOrg.surveyConfig.surveyInfo.owner = :owner"
            queryParams << [owner: params.consortiaOrg]
        }

        if(params.participantsNotFinish) {
            base_qry += " and surveyOrg.finishDate is null"
        }

        if(params.participantsFinish) {
            base_qry += " and surveyOrg.finishDate is not null"
        }

        if (params.filterPropDef) {
                PropertyDefinition pd = (PropertyDefinition) genericOIDService.resolveOID(params.filterPropDef)
                base_qry += ' and exists (select surResult from SurveyResult as surResult where surResult.surveyConfig = surveyOrg.surveyConfig and participant = surveyOrg.org and surResult.type = :propDef '
                queryParams.put('propDef', pd)
                if (params.filterProp) {
                    if (pd.isRefdataValueType()) {
                            List<String> selFilterProps = params.filterProp.split(',')
                            List filterProp = []
                            selFilterProps.each { String sel ->
                                filterProp << genericOIDService.resolveOID(sel)
                            }
                            base_qry += " and "
                            if (filterProp.contains(RDStore.GENERIC_NULL_VALUE) && filterProp.size() == 1) {
                                base_qry += " surResult.refValue = null "
                                filterProp.remove(RDStore.GENERIC_NULL_VALUE)
                            } else if (filterProp.contains(RDStore.GENERIC_NULL_VALUE) && filterProp.size() > 1) {
                                base_qry += " ( surResult.refValue = null or surResult.refValue in (:prop) ) "
                                filterProp.remove(RDStore.GENERIC_NULL_VALUE)
                                queryParams.put('prop', filterProp)
                            } else {
                                base_qry += " surResult.refValue in (:prop) "
                                queryParams.put('prop', filterProp)
                            }
                    }
                    else if (pd.isIntegerType()) {
                            if (!params.filterProp || params.filterProp.length() < 1) {
                                base_qry += " and surResult.intValue = null "
                            } else {
                                base_qry += " and surResult.intValue = :prop "
                                queryParams.put('prop', AbstractPropertyWithCalculatedLastUpdated.parseValue(params.filterProp, pd.type))
                            }
                    }
                    else if (pd.isStringType()) {
                            if (!params.filterProp || params.filterProp.length() < 1) {
                                base_qry += " and surResult.stringValue = null "
                            } else {
                                base_qry += " and lower(surResult.stringValue) like lower(:prop) "
                                queryParams.put('prop', "%${AbstractPropertyWithCalculatedLastUpdated.parseValue(params.filterProp, pd.type)}%")
                            }
                    }
                    else if (pd.isBigDecimalType()) {
                            if (!params.filterProp || params.filterProp.length() < 1) {
                                base_qry += " and surResult.decValue = null "
                            } else {
                                base_qry += " and surResult.decValue = :prop "
                                queryParams.put('prop', AbstractPropertyWithCalculatedLastUpdated.parseValue(params.filterProp, pd.type))
                            }
                    }
                    else if (pd.isDateType()) {
                            if (!params.filterProp || params.filterProp.length() < 1) {
                                base_qry += " and surResult.dateValue = null "
                            } else {
                                base_qry += " and surResult.dateValue = :prop "
                                queryParams.put('prop', AbstractPropertyWithCalculatedLastUpdated.parseValue(params.filterProp, pd.type))
                            }
                    }
                    else if (pd.isURLType()) {
                            if (!params.filterProp || params.filterProp.length() < 1) {
                                base_qry += " and surResult.urlValue = null "
                            } else {
                                base_qry += " and genfunc_filter_matcher(surResult.urlValue, :prop) = true "
                                queryParams.put('prop', AbstractPropertyWithCalculatedLastUpdated.parseValue(params.filterProp, pd.type))
                            }
                    }
                }
                base_qry += ')'
        }

        if((params.hasSubscription &&  !params.hasNotSubscription) || (!params.hasSubscription && params.hasNotSubscription)) {
            String subQuery = " and "
            if(params.hasSubscription) {
                subQuery = subQuery
            }
            if(params.hasNotSubscription) {
                subQuery = subQuery + " not"
            }

            subQuery = subQuery + " exists (select oo.id from OrgRole oo join oo.sub sub join sub.orgRelations ooCons where oo.org.id = surveyOrg.org.id and oo.roleType in (:subscrRoles) and ooCons.org = :context and ooCons.roleType = :consType and sub.instanceOf = surveyOrg.surveyConfig.subscription)"
            queryParams << [subscrRoles: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN], consType: RDStore.OR_SUBSCRIPTION_CONSORTIA, context: contextService.getOrg()]
            base_qry = base_qry + subQuery
        }


        if ((params.sort != null) && (params.sort.length() > 0)) {
                base_qry += " order by ${params.sort} ${params.order ?: "asc"}"
        } else {
            base_qry += " order by surveyOrg.org.sortname "
        }

        result.query = base_qry
        result.queryParams = queryParams

        result

    }

    /**
     * Processes the given filter parameters and generates a base for the package title query
     * @param params the filter parameter map
     * @param qry_params the query filter map
     * @param showDeletedTipps should deleted titles be shown or not?
     * @param asAt the date when the package holding should be considered (because of access start and end dates)
     * @param forBase which instance is the base from which titles should be retrieved?
     * @return the map containing the query and the prepared query parameters
     */
    Map<String,Object> generateBasePackageQuery(params, qry_params, showDeletedTipps, asAt, forBase) {
        Locale locale = LocaleUtils.getCurrentLocale()
        String base_qry
        SimpleDateFormat sdf = new SimpleDateFormat(messageSource.getMessage('default.date.format.notime',null,locale))
        boolean filterSet = false
        if(forBase == 'Package')
            base_qry = "from TitleInstancePackagePlatform as tipp where tipp.pkg = :pkgInstance "
        else if(forBase == 'Platform')
            base_qry = "from TitleInstancePackagePlatform as tipp where tipp.platform = :platInstance "

        if (params.mode != 'advanced') {
            base_qry += "and tipp.status = :tippStatusCurrent "
            qry_params.tippStatusCurrent = RDStore.TIPP_STATUS_CURRENT
            filterSet = true
        }
        else if (params.mode == 'advanced' && showDeletedTipps != true) {
            base_qry += "and tipp.status != :tippStatusRemoved "
            qry_params.tippStatusRemoved = RDStore.TIPP_STATUS_REMOVED
        }

        if (asAt != null) {
            base_qry += " and ( ( :startDate >= tipp.accessStartDate or tipp.accessStartDate is null ) and ( :endDate <= tipp.accessEndDate or tipp.accessEndDate is null ) ) "
            qry_params.startDate = asAt
            qry_params.endDate = asAt
            filterSet = true
        }

        if (params.filter) {
            //causes GC overhead
            //base_qry += " and ( genfunc_filter_matcher(tipp.name,:title) = true or ( exists ( select id.id from Identifier id where genfunc_filter_matcher(id.value,:title) = true and id.tipp = tipp ) ) )"
            base_qry += " and genfunc_filter_matcher(tipp.name,:title) = true "
            qry_params.title = params.filter
            filterSet = true
        }

        if (params.coverageNoteFilter) {
            base_qry += "and genfunc_filter_matcher(tipp.coverageNote,:coverageNote) = true"
            qry_params.coverageNote = params.coverageNoteFilter
            filterSet = true
        }

        if (params.endsAfter) {
            base_qry += " and (select max(tc.endDate) from TIPPCoverage tc where tc.tipp = tipp) >= :endDate"
            qry_params.endDate = sdf.parse(params.endsAfter)
            filterSet = true
        }

        if (params.startsBefore) {
            base_qry += " and (select min(tc.startDate) from TIPPCoverage tc where tc.tipp = tipp) <= :startDate"
            qry_params.startDate = sdf.parse(params.startsBefore)
            filterSet = true
        }

        if (params.accessStartDate) {
            base_qry += " and tipp.accessStartDate <= :accessStartDate"
            qry_params.accessStartDate = sdf.parse(params.accessStartDate)
            filterSet = true
        }

        if (params.accessEndDate) {
            base_qry += " and tipp.accessEndDate >= :accessEndDate"
            qry_params.accessEndDate = sdf.parse(params.accessEndDate)
            filterSet = true
        }

        return [base_qry:base_qry,qry_params:qry_params,filterSet:filterSet]
    }

    /**
     * Substitution call for {@link #getIssueEntitlementQuery(grails.web.servlet.mvc.GrailsParameterMap, de.laser.Subscription)} for a single subscription
     * @param params the filter parameter map
     * @param subscription the subscriptions whose dates should be considered
     * @return the map containing the query and the prepared query parameters
     */
    Map<String, Object> getIssueEntitlementQuery(Map params, Subscription subscription) {
        getIssueEntitlementQuery(params, [subscription])
    }

    /**
     * Processes the given filter parameters and generates a query to retrieve issue entitlements
     * @param params the filter parameter map
     * @param subscriptions the subscriptions whose dates should be considered
     * @return the map containing the query and the prepared query parameters
     */
    Map<String,Object> getIssueEntitlementQuery(Map params, Collection<Subscription> subscriptions) {
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        Map result = [:]



        String base_qry
        Map<String,Object> qry_params = [subscriptions: subscriptions]
        boolean filterSet = false
        Date date_filter
        if (params.asAt && params.asAt.length() > 0) {
            date_filter = sdf.parse(params.asAt)
            result.as_at_date = date_filter
            result.editable = false
        }
        if (params.filter) {
            base_qry = " from IssueEntitlement as ie left join ie.coverages ic join ie.tipp tipp where ie.subscription in (:subscriptions) "
            if (date_filter) {
                // If we are not in advanced mode, hide IEs that are not current, otherwise filter
                // base_qry += "and ie.status <> ? and ( ? >= coalesce(ie.accessStartDate,subscription.startDate) ) and ( ( ? <= coalesce(ie.accessEndDate,subscription.endDate) ) OR ( ie.accessEndDate is null ) )  "
                // qry_params.add(deleted_ie);
                base_qry += "and ( ( :startDate >= coalesce(ie.accessStartDate,ie.subscription.startDate,ie.tipp.accessStartDate) or (ie.accessStartDate is null and ie.subscription.startDate is null and ie.tipp.accessStartDate is null) ) and ( :endDate <= coalesce(ie.accessEndDate,ie.subscription.endDate,ie.tipp.accessEndDate) or (ie.accessEndDate is null and ie.subscription.endDate is null and ie.tipp.accessEndDate is null) OR ( ie.subscription.hasPerpetualAccess = true ) ) ) "
                qry_params.startDate = date_filter
                qry_params.endDate = date_filter
            }
            base_qry += "and ( ( lower(ie.tipp.name) like :title ) or ( exists ( from Identifier ident where ident.tipp.id = ie.tipp.id and ident.value like :identifier ) ) or ((lower(ie.tipp.firstAuthor) like :ebookFirstAutorOrFirstEditor or lower(ie.tipp.firstEditor) like :ebookFirstAutorOrFirstEditor)) ) "
            qry_params.title = "%${params.filter.trim().toLowerCase()}%"
            qry_params.identifier = "%${params.filter}%"
            qry_params.ebookFirstAutorOrFirstEditor = "%${params.filter.trim().toLowerCase()}%"
            filterSet = true
        }
        else {
            base_qry = " from IssueEntitlement as ie left join ie.coverages ic join ie.tipp tipp where ie.subscription in (:subscriptions) "
            /*if (params.mode != 'advanced') {
                // If we are not in advanced mode, hide IEs that are not current, otherwise filter

                base_qry += " and ( :startDate >= coalesce(ie.accessStartDate,ie.subscription.startDate,ie.tipp.accessStartDate) or (ie.accessStartDate is null and ie.subscription.startDate is null and ie.tipp.accessStartDate is null) ) and ( ( :endDate <= coalesce(ie.accessEndDate,ie.subscription.endDate,ie.accessEndDate) or (ie.accessEndDate is null and ie.subscription.endDate is null and ie.tipp.accessEndDate is null)  or (ie.subscription.hasPerpetualAccess = true) ) ) "
                qry_params.startDate = date_filter
                qry_params.endDate = date_filter
            }*/
        }

        if (params.status == RDStore.TIPP_STATUS_REMOVED.id.toString()) {
            base_qry += " and ie.tipp.status.id = :status and ie.status.id != :status "
            qry_params.status = params.long('status')
        }
        else if(params.status != '' && params.status != null && listReaderWrapper(params, 'status')) {
            List<Long> status = []
            listReaderWrapper(params, 'status').each { String statusId ->
                status << Long.parseLong(statusId)
            }
            base_qry += " and ie.status.id in (:status) "
            qry_params.status = status
            filterSet = true
        }
        else if (params.notStatus != '' && params.notStatus != null){
            base_qry += " and ie.status.id != :notStatus "
            qry_params.notStatus = params.notStatus
        }
        else {
            base_qry += " and ie.status = :current "
            qry_params.current = RDStore.TIPP_STATUS_CURRENT
        }

        /*if(params.mode != 'advanced') {
            base_qry += " and ie.status = :current "
            qry_params.current = RDStore.TIPP_STATUS_CURRENT
        }
        else {
            base_qry += " and ie.status != :removed "
            qry_params.deleted = RDStore.TIPP_STATUS_REMOVED
        }*/

        if (params.pkgfilter && (params.pkgfilter != '')) {
            base_qry += " and tipp.pkg.id = :pkgId "
            qry_params.pkgId = Long.parseLong(params.pkgfilter)
            filterSet = true
        }
        if (params.titleGroup && (params.titleGroup != '') && !params.forCount) {
            base_qry += " and exists ( select iegi from IssueEntitlementGroupItem as iegi where iegi.ieGroup.id = :titleGroup and iegi.ie = ie) "
            qry_params.titleGroup = Long.parseLong(params.titleGroup)
        }

        if (params.inTitleGroups && (params.inTitleGroups != '') && !params.forCount) {
            if(params.inTitleGroups == RDStore.YN_YES.id.toString()) {
                base_qry += " and exists ( select iegi from IssueEntitlementGroupItem as iegi where iegi.ie = ie) "
            }else{
                base_qry += " and not exists ( select iegi from IssueEntitlementGroupItem as iegi where iegi.ie = ie) "
            }
        }

        if (params.ddcs && params.ddcs != "" && params.list('ddcs')) {
            base_qry += " and exists ( select ddc.id from DeweyDecimalClassification ddc where ddc.tipp = tipp and ddc.ddc.id in (:ddcs) ) "
            qry_params.ddcs = params.list('ddcs').collect { String key -> Long.parseLong(key) }
            filterSet = true
        }

        if (params.languages && params.languages != "" && params.list('languages')) {
            base_qry += " and exists ( select lang.id from Language lang where lang.tipp = tipp and lang.language.id in (:languages) ) "
            qry_params.languages = params.list('languages').collect { String key -> Long.parseLong(key) }
            filterSet = true
        }

        if (params.subject_references && params.subject_references != "" && params.list('subject_references')) {
            Set<String> subjectQuery = []
            params.list('subject_references').each { String subReference ->
                subjectQuery << "genfunc_filter_matcher(tipp.subjectReference, '${subReference.toLowerCase()}') = true"
            }
            base_qry += " and (${subjectQuery.join(" or ")}) "
            filterSet = true
        }

        if (params.series_names && params.series_names != "" && params.list('series_names')) {
            base_qry += " and lower(ie.tipp.seriesName) in (:series_names)"
            qry_params.series_names = params.list('series_names').collect { ""+it.toLowerCase()+"" }
            filterSet = true
        }

        if(params.summaryOfContent) {
            base_qry += " and lower(tipp.summaryOfContent) like :summaryOfContent "
            qry_params.summaryOfContent = "%${params.summaryOfContent.trim().toLowerCase()}%"
        }

        if(params.ebookFirstAutorOrFirstEditor) {
            base_qry += " and (lower(tipp.firstAuthor) like :ebookFirstAutorOrFirstEditor or lower(tipp.firstEditor) like :ebookFirstAutorOrFirstEditor) "
            qry_params.ebookFirstAutorOrFirstEditor = "%${params.ebookFirstAutorOrFirstEditor.trim().toLowerCase()}%"
        }

        if(params.dateFirstOnlineFrom) {
            base_qry += " and (tipp.dateFirstOnline is not null AND tipp.dateFirstOnline >= :dateFirstOnlineFrom) "
            qry_params.dateFirstOnlineFrom = sdf.parse(params.dateFirstOnlineFrom)

        }
        if(params.dateFirstOnlineTo) {
            base_qry += " and (tipp.dateFirstOnline is not null AND tipp.dateFirstOnline <= :dateFirstOnlineTo) "
            qry_params.dateFirstOnlineTo = sdf.parse(params.dateFirstOnlineTo)
        }

        if(params.yearsFirstOnline) {
            base_qry += " and (Year(tipp.dateFirstOnline) in (:yearsFirstOnline)) "
            qry_params.yearsFirstOnline = params.list('yearsFirstOnline').collect { Integer.parseInt(it) }
        }

        if (params.identifier) {
            base_qry += "and ( exists ( from Identifier ident where ident.tipp.id = tipp.id and ident.value like :identifier ) ) "
            qry_params.identifier = "${params.identifier}"
            filterSet = true
        }

        if (params.publishers) {
            //(exists (select orgRole from OrgRole orgRole where orgRole.tipp = ie.tipp and orgRole.roleType.id = ${RDStore.OR_PUBLISHER.id} and orgRole.org.name in (:publishers)) )
            base_qry += "and lower(tipp.publisherName) in (:publishers) "
            qry_params.publishers = params.list('publishers').collect { it.toLowerCase() }
            filterSet = true
        }


        if (params.title_types && params.title_types != "" && listReaderWrapper(params, 'title_types')) {
            base_qry += " and lower(tipp.titleType) in (:title_types)"
            qry_params.title_types = params.list('title_types').collect { ""+it.toLowerCase()+"" }
            filterSet = true
        }

        if (params.medium && params.medium != "" && listReaderWrapper(params, 'medium')) {
            base_qry += " and tipp.medium.id in (:medium) "
            qry_params.medium = listReaderWrapper(params, 'medium').collect { String key -> Long.parseLong(key) }
            filterSet = true
        }

        if (params.hasPerpetualAccess && !params.hasPerpetualAccessBySubs) {
            if(params.hasPerpetualAccess == RDStore.YN_YES.id.toString()) {
                base_qry += "and ie.perpetualAccessBySub is not null "
            }else{
                base_qry += "and ie.perpetualAccessBySub is null "
            }
            filterSet = true
        }

        if (params.hasPerpetualAccess && params.hasPerpetualAccessBySubs) {
            if(params.hasPerpetualAccess == RDStore.YN_NO.id.toString()) {
                base_qry += "and ie.tipp.hostPlatformURL not in (select ie2.tipp.hostPlatformURL from IssueEntitlement as ie2 where ie2.perpetualAccessBySub in (:subs)) "
                qry_params.subs = params.list('hasPerpetualAccessBySubs')
            }else {
                base_qry += "and ie.tipp.hostPlatformURL in (select ie2.tipp.hostPlatformURL from IssueEntitlement as ie2 where ie2.perpetualAccessBySub in (:subs)) "
                qry_params.subs = params.list('hasPerpetualAccessBySubs')
            }
            filterSet = true
        }

        if(!params.forCount && !params.bulkOperation)
            base_qry += " group by tipp, ic, ie.id "
        else if(!params.bulkOperation) base_qry += " group by tipp, ic "

        if (params.sort != null && params.sort.length() > 0 && params.sort != 'count') {
            if(params.sort == 'startDate')
                base_qry += "order by ic.startDate ${params.order}, lower(tipp.sortname) "
            else if(params.sort == 'endDate')
                base_qry += "order by ic.endDate ${params.order}, lower(tipp.sortname) "
            else {
                if(params.sort.contains("sortname"))
                    base_qry += "order by tipp.sortname ${params.order}, tipp.name ${params.order}"
                else base_qry += "order by ie.${params.sort} ${params.order} "
            }
        }
        else if(!params.forCount && !params.bulkOperation){
            base_qry += "order by tipp.sortname"
        }


        result.query = base_qry
        result.queryParams = qry_params
        result.filterSet = filterSet
        
        result

    }

    /**
     * Processes the given filter parameters and generates a query to retrieve permanent titles
     * @param params the filter parameter map
     * @param owner the org whose be the owner of permanent titles
     * @return the map containing the query and the prepared query parameters
     */
    Map<String,Object> getPermanentTitlesQuery(GrailsParameterMap params, Org owner) {
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        Map result = [:]



        String base_qry
        Map<String,Object> qry_params = [owner: owner]
        boolean filterSet = false
        Date date_filter
        if (params.asAt && params.asAt.length() > 0) {
            date_filter = sdf.parse(params.asAt)
            result.as_at_date = date_filter
            result.editable = false
        }
        if (params.filter) {
            base_qry = " from PermanentTitle as pt left join pt.issueEntitlement ie join pt.tipp tipp where pt.owner = :owner "
            if (date_filter) {
                // If we are not in advanced mode, hide IEs that are not current, otherwise filter
                // base_qry += "and ie.status <> ? and ( ? >= coalesce(ie.accessStartDate,subscription.startDate) ) and ( ( ? <= coalesce(ie.accessEndDate,subscription.endDate) ) OR ( ie.accessEndDate is null ) )  "
                // qry_params.add(deleted_ie);
                base_qry += "and ( ( :startDate >= coalesce(ie.accessStartDate,ie.subscription.startDate,ie.tipp.accessStartDate) or (ie.accessStartDate is null and ie.subscription.startDate is null and ie.tipp.accessStartDate is null) ) and ( :endDate <= coalesce(ie.accessEndDate,ie.subscription.endDate,ie.tipp.accessEndDate) or (ie.accessEndDate is null and ie.subscription.endDate is null and ie.tipp.accessEndDate is null) OR ( ie.subscription.hasPerpetualAccess = true ) ) ) "
                qry_params.startDate = date_filter
                qry_params.endDate = date_filter
            }
            base_qry += "and ( ( lower(ie.tipp.name) like :title ) or ( exists ( from Identifier ident where ident.tipp.id = ie.tipp.id and ident.value like :identifier ) ) or ((lower(ie.tipp.firstAuthor) like :ebookFirstAutorOrFirstEditor or lower(ie.tipp.firstEditor) like :ebookFirstAutorOrFirstEditor)) ) "
            qry_params.title = "%${params.filter.trim().toLowerCase()}%"
            qry_params.identifier = "%${params.filter}%"
            qry_params.ebookFirstAutorOrFirstEditor = "%${params.filter.trim().toLowerCase()}%"
            filterSet = true
        }
        else {
            base_qry = " from PermanentTitle as pt left join pt.issueEntitlement ie join pt.tipp tipp where pt.owner = :owner "
        }

        if (params.status == RDStore.TIPP_STATUS_REMOVED.id.toString()) {
            base_qry += " and ie.tipp.status.id = :status and ie.status.id != :status "
            qry_params.status = params.long('status')
        }
        else if(params.status != '' && params.status != null && params.list('status')) {
            List<Long> status = []
            params.list('status').each { String statusId ->
                status << Long.parseLong(statusId)
            }
            base_qry += " and ie.status.id in (:status) "
            qry_params.status = status
            filterSet = true
        }
        else if (params.notStatus != '' && params.notStatus != null){
            base_qry += " and ie.status.id != :notStatus "
            qry_params.notStatus = params.notStatus
        }
        else {
            base_qry += " and ie.status = :current "
            qry_params.current = RDStore.TIPP_STATUS_CURRENT
        }

        if (params.pkgfilter && (params.pkgfilter != '')) {
            base_qry += " and tipp.pkg.id = :pkgId "
            qry_params.pkgId = Long.parseLong(params.pkgfilter)
            filterSet = true
        }

        if (params.ddcs && params.ddcs != "" && params.list('ddcs')) {
            base_qry += " and exists ( select ddc.id from DeweyDecimalClassification ddc where ddc.tipp = tipp and ddc.ddc.id in (:ddcs) ) "
            qry_params.ddcs = params.list('ddcs').collect { String key -> Long.parseLong(key) }
            filterSet = true
        }

        if (params.languages && params.languages != "" && params.list('languages')) {
            base_qry += " and exists ( select lang.id from Language lang where lang.tipp = tipp and lang.language.id in (:languages) ) "
            qry_params.languages = params.list('languages').collect { String key -> Long.parseLong(key) }
            filterSet = true
        }

        if (params.subject_references && params.subject_references != "" && params.list('subject_references')) {
            Set<String> subjectQuery = []
            params.list('subject_references').each { String subReference ->
                subjectQuery << "genfunc_filter_matcher(tipp.subjectReference, '${subReference.toLowerCase()}') = true"
            }
            base_qry += " and (${subjectQuery.join(" or ")}) "
            filterSet = true
        }

        if (params.series_names && params.series_names != "" && params.list('series_names')) {
            base_qry += " and lower(ie.tipp.seriesName) in (:series_names)"
            qry_params.series_names = params.list('series_names').collect { ""+it.toLowerCase()+"" }
            filterSet = true
        }

        if(params.summaryOfContent) {
            base_qry += " and lower(tipp.summaryOfContent) like :summaryOfContent "
            qry_params.summaryOfContent = "%${params.summaryOfContent.trim().toLowerCase()}%"
        }

        if(params.ebookFirstAutorOrFirstEditor) {
            base_qry += " and (lower(tipp.firstAuthor) like :ebookFirstAutorOrFirstEditor or lower(tipp.firstEditor) like :ebookFirstAutorOrFirstEditor) "
            qry_params.ebookFirstAutorOrFirstEditor = "%${params.ebookFirstAutorOrFirstEditor.trim().toLowerCase()}%"
        }

        if(params.dateFirstOnlineFrom) {
            base_qry += " and (tipp.dateFirstOnline is not null AND tipp.dateFirstOnline >= :dateFirstOnlineFrom) "
            qry_params.dateFirstOnlineFrom = sdf.parse(params.dateFirstOnlineFrom)

        }
        if(params.dateFirstOnlineTo) {
            base_qry += " and (tipp.dateFirstOnline is not null AND tipp.dateFirstOnline <= :dateFirstOnlineTo) "
            qry_params.dateFirstOnlineTo = sdf.parse(params.dateFirstOnlineTo)
        }

        if(params.yearsFirstOnline) {
            base_qry += " and (Year(tipp.dateFirstOnline) in (:yearsFirstOnline)) "
            qry_params.yearsFirstOnline = params.list('yearsFirstOnline').collect { Integer.parseInt(it) }
        }

        if (params.identifier) {
            base_qry += "and ( exists ( from Identifier ident where ident.tipp.id = tipp.id and ident.value like :identifier ) ) "
            qry_params.identifier = "${params.identifier}"
            filterSet = true
        }

        if (params.publishers) {
            //(exists (select orgRole from OrgRole orgRole where orgRole.tipp = ie.tipp and orgRole.roleType.id = ${RDStore.OR_PUBLISHER.id} and orgRole.org.name in (:publishers)) )
            base_qry += "and lower(tipp.publisherName) in (:publishers) "
            qry_params.publishers = params.list('publishers').collect { it.toLowerCase() }
            filterSet = true
        }


        if (params.title_types && params.title_types != "" && params.list('title_types')) {
            base_qry += " and lower(tipp.titleType) in (:title_types)"
            qry_params.title_types = params.list('title_types').collect { ""+it.toLowerCase()+"" }
            filterSet = true
        }

        if (params.medium && params.medium != "" && listReaderWrapper(params, 'medium')) {
            base_qry += " and tipp.medium.id in (:medium) "
            qry_params.medium = listReaderWrapper(params, 'medium').collect { String key -> Long.parseLong(key) }
            filterSet = true
        }

        if ((params.sort != null) && (params.sort.length() > 0)) {
            base_qry += " order by ${params.sort} ${params.order} "
        }
        else {
            base_qry += " order by tipp.sortname"
        }

        result.query = base_qry
        result.queryParams = qry_params
        result.filterSet = filterSet

        result

    }

    /**
     * Processes the given filter parameters and generates a query to retrieve titles of the given packages
     * @param params the filter parameter map
     * @param pkgs the packages whose titles should be queried
     * @return the map containing the query and the prepared query parameters
     */
    Map<String,Object> getTippQuery(Map params, List<Package> pkgs) {
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        Map result = [:]

        String base_qry = "select tipp.id from TitleInstancePackagePlatform as tipp where "
        Map<String,Object> qry_params = [:]

        if(pkgs){
            qry_params.pkgs = pkgs
            base_qry += " tipp.pkg in (:pkgs) "
        }

        boolean filterSet = false
        Date date_filter
        if (params.asAt && params.asAt.length() > 0) {
            date_filter = sdf.parse(params.asAt)
            result.as_at_date = date_filter
            result.editable = false
        }


        if (params.filter) {
           /* if (date_filter) {
                base_qry += "and ( ( :startDate >= tipp.accessStartDate or tipp.accessStartDate is null ) and ( :endDate <= tipp.accessEndDate or tipp.accessEndDate is null) ) "
                qry_params.startDate = date_filter
                qry_params.endDate = date_filter
            }*/
            if(qry_params.size() > 0){
                base_qry += " and "
            }

            base_qry += " ( ( lower(tipp.name) like :title ) or ( exists ( from Identifier ident where ident.tipp.id = tipp.id and ident.value like :identifier ) ) or ((lower(tipp.firstAuthor) like :ebookFirstAutorOrFirstEditor or lower(tipp.firstEditor) like :ebookFirstAutorOrFirstEditor)) ) "
            qry_params.title = "%${params.filter.trim().toLowerCase()}%"
            qry_params.identifier = "%${params.filter}%"
            qry_params.ebookFirstAutorOrFirstEditor = "%${params.filter.trim().toLowerCase()}%"
            filterSet = true
        }

        if (date_filter) {
            if(qry_params.size() > 0){
                base_qry += " and "
            }
            base_qry += " ( ( :startDate >= tipp.accessStartDate or tipp.accessStartDate is null ) and ( :endDate <= tipp.accessEndDate or tipp.accessEndDate is null) ) "
            qry_params.startDate = new Timestamp(date_filter.getTime())
            qry_params.endDate = new Timestamp(date_filter.getTime())
        }

        if(params.addEntitlements && params.subscription && params.issueEntitlementStatus) {
            if(qry_params.size() > 0){
                base_qry += " and "
            }
            base_qry += " tipp.pkg in ( select pkg from SubscriptionPackage sp where sp.subscription = :subscription ) and " +
                    "( not exists ( select ie from IssueEntitlement ie where ie.subscription = :subscription and ie.tipp.id = tipp.id and ie.status = :issueEntitlementStatus ) )"
            qry_params.subscription = params.subscription
            qry_params.issueEntitlementStatus = params.issueEntitlementStatus
        }

       /* if(params.mode != 'advanced') {
            base_qry += " and tipp.status = :current "
            qry_params.current = RDStore.TIPP_STATUS_CURRENT
        }
        else {
            base_qry += " and tipp.status != :removed "
            qry_params.deleted = RDStore.TIPP_STATUS_REMOVED
        }*/

        if(params.status != '' && params.status != null && params.list('status')) {
            List<Long> status = []
            params.list('status').each { String statusId ->
                status << Long.parseLong(statusId)
            }
            if(qry_params.size() > 0){
                base_qry += " and "
            }

            base_qry += " tipp.status.id = :status "
            qry_params.status = status

        } else if (params.notStatus != '' && params.notStatus != null){
            if(qry_params.size() > 0){
                base_qry += " and "
            }
            base_qry += " tipp.status.id != :notStatus "
            qry_params.notStatus = params.notStatus
        }
        else {
            if(qry_params.size() > 0){
                base_qry += " and "
            }
            base_qry += " tipp.status = :current "
            qry_params.current = RDStore.TIPP_STATUS_CURRENT
        }

        /*if (params.planned) {
            base_qry += " and ( coalesce(tipp.accessStartDate, tipp.pkg.startDate) >= :date ) "
            qry_params.date = new Date()
        }
        if (params.expired) {
            base_qry += " and ( tipp.accessEndDate <= :date ) "
            qry_params.date = new Date()
        }*/

        if (params.ddcs && params.ddcs != "" && listReaderWrapper(params, 'ddcs')) {
            if(qry_params.size() > 0){
                base_qry += " and "
            }
            base_qry += " exists ( select ddc.id from DeweyDecimalClassification ddc where ddc.tipp = tipp and ddc.ddc.id in (:ddcs) ) "
            qry_params.ddcs = listReaderWrapper(params, 'ddcs').collect { String key -> Long.parseLong(key) }
            filterSet = true
        }

        if (params.languages && params.languages != "" && listReaderWrapper(params, 'languages')) {
            if(qry_params.size() > 0){
                base_qry += " and "
            }
            base_qry += " exists ( select lang.id from Language lang where lang.tipp = tipp and lang.language.id in (:languages) ) "
            qry_params.languages = listReaderWrapper(params, 'languages').collect { String key -> Long.parseLong(key) }
            filterSet = true
        }

        if (params.subject_references && params.subject_references != "" && listReaderWrapper(params, 'subject_references')) {
            if(qry_params.size() > 0){
                base_qry += " and "
            }
            base_qry += ' ( '
            listReaderWrapper(params, 'subject_references').eachWithIndex { String subRef, int i ->
                base_qry += " genfunc_filter_matcher(tipp.subjectReference,'"+subRef.trim().toLowerCase()+"') "
                if(i < listReaderWrapper(params, 'subject_references').size()-1)
                    base_qry += 'or'
            }
            base_qry += ' ) '
            /*base_qry += " and lower(tipp.subjectReference) in (select * from value (:subject_references))"
            qry_params.subject_references = params.list('subject_references').collect { "%"+it.toLowerCase()+"%" }*/
            filterSet = true
        }
        if (params.series_names && params.series_names != "" && listReaderWrapper(params, 'series_names')) {
            if(qry_params.size() > 0){
                base_qry += " and "
            }
            base_qry += " lower(tipp.seriesName) in (:series_names)"
            qry_params.series_names = listReaderWrapper(params, 'series_names').collect { ""+it.toLowerCase()+"" }
            filterSet = true
        }

        if(params.summaryOfContent) {
            if(qry_params.size() > 0){
                base_qry += " and "
            }
            base_qry += " lower(tipp.summaryOfContent) like :summaryOfContent "
            qry_params.summaryOfContent = "%${params.summaryOfContent.trim().toLowerCase()}%"
            filterSet = true
        }

        if(params.ebookFirstAutorOrFirstEditor) {
            if(qry_params.size() > 0){
                base_qry += " and "
            }
            base_qry += " (lower(tipp.firstAuthor) like :ebookFirstAutorOrFirstEditor or lower(tipp.firstEditor) like :ebookFirstAutorOrFirstEditor) "
            qry_params.ebookFirstAutorOrFirstEditor = "%${params.ebookFirstAutorOrFirstEditor.trim().toLowerCase()}%"
            filterSet = true
        }

        if(params.dateFirstOnlineFrom) {
            if(qry_params.size() > 0){
                base_qry += " and "
            }
            base_qry += " (tipp.dateFirstOnline is not null AND tipp.dateFirstOnline >= :dateFirstOnlineFrom) "
            qry_params.dateFirstOnlineFrom = sdf.parse(params.dateFirstOnlineFrom)
            filterSet = true

        }
        if(params.dateFirstOnlineTo) {
            if(qry_params.size() > 0){
                base_qry += " and "
            }
            base_qry += " (tipp.dateFirstOnline is not null AND tipp.dateFirstOnline <= :dateFirstOnlineTo) "
            qry_params.dateFirstOnlineTo = sdf.parse(params.dateFirstOnlineTo)
            filterSet = true
        }

        if(params.yearsFirstOnline) {
            if(qry_params.size() > 0){
                base_qry += " and "
            }
            base_qry += " (Year(tipp.dateFirstOnline) in (:yearsFirstOnline)) "
            qry_params.yearsFirstOnline = listReaderWrapper(params, 'yearsFirstOnline').collect { it instanceof String ? Integer.parseInt(it) : it }
            filterSet = true
        }

        if (params.identifier) {
            if(qry_params.size() > 0){
                base_qry += " and "
            }
            base_qry += " ( exists ( from Identifier ident where ident.tipp.id = tipp.id and ident.value like :identifier ) ) "
            qry_params.identifier = "${params.identifier}"
            filterSet = true
        }

        if (params.publishers) {
            if(qry_params.size() > 0){
                base_qry += " and "
            }
            //(exists (select orgRole from OrgRole orgRole where orgRole.tipp = tipp and orgRole.roleType.id = ${RDStore.OR_PUBLISHER.id} and orgRole.org.name in (:publishers))
            base_qry += " (lower(tipp.publisherName)) in (:publishers) "
            qry_params.publishers = listReaderWrapper(params, 'publishers').collect { it.toLowerCase() }
            filterSet = true
        }

        if (params.coverageDepth) {
            if(qry_params.size() > 0){
                base_qry += " and "
            }
            base_qry += " exists (select tc.id from tipp.coverages tc where lower(tc.coverageDepth) in (:coverageDepth))"
            qry_params.coverageDepth = listReaderWrapper(params, 'coverageDepth').collect { it.toLowerCase() }
            filterSet = true
        }

        if (params.title_types && params.title_types != "" && listReaderWrapper(params, 'title_types')) {
            if(qry_params.size() > 0){
                base_qry += " and "
            }
            base_qry += " lower(tipp.titleType) in (:title_types)"
            qry_params.title_types = listReaderWrapper(params, 'title_types').collect { ""+it.toLowerCase()+"" }
            filterSet = true
        }

        if (params.medium && params.medium != "" && listReaderWrapper(params, 'medium')) {
            if(qry_params.size() > 0){
                base_qry += " and "
            }
            base_qry += " tipp.medium.id in (:medium) "
            qry_params.medium = listReaderWrapper(params, 'medium').collect { String key -> Long.parseLong(key) }
            filterSet = true
        }

        if (params.gokbIds && params.gokbIds != "" && listReaderWrapper(params, 'gokbIds')) {
            if(qry_params.size() > 0){
                base_qry += " and "
            }
            base_qry += " gokbId in (:gokbIds)"
            qry_params.gokbIds = listReaderWrapper(params, 'gokbIds')
        }

        if ((params.sort != null) && (params.sort.length() > 0)) {
                base_qry += "order by ${params.sort} ${params.order} "
        }
        else {
            base_qry += "order by tipp.sortname"
        }

        result.query = base_qry
        result.queryParams = qry_params
        result.filterSet = filterSet

        result
    }

    Map<String, Object> prepareTitleSQLQuery(Map configMap, String entitlementInstance, Sql sql) {
        /*
        currently existing config parameters:
        configMap.sub
        configMap.ieStatus
        configMap.tippIds
        as defined in filterService.getTippQuery(), filterServie.getIssueEntitlementQuery()
        as defined in myInstitutionController.currentTitles()
         */
        String query = "", join = "", subJoin = "", where = "", orderClause = "", refdata_value_col = configMap.format == 'kbart' ? 'rdv_value' : I10nTranslation.getRefdataValueColumn(LocaleUtils.getCurrentLocale())
        Map<String, Object> params = [:]
        Connection connection = sql.dataSource.getConnection()
        //sql.withTransaction {
            SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
            if(entitlementInstance == TitleInstancePackagePlatform.class.name) {
                List<String> columns = ['tipp_id', '(select pkg_name from package where pkg_id = tipp_pkg_fk) as tipp_pkg_name', '(select plat_name from platform where plat_id = tipp_plat_fk) as tipp_plat_name',
                                        '(select plat_title_namespace from platform where plat_id = tipp_plat_fk) as tipp_plat_namespace',
                                        "case tipp_title_type when 'Journal' then 'serial' when 'Book' then 'monograph' when 'Database' then 'database' else 'other' end as title_type",
                                        'tipp_name as name', 'tipp_access_start_date as accessStartDate', 'tipp_access_end_date as accessEndDate',
                                        'tipp_publisher_name', "(select ${refdata_value_col} from refdata_value where rdv_id = tipp_medium_rv_fk) as tipp_medium", 'tipp_host_platform_url', 'tipp_date_first_in_print',
                                        'tipp_date_first_online', 'tipp_gokb_id', '(select pkg_gokb_id from package where pkg_id = tipp_pkg_fk) as tipp_pkg_uuid', 'tipp_date_created', 'tipp_last_updated', 'tipp_first_author', 'tipp_first_editor', 'tipp_volume', 'tipp_edition_number', 'tipp_series_name', 'tipp_subject_reference',
                                        "(select ${refdata_value_col} from refdata_value where rdv_id = tipp_status_rv_fk) as status",
                                        "(select ${refdata_value_col} from refdata_value where rdv_id = tipp_access_type_rv_fk) as accessType",
                                        "(select ${refdata_value_col} from refdata_value where rdv_id = tipp_open_access_rv_fk) as openAccess"]
                orderClause = " order by tipp_sort_name, tipp_name"
                query = "select ${columns.join(',')} from title_instance_package_platform"
                String subFilter = ""
                if(configMap.sub) {
                    params.subscription = configMap.sub.id
                    join += " join issue_entitlement on ie_tipp_fk = tipp_id"
                    subFilter = " and ie_subscription_fk = :subscription"
                }
                else if(configMap.subscription) {
                    params.subscription = configMap.subscription.id
                    join += " join issue_entitlement on ie_tipp_fk = tipp_id"
                    subFilter = " and ie_subscription_fk = :subscription"
                }
                else if(configMap.subscriptions) {
                    List<Object> subIds = []
                    subIds.addAll(configMap.subscriptions.id)
                    params.subscriptions = connection.createArrayOf('bigint', subIds.toArray())
                    join += " join issue_entitlement on ie_tipp_fk = tipp_id"
                    subFilter = " and ie_subscription_fk = any(:subscriptions)"
                }
                if(configMap.pkgfilter != null && !configMap.pkgfilter.isEmpty()) {
                    params.pkgId = Long.parseLong(configMap.pkgfilter)
                    where += " tipp_pkg_fk = :pkgId"
                }
                else {
                    List<Object> pkgIds = []
                    pkgIds.addAll(configMap.pkgIds)
                    params.pkgIds = connection.createArrayOf('bigint', pkgIds.toArray())
                    where += " tipp_pkg_fk = any(:pkgIds)"
                }
                where += subFilter
                if(configMap.asAt && configMap.asAt.length() > 0) {
                    Date dateFilter = DateUtils.getLocalizedSDF_noTime().parse(configMap.asAt)
                    params.asAt = new Timestamp(dateFilter.getTime())
                    where += " and ((:asAt >= tipp_access_start_date or tipp_access_start_date is null) and (:asAt <= tipp_access_end_date or tipp_access_end_date is null))"
                }
                if(configMap.status != null && !configMap.status.isEmpty()) {
                    params.tippStatus = configMap.status instanceof String ? Long.parseLong(configMap.status) : configMap.status //already id
                    where += " and tipp_status_rv_fk = :tippStatus"
                }
                else if(configMap.notStatus != null && !configMap.notStatus.isEmpty()) {
                    params.tippStatus = configMap.notStatus instanceof String ? Long.parseLong(configMap.notStatus) : configMap.status //already id
                    where += " and tipp_status_rv_fk != :tippStatus"
                }
                else {
                    params.tippStatus = RDStore.TIPP_STATUS_CURRENT.id
                    where += " and tipp_status_rv_fk = :tippStatus"
                }
            }
            else if(entitlementInstance == IssueEntitlement.class.name) {
                List<String> columns
                switch(configMap.select) {
                    case 'bulkInsertTitleGroup': columns = ['0', 'now()', 'ie_id', configMap.titleGroupInsert, 'now()']
                        break
                    case 'bulkInsertPriceItem': columns = ['ie_id']
                        break
                    default: columns = ['ie_id', 'tipp_id', '(select pkg_name from package where pkg_id = tipp_pkg_fk) as tipp_pkg_name', '(select plat_name from platform where plat_id = tipp_plat_fk) as tipp_plat_name',
                                   '(select plat_title_namespace from platform where plat_id = tipp_plat_fk) as tipp_plat_namespace',
                                   "case tipp_title_type when 'Journal' then 'serial' when 'Book' then 'monograph' when 'Database' then 'database' else 'other' end as title_type",
                                   'tipp_name as name', 'coalesce(ie_access_start_date, tipp_access_start_date) as accessStartDate', 'coalesce(ie_access_end_date, tipp_access_end_date) as accessEndDate',
                                   'tipp_publisher_name', "(select ${refdata_value_col} from refdata_value where rdv_id = tipp_medium_rv_fk) as tipp_medium", 'tipp_host_platform_url', 'tipp_date_first_in_print',
                                   'tipp_date_first_online', 'tipp_gokb_id', '(select pkg_gokb_id from package where pkg_id = tipp_pkg_fk) as tipp_pkg_uuid', 'tipp_date_created', 'tipp_last_updated', 'tipp_first_author', 'tipp_first_editor', 'tipp_volume', 'tipp_edition_number', 'tipp_series_name', 'tipp_subject_reference',
                                   "(select ${refdata_value_col} from refdata_value where rdv_id = ie_status_rv_fk) as status",
                                   "(select ${refdata_value_col} from refdata_value where rdv_id = tipp_access_type_rv_fk) as accessType",
                                   "(select ${refdata_value_col} from refdata_value where rdv_id = tipp_open_access_rv_fk) as openAccess"]
                        break
                }
                query = "select ${columns.join(',')} from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id join subscription on ie_subscription_fk = sub_id"
                if(configMap.pkgfilter != null && !configMap.pkgfilter.isEmpty()) {
                    params.pkgId = Long.parseLong(configMap.pkgfilter)
                    where = " tipp_pkg_fk = :pkgId"
                }
                else {
                    List<Object> pkgIds = []
                    pkgIds.addAll(configMap.pkgIds)
                    params.pkgIds = connection.createArrayOf('bigint', pkgIds.toArray())
                    where = " tipp_pkg_fk = any(:pkgIds)"
                }
                /*
                where += " and or_sub_fk = :ctxId"
                subJoin = " join org_role on or_sub_fk = ie_subscription_fk "
                params.ctxId = contextService.getOrg().id
                if(contextService.getOrg().getCustomerType() == CustomerTypeService.ORG_CONSORTIUM_BASIC)
                    where += " and sub_parent_sub_fk is null"
                */
                orderClause = " order by tipp_sort_name, tipp_name"
                if(configMap.sub) {
                    if(configMap.sub instanceof Subscription)
                        params.subscription = configMap.sub.id
                    else params.subscription = Long.parseLong(configMap.sub)
                    where += " and ie_subscription_fk = :subscription"
                }
                else if(configMap.subscription) {
                    if(configMap.subscription instanceof Subscription)
                        params.subscription = configMap.subscription.id
                    else params.subscription = Long.parseLong(configMap.subscription)
                    where += " and ie_subscription_fk = :subscription"
                }
                else if(configMap.subscriptions) {
                    List<Object> subIds = []
                    subIds.addAll(configMap.subscriptions.id)
                    params.subscriptions = connection.createArrayOf('bigint', subIds.toArray())
                    where += " and ie_subscription_fk = any(:subscriptions)"
                }
                if(configMap.asAt != null && !configMap.asAt.isEmpty()) {
                    Date dateFilter = sdf.parse(configMap.asAt)
                    params.asAt = new Timestamp(dateFilter.getTime())
                    where += " and ((:asAt >= (coalesce(ie_access_start_date, tipp_access_start_date, sub_start_date)) or (ie_access_start_date is null and tipp_access_start_date is null and sub_start_date is null)) and (:asAt <= coalesce(ie_access_end_date, tipp_access_end_date, sub_end_date) or (ie_access_start_date is null and tipp_access_end_date is null and sub_end_date is null)))"
                }
                if(configMap.validOn != null) {
                    params.validOn = new Timestamp(configMap.validOn)
                    where += ' and ( (:validOn >= coalesce(ie_access_start_date, sub_start_date, tipp_access_start_date) or (ie_access_start_date is null and sub_start_date is null and tipp_access_start_date is null) ) and ( :validOn <= coalesce(ie_access_end_date, sub_end_date, tipp_access_end_date) or (ie_access_end_date is null and sub_end_date is null and tipp_access_end_date is null) ) or sub_has_perpetual_access = true)'
                }
                if(configMap.ieStatus) {
                    params.ieStatus = configMap.ieStatus.id
                    where += " and ie_status_rv_fk = :ieStatus"
                }
                else if(configMap.status) {
                    params.ieStatus = connection.createArrayOf('bigint', listReaderWrapper(configMap, 'status').toArray())
                    where += " and ie_status_rv_fk = any(:ieStatus)"
                }
                else if(configMap.notStatus != null && !configMap.notStatus.isEmpty()) {
                    params.ieStatus = configMap.notStatus instanceof String ? Long.parseLong(configMap.notStatus) : configMap.notStatus //already id
                    where += " and ie_status_rv_fk != :ieStatus"
                }
                else {
                    params.ieStatus = RDStore.TIPP_STATUS_CURRENT.id
                    where += " and ie_status_rv_fk = :ieStatus"
                }
                if(configMap.titleGroup != null && !configMap.titleGroup.isEmpty()) {
                    params.titleGroup = Long.parseLong(configMap.titleGroup)
                    where += " and exists(select igi_id from issue_entitlement_group_item where igi_ie_group_fk = :titleGroup and igi_ie_fk = ie_id)"
                }
                if(configMap.inTitleGroups != null && !configMap.inTitleGroups.isEmpty()) {
                    if(configMap.inTitleGroups == RDStore.YN_YES.id.toString()) {
                        where += " and exists ( select igi_id from issue_entitlement_group_item where igi_ie_fk = ie_id) "
                    }
                    else{
                        where += " and not exists ( select igi_id from issue_entitlement_group_item where igi_ie_fk = ie_id) "
                    }
                }
                if (configMap.hasPerpetualAccess && !configMap.hasPerpetualAccessBySubs) {
                    if(configMap.hasPerpetualAccess == RDStore.YN_YES.id.toString()) {
                        where += " and ie_perpetual_access_by_sub_fk is not null "
                    }
                    else{
                        where += " and ie_perpetual_access_by_sub_fk is null "
                    }
                }
                if (configMap.hasPerpetualAccess && configMap.hasPerpetualAccessBySubs) {
                    List<Object> perpetualSubs = []
                    perpetualSubs.addAll(listReaderWrapper(configMap, 'hasPerpetualAccessBySubs'))
                    params.perpetualSubs = connection.createArrayOf('bigint', perpetualSubs.toArray())
                    if(configMap.hasPerpetualAccess == RDStore.YN_NO.id.toString()) {
                        where += " and tipp_host_platform_url not in (select tipp2.tipp_host_platform_url from issue_entitlement as ie2 join title_instance_package_platform as tipp2 on ie2.ie_tipp_fk = tipp2.tipp_id where ie2.ie_perpetual_access_by_sub_fk = any(:perpetualSubs)) "
                    }
                    else {
                        where += " and tipp_host_platform_url in (select tipp2.tipp_host_platform_url from issue_entitlement as ie2 join title_instance_package_platform as tipp2 on ie2.ie_tipp_fk = tipp2.tipp_id where ie2.ie_perpetual_access_by_sub_fk = any(:perpetualSubs)) "
                    }
                }
                if (configMap.coverageDepth != null && !configMap.coverageDepth.isEmpty()) {
                    List<Object> coverageDepths = []
                    coverageDepths.addAll(listReaderWrapper(configMap, 'coverageDepth').collect { it.toLowerCase() })
                    params.coverageDepth = connection.createArrayOf('varchar', coverageDepths.toArray())
                    where += " and exists (select ic_id from issue_entitlement_coverages where ic_ie_fk = ie_id and lower(ic_coverage_depth) = any(:coverageDepth))"
                }
                if(configMap.filterSub != null && !configMap.filterSub.isEmpty()) {
                    List<Object> subscriptions = []
                    subscriptions.addAll(listReaderWrapper(configMap, 'filterSub').collect { Long.parseLong(it)} )
                    params.subscriptions = connection.createArrayOf('bigint', subscriptions.toArray())
                    where += " and sub_id = any(:subscriptions)"
                }
            }
            if(configMap.tippIds != null && !configMap.tippIds.isEmpty()) {
                List<Object> tippIDs = []
                tippIDs.addAll(configMap.tippIds)
                params.tippIds = connection.createArrayOf('bigint', tippIDs.toArray())
                where += " and tipp_id = any(:tippIds)"
            }
            if(configMap.filter != null && !configMap.filter.isEmpty()) {
                params.stringFilter = configMap.filter
                where += " and ((genfunc_filter_matcher(tipp_name, :stringFilter) = true) or (genfunc_filter_matcher(tipp_first_author, :stringFilter) = true) or (genfunc_filter_matcher(tipp_first_editor, :stringFilter) = true) or exists(select id_id from identifier where id_tipp_fk = tipp_id and genfunc_filter_matcher(id_value, :stringFilter) = true))"
            }
            if(configMap.ddcs != null && !configMap.ddcs.isEmpty()) {
                List<Object> ddcs = []
                ddcs.addAll(listReaderWrapper(configMap, 'ddcs').collect{ String key -> Long.parseLong(key) })
                params.ddcs = connection.createArrayOf('bigint', ddcs.toArray())
                where += " and exists(select ddc_id from dewey_decimal_classification where ddc_tipp_fk = tipp_id and ddc_rv_fk = any(:ddcs))"
            }
            if(configMap.languages != null && !configMap.languages.isEmpty()) {
                List<Object> languages = []
                languages.addAll(listReaderWrapper(configMap, 'languages').collect{ String key -> Long.parseLong(key) })
                params.languages = connection.createArrayOf('bigint', languages.toArray())
                where += " and exists(select lang_id from language where lang_tipp_fk = tipp_id and lang_rv_fk = any(:languages))"
            }
            if(configMap.subject_references != null && !configMap.subject_references.isEmpty()) {
                List<Object> subjectReferences = []
                subjectReferences.addAll(listReaderWrapper(configMap, 'subject_references').collect { '%'+it.toLowerCase()+'%' })
                params.subjectReferences = connection.createArrayOf('varchar', subjectReferences.toArray())
                where += " and lower(tipp_subject_reference) like any(:subjectReferences)"
            }
            if(configMap.series_names != null && !configMap.series_names.isEmpty()) {
                List<Object> seriesNames = []
                seriesNames.addAll(listReaderWrapper(configMap, 'series_names').collect { it.toLowerCase() })
                params.seriesNames = connection.createArrayOf('varchar', seriesNames.toArray())
                where += " and lower(tipp_series_name) in (:seriesNames)"
            }
            if(configMap.summaryOfContent != null && !configMap.summaryOfContent.isEmpty()) {
                params.summaryOfContent = configMap.summaryOfContent
                where += " and genfunc_filter_matcher(tipp_summary_of_content, :summaryOfContent) = true"
            }
            if(configMap.ebookFirstAutorOrFirstEditor != null && !configMap.ebookFirstAutorOrFirstEditor.isEmpty()) {
                params.firstAuthorEditor = configMap.ebookFirstAutorOrFirstEditor
                where += " and genfunc_filter_matcher(tipp_first_author, :firstAuthorEditor) = true or genfunc_filter_matcher(tipp_first_editor, :firstAuthorEditor) = true)"
            }
            if(configMap.dateFirstOnlineFrom != null && !configMap.dateFirstOnlineFrom.isEmpty()) {
                Date dateFirstOnlineFrom = sdf.parse(configMap.dateFirstOnlineFrom)
                params.dateFirstOnlineFrom = DateUtils.getSDF_yyyyMMdd().format(dateFirstOnlineFrom)
                where += " and (tipp_date_first_online is not null AND tipp_date_first_online >= :dateFirstOnlineFrom)"
            }
            if(configMap.dateFirstOnlineTo != null && !configMap.dateFirstOnlineTo.isEmpty()) {
                Date dateFirstOnlineTo = sdf.parse(configMap.dateFirstOnlineTo)
                params.dateFirstOnlineTo = DateUtils.getSDF_yyyyMMdd().format(dateFirstOnlineTo)
                where += " and (tipp.date_first_online is not null AND tipp_date_first_online <= :dateFirstOnlineTo)"
            }
            if(configMap.yearsFirstOnline != null && !configMap.yearsFirstOnline.isEmpty()) {
                List<Object> yearsFirstOnline = []
                yearsFirstOnline.addAll(listReaderWrapper(configMap, 'yearsFirstOnline').collect { Integer.parseInt(it) })
                params.yearsFirstOnline = connection.createArrayOf('int', yearsFirstOnline.toArray())
                where += " and (date_part('year', tipp_date_first_online) = any(:yearsFirstOnline))"
            }
            if(configMap.identifier != null && !configMap.identifier.isEmpty()) {
                params.identifier = configMap.identifier
                where += " and exists(select id_id from identifier where id_tipp_fk = tipp_id and genfunc_filter_matcher(id_value, :identifier) = true)"
            }
            if(configMap.publishers != null && !configMap.publishers.isEmpty()) {
                List<Object> publishers = []
                publishers.addAll(listReaderWrapper(configMap, 'publishers').collect { it.toLowerCase() })
                params.publishers = connection.createArrayOf('varchar', publishers.toArray())
                where += " and lower(tipp_publisher_name) = any(:publishers)"
            }
            if(configMap.title_types != null && !configMap.title_types.isEmpty()) {
                List<Object> titleTypes = []
                titleTypes.addAll(listReaderWrapper(configMap, 'title_types').collect { it.toLowerCase() })
                params.titleTypes = connection.createArrayOf('varchar', titleTypes.toArray())
                where += " and lower(tipp_title_type) = any(:titleTypes)"
            }
            if(configMap.medium != null && !configMap.medium.isEmpty()) {
                List<Object> medium = []
                medium.addAll(listReaderWrapper(configMap, 'medium').collect{ String key -> Long.parseLong(key) })
                params.medium = connection.createArrayOf('bigint', medium.toArray())
                where += " and tipp_medium_rv_fk = any(:medium)"
            }
            if(configMap.filterPvd != null && !configMap.filterPvd.isEmpty()) {
                List<Object> providers = [], providerRoleTypes = [RDStore.OR_CONTENT_PROVIDER.id, RDStore.OR_PROVIDER.id, RDStore.OR_AGENCY.id, RDStore.OR_PUBLISHER.id]
                providers.addAll(listReaderWrapper(configMap, 'filterPvd').collect { Long.parseLong(it)} )
                params.providers = connection.createArrayOf('bigint', providers.toArray())
                params.providerRoleTypes = connection.createArrayOf('bigint', providerRoleTypes.toArray())
                where += " and exists(select or_id from org_role where or_tipp_fk = tipp_id and or_org_fk = any(:providers) and or_roletype_fk = any(:providerRoleTypes))"
            }
            if(configMap.filterHostPlat != null && !configMap.filterHostPlat.isEmpty()) {
                List<Object> hostPlatforms = []
                hostPlatforms.addAll(listReaderWrapper(configMap, 'filterHostPlat').collect { Long.parseLong(it) })
                params.platforms = connection.createArrayOf('bigint', hostPlatforms.toArray())
                where += " and tipp_plat_fk = any(:platforms)"
            }
            /*

            if(!params.forCount)
                base_qry += " group by tipp, ic, ie.id "
            else base_qry += " group by tipp, ic "

            if ((params.sort != null) && (params.sort.length() > 0)) {
                if(params.sort == 'startDate')
                    base_qry += "order by ic.startDate ${params.order}, lower(tipp.sortname) asc "
                else if(params.sort == 'endDate')
                    base_qry += "order by ic.endDate ${params.order}, lower(tipp.sortname) asc "
                else
                    base_qry += "order by ie.${params.sort} ${params.order} "
            }
            else if(!params.forCount){
                base_qry += "order by lower(ie.sortname) asc, lower(ie.tipp.sortname) asc"
            }
            */
        //}
        [query: query, join: join, where: where, order: orderClause, params: params, subJoin: subJoin]
    }

    List listReaderWrapper(Map params, String key) {
        if(params instanceof GrailsParameterMap)
            return params.list(key)
        //.respondsTo('size') is a substitute for instanceof Ljava.lang.String;
        else if(params[key] instanceof List || (params[key].respondsTo('size') && !(params[key] instanceof String))) {
            return params[key]
        }
        else return [params[key]]
    }

}
