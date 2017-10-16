databaseChangeLog = {

    changeSet(author: "kloberd", id: "1508147866666-1") {
        modifyDataType(columnName: "org_imp_id", newDataType: "varchar(255)", tableName: "org")
    }

    changeSet(author: "kloberd", id: "1508147866666-2") {
        //Error executing SQL CREATE INDEX `org_imp_id_idx` ON `org`(`org_imp_id`): Specified key was too long; max key length is 767 bytes

        createIndex(indexName: "org_imp_id_idx", tableName: "org") {
            column(name: "org_imp_id")
        }
    }

    // TODO

    // Error executing SQL ALTER TABLE `global_record_tracker` ADD CONSTRAINT `FK808F5966F6287F86` FOREIGN KEY (`grt_owner_fk`) REFERENCES `global_record_info` (`gri_id`): Cannot add or update a child row: a foreign key constraint fails (`KBPlusDev`.`#sql-121a_489`, CONSTRAINT `FK808F5966F6287F86` FOREIGN KEY (`grt_owner_fk`) REFERENCES `global_record_info` (`gri_id`))
    // Error executing SQL CREATE INDEX `td_name_idx` ON `property_definition`(`pd_name`): Duplicate key name 'td_name_idx'

}