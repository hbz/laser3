databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1621339560645-1") {
        grailsChange {
            change {
                sql.execute("INSERT INTO global_record_source (grs_id, grs_version, grs_active, grs_creds, grs_full_prefix, grs_have_up_to, grs_identifier, grs_list_prefix, grs_name, grs_principal, grs_rectype, grs_type, grs_uri, grs_date_created, grs_last_updated, grs_edit_uri) VALUES" +
                        "(2, 1, true, NULL, NULL, '2021-01-01 00:00:00', 'we:kb Phaeton', 'oai_dc', 'we:kb Phaeton', NULL, 2, 'JSON', 'https://wekb.hbz-nrw.de/api/', now(), now(), 'https://wekb.hbz-nrw.de/api/')")
            }
            rollback {}
        }
    }

}
