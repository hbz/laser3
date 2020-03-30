import com.k_int.kbplus.IdentifierNamespace

databaseChangeLog = {

	changeSet(author: "klober (modified)", id: "1585548682322-1") {
		grailsChange {
			change {
				IdentifierNamespace isp = IdentifierNamespace.findByNs('ISIL_Paketsigel')
				if (!isp) {
					new IdentifierNamespace(
							ns: 'ISIL_Paketsigel'
					).save(flush: true)
				}
			}
		}
	}

	changeSet(author: "klober (modified)", id: "1585548682322-2") {
		grailsChange {
			change {
				sql.execute("""
update identifier set id_ns_fk = (
    select idns_id from identifier_namespace where idns_ns = 'ISIL_Paketsigel'
)
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
				sql.execute("update identifier_namespace set idns_type = 'com.k_int.kbplus.Org' where idns_ns = 'ISIL';")
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
				).save(flush: true)
			}
		}
	}

	changeSet(author: "klober (modified)", id: "1585548682322-5") {
		grailsChange {
			change {
				sql.execute("""
update identifier set id_ns_fk = (
    select idns_id from identifier_namespace where idns_ns = 'ezb_org_id'
)
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
				sql.execute("update identifier_namespace set idns_type = 'com.k_int.kbplus.Org' where idns_ns = 'wibid';")
			}
		}
	}
}
