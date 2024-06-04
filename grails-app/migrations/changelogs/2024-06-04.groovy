package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1717494364714-1") {
        grailsChange {
            change {
                sql.execute('ALTER TABLE public.refdata_value ALTER COLUMN rdv_value_de TYPE character varying(511) COLLATE public."de-u-co-phonebk-x-icu";')
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1717494364714-2") {
        grailsChange {
            change {
                sql.execute('ALTER TABLE public.refdata_value ALTER COLUMN rdv_value_en TYPE character varying(511) COLLATE public."de-u-co-phonebk-x-icu";')
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1717494364714-3") {
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
    changeSet(author: "djebeniani (hand-coded)", id: "1717494364714-4") {
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

    changeSet(author: "djebeniani (hand-coded)", id: "1717494364714-5") {
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

    changeSet(author: "djebeniani (hand-coded)", id: "1717494364714-6") {
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
