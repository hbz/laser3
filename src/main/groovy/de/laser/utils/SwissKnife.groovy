package de.laser.utils

import de.laser.auth.User
import de.laser.storage.BeanStore
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.util.logging.Slf4j
import org.springframework.context.MessageSource

/**
 * The "Swiss knife", containing various helper methods for quick reuse
 * which have been copy-pasted all over the code
 */
@Slf4j
class SwissKnife {

    static final Map <String, String> NUMBER_AS_STRING = [
            '0' : 'zero',
            '1' : 'one',
            '2' : 'two',
            '3' : 'three',
            '4' : 'four',
            '5' : 'five',
            '6' : 'six',
            '7' : 'seven',
            '8' : 'eight',
            '9' : 'nine',
            '10' : 'ten',
            '11' : 'eleven',
            '12' : 'twelve',
            '13' : 'thirteen',
            '14' : 'fourteen'
    ]

    /**
     * Retrieves the message token and translation for the given label
     * @param attrs the entire HTML attribute map
     * @return a {@link List} of matching translation strings
     */
    static List<String> getTextAndMessage(Map<String, Object> attrs) {
        MessageSource messageSource = BeanStore.getMessageSource()
        Locale locale = LocaleUtils.getCurrentLocale()

        List<String> result = []
        result.add(attrs.text ? attrs.text as String : '') // plain text

        if (attrs.message)
            if (checkMessageKey(attrs.message)) {
                result.add("${messageSource.getMessage(attrs.message, attrs.args, locale)}") // translation via messages_<lang>.properties
            }
            else {
                result.add(attrs.message as String) // plain text; fallback
            }
        else {
            result.add('')
        }

        result
    }

    /**
     * Checking if key exists in messages_<lang>.properties
     * @param key
     * @return true if it exists, false otherwise
     */
    static boolean checkMessageKey(String key) {
        if (key) {
            MessageSource messageSource = BeanStore.getMessageSource()
            Locale locale = LocaleUtils.getCurrentLocale()

            def keys = messageSource.getMergedProperties(locale).getProperties().keySet()

            if (keys.contains(key)) {
                return true
            }
            else {
                log.warn("checkMessageKey() -> key '${key}' not found for locale '${locale}'")
            }
        }
        return false
    }

    /**
     * Adds max and offset to given map – sets pagination parameters for list views
     * @param result the result map, rendered in the view, containing the results
     * @param params the request parameter map
     * @param user the current {@link User}
     * @return the result map filled with max and offset
     */
    static Map<String, Object> setPaginationParams(Map<String, Object> result, GrailsParameterMap params, User user) {
        result.max    = params.max    ? Integer.parseInt(params.max.toString()) : user.getPageSizeOrDefault()
        result.offset = params.offset ? Integer.parseInt(params.offset.toString()) : 0

        result
    }

    /**
     * Converts the given string with underscores into camel case
     * @param text the input text
     * @param capitalized should the text be capitalised?
     * @return the converted text
     */
    static String toCamelCase(String text, boolean capitalized) {
        text = text.replaceAll( "(_)([A-Za-z0-9])", { Object[] it -> it[2].toUpperCase() } )
        return capitalized ? capitalize(text) : text
    }

    /**
     * Converts the given text into snake case, i.e. explodes it with underscores (and removes initial underscores)
     * @param text the input text
     * @return the converted text
     */
    static String toSnakeCase(String text) {
        text.replaceAll( /([A-Z])/, /_$1/ ).toLowerCase().replaceAll( /^_/, '' )
    }

    /**
     * Performs a deep clone of the given input map, i.e. all leaves are being copied as well. It works depth-first
     * @param map the {@link Map} to clone
     * @return the map clone
     */
    static Map deepClone(Map map) {
        Map cloned = [:]
        map.each { k,v ->
            if (v instanceof Map) {
                cloned[k] = deepClone(v)
            }
            else {
                cloned[k] = v
            }
        }
        return cloned
    }
}
