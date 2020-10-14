databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1563450092883-1") {
		addNotNullConstraint(columnDataType: "int8", columnName: "combo_from_org_fk", tableName: "combo")
	}

	changeSet(author: "kloberd (generated)", id: "1563450092883-2") {
		addNotNullConstraint(columnDataType: "int8", columnName: "combo_to_org_fk", tableName: "combo")
	}

	changeSet(author: "kloberd (modified)", id: "1563450092883-3") {
		grailsChange {
			change {
				sql.execute("""
UPDATE combo SET combo_type_rv_fk = (
		SELECT rv.rdv_id FROM refdata_value rv JOIN refdata_category rc ON rv.rdv_owner = rc.rdc_id
        	WHERE rc.rdc_description = 'Combo Type' AND rv.rdv_value = 'Consortium'
) WHERE combo_type_rv_fk ISNULL
""")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1563450092883-4") {
		addNotNullConstraint(columnDataType: "int8", columnName: "combo_type_rv_fk", tableName: "combo")
	}

	changeSet(author: "kloberd (modified)", id: "1563450092883-5") {
		grailsChange {
			change {
				sql.execute("DELETE FROM user_role WHERE role_id = (SELECT id FROM role WHERE authority = 'ROLE_DATAMANAGER')")
				sql.execute("DELETE FROM role WHERE authority = 'ROLE_DATAMANAGER'")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1563450092883-6") {
		grailsChange {
			change {
				sql.execute("""
DELETE FROM refdata_value WHERE rdv_id IN (
    SELECT rdv.rdv_id
    FROM refdata_value rdv
             JOIN refdata_category rdc on rdv.rdv_owner = rdc.rdc_id
    WHERE rdv.rdv_value = 'Deleted'
      AND rdc.rdc_description = 'Subscription Status'
) """)
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1563450092883-7") {
		grailsChange {
			change {
				sql.execute("""
DELETE FROM refdata_value WHERE rdv_id IN (
    SELECT rdv.rdv_id
    FROM refdata_value rdv
             JOIN refdata_category rdc on rdv.rdv_owner = rdc.rdc_id
    WHERE rdv.rdv_value = 'Deleted'
      AND rdc.rdc_description = 'License Status'
) """)
			}
			rollback {}
		}
	}
}
