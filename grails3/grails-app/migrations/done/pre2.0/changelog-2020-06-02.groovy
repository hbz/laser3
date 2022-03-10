databaseChangeLog = {

	changeSet(author: "Moe (modified)", id: "1591111289343-1") {
		grailsChange {
			change {
				sql.execute("update title_instance set bk_summaryofcontent = null where bk_summaryofcontent is not  null;")
			}
			rollback {}
		}
	}
}
