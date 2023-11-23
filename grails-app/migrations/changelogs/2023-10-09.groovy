package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1696849903996-1") {
        grailsChange {
            change {
                sql.execute("insert into global_record_source (grs_version, grs_active, grs_creds, grs_full_prefix, grs_have_up_to, grs_identifier, grs_list_prefix, grs_name, grs_principal, grs_rectype, grs_type, grs_uri, grs_date_created, grs_last_updated, grs_edit_uri) VALUES (1, true, null, null, '1970-01-01 00:00:00', 'WEKB Phaeton', 'oai_dc', 'WEKB Phaeton', null, 4, 'JSON', 'https://wekb.hbz-nrw.de/api2/', now(), now(), 'https://wekb.hbz-nrw.de/api2/')")
            }
            rollback {}
        }
    }
}
