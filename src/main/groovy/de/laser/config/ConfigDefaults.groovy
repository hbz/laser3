package de.laser.config

import groovy.transform.CompileStatic

/**
 * Contains default configuration settings. May be overridden in the local configuration file
 */
@CompileStatic
class ConfigDefaults {

    public static final String DATASOURCE_DEFAULT              = 'dataSource'
    public static final String DATASOURCE_STORAGE              = 'dataSources.storage'

    public static final String SETUP_COST_INFORMATION_DEFINITION_CSV   = 'setup/CostInformationDefinition.csv'
    public static final String SETUP_REFDATA_CATEGORY_CSV      = 'setup/RefdataCategory.csv'                       // -> BootstrapService
    public static final String SETUP_REFDATA_VALUE_CSV         = 'setup/RefdataValue.csv'
    public static final String SETUP_PROPERTY_DEFINITION_CSV   = 'setup/PropertyDefinition.csv'
    public static final String SETUP_IDENTIFIER_NAMESPACE_CSV  = 'setup/IdentifierNamespace.csv'

    public static final String DOCSTORE_LOCATION_FALLBACK      = System.getProperty('java.io.tmpdir') + '/laser'   // -> ConfigMapper.getDocumentStorageLocation()
    public static final String DEPLOYBACKUP_LOCATION_FALLBACK  = System.getProperty('java.io.tmpdir')              // -> CustomMigrationCallbacks.onStartMigration()

}