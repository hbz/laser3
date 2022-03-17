package de.laser.helper

import grails.util.Environment

class ServerUtils {

    static final SERVER_LOCAL = 'SERVER_LOCAL'
    static final SERVER_DEV   = 'SERVER_DEV'
    static final SERVER_QA    = 'SERVER_QA'
    static final SERVER_PROD  = 'SERVER_PROD'

    static String getCurrentServer() {
        // laserSystemId mapping for runtime check; do not delete

        if (! Environment.isDevelopmentMode()) {

            switch (ConfigUtils.getLaserSystemId()) {
                case 'LAS:eR-Dev':
                case 'LAS:eR-Dev @ Grails3': // TODO - remove
                case 'LAS:eR-Dev @ Grails4': // TODO - remove
                    return SERVER_DEV
                    break
                case 'LAS:eR-QA/Stage':
                case 'LAS:eR-QA/Stage @ Grails3': // TODO - remove
                case 'LAS:eR-QA/Stage @ Grails4': // TODO - remove
                    return SERVER_QA
                    break
                case 'LAS:eR-Productive':
                    return SERVER_PROD
                    break
            }
        }

        return SERVER_LOCAL
    }
}
