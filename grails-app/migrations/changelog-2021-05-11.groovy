import de.laser.Identifier
import de.laser.IdentifierNamespace
import de.laser.TitleInstancePackagePlatform

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1620713794091-1") {
        createTable(tableName: "language") {
            column(autoIncrement: "true", name: "lang_id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "languagePK")
            }

            column(name: "lang_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "lang_tipp_fk", type: "BIGINT")

            column(name: "lang_date_created", type: "TIMESTAMP WITHOUT TIME ZONE")

            column(name: "lang_last_updated_cascading", type: "TIMESTAMP WITHOUT TIME ZONE")

            column(name: "lang_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE")

            column(name: "lang_rv_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "lang_pkg_fk", type: "BIGINT")
        }
    }

    changeSet(author: "galffy (generated)", id: "1620713794091-2") {
        addForeignKeyConstraint(baseColumnNames: "lang_pkg_fk", baseTableName: "language", constraintName: "FK4ex6ksv5tbw93tnmq3i6772ip", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pkg_id", referencedTableName: "package")
    }

    changeSet(author: "galffy (generated)", id: "1620713794091-3") {
        addForeignKeyConstraint(baseColumnNames: "lang_tipp_fk", baseTableName: "language", constraintName: "FK9gy5oqcw8wy5nrylrb6rcmv4w", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "tipp_id", referencedTableName: "title_instance_package_platform")
    }

    changeSet(author: "galffy (generated)", id: "1620713794091-4") {
        addForeignKeyConstraint(baseColumnNames: "lang_rv_fk", baseTableName: "language", constraintName: "FKl3g3hk4bm63y9bglnplomoawx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "galffy (hand-coded)", id: "1620713794091-5") {
        grailsChange {
            change {
                String tippClassName = TitleInstancePackagePlatform.class.name
                List avaCount = Identifier.executeQuery("select count(id.id) from Identifier id join id.ns ns where ns.ns = 'ava' and ns.nsType is null")
                List avaTippId = IdentifierNamespace.executeQuery("select ns.id from IdentifierNamespace ns where ns.ns = 'ava' and ns.nsType = '"+tippClassName+"'")
                if(avaCount && !avaTippId) {
                    sql.execute("update identifier_namespace set idns_type = '"+tippClassName+"' where idns_ns = 'ava' and idns_type is null")
                }
                else if(avaCount && avaTippId) {
                    sql.execute("update identifier set id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'ava' and idns_type = '"+tippClassName+"') where id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'ava' and idns_type is null) and id_tipp_fk is not null;")
                }

            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1620713794091-6") {
        grailsChange {
            change {
                sql.execute("delete from identifier_namespace where idns_ns = 'ava' and idns_type is null;")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1620713794091-7") {
        grailsChange {
            change {
                sql.execute("update identifier set id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'carelit' and idns_type = 'de.laser.TitleInstancePackagePlatform') where id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'carelit' and idns_type is null) and id_tipp_fk is not null;")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1620713794091-8") {
        grailsChange {
            change {
                sql.execute("delete from identifier_namespace where idns_ns = 'carelit' and idns_type is null;")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1620713794091-9") {
        grailsChange {
            change {
                String tippClassName = TitleInstancePackagePlatform.class.name
                List doiCount = Identifier.executeQuery("select count(id.id) from Identifier id join id.ns ns where ns.ns = 'doi' and ns.nsType is null")
                List doiTippId = IdentifierNamespace.executeQuery("select ns.id from IdentifierNamespace ns where ns.ns = 'doi' and ns.nsType = '"+tippClassName+"'")
                if(doiCount && !doiTippId) {
                    sql.execute("update identifier_namespace set idns_type = '"+tippClassName+"' where idns_ns = 'doi' and idns_type is null")
                }
                else if(doiCount && doiTippId) {
                    sql.execute("update identifier set id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'doi' and idns_type = 'de.laser.TitleInstancePackagePlatform') where id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'doi' and idns_type is null) and id_tipp_fk is not null;")
                }
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1620713794091-10") {
        grailsChange {
            change {
                sql.execute("delete from identifier_namespace where idns_ns = 'doi' and idns_type is null;")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1620713794091-11") {
        grailsChange {
            change {
                String tippClassName = TitleInstancePackagePlatform.class.name
                List doiCount = Identifier.executeQuery("select count(id.id) from Identifier id join id.ns ns where ns.ns = 'emerald' and ns.nsType is null")
                List doiTippId = IdentifierNamespace.executeQuery("select ns.id from IdentifierNamespace ns where ns.ns = 'emerald' and ns.nsType = '"+tippClassName+"'")
                if(doiCount && !doiTippId) {
                    sql.execute("update identifier_namespace set idns_type = '"+tippClassName+"' where idns_ns = 'emerald' and idns_type is null")
                }
                else if(doiCount && doiTippId) {
                    sql.execute("update identifier set id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'emerald' and idns_type = 'de.laser.TitleInstancePackagePlatform') where id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'emerald' and idns_type is null) and id_tipp_fk is not null;")
                }
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1620713794091-12") {
        grailsChange {
            change {
                sql.execute("update identifier_namespace set idns_type = 'de.laser.Subscription' where idns_ns = 'emerald' and idns_type is null;")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1620713794091-13") {
        grailsChange {
            change {
                String tippClassName = TitleInstancePackagePlatform.class.name
                List doiCount = Identifier.executeQuery("select count(id.id) from Identifier id join id.ns ns where ns.ns = 'eissn' and ns.nsType is null")
                List doiTippId = IdentifierNamespace.executeQuery("select ns.id from IdentifierNamespace ns where ns.ns = 'eissn' and ns.nsType = '"+tippClassName+"'")
                if(doiCount && !doiTippId) {
                    sql.execute("update identifier_namespace set idns_type = '"+tippClassName+"' where idns_ns = 'eissn' and idns_type is null")
                }
                else if(doiCount && doiTippId) {
                    sql.execute("update identifier set id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'eissn' and idns_type = 'de.laser.TitleInstancePackagePlatform') where id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'eissn' and idns_type is null) and id_tipp_fk is not null;")
                }
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1620713794091-14") {
        grailsChange {
            change {
                sql.execute("update identifier_namespace set idns_type = 'de.laser.Subscription' where idns_ns = 'eissn' and idns_type is null;")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1620713794091-15") {
        grailsChange {
            change {
                String tippClassName = TitleInstancePackagePlatform.class.name
                List doiCount = Identifier.executeQuery("select count(id.id) from Identifier id join id.ns ns where ns.ns = 'herdt' and ns.nsType is null")
                List doiTippId = IdentifierNamespace.executeQuery("select ns.id from IdentifierNamespace ns where ns.ns = 'herdt' and ns.nsType = '"+tippClassName+"'")
                if(doiCount && !doiTippId) {
                    sql.execute("update identifier_namespace set idns_type = '"+tippClassName+"' where idns_ns = 'herdt' and idns_type is null")
                }
                else if(doiCount && doiTippId) {
                    sql.execute("update identifier set id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'herdt' and idns_type = 'de.laser.TitleInstancePackagePlatform') where id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'herdt' and idns_type is null) and id_tipp_fk is not null;")
                }
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1620713794091-16") {
        grailsChange {
            change {
                sql.execute("delete from identifier_namespace where idns_ns = 'herdt' and idns_type is null;")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1620713794091-17") {
        grailsChange {
            change {
                sql.execute("delete from identifier where id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'isbn' and idns_type is null) and id_sub_fk is not null;")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1620713794091-18") {
        grailsChange {
            change {
                String tippClassName = TitleInstancePackagePlatform.class.name
                List doiCount = Identifier.executeQuery("select count(id.id) from Identifier id join id.ns ns where ns.ns = 'isbn' and ns.nsType is null")
                List doiTippId = IdentifierNamespace.executeQuery("select ns.id from IdentifierNamespace ns where ns.ns = 'isbn' and ns.nsType = '"+tippClassName+"'")
                if(doiCount && !doiTippId) {
                    sql.execute("update identifier_namespace set idns_type = '"+tippClassName+"' where idns_ns = 'isbn' and idns_type is null")
                }
                else if(doiCount && doiTippId) {
                    sql.execute("update identifier set id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'isbn' and idns_type = 'de.laser.TitleInstancePackagePlatform') where id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'isbn' and idns_type is null) and id_tipp_fk is not null;")
                }
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1620713794091-19") {
        grailsChange {
            change {
                sql.execute("delete from identifier_namespace where idns_ns = 'isbn' and idns_type is null;")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1620713794091-20") {
        grailsChange {
            change {
                String tippClassName = TitleInstancePackagePlatform.class.name
                List doiCount = Identifier.executeQuery("select count(id.id) from Identifier id join id.ns ns where ns.ns = 'issn' and ns.nsType is null")
                List doiTippId = IdentifierNamespace.executeQuery("select ns.id from IdentifierNamespace ns where ns.ns = 'issn' and ns.nsType = '"+tippClassName+"'")
                if(doiCount && !doiTippId) {
                    sql.execute("update identifier_namespace set idns_type = '"+tippClassName+"' where idns_ns = 'issn' and idns_type is null")
                }
                else if(doiCount && doiTippId) {
                    sql.execute("update identifier set id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'issn' and idns_type = 'de.laser.TitleInstancePackagePlatform') where id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'issn' and idns_type is null) and id_tipp_fk is not null;")
                }
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1620713794091-21") {
        grailsChange {
            change {
                sql.execute("update identifier_namespace set idns_type = 'de.laser.Subscription' where idns_ns = 'issn' and idns_type is null;")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1620713794091-22") {
        grailsChange {
            change {
                sql.execute("delete from identifier where id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'uri');")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1620713794091-23") {
        grailsChange {
            change {
                sql.execute("delete from identifier_namespace where idns_ns = 'uri';")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1620713794091-24") {
        grailsChange {
            change {
                String tippClassName = TitleInstancePackagePlatform.class.name
                List doiCount = Identifier.executeQuery("select count(id.id) from Identifier id join id.ns ns where ns.ns = 'zdb' and ns.nsType is null")
                List doiTippId = IdentifierNamespace.executeQuery("select ns.id from IdentifierNamespace ns where ns.ns = 'zdb' and ns.nsType = '"+tippClassName+"'")
                if(doiCount && !doiTippId) {
                    sql.execute("update identifier_namespace set idns_type = '"+tippClassName+"' where idns_ns = 'zdb' and idns_type is null")
                }
                else if(doiCount && doiTippId) {
                    sql.execute("update identifier set id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'zdb' and idns_type = 'de.laser.TitleInstancePackagePlatform') where id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'zdb' and idns_type is null) and id_tipp_fk is not null;")
                }
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1620713794091-25") {
        grailsChange {
            change {
                sql.execute("update identifier_namespace set idns_type = 'de.laser.Subscription' where idns_ns = 'zdb' and idns_type is null;")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1620713794091-26") {
        grailsChange {
            change {
                sql.execute("delete from identifier where id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'zdb_ppn' and idns_type is null) and id_org_fk is not null;")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1620713794091-27") {
        grailsChange {
            change {
                String tippClassName = TitleInstancePackagePlatform.class.name
                List doiCount = Identifier.executeQuery("select count(id.id) from Identifier id join id.ns ns where ns.ns = 'zdb_ppn' and ns.nsType is null")
                List doiTippId = IdentifierNamespace.executeQuery("select ns.id from IdentifierNamespace ns where ns.ns = 'zdb_ppn' and ns.nsType = '"+tippClassName+"'")
                if(doiCount && !doiTippId) {
                    sql.execute("update identifier_namespace set idns_type = '"+tippClassName+"' where idns_ns = 'zdb_ppn' and idns_type is null")
                }
                else if(doiCount && doiTippId) {
                    sql.execute("update identifier set id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'zdb_ppn' and idns_type = 'de.laser.TitleInstancePackagePlatform') where id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'zdb_ppn' and idns_type is null) and id_tipp_fk is not null;")
                }
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1620713794091-28") {
        grailsChange {
            change {
                sql.execute("update identifier_namespace set idns_type = 'de.laser.Subscription' where idns_ns = 'zdb_ppn' and idns_type is null;")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1620713794091-29") {
        grailsChange {
            change {
                String tippClassName = TitleInstancePackagePlatform.class.name
                List doiCount = Identifier.executeQuery("select count(id.id) from Identifier id join id.ns ns where ns.ns = 'ezb' and ns.nsType is null")
                List doiTippId = IdentifierNamespace.executeQuery("select ns.id from IdentifierNamespace ns where ns.ns = 'ezb' and ns.nsType = '"+tippClassName+"'")
                if(doiCount && !doiTippId) {
                    sql.execute("update identifier_namespace set idns_type = '"+tippClassName+"' where idns_ns = 'ezb' and idns_type is null")
                }
                else if(doiCount && doiTippId) {
                    sql.execute("update identifier set id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'ezb' and idns_type = 'de.laser.TitleInstancePackagePlatform') where id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'ezb' and idns_type is null) and id_tipp_fk is not null;")
                }
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1620713794091-30") {
        grailsChange {
            change {
                sql.execute("update identifier_namespace set idns_type = 'de.laser.Subscription' where idns_ns = 'ezb' and idns_type is null;")
            }
            rollback {}
        }
    }

}
