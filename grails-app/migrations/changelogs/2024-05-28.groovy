package changelogs

databaseChangeLog = {

    changeSet(author: "djebeniani (modified)", id: "1716910938586-1") {
        grailsChange {
            change {
                sql.execute("update cost_item set ci_pkg_fk = (select sp_pkg_fk from subscription_package where sp_id = ci_sub_pkg_fk) where ci_sub_pkg_fk is not null")
                String info = "cost_item set ci_pkg_fk from ci_sub_pkg_fk: ${sql.getUpdateCount()}"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }


}
