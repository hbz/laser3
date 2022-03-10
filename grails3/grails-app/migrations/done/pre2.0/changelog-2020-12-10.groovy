import de.laser.Org
import de.laser.Subscription
import de.laser.titles.TitleInstance

String flaggingToLaser = "'Anbieter_Produkt_ID','DBIS Anker','DBS-ID','EZB anchor','ezb_collection_id','ezb_org_id','global','gnd_org_id','GRID ID','ISIL','ISIL_Paketsigel','Leitkriterium (intern)','Leitweg-ID','VAT','wibid'"

databaseChangeLog = {

    changeSet(author: "galffy (genereated)", id: "1607669186880-1") {
        addColumn(tableName: "identifier_namespace") {
            column(name: "idns_is_from_laser", type: "boolean")
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-2") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_is_from_laser = true where idns_ns in ("+flaggingToLaser+")"
                sql.execute ( query )
            }
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-3") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_is_from_laser = false where idns_ns not in ("+flaggingToLaser+")"
                sql.execute ( query )
            }
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-4") {
        addNotNullConstraint(columnDataType: "bool", columnName: "idns_is_from_laser", tableName: "identifier_namespace")
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-5") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${Subscription.class.name}' where idns_ns = 'Anbieter_Produkt_ID';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-6") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${TitleInstance.class.name}' where idns_ns = 'cup';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-7") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${Subscription.class.name}' where idns_ns = 'DBIS Anker';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-8") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${Org.class.name}' where idns_ns = 'DBS-ID';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-9") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${Org.class.name}' where idns_ns = 'dnb';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-10") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${de.laser.Package.class.name}' where idns_ns = 'duz';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-11") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${TitleInstance.class.name}' where idns_ns = 'ebookcentral';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-12") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${Subscription.class.name}' where idns_ns = 'ezb_collection_id';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-13") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${Org.class.name}' where idns_ns = 'ezb_org_id';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-14") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${Org.class.name}' where idns_ns = 'global';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-15") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${Org.class.name}' where idns_ns = 'gnd_org_nr';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-16") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${Org.class.name}' where idns_ns = 'GRID ID';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-17") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${Org.class.name}' where idns_ns = 'Leitkriterium (intern)';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-18") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${Org.class.name}' where idns_ns = 'Leitweg-ID';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-19") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${TitleInstance.class.name}' where idns_ns = 'mitpress';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-20") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${TitleInstance.class.name}' where idns_ns = 'oup';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-21") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${TitleInstance.class.name}' where idns_ns = 'pisbn';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-22") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${TitleInstance.class.name}' where idns_ns = 'preselect';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-23") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${TitleInstance.class.name}' where idns_ns = 'statssid';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-24") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${de.laser.Package.class.name}' where idns_ns = 'thieme';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-25") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${TitleInstance.class.name}' where idns_ns = 'utb';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-26") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${Org.class.name}' where idns_ns = 'VAT';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-27") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_type = '${Org.class.name}' where idns_ns = 'wibid';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-28") {
        grailsChange {
            change {
                String query = "delete from identifier where id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'hbz_at-ID');"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-29") {
        grailsChange {
            change {
                String query = "delete from identifier_namespace where idns_ns = 'hbz_at-ID';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-30") {
        grailsChange {
            change {
                String query = "delete from identifier where id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'originEditUrl');"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-31") {
        grailsChange {
            change {
                String query = "delete from identifier_namespace where idns_ns = 'originEditUrl';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-32") {
        grailsChange {
            change {
                String query = "delete from identifier where id_ns_fk = (select idns_id from identifier_namespace where idns_ns = 'unknown');"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-33") {
        grailsChange {
            change {
                String query = "delete from identifier_namespace where idns_ns = 'unknown';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-34") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_is_unique = false where idns_ns = 'DBIS Anker';"
                sql.execute( query )
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1607669186880-35") {
        grailsChange {
            change {
                String query = "update identifier_namespace set idns_is_unique = false where idns_ns = 'EZB anchor';"
                sql.execute( query )
            }
            rollback {}
        }
    }
}
