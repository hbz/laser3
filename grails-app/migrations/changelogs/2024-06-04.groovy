package changelogs

databaseChangeLog = {

    changeSet(author: "djebeniani (hand-coded)", id: "1717498671893-1") {
        grailsChange {
            change {
                sql.execute("update title_instance_package_platform set tipp_title_type = 'serial' where tipp_title_type = 'Journal'")
                String info = "title_instance_package_platform set tipp_title_type to serial: ${sql.getUpdateCount()}"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }
    changeSet(author: "djebeniani (hand-coded)", id: "1717498671893-2") {
        grailsChange {
            change {
                sql.execute("update title_instance_package_platform set tipp_title_type = 'monograph' where tipp_title_type = 'Book'")
                String info = "title_instance_package_platform set tipp_title_type to monograph: ${sql.getUpdateCount()}"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1717498671893-3") {
        grailsChange {
            change {
                sql.execute("update title_instance_package_platform set tipp_title_type = 'database' where tipp_title_type = 'Database'")
                String info = "title_instance_package_platform set tipp_title_type to database: ${sql.getUpdateCount()}"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1717498671893-4") {
        grailsChange {
            change {
                sql.execute("update title_instance_package_platform set tipp_title_type = 'other' where tipp_title_type = 'Other'")
                String info = "title_instance_package_platform set tipp_title_type to other: ${sql.getUpdateCount()}"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

}
