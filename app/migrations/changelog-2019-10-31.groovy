databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1572516031732-1") {
		grailsChange {
			change {
				sql.execute("update subscription set sub_is_multi_year = true where sub_id in(select sub_id from subscription where DATE_PART('day', sub_end_date - sub_start_date) >= 724 and sub_end_date is not null)")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (generated)", id: "1572516031732-2") {
		grailsChange {
			change {
				sql.execute("update i10n_translation set i10n_reference_field='expl' where i10n_reference_field='explain' and i10n_reference_class='com.k_int.kbplus.SurveyProperty'")
			}
			rollback {}
		}
	}
}
