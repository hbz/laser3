package de.laser

import grails.converters.JSON

class ClickMeConfig {

    String name
    Org contextOrg
    String contextUrl
    String jsonConfig
    String nameOfClickMeMap
    String clickMeType
    int configOrder
    String note = ""

    Date dateCreated
    Date lastUpdated

    static constraints = {
        note (nullable: true, blank: true)
    }

    static mapping = {
        id column: 'cmc_id'
        version column: 'cmc_version'

        dateCreated column: 'cmc_date_created'
        lastUpdated column: 'cmc_last_updated'

        name column: 'cmc_name'
        nameOfClickMeMap column: 'cmc_name_of_click_me_map'
        clickMeType column: 'cmc_click_me_type'
        contextUrl column: 'cmc_context_url'
        jsonConfig column: 'cmc_json_config', type: 'text'
        configOrder column: 'cmc_config_order'
        note column: 'cmc_note', type: 'text'

        contextOrg column: 'cmc_context_org_fk', index: 'cmc_context_org_idx'

    }

    Map getClickMeConfigMap(){
        Map clickMeConfigMap = jsonConfig ? JSON.parse(jsonConfig) : [:]
        return clickMeConfigMap
    }

    int getClickMeConfigSize(){
        int count = 0
        Map clickMeConfigMap = getClickMeConfigMap()
        if(clickMeConfigMap)
            count = clickMeConfigMap.size()

        return count
    }
}
