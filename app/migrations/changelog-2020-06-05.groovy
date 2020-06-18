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

	changeSet(author: "klober (modified)", id: "1592465779044-x") {
		// testing MigrationCallbacks; empty changeSet will be removed next commit
	}
}
