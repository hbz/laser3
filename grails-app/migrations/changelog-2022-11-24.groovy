databaseChangeLog = {

    //indirect cherry-pick from changelogs/2022-06-24.groovy - sorting has been complained again ...

    changeSet(author: "galffy (hand-coded)", id: "1669284224116-1") {
        grailsChange {
            change {
                //normalise l'article indéfini français succombé à la liaison (normalise indefinite French article which got victims of liaison)
                sql.execute("update title_instance_package_platform set tipp_sort_name = regexp_replace(tipp_sort_name, '^l''', '', 'g') where tipp_sort_name like '%l''%'")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1669284224116-2") {
        grailsChange {
            change {
                //normalise l'article indéfini français succombé à la liaison (normalise indefinite French article which got victims of liaison)
                sql.execute("update issue_entitlement set ie_sortname = regexp_replace(ie_sortname, '^l''', '', 'g') where ie_sortname like '%l''%'")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1669284224116-3") {
        grailsChange {
            change {
                //normalise all other special characters
                sql.execute("update title_instance_package_platform set tipp_sort_name = trim(regexp_replace(tipp_sort_name, '[\"°«»[:punct:]]', ' ', 'g')) where tipp_sort_name ~ '[\"°«»[:punct:]]'")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1669284224116-4") {
        grailsChange {
            change {
                //normalise all other special characters
                sql.execute("update issue_entitlement set ie_sortname = trim(regexp_replace(ie_sortname, '[\"°«»[:punct:]]', ' ', 'g')) where ie_sortname ~ '[\"°«»[:punct:]]'")
            }
            rollback {}
        }
    }
}
