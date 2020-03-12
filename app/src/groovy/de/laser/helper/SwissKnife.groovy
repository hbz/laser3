package de.laser.helper

import com.k_int.properties.PropertyDefinition
import grails.util.Holders
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

class SwissKnife {

    static Log static_logger = LogFactory.getLog(SwissKnife) // TODO

    static List<String> getTextAndMessage(Map<String, Object> attrs) {
        def messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
        Locale locale = org.springframework.context.i18n.LocaleContextHolder.getLocale()

        List<String> result = []
        result.add(attrs.text ? attrs.text : '') // plain text

        if (attrs.message)
            if (SwissKnife.checkMessageKey(attrs.message)) {
                result.add("${messageSource.getMessage(attrs.message, attrs.args, locale)}") // translation via messages_<lang>.properties
            }
            else {
                result.add(attrs.message) // plain text; fallback
            }
        else {
            result.add('')
        }

        result
    }

    /**
     * Checking if key exists in messages_<lang>.properties
     *
     * @param key
     * @return boolean
     */
    static boolean checkMessageKey(String key) {
        if (key) {
            def messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
            Locale locale = org.springframework.context.i18n.LocaleContextHolder.getLocale()

            def keys = messageSource.getMergedProperties(locale).getProperties().keySet()

            if (keys.contains(key)) {
                return true
            }
            else {
                println("WARNING: SwissKnife.checkMessageKey() -> key '${key}' not found for locale '${locale}'")
                static_logger.warn("SwissKnife.checkMessageKey() -> key '${key}' not found for locale '${locale}'")
            }
        }
        return false
    }

    static List<PropertyDefinition> getCalculatedPropertiesForPropDefGroups(Map<String, Object> propDefStruct) {

        List<PropertyDefinition> result = []

        propDefStruct.global.each { it ->
            List<PropertyDefinition> tmp = it.getPropertyDefinitions()
            result.addAll(tmp)
        }
        propDefStruct.local.each {it ->
            List<PropertyDefinition> tmp = it[0].getPropertyDefinitions()
            result.addAll(tmp)
        }
        propDefStruct.member.each {it ->
            List<PropertyDefinition> tmp = it[0].getPropertyDefinitions()
            result.addAll(tmp)
        }

        if (propDefStruct.orphanedProperties) {
            result.addAll(propDefStruct.orphanedProperties)
        }

        //println " >>>>> " + result.unique().collect { it.id }

        result.unique()
    }

    static String toCamelCase(String text, boolean capitalized) {
        text = text.replaceAll( "(_)([A-Za-z0-9])", { Object[] it -> it[2].toUpperCase() } )
        return capitalized ? capitalize(text) : text
    }

    static String toSnakeCase(String text) {
        text.replaceAll( /([A-Z])/, /_$1/ ).toLowerCase().replaceAll( /^_/, '' )
    }
}
