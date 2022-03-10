databaseChangeLog = {

    changeSet(author: "jaegle (hand-coded)", id: "1571847838001-1") {
        grailsChange {
            change {

                def propDefId = sql.rows("select pd_id from property_definition where pd_name='NatStat Supplier ID'")

                if (! propDefId) {
                    // because 'NatStat Supplier ID' is not existing yet
                    // will be updated during next bootstrap
                    sql.execute("""
                        insert into property_definition(
                            pd_description,
                            pd_name,
                            version,
                            pd_mandatory,
                            pd_multiple_occurrence,
                            pd_used_for_logic,
                            pd_hard_data,
                            pd_type) values ('Platform Property', 'NatStat Supplier ID', 0, false, false, true, false, 'class java.lang.String')
                        """)

                    propDefId = sql.rows("select pd_id from property_definition where pd_name='NatStat Supplier ID'")
                }

                def mapping = [:] // LAS:eR Platform -> NatStat Platform
                mapping['4a9ccc66-eb60-4355-8eb6-516300b71354'] = 'American Chemical Society (ACS)' //'ACS Publications'
                mapping['83415044-6f9b-43ec-a7e2-a2c7a35e7dfd'] = 'American Physical Society (APS)' //American Physical Society
                mapping['711cd72a-0d32-4541-9005-307d7ad648d5'] = 'Annual Reviews' //Annual Reviews
                mapping['1e121035-0c07-4185-afd5-1227deddc670'] = 'Brepols Online' //Brepolis
                mapping['4e5e83fc-a0e4-4997-a296-6b61dc3cab14'] = 'De Gruyter' //de Gruyter
                mapping['6eae6a82-c38a-4f35-ae11-94a8b61c41ff'] = 'EBSCOhost' //EBSCOhost
                mapping['b5195b47-c36c-4061-8d1f-6557e9298322'] = 'Emerald Insight' //Emerald Insight
                mapping['827bf762-d390-42bf-83d2-d7d7083b2880'] = 'Gale' //Gale Databases
                mapping['64e05f6b-a08e-4696-a9cd-e5203b16eeee'] = 'JAMA Network' //JAMA Network
                mapping['c8d11fb2-b49e-47a2-b1de-199d455e14ff'] = 'LexisNexis' //Nexis
                mapping['113660c1-6e08-45ed-84e2-d33767a89f75'] = 'Mary Ann Liebert' //Mary Ann Liebert, Inc. Publishers
                mapping['5f722e57-3c6c-4a5b-aab2-7a069bd34711'] = 'Microbiology Society' //Microbiology Society
                mapping['17d31712-9a96-4e92-b82d-9a1500f3e210'] = 'MIT Press' //MIT Press Journals
                mapping['362df969-ebc3-4b21-ad3e-a75f483b8595'] = 'OvidSP' //Ovid SP
                mapping['845dac4d-a6f9-4ca0-91e4-b03791a8240a'] = 'ProQuest' //ProQuest
                mapping['6005b2ac-2f76-4407-9f76-6616e5d91441'] = 'ProQuest Ebook Central' //ProQuest: Ebook Central
                mapping['c672bd34-1162-407f-86a6-3e774aaa6d47'] = 'Scitation' //Scitation
                mapping['2ce8cd11-6c18-4ba4-a8a2-6414e9094980'] = 'Society for Industrial and Applied Mathematics (SIAM)' //epubs SIAM
                mapping['a1d5bba4-8130-462b-bc69-c4c685341def'] = 'Thieme Connect' //Thieme Connect
                mapping['f03990c5-93a8-4539-93af-482765a6ebf4'] = 'Web of Science' //Web of Science
                mapping['3a8f1652-f88c-4908-bf63-e0985a3cb73e'] = 'Wiley Online Library' //Cochrane Library

                mapping.each() {
                    def owner = sql.rows("select plat_id from platform where plat_gokb_id = ?", it.key)
                    def existingProperty = sql.firstRow("select * from platform_custom_property where string_value = ?", it.value)
                    if (owner.plat_id[0] && !existingProperty) {
                        sql.execute("insert into platform_custom_property (version, owner_id, string_value, type_id) values (1,${owner.plat_id[0]},${it.value},${propDefId.pd_id[0]})")
                    }
                }
            }
        }
    }

    changeSet(author: "kloberd (generated)", id: "1571927484417-2") {
        dropForeignKeyConstraint(baseTableName: "fact", baseTableSchemaName: "public", constraintName: "fk2fd66c40c7d5b5")
    }

    changeSet(author: "kloberd (generated)", id: "1571927484417-3") {
        dropColumn(columnName: "supplier_id", tableName: "fact")
    }

    changeSet(author: "kloberd (generated)", id: "1571927484417-4") {
        addColumn(schemaName: "public", tableName: "fact") {
            column(name: "supplier_id", type: "int8")
        }
    }

    changeSet(author: "kloberd (generated)", id: "1571927484417-5") {
        createIndex(indexName: "fact_access_idx", schemaName: "public", tableName: "fact") {
            column(name: "inst_id")

            column(name: "related_title_id")

            column(name: "supplier_id")
        }
    }

    changeSet(author: "kloberd (generated)", id: "1571927484417-6") {
        addForeignKeyConstraint(baseColumnNames: "supplier_id", baseTableName: "fact", baseTableSchemaName: "public", constraintName: "FK2FD66CA859705E", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "plat_id", referencedTableName: "platform", referencedTableSchemaName: "public", referencesUniqueColumn: "false")
    }
}