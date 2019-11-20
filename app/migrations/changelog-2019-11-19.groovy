databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1574166645366-1") {
		grailsChange {
			change {
				sql.execute("update i10n_translation set i10n_reference_field='expl' where i10n_reference_field='explain' and i10n_reference_class='com.k_int.kbplus.SurveyProperty'")
			}
			rollback {}
		}
	}
}
