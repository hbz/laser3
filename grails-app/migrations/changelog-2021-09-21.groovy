databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1632228806219-1") {
        grailsChange {
            change {
                sql.execute("delete from subscription_property where sp_type_fk = (select pd_id from property_definition where pd_name = 'KfL')")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1632228806219-2") {
        grailsChange {
            change {
                sql.execute("delete from property_definition where pd_name = 'KfL'")
            }
            rollback {}
        }
    }

}
