package de.laser.remote

import de.laser.config.ConfigMapper

/**
 * Represents an external API-Source. Currently in use to retain we:kb API connections which are not used for bulk operations;
 * the title synchronisation is being done via {@link GlobalRecordSource}s
 */
@Deprecated
class ApiSource {

    String name
    String baseUrl
    String editUrl
    String fixToken

    ApiSource() {
        name     = 'WE:KB'
        baseUrl  = ConfigMapper.getWekbServerURL()
        editUrl  = ConfigMapper.getWekbServerURL()
        fixToken = '/api2'
    }

    static mapWith = 'none'

    static ApiSource getCurrent() {
        new ApiSource()
    }
}
