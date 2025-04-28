package de.laser


import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.helper.Params
import de.laser.storage.PropertyStore
import de.laser.storage.RDConstants
import de.laser.survey.SurveyConfigPackage
import de.laser.survey.SurveyConfigProperties
import de.laser.survey.SurveyConfigVendor
import de.laser.survey.SurveyResult
import de.laser.survey.SurveyVendorResult
import de.laser.utils.DateUtils
import de.laser.storage.RDStore
import de.laser.properties.PropertyDefinition
import de.laser.survey.SurveyConfig
import de.laser.utils.LocaleUtils
import de.laser.wekb.Package
import de.laser.wekb.TitleInstancePackagePlatform
import de.laser.wekb.Vendor
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
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
    PropertyService propertyService

    // FilterService.Result fsr = filterService.getXQuery(paramsClone, ..)
    // if (fsr.isFilterSet) { paramsClone.filterSet = true }

    static Map<String, Map> PLATFORM_FILTER_AUTH_FIELDS = ['shibbolethAuthentication': [rdcat: RDConstants.Y_N, label: 'platform.auth.shibboleth.supported'],
                                                              'openAthens': [rdcat: RDConstants.Y_N, label: 'platform.auth.openathens.supported'],
                                                              'ipAuthentication': [rdcat: RDConstants.IP_AUTHENTICATION, label: 'platform.auth.ip.supported'],
                                                              'passwordAuthentication': [rdcat: RDConstants.Y_N, label: 'platform.auth.userPass.supported'],
                                                              'mailDomain': [rdcat: RDConstants.Y_N, label: 'platform.auth.mailDomain.supported'],
                                                              'refererAuthentication': [rdcat: RDConstants.Y_N, label: 'platform.auth.referer.supported'],
                                                              'ezProxy': [rdcat: RDConstants.Y_N, label: 'platform.auth.ezProxy.supported'],
                                                              'hanServer': [rdcat: RDConstants.Y_N, label: 'platform.auth.hanServer.supported'],
                                                              'otherProxies': [rdcat: RDConstants.Y_N, label: 'platform.auth.other.proxies']]
    static Map<String, Map> PLATFORM_FILTER_ACCESSIBILITY_FIELDS = ['accessPlatform': [rdcat: RDConstants.ACCESSIBILITY_COMPLIANCE, label: 'platform.accessibility.accessPlatform'],
                                                                    'viewerForPdf': [rdcat: RDConstants.ACCESSIBILITY_COMPLIANCE, label: 'platform.accessibility.viewerForPdf'],
                                                                    'viewerForEpub': [rdcat: RDConstants.ACCESSIBILITY_COMPLIANCE, label: 'platform.accessibility.viewerForEpub'],
                                                                    'playerForAudio': [rdcat: RDConstants.ACCESSIBILITY_COMPLIANCE, label: 'platform.accessibility.playerForAudio'],
                                                                    'playerForVideo': [rdcat: RDConstants.ACCESSIBILITY_COMPLIANCE, label: 'platform.accessibility.playerForVideo'],
                                                                    'accessibilityStatementAvailable': [rdcat: RDConstants.Y_N, label: 'platform.accessibility.accessibilityStatementAvailable'],
                                                                    'accessEPub': [rdcat: RDConstants.ACCESSIBILITY_COMPLIANCE, label: 'platform.accessibility.accessEPub'],
                                                                    'accessPdf': [rdcat: RDConstants.ACCESSIBILITY_COMPLIANCE, label: 'platform.accessibility.accessPdf'],
                                                                    'accessAudio': [rdcat: RDConstants.ACCESSIBILITY_COMPLIANCE, label: 'platform.accessibility.accessAudio'],
                                                                    'accessVideo': [rdcat: RDConstants.ACCESSIBILITY_COMPLIANCE, label: 'platform.accessibility.accessVideo'],
                                                                    'accessDatabase': [rdcat: RDConstants.ACCESSIBILITY_COMPLIANCE, label: 'platform.accessibility.accessDatabase']]
    static Map<String, Map> PLATFORM_FILTER_ADDITIONAL_SERVICE_FIELDS = ['individualDesignLogo': [rdcat: RDConstants.Y_N, label: 'platform.additional.individualDesignLogo'],
                                                                         'fullTextSearch': [rdcat: RDConstants.Y_N, label: 'platform.additional.fullTextSearch']]
    static Map<String, Map> PACKAGE_FILTER_GENERIC_FIELDS = ['curatoryGroup' : [label: 'package.curatoryGroup.label'],
                                                             'automaticUpdates': [label: 'package.source.automaticUpdates'],
                                                             'paymentType': [label: 'package.paymentType.label'],
                                                             'contentType': [label: 'package.content.type.label'],
                                                             'openAccess': [label: 'package.openAccess.label'],
                                                             'ddc': [label: 'package.ddc.label'],
                                                             'archivingAgency': [label: 'package.archivingAgency.label']]

    /**
     * Subclass for generic parameter containing:
     * <ul>
     *     <li>the query string</li>
     *     <li>the parameter map</li>
     *     <li>the flag marking whether a filter has been set</li>
     * </ul>
     */
    class Result {

        /**
         * Constructor call to initialise the generic container
         * @param query the query string
         * @param queryParams the named argument map
         * @param isFilterSet the flag whether a filter has been applied to the results to be retrieved or not
         */
        Result(String query = null, Map<String, Object>queryParams = [:], boolean isFilterSet = false) {
            this.query        = query
            this.queryParams  = queryParams
            this.isFilterSet  = isFilterSet

            log.debug('query:       ' + query)
            log.debug('queryParams: ' + queryParams.toMapString())
        }

        public String query
        public Map<String, Object> queryParams
        public boolean isFilterSet
    }

    /**
     * Processes organisation filters and generates a query to fetch organisations
     * @param params the filter parameter map
     * @return the result containing the query and the prepared query parameters
     */
    Result getOrgQuery(GrailsParameterMap params) {
        int hashCode = params.hashCode()

        ArrayList<String> query = []
        Map<String, Object> queryParams = [:]

        if (params.orgNameContains?.length() > 0) {
            query << "((genfunc_filter_matcher(o.name, :orgNameContains) = true or genfunc_filter_matcher(o.sortname, :orgNameContains) = true) or exists(select alt.id from AlternativeName alt where alt.org = o and genfunc_filter_matcher(alt.name, :orgNameContains) = true) )"
             queryParams << [orgNameContains : "${params.orgNameContains}"]
        }

        if (params.orgRole) {
            query << " exists (select ogr from o.links as ogr where ogr.roleType.id = :orgRole )"
             queryParams << [orgRole : params.long('orgRole')]
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

        if (params.subjectGroup) {
            query << "exists (select osg from OrgSubjectGroup as osg where osg.org.id = o.id and osg.subjectGroup.id in (:subjectGroup))"
            queryParams << [subjectGroup : Params.getLongList(params, 'subjectGroup')]
        }

        if (params.discoverySystemsFrontend) {
            query << "exists (select dsf from DiscoverySystemFrontend as dsf where dsf.org.id = o.id and dsf.frontend.id in (:frontends))"
            queryParams << [frontends : Params.getLongList(params, 'discoverySystemsFrontend')]
        }
        if (params.discoverySystemsIndex) {
            query << "exists (select dsi from DiscoverySystemIndex as dsi where dsi.org.id = o.id and dsi.index.id in (:indices))"
            queryParams << [indices : Params.getLongList(params, 'discoverySystemsIndex')]
        }

        if (params.libraryNetwork) {
            query << "o.libraryNetwork.id in (:libraryNetwork)"
            queryParams << [libraryNetwork : Params.getLongList(params, 'libraryNetwork')]
        }
        if (params.libraryType) {
            query << "o.libraryType.id in (:libraryType)"
            queryParams << [libraryType : Params.getLongList(params, 'libraryType')]
        }

        if (params.country) {
            query << "o.country.id in (:country)"
            queryParams << [country : Params.getLongList(params, 'country')]
        }
        if (params.region) {
            query << "o.region.id in (:region)"
            queryParams << [region : Params.getLongList(params, 'region')]
        }

        if (params.customerType) {
            query << "exists (select oss from OrgSetting as oss where oss.org.id = o.id and oss.key = :customerTypeKey and oss.roleValue.id in (:customerTypeList))"
            queryParams << [customerTypeList : Params.getLongList(params, 'customerType')]
            queryParams << [customerTypeKey  : OrgSetting.KEYS.CUSTOMER_TYPE]
        }
        if (params.osApiLevel) {
            query << "exists (select osApi from OrgSetting as osApi where osApi.org.id = o.id and osApi.key = :apiLevelKey and osApi.strValue in (:apiLevelList))"
            queryParams << [apiLevelList : params.list('osApiLevel')]
            queryParams << [apiLevelKey  : OrgSetting.KEYS.API_LEVEL]
        }
        if (params.osServerAccess) {
            query << "exists (select osAcc from OrgSetting as osAcc where osAcc.org.id = o.id and osAcc.key in (:serverAccessKeys) and osAcc.rdValue = :serverAccessYes)"
            queryParams << [serverAccessKeys : params.list('osServerAccess').collect{ OrgSetting.KEYS[it as String] }]
            queryParams << [serverAccessYes  : RDStore.YN_YES]
        }

        if (params.isBetaTester) {
            if (params.long('isBetaTester') == RDStore.YN_YES.id) {
                query << "o.isBetaTester is true"
            } else {
                query << "o.isBetaTester is false"
            }
        }

        if (params.isLegallyObliged in ['yes', 'no']) {
            query << "o.legallyObligedBy " + (params.isLegallyObliged == 'yes' ? "is not null" : "is null")
        }
        if (params.legallyObligedBy) {
            query << "o.legallyObligedBy.id in (:legallyObligedBy)"
            queryParams << [legallyObligedBy: Params.getLongList(params, 'legallyObligedBy')]
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

        String hql = (query.size() > 0 ? "from Org o where " + query.join(" and ") : "from Org o ") + defaultOrder

        if (params.hashCode() != hashCode) {
            log.debug 'GrailsParameterMap was modified @ getOrgQuery()'
        }

        new Result( hql, queryParams )
    }

    /**
     * Processes institution filters and generates a query to fetch institution; in addition, institutions
     * linked by combo to the given institution are fetched
     * @param params the filter parameter map
     * @param org the consortium to which the institutions are linked
     * @return the result containing the query and the prepared query parameters
     */
    Result getOrgComboQuery(GrailsParameterMap params, Org org) {
        int hashCode = params.hashCode()
        boolean isFilterSet = false

        ArrayList<String> query = []
        Map<String, Object> queryParams = [:]

        if (params.orgNameContains?.length() > 0) {
            query << "(genfunc_filter_matcher(o.name, :orgNameContains1) = true or genfunc_filter_matcher(o.sortname, :orgNameContains2) = true) "
             queryParams << [orgNameContains1 : "${params.orgNameContains}"]
             queryParams << [orgNameContains2 : "${params.orgNameContains}"]
        }
        if (params.region) {
            query << "o.region.id in (:region)"
            queryParams << [region : Params.getLongList(params, 'region')]
        }
        if (params.country) {
            query << "o.country.id in (:country)"
            queryParams << [country : Params.getLongList(params, 'country')]
        }

        if (params.subjectGroup) {
            query << "exists (select osg from OrgSubjectGroup as osg where osg.org.id = o.id and osg.subjectGroup.id in (:subjectGroup))"
            queryParams << [subjectGroup : Params.getLongList(params, "subjectGroup")]
        }

        if (params.discoverySystemsFrontend) {
            query << "exists (select dsf from DiscoverySystemFrontend as dsf where dsf.org.id = o.id and dsf.frontend.id in (:frontends))"
            queryParams << [frontends : Params.getLongList(params, "discoverySystemsFrontend")]
        }

        if (params.discoverySystemsIndex) {
            query << "exists (select dsi from DiscoverySystemIndex as dsi where dsi.org.id = o.id and dsi.index.id in (:indices))"
            queryParams << [indices : Params.getLongList(params, "discoverySystemsIndex")]
        }

        if (params.libraryNetwork) {
            query << "o.libraryNetwork.id in (:libraryNetwork)"
            queryParams << [libraryNetwork : Params.getLongList(params, "libraryNetwork")]
        }

        if (params.libraryType) {
            query << "o.libraryType.id in (:libraryType)"
            queryParams << [libraryType : Params.getLongList(params, "libraryType")]
        }

        if ((params.subStatus || params.subValidOn || params.subPerpetual) && !params.filterPvd) {
            List<RefdataValue> subStatus
            String subQuery = "exists (select oo.id from OrgRole oo join oo.sub sub join sub.orgRelations ooCons where oo.org.id = o.id and oo.roleType in (:subscrRoles) and ooCons.org = :context and ooCons.roleType = :consType"
            if(params.invertDirection) {
                subQuery = "exists (select oo.id from OrgRole oo join oo.sub sub join sub.orgRelations ooCons where oo.org = :context and oo.roleType in (:subscrRoles) and ooCons.org.id = o.id and ooCons.roleType = :consType"
                queryParams << [subscrRoles: [RDStore.OR_SUBSCRIBER_CONS], consType: RDStore.OR_SUBSCRIPTION_CONSORTIUM, context: contextService.getOrg()]
            }
            else
                queryParams << [subscrRoles: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN], consType: RDStore.OR_SUBSCRIPTION_CONSORTIUM, context: contextService.getOrg()]
            String subQueryBase = subQuery
            if (params.subStatus) {
                subQuery +=  " and (sub.status in (:subStatus)" // ( closed in line 281; needed to prevent consortia members without any subscriptions because or would lift up the other restrictions)
                subStatus = Params.getRefdataList(params, "subStatus")
                queryParams << [subStatus: subStatus]
                if (!params.subValidOn && params.subPerpetual && RDStore.SUBSCRIPTION_CURRENT in subStatus)
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
            subQuery+=")" //opened in line 267
            if(subStatus && RDStore.GENERIC_NULL_VALUE in subStatus) {
                subQuery = "(${subQuery} or not (${subQueryBase})))"
            }
            query << subQuery
        }

        if(params.sub && (params.hasSubscription &&  !params.hasNotSubscription) || (!params.hasSubscription && params.hasNotSubscription)) {
            String subQuery = ""

            if(params.hasNotSubscription) {
                subQuery = " not"
            }

            subQuery = subQuery + " exists (select oo.id from OrgRole oo join oo.sub sub join sub.orgRelations ooCons where oo.org.id = o.id and oo.roleType in (:subscrRoles) and ooCons.org = :context and ooCons.roleType = :consType and sub.instanceOf = :sub)"
            queryParams << [subscrRoles: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN], consType: RDStore.OR_SUBSCRIPTION_CONSORTIUM, context: contextService.getOrg(), sub: params.sub]
            query << subQuery
        }

        if (params.sub && (params.subRunTimeMultiYear || params.subRunTime)) {
            String subQuery = ""

            subQuery = subQuery + " exists (select oo.id from OrgRole oo join oo.sub sub join sub.orgRelations ooCons where oo.org.id = o.id and oo.roleType in (:subscrRoles) and ooCons.org = :context and ooCons.roleType = :consType and sub.instanceOf = :sub "
            queryParams << [subscrRoles: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN], consType: RDStore.OR_SUBSCRIPTION_CONSORTIUM, context: contextService.getOrg(), sub: params.sub]
            if (params.subRunTimeMultiYear && !params.subRunTime) {
                subQuery += " and sub.isMultiYear = :subRunTimeMultiYear "
                queryParams.put('subRunTimeMultiYear', true)

            }else if (!params.subRunTimeMultiYear && params.subRunTime){
                subQuery += " and sub.isMultiYear = :subRunTimeMultiYear "
                queryParams.put('subRunTimeMultiYear', false)
            }
            query << subQuery + ' )'
        }

        if (params.sub && !((params.hasSubscription &&  !params.hasNotSubscription) || (!params.hasSubscription && params.hasNotSubscription) || (params.subRunTimeMultiYear || params.subRunTime))) {
            String subQuery = ""

            subQuery = subQuery + " exists (select oo.id from OrgRole oo join oo.sub sub join sub.orgRelations ooCons where oo.org.id = o.id and oo.roleType in (:subscrRoles) and ooCons.org = :context and ooCons.roleType = :consType and sub.instanceOf = :sub)"

            queryParams << [subscrRoles: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN], consType: RDStore.OR_SUBSCRIPTION_CONSORTIUM, context: contextService.getOrg(), sub: params.sub]
            query << subQuery
        }

        if (params.filterPvd) {
            String subQuery = " exists (select oo.id from OrgRole oo join oo.sub sub join sub.orgRelations ooCons where oo.org.id = o.id and oo.roleType in (:subscrRoles) and ooCons.org = :context and ooCons.roleType = :consType and exists (select pvr from ProviderRole pvr where pvr.subscription = sub and pvr.provider.id in (:filterPvd)) "
            queryParams << [subscrRoles: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN], consType: RDStore.OR_SUBSCRIPTION_CONSORTIUM, context: contextService.getOrg(), filterPvd: Params.getLongList(params, 'filterPvd')]
            String subQueryBase = "not exists (select oo2.id from OrgRole oo2 join oo2.sub sub2 join sub2.orgRelations ooCons2 where oo2.org.id = o.id and oo2.roleType in (:subscrRoles) and ooCons2.org = :context and ooCons2.roleType = :consType)"
            if (params.subStatus) {
                subQuery +=  " and (sub.status in (:subStatus)" // ( closed in line 322; needed to prevent consortia members without any subscriptions because or would lift up the other restrictions)
                List<RefdataValue> subStatus = Params.getRefdataList(params, 'subStatus')
                subStatus.remove(RDStore.GENERIC_NULL_VALUE)
                queryParams << [subStatus: subStatus]
                if (!params.subValidOn && params.subPerpetual && RDStore.SUBSCRIPTION_CURRENT in subStatus)
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

            subQuery+=")"

            if(params.subStatus && RDStore.GENERIC_NULL_VALUE in Params.getRefdataList(params, 'subStatus')) {
                subQuery = "((${subQuery}) or (${subQueryBase}))"
            }
            log.debug(subQuery)
            query << subQuery
            // params.filterSet = true // ERMS-5516
            isFilterSet = true
        }

        if (params.customerType) {
            query << "exists (select oss from OrgSetting as oss where oss.org.id = o.id and oss.key = :customerTypeKey and oss.roleValue.id in (:customerType))"
            queryParams << [customerType : Params.getLongList(params,'customerType')]
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
            List<String> fields = listReaderWrapper(params, 'customerIDNamespace')
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

        String hql = ''
        String defaultOrder = " order by " + (params.sort && (!params.consSort && !params.ownSort && !params.subscrSort) ? params.sort : " LOWER(o.sortname)") + " " + (params.order && (!params.consSort && !params.ownSort && !params.subscrSort) ? params.order : "asc")

        String direction = "c.fromOrg = o and c.toOrg = :org"
        if(params.invertDirection)
            direction = "c.fromOrg = :org and c.toOrg = o"
        if (query.size() > 0) {
            hql = "select o from Org as o, Combo as c where " + query.join(" and ") + " and " + direction + " and c.type.value = :comboType " + defaultOrder
        } else {
            hql = "select o from Org as o, Combo as c where " + direction + " and c.type.value = :comboType " + defaultOrder
        }

        if (params.hashCode() != hashCode) {
            log.debug 'GrailsParameterMap was modified @ getOrgComboQuery()'
        }

        new Result( hql, queryParams, isFilterSet )
    }

    /**
     * Processes the task filter parameters and gets the task query with the prepared filter values
     * @param params the filter query parameter map
     * @param sdFormat the date format to use to parse dates
     * @return the result containing the query and the prepared query parameters
     */
    Result getTaskQuery(GrailsParameterMap params, DateFormat sdFormat) {
        int hashCode = params.hashCode()

        ArrayList<String> query = []
        Map<String, Object> queryParams = [:]

        if (params.taskName) {
            query << "genfunc_filter_matcher(t.title, :taskName) = true "
            queryParams << [taskName : "${params.taskName}"]
        }
        if (params.taskStatus) {
            if (params.taskStatus == 'not done') {
                query << "t.status.id != :rdvDone"
                queryParams << [rdvDone : RDStore.TASK_STATUS_DONE.id]
            }
            else {
                query << "t.status.id = :statusId"
                queryParams << [statusId : params.long('taskStatus')]
            }
        }
        if (params.taskObject) {
             if (params.taskObject in ['license', 'org', 'provider', 'subscription', 'surveyConfig', 'tipp', 'vendor']) {
                 query << ('t.' + params.taskObject + ' != null' as String)
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

        String hql = ( query.size() > 0 ? " and " + query.join(" and ") : "" ) + defaultOrder

        if (params.hashCode() != hashCode) {
            log.debug 'GrailsParameterMap was modified @ getTaskQuery()'
        }

        new Result( hql, queryParams )
    }

    /**
     * Kept as reference.
     * Processes the given filter parameters and generates a query to retrieve documents
     * @deprecated should be moved to the external document service
     * @param params the filter parameter map
     * @return the map containing the query and the prepared query parameters
     */
    @Deprecated
    Map<String,Object> getDocumentQuery(GrailsParameterMap params) {
        int hashCode = params.hashCode()

        Map<String, Object> result = [:]
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

        if (params.hashCode() != hashCode) {
            log.debug 'GrailsParameterMap was modified @ getDocumentQuery()'
        }
        result
    }

    /**
     * Processes the given filter parameters and generates a survey query
     * @param params the filter parameter map
     * @param sdFormat the format to use for date parsing
     * @param contextOrg the context institution whose perspective should be taken
     * @return the map containing the query and the prepared query parameters
     */
    Result getSurveyConfigQueryConsortia(GrailsParameterMap params, DateFormat sdFormat, Org contextOrg) {
        int hashCode = params.hashCode()
        boolean isFilterSet = false

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
            isFilterSet = true
        }

        if (params.validOnYear) {
                if('all' in params.list('validOnYear')) {
                    isFilterSet = true
                    params.validOnYear = ['all']
                }else{
                    query += " and Year(surInfo.startDate) in (:validOnYear) "
                    queryParams << [validOnYear : Params.getLongList(params, 'validOnYear').collect{ Integer.valueOf(it.toString()) }]
                    isFilterSet = true
                }
        }

        if(params.name) {
            query += " and (genfunc_filter_matcher(surInfo.name, :name) = true or exists ( select surC from SurveyConfig as surC where surC.surveyInfo = surC and (genfunc_filter_matcher(surC.subscription.name, :name) = true))) "
            queryParams << [name:"${params.name}"]
            isFilterSet = true
        }

        if(params.status) {
            query += " and surInfo.status = :status"
            queryParams << [status: RefdataValue.get(params.status)]
            isFilterSet = true
        }
        if(params.type) {
            query += " and surInfo.type = :type"
            queryParams << [type: RefdataValue.get(params.type)]
            isFilterSet = true
        }
        if (params.startDate && sdFormat) {
            query += " and surInfo.startDate >= :startDate"
            queryParams << [startDate : sdFormat.parse(params.startDate)]
            isFilterSet = true
        }
        if (params.endDate && sdFormat) {
            query += " and surInfo.endDate <= :endDate"
            queryParams << [endDate : sdFormat.parse(params.endDate)]
            isFilterSet = true
        }

        if (params.mandatory || params.noMandatory) {

            if (params.mandatory && !params.noMandatory) {
                query += " and surInfo.isMandatory = :mandatory"
                queryParams << [mandatory: true]
            }else if (!params.mandatory && params.noMandatory){
                query += " and surInfo.isMandatory = :mandatory"
                queryParams << [mandatory: false]
            }
            isFilterSet = true
        }

        if(params.ids) {
            query += " and surInfo.id in (:ids)"
            queryParams << [ids: Params.getLongList(params, 'ids')]
            isFilterSet = true
        }

        if(params.checkSubSurveyUseForTransfer) {
            query += " and surConfig.subSurveyUseForTransfer = :checkSubSurveyUseForTransfer"
            queryParams << [checkSubSurveyUseForTransfer: true]
            isFilterSet = true
        }

        if(params.checkPackageSurvey) {
            query += " and surConfig.packageSurvey = :checkPackageSurvey"
            queryParams << [checkPackageSurvey: true]
            isFilterSet = true
        }

        if(params.checkVendorSurvey) {
            query += " and surConfig.vendorSurvey = :checkVendorSurvey"
            queryParams << [checkVendorSurvey: true]
            isFilterSet = true
        }

        if(params.checkInvoicingInformation) {
            query += " and surConfig.invoicingInformation = :checkInvoicingInformation"
            queryParams << [checkInvoicingInformation: true]
            isFilterSet = true
        }

        if (params.provider) {
            query += " and exists (select orgRole from OrgRole orgRole where orgRole.sub = surConfig.subscription and orgRole.org = :provider)"
            queryParams << [provider : Org.get(params.provider)]
            isFilterSet = true
        }

        if (params.list('filterSub')) {
            query += " and surConfig.subscription.name in (:subs) "
            queryParams << [subs : params.list('filterSub')]
            isFilterSet = true
        }

        if (params.filterStatus && params.filterStatus != "" && params.list('filterStatus')) {
            query += " and surInfo.status.id in (:filterStatus) "
            queryParams << [filterStatus : Params.getLongList(params, 'filterStatus')]
            isFilterSet = true
        }

        if (params.filterPvd) {
            query += " and exists (select pvr from ProviderRole pvr where pvr.subscription = surConfig.subscription and pvr.provider.id in (:filterPvd))"
            queryParams << [filterPvd : Params.getLongList(params, 'filterPvd')]
            isFilterSet = true
        }

        if (params.participant) {
            query += " and exists (select surResult from SurveyResult as surResult where surResult.surveyConfig = surConfig and participant = :participant)"
            queryParams << [participant : params.participant]
        }

        if (params.filterPropDef?.size() > 0) {
            def psq = propertyService.evalFilterQuery(params, query, 'surConfig', queryParams)
            query = psq.query
            queryParams = psq.queryParams
            isFilterSet = true
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

        if (params.hashCode() != hashCode) {
            log.debug 'GrailsParameterMap was modified @ getSurveyConfigQueryConsortia()'
        }

        new Result( query, queryParams, isFilterSet )
    }

    /**
     * Processes the given filter parameters and generates a query to retrieve surveys from the participant's point of view
     * @param params the filter parameter map
     * @param sdFormat the format to use to parse dates
     * @param org the context institution
     * @return the map containing the query and the prepared query parameters
     */
    Result getParticipantSurveyQuery_New(GrailsParameterMap params, DateFormat sdFormat, Org org) {
        int hashCode = params.hashCode()
        boolean isFilterSet = false

        Map<String, Object> result = [:]
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
            isFilterSet = true
        }

        if (params.validOnYear) {
                if('all' in params.list('validOnYear')) {
                    isFilterSet = true
                    params.validOnYear = ['all']
                }else{
                    query += " Year(surInfo.startDate) in (:validOnYear) "
                    queryParams << [validOnYear : Params.getLongList(params, 'validOnYear').collect{ Integer.valueOf(it.toString()) }]
                    isFilterSet = true
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
            isFilterSet = true
        }

        if(params.checkSubSurveyUseForTransfer) {
            query << "surConfig.subSurveyUseForTransfer = :checkSubSurveyUseForTransfer"
            queryParams << [checkSubSurveyUseForTransfer: true]
            isFilterSet = true
        }

        if(params.checkPackageSurvey) {
            query += "surConfig.packageSurvey = :checkPackageSurvey"
            queryParams << [checkPackageSurvey: true]
            isFilterSet = true
        }

        if(params.checkVendorSurvey) {
            query += "surConfig.vendorSurvey = :checkVendorSurvey"
            queryParams << [checkVendorSurvey: true]
            isFilterSet = true
        }

        if(params.checkInvoicingInformation) {
            query += "surConfig.invoicingInformation = :checkInvoicingInformation"
            queryParams << [checkInvoicingInformation: true]
            isFilterSet = true
        }


        if (params.list('filterSub')) {
            query << " surConfig.subscription.name in (:subs) "
            queryParams << [subs : params.list('filterSub')]
            isFilterSet = true
        }

        if (params.filterPvd) {
            query << "exists (select pvr from ProviderRole pvr where pvr.subscription = surConfig.subscription and pvr.provider.id in (:filterPvd))"
            queryParams << [filterPvd: Params.getLongList(params, 'filterPvd')]
            isFilterSet = true
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

        if(params.tab == "open"){
            query << "surOrg.org = :org and surOrg.finishDate is null and surInfo.status = :status"
            queryParams << [status: RDStore.SURVEY_SURVEY_STARTED]
            queryParams << [org : org]
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

        if(params.tab == "all"){
            query << "surInfo.status in (:status) and surOrg.org = :org"
            queryParams << [status: [RDStore.SURVEY_SURVEY_STARTED, RDStore.SURVEY_SURVEY_COMPLETED, RDStore.SURVEY_IN_EVALUATION, RDStore.SURVEY_COMPLETED]]
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
            isFilterSet = true
        }

        if (params.hashCode() != hashCode) {
            log.debug 'GrailsParameterMap was modified @ getParticipantSurveyQuery_New()'
        }

        new Result(result.query as String, queryParams, isFilterSet )
    }

    /**
     * Processes the given filter parameters and generates a query to retrieve the given survey's participants
     * @param params the filter parameter map
     * @param surveyConfig the survey whose participants should be retrieved
     * @return the map containing the query and the prepared query parameters
     */
    Map<String,Object> getSurveyOrgQuery(GrailsParameterMap params, SurveyConfig surveyConfig) {
        int hashCode = params.hashCode()

        Map<String, Object> result = [:]
        String base_qry = "from SurveyOrg as surveyOrg where surveyOrg.surveyConfig = :surveyConfig "
        Map<String,Object> queryParams = [surveyConfig: surveyConfig]

        if (params.orgNameContains?.length() > 0) {
            base_qry += " and (genfunc_filter_matcher(surveyOrg.org.name, :orgNameContains1) = true or genfunc_filter_matcher(surveyOrg.org.sortname, :orgNameContains2) = true) "
            queryParams << [orgNameContains1 : "${params.orgNameContains}"]
            queryParams << [orgNameContains2 : "${params.orgNameContains}"]
        }
        if (params.region) {
            base_qry += " and surveyOrg.org.region.id in (:region)"
            queryParams << [region : Params.getLongList(params, 'region')]
        }
        if (params.country) {
            base_qry += " and surveyOrg.org.country.id in (:country)"
            queryParams << [country : Params.getLongList(params, 'country')]
        }
        if (params.subjectGroup) {
            base_qry +=  " and exists (select osg from OrgSubjectGroup as osg where osg.org.id = surveyOrg.org.id and osg.subjectGroup.id in (:subjectGroup))"
            queryParams << [subjectGroup : Params.getLongList(params, 'subjectGroup')]
        }

        if (params.libraryNetwork) {
            base_qry += " and surveyOrg.org.libraryNetwork.id in (:libraryNetwork)"
            queryParams << [libraryNetwork : Params.getLongList(params, 'libraryNetwork')]
        }
        if (params.libraryType) {
            base_qry += " and surveyOrg.org.libraryType.id in (:libraryType)"
            queryParams << [libraryType : Params.getLongList(params, 'libraryType')]
        }

        if (params.customerType?.length() > 0) {
            base_qry += " and exists (select oss from OrgSetting as oss where oss.id = surveyOrg.org.id and oss.key = :customerTypeKey and oss.roleValue.id = :customerType)"
            queryParams << [customerType : params.long('customerType')]
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

        if (params.discoverySystemsFrontend) {
            base_qry += "and exists (select dsf from DiscoverySystemFrontend as dsf where dsf.org.id = surveyOrg.org.id and dsf.frontend.id in (:frontends))"
            queryParams << [frontends : Params.getLongList(params, 'discoverySystemsFrontend')]
        }
        if (params.discoverySystemsIndex) {
            base_qry += "and exists (select dsi from DiscoverySystemIndex as dsi where dsi.org.id = surveyOrg.org.id and dsi.index.id in (:indices))"
            queryParams << [indices : Params.getLongList(params, 'discoverySystemsIndex')]
        }

        if (params.filterPropDef) {
                PropertyDefinition pd = (PropertyDefinition) genericOIDService.resolveOID(params.filterPropDef)

                if (pd.descr == PropertyDefinition.ORG_PROP) {
                    def psq = propertyService.evalFilterQuery(params, '', 'surveyOrg.org', queryParams)
                    base_qry += ' and ' + psq.query.split(' and', 2)[1]
                    queryParams << psq.queryParams
                }else {
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
                        } else if (pd.isLongType()) {
                            if (!params.filterProp || params.filterProp.length() < 1) {
                                base_qry += " and surResult.longValue = null "
                            } else {
                                base_qry += " and surResult.longValue = :prop "
                                queryParams.put('prop', AbstractPropertyWithCalculatedLastUpdated.parseValue(params.filterProp, pd.type))
                            }
                        } else if (pd.isStringType()) {
                            if (!params.filterProp || params.filterProp.length() < 1) {
                                base_qry += " and surResult.stringValue = null "
                            } else {
                                base_qry += " and lower(surResult.stringValue) like lower(:prop) "
                                queryParams.put('prop', "%${AbstractPropertyWithCalculatedLastUpdated.parseValue(params.filterProp, pd.type)}%")
                            }
                        } else if (pd.isBigDecimalType()) {
                            if (!params.filterProp || params.filterProp.length() < 1) {
                                base_qry += " and surResult.decValue = null "
                            } else {
                                base_qry += " and surResult.decValue = :prop "
                                queryParams.put('prop', AbstractPropertyWithCalculatedLastUpdated.parseValue(params.filterProp, pd.type))
                            }
                        } else if (pd.isDateType()) {
                            if (!params.filterProp || params.filterProp.length() < 1) {
                                base_qry += " and surResult.dateValue = null "
                            } else {
                                base_qry += " and surResult.dateValue = :prop "
                                queryParams.put('prop', AbstractPropertyWithCalculatedLastUpdated.parseValue(params.filterProp, pd.type))
                            }
                        } else if (pd.isURLType()) {
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
        }

        if(params.chartFilter){
            List<PropertyDefinition> propList = SurveyConfigProperties.executeQuery("select scp.surveyProperty from SurveyConfigProperties scp where scp.surveyConfig = :surveyConfig", [surveyConfig: surveyConfig])
            propList.eachWithIndex {PropertyDefinition prop, int i ->
                if (prop.isRefdataValueType()) {
                    def refDatas = SurveyResult.executeQuery("select sr.refValue.id from SurveyResult sr where sr.surveyConfig = :surveyConfig and sr.refValue is not null and sr.type = :propType group by sr.refValue.id", [propType: prop, surveyConfig: surveyConfig])
                    refDatas.each {
                        if(params.chartFilter == "${prop.getI10n('name')}: ${RefdataValue.get(it).getI10n('value')}") {
                            base_qry += ' and exists (select surResult from SurveyResult as surResult where surResult.surveyConfig = surveyOrg.surveyConfig and surResult.participant = surveyOrg.org and surResult.type = :propDef and surResult.refValue = :refValue) '
                            queryParams.put('propDef', prop)
                            queryParams.put('refValue', RefdataValue.get(it))
                        }
                    }
                }
                if (prop.isLongType()) {
                    if(params.chartFilter == prop.getI10n('name')){
                        base_qry += " and exists (select surResult from SurveyResult as surResult where surResult.surveyConfig = surveyOrg.surveyConfig and surResult.participant = surveyOrg.org and surResult.type = :propDef and (surResult.longValue is not null)) "
                        queryParams.put('propDef', prop)
                    }
                }
                else if (prop.isStringType()) {
                    if(params.chartFilter == prop.getI10n('name')){
                        base_qry +=  " and exists (select surResult from SurveyResult as surResult where surResult.surveyConfig = surveyOrg.surveyConfig and surResult.participant = surveyOrg.org and surResult.type = :propDef and (surResult.stringValue is not null or surResult.stringValue != '')) "
                        queryParams.put('propDef', prop)
                    }
                  }
                else if (prop.isBigDecimalType()) {
                    if(params.chartFilter == prop.getI10n('name')){
                        base_qry += " and exists (select surResult from SurveyResult as surResult where surResult.surveyConfig = surveyOrg.surveyConfig and surResult.participant = surveyOrg.org and surResult.type = :propDef and (surResult.decValue is not null)) "
                        queryParams.put('propDef', prop)
                    }
                }
                else if (prop.isDateType()) {
                    if(params.chartFilter == prop.getI10n('name')){
                        base_qry += " and exists (select surResult from SurveyResult as surResult where surResult.surveyConfig = surveyOrg.surveyConfig and surResult.participant = surveyOrg.org and surResult.type = :propDef and (surResult.dateValue is not null)) "
                        queryParams.put('propDef', prop)
                    }
                }
                else if (prop.isURLType()) {
                    if(params.chartFilter == prop.getI10n('name')){
                        base_qry += " and exists (select surResult from SurveyResult as surResult where surResult.surveyConfig = surveyOrg.surveyConfig and surResult.participant = surveyOrg.org and surResult.type = :propDef and (surResult.urlValue is not null or surResult.urlValue != '')) "
                        queryParams.put('propDef', prop)
                    }
                }
            }

            if(surveyConfig.vendorSurvey){
                List<Vendor> vendors = SurveyConfigVendor.executeQuery("select scv.vendor from SurveyConfigVendor scv where scv.surveyConfig = :surveyConfig order by scv.vendor.name asc", [surveyConfig: surveyConfig])

                vendors.each {Vendor vendor ->
                    if(params.chartFilter == vendor.name.replace('"', '')){
                        base_qry += " and exists (select svr from SurveyVendorResult as svr where svr.surveyConfig = surveyOrg.surveyConfig and svr.participant = surveyOrg.org and svr.vendor = :vendor) "
                        queryParams.put('vendor', vendor)
                    }
                }
            }

            if(surveyConfig.packageSurvey){
                List<Package> packages = SurveyConfigPackage.executeQuery("select scp.pkg from SurveyConfigPackage scp where scp.surveyConfig = :surveyConfig order by scp.pkg.name asc", [surveyConfig: surveyConfig])

                packages.each {Package pkg ->
                    if(params.chartFilter == pkg.name.replace('"', '')){
                        base_qry += " and exists (select spr from SurveyPackageResult as spr where spr.surveyConfig = surveyOrg.surveyConfig and spr.participant = surveyOrg.org and spr.pkg = :pkg) "
                        queryParams.put('pkg', pkg)
                    }
                }
            }
        }

        if (params.filterPropDefAllMultiYear) {
            base_qry += ' and exists (select surResult from SurveyResult as surResult where surResult.surveyConfig = surveyOrg.surveyConfig and participant = surveyOrg.org and surResult.type in (:propDef) and surResult.refValue = :refValue) '
            queryParams.put('propDef', [PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_2, PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_3, PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_4, PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_5])
            queryParams.put('refValue', RDStore.YN_YES)
        }


        if(params.sub && (params.hasSubscription &&  !params.hasNotSubscription) || (!params.hasSubscription && params.hasNotSubscription)) {
            String subQuery = " and "

            if(params.hasNotSubscription) {
                subQuery = subQuery + " not"
            }

            subQuery = subQuery + " exists (select oo.id from OrgRole oo join oo.sub sub join sub.orgRelations ooCons where oo.org.id = surveyOrg.org.id and oo.roleType in (:subscrRoles) and ooCons.org = :context and ooCons.roleType = :consType and sub.instanceOf = :sub)"
            queryParams << [subscrRoles: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN], consType: RDStore.OR_SUBSCRIPTION_CONSORTIUM, context: contextService.getOrg(), sub: params.sub]
            base_qry += subQuery
        }

        if (params.sub && (params.subRunTimeMultiYear || params.subRunTime)) {
            String subQuery = " and "

            subQuery = subQuery + " exists (select oo.id from OrgRole oo join oo.sub sub join sub.orgRelations ooCons where oo.org.id = surveyOrg.org.id and oo.roleType in (:subscrRoles) and ooCons.org = :context and ooCons.roleType = :consType and sub.instanceOf = :sub "
            queryParams << [subscrRoles: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN], consType: RDStore.OR_SUBSCRIPTION_CONSORTIUM, context: contextService.getOrg(), sub: params.sub]
            if (params.subRunTimeMultiYear && !params.subRunTime) {
                subQuery += " and sub.isMultiYear = :subRunTimeMultiYear "
                queryParams.put('subRunTimeMultiYear', true)

            }else if (!params.subRunTimeMultiYear && params.subRunTime){
                subQuery += " and sub.isMultiYear = :subRunTimeMultiYear "
                queryParams.put('subRunTimeMultiYear', false)
            }
            base_qry += subQuery + ' )'
        }

        if (params.sub && !((params.hasSubscription &&  !params.hasNotSubscription) || (!params.hasSubscription && params.hasNotSubscription) || (params.subRunTimeMultiYear || params.subRunTime))) {
            String subQuery = " and "

            subQuery = subQuery + " exists (select oo.id from OrgRole oo join oo.sub sub join sub.orgRelations ooCons where oo.org.id = surveyOrg.org.id and oo.roleType in (:subscrRoles) and ooCons.org = :context and ooCons.roleType = :consType and sub.instanceOf = :sub)"

            queryParams << [subscrRoles: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN], consType: RDStore.OR_SUBSCRIPTION_CONSORTIUM, context: contextService.getOrg(), sub: params.sub]
            base_qry += subQuery
        }


        if (params.surveyVendors) {
            base_qry += ' and exists (select svr from SurveyVendorResult as svr where svr.surveyConfig = surveyOrg.surveyConfig and svr.participant = surveyOrg.org and svr.vendor.id in (:vendors)) '
            queryParams << [vendors : Params.getLongList(params, 'surveyVendors')]
        }

        if (params.surveyPackages) {
            base_qry += ' and exists (select spr from SurveyPackageResult as spr where spr.surveyConfig = surveyOrg.surveyConfig and spr.participant = surveyOrg.org and spr.pkg.id in (:pkgs))'
            queryParams << [pkgs : Params.getLongList(params, 'surveyPackages')]
        }


        if ((params.sort != null) && (params.sort.length() > 0)) {
                base_qry += " order by ${params.sort} ${params.order ?: "asc"}"
        } else {
            base_qry += " order by surveyOrg.org.sortname "
        }

        result.query = base_qry
        result.queryParams = queryParams

        if (params.hashCode() != hashCode) {
            log.debug 'GrailsParameterMap was modified @ getSurveyOrgQuery()'
        }
        result
    }

    /**
     * Substitution call for {@link #getIssueEntitlementQuery(grails.web.servlet.mvc.GrailsParameterMap, de.laser.Subscription)} for a single subscription
     * @param params the filter parameter map
     * @param subscription the subscriptions whose dates should be considered
     * @return the map containing the query and the prepared query parameters
     */
    Map<String, Object> getIssueEntitlementQuery(GrailsParameterMap params, Subscription subscription) {
        getIssueEntitlementQuery(params, [subscription])
    }

    /**
     * Processes the given filter parameters and generates a query to retrieve issue entitlements
     * @param params the filter parameter map
     * @param subscriptions the subscriptions whose dates should be considered
     * @return the map containing the query and the prepared query parameters
     */
    Map<String,Object> getIssueEntitlementQuery(GrailsParameterMap params, Collection<Subscription> subscriptions) {
        log.debug 'getIssueEntitlementQuery'

        int hashCode = params.hashCode()

        Map<String, Object> result = [:]
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()

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
        }

        if (params.status == RDStore.TIPP_STATUS_REMOVED.id.toString()) {
            base_qry += " and ie.tipp.status.id = :status and ie.status.id != :status "
            qry_params.status = params.long('status')
        }
        else if (params.status) {
            base_qry += " and ie.status.id in (:status) "
            qry_params.status = Params.getLongList(params, 'status')
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

        if (params.pkgfilter) {
            base_qry += " and tipp.pkg.id = :pkgId "
            qry_params.pkgId = params.long('pkgfilter')
            filterSet = true
        }
        if (params.titleGroup && !params.forCount) {
            if(params.titleGroup == 'notInGroups'){
                base_qry += " and not exists ( select iegi from IssueEntitlementGroupItem as iegi where iegi.ie = ie) "
            }else {
                base_qry += " and exists ( select iegi from IssueEntitlementGroupItem as iegi where iegi.ieGroup.id = :titleGroup and iegi.ie = ie) "
                qry_params.titleGroup = params.long('titleGroup')
            }
        }

        if (params.inTitleGroups && (params.inTitleGroups != '') && !params.forCount) {
            if(params.inTitleGroups == RDStore.YN_YES.id.toString()) {
                base_qry += " and exists ( select iegi from IssueEntitlementGroupItem as iegi where iegi.ie = ie) "
            }else{
                base_qry += " and not exists ( select iegi from IssueEntitlementGroupItem as iegi where iegi.ie = ie) "
            }
        }

        if (params.ddcs) {
            base_qry += " and exists ( select ddc.id from DeweyDecimalClassification ddc where ddc.tipp = tipp and ddc.ddc.id in (:ddcs) ) "
            qry_params.ddcs = Params.getLongList_forCommaSeparatedString(params, 'ddcs') // ?
            filterSet = true
        }

        if (params.languages) {
            base_qry += " and exists ( select lang.id from Language lang where lang.tipp = tipp and lang.language.id in (:languages) ) "
            qry_params.languages = Params.getLongList_forCommaSeparatedString(params, 'languages')  // ?
            filterSet = true
        }

        if (params.subject_references && params.subject_references != "" && listReaderWrapper(params, 'subject_references')) {
            Set<String> subjectQuery = []
            params.list('subject_references').each { String subReference ->
                //subjectQuery << "genfunc_filter_matcher(tipp.subjectReference, '${subReference.toLowerCase()}') = true"
                subjectQuery << "tipp.subjectReference = '${subReference}'"
            }
            base_qry += " and (${subjectQuery.join(" or ")}) "
            filterSet = true
        }

        if (params.series_names && params.series_names != "" && listReaderWrapper(params, 'series_names')) {
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
            qry_params.yearsFirstOnline = Params.getLongList_forCommaSeparatedString(params, 'yearsFirstOnline').collect { Integer.valueOf(it.toString()) }
        }

        if (params.identifier) {
            base_qry += "and ( exists ( from Identifier ident where ident.tipp.id = tipp.id and ident.value like :identifier ) ) "
            qry_params.identifier = "${params.identifier}"
            filterSet = true
        }

        if (params.publishers) {
            //(exists (select orgRole from OrgRole orgRole where orgRole.tipp = ie.tipp and orgRole.roleType.id = ${RDStore.OR_PUBLISHER.id} and orgRole.org.name in (:publishers)) )
            base_qry += "and lower(tipp.publisherName) in (:publishers) "
            qry_params.publishers = listReaderWrapper(params, 'publishers').collect { it.toLowerCase() }
            filterSet = true
        }

        if (params.title_types && params.title_types != "" && listReaderWrapper(params, 'title_types')) {
            base_qry += " and lower(tipp.titleType) in (:title_types)"
            qry_params.title_types = listReaderWrapper(params, 'title_types').collect { ""+it.toLowerCase()+"" }
            filterSet = true
        }

        if (params.medium) {
            base_qry += " and tipp.medium.id in (:medium) "
            qry_params.medium = Params.getLongList_forCommaSeparatedString(params, 'medium')  // ?
            filterSet = true
        }

        if (params.hasPerpetualAccess && !params.hasPerpetualAccessBySubs) {
            //may become a performance bottleneck; keep under observation!
            String permanentTitleQuery = "select pt from PermanentTitle pt where pt.tipp = ie.tipp and pt.owner in (:subscribers)"
            qry_params.subscribers = subscriptions.collect { Subscription s -> s.getSubscriberRespConsortia() }
            if (params.long('hasPerpetualAccess') == RDStore.YN_YES.id) {
                base_qry += "and exists(${permanentTitleQuery}) "
            }else{
                base_qry += "and not exists(${permanentTitleQuery}) "
            }
            filterSet = true
        }

        if (params.hasPerpetualAccess && params.hasPerpetualAccessBySubs) {
            if (params.long('hasPerpetualAccess') == RDStore.YN_NO.id) {
                base_qry += "and ie.tipp.hostPlatformURL not in (select ie2.tipp.hostPlatformURL from IssueEntitlement as ie2 where ie2.perpetualAccessBySub in (:subs)) "
                qry_params.subs = listReaderWrapper(params, 'hasPerpetualAccessBySubs')
            }else {
                base_qry += "and ie.tipp.hostPlatformURL in (select ie2.tipp.hostPlatformURL from IssueEntitlement as ie2 where ie2.perpetualAccessBySub in (:subs)) "
                qry_params.subs = listReaderWrapper(params, 'hasPerpetualAccessBySubs')
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

        if (params.hashCode() != hashCode) {
            log.debug 'GrailsParameterMap was modified @ getIssueEntitlementQuery()'
        }
        result
    }

    Map<String,Object> getIssueEntitlementSubsetQuery(Map params) {
        log.debug 'getIssueEntitlementSubsetQuery'
        Map<String, Object> result = [:], clauses = getIssueEntitlementSubsetArguments(params)
        String query = "select new map(ie.id as ieID, tipp.id as tippID) from IssueEntitlement ie join ie.tipp tipp "

        result.query = query + "where ${clauses.arguments} order by ${params.sort} ${params.order}"
        result.queryParams = clauses.queryParams

        result
    }

    Map<String, Object> getIssueEntitlementSubsetArguments(Map params) {
        Map<String, Object> result = [:], queryParams = [:]
        Set<String> queryArgs = []

        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()

        if (params.subscriptions) {
            queryArgs << 'ie.subscriptions in (:subscriptions)'
            queryParams.subscriptions = params.subscriptions
        }
        else {
            queryArgs << 'ie.subscription = :subscription'
            queryParams.subscription = params.subscription
        }

        if (params.asAt && params.asAt.length() > 0) {
            queryArgs << 'ie.accessStartDate >= :asAt'
            queryArgs << 'ie.accessEndDate <= :asAt'
            queryParams.asAt = sdf.parse(params.asAt)
        }

        if(params.get('ieStatus') instanceof List) {
            queryArgs << 'ie.status in (:ieStatus)'
            queryParams.ieStatus = Params.getRefdataList(params, 'ieStatus')
        }
        else if (params.containsKey('ieStatus')) {
            queryArgs << 'ie.status = :ieStatus'
            queryParams.ieStatus = RefdataValue.get(params.ieStatus)
        }
        else {
            queryArgs << 'ie.status != :ieStatus'
            queryParams.ieStatus = RDStore.TIPP_STATUS_REMOVED
        }

        if (params.titleGroup) {
            if(params.titleGroup == 'notInGroups'){
                queryArgs << "not exists ( select iegi from IssueEntitlementGroupItem as iegi where iegi.ie = ie)"
            }else {
                queryArgs << "exists ( select iegi from IssueEntitlementGroupItem as iegi where iegi.ieGroup = :titleGroup and iegi.ie = ie)"
                queryParams.titleGroup = params.titleGroup
            }
        }
        String arguments = queryArgs.join(' and ')
        result.queryParams = queryParams
        result.arguments = arguments
        result
    }

    Map<String, Object> getIssueEntitlementSubsetSQLQuery(Map params) {
        log.debug 'getIssueEntitlementSubsetSQLQuery'
        String query = "select ie_id, tipp_id from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id where tipp_id = any(:tippIDs) "
        Map<String, Object> result = [:], clauses = getIssueEntitlementSubsetSQLArguments(params)
        result.query = query + "and ${clauses.arguments} order by tipp_sort_name"
        result.queryParams = clauses.queryParams
        result.arrayParams = clauses.arrayParams

        result
    }

    Map<String, Object> getIssueEntitlementSubsetSQLArguments(Map params) {
        Map<String, Object> result = [:], queryParams = [:], arrayParams = [:]
        Set<String> queryArgs = []

        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()

        if (params.tippIDs)
            arrayParams.tippIDs = params.tippIDs

        if (params.subscriptions) {
            queryArgs << 'ie_subscription_fk = any(:subscriptions)'
            arrayParams.subscriptions = params.subscriptions.id
        }
        else {
            queryArgs << 'ie_subscription_fk = :subscription'
            queryParams.subscription = params.subscription.id
        }

        if (params.asAt && params.asAt.length() > 0) {
            queryArgs << 'ie_access_start_date >= :asAt'
            queryArgs << 'ie_access_end_date <= :asAt'
            queryParams.asAt = sdf.parse(params.asAt)
        }

        if (params.ieStatus) {
            queryArgs << 'ie_status_rv_fk = any(:ieStatus)'
            arrayParams.ieStatus = Params.getLongList(params, 'ieStatus')
        }

        if (params.titleGroup) {
            if(params.titleGroup == 'notInGroups'){
                queryArgs << "not exists ( select igi_id from issue_entitlement_group_item where igi_ie_fk = ie_id)"
            }else {
                queryArgs << "exists ( select igi_id from issue_entitlement_group_item where igi_ie_group_fk = :titleGroup and igi_ie_fk = ie_id)"
                queryParams.titleGroup = params.titleGroup.id
            }
        }

        String arguments = queryArgs.join(' and ')
        result.arguments = arguments
        result.queryParams = queryParams
        result.arrayParams = arrayParams
        result
    }

    /**
     * Processes the given filter parameters and generates a query to retrieve permanent titles
     * @param params the filter parameter map
     * @param owner the org whose be the owner of permanent titles
     * @return the map containing the query and the prepared query parameters
     */
    Map<String,Object> getPermanentTitlesQuery(GrailsParameterMap params, Org owner) {
        log.debug 'getPermanentTitlesQuery'

        int hashCode = params.hashCode()

        Map<String, Object> result = [:]
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()

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
        else if (params.status) {
            base_qry += " and ie.status.id in (:status) "
            qry_params.status = Params.getLongList(params, 'status')
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
            qry_params.pkgId = params.long('pkgfilter')
            filterSet = true
        }

        if (params.ddcs) {
            base_qry += " and exists ( select ddc.id from DeweyDecimalClassification ddc where ddc.tipp = tipp and ddc.ddc.id in (:ddcs) ) "
            qry_params.ddcs = Params.getLongList_forCommaSeparatedString(params, 'ddcs') // ?
            filterSet = true
        }

        if (params.languages) {
            base_qry += " and exists ( select lang.id from Language lang where lang.tipp = tipp and lang.language.id in (:languages) ) "
            qry_params.languages = Params.getLongList_forCommaSeparatedString(params, 'languages')  // ?
            filterSet = true
        }

        if (params.subject_references && params.subject_references != "" && params.list('subject_references')) {
            Set<String> subjectQuery = []
            params.list('subject_references').each { String subReference ->
                //subjectQuery << "genfunc_filter_matcher(tipp.subjectReference, '${subReference.toLowerCase()}') = true"
                subjectQuery << "tipp.subjectReference = '${subReference}'"
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

        // todo: ERMS-5517 > multiple values @ currentPermanentTitles
        if(params.yearsFirstOnline) {
            base_qry += " and (Year(tipp.dateFirstOnline) in (:yearsFirstOnline)) "
            qry_params.yearsFirstOnline = Params.getLongList_forCommaSeparatedString(params, 'yearsFirstOnline').collect { Integer.valueOf(it.toString()) }
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

        if (params.medium) {
            base_qry += " and tipp.medium.id in (:medium) "
            qry_params.medium = Params.getLongList_forCommaSeparatedString(params, 'medium')  // ?
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

        if (params.hashCode() != hashCode) {
            log.debug 'GrailsParameterMap was modified @ getPermanentTitlesQuery()'
        }
        result
    }

    /**
     * Processes the given filter parameters and generates a query to retrieve titles of the given packages
     * @param params the filter parameter map
     * @param pkgs the packages whose titles should be queried
     * @return the map containing the query and the prepared query parameters
     */
    Map<String,Object> getTippQuery(Map params, List<Package> pkgs) {
        log.debug 'getTippQuery'

        Map<String, Object> result = [:]
        List<String> qry_parts = []
        Map<String,Object> qry_params = [:]

        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()

        if(pkgs){
            qry_parts << " tipp.pkg in (:pkgs) "
            qry_params.pkgs = pkgs
        }

        boolean filterSet = false
        Date date_filter
        if (params.asAt && params.asAt.length() > 0) {
            date_filter = sdf.parse(params.asAt)
            result.as_at_date = date_filter
            filterSet = true
        }

        if (params.filter) {
           /* if (date_filter) {
                base_qry += "and ( ( :startDate >= tipp.accessStartDate or tipp.accessStartDate is null ) and ( :endDate <= tipp.accessEndDate or tipp.accessEndDate is null) ) "
                qry_params.startDate = date_filter
                qry_params.endDate = date_filter
            }*/
            qry_parts << " ( lower(tipp.name) like lower(:nameFilter) or lower(tipp.firstAuthor) like lower(:nameFilter) or lower(tipp.firstEditor) like lower(:nameFilter) or ( exists ( from Identifier ident where ident.tipp.id = tipp.id and ident.value like lower(:nameFilter) ) ) ) "
            qry_params.nameFilter = "%${params.filter.trim()}%"
            filterSet = true
        }

        if (date_filter) {
            qry_parts << " ( ( :startDate >= tipp.accessStartDate or tipp.accessStartDate is null ) and ( :endDate <= tipp.accessEndDate or tipp.accessEndDate is null) ) "
            qry_params.startDate = new Timestamp(date_filter.getTime())
            qry_params.endDate = new Timestamp(date_filter.getTime())
        }

        if(params.addEntitlements && params.subscription && params.issueEntitlementStatus) {
            String q = " tipp.pkg in ( select pkg from SubscriptionPackage sp where sp.subscription = :subscription ) and " +
                    "( not exists ( select ie from IssueEntitlement ie where ie.subscription = :subscription and ie.tipp.id = tipp.id and ie.status = :issueEntitlementStatus ) )"
            qry_parts << q
            qry_params.subscription = params.subscription instanceof Subscription ? params.subscription : Subscription.get(params.subscription)
            qry_params.issueEntitlementStatus = params.issueEntitlementStatus
        }

        if (params.status) {
            qry_parts << " tipp.status.id in (:status) "
            qry_params.status = Params.getLongList(params, 'status')

        } else if (params.notStatus != '' && params.notStatus != null){
            qry_parts << " tipp.status.id != :notStatus "
            qry_params.notStatus = params.notStatus
        }
        else {
            qry_parts << " tipp.status = :current "
            qry_params.current = RDStore.TIPP_STATUS_CURRENT
        }

        if (params.ddcs) {
            qry_parts << " exists ( select ddc.id from DeweyDecimalClassification ddc where ddc.tipp = tipp and ddc.ddc.id in (:ddcs) ) "
            qry_params.ddcs = Params.getLongList_forCommaSeparatedString(params, 'ddcs') // ?
            filterSet = true
        }

        if (params.languages) {
            qry_parts << " exists ( select lang.id from Language lang where lang.tipp = tipp and lang.language.id in (:languages) ) "
            qry_params.languages = Params.getLongList_forCommaSeparatedString(params, 'languages') // ?
            filterSet = true
        }

        if (params.subject_references && params.subject_references != "" && listReaderWrapper(params, 'subject_references')) {
            String q = ' ( '
            listReaderWrapper(params, 'subject_references').eachWithIndex { String subRef, int i ->
                //q += " genfunc_filter_matcher(tipp.subjectReference,'"+subRef.trim().toLowerCase()+"') = true "
                q += " tipp.subjectReference = '"+subRef.trim()+"' "
                if(i < listReaderWrapper(params, 'subject_references').size()-1)
                    q += 'or'
            }
            q += ' ) '
            qry_parts << q
            filterSet = true
        }
        if (params.series_names && params.series_names != "" && listReaderWrapper(params, 'series_names')) {
            qry_parts << " lower(tipp.seriesName) in (:series_names) "
            qry_params.series_names = listReaderWrapper(params, 'series_names').collect { ""+it.toLowerCase()+"" }
            filterSet = true
        }

        if(params.first_author) {
            qry_parts << " lower(tipp.firstAuthor) like :first_author "
            qry_params.first_author = "%${params.first_author.toLowerCase()}%"
            filterSet = true
        }

        if(params.first_editor) {
            qry_parts << " lower(tipp.firstEditor) like :first_editor "
            qry_params.first_editor = "%${params.first_editor.toLowerCase()}%"
            filterSet = true
        }

        if(params.summaryOfContent) {
            qry_parts << " lower(tipp.summaryOfContent) like :summaryOfContent "
            qry_params.summaryOfContent = "%${params.summaryOfContent.trim().toLowerCase()}%"
            filterSet = true
        }

        if(params.ebookFirstAutorOrFirstEditor) {
            qry_parts << " (lower(tipp.firstAuthor) like :ebookFirstAutorOrFirstEditor or lower(tipp.firstEditor) like :ebookFirstAutorOrFirstEditor) "
            qry_params.ebookFirstAutorOrFirstEditor = "%${params.ebookFirstAutorOrFirstEditor.trim().toLowerCase()}%"
            filterSet = true
        }

        if(params.dateFirstOnlineFrom) {
            qry_parts << " (tipp.dateFirstOnline is not null AND tipp.dateFirstOnline >= :dateFirstOnlineFrom) "
            qry_params.dateFirstOnlineFrom = sdf.parse(params.dateFirstOnlineFrom)
            filterSet = true

        }
        if(params.dateFirstOnlineTo) {
            qry_parts << " (tipp.dateFirstOnline is not null AND tipp.dateFirstOnline <= :dateFirstOnlineTo) "
            qry_params.dateFirstOnlineTo = sdf.parse(params.dateFirstOnlineTo)
            filterSet = true
        }

        if(params.yearsFirstOnline) {
            qry_parts << " (Year(tipp.dateFirstOnline) in (:yearsFirstOnline)) "
            qry_params.yearsFirstOnline = Params.getLongList_forCommaSeparatedString(params, 'yearsFirstOnline').collect { Integer.valueOf(it.toString()) }
            filterSet = true
        }

        if (params.identifier) {
            qry_parts << " ( exists ( from Identifier ident where ident.tipp.id = tipp.id and ident.value like :identifier ) ) "
            qry_params.identifier = "${params.identifier}"
            filterSet = true
        }

        if (params.provider) {
            qry_parts << " tipp.pkg in (select pkg from Package pkg where pkg.provider.id in (:provider)) "
            qry_params.provider = Params.getLongList(params, 'provider')
            filterSet = true
        }

        if (params.publishers) {
            //(exists (select orgRole from OrgRole orgRole where orgRole.tipp = tipp and orgRole.roleType.id = ${RDStore.OR_PUBLISHER.id} and orgRole.org.name in (:publishers))
            qry_parts << " lower(tipp.publisherName) in (:publishers) "
            qry_params.publishers = listReaderWrapper(params, 'publishers').collect { it.toLowerCase().replaceAll('&quot;', '"') }
            filterSet = true
        }

        if (params.coverageDepth) {
            qry_parts << " exists (select tc.id from tipp.coverages tc where lower(tc.coverageDepth) in (:coverageDepth)) "
            qry_params.coverageDepth = listReaderWrapper(params, 'coverageDepth').collect { it.toLowerCase() }
            filterSet = true
        }

        if (params.title_types && params.title_types != "" && listReaderWrapper(params, 'title_types')) {
            qry_parts << " lower(tipp.titleType) in (:title_types) "
            qry_params.title_types = listReaderWrapper(params, 'title_types').collect { ""+it.toLowerCase()+"" }
            filterSet = true
        }

        if (params.medium) {
            qry_parts << " tipp.medium.id in (:medium) "
            qry_params.medium = Params.getLongList_forCommaSeparatedString(params, 'medium') // ?
            filterSet = true
        }

        if (params.gokbIds && params.gokbIds != "" && listReaderWrapper(params, 'gokbIds')) {
            qry_parts << " gokbId in (:gokbIds) "
            qry_params.gokbIds = listReaderWrapper(params, 'gokbIds')
        }

        String base_qry = "select tipp.id from TitleInstancePackagePlatform as tipp "
        if (qry_parts.size() > 0) {
            base_qry += 'where ' + qry_parts.join(' and ')
        }

        if ((params.sort != null) && (params.sort.length() > 0)) {
                base_qry += "order by ${params.sort} ${params.order}, tipp.sortname "
        }
        else {
            base_qry += "order by tipp.sortname"
        }

        result.query = base_qry
        result.queryParams = qry_params
        result.filterSet = filterSet

        result
    }

    /**
     * Note: this query includes NO sorting because sorting should be done in subsequent queries in order to save performance!
     * Use this query ONLY for fetching IssueEntitlements after because only a part of the filter settings are being treated here!
     * @param params
     * @return
     */
    Map<String,Object> getTippSubsetQuery(Map params) {
        log.debug 'getTippSubsetQuery'
        Map<String, Object> result = [:], clauses = getTippSubsetArguments(params)
        String query = 'select tipp.id from TitleInstancePackagePlatform tipp where'
        result.query = query+" ${clauses.arguments} "
        if(!params.containsKey('noSort'))
            result.query += "order by ${params.sort} ${params.order}"
        result.queryParams = clauses.queryParams
        result
    }

    Map<String, Object> getTippSubsetArguments(Map params) {
        Map<String, Object> result = [:], queryParams = [:]
        Set<String> queryArgs = []
        if(params.packages) {
            queryArgs << 'tipp.pkg in (:packages)'
            queryParams.packages = params.packages
        }
        if(params.platforms) {
            queryArgs << 'tipp.platform in (:platforms)'
            queryParams.platforms = params.platforms
        }
        if(params.get('tippStatus') instanceof List) {
            queryArgs << 'tipp.status in (:tippStatus)'
            queryParams.tippStatus = Params.getRefdataList(params, 'tippStatus')
        }
        else if(params.containsKey('tippStatus')) {
            queryArgs << 'tipp.status = :tippStatus'
            queryParams.tippStatus = RefdataValue.get(params.tippStatus)
        }
        else {
            queryArgs << 'tipp.status != :tippStatus'
            queryParams.tippStatus = RDStore.TIPP_STATUS_REMOVED
        }
        if(params.filter) {
            //deactivated because of performance reasons; query would take 11 seconds instead of millis
            //queryArgs << " ( genfunc_filter_matcher(tipp.name, :filter) = true or genfunc_filter_matcher(tipp.firstAuthor, :filter) = true or genfunc_filter_matcher(tipp.firstEditor, :filter) = true )"
            queryArgs << " ( lower(tipp.name) like :filter or lower(tipp.firstAuthor) like :filter or lower(tipp.firstEditor) like :filter )"
            queryParams.filter = "%${params.filter.toLowerCase()}%"
        }
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        //ERMS-5972
        if (params.asAt) {
            queryArgs << " ( ( :asAt >= tipp.accessStartDate or tipp.accessStartDate is null ) and ( :asAt <= tipp.accessEndDate or tipp.accessEndDate is null) ) "
            queryParams.asAt = sdf.parse(params.asAt)
        }
        if (params.ddcs) {
            queryArgs << " exists ( select ddc.id from DeweyDecimalClassification ddc where ddc.tipp = tipp and ddc.ddc.id in (:ddcs) ) "
            queryParams.ddcs = Params.getLongList_forCommaSeparatedString(params, 'ddcs') //move to getRefdataList()
        }
        if (params.languages) {
            queryArgs << " exists ( select lang.id from Language lang where lang.tipp = tipp and lang.language.id in (:languages) ) "
            queryParams.languages = Params.getLongList_forCommaSeparatedString(params, 'languages') //move to getRefdataList()
        }
        if (params.subject_references && params.subject_references != "" && listReaderWrapper(params, 'subject_references')) {
            String q = ' ( '
            listReaderWrapper(params, 'subject_references').eachWithIndex { String subRef, int i ->
                //q += " genfunc_filter_matcher(tipp.subjectReference,'"+subRef.trim().toLowerCase()+"') = true "
                q += " tipp.subjectReference = '"+subRef.trim()+"' "
                if(i < listReaderWrapper(params, 'subject_references').size()-1)
                    q += 'or'
            }
            q += ' ) '
            queryArgs << q
        }
        if (params.series_names && params.series_names != "" && listReaderWrapper(params, 'series_names')) {
            queryArgs << " tipp.seriesName in (:series_names) "
            queryParams.series_names = listReaderWrapper(params, 'series_names').collect { ""+it+"" }
        }

        if (params.first_author && params.first_author != "" && listReaderWrapper(params, 'first_author')) {
            queryArgs << " lower(tipp.firstAuthor) like :first_author "
            queryParams.first_author = "${params.first_author.toLowerCase()}"
        }

        if (params.first_editor && params.first_editor != "" && listReaderWrapper(params, 'first_editor')) {
            queryArgs << " lower(tipp.firstEditor) like :first_editor "
            queryParams.first_editor = "${params.first_editor.toLowerCase()}"
        }

        if(params.summaryOfContent) {
            queryArgs << " lower(tipp.summaryOfContent) like :summaryOfContent "
            queryParams.summaryOfContent = "%${params.summaryOfContent.trim().toLowerCase()}%"
        }

        if(params.ebookFirstAutorOrFirstEditor) {
            queryArgs << " (lower(tipp.firstAuthor) like :ebookFirstAutorOrFirstEditor or lower(tipp.firstEditor) like :ebookFirstAutorOrFirstEditor) "
            queryParams.ebookFirstAutorOrFirstEditor = "%${params.ebookFirstAutorOrFirstEditor.trim().toLowerCase()}%"
        }

        if(params.dateFirstOnlineFrom) {
            queryArgs << " (tipp.dateFirstOnline is not null AND tipp.dateFirstOnline >= :dateFirstOnlineFrom) "
            queryParams.dateFirstOnlineFrom = sdf.parse(params.dateFirstOnlineFrom)

        }
        if(params.dateFirstOnlineTo) {
            queryArgs << " (tipp.dateFirstOnline is not null AND tipp.dateFirstOnline <= :dateFirstOnlineTo) "
            queryParams.dateFirstOnlineTo = sdf.parse(params.dateFirstOnlineTo)
        }

        if(params.yearsFirstOnline) {
            queryArgs << " (Year(tipp.dateFirstOnline) in (:yearsFirstOnline)) "
            queryParams.yearsFirstOnline = Params.getLongList_forCommaSeparatedString(params, 'yearsFirstOnline').collect { Integer.valueOf(it.toString()) }
        }

        if(params.openAccess) {
            String openAccessString = " (tipp.openAccess in (:openAccess) "
            Set<RefdataValue> openAccess = listReaderWrapper(params, 'openAccess').collect { String key -> RefdataValue.get(key) }
            if(RDStore.GENERIC_NULL_VALUE in openAccess) {
                openAccess.remove(RDStore.GENERIC_NULL_VALUE)
                openAccessString += 'or tipp.openAccess = null'
            }
            openAccessString += ')'
            queryArgs << openAccessString
            queryParams.openAccess = openAccess
        }

        if (params.provider) {
            queryArgs << " tipp.pkg in (select pkg from Package pkg where pkg.provider.id in (:provider)) "
            queryParams.provider = Params.getLongList(params, 'provider')
        }

        if (params.publishers) {
            queryArgs << " lower(tipp.publisherName) in (:publishers) "
            queryParams.publishers = listReaderWrapper(params, 'publishers').collect { it.toLowerCase().replaceAll('&quot;', '"') }
        }

        if (params.coverageDepth) {
            queryArgs << " exists (select tc.id from tipp.coverages tc where lower(tc.coverageDepth) in (:coverageDepth)) "
            queryParams.coverageDepth = listReaderWrapper(params, 'coverageDepth').collect { it.toLowerCase() }
        }

        if (params.title_types && params.title_types != "" && listReaderWrapper(params, 'title_types')) {
            queryArgs << " lower(tipp.titleType) in (:title_types) "
            queryParams.title_types = listReaderWrapper(params, 'title_types').collect { ""+it.toLowerCase()+"" }
        }

        if (params.medium) {
            queryArgs << " tipp.medium.id in (:medium) "
            queryParams.medium = Params.getLongList_forCommaSeparatedString(params, 'medium') // move to getRefdataValue
        }
        String arguments = queryArgs.join(' and ')
        result.queryParams = queryParams
        result.arguments = arguments
        result
    }

    /**
     * Prepares the SQL query for title retrieval, assembling the columns necessary, departing from the given type of entitlement (= which table and set of columns to fetch)
     * currently existing config parameters:
     * <ul>
     *  <li>configMap.sub</li>
     *  <li>configMap.ieStatus</li>
     *  <li>configMap.tippIds</li>
     *  <li>as defined in {@link #getTippQuery(java.util.Map, java.util.List)}}, {@link #getIssueEntitlementQuery(grails.web.servlet.mvc.GrailsParameterMap, java.util.Collection)}</li>
     *  <li>as defined in {@link MyInstitutionController#currentTitles()}</li>
     * </ul>
     * @param configMap the filter parameters
     * @param entitlementInstance the type of object (i.e. table) to fetch
     * @param sql the SQL connection; needed for generation of arrays
     * @return a {@link Map} containing the following data:
     * [query: query,
     *  join: join,
     *  where: where,
     *  order: orderClause,
     *  params: params,
     *  subJoin: subJoin]
     */
    @Deprecated
    Map<String, Object> prepareTitleSQLQuery(Map configMap, String entitlementInstance, Sql sql) {
        String query = "", join = "", subJoin = "", orderClause = "", refdata_value_col = configMap.format == 'kbart' ? 'rdv_value' : I10nTranslation.getRefdataValueColumn(LocaleUtils.getCurrentLocale())
        List whereClauses = []
        Map<String, Object> params = [:]
        Connection connection = sql.dataSource.getConnection()
        //sql.withTransaction {
            SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
            if(entitlementInstance == TitleInstancePackagePlatform.class.name) {
                List<String> columns = ['tipp_id', '(select pkg_name from package where pkg_id = tipp_pkg_fk) as tipp_pkg_name', '(select plat_name from platform where plat_id = tipp_plat_fk) as tipp_plat_name',
                                        '(select plat_title_namespace from platform where plat_id = tipp_plat_fk) as tipp_plat_namespace',
                                        "tipp_title_type as title_type",
                                        'tipp_name as name', 'tipp_access_start_date as accessStartDate', 'tipp_access_end_date as accessEndDate',
                                        'tipp_publisher_name', "(select ${refdata_value_col} from refdata_value where rdv_id = tipp_medium_rv_fk) as tipp_medium", 'tipp_host_platform_url', 'tipp_date_first_in_print',
                                        'tipp_date_first_online', 'tipp_gokb_id', '(select pkg_gokb_id from package where pkg_id = tipp_pkg_fk) as tipp_pkg_uuid', 'tipp_date_created', 'tipp_last_updated', 'tipp_first_author', 'tipp_first_editor', 'tipp_volume', 'tipp_edition_number', 'tipp_series_name', 'tipp_subject_reference',
                                        "(select ${refdata_value_col} from refdata_value where rdv_id = tipp_status_rv_fk) as status",
                                        "(select ${refdata_value_col} from refdata_value where rdv_id = tipp_access_type_rv_fk) as accessType",
                                        "(select ${refdata_value_col} from refdata_value where rdv_id = tipp_open_access_rv_fk) as openAccess"]
                orderClause = " order by tipp_sort_name, tipp_name"
                query = "select ${columns.join(',')} from title_instance_package_platform"
                String subFilter
                if(configMap.sub) {
                    params.subscription = configMap.sub.id
                    if(configMap.containsKey('ieStatus') || configMap.containsKey('notStatus')) {
                        join += " join issue_entitlement on ie_tipp_fk = tipp_id"
                        subFilter = "ie_subscription_fk = :subscription"
                    }
                    else {
                        join += " join subscription_package on sp_pkg_fk = tipp_pkg_fk"
                        subFilter = "sp_sub_fk = :subscription"
                    }
                }
                else if(configMap.subscription) {
                    params.subscription = configMap.subscription.id
                    join += " join subscription_package on sp_pkg_fk = tipp_pkg_fk"
                    subFilter = "sp_sub_fk = :subscription"
                }
                else if(configMap.subscriptions) {
                    List<Object> subIds = []
                    subIds.addAll(configMap.subscriptions.id)
                    params.subscriptions = connection.createArrayOf('bigint', subIds.toArray())
                    if(configMap.containsKey('ieStatus')) {
                        join += " join issue_entitlement on ie_tipp_fk = tipp_id"
                        subFilter = "ie_subscription_fk = any(:subscriptions)"
                        if(configMap.containsKey('subscribers')) {
                            List<Object> subscriberIds = []
                            subscriberIds.addAll(configMap.subscribers)
                            params.subscribers = connection.createArrayOf('bigint', subscriberIds.toArray())
                            whereClauses << "exists(select pt_id from permanent_title where pt_tipp_fk = ie_tipp_fk and pt_owner_fk = any(:subscribers))"
                        }
                    }
                    else {
                        join += " join subscription_package on sp_pkg_fk = tipp_pkg_fk"
                        subFilter = "sp_sub_fk = any(:subscriptions)"
                    }
                }
                else if(configMap.defaultSubscriptionFilter) {
                    String consFilter = ""
                    if(contextService.getOrg().isCustomerType_Consortium())
                        consFilter = "and sub_parent_sub_fk is null"
                    subFilter = "exists (select ie_id from issue_entitlement join subscription on ie_subscription_fk = sub_id join subscription_package on sub_id = sp_sub_fk join org_role on or_sub_fk = sub_id where ie_tipp_fk = tipp_id and (sub_status_rv_fk = any(:subStatus) or sub_has_perpetual_access = true) and or_org_fk = :contextOrg ${consFilter})"
                    //temp; should be enlarged later to configMap.subStatus
                    List<Object> subStatus = [RDStore.SUBSCRIPTION_CURRENT.id]
                    params.subStatus = connection.createArrayOf('bigint', subStatus.toArray())
                    params.contextOrg = contextService.getOrg().id
                }
                if(configMap.pkgfilter != null && !configMap.pkgfilter.isEmpty()) {
                    params.pkgId = Long.parseLong(configMap.pkgfilter)
                    whereClauses << "tipp_pkg_fk = :pkgId"
                }
                else if(configMap.containsKey('pkgIds')) {
                    List<Object> pkgIds = []
                    pkgIds.addAll(configMap.pkgIds)
                    params.pkgIds = connection.createArrayOf('bigint', pkgIds.toArray())
                    whereClauses << "tipp_pkg_fk = any(:pkgIds)"
                }
                if(subFilter) {
                    whereClauses << subFilter
                    if(!configMap.containsKey('defaultSubscriptionFilter')) {
                        if (configMap.status != null && configMap.status != '') {
                            params.ieStatus = connection.createArrayOf('bigint', listReaderWrapper(configMap, 'status').toArray())
                            whereClauses << "ie_status_rv_fk = any(:ieStatus)"
                        }
                        else if (configMap.notStatus != null) {
                            if(configMap.notStatus instanceof String && !configMap.notStatus.isEmpty()) {
                                params.ieStatus = Long.parseLong(configMap.notStatus)
                            }
                            else if(configMap.notStatus instanceof Long) {
                                //already id
                                params.ieStatus = configMap.notStatus
                            }
                            else params.ieStatus = configMap.status
                            whereClauses << "ie_status_rv_fk != :ieStatus"
                        }
                        else {
                            params.ieStatus = RDStore.TIPP_STATUS_CURRENT.id
                            whereClauses << "ie_status_rv_fk = :ieStatus"
                        }
                    }

                    if(configMap.titleGroup != null && !configMap.titleGroup.isEmpty()) {
                        if(params.titleGroup == 'notInGroups'){
                            whereClauses << "not exists ( select igi_id from issue_entitlement_group_item where igi_ie_fk = ie_id) "
                        }else {
                            params.titleGroup = Long.parseLong(configMap.titleGroup)
                            whereClauses << "exists(select igi_id from issue_entitlement_group_item where igi_ie_group_fk = :titleGroup and igi_ie_fk = ie_id)"
                        }
                    }

                }
                if(configMap.asAt && configMap.asAt.length() > 0) {
                    Date dateFilter = DateUtils.getLocalizedSDF_noTime().parse(configMap.asAt)
                    params.asAt = new Timestamp(dateFilter.getTime())
                    whereClauses << "((:asAt >= tipp_access_start_date or tipp_access_start_date is null) and (:asAt <= tipp_access_end_date or tipp_access_end_date is null))"
                }
                if(configMap.status != null && configMap.status != '') {
                    params.tippStatus = connection.createArrayOf('bigint', listReaderWrapper(configMap, 'status').toArray())
                    whereClauses << "tipp_status_rv_fk = any(:tippStatus)"
                }
                else if(configMap.notStatus != null) {
                    if(configMap.notStatus instanceof String && !configMap.notStatus.isEmpty())
                        params.tippStatus = Long.parseLong(configMap.notStatus)
                    else if(configMap.notStatus instanceof Long) {
                        //already id
                        params.tippStatus = configMap.notStatus
                    }
                    else params.tippStatus = configMap.status
                    whereClauses << "tipp_status_rv_fk != :tippStatus"
                }
                else {
                    params.tippStatus = RDStore.TIPP_STATUS_CURRENT.id
                    whereClauses << "tipp_status_rv_fk = :tippStatus"
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
                                   "tipp_title_type as title_type",
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
                    whereClauses << "tipp_pkg_fk = :pkgId"
                }
                else if(configMap.containsKey('pkgIds')) {
                    List<Object> pkgIds = []
                    pkgIds.addAll(configMap.pkgIds)
                    params.pkgIds = connection.createArrayOf('bigint', pkgIds.toArray())
                    whereClauses << "tipp_pkg_fk = any(:pkgIds)"
                }
                /*
                where += " or_sub_fk = :ctxId"
                subJoin = " join org_role on or_sub_fk = ie_subscription_fk "
                params.ctxId = contextService.getOrg().id
                if(contextService.getOrg().getCustomerType() == CustomerTypeService.ORG_CONSORTIUM_BASIC)
                    where += " sub_parent_sub_fk is null"
                */
                orderClause = " order by tipp_sort_name, tipp_name"
                if(configMap.sub) {
                    if(configMap.sub instanceof Subscription)
                        params.subscription = configMap.sub.id
                    else params.subscription = Long.parseLong(configMap.sub)
                    whereClauses << "ie_subscription_fk = :subscription"
                }
                else if(configMap.subscription) {
                    if(configMap.subscription instanceof Subscription)
                        params.subscription = configMap.subscription.id
                    else params.subscription = Long.parseLong(configMap.subscription)
                    whereClauses << "ie_subscription_fk = :subscription"
                }
                else if(configMap.subscriptions) {
                    List<Object> subIds = []
                    if(configMap.subscriptions[0] instanceof Subscription)
                        subIds.addAll(configMap.subscriptions.id)
                    else if(configMap.subscriptions[0] instanceof Long)
                        subIds.addAll(configMap.subscriptions)
                    params.subscriptions = connection.createArrayOf('bigint', subIds.toArray())
                    whereClauses << "ie_subscription_fk = any(:subscriptions)"
                }
                if(configMap.asAt != null && !configMap.asAt.isEmpty()) {
                    Date dateFilter = sdf.parse(configMap.asAt)
                    params.asAt = new Timestamp(dateFilter.getTime())
                    whereClauses << "((:asAt >= (coalesce(ie_access_start_date, tipp_access_start_date, sub_start_date)) or (ie_access_start_date is null and tipp_access_start_date is null and sub_start_date is null)) and (:asAt <= coalesce(ie_access_end_date, tipp_access_end_date, sub_end_date) or (ie_access_start_date is null and tipp_access_end_date is null and sub_end_date is null)))"
                }
                if(configMap.validOn != null) {
                    params.validOn = new Timestamp(configMap.validOn)
                    whereClauses << '( (:validOn >= coalesce(ie_access_start_date, sub_start_date, tipp_access_start_date) or (ie_access_start_date is null and sub_start_date is null and tipp_access_start_date is null) ) and ( :validOn <= coalesce(ie_access_end_date, sub_end_date, tipp_access_end_date) or (ie_access_end_date is null and sub_end_date is null and tipp_access_end_date is null) ) or sub_has_perpetual_access = true)'
                }
                if(configMap.ieStatus) {
                    params.ieStatus = configMap.ieStatus.id
                    whereClauses << "ie_status_rv_fk = :ieStatus"
                }
                else if(configMap.status) {
                    params.ieStatus = connection.createArrayOf('bigint', listReaderWrapper(configMap, 'status').toArray())
                    whereClauses << "ie_status_rv_fk = any(:ieStatus)"
                }
                else if(configMap.notStatus != null && !configMap.notStatus.isEmpty()) {
                    params.ieStatus = configMap.notStatus instanceof String ? Long.parseLong(configMap.notStatus) : configMap.notStatus //already id
                    whereClauses << "ie_status_rv_fk != :ieStatus"
                }
                else {
                    params.ieStatus = RDStore.TIPP_STATUS_CURRENT.id
                    whereClauses << "ie_status_rv_fk = :ieStatus"
                }
                if(configMap.titleGroup != null && !configMap.titleGroup.isEmpty()) {
                    if(params.titleGroup == 'notInGroups'){
                        whereClauses << "not exists ( select igi_id from issue_entitlement_group_item where igi_ie_fk = ie_id) "
                    }else {
                        params.titleGroup = Long.parseLong(configMap.titleGroup)
                        whereClauses << "exists(select igi_id from issue_entitlement_group_item where igi_ie_group_fk = :titleGroup and igi_ie_fk = ie_id)"
                    }
                }
                if(configMap.inTitleGroups != null && !configMap.inTitleGroups.isEmpty()) {
                    if(configMap.inTitleGroups == RDStore.YN_YES.id.toString()) {
                        whereClauses << "exists ( select igi_id from issue_entitlement_group_item where igi_ie_fk = ie_id) "
                    }
                    else{
                        whereClauses << "not exists ( select igi_id from issue_entitlement_group_item where igi_ie_fk = ie_id) "
                    }
                }
                if (configMap.hasPerpetualAccess && !configMap.hasPerpetualAccessBySubs) {
                    if(configMap.hasPerpetualAccess == RDStore.YN_YES.id.toString()) {
                        whereClauses << "ie_perpetual_access_by_sub_fk is not null "
                    }
                    else{
                        whereClauses << "ie_perpetual_access_by_sub_fk is null "
                    }
                }
                if (configMap.hasPerpetualAccess && configMap.hasPerpetualAccessBySubs) {
                    List<Object> perpetualSubs = []
                    perpetualSubs.addAll(listReaderWrapper(configMap, 'hasPerpetualAccessBySubs'))
                    params.perpetualSubs = connection.createArrayOf('bigint', perpetualSubs.toArray())
                    if(configMap.hasPerpetualAccess == RDStore.YN_NO.id.toString()) {
                        whereClauses << "tipp_host_platform_url not in (select tipp2.tipp_host_platform_url from issue_entitlement as ie2 join title_instance_package_platform as tipp2 on ie2.ie_tipp_fk = tipp2.tipp_id where ie2.ie_perpetual_access_by_sub_fk = any(:perpetualSubs)) "
                    }
                    else {
                        whereClauses << "tipp_host_platform_url in (select tipp2.tipp_host_platform_url from issue_entitlement as ie2 join title_instance_package_platform as tipp2 on ie2.ie_tipp_fk = tipp2.tipp_id where ie2.ie_perpetual_access_by_sub_fk = any(:perpetualSubs)) "
                    }
                }
                if (configMap.coverageDepth != null && !configMap.coverageDepth.isEmpty()) {
                    List<Object> coverageDepths = []
                    coverageDepths.addAll(listReaderWrapper(configMap, 'coverageDepth').collect { it.toLowerCase() })
                    params.coverageDepth = connection.createArrayOf('varchar', coverageDepths.toArray())
                    whereClauses << "exists (select ic_id from issue_entitlement_coverages where ic_ie_fk = ie_id and lower(ic_coverage_depth) = any(:coverageDepth))"
                }
                if(configMap.filterSub != null && !configMap.filterSub.isEmpty()) {
                    List<Object> subscriptions = []
                    subscriptions.addAll(listReaderWrapper(configMap, 'filterSub').collect { Long.parseLong(it)} )
                    params.subscriptions = connection.createArrayOf('bigint', subscriptions.toArray())
                    whereClauses << "sub_id = any(:subscriptions)"
                }
            }
            if(configMap.tippIds != null && !configMap.tippIds.isEmpty()) {
                List<Object> tippIDs = []
                tippIDs.addAll(configMap.tippIds)
                params.tippIds = connection.createArrayOf('bigint', tippIDs.toArray())
                whereClauses << "tipp_id = any(:tippIds)"
            }
            if(configMap.filter != null && !configMap.filter.isEmpty()) {
                params.stringFilter = configMap.filter
                whereClauses << "((genfunc_filter_matcher(tipp_name, :stringFilter) = true) or (genfunc_filter_matcher(tipp_first_author, :stringFilter) = true) or (genfunc_filter_matcher(tipp_first_editor, :stringFilter) = true) or exists(select id_id from identifier where id_tipp_fk = tipp_id and genfunc_filter_matcher(id_value, :stringFilter) = true))"
            }
            if(configMap.ddcs != null && !configMap.ddcs.isEmpty()) {
                List<Object> ddcs = []
                ddcs.addAll(listReaderWrapper(configMap, 'ddcs').collect{ String key -> Long.parseLong(key) })
                params.ddcs = connection.createArrayOf('bigint', ddcs.toArray())
                whereClauses << "exists(select ddc_id from dewey_decimal_classification where ddc_tipp_fk = tipp_id and ddc_rv_fk = any(:ddcs))"
            }
            if(configMap.languages != null && !configMap.languages.isEmpty()) {
                List<Object> languages = []
                languages.addAll(listReaderWrapper(configMap, 'languages').collect{ String key -> Long.parseLong(key) })
                params.languages = connection.createArrayOf('bigint', languages.toArray())
                whereClauses << "exists(select lang_id from language where lang_tipp_fk = tipp_id and lang_rv_fk = any(:languages))"
            }
            if(configMap.subject_references != null && !configMap.subject_references.isEmpty()) {
                List<Object> subjectReferences = []
                subjectReferences.addAll(listReaderWrapper(configMap, 'subject_references').collect { '%'+it.toLowerCase()+'%' })
                params.subjectReferences = connection.createArrayOf('varchar', subjectReferences.toArray())
                whereClauses << "lower(tipp_subject_reference) like any(:subjectReferences)"
            }
            if(configMap.series_names != null && !configMap.series_names.isEmpty()) {
                List<Object> seriesNames = []
                seriesNames.addAll(listReaderWrapper(configMap, 'series_names').collect { it.toLowerCase() })
                params.seriesNames = connection.createArrayOf('varchar', seriesNames.toArray())
                whereClauses << "lower(tipp_series_name) in (:seriesNames)"
            }
            if(configMap.summaryOfContent != null && !configMap.summaryOfContent.isEmpty()) {
                params.summaryOfContent = configMap.summaryOfContent
                whereClauses << "genfunc_filter_matcher(tipp_summary_of_content, :summaryOfContent) = true"
            }
            if(configMap.ebookFirstAutorOrFirstEditor != null && !configMap.ebookFirstAutorOrFirstEditor.isEmpty()) {
                params.firstAuthorEditor = configMap.ebookFirstAutorOrFirstEditor
                whereClauses << "genfunc_filter_matcher(tipp_first_author, :firstAuthorEditor) = true or genfunc_filter_matcher(tipp_first_editor, :firstAuthorEditor) = true)"
            }
            if(configMap.dateFirstOnlineFrom != null && !configMap.dateFirstOnlineFrom.isEmpty()) {
                Date dateFirstOnlineFrom = sdf.parse(configMap.dateFirstOnlineFrom)
                params.dateFirstOnlineFrom = DateUtils.getSDF_yyyyMMdd().format(dateFirstOnlineFrom)
                whereClauses << "(tipp_date_first_online is not null AND tipp_date_first_online >= :dateFirstOnlineFrom)"
            }
            if(configMap.dateFirstOnlineTo != null && !configMap.dateFirstOnlineTo.isEmpty()) {
                Date dateFirstOnlineTo = sdf.parse(configMap.dateFirstOnlineTo)
                params.dateFirstOnlineTo = DateUtils.getSDF_yyyyMMdd().format(dateFirstOnlineTo)
                whereClauses << "(tipp.date_first_online is not null AND tipp_date_first_online <= :dateFirstOnlineTo)"
            }
            if(configMap.yearsFirstOnline != null && !configMap.yearsFirstOnline.isEmpty()) {
                List<Object> yearsFirstOnline = []
                yearsFirstOnline.addAll(listReaderWrapper(configMap, 'yearsFirstOnline').collect { Integer.parseInt(it) })
                params.yearsFirstOnline = connection.createArrayOf('int', yearsFirstOnline.toArray())
                whereClauses << "(date_part('year', tipp_date_first_online) = any(:yearsFirstOnline))"
            }
            if(configMap.identifier != null && !configMap.identifier.isEmpty()) {
                params.identifier = configMap.identifier
                whereClauses << "exists(select id_id from identifier where id_tipp_fk = tipp_id and genfunc_filter_matcher(id_value, :identifier) = true)"
            }
            if(configMap.publishers != null && !configMap.publishers.isEmpty()) {
                List<Object> publishers = []
                publishers.addAll(listReaderWrapper(configMap, 'publishers').collect { it.toLowerCase() })
                params.publishers = connection.createArrayOf('varchar', publishers.toArray())
                whereClauses << "lower(tipp_publisher_name) = any(:publishers)"
            }
            if(configMap.title_types != null && !configMap.title_types.isEmpty()) {
                List<Object> titleTypes = []
                titleTypes.addAll(listReaderWrapper(configMap, 'title_types').collect { it.toLowerCase() })
                params.titleTypes = connection.createArrayOf('varchar', titleTypes.toArray())
                whereClauses << "lower(tipp_title_type) = any(:titleTypes)"
            }
            if(configMap.medium != null && !configMap.medium.isEmpty()) {
                List<Object> medium = []
                medium.addAll(listReaderWrapper(configMap, 'medium').collect{ String key -> Long.parseLong(key) })
                params.medium = connection.createArrayOf('bigint', medium.toArray())
                whereClauses << "tipp_medium_rv_fk = any(:medium)"
            }
            if(configMap.filterPvd != null && !configMap.filterPvd.isEmpty()) {
                List<Object> providers = []
                providers.addAll(listReaderWrapper(configMap, 'filterPvd').collect { Long.parseLong(it.split(':')[1])} )
                params.providers = connection.createArrayOf('bigint', providers.toArray())
                whereClauses << "pkg_provider_fk = any(:providers)"
            }
            if(configMap.filterVen != null && !configMap.filterVen.isEmpty()) {
                List<Object> vendors = []
                vendors.addAll(listReaderWrapper(configMap, 'filterVen').collect { Long.parseLong(it.split(':')[1])} )
                params.vendors = connection.createArrayOf('bigint', vendors.toArray())
                whereClauses << "exists(select pv_id from package_vendor where pv_pkg_fk = pkg_id and pv_vendor_fk = any(:vendors))"
            }
            if(configMap.filterHostPlat != null && !configMap.filterHostPlat.isEmpty()) {
                List<Object> hostPlatforms = []
                hostPlatforms.addAll(listReaderWrapper(configMap, 'filterHostPlat').collect { Long.parseLong(it.split(':')[1]) })
                params.platforms = connection.createArrayOf('bigint', hostPlatforms.toArray())
                whereClauses << "tipp_plat_fk = any(:platforms)"
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
        [query: query, join: join, where: whereClauses.join(' and '), order: orderClause, params: params, subJoin: subJoin]
    }

    Map<String, Object> getWekbPlatformFilterParams(GrailsParameterMap params) {
        Map<String, Object> queryParams = [componentType: "Platform"]

        if(params.q) {
            queryParams.name = params.q
        }

        if(params.provider) {
            queryParams.provider = params.provider
        }

        if(Params.getLongList(params, 'platStatus')) {
            queryParams.status = RefdataValue.findAllByIdInList(Params.getLongList(params, 'platStatus')).value
        }
        else if(!params.filterSet) {
            queryParams.status = "Current"
            params.platStatus = RDStore.PLATFORM_STATUS_CURRENT.id
        }

        Set<String> refdataListParams = PLATFORM_FILTER_ACCESSIBILITY_FIELDS.keySet()+PLATFORM_FILTER_ADDITIONAL_SERVICE_FIELDS.keySet()+PLATFORM_FILTER_AUTH_FIELDS.keySet()+['statisticsFormat']
        refdataListParams.each { String refdataListParam ->
            if (params.containsKey(refdataListParam)) {
                queryParams.put(refdataListParam, Params.getRefdataList(params, refdataListParam).collect{ (it == RDStore.GENERIC_NULL_VALUE) ? 'null' : it.value })
            }
        }
        if (params.counterSupport) {
            queryParams.counterSupport = params.list('counterSupport')
        }
        if(params.counterAPISupport) {
            queryParams.counterAPISupport = params.list('counterAPISupport')
        }
        if (params.curatoryGroup) {
            queryParams.curatoryGroupExact = params.curatoryGroup
        }

        if (params.curatoryGroupType) {
            queryParams.curatoryGroupType = params.curatoryGroupType
        }
        queryParams
    }

    /**
     * Helper method for a shorthand of parameter list reading.
     * Note that also lists are being delivered with only one entry
     * @param params the parameter map
     * @param key the key to be read from the map
     * @return a list containing the parameter values
     */
    List listReaderWrapper(Map params, String key) {
        log.debug ('listReaderWrapper: ' + key + ' @ ' + params.toMapString())
        List result
        if(params instanceof GrailsParameterMap) {
            result = params.list(key)
            //there are some cases when list does not resolve properly
            if(result[0] instanceof String && result[0].contains(',')) {
                List newResult = result[0].split(',')
                result = newResult
            }
        }
        //.respondsTo('size') is a substitute for instanceof Ljava.lang.String;
        else if(params[key] instanceof List || (params[key].respondsTo('size') && !(params[key] instanceof String))) {
            result = params[key]
        }
        else if(params[key] instanceof String && params[key].contains(',')) {
            result = params[key].split(',')
        }
        else result = [params[key]]
        result
    }
}
