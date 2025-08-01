package de.laser.reporting.report

import de.laser.annotations.RefdataInfo
import de.laser.base.AbstractBase
import de.laser.storage.BeanStore
import de.laser.reporting.export.base.BaseDetailsExport
import de.laser.reporting.report.myInstitution.base.BaseConfig
import de.laser.utils.LocaleUtils
import groovy.util.logging.Slf4j
import org.springframework.context.MessageSource

import java.lang.reflect.Field

/**
 * Helper class containing frequently used methods
 */
@Slf4j
class GenericHelper {

    /**
     * Returns the field matching the given field name from the given configuration map
     * @param objConfig the object configuration map containing the fields
     * @param fieldName the name key to retrieve
     * @return the value defined for the given field in the object configuration map
     */
    static Map<String, Object> getField(Map<String, Object> objConfig, String fieldName) {
        Map<String, Object> field = objConfig.fields.get(fieldName)
        if (field) {
            field
        }
        else {
            log.warn 'getField() ' + fieldName + ' for ' + objConfig.meta + ' not found'
            null
        }
    }

    /**
     * Checks if the given field in the configuration map may be distributed multiple times
     * @param cfg the object configuration map
     * @param fieldName the field name to check
     * @return true if the field can be assigned multiple times, false otherwise
     */
    static boolean isFieldMultiple(Map<String, Object> cfg, String fieldName) {
        Map field = getField(cfg, fieldName) ?: [:]
        field.spec == BaseConfig.FIELD_IS_MULTIPLE
    }

    /**
     * Checks if the given object is a {@link Collection}
     * @param obj the object to check
     * @return true if the object is an instance of {@link Collection}, false otherwise
     */
    static boolean isCollection(def obj) {
        obj instanceof Collection
    }

    /**
     * Checks if the given field in the configuration map is virtual, i.e. influenced by other fields or not
     * @param cfg the object configuration map
     * @param fieldName the field name to check
     * @return true if the field is virtual, false otherwise
     */
    static boolean isFieldVirtual(Map<String, Object> cfg, String fieldName) {
        Map field = getField(cfg, fieldName) ?: [:]
        field.spec == BaseConfig.FIELD_IS_VIRTUAL
    }

    /**
     * Returns the type of the given field name
     * @param cfg the object configuration map
     * @param fieldName the field name
     * @return the field type, see {@link BaseConfig} and {@link BaseDetailsExport} for the possible types
     */
    static String getFieldType(Map<String, Object> objConfig, String fieldName) {
        // println '- GenericHelper.getFieldType() : ' + fieldName
        getField(objConfig, fieldName)?.type
    }

    /**
     * Retrieves the label to the given field name
     * @param objConfig the object configuration map
     * @param fieldName the field name to which the label should be retrieved
     * @return the associated label
     */
    static String getFieldLabel(Map<String, Object> objConfig, String fieldName) {
        // println '- GenericHelper.getFieldLabel() : ' + fieldName
        String label = '?' // -> BaseDetails.getFieldLabelforColumn() fallback
        Map<String, Object> field = getField(objConfig, fieldName)
        String type = field?.type

        MessageSource messageSource = BeanStore.getMessageSource()
        Locale locale = LocaleUtils.getCurrentLocale()
//
//        if (field?.label) {
//            println 'GenericHelper.getFieldLabel() overrides ' + field.label
//            label = messageSource.getMessage(field.label as String, null, locale)
//        }
        if (type in [BaseConfig.FIELD_TYPE_PROPERTY, BaseDetailsExport.FIELD_TYPE_PROPERTY] ) {
            // ReportingTagLib:filterProperty

            Field prop = (fieldName == 'laserID') ? AbstractBase.getDeclaredField(fieldName) : objConfig.meta.class.getDeclaredField(fieldName)
            String csn = objConfig.meta.class.simpleName.uncapitalize() // TODO -> check

            label = messageSource.getMessage(csn + '.' + prop.getName() + '.label', null, locale)
        }
        else if (type in [BaseConfig.FIELD_TYPE_REFDATA, BaseDetailsExport.FIELD_TYPE_REFDATA] ) {
            // ReportingTagLib:filterRefdata

            Field refdata   = objConfig.meta.class.getDeclaredField(fieldName)
            def anno        = refdata.getAnnotationsByType(RefdataInfo).head()
            String rdCat    = anno.cat()
            String rdI18n   = anno.i18n()

            label = rdI18n != 'n/a' ? messageSource.getMessage(rdI18n, null, locale) : messageSource.getMessage(rdCat + '.label', null, locale) // TODO -> @RefdataInfo
        }
        else if (type in [BaseConfig.FIELD_TYPE_REFDATA_JOINTABLE, BaseDetailsExport.FIELD_TYPE_REFDATA_JOINTABLE] ) {
            // ReportingTagLib:filterRefdataRelTable

            //println 'GenericHelper.getFieldLabel() BaseConfig.FIELD_TYPE_REFDATA_JOINTABLE, BaseDetailsExport.FIELD_TYPE_REFDATA_JOINTABLE ---> BaseConfig.getCustomImplRefdata()'

            Map<String, Object> customRdv = BaseConfig.getCustomImplRefdata(field.customImpl ?: fieldName)
            label = customRdv.get('label')
        }
        else if (type in [BaseConfig.FIELD_TYPE_CUSTOM_IMPL, BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL] ) {
            // ReportingTagLib:filterRefdataRelTable

            // println 'GenericHelper.getFieldLabel() BaseConfig.FIELD_TYPE_CUSTOM_IMPL ---> BaseConfig.getCustomImplRefdata( ' + fieldName + ') ' + field
            // println objConfig

            Map<String, Object> rdv = BaseConfig.getCustomImplRefdata(field.customImpl ?: fieldName)
            if (!rdv) {
                log.warn '>> ' + fieldName + ' : ' + type + ' not found'
            }
            label = rdv.get('label')
        }
        else if (type in [BaseConfig.FIELD_TYPE_ELASTICSEARCH, BaseDetailsExport.FIELD_TYPE_ELASTICSEARCH] ) {
            // ReportingTagLib:filterRefdataRelTable

            Map<String, Object> rdv = BaseConfig.getElasticSearchRefdata(fieldName)
            if (!rdv) {
                log.warn '>> ' + fieldName + ' : ' + type + ' not found'
            }
            label = rdv.get('label')
        }
        else if (type in [BaseDetailsExport.FIELD_TYPE_COMBINATION, null] ) { // TODO: null
            // ReportingTagLib:filterRefdataRelTable
            String base = ''

            if (fieldName.endsWith('+sortname+name')) {
                label = messageSource.getMessage('default.sortname.label', null, locale) + ', ' + messageSource.getMessage('default.name.label', null, locale)
            }
            else if (fieldName == 'sortname' || fieldName.endsWith('+sortname')) {
                label = messageSource.getMessage('default.sortname.label', null, locale)
            }
            else if (fieldName.endsWith('+abbreviatedName+name')) {
                label = messageSource.getMessage('default.abbreviatedName.label', null, locale) + ', ' + messageSource.getMessage('default.name.label', null, locale)
            }
            else if (fieldName == 'abbreviatedName' || fieldName.endsWith('+abbreviatedName')) {
                label = messageSource.getMessage('default.abbreviatedName.label', null, locale)
            }
            else if (fieldName == 'name' || fieldName.endsWith('+name')) {
                label = messageSource.getMessage('default.name.label', null, locale)
            }

            //
            if (fieldName.startsWith('provider+')) { // 'platform/provider+abbreviatedName+name' // todo
                base = messageSource.getMessage('platform.provider', null, locale)
            }
            //
            else if (fieldName.startsWith('x-platform+')) {
                base = messageSource.getMessage('platform.label', null, locale)

                if (fieldName == 'x-platform+name+primaryUrl') {
                    label = messageSource.getMessage('default.name.label', null, locale) + ', ' + messageSource.getMessage('platform.primaryUrl.label', null, locale)
                }
                else if (fieldName == 'x-platform+primaryUrl') {
                    label = messageSource.getMessage('platform.primaryUrl.label', null, locale)
                }
            }
            //
            else if (fieldName.startsWith('x-provider+')) {
                base = messageSource.getMessage('provider.label', null, locale)
            }
            //
            else if (fieldName.startsWith('x-vendor+')) {
                base = messageSource.getMessage('vendor', null, locale)
            }

            if (base && label) {
                label = base + ' (' + label + ')'
            }
        }
        label
    }

    /**
     * Flags the given value as fuzzy-matched
     * @param value the value to flag
     * @return a concatenated string ( value *), marking the value as unmatched
     */
    static String flagUnmatched(String value) {
        '(' + value + ' *)'
    }

    /**
     * Returns from the given filter result the requested database ID list
     * @param filterResult the filter result map
     * @param key the ID list to be retrieved
     * @return a list of database IDs or an empty list if the list does not exist in the filter result map
     */
    static List<Long> getFilterResultDataIdList(Map<String, Object> filterResult, String key) {
        filterResult.data.getAt( key + 'IdList') as List ?: []
    }
}
