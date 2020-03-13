databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1584088132653-1") {
		addColumn(schemaName: "public", tableName: "system_profiler") {
			column(name: "sp_archive", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (modified)", id: "1584088132653-2") {
		grailsChange {
			change {
				sql.execute("update system_profiler set sp_archive = '1.2' where sp_archive is null;")
			}
			rollback {
			}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1584088132653-3") {
		grailsChange {
			change {
				sql.execute("update refdata_category set rdc_description = 'subjectgroup' where rdc_description = 'subjectGroup';")
			}
			rollback {
			}
		}
	}
}
