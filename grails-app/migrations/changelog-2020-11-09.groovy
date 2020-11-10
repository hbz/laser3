databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1604920204115-1") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "formal_role_id", tableName: "user_org")
    }

    changeSet(author: "klober (generated)", id: "1604920204115-2") {
        addNotNullConstraint(columnDataType: "boolean", columnName: "org_e_invoice", tableName: "org")
    }

    changeSet(author: "klober (generated)", id: "1604920204115-3") {
        addUniqueConstraint(columnNames: "os_key_enum, os_org_fk", constraintName: "org_settings_os_key_enum_os_org_fk_key", tableName: "org_setting")
    }

    changeSet(author: "klober (generated)", id: "1604920204115-4") {
        dropColumn(columnName: "version", tableName: "perm")
    }

    changeSet(author: "klober (generated)", id: "1604920204115-5") {
        dropColumn(columnName: "version", tableName: "perm_grant")
    }

    changeSet(author: "klober (generated)", id: "1604920204115-6") {
        dropColumn(columnName: "version", tableName: "role")
    }

    changeSet(author: "klober (generated)", id: "1604920204115-7") {
        grailsChange {
            change {
                sql.execute("ALTER SEQUENCE system_activity_profiler_ap_id_seq RENAME TO system_activity_profiler_sap_id_seq")
                sql.execute("ALTER TABLE system_activity_profiler ALTER COLUMN sap_id SET DEFAULT nextval('public.system_activity_profiler_sap_id_seq'::regclass)")

            }
            rollback {}
        }
    }
}
