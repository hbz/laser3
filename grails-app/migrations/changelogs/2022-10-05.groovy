package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1664951418950-1") {
        grailsChange {
            change {
                sql.execute("delete from identifier where id_id in (select id_id from (select id_id, id_value, row_number() over (partition by id_ns_fk, id_org_fk order by id_id desc) as row_num from identifier join identifier_namespace on id_ns_fk = idns_id where idns_ns = 'wibid') ident where ident.row_num > 1)")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1664951418950-2") {
        grailsChange {
            change {
                sql.execute("update identifier set id_value = concat('WIB',(regexp_match(id_value, '\\d{4}'))[1]) from identifier_namespace where id_ns_fk = idns_id and idns_ns = 'wibid' and id_value !~ 'WIB\\d{4}' and (id_value ~ '\\d{4}' or id_value ~ 'WIB-\\d{4}')")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1664951418950-3") {
        grailsChange {
            change {
                sql.execute("delete from identifier where id_id in (select id_id from (select id_id, id_value, row_number() over (partition by id_ns_fk, id_org_fk order by id_id desc) as row_num from identifier join identifier_namespace on id_ns_fk = idns_id where idns_ns = 'VAT') ident where ident.row_num > 1)")
            }
            rollback {

            }
        }
    }
}
