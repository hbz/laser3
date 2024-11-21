package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1732194826494-1") {
        grailsChange {
            change {
                String query = "update org set org_url = 'http://' || org_url where org_url ~ '^www' and not (org_url ilike '%,%')"
                sql.execute(query)
                String info = "${query}: ${sql.getUpdateCount()}"
                confirm(info)
                changeSet.setComments(info)
            }
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1732194826494-2") {
        grailsChange {
            change {
                String query = "update org set org_url_gov = 'http://' || org_url_gov where org_url_gov ~ '^www'"
                sql.execute(query)
                String info = "${query}: ${sql.getUpdateCount()}"
                confirm(info)
                changeSet.setComments(info)
            }
        }
    }
}
