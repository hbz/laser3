databaseChangeLog = {

	changeSet(author: "kloberd (modified)", id: "1602225499103-1") {
		grailsChange {
			change {
				sql.execute("update audit_config set auc_reference_class = 'de.laser.License' 		where auc_reference_class = 'com.k_int.kbplus.License'")
				sql.execute("update audit_config set auc_reference_class = 'de.laser.Org' 			where auc_reference_class = 'com.k_int.kbplus.Org'")
				sql.execute("update audit_config set auc_reference_class = 'de.laser.Package' 		where auc_reference_class = 'com.k_int.kbplus.Package'")
				sql.execute("update audit_config set auc_reference_class = 'de.laser.Platform' 		where auc_reference_class = 'com.k_int.kbplus.Platform'")
				sql.execute("update audit_config set auc_reference_class = 'de.laser.Subscription'  where auc_reference_class = 'com.k_int.kbplus.Subscription'")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1602225499103-2") {
		grailsChange {
			change {
				sql.execute("update audit_log set class_name = 'de.laser.License' 		where class_name = 'com.k_int.kbplus.License'")
				sql.execute("update audit_log set class_name = 'de.laser.Org' 			where class_name = 'com.k_int.kbplus.Org'")
				sql.execute("update audit_log set class_name = 'de.laser.Package' 		where class_name = 'com.k_int.kbplus.Package'")
				sql.execute("update audit_log set class_name = 'de.laser.Platform' 		where class_name = 'com.k_int.kbplus.Platform'")
				sql.execute("update audit_log set class_name = 'de.laser.Subscription'  where class_name = 'com.k_int.kbplus.Subscription'")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1602225499103-3") {
		grailsChange {
			change {
				sql.execute("update audit_log set new_value = replace(new_value, 'com.k_int.kbplus.License :',		'de.laser.License :') where new_value is not null")
				sql.execute("update audit_log set new_value = replace(new_value, 'com.k_int.kbplus.Org :',			'de.laser.Org :') where new_value is not null")
				sql.execute("update audit_log set new_value = replace(new_value, 'com.k_int.kbplus.Package :',		'de.laser.Package :') where new_value is not null")
				sql.execute("update audit_log set new_value = replace(new_value, 'com.k_int.kbplus.Platform :',		'de.laser.Platform :') where new_value is not null")
				sql.execute("update audit_log set new_value = replace(new_value, 'com.k_int.kbplus.Subscription :',	'de.laser.Subscription :') where new_value is not null")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1602225499103-4") {
		grailsChange {
			change {
				sql.execute("update audit_log set old_value = replace(old_value, 'com.k_int.kbplus.License :',		'de.laser.License :') where old_value is not null")
				sql.execute("update audit_log set old_value = replace(old_value, 'com.k_int.kbplus.Org :', 			'de.laser.Org :') where old_value is not null")
				sql.execute("update audit_log set old_value = replace(old_value, 'com.k_int.kbplus.Package :', 		'de.laser.Package :') where old_value is not null")
				sql.execute("update audit_log set old_value = replace(old_value, 'com.k_int.kbplus.Platform :', 	'de.laser.Platform :') where old_value is not null")
				sql.execute("update audit_log set old_value = replace(old_value, 'com.k_int.kbplus.Subscription :',	'de.laser.Subscription :') where old_value is not null")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1602225499103-5") {
		grailsChange {
			change {
				sql.execute("update change_notification_queue_item set cnqi_oid = replace(cnqi_oid, 'com.k_int.kbplus.License:', 	  'de.laser.License:') where cnqi_oid is not null")
				sql.execute("update change_notification_queue_item set cnqi_oid = replace(cnqi_oid, 'com.k_int.kbplus.Org:',		  'de.laser.Org:') where cnqi_oid is not null")
				sql.execute("update change_notification_queue_item set cnqi_oid = replace(cnqi_oid, 'com.k_int.kbplus.Package:',	  'de.laser.Package:') where cnqi_oid is not null")
				sql.execute("update change_notification_queue_item set cnqi_oid = replace(cnqi_oid, 'com.k_int.kbplus.Platform:',	  'de.laser.Platform:') where cnqi_oid is not null")
				sql.execute("update change_notification_queue_item set cnqi_oid = replace(cnqi_oid, 'com.k_int.kbplus.Subscription:', 'de.laser.Subscription:') where cnqi_oid is not null")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1602225499103-6") {
		grailsChange {
			change {
				sql.execute("update change_notification_queue_item set cnqi_change_document = replace(cnqi_change_document, 'com.k_int.kbplus.License:',      'de.laser.License:') where cnqi_change_document is not null")
				sql.execute("update change_notification_queue_item set cnqi_change_document = replace(cnqi_change_document, 'com.k_int.kbplus.Org:', 		  'de.laser.Org:') where cnqi_change_document is not null")
				sql.execute("update change_notification_queue_item set cnqi_change_document = replace(cnqi_change_document, 'com.k_int.kbplus.Package:', 	  'de.laser.Package:') where cnqi_change_document is not null")
				sql.execute("update change_notification_queue_item set cnqi_change_document = replace(cnqi_change_document, 'com.k_int.kbplus.Platform:', 	  'de.laser.Platform:') where cnqi_change_document is not null")
				sql.execute("update change_notification_queue_item set cnqi_change_document = replace(cnqi_change_document, 'com.k_int.kbplus.Subscription:', 'de.laser.Subscription:') where cnqi_change_document is not null")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1602225499103-7") {
		grailsChange {
			change {
				sql.execute("update due_date_object set ddo_oid = replace(ddo_oid, 'com.k_int.kbplus.License:', 	 'de.laser.License:') where ddo_oid is not null")
				sql.execute("update due_date_object set ddo_oid = replace(ddo_oid, 'com.k_int.kbplus.Org:', 		 'de.laser.Org:') where ddo_oid is not null")
				sql.execute("update due_date_object set ddo_oid = replace(ddo_oid, 'com.k_int.kbplus.Package:', 	 'de.laser.Package:') where ddo_oid is not null")
				sql.execute("update due_date_object set ddo_oid = replace(ddo_oid, 'com.k_int.kbplus.Platform:', 	 'de.laser.Platform:') where ddo_oid is not null")
				sql.execute("update due_date_object set ddo_oid = replace(ddo_oid, 'com.k_int.kbplus.Subscription:', 'de.laser.Subscription:') where ddo_oid is not null")
				}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1602225499103-8") {
		grailsChange {
			change {
				sql.execute("update identifier_namespace set idns_type = 'de.laser.License' 	 where idns_type = 'com.k_int.kbplus.License'")
				sql.execute("update identifier_namespace set idns_type = 'de.laser.Org' 		 where idns_type = 'com.k_int.kbplus.Org'")
				sql.execute("update identifier_namespace set idns_type = 'de.laser.Package' 	 where idns_type = 'com.k_int.kbplus.Package'")
				sql.execute("update identifier_namespace set idns_type = 'de.laser.Platform' 	 where idns_type = 'com.k_int.kbplus.Platform'")
				sql.execute("update identifier_namespace set idns_type = 'de.laser.Subscription' where idns_type = 'com.k_int.kbplus.Subscription'")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1602225499103-9") {
		grailsChange {
			change {
				sql.execute("update pending_change set pc_payload = replace(pc_payload, 'com.k_int.kbplus.License:', 	  'de.laser.License:') where pc_payload is not null")
				sql.execute("update pending_change set pc_payload = replace(pc_payload, 'com.k_int.kbplus.Org:', 	      'de.laser.Org:') where pc_payload is not null")
				sql.execute("update pending_change set pc_payload = replace(pc_payload, 'com.k_int.kbplus.Package:', 	  'de.laser.Package:') where pc_payload is not null")
				sql.execute("update pending_change set pc_payload = replace(pc_payload, 'com.k_int.kbplus.Platform:', 	  'de.laser.Platform:') where pc_payload is not null")
				sql.execute("update pending_change set pc_payload = replace(pc_payload, 'com.k_int.kbplus.Subscription:', 'de.laser.Subscription:') where pc_payload is not null")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1602225499103-10") {
		grailsChange {
			change {
				sql.execute("update pending_change set pc_oid = replace(pc_oid, 'com.k_int.kbplus.License:', 	 'de.laser.License:') where pc_oid is not null")
				sql.execute("update pending_change set pc_oid = replace(pc_oid, 'com.k_int.kbplus.Org:', 		 'de.laser.Org:') where pc_oid is not null")
				sql.execute("update pending_change set pc_oid = replace(pc_oid, 'com.k_int.kbplus.Package:', 	 'de.laser.Package:') where pc_oid is not null")
				sql.execute("update pending_change set pc_oid = replace(pc_oid, 'com.k_int.kbplus.Platform:', 	 'de.laser.Platform:') where pc_oid is not null")
				sql.execute("update pending_change set pc_oid = replace(pc_oid, 'com.k_int.kbplus.Subscription:', 'de.laser.Subscription:') where pc_oid is not null")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1602225499103-11") {
		grailsChange {
			change {
				sql.execute("update property_definition_group set pdg_owner_type = 'de.laser.License' 		where pdg_owner_type = 'com.k_int.kbplus.License'")
				sql.execute("update property_definition_group set pdg_owner_type = 'de.laser.Org' 			where pdg_owner_type = 'com.k_int.kbplus.Org'")
				sql.execute("update property_definition_group set pdg_owner_type = 'de.laser.Package' 		where pdg_owner_type = 'com.k_int.kbplus.Package'")
				sql.execute("update property_definition_group set pdg_owner_type = 'de.laser.Platform' 		where pdg_owner_type = 'com.k_int.kbplus.Platform'")
				sql.execute("update property_definition_group set pdg_owner_type = 'de.laser.Subscription'  where pdg_owner_type = 'com.k_int.kbplus.Subscription'")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (generated)", id: "1602225499103-12") {
		dropForeignKeyConstraint(baseTableName: "subscription", baseTableSchemaName: "public", constraintName: "fk1456591d93740d18")
	}

	changeSet(author: "galffy (generated)", id: "1602225499103-13") {
		dropColumn(columnName: "sub_previous_subscription_fk", tableName: "subscription")
	}

}
