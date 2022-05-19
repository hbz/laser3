databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1652873827838-1") {
        createIndex(indexName: "pending_change_msg_token_idx", tableName: "pending_change") {
            column(name: "pc_msg_token")
        }
    }

}
