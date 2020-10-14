databaseChangeLog = {

	changeSet(author: "kloberd (modified)", id: "1573731660644-1") {
		grailsChange {
			change {
				sql.execute("create table identifier_distinct_tmp as select * from identifier with no data")
				
				sql.execute("create table identifier_backup as table identifier")
				sql.execute("create table identifier_namespace_backup as table identifier_namespace")
				sql.execute("create table identifier_occurrence_backup as table identifier_occurrence")
			}
		}
		rollback {}
	}

	changeSet(author: "kloberd (modified)", id: "1573731660644-2") {
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

	changeSet(author: "kloberd (modified)", id: "1573731660644-3") {
		grailsChange {
			change {
				sql.execute("truncate table identifier RESTART IDENTITY cascade")

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

	changeSet(author: "kloberd (modified)", id: "1573731660644-4") {
		grailsChange {
			change {
				sql.execute("update identifier_namespace set idns_is_unique = false where idns_ns in ('ezb','edb','zdb','doi','isbn','pisbn','issn','eissn','uri','unkown') and idns_type is null")
			}
			rollback {}
		}
	}

	changeSet(author: "kloberd (modified)", id: "1573731660644-5") {
		grailsChange {
			change {
				sql.execute("drop table identifier_occurrence cascade")
				sql.execute("drop table identifier_distinct_tmp")
			}
			rollback {}
		}
	}
}
