databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1566992531378-1") {
		addColumn(schemaName: "public", tableName: "survey_config") {
			column(name: "surconf_is_subscription_survey_fix", type: "bool")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1566992531378-2") {
		addColumn(schemaName: "public", tableName: "survey_info") {
			column(name: "surin_is_subscription_survey", type: "bool")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1566992531378-3") {
		addColumn(schemaName: "public", tableName: "survey_result") {
			column(name: "surre_owner_comment", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (modified)", id: "1566992531378-4") {
		grailsChange {
			change {
				sql.execute("UPDATE survey_info SET surin_is_subscription_survey = true")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1566992531378-5") {
		grailsChange {
			change {
				sql.execute("UPDATE survey_config SET surconf_is_subscription_survey_fix = true WHERE surconf_sub_fk IS NOT NULL")
				sql.execute("UPDATE survey_config SET surconf_is_subscription_survey_fix = false WHERE surconf_sub_fk IS NULL")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1566992531378-6") {
		grailsChange {
			change {
				sql.execute("update i10n_translation set i10n_value_de = 'Wissenschaftliche Spezialbibliothek' where i10n_value_de = 'Wissenschafltiche Spezialbibliothek'")
				sql.execute("update i10n_translation set i10n_value_en = 'Wissenschaftliche Spezialbibliothek' where i10n_value_en = 'Wissenschafltiche Spezialbibliothek'")
				sql.execute("update refdata_value set rdv_value = 'Wissenschaftliche Spezialbibliothek' where rdv_value = 'Wissenschafltiche Spezialbibliothek'")
			}
			rollback {}
		}
	}
}
