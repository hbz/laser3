databaseChangeLog = {

	changeSet(author: "kloberd (modified)", id: "1601287052853-1") {
		grailsChange {
			change {
				sql.execute("update audit_log set new_value = replace(new_value, 'com.k_int.kbplus.OrgRole', 'de.laser.OrgRole') where new_value is not null")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1601287052853-2") {
		grailsChange {
			change {
				sql.execute("update audit_log set new_value = replace(new_value, 'com.k_int.kbplus.PersonRole', 'de.laser.PersonRole') where new_value is not null")
			}
			rollback {}
		}
	}

	changeSet(author: "klober (modified)", id: "1601287052853-3") {
		grailsChange {
			change {
				sql.execute("update org_access_point set class = replace(class, 'de.laser.OrgAccess', 'de.laser.oap.OrgAccess') where class is not null")
			}
			rollback {}
		}
	}

}
