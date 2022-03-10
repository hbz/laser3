import de.laser.IdentifierNamespace

databaseChangeLog = {

	changeSet(author: "klober (modified)", id: "1585548682322-1") {
		grailsChange {
			change {
				IdentifierNamespace isp = IdentifierNamespace.findByNs('ISIL_Paketsigel')
				if (!isp) {
					new IdentifierNamespace(
							ns: 'ISIL_Paketsigel',
							dateCreated: new Date(),
							lastUpdated: new Date()
					).save(flush: true)
				}
			}
		}
	}

	changeSet(author: "klober (modified)", id: "1585548682322-2") {
		grailsChange {
			change {
				sql.execute("""
update identifier set 
	id_ns_fk = ( select idns_id from identifier_namespace where idns_ns = 'ISIL_Paketsigel' ),
	id_last_updated = now()
where id_ns_fk = (
    select idns_id from identifier_namespace where idns_ns = 'ISIL'
) and ( id_sub_fk is not null or id_pkg_fk is not null );
                """)
			}
		}
	}

	changeSet(author: "klober (modified)", id: "1585548682322-3") {
		grailsChange {
			change {
				sql.execute("""
update identifier_namespace set 
	idns_type = 'com.k_int.kbplus.Org',
	idns_last_updated = now()
where idns_ns = 'ISIL';
""")
			}
		}
	}

	changeSet(author: "klober (modified)", id: "1585548682322-4") {
		grailsChange {
			IdentifierNamespace eoi = IdentifierNamespace.findByNs('ezb_org_id')
			if (!eoi) {
				new IdentifierNamespace(
						ns: 'ezb_org_id',
						nsType: 'com.k_int.kbplus.Org',
						dateCreated: new Date(),
						lastUpdated: new Date()
				).save(flush: true)
			}
		}
	}

	changeSet(author: "klober (modified)", id: "1585548682322-5") {
		grailsChange {
			change {
				sql.execute("""
update identifier set 
	id_ns_fk = ( select idns_id from identifier_namespace where idns_ns = 'ezb_org_id' ),
	id_last_updated = now()
where id_ns_fk = (
    select idns_id from identifier_namespace where idns_ns = 'ezb'
) and id_org_fk is not null;
                """)
			}
		}
	}

	changeSet(author: "klober (modified)", id: "1585548682322-6") {
		grailsChange {
			change {
				sql.execute("""
delete from identifier where id_ns_fk = (
	select idns_id from identifier_namespace where idns_ns = 'wibid'
) and id_org_fk is null;
""")
			}
		}
	}

	changeSet(author: "klober (modified)", id: "1585548682322-7") {
		grailsChange {
			change {
				sql.execute("""
update identifier_namespace set 
	idns_type = 'com.k_int.kbplus.Org',
	idns_last_updated = now() 
where idns_ns = 'wibid';
""")
			}
		}
	}

    changeSet(author: "klober (modified)", id: "1585548682322-8") {
        grailsChange {
            change {
                sql.execute("""
delete from identifier where id_ns_fk in (
    select idns_id from identifier_namespace where idns_ns in (
        'edb', 'ACM_TN', 'EBSCO_Deutschland_TN', 'EBSCO_TN', 'LexisNexis_TN', 'OECD_TN', 'Statista_TN', 'Taylor&Francis_TN'
    )
);
""")
            }
        }
    }

    changeSet(author: "klober (modified)", id: "1585548682322-9") {
        grailsChange {
            change {
                sql.execute("""
delete from identifier_namespace where idns_ns in (
    'edb', 'ACM_TN', 'EBSCO_Deutschland_TN', 'EBSCO_TN', 'LexisNexis_TN', 'OECD_TN', 'Statista_TN', 'Taylor&Francis_TN'
);
""")
			}
		}
	}

	changeSet(author: "klober (modified)", id: "1585548682322-10") {
		grailsChange {
			change {
				sql.execute("""
delete from identifier where id_sub_fk is not null and id_ns_fk = (
		select idns_id
from identifier_namespace
where idns_ns = 'Unkown'
);
""")
			}
		}
	}
}
