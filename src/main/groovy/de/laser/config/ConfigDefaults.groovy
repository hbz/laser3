package de.laser.config

import groovy.util.logging.Slf4j

@Slf4j
class ConfigDefaults {

    final static String DEVTOOLS_TRIGGER_FILE = './grails-app/conf/spring/restart.trigger' // -> AppUtils.isRestartedByDevtools()

    final static String DOCSTORE_LOCATION_FALLBACK = '/tmp/laser' // -> ConfigMapper.getDocumentStorageLocation()
}