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

    changeSet(author: "klober (modified)", id: "1604920204115-7") {
        grailsChange {
            change {
                sql.execute("ALTER SEQUENCE system_activity_profiler_ap_id_seq RENAME TO system_activity_profiler_sap_id_seq")
                sql.execute("ALTER TABLE system_activity_profiler ALTER COLUMN sap_id SET DEFAULT nextval('public.system_activity_profiler_sap_id_seq'::regclass)")

            }
            rollback {}
        }
    }

    changeSet(author: "klober (generated)", id: "1604920204115-8") {
        dropColumn(columnName: "sap_version", tableName: "system_activity_profiler")
    }

    changeSet(author: "klober (generated)", id: "1604920204115-9") {
        dropColumn(columnName: "sp_version", tableName: "system_profiler")
    }

    changeSet(author: "klober (generated)", id: "1604920204115-10") {
        dropColumn(columnName: "version", tableName: "system_event")
    }

    changeSet(author: "kloberd (modified)", id: "1604920204115-11") {
        grailsChange {
            change {
                sql.execute("update pending_change set pc_change_doc_oid = replace(pc_change_doc_oid, 'com.k_int.kbplus.Subscription:', 'de.laser.Subscription:') where pc_change_doc_oid is not null")
                sql.execute("update pending_change set pc_change_target_oid = replace(pc_change_target_oid, 'com.k_int.kbplus.Subscription:', 'de.laser.Subscription:') where pc_change_target_oid is not null")


            }
            rollback {}
        }
    }
    changeSet(author: "kloberd (modified)", id: "1604920204115-12") {
        grailsChange {
            change {
                sql.execute("update pending_change set pc_change_doc_oid = replace(pc_change_doc_oid, 'com.k_int.kbplus.License:', 'de.laser.License:') where pc_change_doc_oid is not null")
                sql.execute("update pending_change set pc_change_target_oid = replace(pc_change_target_oid, 'com.k_int.kbplus.License:', 'de.laser.License:') where pc_change_target_oid is not null")
            }
            rollback {}
        }
    }

    changeSet(author: "kloberd (modified)", id: "1604920204115-13") {
        grailsChange {
            change {
                sql.execute("update pending_change set pc_change_doc_oid = replace(pc_change_doc_oid, 'com.k_int.kbplus.SubscriptionProperty:', 'de.laser.properties.SubscriptionProperty:') where pc_change_doc_oid is not null")
                sql.execute("update pending_change set pc_change_doc_oid = replace(pc_change_doc_oid, 'com.k_int.kbplus.LicenseProperty:', 'de.laser.properties.LicenseProperty:') where pc_change_doc_oid is not null")
            }
            rollback {}
        }
    }

    changeSet(author: "kloberd (modified)", id: "1604920204115-14") {
        grailsChange {
            change {
                sql.execute("update property_definition set pd_type = replace(pd_type, 'class ', '') where pd_type is not null")
            }
            rollback {}
        }
    }
    changeSet(author: "kloberd (modified)", id: "1604920204115-15") {
        grailsChange {
            change {
                sql.execute("update pending_change set pc_payload = replace(pc_payload, 'type\":\"class ', 'type\":\"') where pc_payload is not null")
            }
            rollback {}
        }
    }
    changeSet(author: "kloberd (modified)", id: "1604920204115-16") {
        grailsChange {
            change {
                sql.execute("update survey_property set surpro_type = replace(surpro_type, 'class ', '')  where surpro_type is not null")
            }
            rollback {}
        }
    }
}
