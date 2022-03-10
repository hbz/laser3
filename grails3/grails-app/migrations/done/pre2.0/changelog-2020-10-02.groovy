databaseChangeLog = {

	changeSet(author: "galffy (modified)", id: "1601636575084-1") {
		grailsChange {
			change {
				sql.execute("update property_definition set pd_multiple_occurrence = true where pd_name in ('Archival Copy: Cost','Archiving format','Authority','Electronically Archivable Version','Ill ZETA code','Usage Statistics Format','Usage Statistics Delivery','Usage Statistics Standard Compliance')")
			}
			rollback {}
		}
	}

}
