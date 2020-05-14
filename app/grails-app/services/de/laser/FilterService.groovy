package de.laser

import com.k_int.kbplus.*
import com.k_int.kbplus.auth.User
import de.laser.helper.DateUtil
import de.laser.helper.RDStore
import org.springframework.context.i18n.LocaleContextHolder

import javax.annotation.PostConstruct
import java.text.DateFormat
import java.text.SimpleDateFormat

class FilterService {

    private static final Long FAKE_CONSTRAINT_ORGID_WITHOUT_HITS = new Long(-1)
    def springSecurityService
    def genericOIDService
    def contextService
    def messageSource
    Locale locale

    @PostConstruct
    void init() {
        locale = LocaleContextHolder.getLocale()
    }

    Map<String, Object> getOrgQuery(Map params) {
        Map<String, Object> result = [:]
        ArrayList<String> query = ["(o.status is null or o.status != :orgStatus)"]
        Map<String, Object> queryParams = ["orgStatus" : RDStore.ORG_STATUS_DELETED]

        if (params.orgNameContains?.length() > 0) {
            query << "(genfunc_filter_matcher(o.name, :orgNameContains1) = true or genfunc_filter_matcher(o.shortname, :orgNameContains2) = true or genfunc_filter_matcher(o.sortname, :orgNameContains3) = true) "
             queryParams << [orgNameContains1 : "${params.orgNameContains}"]
             queryParams << [orgNameContains2 : "${params.orgNameContains}"]
             queryParams << [orgNameContains3 : "${params.orgNameContains}"]
        }
        if (params.orgType?.length() > 0) {
            query << " exists (select roletype from o.orgType as roletype where roletype.id = :orgType )"
             queryParams << [orgType : Long.parseLong(params.orgType)]
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
        if (params.country?.length() > 0) {
            query << "o.country.id = :country"
             queryParams << [country : Long.parseLong(params.country)]
        }

        if (params.customerType?.length() > 0) {
            query << "exists (select oss from OrgSettings as oss where oss.id = o.id and oss.key = :customerTypeKey and oss.roleValue.id = :customerType)"
            queryParams << [customerType : Long.parseLong(params.customerType)]
            queryParams << [customerTypeKey : OrgSettings.KEYS.CUSTOMER_TYPE]
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

        log.debug(result)
        result
    }

    Map<String, Object> getOrgComboQuery(Map params, Org org) {
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
            query << "o.libraryType.id = :libraryType"
            List<String> selLibraryTypes = params.list("libraryType")
            List<Long> libraryTypes = []
            selLibraryTypes.each { String sel ->
                libraryTypes << Long.parseLong(sel)
            }
            queryParams << [libraryType : libraryTypes]
        }

        if (params.customerType?.length() > 0) {
            query << "exists (select oss from OrgSettings as oss where oss.id = o.id and oss.key = :customerTypeKey and oss.roleValue.id = :customerType)"
            queryParams << [customerType : Long.parseLong(params.customerType)]
            queryParams << [customerTypeKey : OrgSettings.KEYS.CUSTOMER_TYPE]
        }

        if (params.orgIdentifier?.length() > 0) {
            query << " exists (select ident from Identifier io join io.org ioorg " +
                    " where ioorg = o and LOWER(ident.value) like LOWER(:orgIdentifier)) "
            queryParams << [orgIdentifier: "%${params.orgIdentifier}%"]
        }

         queryParams << [org : org]
         queryParams << [comboType : params.comboType]

        String defaultOrder = " order by " + (params.sort && (!params.consSort && !params.ownSort && !params.subscrSort) ?: " LOWER(o.sortname)") + " " + (params.order && (!params.consSort && !params.ownSort && !params.subscrSort) ?: "asc")

        if (query.size() > 0) {
            result.query = "select o from Org as o, Combo as c where " + query.join(" and ") + " and c.fromOrg = o and c.toOrg = :org and c.type.value = :comboType " + defaultOrder
        } else {
            result.query = "select o from Org as o, Combo as c where c.fromOrg = o and c.toOrg = :org and c.type.value = :comboType " + defaultOrder
        }
        result.queryParams = queryParams
        result
    }

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
        if(params.docCreator) {
            query << "d.creator = :creator"
            queryParams << [creator: User.get(params.docCreator)]
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
                    case com.k_int.kbplus.Package.class.name: targetQuery << "dc.pkg in (:pkgs)"
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
            queryParams << [context:contextService.org]
        }
        if(query.size() > 0)
            result.query = " and "+query.join(" and ")
        else result.query = ""
        result.queryParams = queryParams
        result
    }

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

    Map<String,Object> getSurveyConfigQueryConsortia(Map params, DateFormat sdFormat, Org contextOrg) {
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

        if(params.name) {
            query << "(genfunc_filter_matcher(surInfo.name, :name) = true or (genfunc_filter_matcher(surConfig.subscription.name, :name) = true))"
            queryParams << [name:"${params.name}"]
            params.filterSet = true
        }

        if(params.status) {
            query << "surInfo.status = :status"
            queryParams << [status: RefdataValue.get(params.status)]
            params.filterSet = true
        }
        if(params.type) {
            query << "surInfo.type = :type"
            queryParams << [type: RefdataValue.get(params.type)]
            params.filterSet = true
        }
        if (params.startDate && sdFormat) {
            query << "surInfo.startDate >= :startDate"
            queryParams << [startDate : sdFormat.parse(params.startDate)]
            params.filterSet = true
        }
        if (params.endDate && sdFormat) {
            query << "surInfo.endDate <= :endDate"
            queryParams << [endDate : sdFormat.parse(params.endDate)]
            params.filterSet = true
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

        if(params.ids) {
            query << "surInfo.id in (:ids)"
            queryParams << [ids: params.list('ids').collect{Long.parseLong(it)}]
            params.filterSet = true
        }

        if(params.checkSubSurveyUseForTransfer) {
            query << "surConfig.subSurveyUseForTransfer = :checkSubSurveyUseForTransfer"
            queryParams << [checkSubSurveyUseForTransfer: true]
            params.filterSet = true
        }

        if (params.provider) {
            query << "exists (select orgRole from OrgRole orgRole where orgRole.sub = surConfig.subscription and orgRole.org = :provider)"
            queryParams << [provider : Org.get(params.provider)]
            params.filterSet = true
        }

        if (params.list('filterSub')) {
            query << " surConfig.subscription.name in (:subs) "
            queryParams << [subs : params.list('filterSub')]
            params.filterSet = true
        }

        if (params.filterStatus && params.filterStatus != "" && params.list('filterStatus')) {
            query << " surInfo.status.id in (:filterStatus) "
            queryParams << [filterStatus : params.list('filterStatus').collect { Long.parseLong(it) }]
            params.filterSet = true
        }

        if (params.filterPvd && params.filterPvd != "" && params.list('filterPvd')) {
            query << "exists (select orgRole from OrgRole orgRole where orgRole.sub = surConfig.subscription and orgRole.org.id in (:filterPvd))"
            queryParams << [filterPvd : params.list('filterPvd').collect { Long.parseLong(it) }]
            params.filterSet = true
        }

        if (params.participant) {
            query << "" +
                    " exists (select surResult from SurveyResult as surResult where surResult.surveyConfig = surConfig and participant = :participant)"
            queryParams << [participant : params.participant]
        }

        if(params.tab == "created"){
            query << "(surInfo.status in (:status))"
            queryParams << [status: [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY]]
        }

        if(params.tab == "active"){
            query << "surInfo.status = :status"
            queryParams << [status: RDStore.SURVEY_SURVEY_STARTED]
        }

        if(params.tab == "finish"){
            query << "surInfo.status = :status"
            queryParams << [status: RDStore.SURVEY_SURVEY_COMPLETED]
        }

        if(params.tab == "inEvaluation"){
            query << "surInfo.status = :status"
            queryParams << [status: RDStore.SURVEY_IN_EVALUATION]
        }

        if(params.tab == "completed"){
            query << "surInfo.status = :status"
            queryParams << [status: RDStore.SURVEY_COMPLETED]
        }

        String defaultOrder = " order by " + (params.sort ?: " surInfo.endDate DESC") + " " + (params.order ?: "asc")

        /*if (query.size() > 0) {
            result.query = "select surConfig from SurveyConfig surConfig left join surConfig.surveyInfo surInfo where surInfo.owner = :contextOrg and " + query.join(" and ") + defaultOrder
        } else {
            result.query = "select surConfig from SurveyConfig surConfig left join surConfig.surveyInfo surInfo where surInfo.owner = :contextOrg" + defaultOrder
        }
*/
        if (query.size() > 0) {
            result.query = "from SurveyInfo surInfo left join surInfo.surveyConfigs surConfig where surInfo.owner = :contextOrg and " + query.join(" and ") + defaultOrder
        } else {
            result.query = "from SurveyInfo surInfo left join surInfo.surveyConfigs surConfig where surInfo.owner = :contextOrg" + defaultOrder
        }

        queryParams << [contextOrg : contextOrg]


        result.queryParams = queryParams
        result
    }

    Map<String,Object> getParticipantSurveyQuery(Map params, DateFormat sdFormat, Org org) {
        Map result = [:]
        List query = []
        Map<String,Object> queryParams = [:]
        if(params.name) {
            query << "(genfunc_filter_matcher(surInfo.name, :name) = true or (genfunc_filter_matcher(surConfig.subscription.name, :name) = true))"
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

            query << "surveyConfig.surveyInfo.startDate >= :startDate"
            queryParams << [startDate : params.startDate]
        }
        if (params.endDate && sdFormat && !params.currentDate) {

            params.endDate = params.endDate instanceof Date ? params.endDate : sdFormat.parse(params.endDate)

            query << "(surResult.surveyConfig.surveyInfo.endDate <= :endDate or surResult.surveyConfig.surveyInfo.endDate is null)"
            queryParams << [endDate : params.endDate]
        }



        if(params.tab == "new"){
            query << "(surResult.surveyConfig.surveyInfo.status = :status and surResult.id in (select sr.id from SurveyResult sr where sr.surveyConfig  = surveyConfig and sr.dateCreated = sr.lastUpdated and sr.finishDate is null))"
            queryParams << [status: RDStore.SURVEY_SURVEY_STARTED]
        }

        if(params.tab == "processed"){
            query << "(surResult.surveyConfig.surveyInfo.status = :status and surResult.id in (select sr.id from SurveyResult sr where sr.surveyConfig  = surveyConfig and sr.dateCreated < sr.lastUpdated and sr.finishDate is null))"
            queryParams << [status: RDStore.SURVEY_SURVEY_STARTED]
        }

        if(params.tab == "finish"){
            query << "(surResult.finishDate is not null)"
        }

        if(params.tab == "notFinish"){
            query << "((surResult.surveyConfig.surveyInfo.status in (:status)) and surResult.finishDate is null)"
            queryParams << [status: [RDStore.SURVEY_SURVEY_COMPLETED, RDStore.SURVEY_IN_EVALUATION, RDStore.SURVEY_COMPLETED]]
        }

        if(params.consortiaOrg) {
            query << "surResult.owner = :owner"
            queryParams << [owner: params.consortiaOrg]
        }


        String defaultOrder = " order by " + (params.sort ?: " LOWER(surResult.surveyConfig.surveyInfo.name)") + " " + (params.order ?: "asc")

        if (query.size() > 0) {
            result.query = "from SurveyInfo surInfo left join surInfo.surveyConfigs surConfig left join surConfig.surResults surResult  where (surResult.participant = :org)  and " + query.join(" and ") + defaultOrder
        } else {
            result.query = "from SurveyInfo surInfo left join surInfo.surveyConfigs surConfig left join surConfig.surResults surResult where (surResult.participant = :org) " + defaultOrder
        }
        queryParams << [org : org]


        result.queryParams = queryParams
        result
    }

    Map<String,Object> getParticipantSurveyQuery_New(Map params, DateFormat sdFormat, Org org) {
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

        if(params.name) {
            query << "(genfunc_filter_matcher(surInfo.name, :name) = true or (genfunc_filter_matcher(surConfig.subscription.name, :name) = true))"
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
            query << "(exists (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = surConfig AND surOrg.org = :org and surOrg.finishDate is null and surConfig.pickAndChoose = true and surConfig.surveyInfo.status = :status) " +
                    "or exists (select surResult from SurveyResult surResult where surResult.surveyConfig = surConfig and surConfig.surveyInfo.status = :status and surResult.dateCreated = surResult.lastUpdated and surResult.finishDate is null and surResult.participant = :org))"
            queryParams << [status: RDStore.SURVEY_SURVEY_STARTED]
            queryParams << [org : org]
        }

        if(params.tab == "processed"){
            query << "(surInfo.status = :status and exists (select surResult from SurveyResult surResult where surResult.surveyConfig = surConfig and surResult.participant = :org and surResult.dateCreated < surResult.lastUpdated and surResult.finishDate is null))"
            queryParams << [status: RDStore.SURVEY_SURVEY_STARTED]
            queryParams << [org : org]
        }

        if(params.tab == "finish"){
            query << "exists (select surResult from SurveyResult surResult where surResult.surveyConfig = surConfig and surResult.participant = :org and surResult.finishDate is not null) " +
                    "or exists (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = surConfig AND surOrg.org = :org and surOrg.finishDate is not null and surConfig.pickAndChoose = true)"
            queryParams << [org : org]
        }

        if(params.tab == "notFinish"){
            query << "surConfig.subSurveyUseForTransfer = false and ((surInfo.status in (:status)) and exists (select surResult from SurveyResult surResult where surResult.surveyConfig = surConfig and surResult.participant = :org and surResult.finishDate is null))"
            queryParams << [status: [RDStore.SURVEY_SURVEY_COMPLETED, RDStore.SURVEY_IN_EVALUATION, RDStore.SURVEY_COMPLETED]]
            queryParams << [org : org]
        }

        if(params.tab == "termination"){
            query << "surConfig.subSurveyUseForTransfer = true and ((surInfo.status in (:status)) and exists (select surResult from SurveyResult surResult where surResult.surveyConfig = surConfig and surResult.participant = :org and surResult.finishDate is null))"
            queryParams << [status: [RDStore.SURVEY_SURVEY_COMPLETED, RDStore.SURVEY_IN_EVALUATION, RDStore.SURVEY_COMPLETED]]
            queryParams << [org : org]
        }

        if(params.consortiaOrg) {
            query << "surInfo.owner = :owner"
            queryParams << [owner: params.consortiaOrg]
        }


        String defaultOrder = " order by " + (params.sort ?: " LOWER(surInfo.name)") + " " + (params.order ?: "asc")

        if (query.size() > 0) {
            result.query = "from SurveyInfo surInfo left join surInfo.surveyConfigs surConfig where " + query.join(" and ") + defaultOrder
        } else {
            result.query = "from SurveyInfo surInfo left join surInfo.surveyConfigs surConfig " + defaultOrder
        }



        result.queryParams = queryParams
        result
    }

    Map<String,Object> generateBasePackageQuery(params, qry_params, showDeletedTipps, asAt, forBase) {
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
            base_qry += " and ( ( ( :startDate >= coalesce(tipp.accessStartDate, tipp.pkg.startDate) ) or ( tipp.accessEndDate is null ) ) and ( ( :endDate <= tipp.accessEndDate ) or ( tipp.accessEndDate is null ) ) ) "
            qry_params.startDate = asAt
            qry_params.endDate = asAt
            filterSet = true
        }

        if (params.filter) {
            base_qry += " and ( ( genfunc_filter_matcher(tipp.title.title,:title) = true ) or ( exists ( from Identifier ident where ident.ti.id = tipp.title.id and genfunc_filter_matcher(ident.value,:title) = true ) ) )"
            qry_params.title = params.filter
            filterSet = true
        }

        if (params.coverageNoteFilter) {
            base_qry += "and genfunc_filter_matcher(tipp.coverageNote,:coverageNote) = true"
            qry_params.coverageNote = params.coverageNoteFilter
            filterSet = true
        }

        if (params.endsAfter) {
            base_qry += " and tipp.endDate >= :endDate"
            qry_params.endDate = sdf.parse(params.endsAfter)
            filterSet = true
        }

        if (params.startsBefore) {
            base_qry += " and tipp.startDate <= :startDate"
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

        if ((params.sort != null) && (params.sort.length() > 0)) {
            base_qry += " order by lower(${params.sort}) ${params.order}"
        } else {
            base_qry += " order by lower(tipp.title.title) asc"
        }

        return [base_qry:base_qry,qry_params:qry_params,filterSet:filterSet]
    }
}