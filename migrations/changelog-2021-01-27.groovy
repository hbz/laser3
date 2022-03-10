databaseChangeLog = {

    //changes for: title_history_event_participant

    changeSet(author: "agalffy (generated)", id: "1611728842136-1") {
        addColumn(tableName: "title_history_event_participant") {
            column(name: "thep_participant_fk", type: "int8")
        }
    }

    changeSet(author: "agalffy (modified)", id: "1611728842136-2") {
        dropNotNullConstraint(columnDataType: "int8", columnName: "participant_id", tableName: "title_history_event_participant")
    }

    changeSet(author: "agalffy (modified)", id: "1611728842136-3") {
        grailsChange {
            change {
                sql.execute('insert into title_history_event_participant (version,event_id,thep_participant_fk,participant_role,thep_date_created,thep_last_updated) select thep.version,event_id,tipp_id,participant_role,thep_date_created,now() from title_instance_package_platform join title_history_event_participant thep on participant_id = tipp_ti_fk where tipp_ti_fk = participant_id;')
            }
            rollback {}
        }
    }

    changeSet(author: "agalffy (modified)", id: "1611728842136-4") {
        grailsChange {
            change {
                sql.execute('ALTER TABLE public.title_history_event_participant RENAME id TO thep_id;')
            }
            rollback {}
        }
    }

    changeSet(author: "agalffy (modified)", id: "1611728842136-5") {
        grailsChange{
            change {
                sql.execute('ALTER TABLE public.title_history_event_participant RENAME participant_role TO thep_participant_role;')
            }
            rollback {}
        }
    }

    changeSet(author: "agalffy (modified)", id: "1611728842136-6") {
        grailsChange{
            change {
                sql.execute('ALTER TABLE public.title_history_event_participant RENAME version TO thep_version;')
            }
            rollback {}
        }
    }

    changeSet(author: "agalffy (modified)", id: "1611728842136-7") {
        grailsChange{
            change {
                sql.execute('ALTER TABLE public.title_history_event_participant RENAME event_id TO thep_event_fk;')
            }
            rollback {}
        }
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-8") {
        dropForeignKeyConstraint(baseTableName: "title_history_event_participant", constraintName: "fke3ab36fc897ec757")
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-9") {
        dropForeignKeyConstraint(baseTableName: "title_history_event_participant", constraintName: "fke3ab36fcc15ef6e1")
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-10") {
        addForeignKeyConstraint(baseColumnNames: "thep_event_fk", baseTableName: "title_history_event_participant", constraintName: "FK1w52gq8bt9d2fh5fvvkj9tn34", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "title_history_event")
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-11") {
        addForeignKeyConstraint(baseColumnNames: "thep_participant_fk", baseTableName: "title_history_event_participant", constraintName: "FKitbpxt4ihlc6ru1cftx0p3jk5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "tipp_id", referencedTableName: "title_instance_package_platform")
    }

    changeSet(author: "agalffy (modified)", id: "1611728842136-12") {
        grailsChange {
            change {
                sql.execute('delete from title_history_event_participant where thep_participant_fk is null;')
            }
            rollback {}
        }
    }

    changeSet(author: "agalffy (modified)", id: "1611728842136-13") {
        addNotNullConstraint(columnDataType: "int8", columnName: "thep_participant_fk", tableName: "title_history_event_participant")
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-14") {
        dropColumn(columnName: "participant_id", tableName: "title_history_event_participant")
    }

    //changes for: org_role

    changeSet(author: "agalffy (generated)", id: "1611728842136-15") {
        addColumn(tableName: "org_role") {
            column(name: "or_tipp_fk", type: "int8")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-16") {
        createIndex(indexName: "or_tipp_idx", tableName: "org_role") {
            column(name: "or_tipp_fk")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-17") {
        addForeignKeyConstraint(baseColumnNames: "or_tipp_fk", baseTableName: "org_role", constraintName: "FKq079neqmeso2oq7s1oxaleit1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "tipp_id", referencedTableName: "title_instance_package_platform")
    }

    changeSet(author: "agalffy (modified)", id: "1611728842136-18") {
        grailsChange {
            change {
                sql.execute('insert into org_role (or_version,or_org_fk,or_tipp_fk,or_is_shared,or_roletype_fk,or_shared_from_fk,or_date_created,or_last_updated) select or_version,or_org_fk,tipp_id,or_is_shared,or_roletype_fk,or_shared_from_fk,or_date_created,now() from title_instance_package_platform join org_role on or_title_fk = tipp_ti_fk where tipp_ti_fk = or_title_fk;')
            }
            rollback {}
        }
    }

    /* deactivated because the delete query needs an half an hour and I doubt its sense
    changeSet(author: "agalffy (modified)", id: "1611728842136-19") {
        grailsChange {
            change {
                //sql.execute('delete from org_role where or_title_fk is not null;')
            }
            rollback {}
        }
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-20") {
        dropForeignKeyConstraint(baseTableName: "org_role", constraintName: "fk4e5c38f16d6b9898")
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-21") {
        dropColumn(columnName: "or_title_fk", tableName: "org_role")
    }*/

    //changes for: person_role

    changeSet(author: "agalffy (generated)", id: "1611728842136-22") {
        addColumn(tableName: "person_role") {
            column(name: "pr_tipp_fk", type: "int8")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-23") {
        addForeignKeyConstraint(baseColumnNames: "pr_tipp_fk", baseTableName: "person_role", constraintName: "FKpktku042pj59xdgp34oq4oi28", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "tipp_id", referencedTableName: "title_instance_package_platform")
    }

    changeSet(author: "agalffy (modified)", id: "1611728842136-24") {
        grailsChange {
            change {
                sql.execute('insert into person_role (pr_version,pr_org_fk,pr_prs_fk,pr_tipp_fk,pr_position_type_rv_fk,pr_responsibility_type_rv_fk,pr_function_type_rv_fk,pr_date_created,pr_last_updated) select pr_version,pr_org_fk,pr_prs_fk,tipp_id,pr_position_type_rv_fk,pr_responsibility_type_rv_fk,pr_function_type_rv_fk,pr_date_created,now() from title_instance_package_platform join person_role on pr_title_fk = tipp_ti_fk where tipp_ti_fk = pr_title_fk;')
            }
            rollback {}
        }
    }

    changeSet(author: "agalffy (modified)", id: "1611728842136-25") {
        grailsChange {
            change {
                sql.execute('delete from person_role where pr_title_fk is not null;')
            }
            rollback {}
        }
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-26") {
        dropForeignKeyConstraint(baseTableName: "person_role", constraintName: "fke6a16b202504b59")
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-27") {
        dropColumn(columnName: "pr_title_fk", tableName: "person_role")
    }

    //changes for: identifier

    changeSet(author: "agalffy (modified)", id: "1611728842136-28") {
        grailsChange {
            change {
                sql.execute('insert into identifier (version,id_ns_fk,id_value,id_date_created,id_last_updated,id_note,id_last_updated_cascading,id_tipp_fk) select version,id_ns_fk,id_value,id_date_created,id_last_updated,id_note,id_last_updated_cascading,tipp_id from title_instance_package_platform join identifier on id_ti_fk = tipp_ti_fk where id_ti_fk = tipp_ti_fk;')
            }
            rollback {}
        }
    }

    changeSet(author: "agalffy (modified)", id: "1611728842136-29") {
        grailsChange {
            change {
                sql.execute('delete from identifier where id_ti_fk is not null;')
            }
            rollback {}
        }
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-30") {
        dropForeignKeyConstraint(baseTableName: "identifier", constraintName: "fk9f88aca96d99385b")
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-31") {
        dropColumn(columnName: "id_ti_fk", tableName: "identifier")
    }

    //changes for: title_instance_package_platform

    changeSet(author: "agalffy (generated)", id: "1611728842136-32") {
        addColumn(tableName: "title_instance_package_platform") {
            column(name: "tipp_date_first_in_print", type: "timestamp")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-33") {
        addColumn(tableName: "title_instance_package_platform") {
            column(name: "tipp_date_first_online", type: "timestamp")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-34") {
        addColumn(tableName: "title_instance_package_platform") {
            column(name: "tipp_edition_differentiator", type: "varchar(255)")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-35") {
        addColumn(tableName: "title_instance_package_platform") {
            column(name: "tipp_edition_number", type: "int4")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-36") {
        addColumn(tableName: "title_instance_package_platform") {
            column(name: "tipp_edition_statement", type: "varchar(255)")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-37") {
        addColumn(tableName: "title_instance_package_platform") {
            column(name: "tipp_first_author", type: "varchar(255)")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-38") {
        addColumn(tableName: "title_instance_package_platform") {
            column(name: "tipp_first_editor", type: "varchar(255)")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-39") {
        addColumn(tableName: "title_instance_package_platform") {
            column(name: "tipp_medium_rv_fk", type: "int8")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-40") {
        addColumn(tableName: "title_instance_package_platform") {
            column(name: "tipp_name", type: "text")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-41") {
        addColumn(tableName: "title_instance_package_platform") {
            column(name: "tipp_norm_name", type: "text")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-42") {
        addColumn(tableName: "title_instance_package_platform") {
            column(name: "tipp_series_name", type: "text")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-43") {
        addColumn(tableName: "title_instance_package_platform") {
            column(name: "tipp_sort_name", type: "text")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-44") {
        addColumn(tableName: "title_instance_package_platform") {
            column(name: "tipp_subject_reference", type: "text")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-45") {
        addColumn(tableName: "title_instance_package_platform") {
            column(name: "tipp_summary_of_content", type: "varchar(255)")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-46") {
        addColumn(tableName: "title_instance_package_platform") {
            column(name: "tipp_title_type", type: "varchar(255)")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-47") {
        addColumn(tableName: "title_instance_package_platform") {
            column(name: "tipp_imprint", type: "text")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-48") {
        addColumn(tableName: "title_instance_package_platform") {
            column(name: "tipp_volume", type: "varchar(255)")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-49") {
        addForeignKeyConstraint(baseColumnNames: "tipp_medium_rv_fk", baseTableName: "title_instance_package_platform", constraintName: "FKiakmns0h0193nhxeru4f6dpye", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value")
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-50") {
        dropForeignKeyConstraint(baseTableName: "title_instance_package_platform", constraintName: "fke793fb8f40e502f5")
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-51") {
        dropColumn(columnName: "tipp_core_status_end_date", tableName: "title_instance_package_platform")
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-52") {
        dropColumn(columnName: "tipp_core_status_start_date", tableName: "title_instance_package_platform")
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-53") {
        dropColumn(columnName: "tipp_rectype", tableName: "title_instance_package_platform")
    }

    changeSet(author: "agalffy (modified)", id: "1611728842136-54") {
        grailsChange {
            change {
                sql.execute("update title_instance_package_platform" +
                        " set tipp_name = ti_title," +
                        " tipp_title_type = (select case" +
                        " when class = 'com.k_int.kbplus.DatabaseInstance' then 'Database'" +
                        " when class = 'com.k_int.kbplus.BookInstance' then 'Book'" +
                        " when class = 'com.k_int.kbplus.JournalInstance' then 'Journal'" +
                        " end)," +
                        " tipp_norm_name = ti_norm_title," +
                        " tipp_sort_name = sort_title," +
                        " tipp_medium_rv_fk = ti_medium_rv_fk," +
                        " tipp_series_name = ti_series_name," +
                        " tipp_subject_reference = ti_subject_reference," +
                        " tipp_date_first_in_print = bk_datefirstinprint," +
                        " tipp_date_first_online = bk_datefirstonline," +
                        " tipp_summary_of_content = bk_summaryofcontent," +
                        " tipp_volume = bk_volume," +
                        " tipp_first_editor = bk_first_editor," +
                        " tipp_first_author = bk_first_author," +
                        " tipp_edition_number = bk_edition_number," +
                        " tipp_edition_statement = bk_edition_statement," +
                        " tipp_edition_differentiator = bk_edition_differentiator" +
                        " from title_instance where ti_id = tipp_ti_fk;")
            }
            rollback {}
        }
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-55") {
        dropColumn(columnName: "tipp_ti_fk", tableName: "title_instance_package_platform")
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-56") {
        addColumn(tableName: "price_item") {
            column(name: "pi_tipp_fk", type: "int8")
        }
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-57") {
        addForeignKeyConstraint(baseColumnNames: "pi_tipp_fk", baseTableName: "price_item", constraintName: "FKlhc7vftba2vqpbfnjffx4syk1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "tipp_id", referencedTableName: "title_instance_package_platform")
    }

    changeSet(author: "agalffy (modified)", id: "1611728842136-58") {
        dropNotNullConstraint(columnDataType: "int8", columnName: "pi_ie_fk", tableName: "price_item")
    }

    changeSet(author: "agalffy (generated)", id: "1611728842136-59") {
        addColumn(tableName: "price_item") {
            column(name: "pi_end_date", type: "timestamp")
        }
    }

    changeSet(author: "agalffy (modified)", id: "1611728842136-60") {
        grailsChange {
            change {
                sql.execute('alter table price_item rename pi_price_date to pi_start_date;')
            }
            rollback {}
        }
    }

}
