import de.laser.RefdataCategory
import de.laser.RefdataValue

databaseChangeLog = {

	changeSet(author: "galffy (hand-coded)", id: "1584706679823-1") {
		grailsChange {
			change {
				sql.execute("""UPDATE cost_item SET ci_billing_currency_rv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_value = 'EUR') WHERE ci_billing_currency_rv_fk is null;""")
			}
		}
	}

	changeSet(author: "djebeniani (generated)", id: "1584706679823-2") {
		addNotNullConstraint(columnDataType: "int8", columnName: "ci_billing_currency_rv_fk", tableName: "cost_item")
	}

	changeSet(author: "djebeniani (generated)", id: "1584706679823-3") {
		addNotNullConstraint(columnDataType: "int8", columnName: "ci_billing_currency_rv_fk", tableName: "cost_item")
	}

	changeSet(author: "djebeniani (modified)", id: "1584706679823-4") {
		grailsChange {
			change {

				RefdataCategory rdc = RefdataCategory.findByDescIlike('filter.fake.values')

				if (!rdc) {
					rdc = new RefdataCategory(desc: 'filter.fake.values')
				}
				rdc.save(flush: true)
				if (rdc) {

					RefdataValue rdv = RefdataValue.findByOwnerAndValueIlike(rdc, 'generic.null.value')
					if (!rdv) {
						rdv = new RefdataValue(owner: rdc, value: 'generic.null.value')
					}
					rdv.save(flush: true)

					sql.execute("""UPDATE cost_item SET ci_status_rv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_value = 'generic.null.value') WHERE ci_status_rv_fk is null;""")
				}
			}
			rollback {
			}
		}
	}

	changeSet(author: "djebeniani (modified)", id: "1584706679823-5") {
		grailsChange {
			change {
				sql.execute("""ALTER TABLE public.cost_item ALTER COLUMN ci_status_rv_fk SET NOT NULL;""")
			}
			rollback {
			}
		}
	}

	changeSet(author: "djebeniani (generated)", id: "1584706679823-6") {
		addNotNullConstraint(columnDataType: "int8", columnName: "ci_status_rv_fk", tableName: "cost_item")
	}

	changeSet(author: "djebeniani (generated)", id: "1584706679823-7") {
		dropForeignKeyConstraint(baseTableName: "survey_config", baseTableSchemaName: "public", constraintName: "fk79dbbfc7b109e788")
	}

	changeSet(author: "djebeniani (generated)", id: "1584706679823-8") {
		dropForeignKeyConstraint(baseTableName: "survey_config_properties", baseTableSchemaName: "public", constraintName: "fkee6a50aba2c7e99a")
	}

	changeSet(author: "djebeniani (generated)", id: "1584706679823-9") {
		dropForeignKeyConstraint(baseTableName: "survey_result", baseTableSchemaName: "public", constraintName: "fk92ea04a27e23dd3a")
	}

	changeSet(author: "djebeniani (modified)", id: "1584706679823-10") {
		grailsChange {
			change {
				sql.execute("""INSERT INTO property_definition (pd_description, pd_name, pd_name_de, pd_name_en, pd_rdc, pd_tenant_fk, pd_type, pd_explanation_de, pd_explanation_en, version, pd_hard_data, pd_mandatory, pd_multiple_occurrence, pd_used_for_logic)
								SELECT 
									'Survey Property',
								   surpro_name,
								   i10nName.i10n_value_de,
								   i10nName.i10n_value_en,
								   surpro_refdata_category,
								   surpro_org_fk,
								   surpro_type,
								   i10nExpl.i10n_value_de,
								   i10nExpl.i10n_value_en,
								   0,
								   false,
								   false,
								   false,
								   false
    							FROM survey_property
       							LEFT JOIN i10n_translation as i10nExpl on (surpro_id = i10nExpl.i10n_reference_id and i10nExpl.i10n_reference_class = 'com.k_int.kbplus.SurveyProperty' and i10nExpl.i10n_reference_field = 'expl')
								LEFT JOIN i10n_translation as i10nName on (surpro_id = i10nName.i10n_reference_id and i10nName.i10n_reference_class = 'com.k_int.kbplus.SurveyProperty' and i10nName.i10n_reference_field = 'name');""")
			}
			rollback {
			}
		}
	}

	changeSet(author: "djebeniani (modified)", id: "1584706679823-11") {
		grailsChange {
			change {
				sql.execute("""UPDATE survey_config_properties SET surconpro_survey_property_fk = property_definition.pd_id FROM property_definition WHERE pd_name = (SELECT surpro_name FROM survey_property WHERE surpro_id = surconpro_survey_property_fk);""")
			}
			rollback {
			}
		}
	}

	changeSet(author: "djebeniani (modified)", id: "1584706679823-12") {
		grailsChange {
			change {
				sql.execute("""UPDATE survey_config SET surconf_surprop_fk = property_definition.pd_id FROM property_definition WHERE pd_name = (SELECT surpro_name FROM survey_property WHERE surpro_id = surconf_surprop_fk);""")
			}
			rollback {
			}
		}
	}

	changeSet(author: "djebeniani (modified)", id: "1584706679823-13") {
		grailsChange {
			change {
				sql.execute("""UPDATE survey_result SET surre_type_fk = property_definition.pd_id FROM property_definition WHERE pd_name = (SELECT surpro_name FROM survey_property WHERE surpro_id = surre_type_fk);""")
			}
			rollback {
			}
		}
	}

	changeSet(author: "djebeniani (generated)", id: "1584706679823-14") {
		addForeignKeyConstraint(baseColumnNames: "surconf_surprop_fk", baseTableName: "survey_config", baseTableSchemaName: "public", constraintName: "FK79DBBFC7619859BF", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pd_id", referencedTableName: "property_definition", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "djebeniani (generated)", id: "1584706679823-15") {
		addForeignKeyConstraint(baseColumnNames: "surconpro_survey_property_fk", baseTableName: "survey_config_properties", baseTableSchemaName: "public", constraintName: "FKEE6A50AB53565BD1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pd_id", referencedTableName: "property_definition", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "djebeniani (generated)", id: "1584706679823-16") {
		addForeignKeyConstraint(baseColumnNames: "surre_type_fk", baseTableName: "survey_result", baseTableSchemaName: "public", constraintName: "FK92EA04A22EB24F71", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pd_id", referencedTableName: "property_definition", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "djebeniani (generated)", id: "1584706679823-17") {
		addColumn(schemaName: "public", tableName: "survey_config") {
			column(name: "surconf_url", type: "varchar(512)")
		}
	}


}
