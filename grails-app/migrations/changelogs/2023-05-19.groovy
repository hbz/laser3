package changelogs

databaseChangeLog = {

    changeSet(author: "djebeniani (hand-coded)", id: "1684682665449-1") {
        grailsChange {
            change {
                sql.executeUpdate('''UPDATE identifier_namespace
SET idns_ns      = 'eisbn',
    idns_name_de = 'eISBN'
WHERE idns_type = 'de.laser.TitleInstancePackagePlatform' and idns_ns = 'isbn';''')
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1684682665449-2") {
        grailsChange {
            change {
                sql.executeUpdate('''UPDATE identifier_namespace
SET idns_ns      = 'isbn',
    idns_name_de = 'ISBN'
WHERE idns_type = 'de.laser.TitleInstancePackagePlatform' and idns_ns = 'pisbn';''')
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1684682665449-3") {
        grailsChange {
            change {
                sql.executeInsert('''INSERT INTO identifier_namespace (idns_id, idns_version, idns_family, idns_is_hidden, idns_ns, idns_type,
                                         idns_val_regex, idns_is_unique, idns_date_created, idns_last_updated,
                                         idns_description_de, idns_description_en, idns_name_de, idns_name_en,
                                         idns_url_prefix, idns_last_updated_cascading, idns_is_from_laser,
                                         idns_is_hard_data) VALUES (DEFAULT, 0, null, false, 'title_id', 'de.laser.TitleInstancePackagePlatform', null, false, now(), now(), null, null, 'Title_ID', 'Title_ID', null, now(), false, false);''')
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1684682665449-4") {
        grailsChange {
            change {

                List<String> idNamespaces = ['acs',
                                             'ava',
                                             'ava.at',
                                             'beck',
                                             'berliner-philharmoniker',
                                             'beuth',
                                             'bloomsbury',
                                             'brepols',
                                             'cambridge',
                                             'carelit',
                                             'cas',
                                             'clarivate',
                                             'contentselect',
                                             'cup',
                                             'degruyter',
                                             'duncker&humblot',
                                             'duz',
                                             'ebookcentral',
                                             'elsevier',
                                             'emerald',
                                             'europalehrmittel',
                                             'felixmeiner',
                                             'filmfriend',
                                             'gbi',
                                             'goodhabitz',
                                             'hanser',
                                             'henle',
                                             'herdt',
                                             'ibisworld',
                                             'igpublishing',
                                             'jstor',
                                             'karger',
                                             'lexisnexis',
                                             'materialatlas',
                                             'meiner',
                                             'meinunterricht',
                                             'mgg online',
                                             'munzinger',
                                             'narr',
                                             'naxos',
                                             'ne_gmbh',
                                             'newyorktimes',
                                             'nkoda',
                                             'nomos',
                                             'oclc',
                                             'oecd',
                                             'oup',
                                             'peterlang',
                                             'pons',
                                             'preselect',
                                             'project_muse',
                                             'prometheus',
                                             'proquest',
                                             'reguvis',
                                             'spie',
                                             'springer',
                                             'taylor&francis',
                                             'thieme',
                                             'utb',
                                             'vde',
                                             'vdi_elibrary',
                                             'wiley',
                                             'wolterskluwer']

                Integer countUpdateSum = 0
                idNamespaces.each {String namespace ->

                    Integer countUpdate = sql.executeUpdate("update identifier set id_ns_fk = (select idns_id from identifier_namespace WHERE idns_type = 'de.laser.TitleInstancePackagePlatform' and idns_ns = 'title_id' ) where id_ns_fk = (select idns_id from identifier_namespace WHERE idns_type = 'de.laser.TitleInstancePackagePlatform' and idns_ns = '" + namespace + "')")

                    countUpdateSum = countUpdateSum + countUpdate

                }
                confirm("update identifier set id_ns_fk to title_id: ${countUpdateSum}")
                changeSet.setComments("update identifier set id_ns_fk to title_id: ${countUpdateSum}")
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1684682665449-5") {
        grailsChange {
            change {

                List<String> idNamespaces = ['acs',
                                             'ava',
                                             'ava.at',
                                             'beck',
                                             'berliner-philharmoniker',
                                             'beuth',
                                             'bloomsbury',
                                             'brepols',
                                             'cambridge',
                                             'carelit',
                                             'cas',
                                             'clarivate',
                                             'contentselect',
                                             'cup',
                                             'degruyter',
                                             'duncker&humblot',
                                             'duz',
                                             'ebookcentral',
                                             'elsevier',
                                             'emerald',
                                             'europalehrmittel',
                                             'felixmeiner',
                                             'filmfriend',
                                             'gbi',
                                             'goodhabitz',
                                             'hanser',
                                             'henle',
                                             'herdt',
                                             'ibisworld',
                                             'igpublishing',
                                             'jstor',
                                             'karger',
                                             'lexisnexis',
                                             'materialatlas',
                                             'meiner',
                                             'meinunterricht',
                                             'mgg online',
                                             'munzinger',
                                             'narr',
                                             'naxos',
                                             'ne_gmbh',
                                             'newyorktimes',
                                             'nkoda',
                                             'nomos',
                                             'oclc',
                                             'oecd',
                                             'oup',
                                             'peterlang',
                                             'pons',
                                             'preselect',
                                             'project_muse',
                                             'prometheus',
                                             'proquest',
                                             'reguvis',
                                             'spie',
                                             'springer',
                                             'taylor&francis',
                                             'thieme',
                                             'utb',
                                             'vde',
                                             'vdi_elibrary',
                                             'wiley',
                                             'wolterskluwer']

                Integer countDeleteSum = 0
                idNamespaces.each {String namespace ->

                    Integer countDelete = sql.executeUpdate("delete from identifier_namespace WHERE idns_type = 'de.laser.TitleInstancePackagePlatform' and idns_ns = '" + namespace + "'")

                    countDeleteSum = countDeleteSum + countDelete

                }
                confirm("delete from identifier_namespace : ${countDeleteSum}")
                changeSet.setComments("delete from identifier_namespace: ${countDeleteSum}")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (generated)", id: "1684682665449-6") {
        createIndex(indexName: "igi_ie_group_idx", tableName: "issue_entitlement_group_item") {
            column(name: "igi_ie_fk")

            column(name: "igi_ie_group_fk")
        }
    }
}
