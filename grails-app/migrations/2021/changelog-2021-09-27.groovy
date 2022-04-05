databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1632724083139-1") {
        grailsChange {
            change {
                sql.execute("ALTER TABLE public.reader_number ALTER COLUMN num_value TYPE numeric(19, 2);")
            }
            rollback {}
        }
    }
}
