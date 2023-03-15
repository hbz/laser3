package changelogs

databaseChangeLog = {

    changeSet(author: "klober (modified)", id: "1678798160478-1") {
        grailsChange {
            change {
                sql.execute("UPDATE role set r_authority = 'ORG_MEMBER_BASIC' WHERE r_authority = 'ORG_BASIC_MEMBER'")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1678798160478-2") {
        grailsChange {
            change {
                sql.execute("UPDATE role set r_authority = 'ORG_CONSORTIUM_BASIC' WHERE r_authority = 'ORG_CONSORTIUM'")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1678798160478-3") {
        grailsChange {
            change {
                sql.execute("UPDATE role set r_authority = 'ORG_PRO' WHERE r_authority = 'ORG_INST'")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1678798160478-4") {
        grailsChange {
            change {
                sql.execute("UPDATE role set r_authority = 'ORG_BASIC' WHERE r_authority = 'ORG_MEMBER_BASIC'")
            }
            rollback {}
        }
    }
}
