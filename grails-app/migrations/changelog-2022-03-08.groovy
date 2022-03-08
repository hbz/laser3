databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1646746237792-1") {
        createIndex(indexName: "id_lic_idx", tableName: "identifier") {
            column(name: "id_lic_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1646746237792-2") {
        createIndex(indexName: "id_org_idx", tableName: "identifier") {
            column(name: "id_org_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1646746237792-3") {
        createIndex(indexName: "id_pkg_idx", tableName: "identifier") {
            column(name: "id_pkg_fk")
        }
    }

    changeSet(author: "galffy (generated)", id: "1646746237792-4") {
        createIndex(indexName: "id_sub_idx", tableName: "identifier") {
            column(name: "id_sub_fk")
        }
    }
}
