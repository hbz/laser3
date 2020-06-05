databaseChangeLog = {

	changeSet(author: "agalffy (generated)", id: "1591338927601-1") {
		grailsChange {
			change {
				sql.execute("alter table links alter column l_source_fk type character varying(255)")
			}
		}
	}

	changeSet(author: "agalffy (generated)", id: "1591338927601-2") {
		grailsChange {
			change {
				sql.execute("alter table links alter column l_destination_fk type character varying(255)")
			}
		}
	}

	changeSet(author: "agalffy (generated)", id: "1591338927601-3") {
		grailsChange {
			change {
				sql.execute("update links set l_source_fk = concat(l_object,':',l_source_fk)")
			}
		}
	}

	changeSet(author: "agalffy (generated)", id: "1591338927601-4") {
		grailsChange {
			change {
				sql.execute("update links set l_destination_fk = concat(l_object,':',l_destination_fk)")
			}
		}
	}

	changeSet(author: "agalffy (generated)", id: "1591338927601-5") {
		dropColumn(columnName: "l_object", tableName: "links")
	}

}