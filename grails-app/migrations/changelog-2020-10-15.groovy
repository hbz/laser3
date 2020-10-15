databaseChangeLog = {

	changeSet(author: "kloberd (modified)", id: "1602753960593-1") {
		grailsChange {
			change {
				sql.execute("delete from i10n_translation where i10n_reference_class = 'com.k_int.kbplus.auth.Role'")
			}
			rollback {}
		}
	}
}
