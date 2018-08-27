package de.laser

import com.k_int.kbplus.RefdataValue

class FilterService {

    def springSecurityService

    def getOrgQuery(params) {
        def result = [:]
        def query = []
        def queryParams = []

        if (params.orgNameContains?.length() > 0) {
            query << "lower(o.name) like ?"
            queryParams << "%${params.orgNameContains.toLowerCase()}%"
        }
        if (params.orgType?.length() > 0) {
            query << "o.orgType.id = ?"
            queryParams << Long.parseLong(params.orgType)
        }
        if (params.orgSector?.length() > 0) {
            query << "o.sector.id = ?"
            queryParams << Long.parseLong(params.orgSector)
        }
        if (params.federalState?.length() > 0) {
            query << "o.federalState.id = ?"
            queryParams << Long.parseLong(params.federalState)
        }
        if (params.libraryNetwork?.length() > 0) {
            query << "o.libraryNetwork.id = ?"
            queryParams << Long.parseLong(params.libraryNetwork)
        }
        if (params.libraryType?.length() > 0) {
            query << "o.libraryType.id = ?"
            queryParams << Long.parseLong(params.libraryType)
        }
        if (params.country?.length() > 0) {
            query << "o.country.id = ?"
            queryParams << Long.parseLong(params.country)
        }

        if (query.size() > 0) {
            query = "from Org o where " + query.join(" and ") + " order by LOWER(o.name) asc"
        } else {
            query = "from Org o order by LOWER(o.name) asc"
        }

        result.query = query
        result.queryParams = queryParams

        result
    }

    def getOrgComboQuery(params, org) {
        def result = [:]
        def query = []
        def queryParams = []

        if (params.orgNameContains?.length() > 0) {
            query << "lower(o.name) like ?"
            queryParams << "%${params.orgNameContains.toLowerCase()}%"
        }
        if (params.orgType?.length() > 0) {
            query << "o.orgType.id = ?"
            queryParams << Long.parseLong(params.orgType)
        }
        if (params.orgSector?.length() > 0) {
            query << "o.sector.id = ?"
            queryParams << Long.parseLong(params.orgSector)
        }
        if (params.federalState?.length() > 0) {
            query << "o.federalState.id = ?"
            queryParams << Long.parseLong(params.federalState)
        }
        if (params.libraryNetwork?.length() > 0) {
            query << "o.libraryNetwork.id = ?"
            queryParams << Long.parseLong(params.libraryNetwork)
        }
        if (params.libraryType?.length() > 0) {
            query << "o.libraryType.id = ?"
            queryParams << Long.parseLong(params.libraryType)
        }

        queryParams << org
        queryParams << 'Consortium'

        def defaultOrder = " order by " + (params.sort ?: " LOWER(o.sortname) ") + (params.order ?: " asc")

        if (query.size() > 0) {
            query = "select o from Org as o, Combo as c where " + query.join(" and ") + " and c.fromOrg = o and c.toOrg = ? and c.type.value = ? " + defaultOrder
        } else {
            query = "select o from Org as o, Combo as c where c.fromOrg = o and c.toOrg = ? and c.type.value = ? " + defaultOrder
        }

        result.query = query
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

        def defaultOrder = " order by " + (params.sort ?: "t.endDate") + (params.order ?: " desc")

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