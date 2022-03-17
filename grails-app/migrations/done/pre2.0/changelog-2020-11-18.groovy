databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1605696586207-1") {
        addUniqueConstraint(columnNames: "ig_sub_fk, ig_name", constraintName: "UK826edb8460f3f85d5d301254464e", tableName: "issue_entitlement_group")
    }

    changeSet(author: "klober (modified)", id: "1605696586207-2") {
        grailsChange {
            change {
                sql.execute("alter table org_subject_group alter column osg_date_created type timestamp using osg_date_created::timestamp")
                sql.execute("alter table org_subject_group alter column osg_last_updated type timestamp using osg_last_updated::timestamp")

                sql.execute("update org_subject_group set osg_date_created = (osg_date_created + '0 hour'::interval)")
                sql.execute("update org_subject_group set osg_last_updated = (osg_last_updated + '0 hour'::interval)")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1605696586207-3") {
        grailsChange {
            change {
                sql.execute("alter table system_announcement alter column sa_date_created type timestamp using sa_date_created::timestamp")
                sql.execute("alter table system_announcement alter column sa_last_publishing_date type timestamp using sa_last_publishing_date::timestamp")
                sql.execute("alter table system_announcement alter column sa_last_updated type timestamp using sa_last_updated::timestamp")

                sql.execute("update system_announcement set sa_date_created = (sa_date_created + '0 hour'::interval)")
                sql.execute("update system_announcement set sa_last_publishing_date = (sa_last_publishing_date + '0 hour'::interval)")
                sql.execute("update system_announcement set sa_last_updated = (sa_last_updated + '0 hour'::interval)")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1605696586207-4") {
        grailsChange {
            change {
                sql.execute("alter table issue_entitlement_group alter column ig_date_created type timestamp using ig_date_created::timestamp")
                sql.execute("alter table issue_entitlement_group alter column ig_last_updated type timestamp using ig_last_updated::timestamp")

                sql.execute("update issue_entitlement_group set ig_date_created = (ig_date_created + '0 hour'::interval)")
                sql.execute("update issue_entitlement_group set ig_last_updated = (ig_last_updated + '0 hour'::interval)")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1605696586207-5") {
        grailsChange {
            change {
                sql.execute("alter table issue_entitlement_group_item alter column igi_date_created type timestamp using igi_date_created::timestamp")
                sql.execute("alter table issue_entitlement_group_item alter column igi_last_updated type timestamp using igi_last_updated::timestamp")

                sql.execute("update issue_entitlement_group_item set igi_date_created = (igi_date_created + '0 hour'::interval)")
                sql.execute("update issue_entitlement_group_item set igi_last_updated = (igi_last_updated + '0 hour'::interval)")
            }
            rollback {}
        }
    }
}
