databaseChangeLog = {

	changeSet(author: "Moe (modified)", id: "1588842970521-1") {
		grailsChange {
			change {
				sql.execute("UPDATE user_settings SET us_key_enum = 'IS_REMIND_FOR_SURVEYS_NOT_MANDATORY_ENDDATE', us_rv_fk = null WHERE us_key_enum = 'IS_REMIND_FOR_SURVEYS_ENDDATE';")
				sql.execute("UPDATE user_settings SET us_key_enum = 'REMIND_PERIOD_FOR_SURVEYS_NOT_MANDATORY_ENDDATE' WHERE us_key_enum = 'REMIND_PERIOD_FOR_SURVEYS_ENDDATE';")
			}
			rollback {}
		}
	}

}