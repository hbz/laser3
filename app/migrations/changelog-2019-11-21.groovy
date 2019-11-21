databaseChangeLog = {

	changeSet(author: "kloberd (modified)", id: "1574333247712-1") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE pending_change RENAME pc_change_doc TO pc_payload")
			}
			rollback {}
		}
	}
}
