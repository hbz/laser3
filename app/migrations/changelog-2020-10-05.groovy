databaseChangeLog = {

	changeSet(author: "kloberd (modified)", id: "1601896321308-1") {
		grailsChange {
			change {
				sql.execute("update audit_log set class_name = 'de.laser.titles.BookInstance' where class_name = 'com.k_int.kbplus.BookInstance'")
				sql.execute("update audit_log set class_name = 'de.laser.titles.DatabaseInstance' where class_name = 'com.k_int.kbplus.DatabaseInstance'")
				sql.execute("update audit_log set class_name = 'de.laser.titles.JournalInstance' where class_name = 'com.k_int.kbplus.JournalInstance'")
				sql.execute("update audit_log set class_name = 'de.laser.titles.TitleInstance' where class_name = 'com.k_int.kbplus.TitleInstance'")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1601896321308-2") {
		grailsChange {
			change {
				sql.execute("update audit_log set new_value = replace(new_value, 'com.k_int.kbplus.BookInstance', 'de.laser.titles.BookInstance') where new_value is not null")
				sql.execute("update audit_log set new_value = replace(new_value, 'com.k_int.kbplus.DatabaseInstance', 'de.laser.titles.DatabaseInstance') where new_value is not null")
				sql.execute("update audit_log set new_value = replace(new_value, 'com.k_int.kbplus.JournalInstance', 'de.laser.titles.JournalInstance') where new_value is not null")
				sql.execute("update audit_log set new_value = replace(new_value, 'com.k_int.kbplus.TitleInstance', 'de.laser.titles.TitleInstance') where new_value is not null")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1601896321308-3") {
		grailsChange {
			change {
				sql.execute("update audit_log set old_value = replace(old_value, 'com.k_int.kbplus.BookInstance', 'de.laser.titles.BookInstance') where new_value is not null")
				sql.execute("update audit_log set old_value = replace(old_value, 'com.k_int.kbplus.DatabaseInstance', 'de.laser.titles.DatabaseInstance') where new_value is not null")
				sql.execute("update audit_log set old_value = replace(old_value, 'com.k_int.kbplus.JournalInstance', 'de.laser.titles.JournalInstance') where new_value is not null")
				sql.execute("update audit_log set old_value = replace(old_value, 'com.k_int.kbplus.TitleInstance', 'de.laser.titles.TitleInstance') where new_value is not null")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1601896321308-4") {
		grailsChange {
			change {
				sql.execute("update title_instance set class = replace(class, 'com.k_int.kbplus.', 'de.laser.titles.') where class is not null")
			}
			rollback {}
		}
	}
}
