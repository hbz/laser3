package de.laser

import com.k_int.kbplus.License
import com.k_int.kbplus.Org
import com.k_int.kbplus.RefdataValue
import com.k_int.kbplus.Subscription
import com.k_int.kbplus.auth.User
import de.laser.helper.RDStore
import java.text.DateFormat

class FilterService {

    private static final Long FAKE_CONSTRAINT_ORGID_WITHOUT_HITS = new Long(-1)
    def springSecurityService
    def genericOIDService
    def contextService

    Map<String, Object> getOrgQuery(Map params) {
        Map<String, Object> result = [:]
        ArrayList<String> query = ["(o.status is null or o.status != :orgStatus)"]
        Map<String, Object> queryParams = ["orgStatus" : RDStore.ORG_DELETED]

        if (params.orgNameContains?.length() > 0) {
            query << "(lower(o.name) like :orgNameContains1 or lower(o.shortname) like :orgNameContains2 or lower(o.sortname) like :orgNameContains3)"
             queryParams << [orgNameContains1  : "%${params.orgNameContains.toLowerCase()}%"]
             queryParams << [orgNameContains2  : "%${params.orgNameContains.toLowerCase()}%"]
             queryParams << [orgNameContains3 : "%${params.orgNameContains.toLowerCase()}%"]
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
        if (params.federalState?.length() > 0) {
            query << "o.federalState.id = :federalState"
             queryParams << [federalState : Long.parseLong(params.federalState)]
        }
        if (params.libraryNetwork?.length() > 0) {
            query << "o.libraryNetwork.id = :libraryNetwork"
             queryParams << [libraryNetwork : Long.parseLong(params.libraryNetwork)]
        }
        if (params.libraryType?.length() > 0) {
            query << "o.libraryType.id = :libraryType"
             queryParams << [libraryType : Long.parseLong(params.libraryType)]
        }
        if (params.country?.length() > 0) {
            query << "o.country.id = :country"
             queryParams << [country : Long.parseLong(params.country)]
        }

        // hack: applying filter on org subset
        if (params.containsKey("constraint_orgIds") && params.constraint_orgIds?.size() < 1) {
            query << "o.id = :emptyConstraintOrgIds"
             queryParams << [emptyConstraintOrgIds : FAKE_CONSTRAINT_ORGID_WITHOUT_HITS]
        }else if (params.constraint_orgIds?.size() > 0) {
            query << "o.id in ( :constraint_orgIds )"
             queryParams << [constraint_orgIds : params.constraint_orgIds]
        }

        def defaultOrder = " order by " + (params.sort ?: " LOWER(o.name)") + " " + (params.order ?: "asc")

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
        Map<String, Object> queryParams = ["orgStatus" : RDStore.ORG_DELETED]

        if (params.orgNameContains?.length() > 0) {
            query << "(lower(o.name) like :orgNameContains1 or lower(o.shortname) like :orgNameContains2 or lower(o.sortname) like :orgNameContains3)"
             queryParams << [orgNameContains1 : "%${params.orgNameContains.toLowerCase()}%"]
             queryParams << [orgNameContains2 : "%${params.orgNameContains.toLowerCase()}%"]
             queryParams << [orgNameContains3 : "%${params.orgNameContains.toLowerCase()}%"]
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
        if (params.federalState?.length() > 0) {
            query << "o.federalState.id = :federalState"
             queryParams << [federalState : Long.parseLong(params.federalState)]
        }
        if (params.libraryNetwork?.length() > 0) {
            query << "o.libraryNetwork.id = :libraryNetwork"
             queryParams << [libraryNetwork : Long.parseLong(params.libraryNetwork)]
        }
        if (params.libraryType?.length() > 0) {
            query << "o.libraryType.id = :libraryType"
             queryParams << [libraryType : Long.parseLong(params.libraryType)]
        }

         queryParams << [org : org]
         queryParams << [consortium : 'Consortium']

        String defaultOrder = " order by " + (params.sort && (!params.consSort && !params.ownSort && !params.subscrSort) ?: " LOWER(o.sortname)") + " " + (params.order && (!params.consSort && !params.ownSort && !params.subscrSort) ?: "asc")

        if (query.size() > 0) {
            result.query = "select o from Org as o, Combo as c where " + query.join(" and ") + " and c.fromOrg = o and c.toOrg = :org and c.type.value = :consortium " + defaultOrder
        } else {
            result.query = "select o from Org as o, Combo as c where c.fromOrg = o and c.toOrg = :org and c.type.value = :consortium " + defaultOrder
        }
        result.queryParams = queryParams
        result
    }

    Map<String, Object> getTaskQuery(Map params, DateFormat sdFormat) {
        Map<String, Object> result = [:]
        def query = []
        Map<String, Object> queryParams = [:]

        if (params.taskName) {
            query << "lower(t.title) like :taskName"
            queryParams << [taskName : "%${params.taskName.toLowerCase()}%"]
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

    Map<String,Object> getSurveyQuery(Map params, DateFormat sdFormat, Org contextOrg) {
        Map result = [:]
        List query = []
        Map<String,Object> queryParams = [:]
        if(params.name) {
            query << "lower(si.name) like lower(:name)"
            queryParams << [name:"%${params.name}%"]
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

        def defaultOrder = " order by " + (params.sort ?: " LOWER(si.name)") + " " + (params.order ?: "asc")

        if (query.size() > 0) {
            result.query = "from SurveyInfo si where si.owner = :contextOrg and " + query.join(" and ") + defaultOrder
        } else {
            result.query = "from SurveyInfo si where si.owner = :contextOrg" + defaultOrder
        }
        queryParams << [contextOrg : contextOrg]


        result.queryParams = queryParams
        result
    }
}