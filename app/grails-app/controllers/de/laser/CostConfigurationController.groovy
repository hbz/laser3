package de.laser

import com.k_int.kbplus.*
import com.k_int.kbplus.auth.Role
import com.k_int.kbplus.auth.User
import de.laser.helper.DebugAnnotation
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import grails.plugin.springsecurity.annotation.Secured

class CostConfigurationController {

    def contextService
    def accessService
    def genericOIDService

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_USER", specRole="ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_USER", "ROLE_ADMIN")
    })
    Map<String, Object> index() {
        Map<String, Object> result = [:]

        Org org = contextService.getOrg()
        User user = contextService.getUser()
        def costItemElementConfigurations = []
        def costItemElements = RefdataCategory.getAllRefdataValues(RDConstants.COST_ITEM_ELEMENT)

        costItemElements.each { cie ->
            def currentSetting = CostItemElementConfiguration.findByCostItemElementAndForOrganisation(RefdataValue.getByValueAndCategory(cie.value, RDConstants.COST_ITEM_ELEMENT),org)
            if(currentSetting) {
                costItemElementConfigurations.add(currentSetting)
            }
        }

        result.editable    =  accessService.checkMinUserOrgRole(user, org, "INST_EDITOR")
        result.costItemElementConfigurations = costItemElementConfigurations
        result.costItemElements = costItemElements
        result.institution = org

        result
    }

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    Object createNewConfiguration() {
        Map<String, Object> result = [editable:true] //the user clicking here is already authenticated
        Set<RefdataValue> costItemElements = RefdataCategory.getAllRefdataValues(RDConstants.COST_ITEM_ELEMENT)
        Set<RefdataValue> elementsAlreadyTaken = []
        Org org = contextService.getOrg()

        costItemElements.each { cie ->
            CostItemElementConfiguration currentSetting = CostItemElementConfiguration.findByCostItemElementAndForOrganisation(RefdataValue.getByValueAndCategory(cie.value, RDConstants.COST_ITEM_ELEMENT),org)
            if(currentSetting) {
                elementsAlreadyTaken.add(cie)
            }
        }
        costItemElements.removeAll(elementsAlreadyTaken)

        result.formUrl = g.createLink([controller:'costConfiguration',action:'processConfigurationCreation'])
        result.costItemElements = costItemElements
        result.elementSigns = RefdataCategory.getAllRefdataValues(RDConstants.COST_CONFIGURATION)
        result.institution = org

        render template: '/templates/newCostItemElementConfiguration', model: result
    }

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def processConfigurationCreation() {
        CostItemElementConfiguration ciec = new CostItemElementConfiguration()
        ciec.costItemElement = genericOIDService.resolveOID(params.cie)
        ciec.elementSign = genericOIDService.resolveOID(params.sign)
        ciec.forOrganisation = (Org) contextService.getOrg()
        if(!ciec.validate()) {
            ciec.errors.allErrors.collect {
                log.error("Error occurred: ${it.properties.field} has erroneous value ${it.properties.rejectedValue}, error code: ${it.properties.code}")
            }
        }
        else ciec.save()
        redirect action: 'index'
    }

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def deleteCostConfiguration() {
        if(params.ciec) {
            CostItemElementConfiguration ciec = CostItemElementConfiguration.get(params.ciec)
            if(ciec)
                ciec.delete()
        }
        else {
            flash.error = message(code: 'costConfiguration.delete.noCiec')
        }
        redirect(url: request.getHeader('referer'))
    }

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def setAllCostItems() {
        def cie = genericOIDService.resolveOID(params.cie)
        Org org = contextService.org
        def concernedCostItems = CostItem.findAllByOwnerAndCostItemElementAndCostItemElementConfigurationAndCostItemStatusNotEqual(org,cie,null, RDStore.COST_ITEM_DELETED).collect {it.id}
        def ciec = CostItemElementConfiguration.findByCostItemElementAndForOrganisation(cie,org)
        if(concernedCostItems) {
            CostItem.executeUpdate('UPDATE CostItem ci SET ci.costItemElementConfiguration = :ciec WHERE ci.id IN (:cci)',[ciec:ciec.elementSign,cci:concernedCostItems])
            flash.message = message(code:'costConfiguration.configureAllCostItems.done')
        }
        else flash.message = message(code:'costConfiguration.configureAllCostItems.nothingToDo')
        redirect(url: request.getHeader('referer'))
    }

}
