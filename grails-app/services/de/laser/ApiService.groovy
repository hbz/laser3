package de.laser

import de.laser.storage.RDConstants
import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import groovy.util.slurpersupport.GPathResult

@Deprecated
@Slf4j
@Transactional
class ApiService {

    /**
     * Strips whitespaces from the given string
     * @param s the string to sanitise
     * @return trimmed string with multiple whitespaces removed
     */
    private String normString(String str){
        if(! str)
            return ""
            
        str = str.replaceAll("\\s+", " ").trim()
        str
    }

    /**
     * Reduces the child level and flattens the given object with children to a single level
     * @param obj the object to flatten
     * @return list with children of given object or object if no children exist
     */
    private List flattenToken(Object obj){
        List result = []

        obj?.token?.each{ token ->
            result << token
        }
        if(result.size() == 0 && obj){
            result << obj
        }
        
        result
    }

    @Deprecated
    GPathResult makeshiftSubscriptionImport(GPathResult xml){

        // TODO: in progress - erms-746
        def count = xml.institution.size()
        log.debug("importing ${count} items")

        xml.subscription.each { sub ->
            String strName = sub.name.text()
            RefdataValue rdvType = RefdataValue.getByValueAndCategory(type.name.text(), RDConstants.SUBSCRIPTION_TYPE)

            log.debug("processing ${strName} / ${rdvType.getI10n('value')}")
        }
    }
}
