package de.laser.reporting.export

import de.laser.reporting.myInstitution.base.BaseQuery

class QueryExport {

    String token

    QueryExport(String token) {
        this.token = token
    }

    List<Map<String, Object>> getDataDetails() {
        BaseQuery.getQueryCache( token ).get('dataDetails') as List ?: []
    }
}
