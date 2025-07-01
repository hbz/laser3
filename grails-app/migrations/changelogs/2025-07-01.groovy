package changelogs

databaseChangeLog = {

    changeSet(author: "klober (hand-coded)", id: "1751371897814-1") {
        grailsChange {
            change {
                sql.executeUpdate("""
insert into user_setting (us_key_enum, us_user_fk, us_string_value, us_version, us_date_created)
SELECT 'DASHBOARD_TAB_TIME_CHANGES', src.us_user_fk, src.us_string_value, 1, now() FROM user_setting src
WHERE src.us_key_enum = 'DASHBOARD_ITEMS_TIME_WINDOW';
                """)

                sql.executeUpdate("""
insert into user_setting (us_key_enum, us_user_fk, us_string_value, us_version, us_date_created)
SELECT 'DASHBOARD_TAB_TIME_SERVICE_MESSAGES', src.us_user_fk, src.us_string_value, 1, now() FROM user_setting src
WHERE src.us_key_enum = 'DASHBOARD_ITEMS_TIME_WINDOW';
                """)
            }
            rollback {}
        }
    }

    changeSet(author: "klober (hand-coded)", id: "1751371897814-2") {
        grailsChange {
            change {
                sql.executeUpdate("delete from user_setting where us_key_enum = 'DASHBOARD_ITEMS_TIME_WINDOW';")
            }
            rollback {}
        }
    }
}
