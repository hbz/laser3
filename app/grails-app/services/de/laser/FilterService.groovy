package de.laser

import com.k_int.kbplus.Org
import com.k_int.kbplus.RefdataValue
import com.k_int.kbplus.auth.User
import de.laser.helper.RDStore
import java.text.DateFormat

class FilterService {

    private static final Long FAKE_CONSTRAINT_ORGID_WITHOUT_HITS = new Long(-1)
    def springSecurityService

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

        String defaultOrder = " order by " + (params.sort ?: " LOWER(o.sortname)") + " " + (params.order ?: "asc")

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
        if(params.title) {
            query << "lower(d.title) like lower(:title)"
            queryParams << [title:"%${params.title}%"]
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
        if(params.docOwnerOrg) {
            query << "d.owner = :owner"
            queryParams << [owner: Org.get(params.docOwnerOrg)]
        }
        if(params.docTargetOrg) {
            query << "dc.targetOrg = :target"
            queryParams << [target: Org.get(params.docTargetOrg)]
        }
        if(params.docShareConf) {
            query << "dc.shareConf = :shareConf"
            queryParams << [shareConf: RefdataValue.get(params.docShareConf)]
        }
        if(query.size() > 0)
            result.query = " and "+query.join(" and ")
        else result.query = ""
        result.queryParams = queryParams
        result
    }
}