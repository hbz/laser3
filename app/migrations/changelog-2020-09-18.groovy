databaseChangeLog = {

	changeSet(author: "kloberd (modified)", id: "1600414811405-1") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE setting RENAME TO system_setting")
			}
			rollback {}
		}
	}
}
