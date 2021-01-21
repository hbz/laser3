databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1576759084071-1") {
		addColumn(schemaName: "public", tableName: "pending_change") {
			column(name: "pc_change_doc_oid", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1576759084071-2") {
		addColumn(schemaName: "public", tableName: "pending_change") {
			column(name: "pc_change_target_oid", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1576759084071-3") {
		addColumn(schemaName: "public", tableName: "pending_change") {
			column(name: "pc_change_type", type: "varchar(255)")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1576759084071-4") {
		createIndex(indexName: "pending_change_pl_cd_oid_idx", schemaName: "public", tableName: "pending_change") {
			column(name: "pc_change_doc_oid")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1576759084071-5") {
		createIndex(indexName: "pending_change_pl_ct_oid_idx", schemaName: "public", tableName: "pending_change") {
			column(name: "pc_change_target_oid")
		}
	}
}
