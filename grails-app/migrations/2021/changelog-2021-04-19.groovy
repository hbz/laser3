databaseChangeLog = {

    changeSet(author: "galffy (modified)", id: "1618826469327-1") {
        addColumn(tableName: "identifier_namespace") {
            column(name: "idns_is_hard_data", type: "boolean")
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1618826469327-2") {
        grailsChange {
            change {
                sql.execute("update identifier_namespace set idns_is_hard_data = false;")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1618826469327-3") {
        grailsChange {
            change {
                sql.execute("ALTER TABLE identifier_namespace ALTER COLUMN idns_is_hard_data SET NOT NULL;")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1618826469327-4") {
        addNotNullConstraint(columnDataType: "bool", columnName: "idns_is_hard_data", tableName: "identifier_namespace")
    }

    changeSet(author: "galffy (hand-coded)", id: "1618826469327-5") {
        grailsChange {
            change {
                sql.execute("update identifier_namespace set idns_ns = 'EZB anchor', idns_name_de = 'EZB Anker', idns_is_from_laser = true where idns_ns = 'EZB_anchor';")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (generated)", id: "1618826469327-6") {
        dropColumn(columnName: "pkg_license_fk", tableName: "package")
    }

}
