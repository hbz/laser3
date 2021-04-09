databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1617945512585-1") {
        grailsChange {
            change {
                sql.execute("update public.user set display = replace(display,'singlenutzer','vollnutzer'), username = replace(username,'singlenutzer','vollnutzer') where display like '%singlenutzer%' or username like '%singlenutzer%'")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1617945512585-2") {
        grailsChange {
            change {
                sql.execute("update i10n_translation set i10n_value_de = 'Vollnutzer' where i10n_value_de = 'Singlenutzer'")
            }
            rollback {}
        }
    }

}