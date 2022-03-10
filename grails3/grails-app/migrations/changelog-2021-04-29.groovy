databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1619686578809-1") {
        grailsChange{
            change {
                sql.execute('ALTER TABLE title_instance_package_platform ALTER COLUMN tipp_first_author TYPE text')
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1619686578809-2") {
        grailsChange {
            change {
                sql.execute('ALTER TABLE title_instance_package_platform ALTER COLUMN tipp_first_editor TYPE text;')
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1619686578809-3") {
        grailsChange {
            change {
                sql.execute('ALTER TABLE title_instance_package_platform ALTER COLUMN tipp_edition_statement TYPE text;')
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1619686578809-4") {
        grailsChange {
            change {
                sql.execute('ALTER TABLE title_instance_package_platform ALTER COLUMN tipp_edition_differentiator TYPE text;')
            }
            rollback {}
        }
    }

}
