package de.laser.remote

import de.laser.config.ConfigMapper

/**
 * Represents an external API-Source. Currently in use to retain we:kb API connections which are not used for bulk operations;
 * the title synchronisation is being done via {@link GlobalRecordSource}s
 */
class Wekb {

    // TODO - ERMS-5917

    String name
    String baseUrl

    Wekb() {
        name     = 'WE:KB'
        baseUrl  = ConfigMapper.getWekbServerURL()
    }

    static mapWith = 'none'

//    static Wekb getInstance() {
//        new Wekb()
//    }

    /**
     * @return ConfigMapper.getWekbServerURL()
     */
    static String getURL() {
        ConfigMapper.getWekbServerURL()
    }

    /**
     * @return ConfigMapper.getWekbServerURL() + '/resource/show'
     */
    static String getResourceShowURL() {
        getURL() + '/resource/show'
    }

    /**
     * @return ConfigMapper.getWekbServerURL() + '/api2/searchApi'
     */
    static String getSearchApiURL() {
        getURL() + '/api2/searchApi'
    }
    /**
     * @return ConfigMapper.getWekbServerURL() + '/api2/sushiSources'
     */
    static String getSushiSourcesURL() {
        getURL() + '/api2/sushiSources'
    }
    /**
     * @return ConfigMapper.getWekbServerURL() + '/api2/groups'
     */
    static String getGroupsURL() {
        getURL() + '/api2/groups'
    }
}
