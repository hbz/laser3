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

    String getSearchApiUrl() {
        baseUrl + '/api2/searchApi'
    }
    String getSushiSourcesUrl() {
        baseUrl + '/api2/sushiSources'
    }
    String getGroupsUrl() {
        baseUrl + '/api2/groups'
    }
}
