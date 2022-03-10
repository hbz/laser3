databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1575460244474-1") {
		grailsChange {
			change {
				sql.execute("update refdata_value set rdv_value='Personal Contact' where rdv_value='Personal contact'")
				sql.execute("update refdata_value set rdv_value='Functional Contact' where rdv_value='Functional contact'")
			}
			rollback {}
		}
	}
}
