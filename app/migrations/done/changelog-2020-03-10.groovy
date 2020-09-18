import de.laser.RefdataCategory
import de.laser.RefdataValue

databaseChangeLog = {

	changeSet(author: "djebeniani (modified)", id: "1583834028632-1") {
		grailsChange {
			change {

				RefdataCategory rdc = RefdataCategory.findByDescIlike('subscription.kind')

				if (! rdc) {
					rdc = new RefdataCategory(desc:'subscription.kind')
				}
				rdc.save(flush: true)
				if(rdc) {

					RefdataValue rdv = RefdataValue.findByOwnerAndValueIlike(rdc, 'Alliance Licence')
					if (! rdv) {
						rdv = new RefdataValue(owner: rdc, value: 'Alliance Licence')
					}
					rdv.save(flush: true)

					sql.execute("""UPDATE subscription SET sub_kind_rv_fk = (SELECT rdv_id FROM refdata_value WHERE 
						rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.kind') AND rdv_value = 'Alliance Licence') 
						WHERE sub_type_rv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.type')
						AND rdv_value = 'Alliance Licence');""")

					RefdataValue rdv2 = RefdataValue.findByOwnerAndValueIlike(rdc, 'National Licence')
					if (! rdv2) {
						rdv2 = new RefdataValue(owner: rdc, value: 'National Licence')
					}
					rdv2.save(flush: true)

					sql.execute("""UPDATE subscription SET sub_kind_rv_fk = (SELECT rdv_id FROM refdata_value WHERE  
						rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.kind') AND rdv_value = 'National Licence')  
						WHERE sub_type_rv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.type')  
						AND rdv_value = 'National Licence');""")

					RefdataValue rdv3 = RefdataValue.findByOwnerAndValueIlike(rdc, 'Consortial Licence')
					if (! rdv3) {
						rdv3 = new RefdataValue(owner: rdc, value: 'Consortial Licence')
					}
					rdv3.save(flush: true)

					sql.execute("""UPDATE subscription SET sub_kind_rv_fk = (SELECT rdv_id FROM refdata_value WHERE  
						rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.kind') AND rdv_value = 'Consortial Licence')  
						WHERE sub_type_rv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.type')  
						AND rdv_value = 'Consortial Licence');""")

					sql.execute("""UPDATE subscription SET sub_type_rv_fk = (SELECT rdv_id FROM refdata_value WHERE  
						rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.type') AND rdv_value = 'Consortial Licence') 
						WHERE sub_type_rv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.type')  
						AND rdv_value = 'National Licence');""")

					sql.execute("""UPDATE subscription SET sub_type_rv_fk = (SELECT rdv_id FROM refdata_value WHERE  
						rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.type') AND rdv_value = 'Consortial Licence')  
						WHERE sub_type_rv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.type')  
						AND rdv_value = 'Alliance Licence');""")
				}
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
