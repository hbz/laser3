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

	changeSet(author: "kloberd (modified)", id: "1600754308495-3") {
		grailsChange {
			change {
				sql.execute("ALTER SEQUENCE org_settings_os_id_seq RENAME TO org_setting_os_id_seq")
				sql.execute("ALTER TABLE org_setting ALTER COLUMN os_id SET DEFAULT nextval('public.org_setting_os_id_seq'::regclass)")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1600754308495-4") {
		grailsChange {
			change {
				sql.execute("ALTER SEQUENCE user_settings_us_id_seq RENAME TO user_setting_us_id_seq")
				sql.execute("ALTER TABLE user_setting ALTER COLUMN us_id SET DEFAULT nextval('public.user_setting_us_id_seq'::regclass)")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1600754308495-5") {
		grailsChange {
			change {
				sql.execute("ALTER SEQUENCE license_custom_property_id_seq RENAME TO license_property_lp_id_seq")
				sql.execute("ALTER TABLE license_property ALTER COLUMN lp_id SET DEFAULT nextval('public.license_property_lp_id_seq'::regclass)")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1600754308495-6") {
		grailsChange {
			change {
				sql.execute("ALTER SEQUENCE org_custom_property_id_seq RENAME TO org_property_op_id_seq")
				sql.execute("ALTER TABLE org_property ALTER COLUMN op_id SET DEFAULT nextval('public.org_property_op_id_seq'::regclass)")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1600754308495-7") {
		grailsChange {
			change {
				sql.execute("ALTER SEQUENCE person_private_property_ppp_id_seq RENAME TO person_property_pp_id_seq")
				sql.execute("ALTER TABLE person_property ALTER COLUMN pp_id SET DEFAULT nextval('public.person_property_pp_id_seq'::regclass)")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1600754308495-8") {
		grailsChange {
			change {
				sql.execute("ALTER SEQUENCE platform_custom_property_id_seq RENAME TO platform_property_plp_id_seq")
				sql.execute("ALTER TABLE platform_property ALTER COLUMN plp_id SET DEFAULT nextval('public.platform_property_plp_id_seq'::regclass)")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1600754308495-9") {
		grailsChange {
			change {
				sql.execute("ALTER SEQUENCE subscription_custom_property_id_seq RENAME TO subscription_property_sp_id_seq")
				sql.execute("ALTER TABLE subscription_property ALTER COLUMN sp_id SET DEFAULT nextval('public.subscription_property_sp_id_seq'::regclass)")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1600754308495-10") {
		grailsChange {
			change {
				sql.execute("ALTER SEQUENCE setting_set_id_seq RENAME TO system_setting_set_id_seq")
				sql.execute("ALTER TABLE system_setting ALTER COLUMN set_id SET DEFAULT nextval('public.system_setting_set_id_seq'::regclass)")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1600754308495-11") {
		grailsChange {
			change {
				sql.execute("ALTER SEQUENCE numbers_num_id_seq RENAME TO reader_number_num_id_seq")
				sql.execute("ALTER TABLE reader_number ALTER COLUMN num_id SET DEFAULT nextval('public.reader_number_num_id_seq'::regclass)")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1600754308495-12") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE activity_profiler RENAME TO system_activity_profiler")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1600754308495-13") {
		grailsChange {
			change {
				sql.execute("ALTER SEQUENCE activity_profiler_ap_id_seq RENAME TO system_activity_profiler_ap_id_seq")
				sql.execute("ALTER TABLE system_activity_profiler ALTER COLUMN ap_id SET DEFAULT nextval('public.system_activity_profiler_ap_id_seq'::regclass)")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1600754308495-14") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE system_activity_profiler RENAME COLUMN ap_id TO sap_id")
				sql.execute("ALTER TABLE system_activity_profiler RENAME COLUMN ap_version TO sap_version")
				sql.execute("ALTER TABLE system_activity_profiler RENAME COLUMN ap_date_created TO sap_date_created")
				sql.execute("ALTER TABLE system_activity_profiler RENAME COLUMN ap_user_count TO sap_user_count")
			}
			rollback {}
		}
	}

	changeSet(author: "klober (modified)", id: "1600754308495-15") {
		grailsChange {
			change {
				sql.execute("update audit_log set new_value = replace(new_value, 'com.k_int.kbplus.Identifier', 'de.laser.Identifier') where new_value is not null")
			}
			rollback {}
		}
	}

	changeSet(author: "klober (modified)", id: "1600754308495-16") {
		grailsChange {
			change {
				sql.execute("update audit_log set new_value = replace(new_value, 'com.k_int.kbplus.DocContext', 'de.laser.DocContext') where new_value is not null")
			}
			rollback {}
		}
	}
}
