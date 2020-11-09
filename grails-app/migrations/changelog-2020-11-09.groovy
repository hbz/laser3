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
}
