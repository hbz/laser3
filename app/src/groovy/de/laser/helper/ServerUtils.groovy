package de.laser.helper

import de.laser.ContextService
import grails.util.Holders

class ServerUtils {

    static final SERVER_LOCAL = 'SERVER_LOCAL'
    static final SERVER_DEV   = 'SERVER_DEV'
    static final SERVER_QA    = 'SERVER_QA'
    static final SERVER_PROD  = 'SERVER_PROD'

    static String getCurrentServer() {
        // laserSystemId mapping for runtime check; do not delete

        switch (Holders.grailsApplication.config.laserSystemId) {
            case 'LAS:eR-Dev':
                return SERVER_DEV
                break
            case 'LAS:eR-QA/Stage':
                return SERVER_QA
                break
            case 'LAS:eR-Productive':
                return SERVER_PROD
                break
            default:
                return SERVER_LOCAL
                break
        }
    }
}
