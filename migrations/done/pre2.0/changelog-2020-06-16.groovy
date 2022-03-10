import de.laser.License
import de.laser.Subscription

databaseChangeLog = {

	changeSet(author: "galffy (hand-coded)", id: "1592285771248-1") {
		grailsChange {
			change {
				sql.execute("insert into links (version,l_source_fk,l_destination_fk,l_link_type_rv_fk,l_owner_fk,l_date_created,last_updated) " +
						"select 0,concat('${License.class.name}:',sub_owner_license_fk),concat('${Subscription.class.name}:',sub_id),(select rdv_id from refdata_value where rdv_value = 'license' and rdv_owner = (select rdc_id from refdata_category where rdc_description = 'link.type')),or_org_fk,now(),now() from subscription join org_role on sub_id = or_sub_fk where sub_owner_license_fk is not null and or_roletype_fk in (select rdv_id from refdata_value where rdv_value in ('Subscriber','Subscription Consortia','Subscription Collective'));")
			}
		}
	}

	changeSet(author: "galffy (generated)", id: "1592285771248-2") {
		dropForeignKeyConstraint(baseTableName: "subscription", baseTableSchemaName: "public", constraintName: "fk1456591d7d96d7d2")
	}

	changeSet(author: "galffy (generated)", id: "1592285771248-3") {
		dropColumn(columnName: "sub_owner_license_fk", tableName: "subscription")
	}

}
