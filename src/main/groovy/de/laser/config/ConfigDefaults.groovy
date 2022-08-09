package de.laser.config

import groovy.transform.CompileStatic

@CompileStatic
class ConfigDefaults {

    final static String DATASOURCE_DEFAULT              = 'dataSource'
    final static String DATASOURCE_STORAGE              = 'dataSources.storage'

    final static String SETUP_REFDATA_CATEGORY_CSV      = 'setup/RefdataCategory.csv'                       // -> BootstrapService
    final static String SETUP_REFDATA_VALUE_CSV         = 'setup/RefdataValue.csv'
    final static String SETUP_PROPERTY_DEFINITION_CSV   = 'setup/PropertyDefinition.csv'

    final static String DOCSTORE_LOCATION_FALLBACK      = System.getProperty('java.io.tmpdir') + '/laser'   // -> ConfigMapper.getDocumentStorageLocation()
    final static String DEPLOYBACKUP_LOCATION_FALLBACK  = System.getProperty('java.io.tmpdir')              // -> CustomMigrationCallbacks.onStartMigration()

    final static String DEVTOOLS_TRIGGER_FILE           = './grails-app/conf/spring/restart.trigger'        // -> AppUtils.isRestartedByDevtools()
}