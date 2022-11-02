package changelogs

databaseChangeLog = {

    changeSet(author: "djebeniani (generated)", id: "1666691153668-1") {
        createIndex(indexName: "surconpro_survey_config_idx", tableName: "survey_config_properties") {
            column(name: "surconpro_survey_config_fk")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1666691153668-2") {
        createIndex(indexName: "surconpro_survey_property_idx", tableName: "survey_config_properties") {
            column(name: "surconpro_survey_property_fk")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1666691153668-3") {
        createIndex(indexName: "surre_owner_idx", tableName: "survey_result") {
            column(name: "surre_owner_fk")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1666691153668-4") {
        createIndex(indexName: "surre_participant_idx", tableName: "survey_result") {
            column(name: "surre_participant_fk")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1666691153668-5") {
        createIndex(indexName: "surre_survey_config_idx", tableName: "survey_result") {
            column(name: "surre_survey_config_fk")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1666691153668-6") {
        createIndex(indexName: "surre_type_idx", tableName: "survey_result") {
            column(name: "surre_type_fk")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1666691153668-7") {
        createIndex(indexName: "surorg_org_idx", tableName: "survey_org") {
            column(name: "surorg_org_fk")
        }
    }

    changeSet(author: "djebeniani (generated)", id: "1666691153668-8") {
        createIndex(indexName: "surorg_surveyconfig_idx", tableName: "survey_org") {
            column(name: "surorg_surveyconfig_fk")
        }
    }
}
