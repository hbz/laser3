package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1749717983219-1") {
        grailsChange {
            change {
                String query = "delete from customer_identifier a using customer_identifier b where a.cid_platform_fk = b.cid_platform_fk and a.cid_customer_fk = b.cid_customer_fk and a.cid_id < b.cid_id"
                int cnt = sql.executeUpdate(query)
                confirm("${query}: ${cnt} duplicates deleted")
                changeSet.setComments("${query}: ${cnt} duplicates deleted")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (generated)", id: "1749717983219-2") {
        addUniqueConstraint(columnNames: "cid_customer_fk, cid_platform_fk", constraintName: "UKba7e04ae630f5879b0b794d7e2ef", tableName: "customer_identifier")
    }

}
