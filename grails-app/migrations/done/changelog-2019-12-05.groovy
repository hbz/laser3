databaseChangeLog = {

	changeSet(author: "jaegle(modified)", id: "1575545825489-1") {
		grailsChange {
			change {
				sql.execute("delete from org_access_point_link where active=false")
			}
		}
		rollback {}
	}

	changeSet(author: "jaegle (modified)", id: "1575545825489-2") {
		grailsChange {
			change {
				sql.execute("create table org_access_point_link_bak as select * from org_access_point_link")
			}
			rollback {
			}
		}
	}

	changeSet(author: "jaegle (generated)", id: "1575545825489-3") {
		addColumn(schemaName: "public", tableName: "org_access_point_link") {
			column(name: "sub_pkg_id", type: "int8")
		}
	}

	changeSet(author: "jaegle (generated)", id: "1575545825489-4") {
		dropNotNullConstraint(columnDataType: "int8", columnName: "oap_id", tableName: "org_access_point_link")
	}

	changeSet(author: "jaegle (generated)", id: "1575545825489-5") {
		dropForeignKeyConstraint(baseTableName: "org_access_point_link", baseTableSchemaName: "public", constraintName: "fk1c324e694dfc6d97")
	}

	changeSet(author: "jaegle (generated)", id: "1575545825489-6") {
		dropColumn(columnName: "subscription_id", tableName: "org_access_point_link")
	}

	changeSet(author: "jaegle (generated)", id: "1575545825489-7") {
		addForeignKeyConstraint(baseColumnNames: "sub_pkg_id", baseTableName: "org_access_point_link", baseTableSchemaName: "public", constraintName: "FK1C324E69B67D6819", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "sp_id", referencedTableName: "subscription_package", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
	}

	changeSet(author: "jaegle (modified)", id: "1575545825489-8") {
		grailsChange {
			change {
				sql.execute("truncate table org_access_point_link RESTART IDENTITY")

				sql.execute("""
insert into org_access_point_link(
	version, active, date_created, globaluid, last_updated, oap_id, platform_id
)
select version, active, date_created, globaluid, last_updated, oap_id, platform_id
from org_access_point_link_bak oaplbak where oaplbak.subscription_id is null 
""")

				sql.execute("""
create table org_access_point_link_tmp as (
select distinct oapl.id as oapl_id, oapl.date_created, oapl.globaluid, oapl.last_updated, oapl.oap_id, sp.sp_id, p.pkg_name, p.pkg_id, p2.plat_name, p2.plat_id
from org_access_point_link_bak as oapl
         join subscription s on oapl.subscription_id = s.sub_id
         join subscription_package sp on s.sub_id = sp.sp_sub_fk
         join package p on p.pkg_id = sp.sp_pkg_fk
         join title_instance_package_platform tipp on p.pkg_id = tipp.tipp_pkg_fk
         join platform p2 on tipp.tipp_plat_fk = p2.plat_id
where oapl.subscription_id is not null 
) 
""")

				def subscriptionOapls = sql.rows("select * from org_access_point_link_tmp")

				subscriptionOapls.each {
					// check for marker and insert marker if not yet present
					def existingMarker = sql.firstRow("""
select * from org_access_point_link oapl
	where oap_id is null and platform_id=? and sub_pkg_id=?
""", [it.plat_id, it.sp_id])

					if (!existingMarker) { // create marker
						def markerUid = "orgaccesspointlink:${UUID.randomUUID()}"

						sql.execute("""
insert into org_access_point_link(version, active, date_created, globaluid, last_updated, platform_id, sub_pkg_id)
	values
	(0,true,current_timestamp,'${markerUid}',current_timestamp,${it.plat_id},${it.sp_id}) 
""")
					}
					def globalUid = "orgaccesspointlink:${UUID.randomUUID()}"
					// insert OrgAccessPointLink
					sql.execute("""
insert into org_access_point_link(
	version, active, date_created, globaluid, last_updated, oap_id, platform_id, sub_pkg_id
) values
(0, true, ${it.date_created},'${globalUid}', ${it.last_updated}, ${it.oap_id}, ${it.plat_id}, ${it.sp_id})
""")
				}
			}
			rollback {
			}
		}
	}

	changeSet(author: "jaegle (modified)", id: "1575545825489-9") {
		grailsChange {
			change {
				// we could keep the backup if needed
				sql.execute("drop table org_access_point_link_bak")
				sql.execute("drop table org_access_point_link_tmp")
			}
			rollback {}
		}
	}

}
