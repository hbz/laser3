databaseChangeLog = {

	changeSet(author: "djebeniani (modified)", id: "1583834028632-1") {
		grailsChange {
			change {
				sql.execute("""UPDATE subscription SET sub_kind_rv_fk = (SELECT rdv_id FROM refdata_value WHERE 
						rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.kind') AND rdv_value = 'Alliance Licence') 
						WHERE sub_type_rv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.type')
						AND rdv_value = 'Alliance Licence');""")

				sql.execute( """UPDATE subscription SET sub_kind_rv_fk = (SELECT rdv_id FROM refdata_value WHERE  
						rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.kind') AND rdv_value = 'National Licence')  
						WHERE sub_type_rv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.type')  
						AND rdv_value = 'National Licence');""")

				sql.execute( """UPDATE subscription SET sub_kind_rv_fk = (SELECT rdv_id FROM refdata_value WHERE  
						rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.kind') AND rdv_value = 'Consortial Licence')  
						WHERE sub_type_rv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.type')  
						AND rdv_value = 'Consortial Licence');""")

				sql.execute( """UPDATE subscription SET sub_type_rv_fk = (SELECT rdv_id FROM refdata_value WHERE  
						rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.type') AND rdv_value = 'Consortial Licence') 
						WHERE sub_type_rv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.type')  
						AND rdv_value = 'National Licence');""")

				sql.execute( """UPDATE subscription SET sub_type_rv_fk = (SELECT rdv_id FROM refdata_value WHERE  
						rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.type') AND rdv_value = 'Consortial Licence')  
						WHERE sub_type_rv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.type')  
						AND rdv_value = 'Alliance Licence');""")
			}
			rollback {
			}
		}
	}
	changeSet(author: "djebeniani (modified)", id: "1583834028632-2") {
		grailsChange {
			change {
				sql.execute("DELETE FROM refdata_value WHERE rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.type') AND rdv_value = 'Alliance Licence';")

				sql.execute("DELETE FROM refdata_value WHERE rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.type') AND rdv_value = 'National Licence';")
			}
			rollback {
			}
		}
	}

	changeSet(author: "djebeniani (modified)", id: "1583834028632-3") {
		grailsChange {
			change {

				sql.execute( """UPDATE subscription SET sub_status_rv_fk = (SELECT rdv_id FROM refdata_value WHERE  
						rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.status') AND rdv_value = 'Expired')  
						WHERE sub_status_rv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.status')  
						AND rdv_value = 'ExpiredPerennial');""")

				sql.execute( """UPDATE subscription SET sub_status_rv_fk = (SELECT rdv_id FROM refdata_value WHERE  
						rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.status') AND rdv_value = 'Intended')  
						WHERE sub_status_rv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.status')  
						AND rdv_value = 'IntendedPerennial');""")
			}
			rollback {
			}
		}
	}

	changeSet(author: "djebeniani (modified)", id: "1583834028632-4") {
		grailsChange {
			change {

				sql.execute("DELETE FROM refdata_value WHERE rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.status') AND rdv_value = 'ExpiredPerennial';")

				sql.execute("DELETE FROM refdata_value WHERE rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.status') AND rdv_value = 'IntendedPerennial';")
			}
			rollback {
			}
		}
	}
}
