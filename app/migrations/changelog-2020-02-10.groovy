databaseChangeLog = {

	changeSet(author: "kloberd (modified)", id: "1581320763299-1") {
		grailsChange {
			change {
				sql.execute("""
update doc_context set dc_date_created = '2018-01-01 00:00:0.000000' where dc_date_created is null;
update issue_entitlement set ie_date_created = '2018-01-01 00:00:0.000000' where ie_date_created is null;
update license set date_created = '2018-01-01 00:00:0.000000' where date_created is null;
update license_custom_property set lcp_date_created = '2018-01-01 00:00:0.000000' where lcp_date_created is null;
update license_private_property set lpp_date_created = '2018-01-01 00:00:0.000000' where lpp_date_created is null;
update org set org_date_created = '2018-01-01 00:00:0.000000' where org_date_created is null;
update package set date_created = '2018-01-01 00:00:0.000000' where date_created is null;
update platform set date_created = '2018-01-01 00:00:0.000000' where date_created is null;
update subscription set date_created = '2018-01-01 00:00:0.000000' where date_created is null;
update subscription_custom_property set scp_date_created = '2018-01-01 00:00:0.000000' where scp_date_created is null;
update subscription_private_property set spp_date_created = '2018-01-01 00:00:0.000000' where spp_date_created is null;
update survey_config set surconf_date_created = '2018-01-01 00:00:0.000000' where surconf_date_created is null;
update survey_org set surorg_date_created = '2018-01-01 00:00:0.000000' where surorg_date_created is null;
update task set tsk_date_created = '2018-01-01 00:00:0.000000' where tsk_date_created is null;
update title_instance set date_created = '2018-01-01 00:00:0.000000' where date_created is null;
""")
			}
			rollback {
			}
		}
	}

	changeSet(author: "klober (generated)", id: "1581320763299-2") {
		dropNotNullConstraint(columnDataType: "int4", columnName: "sp_ms", tableName: "system_profiler")
	}

	changeSet(author: "klober (generated)", id: "1581320763299-3") {
		dropForeignKeyConstraint(baseTableName: "reminder", baseTableSchemaName: "public", constraintName: "fke116c0723d70d35d")
	}

	changeSet(author: "klober (generated)", id: "1581320763299-4") {
		dropForeignKeyConstraint(baseTableName: "reminder", baseTableSchemaName: "public", constraintName: "fke116c07248e736b3")
	}

	changeSet(author: "klober (generated)", id: "1581320763299-5") {
		dropForeignKeyConstraint(baseTableName: "reminder", baseTableSchemaName: "public", constraintName: "fke116c072e68d8f67")
	}

	changeSet(author: "klober (generated)", id: "1581320763299-6") {
		dropForeignKeyConstraint(baseTableName: "reminder", baseTableSchemaName: "public", constraintName: "fke116c0723761cec3")
	}

	changeSet(author: "klober (generated)", id: "1581320763299-7") {
		dropTable(tableName: "reminder")
	}
}