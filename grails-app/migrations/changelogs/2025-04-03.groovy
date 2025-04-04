package changelogs

databaseChangeLog = {

    changeSet(author: "djebeniani (hand-coded)", id: "1743668039192-1") {
        grailsChange {
            change {
                String query = "update survey_config_properties set surconpro_survey_property_fk = (SELECT t.pd_id FROM property_definition t WHERE pd_description = 'Survey Property' and pd_name = 'Beck Price Category A-F') " +
                        "where surconpro_survey_property_fk = (SELECT t.pd_id FROM property_definition t WHERE pd_description = 'Survey Property' and pd_name = 'Category A-F')"
                int count = sql.executeUpdate(query)
                String info = "${query}: ${count}"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1743668039192-2") {
        grailsChange {
            change {
                String query = "update survey_result set surre_type_fk = (SELECT t.pd_id FROM property_definition t WHERE pd_description = 'Survey Property' and pd_name = 'Beck Price Category A-F') " +
                        "where surre_type_fk = (SELECT t.pd_id FROM property_definition t WHERE pd_description = 'Survey Property' and pd_name = 'Category A-F')"
                int count = sql.executeUpdate(query)
                String info = "${query}: ${count}"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

    changeSet(author: "djebeniani (hand-coded)", id: "1743668039192-3") {
        grailsChange {
            change {
                String query = "DELETE FROM property_definition t WHERE pd_description = 'Survey Property' and pd_name = 'Category A-F'"
                int count = sql.executeUpdate(query)
                String info = "${query}: ${count}"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }
}
