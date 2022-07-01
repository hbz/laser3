package de.laser.config

class ConfigDefaults {

    final static String SETUP_REFDATA_CATEGORY_CSV      = 'setup/RefdataCategory.csv'       // -> BootstrapService
    final static String SETUP_REFDATA_VALUE_CSV         = 'setup/RefdataValue.csv'
    final static String SETUP_PROPERTY_DEFINITION_CSV   = 'setup/PropertyDefinition.csv'

    final static String DOCSTORE_LOCATION_FALLBACK      = '/tmp/laser'                     // -> ConfigMapper.getDocumentStorageLocation()

    final static String DEVTOOLS_TRIGGER_FILE           = './grails-app/conf/spring/restart.trigger'  // -> AppUtils.isRestartedByDevtools()

}