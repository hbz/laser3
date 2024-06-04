package changelogs

databaseChangeLog = {

    changeSet(author: "djebeniani (hand-coded)", id: "1717498671893-1") {
        grailsChange {
            change {
                sql.execute("update title_instance_package_platform set tipp_title_type = 'serial' where tipp_title_type = 'Journal'")
            }
            rollback {}
        }
    }
    changeSet(author: "djebeniani (hand-coded)", id: "1717498671893-2") {
        grailsChange {
            change {
                sql.execute("update title_instance_package_platform set tipp_title_type = 'monograph' where tipp_title_type = 'Book'")
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1717498671893-3") {
        grailsChange {
            change {
                sql.execute("update title_instance_package_platform set tipp_title_type = 'database' where tipp_title_type = 'Database'")
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1717498671893-4") {
        grailsChange {
            change {
                sql.execute("update title_instance_package_platform set tipp_title_type = 'other' where tipp_title_type = 'Other'")
            }
            rollback {}
        }
    }

}
