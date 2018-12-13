package de.laser

import com.k_int.properties.PropertyDefinition

class SelectListQueryService {

    String buildSubscriptionList(consortia, subscriptions) {
        String subscrQuery
        String existsClause = "exists ( select o from s.orgRelations as o where (o.roleType.value IN ('Subscriber', 'Subscription Consortia')) and o.org = :co) )"
        String andClause1 = "AND ( LOWER(s.status.value) != 'deleted' )"
        if(consortia) {
            existsClause = "exists ( select o from s.orgRelations as o where (o.roleType.value IN ('Subscription Consortia')) and o.org = :co) )"
            andClause1 = "AND ( LOWER(s.status.value) != 'deleted' AND (s.instanceOf is null)"
        }
        String filter = ""
        //to deploy into general filter function
        if(subscriptions) {
            StringJoiner sj = new StringJoiner("') AND (s.identifier != '")
            subscriptions.each { s ->
                sj.add(s.identifier)
            }
            filter = " AND (s.identifier != '"+sj.toString()+"')"
        }
        subscrQuery = """select s from Subscription as s where ("""+existsClause+andClause1+filter+""")"""
        subscrQuery
    }
}
