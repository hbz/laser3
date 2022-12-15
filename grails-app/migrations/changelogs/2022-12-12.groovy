package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1670858151656-1") {
        grailsChange {
            change {
                //generate at all
                sql.execute("update title_instance_package_platform set tipp_sort_name = lower(trim(tipp_name)) where tipp_sort_name is null")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1670858151656-2") {
        grailsChange {
            change {
                //correct umlauts
                sql.execute("update title_instance_package_platform set tipp_sort_name = lower(regexp_replace(regexp_replace(regexp_replace(regexp_replace(regexp_replace(regexp_replace(tipp_name, '[éèêÉÈÊ]', 'e', 'g'), '[áàâÁÀÂ]', 'a', 'g'), '[öÖ]', 'oe', 'g'), '[üÜ]', 'ue', 'g'), '[äÄ]', 'ae', 'g'), 'ß', 'ss', 'g')) where tipp_name ~ '[áàâÁÀÂéèêÉÈÊäöüÄÖÜß]'")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1670858151656-3") {
        grailsChange {
            change {
                //eliminate French liaison
                sql.execute("update title_instance_package_platform set tipp_sort_name = regexp_replace(tipp_sort_name, '^l''', '', 'g') where tipp_sort_name like '%l''%'")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1670858151656-4") {
        grailsChange {
            change {
                //eliminate all other special charcters
                sql.execute("update title_instance_package_platform set tipp_sort_name = trim(regexp_replace(tipp_sort_name, '[\"°«»[:punct:]]', ' ', 'g')) where tipp_sort_name ~ '[\"°«»[:punct:]]'")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1670858151656-5") {
        grailsChange {
            change {
                //eliminate stopwords
                sql.execute("update title_instance_package_platform set tipp_sort_name = regexp_replace(tipp_sort_name, '^(der |die |das |the |copy of |a |an |le |la |les |los )', '') where tipp_sort_name ~ '^(der |die |das |the |copy of |a |an |le |la |les |los )'")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1670858151656-6") {
        grailsChange {
            change {
                //generate at all
                sql.execute("update issue_entitlement set ie_sortname = lower(trim(ie_name)) where ie_sortname is null")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1670858151656-7") {
        grailsChange {
            change {
                //correct umlauts
                sql.execute("update issue_entitlement set ie_sortname = lower(regexp_replace(regexp_replace(regexp_replace(regexp_replace(regexp_replace(regexp_replace(ie_name, '[éèêÉÈÊ]', 'e', 'g'), '[áàâÁÀÂ]', 'a', 'g'), '[öÖ]', 'oe', 'g'), '[üÜ]', 'ue', 'g'), '[äÄ]', 'ae', 'g'), 'ß', 'ss', 'g')) where ie_name ~ '[áàâÁÀÂéèêÉÈÊäöüÄÖÜß]'")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1670858151656-8") {
        grailsChange {
            change {
                //eliminate French liaison
                sql.execute("update issue_entitlement set ie_sortname = regexp_replace(ie_sortname, '^l''', '', 'g') where ie_sortname like '%l''%'")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1670858151656-9") {
        grailsChange {
            change {
                //eliminate all other special charcters
                sql.execute("update issue_entitlement set ie_sortname = trim(regexp_replace(ie_sortname, '[\"°«»[:punct:]]', ' ', 'g')) where ie_sortname ~ '[\"°«»[:punct:]]'")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1670858151656-10") {
        grailsChange {
            change {
                //eliminate stopwords
                sql.execute("update issue_entitlement set ie_sortname = regexp_replace(ie_sortname, '^(der |die |das |the |copy of |a |an |le |la |les |los )', '') where ie_sortname ~ '^(der |die |das |the |copy of |a |an |le |la |les |los )'")
            }
            rollback {}
        }
    }
}
