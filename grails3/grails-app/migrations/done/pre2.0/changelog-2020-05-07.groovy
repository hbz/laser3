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

	changeSet(author: "klober (generated)", id: "1588842970521-2") {
		dropForeignKeyConstraint(baseTableName: "address", baseTableSchemaName: "public", constraintName: "fkbb979bf4ab162193")
	}

	changeSet(author: "klober (modified)", id: "1588842970521-3") {
		grailsChange {
			change {
				sql.execute("ALTER TABLE address RENAME COLUMN adr_state_rv_fk TO adr_region_rv_fk")
			}
			rollback {}
		}
	}

	changeSet(author: "klober (generated)", id: "1588842970521-4") {
		addForeignKeyConstraint(baseColumnNames: "adr_region_rv_fk", baseTableName: "address", baseTableSchemaName: "public", constraintName: "FKBB979BF4541D6AD6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "klober (generated)", id: "1588842970521-5") {
		dropForeignKeyConstraint(baseTableName: "org", baseTableSchemaName: "public", constraintName: "fk1aee47ffcf8a6")
	}

	changeSet(author: "klober (generated)", id: "1588842970521-6") {
		dropColumn(columnName: "org_federal_state_rv_fk", tableName: "org")
	}

	changeSet(author: "klober (modified)", id: "1588842970521-7") {
		grailsChange {
			change {
				sql.execute("""
update org set org_region_rv_fk = null where org_region_rv_fk in (
    select rdv_id
    from refdata_value
    where rdv_owner = (select rdc_id from refdata_category where rdc_description = 'regions.de')
)""")
			}
			rollback {}
		}
	}

	changeSet(author: "klober (modified)", id: "1588842970521-8") {
		grailsChange {
			change {
				sql.execute("delete from refdata_value where rdv_owner = (select rdc_id from refdata_category where rdc_description = 'regions.de')")
			}
			rollback {}
		}
	}

	changeSet(author: "klober (modified)", id: "1588842970521-9") {
		grailsChange {
			change {
				sql.execute("""
update refdata_value
set rdv_owner = (select rdc_id from refdata_category where rdc_description = 'regions.de')
where rdv_owner = (select rdc_id from refdata_category where rdc_description = 'federal.state')
""")
			}
			rollback {}
		}
	}

	changeSet(author: "klober (modified)", id: "1588842970521-10") {
		grailsChange {
			change {
				sql.execute("delete from refdata_category where rdc_description = 'federal.state'")
			}
			rollback {}
		}
	}
}