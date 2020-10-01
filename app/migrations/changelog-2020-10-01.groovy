databaseChangeLog = {

	changeSet(author: "galffy (modified)", id: "1601530275617-1") {
		grailsChange {
			change {
				sql.execute("update reader_number set num_due_date = null where num_semester_rv_fk is not null")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1601530275617-2") {
		grailsChange {
			change {
				sql.execute("delete from reader_number where num_due_date is null and num_semester_rv_fk is null")
			}
			rollback {}
		}
	}

}
