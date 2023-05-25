databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1684995746509-1") {
        grailsChange {
            change {
                sql.executeUpdate("update property_definition set pd_tenant_fk = null where pd_name = 'Global Consumer Survey Test'")
            }
            rollback {}
        }
    }
}
