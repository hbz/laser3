package de.laser

import com.k_int.kbplus.Org
import com.k_int.kbplus.RefdataCategory
import com.k_int.kbplus.RefdataValue
import grails.util.Holders
import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib
import org.codehaus.groovy.grails.web.util.WebUtils

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

        if (query.size() > 0) {
            query = "select o from Org as o, Combo as c where " + query.join(" and ") + " and c.fromOrg = o and c.toOrg = ? and c.type.value = ? order by LOWER(o.name) asc"
        } else {
            query = "select o from Org as o, Combo as c where c.fromOrg = o and c.toOrg = ? and c.type.value = ? order by LOWER(o.name) asc"
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

        if (query.size() > 0) {
            query = " and " + query.join(" and ") + " order by t.endDate desc"
        } else {
            query = " order by t.endDate desc"
        }

        result.query = query
        result.queryParams = queryParams

        result
    }
}