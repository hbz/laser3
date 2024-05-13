package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1715584072160-1") {
        grailsChange {
            change {
                String query = "delete from property_definition where pd_name = 'NatStat Supplier ID'"
                sql.execute(query)
                int deleted = sql.getUpdateCount()
                confirm("${query}: ${deleted}")
                changeSet.setComments("${query}: ${deleted}")
            }
            rollback {}
        }
    }

}
