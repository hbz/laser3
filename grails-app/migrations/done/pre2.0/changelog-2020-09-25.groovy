databaseChangeLog = {

	changeSet(author: "galffy (modified)", id: "1601036406615-1") {
		grailsChange {
			change {
				sql.execute("update doc set doc_owner_fk = org_id from user_org where doc_owner_fk is null and user_id in (doc_creator,doc_user_fk);")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (modified)", id: "1601036406615-2") {
		grailsChange {
			change {
				sql.execute("delete from refdata_value where rdv_value = 'only for creator';")
			}
			rollback {}
		}
	}

	changeSet(author: "galffy (generated)", id: "1601036406615-3") {
		dropColumn(columnName: "doc_creator", tableName: "doc")
	}

	changeSet(author: "galffy (generated)", id: "1601036406615-4") {
		dropColumn(columnName: "doc_user_fk", tableName: "doc")
	}

}
