databaseChangeLog = {

	changeSet(author: "kloberd (modified)", id: "1574333247712-1") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE pending_change RENAME pc_change_doc TO pc_payload")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1574333247712-2") {
		grailsChange {
			change {
				sql.execute("delete from user_settings where us_key_enum like 'DASHBOARD_REMINDER_PERIOD'")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1574333247712-3") {
		grailsChange {
			change {
				sql.execute("alter table dashboard_due_date RENAME das_is_hide TO das_is_hidden")
			}
			rollback {}
		}
	}
}
