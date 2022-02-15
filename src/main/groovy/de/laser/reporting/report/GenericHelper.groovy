package de.laser.reporting.report

import de.laser.annotations.RefdataAnnotation
import de.laser.base.AbstractBase
import de.laser.reporting.export.base.BaseDetailsExport
import de.laser.reporting.report.myInstitution.base.BaseConfig
import grails.util.Holders
import org.springframework.context.i18n.LocaleContextHolder

import java.lang.reflect.Field

class GenericHelper {

    // TODO
    static boolean isFieldMultiple(String object, String fieldName) {
        boolean bool = false

        if (object in [ 'package', null ] && fieldName in [ 'provider', 'platform' ]) {
            bool = true
        }
        else if (object in [ 'platform' ] && fieldName in [ 'org' ]) {
            bool = true
        }
        else if (fieldName in [ 'annual' ]) {
            bool = true
        }
        // if (bool) { println 'isFieldMultiple() ' + object + ' / ' + fieldName }
        bool
    }

    static boolean isFieldVirtual(String object, String fieldName) {
        boolean bool = false

        if (fieldName in [ 'region' ]) {
            bool = true
        }
        // if (bool) { println 'isFieldVirtual() ' + object + ' / ' + fieldName }
        bool
    }

    static String getFieldType(Map<String, Object> objConfig, String fieldName) {
        def tmp = objConfig.fields.get(fieldName)
        if (tmp) {
            tmp[0]
        }
        else {
            println 'GenericHelper.getFieldType() ' + fieldName + ' not found'
            null
        }
    }

    static String getFieldLabel(Map<String, Object> objConfig, String fieldName) {

        String label = '?'
        String type = getFieldType(objConfig, fieldName)

        Object messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
        Locale locale = LocaleContextHolder.getLocale()

        if (type in [BaseConfig.FIELD_TYPE_PROPERTY, BaseDetailsExport.FIELD_TYPE_PROPERTY] ) {
            // LaserReportingTagLib:reportFilterProperty

            Field prop = (fieldName == 'globalUID') ? AbstractBase.getDeclaredField(fieldName) : objConfig.meta.class.getDeclaredField(fieldName)
            String csn = objConfig.meta.class.simpleName.uncapitalize() // TODO -> check

//            try {
            label = messageSource.getMessage(csn + '.' + prop.getName() + '.label', null, locale)
//            } catch(Exception e) {
//                println " -----------> No message found under code '${csn}.${prop.getName()}.label'"
//                label = messageSource.getMessage(csn + '.' + prop.getName(), null, locale)
//            }
        }
        else if (type in [BaseConfig.FIELD_TYPE_REFDATA, BaseDetailsExport.FIELD_TYPE_REFDATA] ) {
            // LaserReportingTagLib:reportFilterRefdata

            Field refdata   = objConfig.meta.class.getDeclaredField(fieldName)
            def anno        = refdata.getAnnotationsByType(RefdataAnnotation).head()
            String rdCat    = anno.cat()
            String rdI18n   = anno.i18n()

            label = rdI18n != 'n/a' ? messageSource.getMessage(rdI18n, null, locale) : messageSource.getMessage(rdCat + '.label', null, locale) // TODO -> @RefdataAnnotation
        }
        else if (type in [BaseConfig.FIELD_TYPE_REFDATA_JOINTABLE, BaseDetailsExport.FIELD_TYPE_REFDATA_JOINTABLE] ) {
            // LaserReportingTagLib:reportFilterRefdataRelTable

            Map<String, Object> customRdv = BaseConfig.getCustomImplRefdata(fieldName)
            label = customRdv.get('label')
        }
        else if (type in [BaseConfig.FIELD_TYPE_CUSTOM_IMPL, BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL] ) {
            // LaserReportingTagLib:reportFilterRefdataRelTable

            Map<String, Object> rdv = BaseConfig.getCustomImplRefdata(fieldName)
            if (!rdv) {
                println '>> ' + fieldName + ' : ' + type + ' not found!'
            }
            label = rdv.get('label')
        }
        else if (type in [BaseConfig.FIELD_TYPE_ELASTICSEARCH, BaseDetailsExport.FIELD_TYPE_ELASTICSEARCH] ) {
            // LaserReportingTagLib:reportFilterRefdataRelTable

            Map<String, Object> rdv = BaseConfig.getElasticSearchRefdata(fieldName)
            if (!rdv) {
                println '>> ' + fieldName + ' : ' + type + ' not found!'
            }
            label = rdv.get('label')
        }
        else if (type in [BaseDetailsExport.FIELD_TYPE_COMBINATION, null] ) { // TODO: null
            // LaserReportingTagLib:reportFilterRefdataRelTable

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
}
