package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1681286344491-1") {
        grailsChange {
            change {
                sql.execute("INSERT INTO global_record_source (grs_version, grs_active, grs_identifier, grs_list_prefix, grs_name, grs_rectype, grs_type, grs_uri, grs_date_created, grs_last_updated, grs_have_up_to, grs_edit_uri) VALUES " +
                        "(0, true, (select grs_identifier from global_record_source where grs_id = 1), 'oai_dc', (select grs_name from global_record_source where grs_id = 1), 0, 'JSON', (select grs_uri from global_record_source where grs_id = 1), now(), now(), '1970-01-01 00:00:00'::timestamp, (select grs_edit_uri from global_record_source where grs_id = 1))")
            }
            rollback {}
        }
    }
}
