databaseChangeLog = {

	changeSet(author: "Moe (modified)", id: "1589467168125-1") {
		grailsChange {
			change {
				sql.execute("""UPDATE subscription SET sub_kind_rv_fk = (SELECT rdv_id FROM refdata_value WHERE
						rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.kind') AND rdv_value = 'Local Licence') 
						WHERE sub_type_rv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.type')
						AND rdv_value = 'Local Licence') 
						AND sub_kind_rv_fk is null;""")
			}
			rollback {
			}
		}
	}
}
