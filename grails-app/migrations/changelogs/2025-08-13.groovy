package changelogs

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1755066930342-1") {
        createTable(tableName: "mule_cache") {
            column(autoIncrement: "true", name: "mc_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "mule_cachePK")
            }

            column(name: "mc_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "mc_cfg", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "mc_string_value", type: "TEXT")

            column(name: "mc_date_created", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "mc_date_value", type: "TIMESTAMP WITHOUT TIME ZONE")

            column(name: "mc_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "mc_server", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }
}
