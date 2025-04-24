package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1745397625037-1") {
        addColumn(tableName: "platform") {
            column(name: "plat_access_audio_rv_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1745397625037-2") {
        addColumn(tableName: "platform") {
            column(name: "plat_access_database_rv_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1745397625037-3") {
        addColumn(tableName: "platform") {
            column(name: "plat_access_epub_rv_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1745397625037-4") {
        addColumn(tableName: "platform") {
            column(name: "plat_access_pdf_rv_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1745397625037-5") {
        addColumn(tableName: "platform") {
            column(name: "plat_access_platform_rv_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1745397625037-6") {
        addColumn(tableName: "platform") {
            column(name: "plat_access_video_rv_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1745397625037-7") {
        addColumn(tableName: "platform") {
            column(name: "plat_accessibility_statement_available_fk_rv", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1745397625037-8") {
        addColumn(tableName: "platform") {
            column(name: "plat_accessibility_statement_url", type: "text")
        }
    }

    changeSet(author: "galffy (generated)", id: "1745397625037-9") {
        addColumn(tableName: "platform") {
            column(name: "plat_onix_metadata_rv_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1745397625037-10") {
        addColumn(tableName: "platform") {
            column(name: "plat_player_for_audio_rv_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1745397625037-11") {
        addColumn(tableName: "platform") {
            column(name: "plat_player_for_video_rv_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1745397625037-12") {
        addColumn(tableName: "platform") {
            column(name: "plat_viewer_for_epub_rv_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1745397625037-13") {
        addColumn(tableName: "platform") {
            column(name: "plat_viewer_for_pdf_rv_fk", type: "int8")
        }
    }

    changeSet(author: "galffy (generated)", id: "1745397625037-14") {
        addForeignKeyConstraint(baseColumnNames: "plat_viewer_for_epub_rv_fk", baseTableName: "platform", constraintName: "FK5n191g28qxxxcipkeyiep3wdx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1745397625037-15") {
        addForeignKeyConstraint(baseColumnNames: "plat_access_epub_rv_fk", baseTableName: "platform", constraintName: "FK66rxg8cu4qihoc2vq2jfe8hs", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1745397625037-16") {
        addForeignKeyConstraint(baseColumnNames: "plat_access_video_rv_fk", baseTableName: "platform", constraintName: "FKbmeqg542ngawp36hc2mbb39x8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1745397625037-17") {
        addForeignKeyConstraint(baseColumnNames: "plat_onix_metadata_rv_fk", baseTableName: "platform", constraintName: "FKea7bx843my0iystys2mbhu5f7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1745397625037-18") {
        addForeignKeyConstraint(baseColumnNames: "plat_player_for_video_rv_fk", baseTableName: "platform", constraintName: "FKf4xsof8o9cw4py4doh7x5w4jr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1745397625037-19") {
        addForeignKeyConstraint(baseColumnNames: "plat_access_database_rv_fk", baseTableName: "platform", constraintName: "FKikjve2jmuhofy85q93b0rot49", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1745397625037-20") {
        addForeignKeyConstraint(baseColumnNames: "plat_access_pdf_rv_fk", baseTableName: "platform", constraintName: "FKlg723vikc7of88cv745vlqwg4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1745397625037-21") {
        addForeignKeyConstraint(baseColumnNames: "plat_access_audio_rv_fk", baseTableName: "platform", constraintName: "FKln5ig3rn3586luy5l77d8xw36", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1745397625037-22") {
        addForeignKeyConstraint(baseColumnNames: "plat_viewer_for_pdf_rv_fk", baseTableName: "platform", constraintName: "FKmx4tdjr6kxp996l7ytg71g4tu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1745397625037-23") {
        addForeignKeyConstraint(baseColumnNames: "plat_player_for_audio_rv_fk", baseTableName: "platform", constraintName: "FKn0dps93v9k9w7fcoqtxog2unu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1745397625037-24") {
        addForeignKeyConstraint(baseColumnNames: "plat_access_platform_rv_fk", baseTableName: "platform", constraintName: "FKnnj7iy99ovite9liffyimmpat", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }

    changeSet(author: "galffy (generated)", id: "1745397625037-25") {
        addForeignKeyConstraint(baseColumnNames: "plat_accessibility_statement_available_fk_rv", baseTableName: "platform", constraintName: "FKrug4gk33b5e9857dvfjiopaic", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "rdv_id", referencedTableName: "refdata_value", validate: "true")
    }
}
