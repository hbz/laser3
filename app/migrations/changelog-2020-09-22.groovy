databaseChangeLog = {

	changeSet(author: "kloberd (modified)", id: "1600754308495-1") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE org_settings RENAME TO org_setting")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1600754308495-2") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE user_settings RENAME TO user_setting")
			}
			rollback {}
		}
	}
}
