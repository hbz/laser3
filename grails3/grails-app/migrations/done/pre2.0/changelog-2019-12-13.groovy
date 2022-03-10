databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1576248708751-1") {
		createTable(schemaName: "public", tableName: "activity_profiler") {
			column(autoIncrement: "true", name: "ap_id", type: "int8") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "activity_profPK")
			}

			column(name: "ap_version", type: "int8") {
				constraints(nullable: "false")
			}

			column(name: "ap_date_created", type: "timestamp") {
				constraints(nullable: "false")
			}

			column(name: "ap_user_count", type: "int4") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1576248708751-2") {
		grailsChange {
			change {
				sql.execute("""
alter table activity_profiler alter column ap_date_created type timestamp using ap_date_created::timestamp;
update activity_profiler set ap_date_created = (ap_date_created + '1 hour'::interval);
""")
			}
			rollback {}
		}
	}
}
