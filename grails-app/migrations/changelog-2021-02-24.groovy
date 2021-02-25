databaseChangeLog = {

    changeSet(author: "klober (modified)", id: "1614160052993-1") {
        grailsChange {
            change {
                sql.execute("""
update org_setting 
    set os_role_fk = (select id from role where authority='ORG_INST' and role_type='org') 
        where os_key_enum = 'CUSTOMER_TYPE' 
            and os_role_fk = (select id from role where authority='ORG_INST_COLLECTIVE' and role_type='org')
""")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1614160052993-2") {
        grailsChange {
            change {
                sql.execute("delete from perm_grant where role_id = (select id from role where authority='ORG_INST_COLLECTIVE' and role_type='org')")
                sql.execute("delete from role where authority='ORG_INST_COLLECTIVE' and role_type='org'")
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1614160052993-3") {
        grailsChange {
            change {
                sql.execute("delete from refdata_value where rdv_value='Department' and rdv_owner = (select rdc_id from refdata_category where rdc_description='combo.type')")
            }
            rollback {}
        }
    }
    changeSet(author: "klober (modified)", id: "1614160052993-4") {
        grailsChange {
            change {
                String rdc_query = "where rdv_value='Licensee_Collective' and rdv_owner = (select rdc_id from refdata_category where rdc_description='organisational.role')"

                sql.execute("delete from org_role where or_roletype_fk = (select rdv_id from refdata_value " + rdc_query + ")")
                sql.execute("delete from refdata_value " + rdc_query)
            }
            rollback {}
        }
    }
    changeSet(author: "klober (modified)", id: "1614160052993-5") {
        grailsChange {
            change {
                String rdc_query = "where rdv_value='Subscriber_Collective' and rdv_owner = (select rdc_id from refdata_category where rdc_description = 'organisational.role')"

                sql.execute("delete from org_role where or_roletype_fk = (select rdv_id from refdata_value " + rdc_query + ")")
                sql.execute("delete from refdata_value " + rdc_query)
            }
            rollback {}
        }
    }
    changeSet(author: "klober (modified)", id: "1614160052993-6") {
        grailsChange {
            change {
                String rdc_query = "where rdv_value='Subscription Collective' and rdv_owner = (select rdc_id from refdata_category where rdc_description = 'organisational.role')"

                sql.execute("delete from org_role where or_roletype_fk = (select rdv_id from refdata_value " + rdc_query + ")")
                sql.execute("delete from refdata_value " + rdc_query)
            }
            rollback {}
        }
    }
}
