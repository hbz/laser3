databaseChangeLog = {

	changeSet(author: "kloberd (modified)", id: "1562147170838-1") {
		grailsChange {
			change {
				sql.execute("""
update issue_entitlement set ie_status_rv_fk = (
	SELECT v.rdv_id FROM refdata_value v JOIN refdata_category c ON (v.rdv_owner = c.rdc_id) WHERE c.rdc_description = 'TIPP Status' and v.rdv_value = 'Current'
) where ie_status_rv_fk in (
	SELECT v.rdv_id FROM refdata_value v JOIN refdata_category c ON (v.rdv_owner = c.rdc_id) WHERE c.rdc_description = 'Entitlement Issue Status' and (
		v.rdv_value = 'Live' or v.rdv_value = 'Current'
	)
)
""")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1562147170838-2") {
		grailsChange {
			change {
				sql.execute("""
update issue_entitlement set ie_status_rv_fk = (
	SELECT v.rdv_id FROM refdata_value v JOIN refdata_category c ON (v.rdv_owner = c.rdc_id) WHERE c.rdc_description = 'TIPP Status' and v.rdv_value = 'Deleted'
) where ie_status_rv_fk = (
	SELECT v.rdv_id FROM refdata_value v JOIN refdata_category c ON (v.rdv_owner = c.rdc_id) WHERE c.rdc_description = 'Entitlement Issue Status' and v.rdv_value = 'Deleted'
)
""")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1562147170838-3") {
		grailsChange {
			change {
				sql.execute("""
delete from refdata_value where rdv_owner = (
	SELECT rdc_id FROM refdata_category WHERE rdc_description = 'Entitlement Issue Status'
)
""")
				sql.execute("""
delete from refdata_category where rdc_id = (
	SELECT rdc_id FROM refdata_category WHERE rdc_description = 'Entitlement Issue Status'
)
""")
			}
			rollback {}
		}
	}
}
