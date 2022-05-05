package de.laser.api.v0.catalogue

import de.laser.Org
import de.laser.RefdataCategory
import de.laser.storage.BeanStore
import de.laser.properties.PropertyDefinition
import de.laser.CacheService
import de.laser.api.v0.*
import de.laser.helper.EhcacheWrapper
import grails.converters.JSON
import groovy.util.logging.Slf4j

/**
 * This class retrieves controlled lists with their values
 */
@Slf4j
class ApiCatalogue {

    /**
     * Retrieves all currently existing property definitions with their attributes
     * @return a {@link JSON} containing the properties
     * @see #getPropertyCollection(de.laser.Org)
     */
    static JSON getAllProperties(Org context) {
        Collection<Object> result = ApiCatalogue.getPropertyCollection(context)

        return (result ? new JSON(result) : null)
    }

    /**
     * Retrieves all currently existing reference data values with their attributes
     * @return a {@link JSON} containing the reference data values
     */
    static JSON getAllRefdatas() {
        Collection<Object> result = ApiCatalogue.getRefdataCollection()

        return (result ? new JSON(result) : null)
    }

    /**
     * Dummy call
     * @return ['dummy']
     */
    static getDummy() {
        def result = ['dummy']
        result
    }

    // ################### MIXED CATALOGUE ###################

    /**
     * Retrieves all property definitions; public ones and those created by the given institution are being returned
     * @return a {@link List} of property definitions with their attributes
     * @see PropertyDefinition
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
            pdTmp.type = PropertyDefinition.validTypes[pd.type]['en']

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
     * Retrieves all currently defined reference data values (i.e. controlled lists and their values)
     * @return a {@link List} of reference data values with their attributes
     * @see de.laser.RefdataValue
     */
    static Collection<Object> getRefdataCollection(){
        CacheService cacheService = BeanStore.getCacheService()

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
