package de.laser.helper

import de.laser.storage.RDStore
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.util.logging.Slf4j

@Slf4j
class FilterLogic {

    static Map<String, Object> resolveParamsForTopAttachedTitleTabs(GrailsParameterMap params, String entites, boolean ignorePlannedIEs = false) {
        log.debug('resolveParamsForTopAttachedTitleTabs( .., ' + entites + ', ' + ignorePlannedIEs + ' )')

        Map<String, Object> result = [:]

        // MyInstitutionController.currentTitles()              entites = 'IEs',   ignorePlannedIEs = false
        // MyInstitutionController.currentPermanentTitles()     entites = 'IEs',   ignorePlannedIEs = true
        // SubscriptionControllerService.index()                entites = 'IEs',   ignorePlannedIEs = false
        // TitleController.list()                               entites = 'Tipps', ignorePlannedIEs = false

        if (params.tab) {
            switch (params.tab) {
                case 'current' + entites:
                    result.status = [RDStore.TIPP_STATUS_CURRENT.id.toString()]
                    break
                case 'planned' + entites:
                    if (!ignorePlannedIEs) {
                        result.status = [RDStore.TIPP_STATUS_EXPECTED.id.toString()]
                    }
                    break
                case 'expired' + entites:
                    result.status = [RDStore.TIPP_STATUS_RETIRED.id.toString()]
                    break
                case 'deleted' + entites:
                    result.status = [RDStore.TIPP_STATUS_DELETED.id.toString()]
                    break
                case 'all' + entites:
                    result.status = [RDStore.TIPP_STATUS_CURRENT.id.toString(), RDStore.TIPP_STATUS_EXPECTED.id.toString(), RDStore.TIPP_STATUS_RETIRED.id.toString(), RDStore.TIPP_STATUS_DELETED.id.toString()]
                    break
            }
        }
        else if(params.list('status').size() == 1) {
            switch (params.list('status')[0]) {
                case RDStore.TIPP_STATUS_CURRENT.id.toString():
                    result.tab = 'current' + entites
                    break
                case RDStore.TIPP_STATUS_RETIRED.id.toString():
                    result.tab = 'expired' + entites
                    break
                case RDStore.TIPP_STATUS_EXPECTED.id.toString():
                    if (!ignorePlannedIEs) {
                        result.tab = 'planned' + entites
                    }
                    break
                case RDStore.TIPP_STATUS_DELETED.id.toString():
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
                result.status = [RDStore.TIPP_STATUS_CURRENT.id.toString()]
            }
        }

        result
    }

    static Map<String, Object> resolveParamsForTopAttachedTitleTabs_TODO(GrailsParameterMap params, String entites, boolean ignorePlannedIEs = false) {
        log.debug('resolveParamsForTopAttachedTitleTabs( .., ' + entites + ', ' + ignorePlannedIEs + ' )')

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
}
