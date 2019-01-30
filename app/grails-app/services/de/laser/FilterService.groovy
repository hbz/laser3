package de.laser

import com.k_int.kbplus.Org
import com.k_int.kbplus.RefdataValue
import de.laser.helper.RDStore

class FilterService {

    private static final Long FAKE_CONSTRAINT_ORGID_WITHOUT_HITS = new Long(-1)
    def springSecurityService

    Map<String, Object> getOrgQuery(Map params) {
        Map<String, Object> result = [:]
        ArrayList<String> query = ["(o.status is null or o.status != :orgStatus)"]
        Map<String, Object> queryParams = ["orgStatus" : RDStore.O_DELETED]

        if (params.orgNameContains?.length() > 0) {
            query << "(lower(o.name) like :orgNameContains1 or lower(o.shortname) like :orgNameContains2 or lower(o.sortname) like :orgNameContains3)"
            queryParams.put("orgNameContains1", "%${params.orgNameContains.toLowerCase()}%")
            queryParams.put("orgNameContains2", "%${params.orgNameContains.toLowerCase()}%")
            queryParams.put("orgNameContains3", "%${params.orgNameContains.toLowerCase()}%")
        }
        if (params.orgRoleType?.length() > 0) {
            query << " exists (select roletype from o.orgRoleType as roletype where roletype.id = :orgRoleType )"
            queryParams.put("orgRoleType", Long.parseLong(params.orgRoleType))
        }
        if (params.orgRole?.length() > 0) {
            query << " exists (select ogr from o.links as ogr where ogr.roleType.id = :orgRole )"
            queryParams.put("orgRole", Long.parseLong(params.orgRole))
        }
        if (params.orgSector?.length() > 0) {
            query << "o.sector.id = :orgSector"
            queryParams.put("orgSector", Long.parseLong(params.orgSector))
        }
        if (params.federalState?.length() > 0) {
            query << "o.federalState.id = :federalState"
            queryParams.put("federalState", Long.parseLong(params.federalState))
        }
        if (params.libraryNetwork?.length() > 0) {
            query << "o.libraryNetwork.id = :libraryNetwork"
            queryParams.put("libraryNetwork", Long.parseLong(params.libraryNetwork))
        }
        if (params.libraryType?.length() > 0) {
            query << "o.libraryType.id = :libraryType"
            queryParams.put("libraryType", Long.parseLong(params.libraryType))
        }
        if (params.country?.length() > 0) {
            query << "o.country.id = :country"
            queryParams.put("country", Long.parseLong(params.country))
        }

        // hack: applying filter on org subset
        if (params.containsKey("constraint_orgIds") && params.constraint_orgIds?.size() < 1) {
            query << "o.id = :emptyConstraintOrgIds"
            queryParams.put("emptyConstraintOrgIds", FAKE_CONSTRAINT_ORGID_WITHOUT_HITS)
        }else if (params.constraint_orgIds?.size() > 0) {
            query << "o.id in ( :constraint_orgIds )"
            queryParams.put("constraint_orgIds", params.constraint_orgIds)
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
        Map<String, Object> queryParams = ["orgStatus" : RDStore.O_DELETED]

        if (params.orgNameContains?.length() > 0) {
            query << "(lower(o.name) like :orgNameContains1 or lower(o.shortname) like :orgNameContains2 or lower(o.sortname) like :orgNameContains3)"
            queryParams.put("orgNameContains1", "%${params.orgNameContains.toLowerCase()}%")
            queryParams.put("orgNameContains2", "%${params.orgNameContains.toLowerCase()}%")
            queryParams.put("orgNameContains3", "%${params.orgNameContains.toLowerCase()}%")
        }
       /* if (params.orgType?.length() > 0) {
            query << "o.orgType.id = ?"
            queryParams << Long.parseLong(params.orgType)
        }*/
        if (params.orgRoleType?.length() > 0) {
            query << " exists (select roletype from o.orgRoleType as roletype where roletype.id = :orgRoleType )"
            queryParams.put("orgRoleType", Long.parseLong(params.orgRoleType))
        }
        if (params.orgSector?.length() > 0) {
            query << "o.sector.id = :orgSector"
            queryParams.put("orgSector", Long.parseLong(params.orgSector))
        }
        if (params.federalState?.length() > 0) {
            query << "o.federalState.id = :federalState"
            queryParams.put("federalState", Long.parseLong(params.federalState))
        }
        if (params.libraryNetwork?.length() > 0) {
            query << "o.libraryNetwork.id = :libraryNetwork"
            queryParams.put("libraryNetwork", Long.parseLong(params.libraryNetwork))
        }
        if (params.libraryType?.length() > 0) {
            query << "o.libraryType.id = :libraryType"
            queryParams.put("libraryType", Long.parseLong(params.libraryType))
        }

        queryParams.put("org", org)
        queryParams.put("consortium", 'Consortium')

        String defaultOrder = " order by " + (params.sort ?: " LOWER(o.sortname)") + " " + (params.order ?: "asc")

        if (query.size() > 0) {
            result.query = "select o from Org as o, Combo as c where " + query.join(" and ") + " and c.fromOrg = o and c.toOrg = :org and c.type.value = :consortium " + defaultOrder
        } else {
            result.query = "select o from Org as o, Combo as c where c.fromOrg = o and c.toOrg = :org and c.type.value = :consortium " + defaultOrder
        }
        result.queryParams = queryParams
        result
    }

    def getTaskQuery(params, sdFormat) {
        def result = [:]
        def query = []
        def queryParams = []

        if (params.taskName?.length() > 0) {
            query << "lower(t.title) like ?"
            queryParams << "%${params.taskName.toLowerCase()}%"
        }
        if (params.taskStatus?.length() > 0) {
            if (params.taskStatus == 'not done') {
                query << "t.status.id != ?"
                queryParams << RefdataValue.getByValueAndCategory('Done', 'Task Status')?.id
            }
            else {
                query << "t.status.id = ?"
                queryParams << Long.parseLong(params.taskStatus)
            }
        }
        if (params.endDateFrom?.length() > 0 && sdFormat) {
            query << "t.endDate >= ?"
            queryParams << sdFormat.parse(params.endDateFrom)
        }
        if (params.endDateTo?.length() > 0 && sdFormat) {
            query << "t.endDate <= ?"
            queryParams << sdFormat.parse(params.endDateTo)
        }

        def defaultOrder = " order by " + (params.sort ?: "t.endDate") + " " + (params.order ?: "desc")

        if (query.size() > 0) {
            query = " and " + query.join(" and ") + defaultOrder
        } else {
            query = defaultOrder
        }
        result.query = query
        result.queryParams = queryParams

        result
    }
}