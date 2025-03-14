package de.laser.helper

import de.laser.annotations.UnstableFeature
import de.laser.storage.RDStore
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.util.logging.Slf4j

/**
 * This helper class contains generic methods for filter logic and resolving
 */
@UnstableFeature
@Slf4j
class FilterLogic {

    /**
     * Resolves which status should be considered for the requested title tab menu
     * @param params the request parameter map
     * @param entites the type of title – one of IEs or Tipps
     * @param ignorePlannedIEs should planned issue entitlements be ignored?
     * @return a {@link Map} containing the tab and status to be rendered
     */
    static Map<String, Object> resolveTabAndStatusForTitleTabsMenu(GrailsParameterMap params, String entites, boolean ignorePlannedIEs = false) {
        log.debug('resolveTabAndStatusForTitleTabsMenu( .., ' + entites + ', ' + ignorePlannedIEs + ' )')

        String debug = '[tab: ' + params.tab + ', status: ' + params.list('status') + ']'
        Map<String, Object> result = [:]

        // MyInstitutionController.currentTitles()              entites = 'IEs',   ignorePlannedIEs = false
        // MyInstitutionController.currentPermanentTitles()     entites = 'IEs',   ignorePlannedIEs = true
        // SubscriptionControllerService.index()                entites = 'IEs',   ignorePlannedIEs = false
        // TitleController.list()                               entites = 'Tipps', ignorePlannedIEs = false

        if (params.tab) {
            switch (params.tab) {
                case 'current' + entites:
                    result.status = [RDStore.TIPP_STATUS_CURRENT.id]
                    break
                case 'planned' + entites:
                    if (!ignorePlannedIEs) {
                        result.status = [RDStore.TIPP_STATUS_EXPECTED.id]
                    }
                    break
                case 'expired' + entites:
                    result.status = [RDStore.TIPP_STATUS_RETIRED.id]
                    break
                case 'deleted' + entites:
                    result.status = [RDStore.TIPP_STATUS_DELETED.id]
                    break
                case 'all' + entites:
                    result.status = [RDStore.TIPP_STATUS_CURRENT.id, RDStore.TIPP_STATUS_EXPECTED.id, RDStore.TIPP_STATUS_RETIRED.id, RDStore.TIPP_STATUS_DELETED.id]
                    break
            }
        }
        else if(params.list('status').size() == 1) {
            Long statusId = Long.valueOf( params.list('status')[0] ) // String or Long

            switch (statusId) {
                case RDStore.TIPP_STATUS_CURRENT.id:
                    result.tab = 'current' + entites
                    break
                case RDStore.TIPP_STATUS_RETIRED.id:
                    result.tab = 'expired' + entites
                    break
                case RDStore.TIPP_STATUS_EXPECTED.id:
                    if (!ignorePlannedIEs) {
                        result.tab = 'planned' + entites
                    }
                    break
                case RDStore.TIPP_STATUS_DELETED.id:
                    result.tab = 'deleted' + entites
                    break
            }
        }
        else {
            if (params.list('status').size() > 1) {
                result.tab = 'all' + entites
            }
            else {
                result.tab = 'current' + entites
                result.status = [RDStore.TIPP_STATUS_CURRENT.id]
            }
        }

        println '! ' + debug + ' -> [tab: ' + params.tab + ', status: ' + params.list('status') + ']'

        result
    }

    static Map<String, Object> resolveTabAndStatusForRenewalTabsMenu(GrailsParameterMap params) {
        log.debug('resolveTabAndStatusForRenewalTabsMenu( .. )')

        String debug = '[tab: ' + params.tab + ', subTab: ' + params.subTab + ', status: ' + params.list('status') + ']'
        Map<String, Object> result = [:]

        if (params.tab) {
            switch(params.tab) {
                case ['allTipps', 'usage']: result.status = [RDStore.TIPP_STATUS_CURRENT]
                    break
                case 'selectedIEs': Map stResult = resolveTabAndStatusForTitleTabsMenu(params, 'IEs')
                    result.subTab = stResult.tab
                    result.status = stResult.status
                    break
                case 'currentPerpetualAccessIEs': result.status = [RDStore.TIPP_STATUS_CURRENT]
                    break
            }

        }
        else {
            result.tab = 'allTipps'
            Map stResult = resolveTabAndStatusForTitleTabsMenu(params, 'IEs')
            result.subTab = stResult.tab
            result.status = stResult.status
        }

        println '! ' + debug + ' -> [tab: ' + params.tab + ', subTab: ' + params.subTab + ', status: ' + params.list('status') + ']'

        result
    }
}
