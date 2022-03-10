package de.laser.helper

import de.laser.AccessService
import de.laser.ContextService
import de.laser.Org
import de.laser.auth.User
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.grails.taglib.GroovyPageAttributes

import javax.servlet.http.HttpServletRequest

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

    /**
     * Adds max and offset to given map
     *
     * @param result
     * @param params
     * @param user
     * @return
     */
    static Map<String, Object> setPaginationParams(Map<String, Object> result, GrailsParameterMap params, User user) {
        result.max    = params.max    ? Integer.parseInt(params.max.toString()) : user.getDefaultPageSizeAsInteger()
        result.offset = params.offset ? Integer.parseInt(params.offset.toString()) : 0

        result
    }

    static String toCamelCase(String text, boolean capitalized) {
        text = text.replaceAll( "(_)([A-Za-z0-9])", { Object[] it -> it[2].toUpperCase() } )
        return capitalized ? capitalize(text) : text
    }

    static String toSnakeCase(String text) {
        text.replaceAll( /([A-Z])/, /_$1/ ).toLowerCase().replaceAll( /^_/, '' )
    }

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

    static boolean checkAndCacheNavPerms(GroovyPageAttributes attrs, HttpServletRequest request) {

        ContextService contextService = (ContextService) Holders.grailsApplication.mainContext.getBean('contextService')
        AccessService accessService   = (AccessService) Holders.grailsApplication.mainContext.getBean('accessService')

        boolean check = false

        if (! request.getAttribute('laser_secured_nav_check')) {
            request.setAttribute('laser_secured_nav_check', [:])
        }

        // IMPORTANT: cache only for current request
        Map<String, Object> checkMap = (Map<String, Object>) request.getAttribute('laser_secured_nav_check')

        if (! checkMap.containsKey('laser_secured_nav_user')) {
            checkMap.put('laser_secured_nav_user', contextService.getUser())
        }
        if (! checkMap.containsKey('laser_secured_nav_org')) {
            checkMap.put('laser_secured_nav_org', contextService.getOrg())
        }
        User user = (User) checkMap.get('laser_secured_nav_user')
        Org org = (Org) checkMap.get('laser_secured_nav_org')

        String lsmnic = org?.id + ':' + attrs.specRole + ':' + attrs.affiliation + ':' + attrs.orgPerm + ':' + attrs.affiliationOrg

        if (checkMap.containsKey(lsmnic)) {
            check = (boolean) checkMap.get(lsmnic)
        }
        else {
            check = SpringSecurityUtils.ifAnyGranted(attrs.specRole ?: [])

            if (!check) {
                if (attrs.affiliation && attrs.orgPerm) {
                    if (user.hasAffiliation(attrs.affiliation) && accessService.checkPerm(attrs.orgPerm)) {
                        check = true
                    }
                }
                else if (attrs.affiliation && user.hasAffiliation(attrs.affiliation)) {
                    check = true
                }
                else if (attrs.orgPerm && accessService.checkPerm(attrs.orgPerm)) {
                    check = true
                }

                if (attrs.affiliation && attrs.affiliationOrg && check) {
                    check = user.hasAffiliationForForeignOrg(attrs.affiliation, attrs.affiliationOrg)
                }
            }
            checkMap.put(lsmnic, check)
        }
        //println lsmnic + ' > ' + check
        check
    }
}
