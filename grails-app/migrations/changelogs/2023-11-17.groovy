package changelogs

databaseChangeLog = {

    changeSet(author: "klober (modified)", id: "1700210103258-1") {
        grailsChange {
            change {
                sql.executeUpdate("""
delete from person_role where pr_responsibility_type_rv_fk is not null and pr_responsibility_type_rv_fk = (
    select rdv_id from refdata_value where rdv_value = 'Specific title editor'
) """)
                sql.executeUpdate("delete from refdata_value where rdv_value = 'Specific title editor'")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1700210103258-2") {
        grailsChange {
            change {
                sql.executeUpdate("""
delete from identifier where id_ns_fk is not null and id_ns_fk in (
    select idns_id from identifier_namespace where idns_type = 'de.laser.titles.TitleInstance'
) """)
                sql.executeUpdate("delete from identifier_namespace where idns_type = 'de.laser.titles.TitleInstance'")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (generated)", id: "1700210103258-3") {
        dropForeignKeyConstraint(baseTableName: "title_instance", constraintName: "fkacc69c66d9594e")
    }

    changeSet(author: "klober (generated)", id: "1700210103258-4") {
        dropForeignKeyConstraint(baseTableName: "title_instance", constraintName: "fkacc69cbfcf2a91")
    }

    changeSet(author: "klober (generated)", id: "1700210103258-5") {
        dropUniqueConstraint(constraintName: "title_instance_ti_guid_key", tableName: "title_instance")
    }

    changeSet(author: "klober (generated)", id: "1700210103258-6") {
        dropTable(tableName: "title_instance")
    }
}
