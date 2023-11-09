package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1699515173171-1") {
        grailsChange {
            change {
                int del = sql.executeUpdate("delete from org_setting where os_key_enum = 'EZB_SERVER_ACCESS'")
                confirm("delete from org_setting where os_key_enum = 'EZB_SERVER_ACCESS': ${del}")
                changeSet.setComments("delete from org_setting where os_key_enum = 'EZB_SERVER_ACCESS': ${del}")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1699515173171-2") {
        grailsChange {
            change {
                int upd = sql.executeUpdate("update identifier set id_value = upper(id_value) where id_org_fk is not null and id_value not in ('Unknown','') and id_value ~ '[a-z]' and id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'ezb_org_id')")
                confirm("update identifier set id_value = upper(id_value) where id_org_fk is not null and id_value not in ('Unknown','') and id_value ~ '[a-z]' and id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'ezb_org_id'): ${upd}")
                changeSet.setComments("update identifier set id_value = upper(id_value) where id_org_fk is not null and id_value not in ('Unknown','') and id_value ~ '[a-z]' and id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'ezb_org_id'): ${upd}")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1699515173171-3") {
        grailsChange {
            change {
                int upd = sql.executeUpdate("update identifier set id_value = split_part(id_value,'=',2) where id_org_fk is not null and id_value not in ('Unknown','') and id_value ~ '=' and id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'ezb_org_id')")
                confirm("update identifier set id_value = split_part(id_value,'=',2) where id_org_fk is not null and id_value not in ('Unknown','') and id_value ~ '=' and id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'ezb_org_id'): ${upd}")
                changeSet.setComments("update identifier set id_value = split_part(id_value,'=',2) where id_org_fk is not null and id_value not in ('Unknown','') and id_value ~ '=' and id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'ezb_org_id'): ${upd}")
            }
            rollback {}
        }
    }
}
