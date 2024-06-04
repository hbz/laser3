package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1717494364714-1") {
        grailsChange {
            change {
                sql.execute('ALTER TABLE public.refdata_value ALTER COLUMN rdv_value_de TYPE character varying(511) COLLATE public."de-u-co-phonebk-x-icu";')
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1717494364714-2") {
        grailsChange {
            change {
                sql.execute('ALTER TABLE public.refdata_value ALTER COLUMN rdv_value_en TYPE character varying(511) COLLATE public."de-u-co-phonebk-x-icu";')
            }
            rollback {}
        }
    }
}
