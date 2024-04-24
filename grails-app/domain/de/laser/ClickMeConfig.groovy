package de.laser

class ClickMeConfig {

    String name
    Org contextOrg
    String contextUrl
    String jsonConfig
    String nameOfClickMeMap
    String clickMeType
    int configOrder

    Date dateCreated
    Date lastUpdated

    static constraints = {

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

        contextOrg column: 'cmc_context_org_fk', index: 'cmc_context_org_idx'

    }
}
