package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1651744084959-1") {
        addColumn(tableName: "role") {
            column(name: "authority_de", type: "varchar(255)")
        }
    }

    changeSet(author: "klober (generated)", id: "1651744084959-2") {
        addColumn(tableName: "role") {
            column(name: "authority_en", type: "varchar(255)")
        }
    }

    changeSet(author: "klober (modified)", id: "1651744084959-3") {
        grailsChange {
            change {
                sql.execute("delete from i10n_translation where i10n_reference_class='de.laser.auth.Role' and i10n_reference_field='authority'")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1651744084959-4") {
        grailsChange {
            change {
                sql.execute("delete from perm where code in ('org_inst_collective', 'org_consortium_survey')")
            }
            rollback {}
        }
    }
}
