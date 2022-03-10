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

	changeSet(author: "kloberd (modified)", id: "1601896321308-5") {
		grailsChange {
			change {
				sql.execute("update audit_log set class_name = 'de.laser.TitleInstancePackagePlatform' where class_name = 'com.k_int.kbplus.TitleInstancePackagePlatform'")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1601896321308-6") {
		grailsChange {
			change {
				sql.execute("update change_notification_queue_item set cnqi_oid = replace(cnqi_oid, 'com.k_int.kbplus.TitleInstancePackagePlatform', 'de.laser.TitleInstancePackagePlatform') where cnqi_oid is not null")
				sql.execute("update change_notification_queue_item set cnqi_change_document = replace(cnqi_change_document, 'com.k_int.kbplus.TitleInstancePackagePlatform', 'de.laser.TitleInstancePackagePlatform') where cnqi_change_document is not null")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1601896321308-7") {
		grailsChange {
			change {
				sql.execute("update pending_change set pc_oid = replace(pc_oid, 'com.k_int.kbplus.TitleInstancePackagePlatform', 'de.laser.TitleInstancePackagePlatform') where pc_oid is not null")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1601896321308-8") {
		grailsChange {
			change {
				sql.execute("update pending_change set pc_payload = replace(pc_payload, 'com.k_int.kbplus.TitleInstancePackagePlatform', 'de.laser.TitleInstancePackagePlatform') where pc_payload is not null")
				sql.execute("update pending_change set pc_msg_doc = replace(pc_msg_doc, 'com.k_int.kbplus.TitleInstancePackagePlatform', 'de.laser.TitleInstancePackagePlatform') where pc_msg_doc is not null")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1601896321308-9") {
		grailsChange {
			change {
				sql.execute("update pending_change set pc_oid = replace(pc_oid, 'com.k_int.kbplus.IssueEntitlement', 'de.laser.IssueEntitlement') where pc_oid is not null")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1601896321308-10") {
		grailsChange {
			change {
				sql.execute("update pending_change set pc_payload = replace(pc_payload, 'com.k_int.kbplus.IssueEntitlement', 'de.laser.IssueEntitlement') where pc_payload is not null")
				sql.execute("update pending_change set pc_msg_doc = replace(pc_msg_doc, 'com.k_int.kbplus.IssueEntitlement', 'de.laser.IssueEntitlement') where pc_msg_doc is not null")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1601896321308-11") {
		grailsChange {
			change {
				sql.execute("update audit_log set old_value = replace(old_value, 'com.k_int.kbplus.PendingChange', 'de.laser.PendingChange') where old_value is not null")
				sql.execute("update audit_log set new_value = replace(new_value, 'com.k_int.kbplus.PendingChange', 'de.laser.PendingChange') where new_value is not null")
			}
			rollback {}
		}
	}
}
