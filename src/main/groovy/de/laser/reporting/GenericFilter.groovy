package de.laser.reporting

import de.laser.annotations.RefdataAnnotation
import grails.util.Holders
import org.springframework.context.i18n.LocaleContextHolder

import java.lang.reflect.Field

class GenericFilter {

    static String getFilterSourceLabel(Map<String, Object> objConfig, String key) {
        objConfig.source.get(key)
    }

    static String getFilterFieldType(Map<String, Object> objConfig, String fieldName) {

        String type = '?' // [ property, refdata ]

        objConfig.filter.each {
            if (it.keySet().contains(fieldName)) {
                type = it.get(fieldName)
            }
        }
        type
    }

    static String getFilterFieldLabel(Map<String, Object> objConfig, String fieldName) {

        String label = '?'
        String type = getFilterFieldType(objConfig, fieldName)

        Object messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
        Locale locale = LocaleContextHolder.getLocale()

        if (type == GenericConfig.FIELD_TYPE_PROPERTY) {
            // LaserReportingTagLib:reportFilterProperty

            Field prop = objConfig.meta.class.getDeclaredField(fieldName)
            String csn = objConfig.meta.class.simpleName.uncapitalize() // TODO -> check

            label = messageSource.getMessage(csn + '.' + prop.getName() + '.label', null, locale)
        }

        if (type == GenericConfig.FIELD_TYPE_REFDATA) {
            // LaserReportingTagLib:reportFilterRefdata

            Field refdata   = objConfig.meta.class.getDeclaredField(fieldName)
            def anno        = refdata.getAnnotationsByType(RefdataAnnotation).head()
            String rdCat    = anno.cat()
            String rdI18n   = anno.i18n()

            label = rdI18n != 'n/a' ? messageSource.getMessage(rdI18n, null, locale) : messageSource.getMessage(rdCat + '.label', null, locale) // TODO -> @RefdataAnnotation
        }

        if (type == GenericConfig.FIELD_TYPE_REFDATA_RELTABLE) {
            // LaserReportingTagLib:reportFilterRefdata

            Map<String, Object> customRdv = GenericConfig.getCustomRefdata(fieldName)
            label = customRdv.get('label')
        }
        if (type == GenericConfig.FIELD_TYPE_CUSTOM_IMPL) {
            // LaserReportingTagLib:reportFilterRefdata

            Map<String, Object> customRdv = GenericConfig.getCustomRefdata(fieldName)
            label = customRdv.get('label')
        }
        label
    }

    static String getDateModifier(String modifier) {

        if (modifier == 'less') {
            return '<'
        }
        else if (modifier == 'greater') {
            return '>'
        }
        else if (modifier == 'less-equal') {
            return '<='
        }
        else if (modifier == 'greater-equal') {
            return '>='
        }
        else {
            return '='
        }
    }

    static String getLegalInfoQueryWhereParts(Long key) {

        if (key == 0){
            return 'org.createdBy is null and org.legallyObligedBy is null'
        }
        else if (key == 1){
            return 'org.createdBy is not null and org.legallyObligedBy is not null'
        }
        else if (key == 2){
            return 'org.createdBy is not null and org.legallyObligedBy is null'
        }
        else if (key == 3){
            return 'org.createdBy is null and org.legallyObligedBy is not null'
        }
    }
}
