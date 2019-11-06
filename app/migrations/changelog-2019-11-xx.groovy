databaseChangeLog = {

	changeSet(author: "kloberd (modified)", id: "xx-1") {
		grailsChange {
			change {
				sql.execute("create table identifier_distinct_tmp as select * from identifier with no data")
			}
		}
		rollback {}
	}

	changeSet(author: "kloberd (modified)", id: "xx-2") {
		grailsChange {
			change {
				sql.execute("""
insert into identifier_distinct_tmp(
    id_value, id_ns_fk, id_ig_fk, /* old */
    id_lic_fk, id_org_fk, id_pkg_fk, id_sub_fk, id_ti_fk, id_tipp_fk /* new */
)
select distinct
    i.id_value, i.id_ns_fk, i.id_ig_fk, /* old */
    io.io_lic_fk, io.io_org_fk, io.io_pkg_fk, io.io_sub_fk, io.io_ti_fk, io.io_tipp_fk /* new */
from identifier_occurrence io
         join identifier i on io.io_canonical_id = i.id_id
where i.id_value is not null and i.id_value != ''
""")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "xx-3") {
		grailsChange {
			change {
				sql.execute("truncate table identifier RESTART IDENTITY cascade")
			}
			rollback {}
		}
	}

	truncate table identifier RESTART IDENTITY cascade;
	changeSet(author: "kloberd (modified)", id: "xx-4") {
		grailsChange {
			change {
				sql.execute("""
insert into identifier(
    version, id_value, id_ns_fk, id_ig_fk, id_lic_fk, id_org_fk, id_pkg_fk, id_sub_fk, id_ti_fk, id_tipp_fk, id_date_created, id_last_updated
)
select
    0 as version, id_value, id_ns_fk, id_ig_fk, id_lic_fk, id_org_fk, id_pkg_fk, id_sub_fk, id_ti_fk, id_tipp_fk, current_date, current_date
from identifier_distinct_tmp
""")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "xx-5") {
		grailsChange {
			change {
				sql.execute("drop table identifier_occurrence cascade")
			}
			rollback {}
		}
	}

	// TODO DBM : delete identifier_distinct_tmp
}
