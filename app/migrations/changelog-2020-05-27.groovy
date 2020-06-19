databaseChangeLog = {

	changeSet(author: "klober (modified)", id: "1590557502641-1") {
		grailsChange {
			change {
				sql.execute("""
update refdata_value set rdv_value = 'Accepted Author Manuscript (AAM)' where rdv_id = (
    select rdv.rdv_id
        from refdata_value rdv join refdata_category rdc on rdv.rdv_owner = rdc.rdc_id
    	where rdc.rdc_description = 'license.oa.earc.version' and rdv.rdv_value = 'Accepted Author'
);
""")
			}
			rollback {}
		}
	}
}
