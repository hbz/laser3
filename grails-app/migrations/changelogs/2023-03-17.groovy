package changelogs

databaseChangeLog = {

    changeSet(author: "klober (modified)", id: "1679034092515-1") {
        grailsChange {
            change {
                sql.execute("UPDATE role set r_authority = 'ORG_INST_BASIC' WHERE r_authority = 'ORG_BASIC'")
                sql.execute("UPDATE perm set pm_code = 'org_inst_basic' WHERE pm_code = 'org_basic'")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1679034092515-2") {
        grailsChange {
            change {
                sql.execute("UPDATE role set r_authority = 'ORG_INST_PRO' WHERE r_authority = 'ORG_PRO'")
                sql.execute("UPDATE perm set pm_code = 'org_inst_pro' WHERE pm_code = 'org_pro'")
            }
            rollback {}
        }
    }

//    changeSet(author: "klober (modified)", id: "1679034092515-3") {
//        grailsChange {
//            change {
//                sql.execute("DELETE from perm WHERE pm_code in ('org_basic_member', 'org_member_basic', 'org_inst', 'org_consortium')")
//            }
//            rollback {}
//        }
//    }
}
