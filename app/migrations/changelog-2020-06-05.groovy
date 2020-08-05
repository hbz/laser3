databaseChangeLog = {

	changeSet(author: "Moe (modified)", id: "1591357660544-1") {
		grailsChange {
			change {
				sql.execute("""UPDATE survey_org SET surorg_owner_comment = (
							SELECT surre_owner_comment FROM survey_result 
							WHERE surre_participant_fk = surorg_org_fk 
							AND surre_survey_config_fk = surorg_surveyconfig_fk
  							AND surre_type_fk = (SELECT pd_id FROM property_definition WHERE pd_description = 'Survey Property' AND pd_name = 'Participation' AND pd_tenant_fk IS NULL) 
  							GROUP BY surre_participant_fk, surre_owner_comment)""")
			}
			rollback {}
		}
	}

	changeSet(author: "agalffy (modified)", id: "1591357660544-2") {
		grailsChange {
			change {
				sql.execute("alter table links alter column l_source_fk type character varying(255)")
			}
		}
	}

	changeSet(author: "agalffy (modified)", id: "1591357660544-3") {
		grailsChange {
			change {
				sql.execute("alter table links alter column l_destination_fk type character varying(255)")
			}
		}
	}

	changeSet(author: "agalffy (modified)", id: "1591357660544-4") {
		grailsChange {
			change {
				sql.execute("update links set l_source_fk = concat(l_object,':',l_source_fk)")
			}
		}
	}

	changeSet(author: "agalffy (modified)", id: "1591357660544-5") {
		grailsChange {
			change {
				sql.execute("update links set l_destination_fk = concat(l_object,':',l_destination_fk)")
			}
		}
	}

	changeSet(author: "agalffy (modified)", id: "1591357660544-6") {
		dropColumn(columnName: "l_object", tableName: "links")
	}

}