package de.laser.api.v0

import com.k_int.kbplus.*
import de.laser.CacheService
import de.laser.helper.Constants
import de.laser.helper.RDStore
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil

@Log4j
class ApiReader {

    static SUPPORTED_FORMATS = [
            'costItem':             [Constants.MIME_APPLICATION_JSON],
            'document':             [],
            'issueEntitlements':    [Constants.MIME_TEXT_PLAIN, Constants.MIME_APPLICATION_JSON],
            'license':              [Constants.MIME_APPLICATION_JSON],
            'onixpl':               [Constants.MIME_APPLICATION_XML],
            'oaMonitor':            [Constants.MIME_APPLICATION_JSON],
            'oaMonitorList':        [Constants.MIME_APPLICATION_JSON],
            'organisation':         [Constants.MIME_APPLICATION_JSON],
            'package':              [Constants.MIME_APPLICATION_JSON],
            'refdataList':          [Constants.MIME_APPLICATION_JSON],
            'statistic':            [Constants.MIME_APPLICATION_JSON],
            'statisticList':        [Constants.MIME_APPLICATION_JSON],
            'subscription':         [Constants.MIME_APPLICATION_JSON]
    ]

    static SIMPLE_QUERIES = ['oaMonitorList', 'refdataList', 'statisticList']


    // ################### CATALOGUE ###################

    /**
     * @return
     */
    static Collection<Object> retrieveRefdataCollection(){
        CacheService cacheService = grails.util.Holders.applicationContext.getBean('cacheService') as CacheService

        def cache = cacheService.getTTL1800Cache('ApiReader/exportRefdatas/')
        def result = []

        if (cache.get('refdatas')) {
            result = cache.get('refdatas')
            log.debug('refdatas from cache')
        }
        else {
            def validLabel = { lb ->
                return (lb != 'null' && lb != 'null °') ? lb : null
            }

            RefdataCategory.where {}.sort('desc').each { rdc ->
                def rdcTmp = [:]

                rdcTmp.desc = rdc.desc
                rdcTmp.label_de = validLabel(rdc.getI10n('desc', 'de'))
                rdcTmp.label_en = validLabel(rdc.getI10n('desc', 'en'))
                rdcTmp.entries = []

                RefdataCategory.getAllRefdataValues(rdc.desc).each { rdv ->
                    def tmpRdv = [:]

                    tmpRdv.value = rdv.value
                    tmpRdv.label_de = validLabel(rdv.getI10n('value', 'de'))
                    tmpRdv.label_en = validLabel(rdv.getI10n('value', 'en'))

                    rdcTmp.entries << ApiToolkit.cleanUp(tmpRdv, true, true)
                }
                result << ApiToolkit.cleanUp(rdcTmp, true, true)
            }
            cache.put('refdatas', result)
        }
        result
    }
}
