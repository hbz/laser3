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
}
