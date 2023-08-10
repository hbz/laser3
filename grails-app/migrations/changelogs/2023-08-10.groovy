package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1691652087822-1") {
        createTable(tableName: "favorite") {
            column(autoIncrement: "true", name: "fav_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "favoritePK")
            }

            column(name: "fav_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "fav_org_fk", type: "BIGINT")

            column(name: "fav_date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "fav_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "fav_plt_fk", type: "BIGINT")

            column(name: "fav_pkg_fk", type: "BIGINT")

            column(name: "fav_user_fk", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "fav_type_enum", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "fav_note", type: "TEXT")
        }
    }

    changeSet(author: "klober (generated)", id: "1691652087822-29") {
        createIndex(indexName: "fav_user_idx", tableName: "favorite") {
            column(name: "fav_user_fk")
        }
    }

    changeSet(author: "klober (generated)", id: "1691652087822-40") {
        addForeignKeyConstraint(baseColumnNames: "fav_plt_fk", baseTableName: "favorite", constraintName: "FKbvm588um9vxgrkv3e6rd935ry", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "plat_id", referencedTableName: "platform", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1691652087822-52") {
        addForeignKeyConstraint(baseColumnNames: "fav_user_fk", baseTableName: "favorite", constraintName: "FKjx48vwk8si2k28760ljkhv7c4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "usr_id", referencedTableName: "user", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1691652087822-56") {
        addForeignKeyConstraint(baseColumnNames: "fav_pkg_fk", baseTableName: "favorite", constraintName: "FKmbkivm4b3crcamf6tjs5pu4c7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pkg_id", referencedTableName: "package", validate: "true")
    }

    changeSet(author: "klober (generated)", id: "1691652087822-60") {
        addForeignKeyConstraint(baseColumnNames: "fav_org_fk", baseTableName: "favorite", constraintName: "FKpl3q85ee79e6g3j4c576yuu5c", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org", validate: "true")
    }
}
