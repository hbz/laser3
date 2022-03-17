databaseChangeLog = {

	changeSet(author: "kloberd (modified)", id: "1600414811405-1") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE setting RENAME TO system_setting")
			}
			rollback {}
		}
	}

	changeSet(author: "klober (modified)", id: "1600414811405-2") {
		grailsChange {
			change {
				sql.execute("update due_date_object set ddo_oid = replace(ddo_oid, 'com.k_int.kbplus.SurveyInfo', 'de.laser.SurveyInfo') where ddo_oid is not null")
				sql.execute("update due_date_object set ddo_oid = replace(ddo_oid, 'com.k_int.kbplus.Task', 'de.laser.Task') where ddo_oid is not null")
			}
			rollback {}
		}
	}

	changeSet(author: "klober (modified)", id: "1600414811405-3") {
		grailsChange {
			change {
				sql.execute("update pending_change set pc_msg_doc = replace(pc_msg_doc, 'com.k_int.properties.', 'de.laser.properties.')")
			}
			rollback {}
		}
	}

	changeSet(author: "klober (modified)", id: "1600414811405-4") {
		grailsChange {
			change {
				sql.execute("update pending_change set pc_oid = replace(pc_oid, 'de.laser.domain.', 'de.laser.')")
			}
			rollback {}
		}
	}

	changeSet(author: "klober (modified)", id: "1600414811405-5") {
		grailsChange {
			change {
				sql.execute("update pending_change set pc_payload = replace(pc_payload, 'com.k_int.kbplus.CostItem', 'de.laser.finance.CostItem')")
			}
			rollback {}
		}
	}

	changeSet(author: "klober (modified)", id: "1600414811405-6") {
		grailsChange {
			change {
				sql.execute("update property_definition set pd_type = replace(pd_type, 'com.k_int.kbplus.Refdata', 'de.laser.Refdata')")
				sql.execute("update survey_property set surpro_type = replace(surpro_type, 'com.k_int.kbplus.Refdata', 'de.laser.Refdata')")
			}
			rollback {}
		}
	}

	changeSet(author: "klober (modified)", id: "1600414811405-7") {
		grailsChange {
			change {
				sql.execute("update change_notification_queue_item set cnqi_change_document = replace(cnqi_change_document, 'com.k_int.kbplus.Refdata', 'de.laser.Refdata')")
			}
			rollback {}
		}
	}

	changeSet(author: "klober (modified)", id: "1600414811405-8") {
		grailsChange {
			change {
				sql.execute("update pending_change set pc_payload = replace(pc_payload, 'com.k_int.kbplus.Refdata', 'de.laser.Refdata')")
				sql.execute("update pending_change set pc_msg_doc = replace(pc_msg_doc, 'com.k_int.kbplus.Refdata', 'de.laser.Refdata')")
			}
			rollback {}
		}
	}

}
