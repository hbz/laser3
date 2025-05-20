package changelogs

databaseChangeLog = {

    changeSet(author: "klober (modified)", id: "1732524050419-1") {
        grailsChange {
            change {
                String query = "update due_date_object set ddo_oid = split_part(ddo_oid, '\$', 1) || ':' || split_part(ddo_oid, ':', 2) where ddo_oid ilike '%HibernateProxy%';"
                sql.execute(query)
                String info = "${query} -> ${sql.getUpdateCount()}"
                confirm(info)
                changeSet.setComments(info)
            }
        }
    }

    changeSet(author: "klober (generated)", id: "1732524050419-2") {
        dropTable(tableName: "elasticsearch_source")
    }

    changeSet(author: "klober (generated)", id: "1732524050419-3") {
        dropTable(tableName: "api_source")
    }
}
