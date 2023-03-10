package changelogs

databaseChangeLog = {

    changeSet(author: "djebeniani (hand-coded)", id: "1678181832490-1") {
        grailsChange {
            change {
                sql.execute('''INSERT INTO role (r_id, r_authority, r_role_type, r_authority_de, r_authority_en) VALUES (DEFAULT, 'ORG_CONSORTIUM_PRO', 'org', null, null);''')
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1678181832490-2") {
        grailsChange {
            change {
                sql.execute('''UPDATE org_setting set os_role_fk = (SELECT r_id from role WHERE r_authority = 'ORG_CONSORTIUM_PRO') where os_role_fk = (SELECT r_id from role WHERE r_authority = 'ORG_CONSORTIUM');''')
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1678181832490-3") {
        grailsChange {
            change {
                sql.execute('''DELETE from identifier where id_ns_fk = (SELECT idns_id from identifier_namespace where idns_ns = 'GRID ID');''')
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1678181832490-4") {
        grailsChange {
            change {
                sql.execute('''DELETE from identifier_namespace where idns_ns = 'GRID ID';''')
            }
            rollback {}
        }
    }

}
