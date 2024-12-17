package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1734421515135-1") {
        dropColumn(columnName: "grs_creds", tableName: "global_record_source")
    }

    changeSet(author: "klober (generated)", id: "1734421515135-2") {
        dropColumn(columnName: "grs_edit_uri", tableName: "global_record_source")
    }

    changeSet(author: "klober (generated)", id: "1734421515135-3") {
        dropColumn(columnName: "grs_full_prefix", tableName: "global_record_source")
    }

    changeSet(author: "klober (generated)", id: "1734421515135-4") {
        dropColumn(columnName: "grs_identifier", tableName: "global_record_source")
    }

    changeSet(author: "klober (generated)", id: "1734421515135-5") {
        dropColumn(columnName: "grs_list_prefix", tableName: "global_record_source")
    }

    changeSet(author: "klober (generated)", id: "1734421515135-6") {
        dropColumn(columnName: "grs_principal", tableName: "global_record_source")
    }

    changeSet(author: "klober (generated)", id: "1734421515135-7") {
        dropColumn(columnName: "grs_uri", tableName: "global_record_source")
    }
}
