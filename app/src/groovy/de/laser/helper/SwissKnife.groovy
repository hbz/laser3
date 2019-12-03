package de.laser.helper

import grails.util.Holders
import groovy.util.logging.Log4j
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

class SwissKnife {

    static Log static_logger = LogFactory.getLog(SwissKnife) // TODO

    static List<String> getTextAndMessage(Map<String, Object> attrs) {
        def messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
        Locale locale = org.springframework.context.i18n.LocaleContextHolder.getLocale()

        List<String> result = []
        result.add(attrs.text ? attrs.text : '')

        if (attrs.message)
            if (SwissKnife.checkMessageKey(attrs.message)) {
                result.add("${messageSource.getMessage(attrs.message, attrs.args, locale)}")
            }
            else {
                result.add(attrs.message)
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
                println("SwissKnife.checkMessageKey() -> key '${key}' not found for locale '${locale}'")
                static_logger.warn("SwissKnife.checkMessageKey() -> key '${key}' not found for locale '${locale}'")
            }
        }
        return false
    }
}
