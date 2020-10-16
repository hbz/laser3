databaseChangeLog = {

	changeSet(author: "kloberd (modified)", id: "1602151961183-1") {
		grailsChange {
			change {
				sql.execute("update audit_config set auc_reference_class = 'de.laser.properties.LicenseProperty' where auc_reference_class = 'com.k_int.kbplus.LicenseProperty'")
				sql.execute("update audit_config set auc_reference_class = 'de.laser.properties.OrgProperty' where auc_reference_class = 'com.k_int.kbplus.OrgProperty'")
				sql.execute("update audit_config set auc_reference_class = 'de.laser.properties.PersonProperty' where auc_reference_class = 'com.k_int.kbplus.PersonProperty'")
				sql.execute("update audit_config set auc_reference_class = 'de.laser.properties.PlatformProperty' where auc_reference_class = 'com.k_int.kbplus.PlatformProperty'")
				sql.execute("update audit_config set auc_reference_class = 'de.laser.properties.SubscriptionProperty' where auc_reference_class = 'com.k_int.kbplus.SubscriptionProperty'")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1602151961183-2") {
		grailsChange {
			change {
				sql.execute("update audit_log set class_name = 'de.laser.properties.LicenseProperty' where class_name = 'com.k_int.kbplus.LicenseProperty'")
				sql.execute("update audit_log set class_name = 'de.laser.properties.OrgProperty' where class_name = 'com.k_int.kbplus.OrgProperty'")
				sql.execute("update audit_log set class_name = 'de.laser.properties.PersonProperty' where class_name = 'com.k_int.kbplus.PersonProperty'")
				sql.execute("update audit_log set class_name = 'de.laser.properties.PlatformProperty' where class_name = 'com.k_int.kbplus.PlatformProperty'")
				sql.execute("update audit_log set class_name = 'de.laser.properties.SubscriptionProperty' where class_name = 'com.k_int.kbplus.SubscriptionProperty'")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1602151961183-3") {
		grailsChange {
			change {
				sql.execute("update due_date_object set ddo_oid = replace(ddo_oid, 'com.k_int.kbplus.LicenseProperty', 'de.laser.properties.LicenseProperty') where ddo_oid is not null")
				sql.execute("update due_date_object set ddo_oid = replace(ddo_oid, 'com.k_int.kbplus.OrgProperty', 'de.laser.properties.OrgProperty') where ddo_oid is not null")
				sql.execute("update due_date_object set ddo_oid = replace(ddo_oid, 'com.k_int.kbplus.PersonProperty', 'de.laser.properties.PersonProperty') where ddo_oid is not null")
				sql.execute("update due_date_object set ddo_oid = replace(ddo_oid, 'com.k_int.kbplus.PlatformProperty', 'de.laser.properties.PlatformProperty') where ddo_oid is not null")
				sql.execute("update due_date_object set ddo_oid = replace(ddo_oid, 'com.k_int.kbplus.SubscriptionProperty', 'de.laser.properties.SubscriptionProperty') where ddo_oid is not null")
				}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1602151961183-4") {
		grailsChange {
			change {
				sql.execute("update pending_change set pc_payload = replace(pc_payload, 'com.k_int.kbplus.LicenseProperty', 'de.laser.properties.LicenseProperty') where pc_payload is not null")
				sql.execute("update pending_change set pc_payload = replace(pc_payload, 'com.k_int.kbplus.OrgProperty', 'de.laser.properties.OrgProperty') where pc_payload is not null")
				sql.execute("update pending_change set pc_payload = replace(pc_payload, 'com.k_int.kbplus.PersonProperty', 'de.laser.properties.PersonProperty') where pc_payload is not null")
				sql.execute("update pending_change set pc_payload = replace(pc_payload, 'com.k_int.kbplus.PlatformProperty', 'de.laser.properties.PlatformProperty') where pc_payload is not null")
				sql.execute("update pending_change set pc_payload = replace(pc_payload, 'com.k_int.kbplus.SubscriptionProperty', 'de.laser.properties.SubscriptionProperty') where pc_payload is not null")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1602151961183-5") {
		grailsChange {
			change {
				sql.execute("update pending_change set pc_msg_doc = replace(pc_msg_doc, 'com.k_int.kbplus.LicenseProperty', 'de.laser.properties.LicenseProperty') where pc_payload is not null")
				sql.execute("update pending_change set pc_msg_doc = replace(pc_msg_doc, 'com.k_int.kbplus.OrgProperty', 'de.laser.properties.OrgProperty') where pc_payload is not null")
				sql.execute("update pending_change set pc_msg_doc = replace(pc_msg_doc, 'com.k_int.kbplus.PersonProperty', 'de.laser.properties.PersonProperty') where pc_payload is not null")
				sql.execute("update pending_change set pc_msg_doc = replace(pc_msg_doc, 'com.k_int.kbplus.PlatformProperty', 'de.laser.properties.PlatformProperty') where pc_payload is not null")
				sql.execute("update pending_change set pc_msg_doc = replace(pc_msg_doc, 'com.k_int.kbplus.SubscriptionProperty', 'de.laser.properties.SubscriptionProperty') where pc_payload is not null")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1602151961183-6") {
		grailsChange {
			change {
				sql.execute("update audit_log set new_value = replace(new_value, 'com.k_int.properties.PropertyDefinition', 'de.laser.properties.PropertyDefinition') where new_value is not null")
				sql.execute("update audit_log set old_value = replace(old_value, 'com.k_int.properties.PropertyDefinition', 'de.laser.properties.PropertyDefinition') where old_value is not null")
			}
			rollback {}
		}
	}
}
