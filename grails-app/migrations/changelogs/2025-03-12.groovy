package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1741770494281-1") {
        grailsChange {
            change {
                String query = "delete from identifier where id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'emerald' and idns_type = 'de.laser.Subscription')"
                sql.execute(query)
                String info = "${query}: ${sql.getUpdateCount()}"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1741770494281-2") {
        grailsChange {
            change {
                String query = "delete from identifier_namespace where idns_ns = 'emerald' and idns_type = 'de.laser.Subscription'"
                sql.execute(query)
                String info = "${query}: ${sql.getUpdateCount()}"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1741770494281-3") {
        grailsChange {
            change {
                String query = "update property_definition set pd_name = 'Tax rate' where pd_name = 'Steuersatz' and pd_tenant_fk is null"
                sql.execute(query)
                String info = "${query}: ${sql.getUpdateCount()}"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

    //exceptional because of separation at-once
    changeSet(author: "galffy (hand-coded)", id: "1741770494281-4") {
        grailsChange {
            change {
                String query = "INSERT INTO identifier_namespace (" +
                        "idns_version, idns_family, idns_is_hidden, idns_ns, idns_type, idns_val_regex, idns_is_unique, idns_date_created, idns_last_updated, idns_description_de, idns_description_en, idns_name_de, idns_name_en, idns_url_prefix, idns_last_updated_cascading, idns_is_from_laser, idns_is_hard_data) VALUES (" +
                        "'1', NULL, false, 'ezb_lic_anchor', 'de.laser.License', NULL, false, now(), now(), 'EZB-Anker werden von Konsortialverwaltern für deren jeweilige angelegte Kollektionen vergeben, um so die genaue Lizenzeinheit abbilden zu können'::text, 'EZB anchors are distributed by consortia managers for their respective collections in order to represent the subscription unit', 'EZB-Anker', 'EZB-Anker', NULL, now(), true, true)"
                sql.execute(query)
                String info = "${query}: ${sql.getUpdateCount()}"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1741770494281-5") {
        grailsChange {
            change {
                String query = "update identifier set id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'ezb_lic_anchor') where id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'ezb_anchor') and id_lic_fk is not null"
                sql.execute(query)
                String info = "${query}: ${sql.getUpdateCount()}"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1741770494281-6") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = 'de.laser.Subscription' where idns_ns = 'ezb_anchor'"
                sql.execute(query)
                String info = "${query}: ${sql.getUpdateCount()}"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1741770494281-7") {
        grailsChange {
            change {
                String query = "update property_definition set pd_type = 'java.util.Date' where pd_name = 'Access last checked' and pd_tenant_fk is null"
                sql.execute(query)
                String info = "${query}: ${sql.getUpdateCount()}"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }
}
