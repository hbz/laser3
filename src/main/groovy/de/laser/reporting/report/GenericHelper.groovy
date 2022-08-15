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

@Slf4j
class GenericHelper {

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

    static boolean isFieldMultiple(Map<String, Object> cfg, String fieldName) {
        Map field = getField(cfg, fieldName) ?: [:]
        field.spec == BaseConfig.FIELD_IS_MULTIPLE
    }

    static boolean isCollection(def obj) {
        obj instanceof Collection
    }

    static boolean isFieldVirtual(Map<String, Object> cfg, String fieldName) {
        Map field = getField(cfg, fieldName) ?: [:]
        field.spec == BaseConfig.FIELD_IS_VIRTUAL
    }

    static String getFieldType(Map<String, Object> objConfig, String fieldName) {
        // println '- GenericHelper.getFieldType() : ' + fieldName
        getField(objConfig, fieldName)?.type
    }

    static String getFieldLabel(Map<String, Object> objConfig, String fieldName) {
        // println '- GenericHelper.getFieldLabel() : ' + fieldName
        String label = '?'
        Map<String, Object> field = getField(objConfig, fieldName)
        String type = field?.type

        MessageSource messageSource = BeanStore.getMessageSource()
        Locale locale = LocaleUtils.getCurrentLocale()

        if (type in [BaseConfig.FIELD_TYPE_PROPERTY, BaseDetailsExport.FIELD_TYPE_PROPERTY] ) {
            // ReportingTagLib:filterProperty

            Field prop = (fieldName == 'globalUID') ? AbstractBase.getDeclaredField(fieldName) : objConfig.meta.class.getDeclaredField(fieldName)
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

            Map<String, Object> customRdv = BaseConfig.getCustomImplRefdata(field.customImplRdv ?: fieldName)
            label = customRdv.get('label')
        }
        else if (type in [BaseConfig.FIELD_TYPE_CUSTOM_IMPL, BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL] ) {
            // ReportingTagLib:filterRefdataRelTable

            // println 'GenericHelper.getFieldLabel() BaseConfig.FIELD_TYPE_CUSTOM_IMPL ---> BaseConfig.getCustomImplRefdata( ' + fieldName + ') ' + field
            // println objConfig

            Map<String, Object> rdv = BaseConfig.getCustomImplRefdata(field.customImplRdv ?: fieldName)
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

            if (fieldName == '+sortname+name') {
                label = messageSource.getMessage('default.sortname.label', null, locale) + ', ' + messageSource.getMessage('default.name.label', null, locale)
            }
            else if (fieldName == 'sortname') {
                label = messageSource.getMessage('default.sortname.label', null, locale)
            }
            else if (fieldName == 'name') {
                label = messageSource.getMessage('default.name.label', null, locale)
            }
            // plt
            else if (fieldName.startsWith('org+')) {
                label = messageSource.getMessage('platform.provider', null, locale)

                if (fieldName == 'org+sortname+name') {
                    label = label + ' (' + messageSource.getMessage('default.sortname.label', null, locale) + ', ' + messageSource.getMessage('default.name.label', null, locale) + ')'
                }
                else if (fieldName == 'org+sortname') {
                    label = label + ' (' + messageSource.getMessage('default.sortname.label', null, locale) + ')'
                }
                else if (fieldName == 'org+name') {
                    label = label + ' (' + messageSource.getMessage('default.name.label', null, locale) + ')'
                }
            }
            //
            else if (fieldName.startsWith('x-provider+')) {
                label = messageSource.getMessage('default.provider.label', null, locale)

                if (fieldName == 'x-provider+sortname+name') {
                    label = label + ' (' + messageSource.getMessage('default.sortname.label', null, locale) + ', ' + messageSource.getMessage('default.name.label', null, locale) + ')'
                }
                else if (fieldName == 'x-provider+sortname') {
                    label = label + ' (' + messageSource.getMessage('default.sortname.label', null, locale) + ')'
                }
                else if (fieldName == 'x-provider+name') {
                    label = label + ' (' + messageSource.getMessage('default.name.label', null, locale) + ')'
                }
            }
            //
            else if (fieldName.startsWith('x-platform+')) {
                label = messageSource.getMessage('platform.label', null, locale)

                if (fieldName == 'x-platform+name+primaryUrl') {
                    label = label + ' (' + messageSource.getMessage('default.name.label', null, locale) + ', ' + messageSource.getMessage('platform.primaryUrl.label', null, locale) + ')'
                }
                else if (fieldName == 'x-platform+name') {
                    label = label + ' (' + messageSource.getMessage('default.name.label', null, locale) + ')'
                }
                else if (fieldName == 'x-platform+primaryUrl') {
                    label = label + ' (' + messageSource.getMessage('platform.primaryUrl.label', null, locale) + ')'
                }
            }
        }
        label
    }

    static String flagUnmatched(String value) {
        '(' + value + ' *)'
    }

    static List<Long> getFilterResultDataIdList(Map<String, Object> filterResult, String key) {
        filterResult.data.getAt( key + 'IdList') as List ?: []
    }
}
