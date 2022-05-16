import de.laser.helper.AppUtils

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1621490825895-1") {
        createTable(tableName: "alternative_name") {
            column(autoIncrement: "true", name: "altname_id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "alternative_namePK")
            }

            column(name: "altname_version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "altname_org_fk", type: "BIGINT")

            column(name: "altname_date_created", type: "TIMESTAMP WITHOUT TIME ZONE")

            column(name: "altname_last_updated", type: "TIMESTAMP WITHOUT TIME ZONE")

            column(name: "altname_plat_fk", type: "BIGINT")

            column(name: "altname_pkg_fk", type: "BIGINT")

            column(name: "altname_tipp_fk", type: "BIGINT")

            column(name: "altname_last_updated_cascading", type: "TIMESTAMP WITHOUT TIME ZONE")

            column(name: "altname_name", type: "TEXT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "galffy (generated)", id: "1621490825895-2") {
        addForeignKeyConstraint(baseColumnNames: "altname_org_fk", baseTableName: "alternative_name", constraintName: "FKdwax8ovqp662t5mjj2sr9d2ea", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "org_id", referencedTableName: "org")
    }

    changeSet(author: "galffy (generated)", id: "1621490825895-3") {
        addForeignKeyConstraint(baseColumnNames: "altname_plat_fk", baseTableName: "alternative_name", constraintName: "FKg6kdybov99o9wk5kqi3wvks8b", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "plat_id", referencedTableName: "platform")
    }

    changeSet(author: "galffy (generated)", id: "1621490825895-4") {
        addForeignKeyConstraint(baseColumnNames: "altname_pkg_fk", baseTableName: "alternative_name", constraintName: "FKm7degfr0acup28g84pg9dqk1n", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "pkg_id", referencedTableName: "package")
    }

    changeSet(author: "galffy (generated)", id: "1621490825895-5") {
        addForeignKeyConstraint(baseColumnNames: "altname_tipp_fk", baseTableName: "alternative_name", constraintName: "FKm8bxthd65t56iawk8lumhtcnk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "tipp_id", referencedTableName: "title_instance_package_platform")
    }

    changeSet(author: "galffy (hand-coded)", id: "1621490825895-6") {
        grailsChange {
            change {
                if(AppUtils.getCurrentServer() != AppUtils.QA) {
                    sql.execute("insert into alternative_name (altname_version, altname_org_fk, altname_name, altname_date_created, altname_last_updated, altname_last_updated_cascading) values " +
                        "(1,(select org_id from org where org_guid = 'org:e63b09c4-67c2-4eb7-879c-132b58f6f2c0'),(select org_name from org where org_guid = 'org:17da6850-f1d3-46f7-9047-6c0c74b554c7'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:d6c20e58-36ba-4f6f-b4c6-acbb081cbec9'),(select org_name from org where org_guid = 'org:7b8f92b0-d823-4025-8e86-fa03982d6c13'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:a1f7c0ee-90fe-42c2-b677-be74d29410fb'),(select org_name from org where org_guid = 'org:b846cfa0-c181-4d24-bf60-2019c73cb6ce'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:a1f7c0ee-90fe-42c2-b677-be74d29410fb'),(select org_name from org where org_guid = 'org:286e9eab-d763-47c0-8c73-12a9c8c51e93'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:8e734af6-b9a7-4a4f-999b-04929c3df440'),(select org_name from org where org_guid = 'org:bdef3aaf-24bd-4390-be11-bac61401da24'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:f50bf5ab-20bf-42e4-9693-23b957d768c4'),(select org_name from org where org_guid = 'org:93df63a0-1be2-4a3f-b0ee-79baa42ceac1'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:1ae8e965-7ef8-4d60-8c82-4ef182c125ab'),(select org_name from org where org_guid = 'org:b5eb9561-6cb0-472b-accb-0e1f68a8c6be'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:bcf75f41-be36-423a-ae4f-474a08a79077'),(select org_name from org where org_guid = 'org:82e41baa-6a64-4d5f-b238-475cb4691f5b'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:bcf75f41-be36-423a-ae4f-474a08a79077'),(select org_name from org where org_guid = 'org:e180b987-56b7-451a-8b7e-5826658baaba'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:bcf75f41-be36-423a-ae4f-474a08a79077'),(select org_name from org where org_guid = 'org:f113d3f2-0d6e-4d9d-88b2-972424d85869'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:bcf75f41-be36-423a-ae4f-474a08a79077'),(select org_name from org where org_guid = 'org:bdd20f38-b7d1-41c8-8522-38bfa0324cb7'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:bcf75f41-be36-423a-ae4f-474a08a79077'),(select org_name from org where org_guid = 'org:f54b84f2-17e9-42c8-a0c2-afa2092f50e3'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:bcf75f41-be36-423a-ae4f-474a08a79077'),(select org_name from org where org_guid = 'org:4de583d8-808c-4746-be72-c6fa5cdc7e75'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:bcf75f41-be36-423a-ae4f-474a08a79077'),(select org_name from org where org_guid = 'org:4b77517c-17e8-4ae6-a8a0-4ce4d0056c56'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:4752867c-6728-4a62-9726-f6b7c2d57732'),(select org_name from org where org_guid = 'org:ac7f345c-9986-4d22-a66a-166d70d8d5f3'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:4752867c-6728-4a62-9726-f6b7c2d57732'),(select org_name from org where org_guid = 'org:364c9ded-d0d3-453a-97e5-a7b92dc7ac06'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:4752867c-6728-4a62-9726-f6b7c2d57732'),(select org_name from org where org_guid = 'org:5cf92938-c592-4d39-8d4b-743746a9b75d'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:2fdf7cb8-dc1d-4729-bd8b-e96c3c92df92'),(select org_name from org where org_guid = 'org:9a9e8128-0464-42e1-8efd-26c2ffb9d637'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:c47229f9-0c8a-4131-89bf-24c92b1aa26f'),(select org_name from org where org_guid = 'org:540ce570-30df-40d3-abfc-511ee8c435d1'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:c47229f9-0c8a-4131-89bf-24c92b1aa26f'),(select org_name from org where org_guid = 'org:d4ced649-5221-4a67-98c0-321c869eb9c7'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:dd43083e-6d8d-4b1d-b3e2-8e62505104db'),(select org_name from org where org_guid = 'org:44d25cae-1807-4280-8917-0e1fb9a53389'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:dd43083e-6d8d-4b1d-b3e2-8e62505104db'),(select org_name from org where org_guid = 'org:3454eed1-0079-47bd-8375-2a0c60550e3f'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:c45d3294-60f0-4e03-998a-119fa1078be4'),(select org_name from org where org_guid = 'org:85e01cc7-144d-4f90-bc33-0e1616dd2203'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:c45d3294-60f0-4e03-998a-119fa1078be4'),(select org_name from org where org_guid = 'org:39ae5e9b-e751-42f9-8aa6-867445d4aa04'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:9163f88b-4f2a-45a6-9a92-4b6d3137d928'),(select org_name from org where org_guid = 'org:37ba14e2-6954-418e-86b9-e853152f9321'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:bcd96d78-4a4b-476c-8485-8b19e45dd176'),(select org_name from org where org_guid = 'org:d1c46d41-4598-41f6-ba0e-448cd17240d2'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:e30e4c39-58f5-4217-a3b0-c9db804095ee'),(select org_name from org where org_guid = 'org:e3c8a101-00ef-4285-af5b-0b2a860b5f53'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:3d19c6be-223f-42b7-9eb7-a686608fafba'),(select org_name from org where org_guid = 'org:436f704f-c569-4746-94b3-ddb751d00ccd'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:23385155-a6a5-4682-a9ff-b05ab3961356'),(select org_name from org where org_guid = 'org:d014fa4b-1295-47ab-a508-6376122115ad'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:3ae993d5-f639-4190-ad13-37ea735fffc7'),(select org_name from org where org_guid = 'org:71955eba-d76d-47fb-8ea3-79769da87ed8'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:916f7a03-8c31-4c9b-afe2-4db21c2d54f8'),(select org_name from org where org_guid = 'org:d4022019-74ee-478a-be58-25ed416bc2b2'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:916f7a03-8c31-4c9b-afe2-4db21c2d54f8'),(select org_name from org where org_guid = 'org:3e661689-0f9e-4ef0-9a2e-6a81c7b4482e'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:b89515f8-ab2d-4d34-920e-a7ea634721fe'),(select org_name from org where org_guid = 'org:a8d560e8-cfb3-4f7a-a21d-205d1046bd3c'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:2870c6da-0eb5-4535-9c28-1970641f6afd'),(select org_name from org where org_guid = 'org:45f8f4b6-4c00-4a6d-b685-326b2f3958fa'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:e8e5c310-ec76-461f-a315-c6df642bcec1'),(select org_name from org where org_guid = 'org:f609a75a-6ac3-4405-88f0-c308ebafbb4f'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:04e2ea97-f431-451e-9355-19428eb2f1d7'),(select org_name from org where org_guid = 'org:c81feaa2-806a-4268-8780-978f7fa314e9'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:c4b8c9f2-4513-4823-9606-17b4a5eeaa26'),(select org_name from org where org_guid = 'org:aee74ed6-dc4e-4248-a460-a6231a9d6a2b'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:e92e4532-5130-48e3-a541-a218a9fb5d7c'),(select org_name from org where org_guid = 'org:b4b6fc19-80e5-4700-ad1c-dbb476ed6216'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:29d3b9d5-dc4a-4d69-b599-0492e3468be3'),(select org_name from org where org_guid = 'org:7f6be80e-b380-4ccc-acec-2b8c745bb3ca'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:b2d23ab4-5772-48f9-9605-6ffa0ae754aa'),(select org_name from org where org_guid = 'org:a29f5339-4380-41cb-b180-435a98376c5d'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:474b93f5-9618-4404-bf22-cd0cfec42033'),(select org_name from org where org_guid = 'org:1d6f984c-750f-4e7d-94d5-849ec5962b48'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:4ca1727e-43d4-4f38-a284-ab2f5cc5ee48'),(select org_name from org where org_guid = 'org:8b85c253-47ed-4b14-a1d1-e5f6b9739151'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:4ca1727e-43d4-4f38-a284-ab2f5cc5ee48'),(select org_name from org where org_guid = 'org:c60c5ef3-5487-4290-844b-387db0057cec'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:40d60a84-5b46-4211-a2f8-055469302a6b'),(select org_name from org where org_guid = 'org:5d11a79e-db78-48ad-a4a8-05491e0095d9'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:5cbd3d72-3f3d-4602-9d9b-fc3b6e0238bc'),(select org_name from org where org_guid = 'org:07da5b9e-2625-4734-ab85-63562a9bf864'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:71a824ae-55d0-413b-90b9-5ffb950bc9db'),(select org_name from org where org_guid = 'org:fb808c2b-63a1-4ee2-a79b-e3427908003a'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:15597d59-8616-4fbe-86ed-1fc6a1456a05'),(select org_name from org where org_guid = 'org:bf1b3e49-af41-4165-8ac7-7c9ab9c931e7'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:b1fe534e-bb70-41c0-ae4a-817050dc46e9'),(select org_name from org where org_guid = 'org:fa0f7179-64d7-418a-ad95-323bec7b13c5'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:cd3f95a2-84b2-46c6-9609-3fb8ff5ae222'),(select org_name from org where org_guid = 'org:7cf3c7a3-a793-4a23-8613-69fb85b7b79b'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:7c7c1d0e-1024-4cd0-9613-2ffdb5ee1dbf'),(select org_name from org where org_guid = 'org:99fe0b0d-7d54-4518-b390-3a7f9180b01e'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:01da6a6d-0785-4907-9958-5c76aca9ea9c'),(select org_name from org where org_guid = 'org:215cde86-6ef8-41a2-9b0a-19036f446640'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:096d51d1-b940-48d4-a146-43a4aaea3db5'),(select org_name from org where org_guid = 'org:9d1841b8-68d9-44ff-9a30-0cb7f2251655'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:1950b32c-9c90-48fb-b5d0-81d5ce7a9c63'),(select org_name from org where org_guid = 'org:d7cc3ab7-8cb8-4db1-98e5-c58b2776266b'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:0019fc25-6e71-4012-9089-ee01022fdcc2'),(select org_name from org where org_guid = 'org:5f49f995-856f-425b-b432-f6dad06e8467'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:8c49d976-abbb-4b48-be8b-8b9beb61b422'),(select org_name from org where org_guid = 'org:1cb10313-d9c1-45bd-9eae-eb8ae6f7f646'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:44e72252-7dd3-4d1e-b4b5-8df4a1931e0a'),(select org_name from org where org_guid = 'org:485e78a7-db42-4c33-aa12-99aa3bd42aa1'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:89d46c7e-e211-4f73-921a-6e3c5b80b8f5'),(select org_name from org where org_guid = 'org:b19bc7cc-0ecd-4adf-a1cc-f220632b209e'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:1eb06058-e7a7-4227-b719-28f1c1823ee7'),(select org_name from org where org_guid = 'org:844b9c79-b8de-4649-9e6a-fe80e006a8e7'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:fb51d077-d9b0-4ac8-9268-0a127472410a'),(select org_name from org where org_guid = 'org:fb51d077-d9b0-4ac8-9268-0a127472410a'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:46dd9bbb-5e8a-4821-88e9-ea8f82323516'),(select org_name from org where org_guid = 'org:55546da1-9dff-40de-be8f-bb5d1848e218'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:4c85d551-aa59-41c4-a58a-083eaaa1d928'),(select org_name from org where org_guid = 'org:12bb771c-2149-4766-a56c-389d4a18351d'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:09815091-7f98-4b12-be11-f5c9cdf9e0f3'),(select org_name from org where org_guid = 'org:918cf525-fa24-40b3-95b5-50b99715cfae'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:bab50952-57ab-4efa-a592-dc3ab318c33a'),(select org_name from org where org_guid = 'org:d59f4392-2621-49d1-a447-4543f1560684'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:7e428d90-384a-46b0-a231-f1a4ff67b300'),(select org_name from org where org_guid = 'org:02833063-ca33-4c19-bf9a-3cb3b4b1c5fe'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:1855e5ee-520a-4ca4-bfff-419f1d917501'),(select org_name from org where org_guid = 'org:6a4f23b6-84f5-498f-8d88-3946fae9bd2d'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:b2815858-6140-4a7d-a494-68e31aa14902'),(select org_name from org where org_guid = 'org:b74e2e9f-e810-4459-9e9f-9614a6a5f132'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:50b0db6d-29ae-4e48-9690-b7d39bbcd6b6'),(select org_name from org where org_guid = 'org:ba82965d-7a6c-4786-a6c7-7b44c080b5f9'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:a779ff3b-e9b1-4105-b422-eab6e8cc81bc'),(select org_name from org where org_guid = 'org:f53c60fd-c7db-4481-b225-fee54cac7b55'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:8e029661-fe36-4766-842c-a1fa0a098854'),(select org_name from org where org_guid = 'org:8f8f13e3-bf0d-4e7c-992e-aafbf01848f4'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:119d807f-90a2-43c0-a5fe-83cdca4e99ec'),(select org_name from org where org_guid = 'org:b32a22b1-c053-40ea-b992-87fca5ca4711'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:17e38a7b-9cfd-4b7c-9b84-3f310df40715'),(select org_name from org where org_guid = 'org:23be0f88-d3e1-4cb1-8ff6-c005180a166a'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:b05aa631-4e79-49ac-9298-b355f5ce8d98'),(select org_name from org where org_guid = 'org:07bd80eb-af27-4d28-a04b-ec7061e459e3'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:7e2a63e5-207c-4b3c-a703-e42dde46bd95'),(select org_name from org where org_guid = 'org:d5d21628-b84b-4ec3-a1e7-285eaa6dfb5d'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:335933e2-bdcd-4163-af34-2e21460b04cc'),(select org_name from org where org_guid = 'org:d3e15119-d685-4e7a-9054-f184a089433c'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:be660aae-9829-4412-9658-fb3b04799363'),(select org_name from org where org_guid = 'org:c9b96ae5-b521-47ce-8efd-64474bb0e1a1'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:368bc5c9-df2f-40f5-b8dc-835b4ac6b2c1'),(select org_name from org where org_guid = 'org:cc3130f9-bd51-455a-9e3c-3ef75ff7578b'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:ef9ab419-2d67-4edf-b6c3-a43916484dc0'),(select org_name from org where org_guid = 'org:a431f5a6-829e-4777-8543-9e4e74c1d3b8'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:88bc5ebf-f409-4238-8732-8d8ccf9bb3ff'),(select org_name from org where org_guid = 'org:d89652bb-f0cd-4d99-9f49-1eb35392ebae'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:88c89482-2b28-4a2b-b26e-85ec4905b7d2'),(select org_name from org where org_guid = 'org:3eb72eac-55dc-47c9-96de-eff70dd86b21'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:cb43aa60-084a-422b-9f21-4daa6cf70b42'),(select org_name from org where org_guid = 'org:5613609c-42e0-4e40-a853-e4d17c5a56a6'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:37930005-cfef-4acd-80cb-27417578bbac'),(select org_name from org where org_guid = 'org:2108efd9-71d6-4916-9d51-507f96cb2e60'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:37930005-cfef-4acd-80cb-27417578bbac'),(select org_name from org where org_guid = 'org:ea63d606-5bea-4fd9-ad4f-e5f115b9e28d'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:70ba1619-3d6a-4e50-ba95-7fc23c100bc1'),(select org_name from org where org_guid = 'org:b1edc221-85fd-4dc7-b4c5-9c1b44061040'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:ad48a3c8-5038-4745-bf5f-3fc059eb1870'),(select org_name from org where org_guid = 'org:05341f43-1dab-4945-b928-250142923fbb'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:394206eb-1d39-4b6b-b6a6-5d0415e86065'),(select org_name from org where org_guid = 'org:50d6b488-2b5e-47fc-bbc4-5c667325f9f0'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:32c5dc79-e551-416f-82ef-440f3628e21e'),(select org_name from org where org_guid = 'org:2b5c03b8-953b-4446-affb-a66400b25179'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:b71b57e4-1603-48a2-978c-4096ceacd35a'),(select org_name from org where org_guid = 'org:230c9d71-3815-485f-9830-6e9add677730'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:4b4162bc-33d8-4e87-bd1c-037691a68f30'),(select org_name from org where org_guid = 'org:d7fbbc69-b4c0-4abd-8a94-9462386a00c5'),now(),now(),now()), " +
                        "(1,(select org_id from org where org_guid = 'org:e92f86bd-2b89-467c-a9c3-9cf15e157be7'),(select org_name from org where org_guid = 'org:c7d77572-8ab3-44b4-bc03-5bacad864209'),now(),now(),now())")
                }
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1621490825895-7") {
        createIndex(indexName: "or_title_idx", tableName: "org_role") {
            column(name: "or_title_fk")
        }
    }

    changeSet(author: "galffy (modified)", id: "1621490825895-8") {
        createIndex(indexName: "ti_id_idx", tableName: "title_instance") {
            column(name: "ti_id")
        }
    }

    changeSet(author: "galffy (modified)", id: "1621490825895-9") {
        createIndex(indexName: "ot_org_rv_idx", tableName: "org_type") {
            column(name: "org_id")

            column(name: "refdata_value_id")
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1621490825895-10") {
        grailsChange {
            change {
                sql.execute("delete from org_role where not exists (select org_id from org_type where org_id = or_org_fk) and not exists (select us_id from user_setting where or_org_fk = us_org_fk);")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1621490825895-11") {
        grailsChange {
            change {
                sql.execute("delete from identifier where not exists (select org_id from org_type where org_id = id_org_fk) and not exists (select us_id from user_setting where id_org_fk = us_org_fk);")
            }
            rollback {}
        }
    }

    /* may delete most of what just has been inserted; there is no way to ensure not null constraint prematurely */
    changeSet(author: "galffy (hand-coded)", id: "1621490825895-12") {
        grailsChange {
            change {
                sql.execute("delete from alternative_name where not exists (select org_id from org_type where org_id = altname_org_fk) and not exists (select us_id from user_setting where altname_org_fk = us_org_fk);")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1621490825895-13") {
        grailsChange {
            change {
                sql.execute("update platform set plat_org_fk = null where not exists (select org_id from org_type where org_id = plat_org_fk) and not exists (select us_id from user_setting where plat_org_fk = us_org_fk);")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1621490825895-14") {
        grailsChange {
            change {
                sql.execute("delete from person_role where not exists (select org_id from org_type where org_id = pr_org_fk) and not exists (select us_id from user_setting where pr_org_fk = us_org_fk);")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1621490825895-15") {
        grailsChange {
            change {
                sql.execute("delete from org_setting where not exists (select org_id from org_type where org_id = os_org_fk) and not exists (select us_id from user_setting where os_org_fk = us_org_fk);")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1621490825895-16") {
        grailsChange {
            change {
                sql.execute("delete from system_profiler where not exists (select org_id from org_type where org_id = sp_context_fk) and not exists (select us_id from user_setting where sp_context_fk = us_org_fk);")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1621490825895-17") {
        grailsChange {
            change {
                sql.execute("delete from org_property where not exists (select org_id from org_type where org_id = op_owner_fk) and not exists (select us_id from user_setting where op_owner_fk = us_org_fk);")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1621490825895-18") {
        grailsChange {
            change {
                sql.execute("delete from combo where not exists (select org_id from org_type where org_id = combo_from_org_fk) and not exists (select us_id from user_setting where combo_from_org_fk = us_org_fk);")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1621490825895-19") {
        grailsChange {
            change {
                sql.execute("delete from doc_context where not exists (select org_id from org_type where org_id = dc_target_org_fk) and not exists (select us_id from user_setting where dc_target_org_fk = us_org_fk);")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1621490825895-20") {
        grailsChange {
            change {
                if(AppUtils.getCurrentServer() != AppUtils.QA) {
                    sql.execute("delete from org as o where not exists (select org_id from org_type where org_id = o.org_id) and not exists (select us_id from user_setting where o.org_id = us_org_fk);")
                }
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1621490825895-21") {
        grailsChange {
            change {
                sql.execute("update org set org_status_rv_fk = (select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdc_description = 'org.status' and rdv_value = 'Current') where org_status_rv_fk is null")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (modified)", id: "1621490825895-22") {
        addNotNullConstraint(columnDataType: "int8", columnName: "org_status_rv_fk", tableName: "org")
    }

}
