package de.laser


import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.helper.DateUtils
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.properties.PropertyDefinition
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.i18n.LocaleContextHolder

import java.text.DateFormat
import java.text.SimpleDateFormat

/**
 * This service handles generic query creating with inclusive filter processing
 */
@Transactional
class FilterService {

    private static final Long FAKE_CONSTRAINT_ORGID_WITHOUT_HITS = new Long(-1)

    def genericOIDService
    def contextService
    def messageSource
    PropertyService propertyService

    /**
     * Processes organisation filters and generates a query to fetch organisations
     * @param params the filter parameter map
     * @return the map containing the query and the prepared query parameters
     */
    Map<String, Object> getOrgQuery(GrailsParameterMap params) {
        Map<String, Object> result = [:]
        ArrayList<String> query = ["o.status != :orgStatus"]
        Map<String, Object> queryParams = ["orgStatus" : RDStore.ORG_STATUS_DELETED]

        if (params.orgNameContains?.length() > 0) {
            query << "(genfunc_filter_matcher(o.name, :orgNameContains) = true or genfunc_filter_matcher(o.shortname, :orgNameContains) = true or genfunc_filter_matcher(o.sortname, :orgNameContains) = true) or exists(select alt.id from AlternativeName alt where alt.org = o and genfunc_filter_matcher(alt.name, :orgNameContains) = true) "
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
        if (params.orgRole?.length() > 0) {
            query << " exists (select ogr from o.links as ogr where ogr.roleType.id = :orgRole )"
             queryParams << [orgRole : Long.parseLong(params.orgRole)]
        }
        if (params.orgSector?.length() > 0) {
            query << "o.sector.id = :orgSector"
             queryParams << [orgSector : Long.parseLong(params.orgSector)]
        }
        if (params.orgIdentifier?.length() > 0) {
            query << " exists (select ident from Identifier ident join ident.org ioorg " +
                     " where ioorg = o and LOWER(ident.value) like LOWER(:orgIdentifier)) "
            queryParams << [orgIdentifier: "%${params.orgIdentifier}%"]
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
            query << "(genfunc_filter_matcher(o.name, :orgNameContains1) = true or genfunc_filter_matcher(o.shortname, :orgNameContains2) = true or genfunc_filter_matcher(o.sortname, :orgNameContains3) = true) "
             queryParams << [orgNameContains1 : "${params.orgNameContains}"]
             queryParams << [orgNameContains2 : "${params.orgNameContains}"]
             queryParams << [orgNameContains3 : "${params.orgNameContains}"]
        }
       /* if (params.orgType?.length() > 0) {
            query << "o.orgType.id = ?"
             queryParams << [Long.parseLong(params.orgType)
        }*/
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

        if (params.customerType?.length() > 0) {
            query << "exists (select oss from OrgSetting as oss where oss.id = o.id and oss.key = :customerTypeKey and oss.roleValue.id = :customerType)"
            queryParams << [customerType : Long.parseLong(params.customerType)]
            queryParams << [customerTypeKey : OrgSetting.KEYS.CUSTOMER_TYPE]
        }

        if (params.orgIdentifier?.length() > 0) {
            query << " exists (select ident from Identifier io join io.org ioorg " +
                    " where ioorg = o and LOWER(ident.value) like LOWER(:orgIdentifier)) "
            queryParams << [orgIdentifier: "%${params.orgIdentifier}%"]
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

    @Deprecated
    Map<String,Object> getSurveyQueryConsortia(Map params, DateFormat sdFormat, Org contextOrg) {
        Map result = [:]
        List query = []
        Map<String,Object> queryParams = [:]
        if(params.name) {
            query << "genfunc_filter_matcher(si.name, :name) = true"
            queryParams << [name:"${params.name}"]
        }
        if(params.status) {
            query << "si.status = :status"
            queryParams << [status: RefdataValue.get(params.status)]
        }
        if(params.type) {
            query << "si.type = :type"
            queryParams << [type: RefdataValue.get(params.type)]
        }
        if (params.startDate && sdFormat) {
            query << "si.startDate >= :startDate"
            queryParams << [startDate : sdFormat.parse(params.startDate)]
        }
        if (params.endDate && sdFormat) {
            query << "si.endDate <= :endDate"
            queryParams << [endDate : sdFormat.parse(params.endDate)]
        }

        if (params.participant) {
            query << "exists (select surConfig from SurveyConfig as surConfig where surConfig.surveyInfo = si and " +
                    " exists (select surResult from SurveyResult as surResult where surResult.surveyConfig = surConfig and participant = :participant))"
            queryParams << [participant : params.participant]
        }

        String defaultOrder = " order by " + (params.sort ?: " LOWER(si.name)") + " " + (params.order ?: "asc")

        if (query.size() > 0) {
            result.query = "from SurveyInfo si where si.owner = :contextOrg and " + query.join(" and ") + defaultOrder
        } else {
            result.query = "from SurveyInfo si where si.owner = :contextOrg" + defaultOrder
        }
        queryParams << [contextOrg : contextOrg]


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
            queryParams << [owner: params.owner instanceof Org ?: Org.get(params.owner) ]
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
            query << "((surOrg.org = :org and surOrg.finishDate is null and surConfig.pickAndChoose = true and surConfig.surveyInfo.status = :status) " +
                    "or exists (select surResult from SurveyResult surResult where surResult.surveyConfig = surConfig and surConfig.surveyInfo.status = :status and surResult.dateCreated = surResult.lastUpdated and surOrg.finishDate is null and surOrg.org = :org))"
            queryParams << [status: RDStore.SURVEY_SURVEY_STARTED]
            queryParams << [org : org]
        }

        if(params.tab == "processed"){
            query << "(surInfo.status = :status and exists (select surResult from SurveyResult surResult where surResult.surveyConfig = surConfig and surResult.dateCreated < surResult.lastUpdated and surOrg.finishDate is null and surOrg.org = :org))"
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
            base_qry += " and (genfunc_filter_matcher(surveyOrg.org.name, :orgNameContains1) = true or genfunc_filter_matcher(surveyOrg.org.shortname, :orgNameContains2) = true or genfunc_filter_matcher(surveyOrg.org.sortname, :orgNameContains3) = true) "
            queryParams << [orgNameContains1 : "${params.orgNameContains}"]
            queryParams << [orgNameContains2 : "${params.orgNameContains}"]
            queryParams << [orgNameContains3 : "${params.orgNameContains}"]
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
                            base_qry += " ) "
                    }
                    else if (pd.isIntegerType()) {
                            if (!params.filterProp || params.filterProp.length() < 1) {
                                base_qry += " and surResult.intValue = null ) "
                            } else {
                                base_qry += " and surResult.intValue = :prop ) "
                                queryParams.put('prop', AbstractPropertyWithCalculatedLastUpdated.parseValue(params.filterProp, pd.type))
                            }
                    }
                    else if (pd.isStringType()) {
                            if (!params.filterProp || params.filterProp.length() < 1) {
                                base_qry += " and surResult.stringValue = null ) "
                            } else {
                                base_qry += " and lower(surResult.stringValue) like lower(:prop) ) "
                                queryParams.put('prop', "%${AbstractPropertyWithCalculatedLastUpdated.parseValue(params.filterProp, pd.type)}%")
                            }
                    }
                    else if (pd.isBigDecimalType()) {
                            if (!params.filterProp || params.filterProp.length() < 1) {
                                base_qry += " and surResult.decValue = null ) "
                            } else {
                                base_qry += " and surResult.decValue = :prop ) "
                                queryParams.put('prop', AbstractPropertyWithCalculatedLastUpdated.parseValue(params.filterProp, pd.type))
                            }
                    }
                    else if (pd.isDateType()) {
                            if (!params.filterProp || params.filterProp.length() < 1) {
                                base_qry += " and surResult.dateValue = null ) "
                            } else {
                                base_qry += " and surResult.dateValue = :prop ) "
                                queryParams.put('prop', AbstractPropertyWithCalculatedLastUpdated.parseValue(params.filterProp, pd.type))
                            }
                    }
                    else if (pd.isURLType()) {
                            if (!params.filterProp || params.filterProp.length() < 1) {
                                base_qry += " and surResult.urlValue = null ) "
                            } else {
                                base_qry += " and genfunc_filter_matcher(surResult.urlValue, :prop) = true ) "
                                queryParams.put('prop', AbstractPropertyWithCalculatedLastUpdated.parseValue(params.filterProp, pd.type))
                            }
                    }
                }
                base_qry += " ) "
            }
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
        Locale locale = LocaleContextHolder.getLocale()
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
            base_qry += "and tipp.status != :tippStatusDeleted "
            qry_params.tippStatusDeleted = RDStore.TIPP_STATUS_DELETED
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
     * Processes the given filter parameters and generates a query to retrieve issue entitlements
     * @param params the filter parameter map
     * @param subscription the subscription whose dates should be considered
     * @return the map containing the query and the prepared query parameters
     */
    Map<String,Object> getIssueEntitlementQuery(GrailsParameterMap params, Subscription subscription) {
        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
        Map result = [:]



        String base_qry
        Map<String,Object> qry_params = [subscription: subscription]
        boolean filterSet = false
        Date date_filter
        if (params.asAt && params.asAt.length() > 0) {
            date_filter = sdf.parse(params.asAt)
            result.as_at_date = date_filter
            result.editable = false
        }
        if (params.filter) {
            base_qry = " from IssueEntitlement as ie left join ie.coverages ic join ie.tipp tipp where ie.subscription = :subscription "
            if (date_filter) {
                // If we are not in advanced mode, hide IEs that are not current, otherwise filter
                // base_qry += "and ie.status <> ? and ( ? >= coalesce(ie.accessStartDate,subscription.startDate) ) and ( ( ? <= coalesce(ie.accessEndDate,subscription.endDate) ) OR ( ie.accessEndDate is null ) )  "
                // qry_params.add(deleted_ie);
                base_qry += "and ( ( :startDate >= coalesce(ie.accessStartDate,ie.subscription.startDate,ie.tipp.accessStartDate) or (ie.accessStartDate is null and ie.subscription.startDate is null and ie.tipp.accessStartDate is null) ) and ( :endDate <= coalesce(ie.accessEndDate,ie.subscription.endDate,ie.tipp.accessEndDate) or (ie.accessEndDate is null and ie.subscription.endDate is null and ie.tipp.accessEndDate is null) OR ( ie.subscription.hasPerpetualAccess = true ) ) ) "
                qry_params.startDate = date_filter
                qry_params.endDate = date_filter
            }
            base_qry += "and ( ( lower(ie.name) like :title ) or ( exists ( from Identifier ident where ident.tipp.id = ie.tipp.id and ident.value like :identifier ) ) or ((lower(ie.tipp.firstAuthor) like :ebookFirstAutorOrFirstEditor or lower(ie.tipp.firstEditor) like :ebookFirstAutorOrFirstEditor)) ) "
            qry_params.title = "%${params.filter.trim().toLowerCase()}%"
            qry_params.identifier = "%${params.filter}%"
            qry_params.ebookFirstAutorOrFirstEditor = "%${params.filter.trim().toLowerCase()}%"
            filterSet = true
        }
        else {
            base_qry = " from IssueEntitlement as ie left join ie.coverages ic join ie.tipp tipp where ie.subscription = :subscription "
            /*if (params.mode != 'advanced') {
                // If we are not in advanced mode, hide IEs that are not current, otherwise filter

                base_qry += " and ( :startDate >= coalesce(ie.accessStartDate,ie.subscription.startDate,ie.tipp.accessStartDate) or (ie.accessStartDate is null and ie.subscription.startDate is null and ie.tipp.accessStartDate is null) ) and ( ( :endDate <= coalesce(ie.accessEndDate,ie.subscription.endDate,ie.accessEndDate) or (ie.accessEndDate is null and ie.subscription.endDate is null and ie.tipp.accessEndDate is null)  or (ie.subscription.hasPerpetualAccess = true) ) ) "
                qry_params.startDate = date_filter
                qry_params.endDate = date_filter
            }*/
        }

        if(params.status != '' && params.status != null) {
            base_qry += " and ie.status.id = :status "
            qry_params.status = params.status
        }else if (params.notStatus != '' && params.notStatus != null){
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
            base_qry += " and ie.status != :deleted "
            qry_params.deleted = RDStore.TIPP_STATUS_DELETED
        }*/

        if(params.ieAcceptStatusFixed) {
            base_qry += " and ie.acceptStatus = :ieAcceptStatus "
            qry_params.ieAcceptStatus = RDStore.IE_ACCEPT_STATUS_FIXED
        }

        if(params.ieAcceptStatusNotFixed) {
            base_qry += " and ie.acceptStatus != :ieAcceptStatus "
            qry_params.ieAcceptStatus = RDStore.IE_ACCEPT_STATUS_FIXED
        }

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
            base_qry += " and lower(tipp.subjectReference) in (:subject_references)"
            qry_params.subject_references = params.list('subject_references').collect { ""+it.toLowerCase()+"" }
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
    Map<String,Object> getTippQuery(GrailsParameterMap params, List<Package> pkgs) {
        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
        Map result = [:]

        String base_qry
        Map<String,Object> qry_params = [pkgs: pkgs]
        boolean filterSet = false
        Date date_filter
        if (params.asAt && params.asAt.length() > 0) {
            date_filter = sdf.parse(params.asAt)
            result.as_at_date = date_filter
            result.editable = false
        }
        if (params.filter) {
            base_qry = "select tipp.id from TitleInstancePackagePlatform as tipp where tipp.pkg in (:pkgs) "
            if (date_filter) {
                base_qry += "and ( ( :startDate >= coalesce(tipp.accessStartDate,tipp.pkg.startDate,tipp.accessStartDate) or (tipp.accessStartDate is null and tipp.pkg.startDate is null and tipp.accessStartDate is null) ) and ( :endDate <= coalesce(tipp.accessEndDate,tipp.pkg.endDate,tipp.accessEndDate) or (tipp.accessEndDate is null and tipp.subscription.endDate is null and tipp.accessEndDate is null) ) ) "
                qry_params.startDate = date_filter
                qry_params.endDate = date_filter
            }
            base_qry += "and ( ( lower(tipp.name) like :title ) or ( exists ( from Identifier ident where ident.tipp.id = tipp.id and ident.value like :identifier ) ) or ((lower(tipp.firstAuthor) like :ebookFirstAutorOrFirstEditor or lower(tipp.firstEditor) like :ebookFirstAutorOrFirstEditor)) ) "
            qry_params.title = "%${params.filter.trim().toLowerCase()}%"
            qry_params.identifier = "%${params.filter}%"
            qry_params.ebookFirstAutorOrFirstEditor = "%${params.filter.trim().toLowerCase()}%"
            filterSet = true
        }
        else {
            base_qry = "select tipp.id from TitleInstancePackagePlatform as tipp where tipp.pkg in (:pkgs) "
        }

        if(params.addEntitlements && params.subscription && params.issueEntitlementStatus) {
            base_qry += " and tipp.pkg in ( select pkg from SubscriptionPackage sp where sp.subscription = :subscription ) and " +
                    "( not exists ( select ie from IssueEntitlement ie where ie.subscription = :subscription and ie.tipp.id = tipp.id and ie.status = :issueEntitlementStatus ) )"
            qry_params.subscription = params.subscription
            qry_params.issueEntitlementStatus = params.issueEntitlementStatus
        }

       /* if(params.mode != 'advanced') {
            base_qry += " and tipp.status = :current "
            qry_params.current = RDStore.TIPP_STATUS_CURRENT
        }
        else {
            base_qry += " and tipp.status != :deleted "
            qry_params.deleted = RDStore.TIPP_STATUS_DELETED
        }*/

        if(params.status != '' && params.status != null) {
            base_qry += " and tipp.status.id = :status "
            qry_params.status = params.status
        }else if (params.notStatus != '' && params.notStatus != null){
            base_qry += " and tipp.status.id != :notStatus "
            qry_params.notStatus = params.notStatus
        }
        else {
            base_qry += " and tipp.status = :current "
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
            base_qry += ' and ( '
            params.list('subject_references').eachWithIndex { String subRef, int i ->
                base_qry += " lower(tipp.subjectReference) like '%"+subRef.trim().toLowerCase()+"%' "
                if(i < params.list('subject_references').size()-1)
                    base_qry += 'or'
            }
            base_qry += ' ) '
            /*base_qry += " and lower(tipp.subjectReference) in (select * from value (:subject_references))"
            qry_params.subject_references = params.list('subject_references').collect { "%"+it.toLowerCase()+"%" }*/
            filterSet = true
        }
        if (params.series_names && params.series_names != "" && params.list('series_names')) {
            base_qry += " and lower(tipp.seriesName) in (:series_names)"
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
            //(exists (select orgRole from OrgRole orgRole where orgRole.tipp = tipp and orgRole.roleType.id = ${RDStore.OR_PUBLISHER.id} and orgRole.org.name in (:publishers))
            base_qry += "and (lower(tipp.publisherName)) in (:publishers) "
            qry_params.publishers = params.list('publishers').collect { it.toLowerCase() }
            filterSet = true
        }

        if (params.coverageDepth) {
            base_qry += "and exists (select tc.id from tipp.coverages tc where lower(tc.coverageDepth) in (:coverageDepth))"
            qry_params.coverageDepth = params.list('coverageDepth').collect { it.toLowerCase() }
            filterSet = true
        }

        if (params.title_types && params.title_types != "" && params.list('title_types')) {
            base_qry += " and lower(tipp.titleType) in (:title_types)"
            qry_params.title_types = params.list('title_types').collect { ""+it.toLowerCase()+"" }
            filterSet = true
        }

        if ((params.sort != null) && (params.sort.length() > 0)) {
                base_qry += "order by ${params.sort} ${params.order} "
        }
        else {
            base_qry += "order by lower(tipp.sortname) asc"
        }


        result.query = base_qry
        result.queryParams = qry_params
        result.filterSet = filterSet


        result

    }


}
