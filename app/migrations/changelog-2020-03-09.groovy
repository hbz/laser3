databaseChangeLog = {

	changeSet(author: "djebeniani (generated)", id: "1583759022240-1") {
		addColumn(schemaName: "public", tableName: "subscription") {
			column(name: "sub_kind_rv_fk", type: "int8")
		}
	}

	changeSet(author: "djebeniani (generated)", id: "1583759022240-2") {
		addForeignKeyConstraint(baseColumnNames: "sub_kind_rv_fk", baseTableName: "subscription", baseTableSchemaName: "public", constraintName: "FK1456591D8312F145", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "djebeniani (generated)", id: "1583759022240-3") {
		grailsChange {
			change {
				sql.execute("UPDATE subscription SET sub_kind_rv_fk = (SELECT rdv_id FROM refdata_value WHERE\n" +
						"rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.kind')\n" +
						"                                                                            AND rdv_value = 'Alliance Licence')\n" +
						"WHERE sub_type_rv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.type')\n" +
						"AND rdv_value = 'Alliance Licence');")

				sql.execute("UPDATE subscription SET sub_kind_rv_fk = (SELECT rdv_id FROM refdata_value WHERE\n" +
						"        rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.kind')\n" +
						"                                                                             AND rdv_value = 'National Licence')\n" +
						"WHERE sub_type_rv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.type')\n" +
						"                                                           AND rdv_value = 'National Licence');")

				sql.execute("UPDATE subscription SET sub_kind_rv_fk = (SELECT rdv_id FROM refdata_value WHERE\n" +
						"        rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.kind')\n" +
						"                                                                             AND rdv_value = 'Consortial Licence')\n" +
						"WHERE sub_type_rv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.type')\n" +
						"                                                           AND rdv_value = 'Consortial Licence');")

				sql.execute("UPDATE subscription SET sub_type_rv_fk = (SELECT rdv_id FROM refdata_value WHERE\n" +
						"        rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.type')\n" +
						"                                                                             AND rdv_value = 'Consortial Licence')\n" +
						"WHERE sub_type_rv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.type')\n" +
						"                                                           AND rdv_value = 'National Licence');")

				sql.execute("UPDATE subscription SET sub_type_rv_fk = (SELECT rdv_id FROM refdata_value WHERE\n" +
						"        rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.type')\n" +
						"                                                                             AND rdv_value = 'Consortial Licence')\n" +
						"WHERE sub_type_rv_fk = (SELECT rdv_id FROM refdata_value WHERE rdv_owner = (SELECT rdc_id FROM refdata_category as rdc WHERE rdc.rdc_description = 'subscription.type')\n" +
						"                                                           AND rdv_value = 'Alliance Licence');")
			}
			rollback {
			}
		}
	}

}
