databaseChangeLog = {

	changeSet(author: "galffy (generated)", id: "1593081971967-1") {
		grailsChange {
			change {
				sql.execute("UPDATE license set lic_is_slaved = TRUE where lic_parent_lic_fk is not null")
			}
			rollback {}
		}
	}

}
