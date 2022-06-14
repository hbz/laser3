databaseChangeLog = {

    changeSet(author: "klober (modified)", id: "1637055671206-1") {

        grailsChange {
            change {
                sql.execute("""
                    alter table public.platform_property alter column plp_is_public set data type boolean
                    using case when plp_is_public = 'true' then true else false end;
                """)
            }
            rollback {}
        }
    }
}