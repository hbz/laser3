package de.laser.exporting

abstract class AbstractExport {

    static String FIELD_TYPE_PROPERTY           = 'property'
    static String FIELD_TYPE_REFDATA            = 'refdata'
    static String FIELD_TYPE_REFDATA_JOINTABLE  = 'refdataJoinTable'
    static String FIELD_TYPE_CUSTOM_IMPL        = 'customImplementation'

    static String CSV_VALUE_SEPARATOR   = ';'
    static String CSV_FIELD_SEPARATOR   = ','
    static String CSV_FIELD_QUOTATION   = '"'

    Map<String, Object> selectedExport = [:]

    abstract Map<String, Object> getAllFields()

    abstract Map<String, Object> getSelectedFields()

    abstract List<String> getObject(Long id, Map<String, Object> fields)
}
