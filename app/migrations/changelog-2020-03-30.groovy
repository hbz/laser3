import com.k_int.kbplus.IdentifierNamespace

databaseChangeLog = {

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
}
