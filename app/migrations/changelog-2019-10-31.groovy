databaseChangeLog = {

	changeSet(author: "kloberd (generated)", id: "1572516031732-1") {
		grailsChange {
			change {
				sql.execute("update subscription set sub_is_multi_year = true where sub_id in(select sub_id from subscription where DATE_PART('day', sub_end_date - sub_start_date) >= 724 and sub_end_date is not null)")
			}
			rollback {}
		}
	}
}
