package changelogs

databaseChangeLog = {

    changeSet(author: "djebeniani (generated)", id: "1682522978736-1") {
        createTable(tableName: "async_mail_attachment") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "async_mail_attachmentPK")
            }

            column(name: "message_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "mime_type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "inline", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "content", type: "BYTEA") {
                constraints(nullable: "false")
            }

            column(name: "attachment_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "attachments_idx", type: "INTEGER")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1682522978736-2") {
        createTable(tableName: "async_mail_bcc") {
            column(name: "message_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "bcc_string", type: "VARCHAR(256)")

            column(name: "bcc_idx", type: "INTEGER")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1682522978736-3") {
        createTable(tableName: "async_mail_cc") {
            column(name: "message_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "cc_string", type: "VARCHAR(256)")

            column(name: "cc_idx", type: "INTEGER")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1682522978736-4") {
        createTable(tableName: "async_mail_header") {
            column(name: "message_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "header_value", type: "VARCHAR(255)")

            column(name: "header_name", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1682522978736-5") {
        createTable(tableName: "async_mail_mess") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "async_mail_messPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "begin_date", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "html", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "alternative", type: "TEXT")

            column(name: "reply_to", type: "VARCHAR(256)")

            column(name: "max_attempts_count", type: "INTEGER") {
                constraints(nullable: "false")
            }

            column(name: "attempts_count", type: "INTEGER") {
                constraints(nullable: "false")
            }

            column(name: "from_column", type: "VARCHAR(256)")

            column(name: "text", type: "TEXT") {
                constraints(nullable: "false")
            }

            column(name: "last_attempt_date", type: "TIMESTAMP WITHOUT TIME ZONE")

            column(name: "create_date", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "end_date", type: "TIMESTAMP WITHOUT TIME ZONE") {
                constraints(nullable: "false")
            }

            column(name: "envelope_from", type: "VARCHAR(256)")

            column(name: "attempt_interval", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "mark_delete", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "priority", type: "INTEGER") {
                constraints(nullable: "false")
            }

            column(name: "sent_date", type: "TIMESTAMP WITHOUT TIME ZONE")

            column(name: "status", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "mark_delete_attachments", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "subject", type: "VARCHAR(988)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1682522978736-6") {
        createTable(tableName: "async_mail_to") {
            column(name: "message_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "to_string", type: "VARCHAR(256)")

            column(name: "to_idx", type: "INTEGER")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1682522978736-7") {
        createTable(tableName: "mail_report") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "mail_reportPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1682522978736-8") {
        addForeignKeyConstraint(baseColumnNames: "message_id", baseTableName: "async_mail_attachment", constraintName: "FK710ojreahvmit1c3706pggyc5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "async_mail_mess", validate: "true")
    }


}
