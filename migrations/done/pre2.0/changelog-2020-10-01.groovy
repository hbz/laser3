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

	changeSet(author: "galffy (modified)", id: "1601530275617-3") {
		grailsChange {
			change {
				sql.execute("delete from reader_number where num_reference_group not in (select rdv_value_de from refdata_value left join refdata_category on rdv_owner = rdc_id where rdc_description = 'number.type')")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1601530275617-4") {
		grailsChange {
			change {
				sql.execute("update refdata_value set rdv_order = 12 where rdv_value_de = 'w17/18'")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1601530275617-5") {
		grailsChange {
			change {
				sql.execute("update refdata_value set rdv_order = 15 where rdv_value_de = 's18'")
			}
			rollback {}
		}
	}

}
