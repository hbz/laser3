databaseChangeLog = {

	changeSet(author: "galffy (modified)", id: "1595574486444-1") {
		grailsChange {
			change {
				sql.execute("update reader_number set num_due_date = null where num_reference_group in ('Studierende','wissenschaftliches Personal') and num_semester_rv_fk = (select rdv_id from refdata_value where rdv_value = 'semester.not.applicable')")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1595574486444-2") {
		grailsChange {
			change {
				sql.execute("update reader_number set num_semester_rv_fk = null where num_reference_group not in ('Studierende','wissenschaftliches Personal')")
				//or num_semester_rv_fk = (select rdv_id from refdata_value where rdv_value = 'semester.not.applicable')
			}
			rollback {}
		}
	}

	/*
	changeSet(author: "galffy (modified)", id: "1595574486444-3") {
		grailsChange {
			change {
				sql.execute("delete from refdata_value where rdv_value = 'semester.not.applicable'")
			}
			rollback {}
		}
	}
	*/

}
