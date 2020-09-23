databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1599136756111-1") {
		dropColumn(columnName: "or_cluster_fk", tableName: "org_role")
	}

	changeSet(author: "kloberd (generated)", id: "1599136756111-2") {
		dropColumn(columnName: "pr_cluster_fk", tableName: "person_role")
	}

	changeSet(author: "kloberd (generated)", id: "1599136756111-3") {
		dropSequence(schemaName: "public", sequenceName: "cluster_cl_id_seq")
	}

	changeSet(author: "kloberd (generated)", id: "1599136756111-4") {
		dropTable(tableName: "cluster")
	}
}
