import com.k_int.kbplus.License
import com.k_int.kbplus.Subscription

databaseChangeLog = {

	changeSet(author: "galffy (generated)", id: "1592210692365-1") {
		addColumn(schemaName: "public", tableName: "license") {
			column(name: "lic_open_ended_rv_fk", type: "int8")
		}
	}

	changeSet(author: "galffy (modified)", id: "1592210692365-2") {
		grailsChange {
			change {
				sql.execute("update license set lic_open_ended_rv_fk = (select rdv_id from refdata_value left join refdata_category on rdv_owner = rdc_id where rdc_description = 'y.n.u' and rdv_value = 'Unknown') where lic_open_ended_rv_fk is null;")
			}
		}
	}

	changeSet(author: "galffy (modified)", id: "1592210692365-3") {
		addNotNullConstraint(columnDataType: "int8", columnName: "lic_open_ended_rv_fk", tableName: "license")
	}

	changeSet(author: "galffy (generated)", id: "1592210692365-4") {
		addForeignKeyConstraint(baseColumnNames: "lic_open_ended_rv_fk", baseTableName: "license", baseTableSchemaName: "public", constraintName: "FK9F084417F199EB0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

}
