package de.laser

import com.k_int.kbplus.Org
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
            query = "from Org o where " + query.join(" and ") + " order by o.name"
        } else {
            query = "from Org o order by o.name"
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
            query = "select o from Org as o, Combo as c where " + query.join(" and ") + " and c.fromOrg = o and c.toOrg = ? and c.type.value = ? order by o.name"
        } else {
            query = "select o from Org as o, Combo as c where c.fromOrg = o and c.toOrg = ? and c.type.value = ? order by o.name"
        }

        result.query = query
        result.queryParams = queryParams

        result
    }
}