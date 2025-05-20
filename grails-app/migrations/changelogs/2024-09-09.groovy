package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1725884642950-1") {
        dropIndex(indexName: "doc_lic_idx", tableName: "doc_context")
    }

    changeSet(author: "klober (generated)", id: "1725884642950-2") {
        dropIndex(indexName: "doc_org_idx", tableName: "doc_context")
    }

    changeSet(author: "klober (generated)", id: "1725884642950-3") {
        dropIndex(indexName: "doc_owner_idx", tableName: "doc_context")
    }

    changeSet(author: "klober (generated)", id: "1725884642950-4") {
        dropIndex(indexName: "doc_prov_idx", tableName: "doc_context")
    }

    changeSet(author: "klober (generated)", id: "1725884642950-5") {
        dropIndex(indexName: "doc_sub_idx", tableName: "doc_context")
    }

    changeSet(author: "klober (generated)", id: "1725884642950-6") {
        dropIndex(indexName: "doc_ven_idx", tableName: "doc_context")
    }

    changeSet(author: "klober (generated)", id: "1725884244985-7") {
        createIndex(indexName: "doc_owner_idx", tableName: "doc") {
            column(name: "doc_owner_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1725884244985-8") {
        createIndex(indexName: "dc_doc_idx", tableName: "doc_context") {
            column(name: "dc_doc_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1725884244985-9") {
        createIndex(indexName: "dc_lic_idx", tableName: "doc_context") {
            column(name: "dc_lic_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1725884244985-10") {
        createIndex(indexName: "dc_org_idx", tableName: "doc_context") {
            column(name: "dc_org_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1725884244985-11") {
        createIndex(indexName: "dc_prov_idx", tableName: "doc_context") {
            column(name: "dc_prov_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1725884244985-12") {
        createIndex(indexName: "dc_sub_idx", tableName: "doc_context") {
            column(name: "dc_sub_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1725884244985-13") {
        createIndex(indexName: "dc_ven_idx", tableName: "doc_context") {
            column(name: "dc_ven_fk")
        }
    }
}
