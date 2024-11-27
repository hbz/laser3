package de.laser.remote

import de.laser.config.ConfigMapper

/**
 * Represents an external API-Source. Currently in use to retain we:kb API connections which are not used for bulk operations;
 * the title synchronisation is being done via {@link GlobalRecordSource}s
 */
@Deprecated
class ApiSource {

    // TODO - ERMS-5917

    String name
    String baseUrl

    ApiSource() {
        name     = 'WE:KB'
        baseUrl  = ConfigMapper.getWekbServerURL()
    }

    static mapWith = 'none'

    static ApiSource getCurrent() {
        new ApiSource()
    }

    /**
     * @return ConfigMapper.getWekbServerURL()
     */
    static String getURL() {
        ConfigMapper.getWekbServerURL()
    }


    /**
     * @return ConfigMapper.getWekbServerURL() + '/resource/show'
     */
    String getResourceShowURL() {
        baseUrl + '/resource/show'
    }
    /**
     * @return ConfigMapper.getWekbServerURL() + '/api2/searchApi'
     */
    String getSearchApiURL() {
        baseUrl + '/api2/searchApi'
    }
    /**
     * @return ConfigMapper.getWekbServerURL() + '/api2/sushiSources'
     */
    String getSushiSourcesURL() {
        baseUrl + '/api2/sushiSources'
    }
    /**
     * @return ConfigMapper.getWekbServerURL() + '/api2/groups'
     */
    String getGroupsURL() {
        baseUrl + '/api2/groups'
    }
}
