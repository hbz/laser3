package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (modified)", id: "1727102531671-1") {
        grailsChange {
            change {
                sql.execute('CREATE INDEX tipp_first_author_idx ON public.title_instance_package_platform USING btree ((lower(tipp_first_author)) ASC NULLS LAST) WITH (deduplicate_items=True);')
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1727102531671-2") {
        grailsChange {
            change {
                sql.execute('CREATE INDEX tipp_first_editor_idx ON public.title_instance_package_platform USING btree ((lower(tipp_first_editor)) ASC NULLS LAST) WITH (deduplicate_items=True);')
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1727102531671-3") {
        grailsChange {
            change {
                sql.execute('CREATE INDEX tipp_name_idx ON public.title_instance_package_platform USING btree ((lower(tipp_name)) ASC NULLS LAST) WITH (deduplicate_items=True);')
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1727102531671-4") {
        grailsChange {
            change {
                sql.execute('CREATE INDEX tipp_sort_name_idx ON public.title_instance_package_platform USING btree ((lower(tipp_sort_name)) ASC NULLS LAST) WITH (deduplicate_items=True);')
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1727102531671-5") {
        grailsChange {
            change {
                sql.execute('CREATE INDEX id_value_idx ON public.identifier USING btree ((lower(id_value)) ASC NULLS LAST) WITH (deduplicate_items=True);')
            }
            rollback {}
        }
    }
}
