package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1706779428901-1") {
        grailsChange {
            change {
                int updated = sql.executeUpdate("delete from identifier where id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'ezb_collection_id') and id_value !~ 'EZB-[A-Z0-9]{2,6}-\\d{5}';")
                confirm("delete from identifier where id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'ezb_collection_id') and id_value !~ 'EZB-[A-Z0-9]{2,6}-\\d{5}';")
                changeSet.setComments("delete from identifier where id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'ezb_collection_id') and id_value !~ 'EZB-[A-Z0-9]{2,6}-\\d{5}';")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1706779428901-2") {
        grailsChange {
            change {
                int updated = sql.executeUpdate("delete from identifier where id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'ezb_sub_id') and id_value !~ 'EZB-[A-Z0-9]{2,6}-\\d{5}';")
                confirm("delete from identifier where id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'ezb_sub_id') and id_value !~ 'EZB-[A-Z0-9]{2,6}-\\d{5}';")
                changeSet.setComments("delete from identifier where id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'ezb_sub_id') and id_value !~ 'EZB-[A-Z0-9]{2,6}-\\d{5}';")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1706779428901-3") {
        grailsChange {
            change {
                int updated = sql.executeUpdate("delete from identifier where id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'ezb_org_id') and id_value not similar to '[A-Z0-9]{2,6}' and id_value != 'Unknown';")
                confirm("delete from identifier where id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'ezb_org_id') and id_value not similar to '[A-Z0-9]{2,6}' and id_value != 'Unknown';")
                changeSet.setComments("delete from identifier where id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'ezb_org_id') and id_value not similar to '[A-Z0-9]{2,6}' and id_value != 'Unknown';")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1706779428901-4") {
        grailsChange {
            change {
                int updated = sql.executeUpdate("delete from identifier where id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'wibid') and id_value !~ '^(WIB)?\\d{1,4}' and id_value != 'Unknown';")
                confirm("delete from identifier where id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'wibid') and id_value !~ '^(WIB)?\\d{1,4}' and id_value != 'Unknown';")
                changeSet.setComments("delete from identifier where id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'wibid') and id_value !~ '^(WIB)?\\d{1,4}' and id_value != 'Unknown';")
            }
            rollback {}
        }
    }
}
