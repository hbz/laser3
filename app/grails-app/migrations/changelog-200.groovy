databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1534927145628-1") {
		addColumn(tableName: "cost_item") {
			column(name: "ci_cost_in_billing_currency_after_tax", type: "double precision")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1534927145628-2") {
		addColumn(tableName: "cost_item") {
			column(name: "ci_cost_in_local_currency_after_tax", type: "double precision")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1534927145628-3") {
		addColumn(tableName: "cost_item") {
			column(name: "ci_final_cost_rounding", type: "bit")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1534927145628-4") {
		addColumn(tableName: "cost_item") {
			column(name: "ci_invoice_date", type: "datetime")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1534927145628-5") {
		addColumn(tableName: "cost_item") {
			column(name: "ci_tax_rate", type: "integer")
		}
	}

	changeSet(author: "kloberd (generated)", id: "1534927145628-7") {
		dropIndex(indexName: "jusp_cursor_idx", tableName: "jusp_triple_cursor")
	}

	changeSet(author: "kloberd (generated)", id: "1534927145628-8") {
		dropTable(tableName: "jusp_triple_cursor")
	}
}
