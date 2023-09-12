package de.laser.config

import groovy.transform.CompileStatic

/**
 * Contains default configuration settings.
 * They may be overridden in the local config file laser3_config (located at {user dir}/.grails) or /opt/laser/.grails
 */
@CompileStatic
class ConfigDefaults {

    public static final String DATASOURCE_DEFAULT              = 'dataSource'
    public static final String DATASOURCE_STORAGE              = 'dataSources.storage'

    public static final String SETUP_REFDATA_CATEGORY_CSV      = 'setup/RefdataCategory.csv'                       // -> BootstrapService
    public static final String SETUP_REFDATA_VALUE_CSV         = 'setup/RefdataValue.csv'
    public static final String SETUP_PROPERTY_DEFINITION_CSV   = 'setup/PropertyDefinition.csv'

    public static final String DOCSTORE_LOCATION_FALLBACK      = System.getProperty('java.io.tmpdir') + '/laser'   // -> ConfigMapper.getDocumentStorageLocation()
    public static final String DEPLOYBACKUP_LOCATION_FALLBACK  = System.getProperty('java.io.tmpdir')              // -> CustomMigrationCallbacks.onStartMigration()

}