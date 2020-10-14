databaseChangeLog = {

	changeSet(author: "Moe (modified)", id: "1590503279420-1") {
		grailsChange {
			change {
				sql.execute("delete from user_settings where us_rv_fk = (select rdv_id from refdata_value where rdv_value = 'Changes' AND rdv_owner = (select rdc_id from refdata_category where rdc_description = 'user.setting.dashboard.tab'))")
				sql.execute("delete from refdata_value where rdv_value = 'Changes' AND rdv_owner = (select rdc_id from refdata_category where rdc_description = 'user.setting.dashboard.tab')")
			}
			rollback {}
		}
	}
}
