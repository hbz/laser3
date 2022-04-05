databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1618998547259-1") {
        grailsChange {
            change {
                sql.execute("delete from identifier where id_pkg_fk is not null and id_ns_fk in (select idns_id from identifier_namespace where idns_type is null or idns_type != 'de.laser.Package');")
            }
            rollback {}
        }
    }

}
