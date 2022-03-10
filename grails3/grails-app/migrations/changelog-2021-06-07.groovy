databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1623049813501-1") {
        addColumn(tableName: "org") {
            column(name: "org_retirement_date", type: "timestamp")
        }
    }

    changeSet(author: "galffy (modified)", id: "1623049813501-2") {
        addColumn(tableName: "subscription_package") {
            column(name: "sp_freeze_holding", type: "boolean")
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1623049813501-3") {
        grailsChange {
            change {
                sql.execute('update subscription_package set sp_freeze_holding = false;')
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1623049813501-4") {
        addNotNullConstraint(columnDataType: "boolean", columnName: "sp_freeze_holding", tableName: "subscription_package")
    }

    changeSet(author: "galffy (hand-coded)", id: "1623049813501-5") {
        grailsChange {
            change {
                sql.execute("update org_role set or_org_fk = (select org_id from org where org_guid = 'org:d6c20e58-36ba-4f6f-b4c6-acbb081cbec9') where or_org_fk = (select org_id from org where org_guid = 'org:7b8f92b0-d823-4025-8e86-fa03982d6c13');" +
                        "update org_role set or_org_fk = (select org_id from org where org_guid = 'org:a1f7c0ee-90fe-42c2-b677-be74d29410fb') where or_org_fk = (select org_id from org where org_guid = 'org:286e9eab-d763-47c0-8c73-12a9c8c51e93');" +
                        "update org_role set or_org_fk = (select org_id from org where org_guid = 'org:1ae8e965-7ef8-4d60-8c82-4ef182c125ab') where or_org_fk = (select org_id from org where org_guid = 'org:b5eb9561-6cb0-472b-accb-0e1f68a8c6be');" +
                        "update org_role set or_org_fk = (select org_id from org where org_guid = 'org:bcf75f41-be36-423a-ae4f-474a08a79077') where or_org_fk in (select org_id from org where org_guid in ('org:f113d3f2-0d6e-4d9d-88b2-972424d85869','org:e180b987-56b7-451a-8b7e-5826658baaba'));" +
                        "update org_role set or_org_fk = (select org_id from org where org_guid = 'org:4752867c-6728-4a62-9726-f6b7c2d57732') where or_org_fk = (select org_id from org where org_guid = 'org:364c9ded-d0d3-453a-97e5-a7b92dc7ac06');" +
                        "update org_role set or_org_fk = (select org_id from org where org_guid = 'org:c47229f9-0c8a-4131-89bf-24c92b1aa26f') where or_org_fk = (select org_id from org where org_guid = 'org:d4ced649-5221-4a67-98c0-321c869eb9c7');" +
                        "update org_role set or_org_fk = (select org_id from org where org_guid = 'org:dd43083e-6d8d-4b1d-b3e2-8e62505104db') where or_org_fk = (select org_id from org where org_guid = 'org:3454eed1-0079-47bd-8375-2a0c60550e3f');" +
                        "update org_role set or_org_fk = (select org_id from org where org_guid = 'org:916f7a03-8c31-4c9b-afe2-4db21c2d54f8') where or_org_fk = (select org_id from org where org_guid = 'org:3e661689-0f9e-4ef0-9a2e-6a81c7b4482e');" +
                        "update org_role set or_org_fk = (select org_id from org where org_guid = 'org:4ca1727e-43d4-4f38-a284-ab2f5cc5ee48') where or_org_fk = (select org_id from org where org_guid = 'org:c60c5ef3-5487-4290-844b-387db0057cec');" +
                        "update org_role set or_org_fk = (select org_id from org where org_guid = 'org:8e029661-fe36-4766-842c-a1fa0a098854') where or_org_fk = (select org_id from org where org_guid = 'org:8f8f13e3-bf0d-4e7c-992e-aafbf01848f4');" +
                        "update org_role set or_org_fk = (select org_id from org where org_guid = 'org:e63b09c4-67c2-4eb7-879c-132b58f6f2c0') where or_org_fk = (select org_id from org where org_guid = 'org:17da6850-f1d3-46f7-9047-6c0c74b554c7');" +
                        "update org_role set or_org_fk = (select org_id from org where org_guid = 'org:b05aa631-4e79-49ac-9298-b355f5ce8d98') where or_org_fk = (select org_id from org where org_guid = 'org:07bd80eb-af27-4d28-a04b-ec7061e459e3');")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1623049813501-6") {
        grailsChange {
            change {
                sql.execute("update org_property set op_owner_fk = (select org_id from org where org_guid = 'org:d6c20e58-36ba-4f6f-b4c6-acbb081cbec9') where op_owner_fk = (select org_id from org where org_guid = 'org:7b8f92b0-d823-4025-8e86-fa03982d6c13');" +
                        "update org_property set op_owner_fk = (select org_id from org where org_guid = 'org:a1f7c0ee-90fe-42c2-b677-be74d29410fb') where op_owner_fk = (select org_id from org where org_guid = 'org:286e9eab-d763-47c0-8c73-12a9c8c51e93');" +
                        "update org_property set op_owner_fk = (select org_id from org where org_guid = 'org:1ae8e965-7ef8-4d60-8c82-4ef182c125ab') where op_owner_fk = (select org_id from org where org_guid = 'org:b5eb9561-6cb0-472b-accb-0e1f68a8c6be');" +
                        "update org_property set op_owner_fk = (select org_id from org where org_guid = 'org:bcf75f41-be36-423a-ae4f-474a08a79077') where op_owner_fk in (select org_id from org where org_guid in ('org:f113d3f2-0d6e-4d9d-88b2-972424d85869','org:e180b987-56b7-451a-8b7e-5826658baaba'));" +
                        "update org_property set op_owner_fk = (select org_id from org where org_guid = 'org:4752867c-6728-4a62-9726-f6b7c2d57732') where op_owner_fk = (select org_id from org where org_guid = 'org:364c9ded-d0d3-453a-97e5-a7b92dc7ac06');" +
                        "update org_property set op_owner_fk = (select org_id from org where org_guid = 'org:c47229f9-0c8a-4131-89bf-24c92b1aa26f') where op_owner_fk = (select org_id from org where org_guid = 'org:d4ced649-5221-4a67-98c0-321c869eb9c7');" +
                        "update org_property set op_owner_fk = (select org_id from org where org_guid = 'org:dd43083e-6d8d-4b1d-b3e2-8e62505104db') where op_owner_fk = (select org_id from org where org_guid = 'org:3454eed1-0079-47bd-8375-2a0c60550e3f');" +
                        "update org_property set op_owner_fk = (select org_id from org where org_guid = 'org:916f7a03-8c31-4c9b-afe2-4db21c2d54f8') where op_owner_fk = (select org_id from org where org_guid = 'org:3e661689-0f9e-4ef0-9a2e-6a81c7b4482e');" +
                        "update org_property set op_owner_fk = (select org_id from org where org_guid = 'org:4ca1727e-43d4-4f38-a284-ab2f5cc5ee48') where op_owner_fk = (select org_id from org where org_guid = 'org:c60c5ef3-5487-4290-844b-387db0057cec');" +
                        "update org_property set op_owner_fk = (select org_id from org where org_guid = 'org:8e029661-fe36-4766-842c-a1fa0a098854') where op_owner_fk = (select org_id from org where org_guid = 'org:8f8f13e3-bf0d-4e7c-992e-aafbf01848f4');" +
                        "update org_property set op_owner_fk = (select org_id from org where org_guid = 'org:e63b09c4-67c2-4eb7-879c-132b58f6f2c0') where op_owner_fk = (select org_id from org where org_guid = 'org:17da6850-f1d3-46f7-9047-6c0c74b554c7');" +
                        "update org_property set op_owner_fk = (select org_id from org where org_guid = 'org:b05aa631-4e79-49ac-9298-b355f5ce8d98') where op_owner_fk = (select org_id from org where org_guid = 'org:07bd80eb-af27-4d28-a04b-ec7061e459e3');")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1623049813501-7") {
        grailsChange {
            change {
                sql.execute("update platform set plat_org_fk = (select org_id from org where org_guid = 'org:d6c20e58-36ba-4f6f-b4c6-acbb081cbec9') where plat_org_fk = (select org_id from org where org_guid = 'org:7b8f92b0-d823-4025-8e86-fa03982d6c13');" +
                        "update platform set plat_org_fk = (select org_id from org where org_guid = 'org:a1f7c0ee-90fe-42c2-b677-be74d29410fb') where plat_org_fk = (select org_id from org where org_guid = 'org:286e9eab-d763-47c0-8c73-12a9c8c51e93');" +
                        "update platform set plat_org_fk = (select org_id from org where org_guid = 'org:1ae8e965-7ef8-4d60-8c82-4ef182c125ab') where plat_org_fk = (select org_id from org where org_guid = 'org:b5eb9561-6cb0-472b-accb-0e1f68a8c6be');" +
                        "update platform set plat_org_fk = (select org_id from org where org_guid = 'org:bcf75f41-be36-423a-ae4f-474a08a79077') where plat_org_fk in (select org_id from org where org_guid in ('org:f113d3f2-0d6e-4d9d-88b2-972424d85869','org:e180b987-56b7-451a-8b7e-5826658baaba'));" +
                        "update platform set plat_org_fk = (select org_id from org where org_guid = 'org:4752867c-6728-4a62-9726-f6b7c2d57732') where plat_org_fk = (select org_id from org where org_guid = 'org:364c9ded-d0d3-453a-97e5-a7b92dc7ac06');" +
                        "update platform set plat_org_fk = (select org_id from org where org_guid = 'org:c47229f9-0c8a-4131-89bf-24c92b1aa26f') where plat_org_fk = (select org_id from org where org_guid = 'org:d4ced649-5221-4a67-98c0-321c869eb9c7');" +
                        "update platform set plat_org_fk = (select org_id from org where org_guid = 'org:dd43083e-6d8d-4b1d-b3e2-8e62505104db') where plat_org_fk = (select org_id from org where org_guid = 'org:3454eed1-0079-47bd-8375-2a0c60550e3f');" +
                        "update platform set plat_org_fk = (select org_id from org where org_guid = 'org:916f7a03-8c31-4c9b-afe2-4db21c2d54f8') where plat_org_fk = (select org_id from org where org_guid = 'org:3e661689-0f9e-4ef0-9a2e-6a81c7b4482e');" +
                        "update platform set plat_org_fk = (select org_id from org where org_guid = 'org:4ca1727e-43d4-4f38-a284-ab2f5cc5ee48') where plat_org_fk = (select org_id from org where org_guid = 'org:c60c5ef3-5487-4290-844b-387db0057cec');" +
                        "update platform set plat_org_fk = (select org_id from org where org_guid = 'org:8e029661-fe36-4766-842c-a1fa0a098854') where plat_org_fk = (select org_id from org where org_guid = 'org:8f8f13e3-bf0d-4e7c-992e-aafbf01848f4');" +
                        "update platform set plat_org_fk = (select org_id from org where org_guid = 'org:e63b09c4-67c2-4eb7-879c-132b58f6f2c0') where plat_org_fk = (select org_id from org where org_guid = 'org:17da6850-f1d3-46f7-9047-6c0c74b554c7');" +
                        "update platform set plat_org_fk = (select org_id from org where org_guid = 'org:b05aa631-4e79-49ac-9298-b355f5ce8d98') where plat_org_fk = (select org_id from org where org_guid = 'org:07bd80eb-af27-4d28-a04b-ec7061e459e3');")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1623049813501-8") {
        grailsChange {
            change {
                sql.execute("update doc_context set dc_target_org_fk = (select org_id from org where org_guid = 'org:d6c20e58-36ba-4f6f-b4c6-acbb081cbec9') where dc_target_org_fk = (select org_id from org where org_guid = 'org:7b8f92b0-d823-4025-8e86-fa03982d6c13');" +
                        "update doc_context set dc_target_org_fk = (select org_id from org where org_guid = 'org:a1f7c0ee-90fe-42c2-b677-be74d29410fb') where dc_target_org_fk = (select org_id from org where org_guid = 'org:286e9eab-d763-47c0-8c73-12a9c8c51e93');" +
                        "update doc_context set dc_target_org_fk = (select org_id from org where org_guid = 'org:1ae8e965-7ef8-4d60-8c82-4ef182c125ab') where dc_target_org_fk = (select org_id from org where org_guid = 'org:b5eb9561-6cb0-472b-accb-0e1f68a8c6be');" +
                        "update doc_context set dc_target_org_fk = (select org_id from org where org_guid = 'org:bcf75f41-be36-423a-ae4f-474a08a79077') where dc_target_org_fk in (select org_id from org where org_guid in ('org:f113d3f2-0d6e-4d9d-88b2-972424d85869','org:e180b987-56b7-451a-8b7e-5826658baaba'));" +
                        "update doc_context set dc_target_org_fk = (select org_id from org where org_guid = 'org:4752867c-6728-4a62-9726-f6b7c2d57732') where dc_target_org_fk = (select org_id from org where org_guid = 'org:364c9ded-d0d3-453a-97e5-a7b92dc7ac06');" +
                        "update doc_context set dc_target_org_fk = (select org_id from org where org_guid = 'org:c47229f9-0c8a-4131-89bf-24c92b1aa26f') where dc_target_org_fk = (select org_id from org where org_guid = 'org:d4ced649-5221-4a67-98c0-321c869eb9c7');" +
                        "update doc_context set dc_target_org_fk = (select org_id from org where org_guid = 'org:dd43083e-6d8d-4b1d-b3e2-8e62505104db') where dc_target_org_fk = (select org_id from org where org_guid = 'org:3454eed1-0079-47bd-8375-2a0c60550e3f');" +
                        "update doc_context set dc_target_org_fk = (select org_id from org where org_guid = 'org:916f7a03-8c31-4c9b-afe2-4db21c2d54f8') where dc_target_org_fk = (select org_id from org where org_guid = 'org:3e661689-0f9e-4ef0-9a2e-6a81c7b4482e');" +
                        "update doc_context set dc_target_org_fk = (select org_id from org where org_guid = 'org:4ca1727e-43d4-4f38-a284-ab2f5cc5ee48') where dc_target_org_fk = (select org_id from org where org_guid = 'org:c60c5ef3-5487-4290-844b-387db0057cec');" +
                        "update doc_context set dc_target_org_fk = (select org_id from org where org_guid = 'org:8e029661-fe36-4766-842c-a1fa0a098854') where dc_target_org_fk = (select org_id from org where org_guid = 'org:8f8f13e3-bf0d-4e7c-992e-aafbf01848f4');" +
                        "update doc_context set dc_target_org_fk = (select org_id from org where org_guid = 'org:e63b09c4-67c2-4eb7-879c-132b58f6f2c0') where dc_target_org_fk = (select org_id from org where org_guid = 'org:17da6850-f1d3-46f7-9047-6c0c74b554c7');" +
                        "update doc_context set dc_target_org_fk = (select org_id from org where org_guid = 'org:b05aa631-4e79-49ac-9298-b355f5ce8d98') where dc_target_org_fk = (select org_id from org where org_guid = 'org:07bd80eb-af27-4d28-a04b-ec7061e459e3');")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1623049813501-9") {
        grailsChange {
            change {
                sql.execute("update doc_context set dc_org_fk = (select org_id from org where org_guid = 'org:d6c20e58-36ba-4f6f-b4c6-acbb081cbec9') where dc_org_fk = (select org_id from org where org_guid = 'org:7b8f92b0-d823-4025-8e86-fa03982d6c13');" +
                        "update doc_context set dc_org_fk = (select org_id from org where org_guid = 'org:a1f7c0ee-90fe-42c2-b677-be74d29410fb') where dc_org_fk = (select org_id from org where org_guid = 'org:286e9eab-d763-47c0-8c73-12a9c8c51e93');" +
                        "update doc_context set dc_org_fk = (select org_id from org where org_guid = 'org:1ae8e965-7ef8-4d60-8c82-4ef182c125ab') where dc_org_fk = (select org_id from org where org_guid = 'org:b5eb9561-6cb0-472b-accb-0e1f68a8c6be');" +
                        "update doc_context set dc_org_fk = (select org_id from org where org_guid = 'org:bcf75f41-be36-423a-ae4f-474a08a79077') where dc_org_fk in (select org_id from org where org_guid in ('org:f113d3f2-0d6e-4d9d-88b2-972424d85869','org:e180b987-56b7-451a-8b7e-5826658baaba'));" +
                        "update doc_context set dc_org_fk = (select org_id from org where org_guid = 'org:4752867c-6728-4a62-9726-f6b7c2d57732') where dc_org_fk = (select org_id from org where org_guid = 'org:364c9ded-d0d3-453a-97e5-a7b92dc7ac06');" +
                        "update doc_context set dc_org_fk = (select org_id from org where org_guid = 'org:c47229f9-0c8a-4131-89bf-24c92b1aa26f') where dc_org_fk = (select org_id from org where org_guid = 'org:d4ced649-5221-4a67-98c0-321c869eb9c7');" +
                        "update doc_context set dc_org_fk = (select org_id from org where org_guid = 'org:dd43083e-6d8d-4b1d-b3e2-8e62505104db') where dc_org_fk = (select org_id from org where org_guid = 'org:3454eed1-0079-47bd-8375-2a0c60550e3f');" +
                        "update doc_context set dc_org_fk = (select org_id from org where org_guid = 'org:916f7a03-8c31-4c9b-afe2-4db21c2d54f8') where dc_org_fk = (select org_id from org where org_guid = 'org:3e661689-0f9e-4ef0-9a2e-6a81c7b4482e');" +
                        "update doc_context set dc_org_fk = (select org_id from org where org_guid = 'org:4ca1727e-43d4-4f38-a284-ab2f5cc5ee48') where dc_org_fk = (select org_id from org where org_guid = 'org:c60c5ef3-5487-4290-844b-387db0057cec');" +
                        "update doc_context set dc_org_fk = (select org_id from org where org_guid = 'org:8e029661-fe36-4766-842c-a1fa0a098854') where dc_org_fk = (select org_id from org where org_guid = 'org:8f8f13e3-bf0d-4e7c-992e-aafbf01848f4');" +
                        "update doc_context set dc_org_fk = (select org_id from org where org_guid = 'org:e63b09c4-67c2-4eb7-879c-132b58f6f2c0') where dc_org_fk = (select org_id from org where org_guid = 'org:17da6850-f1d3-46f7-9047-6c0c74b554c7');" +
                        "update doc_context set dc_org_fk = (select org_id from org where org_guid = 'org:b05aa631-4e79-49ac-9298-b355f5ce8d98') where dc_org_fk = (select org_id from org where org_guid = 'org:07bd80eb-af27-4d28-a04b-ec7061e459e3');")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1623049813501-10") {
        grailsChange {
            change {
                sql.execute("update person_role set pr_org_fk = (select org_id from org where org_guid = 'org:d6c20e58-36ba-4f6f-b4c6-acbb081cbec9') where pr_org_fk = (select org_id from org where org_guid = 'org:7b8f92b0-d823-4025-8e86-fa03982d6c13');" +
                        "update person_role set pr_org_fk = (select org_id from org where org_guid = 'org:a1f7c0ee-90fe-42c2-b677-be74d29410fb') where pr_org_fk = (select org_id from org where org_guid = 'org:286e9eab-d763-47c0-8c73-12a9c8c51e93');" +
                        "update person_role set pr_org_fk = (select org_id from org where org_guid = 'org:1ae8e965-7ef8-4d60-8c82-4ef182c125ab') where pr_org_fk = (select org_id from org where org_guid = 'org:b5eb9561-6cb0-472b-accb-0e1f68a8c6be');" +
                        "update person_role set pr_org_fk = (select org_id from org where org_guid = 'org:bcf75f41-be36-423a-ae4f-474a08a79077') where pr_org_fk in (select org_id from org where org_guid in ('org:f113d3f2-0d6e-4d9d-88b2-972424d85869','org:e180b987-56b7-451a-8b7e-5826658baaba'));" +
                        "update person_role set pr_org_fk = (select org_id from org where org_guid = 'org:4752867c-6728-4a62-9726-f6b7c2d57732') where pr_org_fk = (select org_id from org where org_guid = 'org:364c9ded-d0d3-453a-97e5-a7b92dc7ac06');" +
                        "update person_role set pr_org_fk = (select org_id from org where org_guid = 'org:c47229f9-0c8a-4131-89bf-24c92b1aa26f') where pr_org_fk = (select org_id from org where org_guid = 'org:d4ced649-5221-4a67-98c0-321c869eb9c7');" +
                        "update person_role set pr_org_fk = (select org_id from org where org_guid = 'org:dd43083e-6d8d-4b1d-b3e2-8e62505104db') where pr_org_fk = (select org_id from org where org_guid = 'org:3454eed1-0079-47bd-8375-2a0c60550e3f');" +
                        "update person_role set pr_org_fk = (select org_id from org where org_guid = 'org:916f7a03-8c31-4c9b-afe2-4db21c2d54f8') where pr_org_fk = (select org_id from org where org_guid = 'org:3e661689-0f9e-4ef0-9a2e-6a81c7b4482e');" +
                        "update person_role set pr_org_fk = (select org_id from org where org_guid = 'org:4ca1727e-43d4-4f38-a284-ab2f5cc5ee48') where pr_org_fk = (select org_id from org where org_guid = 'org:c60c5ef3-5487-4290-844b-387db0057cec');" +
                        "update person_role set pr_org_fk = (select org_id from org where org_guid = 'org:8e029661-fe36-4766-842c-a1fa0a098854') where pr_org_fk = (select org_id from org where org_guid = 'org:8f8f13e3-bf0d-4e7c-992e-aafbf01848f4');" +
                        "update person_role set pr_org_fk = (select org_id from org where org_guid = 'org:e63b09c4-67c2-4eb7-879c-132b58f6f2c0') where pr_org_fk = (select org_id from org where org_guid = 'org:17da6850-f1d3-46f7-9047-6c0c74b554c7');" +
                        "update person_role set pr_org_fk = (select org_id from org where org_guid = 'org:b05aa631-4e79-49ac-9298-b355f5ce8d98') where pr_org_fk = (select org_id from org where org_guid = 'org:07bd80eb-af27-4d28-a04b-ec7061e459e3');")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1623049813501-11") {
        grailsChange {
            change {
                sql.execute("update identifier set id_org_fk = (select org_id from org where org_guid = 'org:d6c20e58-36ba-4f6f-b4c6-acbb081cbec9') where id_org_fk = (select org_id from org where org_guid = 'org:7b8f92b0-d823-4025-8e86-fa03982d6c13');" +
                        "update identifier set id_org_fk = (select org_id from org where org_guid = 'org:a1f7c0ee-90fe-42c2-b677-be74d29410fb') where id_org_fk = (select org_id from org where org_guid = 'org:286e9eab-d763-47c0-8c73-12a9c8c51e93');" +
                        "update identifier set id_org_fk = (select org_id from org where org_guid = 'org:1ae8e965-7ef8-4d60-8c82-4ef182c125ab') where id_org_fk = (select org_id from org where org_guid = 'org:b5eb9561-6cb0-472b-accb-0e1f68a8c6be');" +
                        "update identifier set id_org_fk = (select org_id from org where org_guid = 'org:bcf75f41-be36-423a-ae4f-474a08a79077') where id_org_fk in (select org_id from org where org_guid in ('org:f113d3f2-0d6e-4d9d-88b2-972424d85869','org:e180b987-56b7-451a-8b7e-5826658baaba'));" +
                        "update identifier set id_org_fk = (select org_id from org where org_guid = 'org:4752867c-6728-4a62-9726-f6b7c2d57732') where id_org_fk = (select org_id from org where org_guid = 'org:364c9ded-d0d3-453a-97e5-a7b92dc7ac06');" +
                        "update identifier set id_org_fk = (select org_id from org where org_guid = 'org:c47229f9-0c8a-4131-89bf-24c92b1aa26f') where id_org_fk = (select org_id from org where org_guid = 'org:d4ced649-5221-4a67-98c0-321c869eb9c7');" +
                        "update identifier set id_org_fk = (select org_id from org where org_guid = 'org:dd43083e-6d8d-4b1d-b3e2-8e62505104db') where id_org_fk = (select org_id from org where org_guid = 'org:3454eed1-0079-47bd-8375-2a0c60550e3f');" +
                        "update identifier set id_org_fk = (select org_id from org where org_guid = 'org:916f7a03-8c31-4c9b-afe2-4db21c2d54f8') where id_org_fk = (select org_id from org where org_guid = 'org:3e661689-0f9e-4ef0-9a2e-6a81c7b4482e');" +
                        "update identifier set id_org_fk = (select org_id from org where org_guid = 'org:4ca1727e-43d4-4f38-a284-ab2f5cc5ee48') where id_org_fk = (select org_id from org where org_guid = 'org:c60c5ef3-5487-4290-844b-387db0057cec');" +
                        "update identifier set id_org_fk = (select org_id from org where org_guid = 'org:8e029661-fe36-4766-842c-a1fa0a098854') where id_org_fk = (select org_id from org where org_guid = 'org:8f8f13e3-bf0d-4e7c-992e-aafbf01848f4');" +
                        "update identifier set id_org_fk = (select org_id from org where org_guid = 'org:e63b09c4-67c2-4eb7-879c-132b58f6f2c0') where id_org_fk = (select org_id from org where org_guid = 'org:17da6850-f1d3-46f7-9047-6c0c74b554c7');" +
                        "update identifier set id_org_fk = (select org_id from org where org_guid = 'org:b05aa631-4e79-49ac-9298-b355f5ce8d98') where id_org_fk = (select org_id from org where org_guid = 'org:07bd80eb-af27-4d28-a04b-ec7061e459e3');")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1623049813501-12") {
        grailsChange {
            change {
                sql.execute("delete from org_setting where os_org_fk in (select org_id from org where org_guid in ('org:17da6850-f1d3-46f7-9047-6c0c74b554c7','org:7b8f92b0-d823-4025-8e86-fa03982d6c13','org:b846cfa0-c181-4d24-bf60-2019c73cb6ce','org:286e9eab-d763-47c0-8c73-12a9c8c51e93','org:bdef3aaf-24bd-4390-be11-bac61401da24','org:93df63a0-1be2-4a3f-b0ee-79baa42ceac1','org:b5eb9561-6cb0-472b-accb-0e1f68a8c6be','org:82e41baa-6a64-4d5f-b238-475cb4691f5b','org:e180b987-56b7-451a-8b7e-5826658baaba','org:f113d3f2-0d6e-4d9d-88b2-972424d85869','org:bdd20f38-b7d1-41c8-8522-38bfa0324cb7','org:f54b84f2-17e9-42c8-a0c2-afa2092f50e3','org:4de583d8-808c-4746-be72-c6fa5cdc7e75','org:4b77517c-17e8-4ae6-a8a0-4ce4d0056c56','org:ac7f345c-9986-4d22-a66a-166d70d8d5f3','org:364c9ded-d0d3-453a-97e5-a7b92dc7ac06','org:5cf92938-c592-4d39-8d4b-743746a9b75d','org:9a9e8128-0464-42e1-8efd-26c2ffb9d637','org:540ce570-30df-40d3-abfc-511ee8c435d1','org:d4ced649-5221-4a67-98c0-321c869eb9c7','org:44d25cae-1807-4280-8917-0e1fb9a53389','org:3454eed1-0079-47bd-8375-2a0c60550e3f','org:85e01cc7-144d-4f90-bc33-0e1616dd2203','org:39ae5e9b-e751-42f9-8aa6-867445d4aa04','org:37ba14e2-6954-418e-86b9-e853152f9321','org:d1c46d41-4598-41f6-ba0e-448cd17240d2','org:e3c8a101-00ef-4285-af5b-0b2a860b5f53','org:436f704f-c569-4746-94b3-ddb751d00ccd','org:d014fa4b-1295-47ab-a508-6376122115ad','org:71955eba-d76d-47fb-8ea3-79769da87ed8','org:d4022019-74ee-478a-be58-25ed416bc2b2','org:3e661689-0f9e-4ef0-9a2e-6a81c7b4482e','org:a8d560e8-cfb3-4f7a-a21d-205d1046bd3c','org:45f8f4b6-4c00-4a6d-b685-326b2f3958fa','org:f609a75a-6ac3-4405-88f0-c308ebafbb4f','org:c81feaa2-806a-4268-8780-978f7fa314e9','org:aee74ed6-dc4e-4248-a460-a6231a9d6a2b','org:b4b6fc19-80e5-4700-ad1c-dbb476ed6216','org:7f6be80e-b380-4ccc-acec-2b8c745bb3ca','org:a29f5339-4380-41cb-b180-435a98376c5d','org:1d6f984c-750f-4e7d-94d5-849ec5962b48','org:8b85c253-47ed-4b14-a1d1-e5f6b9739151','org:c60c5ef3-5487-4290-844b-387db0057cec','org:5d11a79e-db78-48ad-a4a8-05491e0095d9','org:07da5b9e-2625-4734-ab85-63562a9bf864','org:fb808c2b-63a1-4ee2-a79b-e3427908003a','org:bf1b3e49-af41-4165-8ac7-7c9ab9c931e7','org:fa0f7179-64d7-418a-ad95-323bec7b13c5','org:7cf3c7a3-a793-4a23-8613-69fb85b7b79b','org:99fe0b0d-7d54-4518-b390-3a7f9180b01e','org:215cde86-6ef8-41a2-9b0a-19036f446640','org:9d1841b8-68d9-44ff-9a30-0cb7f2251655','org:d7cc3ab7-8cb8-4db1-98e5-c58b2776266b','org:5f49f995-856f-425b-b432-f6dad06e8467','org:1cb10313-d9c1-45bd-9eae-eb8ae6f7f646','org:485e78a7-db42-4c33-aa12-99aa3bd42aa1','org:b19bc7cc-0ecd-4adf-a1cc-f220632b209e','org:844b9c79-b8de-4649-9e6a-fe80e006a8e7','org:fb51d077-d9b0-4ac8-9268-0a127472410a','org:55546da1-9dff-40de-be8f-bb5d1848e218','org:12bb771c-2149-4766-a56c-389d4a18351d','org:918cf525-fa24-40b3-95b5-50b99715cfae','org:d59f4392-2621-49d1-a447-4543f1560684','org:02833063-ca33-4c19-bf9a-3cb3b4b1c5fe','org:6a4f23b6-84f5-498f-8d88-3946fae9bd2d','org:b74e2e9f-e810-4459-9e9f-9614a6a5f132','org:ba82965d-7a6c-4786-a6c7-7b44c080b5f9','org:f53c60fd-c7db-4481-b225-fee54cac7b55','org:8f8f13e3-bf0d-4e7c-992e-aafbf01848f4','org:b32a22b1-c053-40ea-b992-87fca5ca4711','org:23be0f88-d3e1-4cb1-8ff6-c005180a166a','org:07bd80eb-af27-4d28-a04b-ec7061e459e3','org:d5d21628-b84b-4ec3-a1e7-285eaa6dfb5d','org:d3e15119-d685-4e7a-9054-f184a089433c','org:c9b96ae5-b521-47ce-8efd-64474bb0e1a1','org:cc3130f9-bd51-455a-9e3c-3ef75ff7578b','org:a431f5a6-829e-4777-8543-9e4e74c1d3b8','org:d89652bb-f0cd-4d99-9f49-1eb35392ebae','org:3eb72eac-55dc-47c9-96de-eff70dd86b21','org:5613609c-42e0-4e40-a853-e4d17c5a56a6','org:2108efd9-71d6-4916-9d51-507f96cb2e60','org:ea63d606-5bea-4fd9-ad4f-e5f115b9e28d','org:b1edc221-85fd-4dc7-b4c5-9c1b44061040','org:05341f43-1dab-4945-b928-250142923fbb','org:50d6b488-2b5e-47fc-bbc4-5c667325f9f0','org:2b5c03b8-953b-4446-affb-a66400b25179','org:230c9d71-3815-485f-9830-6e9add677730','org:d7fbbc69-b4c0-4abd-8a94-9462386a00c5','org:c7d77572-8ab3-44b4-bc03-5bacad864209'));")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1623049813501-13") {
        grailsChange {
            change {
                sql.execute("delete from org_type where org_id in (select org_id from org where org_guid in ('org:17da6850-f1d3-46f7-9047-6c0c74b554c7','org:7b8f92b0-d823-4025-8e86-fa03982d6c13','org:b846cfa0-c181-4d24-bf60-2019c73cb6ce','org:286e9eab-d763-47c0-8c73-12a9c8c51e93','org:bdef3aaf-24bd-4390-be11-bac61401da24','org:93df63a0-1be2-4a3f-b0ee-79baa42ceac1','org:b5eb9561-6cb0-472b-accb-0e1f68a8c6be','org:82e41baa-6a64-4d5f-b238-475cb4691f5b','org:e180b987-56b7-451a-8b7e-5826658baaba','org:f113d3f2-0d6e-4d9d-88b2-972424d85869','org:bdd20f38-b7d1-41c8-8522-38bfa0324cb7','org:f54b84f2-17e9-42c8-a0c2-afa2092f50e3','org:4de583d8-808c-4746-be72-c6fa5cdc7e75','org:4b77517c-17e8-4ae6-a8a0-4ce4d0056c56','org:ac7f345c-9986-4d22-a66a-166d70d8d5f3','org:364c9ded-d0d3-453a-97e5-a7b92dc7ac06','org:5cf92938-c592-4d39-8d4b-743746a9b75d','org:9a9e8128-0464-42e1-8efd-26c2ffb9d637','org:540ce570-30df-40d3-abfc-511ee8c435d1','org:d4ced649-5221-4a67-98c0-321c869eb9c7','org:44d25cae-1807-4280-8917-0e1fb9a53389','org:3454eed1-0079-47bd-8375-2a0c60550e3f','org:85e01cc7-144d-4f90-bc33-0e1616dd2203','org:39ae5e9b-e751-42f9-8aa6-867445d4aa04','org:37ba14e2-6954-418e-86b9-e853152f9321','org:d1c46d41-4598-41f6-ba0e-448cd17240d2','org:e3c8a101-00ef-4285-af5b-0b2a860b5f53','org:436f704f-c569-4746-94b3-ddb751d00ccd','org:d014fa4b-1295-47ab-a508-6376122115ad','org:71955eba-d76d-47fb-8ea3-79769da87ed8','org:d4022019-74ee-478a-be58-25ed416bc2b2','org:3e661689-0f9e-4ef0-9a2e-6a81c7b4482e','org:a8d560e8-cfb3-4f7a-a21d-205d1046bd3c','org:45f8f4b6-4c00-4a6d-b685-326b2f3958fa','org:f609a75a-6ac3-4405-88f0-c308ebafbb4f','org:c81feaa2-806a-4268-8780-978f7fa314e9','org:aee74ed6-dc4e-4248-a460-a6231a9d6a2b','org:b4b6fc19-80e5-4700-ad1c-dbb476ed6216','org:7f6be80e-b380-4ccc-acec-2b8c745bb3ca','org:a29f5339-4380-41cb-b180-435a98376c5d','org:1d6f984c-750f-4e7d-94d5-849ec5962b48','org:8b85c253-47ed-4b14-a1d1-e5f6b9739151','org:c60c5ef3-5487-4290-844b-387db0057cec','org:5d11a79e-db78-48ad-a4a8-05491e0095d9','org:07da5b9e-2625-4734-ab85-63562a9bf864','org:fb808c2b-63a1-4ee2-a79b-e3427908003a','org:bf1b3e49-af41-4165-8ac7-7c9ab9c931e7','org:fa0f7179-64d7-418a-ad95-323bec7b13c5','org:7cf3c7a3-a793-4a23-8613-69fb85b7b79b','org:99fe0b0d-7d54-4518-b390-3a7f9180b01e','org:215cde86-6ef8-41a2-9b0a-19036f446640','org:9d1841b8-68d9-44ff-9a30-0cb7f2251655','org:d7cc3ab7-8cb8-4db1-98e5-c58b2776266b','org:5f49f995-856f-425b-b432-f6dad06e8467','org:1cb10313-d9c1-45bd-9eae-eb8ae6f7f646','org:485e78a7-db42-4c33-aa12-99aa3bd42aa1','org:b19bc7cc-0ecd-4adf-a1cc-f220632b209e','org:844b9c79-b8de-4649-9e6a-fe80e006a8e7','org:fb51d077-d9b0-4ac8-9268-0a127472410a','org:55546da1-9dff-40de-be8f-bb5d1848e218','org:12bb771c-2149-4766-a56c-389d4a18351d','org:918cf525-fa24-40b3-95b5-50b99715cfae','org:d59f4392-2621-49d1-a447-4543f1560684','org:02833063-ca33-4c19-bf9a-3cb3b4b1c5fe','org:6a4f23b6-84f5-498f-8d88-3946fae9bd2d','org:b74e2e9f-e810-4459-9e9f-9614a6a5f132','org:ba82965d-7a6c-4786-a6c7-7b44c080b5f9','org:f53c60fd-c7db-4481-b225-fee54cac7b55','org:8f8f13e3-bf0d-4e7c-992e-aafbf01848f4','org:b32a22b1-c053-40ea-b992-87fca5ca4711','org:23be0f88-d3e1-4cb1-8ff6-c005180a166a','org:07bd80eb-af27-4d28-a04b-ec7061e459e3','org:d5d21628-b84b-4ec3-a1e7-285eaa6dfb5d','org:d3e15119-d685-4e7a-9054-f184a089433c','org:c9b96ae5-b521-47ce-8efd-64474bb0e1a1','org:cc3130f9-bd51-455a-9e3c-3ef75ff7578b','org:a431f5a6-829e-4777-8543-9e4e74c1d3b8','org:d89652bb-f0cd-4d99-9f49-1eb35392ebae','org:3eb72eac-55dc-47c9-96de-eff70dd86b21','org:5613609c-42e0-4e40-a853-e4d17c5a56a6','org:2108efd9-71d6-4916-9d51-507f96cb2e60','org:ea63d606-5bea-4fd9-ad4f-e5f115b9e28d','org:b1edc221-85fd-4dc7-b4c5-9c1b44061040','org:05341f43-1dab-4945-b928-250142923fbb','org:50d6b488-2b5e-47fc-bbc4-5c667325f9f0','org:2b5c03b8-953b-4446-affb-a66400b25179','org:230c9d71-3815-485f-9830-6e9add677730','org:d7fbbc69-b4c0-4abd-8a94-9462386a00c5','org:c7d77572-8ab3-44b4-bc03-5bacad864209'));")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1623049813501-14") {
        grailsChange {
            change {
                sql.execute("delete from org where org_guid in ('org:17da6850-f1d3-46f7-9047-6c0c74b554c7','org:7b8f92b0-d823-4025-8e86-fa03982d6c13','org:b846cfa0-c181-4d24-bf60-2019c73cb6ce','org:286e9eab-d763-47c0-8c73-12a9c8c51e93','org:bdef3aaf-24bd-4390-be11-bac61401da24','org:93df63a0-1be2-4a3f-b0ee-79baa42ceac1','org:b5eb9561-6cb0-472b-accb-0e1f68a8c6be','org:82e41baa-6a64-4d5f-b238-475cb4691f5b','org:e180b987-56b7-451a-8b7e-5826658baaba','org:f113d3f2-0d6e-4d9d-88b2-972424d85869','org:bdd20f38-b7d1-41c8-8522-38bfa0324cb7','org:f54b84f2-17e9-42c8-a0c2-afa2092f50e3','org:4de583d8-808c-4746-be72-c6fa5cdc7e75','org:4b77517c-17e8-4ae6-a8a0-4ce4d0056c56','org:ac7f345c-9986-4d22-a66a-166d70d8d5f3','org:364c9ded-d0d3-453a-97e5-a7b92dc7ac06','org:5cf92938-c592-4d39-8d4b-743746a9b75d','org:9a9e8128-0464-42e1-8efd-26c2ffb9d637','org:540ce570-30df-40d3-abfc-511ee8c435d1','org:d4ced649-5221-4a67-98c0-321c869eb9c7','org:44d25cae-1807-4280-8917-0e1fb9a53389','org:3454eed1-0079-47bd-8375-2a0c60550e3f','org:85e01cc7-144d-4f90-bc33-0e1616dd2203','org:39ae5e9b-e751-42f9-8aa6-867445d4aa04','org:37ba14e2-6954-418e-86b9-e853152f9321','org:d1c46d41-4598-41f6-ba0e-448cd17240d2','org:e3c8a101-00ef-4285-af5b-0b2a860b5f53','org:436f704f-c569-4746-94b3-ddb751d00ccd','org:d014fa4b-1295-47ab-a508-6376122115ad','org:71955eba-d76d-47fb-8ea3-79769da87ed8','org:d4022019-74ee-478a-be58-25ed416bc2b2','org:3e661689-0f9e-4ef0-9a2e-6a81c7b4482e','org:a8d560e8-cfb3-4f7a-a21d-205d1046bd3c','org:45f8f4b6-4c00-4a6d-b685-326b2f3958fa','org:f609a75a-6ac3-4405-88f0-c308ebafbb4f','org:c81feaa2-806a-4268-8780-978f7fa314e9','org:aee74ed6-dc4e-4248-a460-a6231a9d6a2b','org:b4b6fc19-80e5-4700-ad1c-dbb476ed6216','org:7f6be80e-b380-4ccc-acec-2b8c745bb3ca','org:a29f5339-4380-41cb-b180-435a98376c5d','org:1d6f984c-750f-4e7d-94d5-849ec5962b48','org:8b85c253-47ed-4b14-a1d1-e5f6b9739151','org:c60c5ef3-5487-4290-844b-387db0057cec','org:5d11a79e-db78-48ad-a4a8-05491e0095d9','org:07da5b9e-2625-4734-ab85-63562a9bf864','org:fb808c2b-63a1-4ee2-a79b-e3427908003a','org:bf1b3e49-af41-4165-8ac7-7c9ab9c931e7','org:fa0f7179-64d7-418a-ad95-323bec7b13c5','org:7cf3c7a3-a793-4a23-8613-69fb85b7b79b','org:99fe0b0d-7d54-4518-b390-3a7f9180b01e','org:215cde86-6ef8-41a2-9b0a-19036f446640','org:9d1841b8-68d9-44ff-9a30-0cb7f2251655','org:d7cc3ab7-8cb8-4db1-98e5-c58b2776266b','org:5f49f995-856f-425b-b432-f6dad06e8467','org:1cb10313-d9c1-45bd-9eae-eb8ae6f7f646','org:485e78a7-db42-4c33-aa12-99aa3bd42aa1','org:b19bc7cc-0ecd-4adf-a1cc-f220632b209e','org:844b9c79-b8de-4649-9e6a-fe80e006a8e7','org:fb51d077-d9b0-4ac8-9268-0a127472410a','org:55546da1-9dff-40de-be8f-bb5d1848e218','org:12bb771c-2149-4766-a56c-389d4a18351d','org:918cf525-fa24-40b3-95b5-50b99715cfae','org:d59f4392-2621-49d1-a447-4543f1560684','org:02833063-ca33-4c19-bf9a-3cb3b4b1c5fe','org:6a4f23b6-84f5-498f-8d88-3946fae9bd2d','org:b74e2e9f-e810-4459-9e9f-9614a6a5f132','org:ba82965d-7a6c-4786-a6c7-7b44c080b5f9','org:f53c60fd-c7db-4481-b225-fee54cac7b55','org:8f8f13e3-bf0d-4e7c-992e-aafbf01848f4','org:b32a22b1-c053-40ea-b992-87fca5ca4711','org:23be0f88-d3e1-4cb1-8ff6-c005180a166a','org:07bd80eb-af27-4d28-a04b-ec7061e459e3','org:d5d21628-b84b-4ec3-a1e7-285eaa6dfb5d','org:d3e15119-d685-4e7a-9054-f184a089433c','org:c9b96ae5-b521-47ce-8efd-64474bb0e1a1','org:cc3130f9-bd51-455a-9e3c-3ef75ff7578b','org:a431f5a6-829e-4777-8543-9e4e74c1d3b8','org:d89652bb-f0cd-4d99-9f49-1eb35392ebae','org:3eb72eac-55dc-47c9-96de-eff70dd86b21','org:5613609c-42e0-4e40-a853-e4d17c5a56a6','org:2108efd9-71d6-4916-9d51-507f96cb2e60','org:ea63d606-5bea-4fd9-ad4f-e5f115b9e28d','org:b1edc221-85fd-4dc7-b4c5-9c1b44061040','org:05341f43-1dab-4945-b928-250142923fbb','org:50d6b488-2b5e-47fc-bbc4-5c667325f9f0','org:2b5c03b8-953b-4446-affb-a66400b25179','org:230c9d71-3815-485f-9830-6e9add677730','org:d7fbbc69-b4c0-4abd-8a94-9462386a00c5','org:c7d77572-8ab3-44b4-bc03-5bacad864209');")
            }
            rollback {}
        }
    }

}
