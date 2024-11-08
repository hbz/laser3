package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1731051888385-1") {
        dropForeignKeyConstraint(baseTableName: "org", constraintName: "FKk3uxe03uscg8ifybv8w6r4lvd")
    }

    changeSet(author: "klober (generated)", id: "1731051888385-2") {
        dropColumn(columnName: "org_type_rv_fk", tableName: "org")
    }

    changeSet(author: "klober (modified)", id: "1731051888385-3") {
        grailsChange {
            change {
                sql.execute("delete from refdata_value where rdv_value != 'Institution' and rdv_value != 'Consortium' and rdv_owner = (select rdc_id from refdata_category where rdc_description = 'org.type')")
                confirm("> refdataValues from 'org.type' removed: ${sql.getUpdateCount()}")
            }
            rollback {}
        }
    }
}
