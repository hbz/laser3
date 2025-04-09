package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1744180845795-1") {
        grailsChange {
            change {
                String query = "truncate table provider_link"
                sql.execute(query)
                String info = "${query} -> ${sql.getUpdateCount()}"
                confirm(info)
                changeSet.setComments(info)
            }
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1744180845795-2") {
        grailsChange {
            change {
                String query = "truncate table vendor_link"
                sql.execute(query)
                String info = "${query} -> ${sql.getUpdateCount()}"
                confirm(info)
                changeSet.setComments(info)
            }
        }
    }
}
