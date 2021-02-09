databaseChangeLog = {

    changeSet(author: "agalffy (modified)", id: "1612343883821-1") {
        grailsChange {
            change {
                sql.execute('ALTER TABLE pending_change_configuration RENAME pcc_with_information TO pcc_with_notification;')
            }
            rollback {}
        }
    }

    changeSet(author: "agalffy (generated)", id: "1612343883821-2") {
        createIndex(indexName: "pending_change_owner_idx", tableName: "pending_change") {
            column(name: "pc_owner")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1612345033366-3") {
        createIndex(indexName: "ic_ie_idx", tableName: "issue_entitlement_coverage") {
            column(name: "ic_ie_fk")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1612345033366-4") {
        createIndex(indexName: "pcc_sp_idx", tableName: "pending_change_configuration") {
            column(name: "pcc_sp_fk")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1612345033366-5") {
        createIndex(indexName: "pi_ie_idx", tableName: "price_item") {
            column(name: "pi_ie_fk")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1612345033366-6") {
        createIndex(indexName: "pi_list_currency_idx", tableName: "price_item") {
            column(name: "pi_list_currency_rv_fk")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1612345033366-7") {
        createIndex(indexName: "pi_local_currency_idx", tableName: "price_item") {
            column(name: "pi_local_currency_rv_fk")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1612345033366-8") {
        createIndex(indexName: "pi_tipp_idx", tableName: "price_item") {
            column(name: "pi_tipp_fk")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1612345033366-9") {
        createIndex(indexName: "tc_tipp_idx", tableName: "tippcoverage") {
            column(name: "tc_tipp_fk")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1612345033366-10") {
        createIndex(indexName: "tipp_idx", tableName: "title_instance_package_platform") {
            column(name: "tipp_plat_fk")

            column(name: "tipp_pkg_fk")
        }
    }

}
