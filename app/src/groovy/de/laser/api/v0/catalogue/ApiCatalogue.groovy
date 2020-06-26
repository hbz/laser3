package de.laser.api.v0.catalogue

import com.k_int.kbplus.Org
import com.k_int.kbplus.RefdataCategory
import com.k_int.properties.PropertyDefinition
import de.laser.CacheService
import de.laser.api.v0.*
import de.laser.helper.EhcacheWrapper
import grails.converters.JSON
import groovy.util.logging.Log4j

@Log4j
class ApiCatalogue {

    /**
     * @return JSON
     */
    static JSON getAllProperties(Org context) {
        Collection<Object> result = ApiCatalogue.getPropertyCollection(context)

        return (result ? new JSON(result) : null)
    }

    /**
     * @return JSON
     */
    static JSON getAllRefdatas() {
        Collection<Object> result = ApiCatalogue.getRefdataCollection()

        return (result ? new JSON(result) : null)
    }

    /**
     * @return []
     */
    static getDummy() {
        def result = ['dummy']
        result
    }

    // ################### MIXED CATALOGUE ###################

    /**
     * @return
     */
    static Collection<Object> getPropertyCollection(Org context){
        def result = []

        def validLabel = { lb ->
            return (lb != 'null' && lb != 'null °') ? lb : null
        }

        PropertyDefinition.where { tenant == null || tenant == context }.sort('name').each { pd ->
            def pdTmp = [:]

            pdTmp.token = pd.name
            pdTmp.scope = pd.descr
            pdTmp.type = PropertyDefinition.validTypes2[pd.type]['en']

            pdTmp.label_de = validLabel(pd.getI10n('name', 'de'))
            pdTmp.label_en = validLabel(pd.getI10n('name', 'en'))

            pdTmp.explanation_de = validLabel(pd.getI10n('expl', 'de'))
            pdTmp.explanation_en = validLabel(pd.getI10n('expl', 'en'))

            pdTmp.usedForLogic = pd.isUsedForLogic ? 'Yes' : 'No'
            pdTmp.multiple = pd.multipleOccurrence ? 'Yes' : 'No'

            pdTmp.isPublic = pd.tenant ? 'No' : 'Yes'
            pdTmp.refdataCategory = pd.refdataCategory

            result << ApiToolkit.cleanUp(pdTmp, true, true)
        }

        result
    }


    // ################### PUBLIC CATALOGUE ###################

    /**
     * @return
     */
    static Collection<Object> getRefdataCollection(){
        CacheService cacheService = grails.util.Holders.applicationContext.getBean('cacheService') as CacheService

        EhcacheWrapper cache = cacheService.getTTL1800Cache('ApiReader/exportRefdatas')
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

                rdcTmp.token = rdc.desc
                rdcTmp.label_de = validLabel(rdc.getI10n('desc', 'de'))
                rdcTmp.label_en = validLabel(rdc.getI10n('desc', 'en'))
                rdcTmp.entries = []

                RefdataCategory.getAllRefdataValues(rdc.desc).each { rdv ->
                    def tmpRdv = [:]

                    tmpRdv.token = rdv.value
                    tmpRdv.label_de = validLabel(rdv.getI10n('value', 'de'))
                    tmpRdv.label_en = validLabel(rdv.getI10n('value', 'en'))
                    tmpRdv.explanation_de = validLabel(rdv.getI10n('expl', 'de'))
                    tmpRdv.explanation_en = validLabel(rdv.getI10n('expl', 'en'))

                    rdcTmp.entries << ApiToolkit.cleanUp(tmpRdv, true, true)
                }
                result << ApiToolkit.cleanUp(rdcTmp, true, true)
            }
            cache.put('refdatas', result)
        }

        result
    }

}
