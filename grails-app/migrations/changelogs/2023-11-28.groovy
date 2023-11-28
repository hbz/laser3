databaseChangeLog = {

    changeSet(author: "klober (modified)", id: "1701155645364-1") {
        grailsChange {
            change {
                sql.executeUpdate("delete from system_profiler where sp_archive = '3.1'")
                sql.executeUpdate("delete from system_profiler where sp_archive = '3.3-RC'")
            }
            rollback {}
        }
    }
}
