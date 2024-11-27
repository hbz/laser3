package de.laser.remote

import de.laser.config.ConfigMapper

/**
 * Represents an external API-Source. Currently in use to retain we:kb API connections which are not used for bulk operations;
 * the title synchronisation is being done via {@link GlobalRecordSource}s
 */
@Deprecated
class ApiSource {

    enum ApiTyp
    {
        /**
         * The we:kb is a fork of the GOKb
         */
        GOKBAPI
    }

    String name     = 'GOKB Phaeton'
    String baseUrl  = ConfigMapper.getWekbServerURL()
    String editUrl  = ConfigMapper.getWekbServerURL()
    String fixToken = '/api2'
    ApiTyp typ      = ApiTyp.GOKBAPI

    static mapWith = 'none'

    ApiSource getCurrent() {
        new ApiSource()
    }
}
