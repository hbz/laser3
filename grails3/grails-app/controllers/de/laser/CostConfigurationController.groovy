package de.laser


import de.laser.auth.User
import de.laser.finance.CostItem
import de.laser.finance.CostItemElementConfiguration
import de.laser.annotations.DebugAnnotation
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import grails.plugin.springsecurity.annotation.Secured

/**
 * This controller manages an institution's cost configurations
 * @see CostItem
 * @see CostItemElementConfiguration
 */
class CostConfigurationController {

    def contextService
    def accessService
    def genericOIDService
    FinanceService financeService

    /**
     * Gets the current list of an institution's cost configurations
     */
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

    /**
     * Opens the creation modal, filtering out those cost item elements for which a configuration already exists
     */
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

    /**
     * Controller call for creating a new cost item element configuration
     * @return the list view
     */
    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def processConfigurationCreation() {
        financeService.processConfigurationCreation(params)
        redirect action: 'index'
    }

    /**
     * Controller call for deleting a cost item element configuration
     * @return the list view
     */
    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def deleteCostConfiguration() {
        if(params.ciec) {
            CostItemElementConfiguration ciec = CostItemElementConfiguration.get(params.ciec)
            if(ciec)
                financeService.deleteCostConfiguration(ciec)
        }
        else {
            flash.error = message(code: 'costConfiguration.delete.noCiec')
        }
        redirect(url: request.getHeader('referer'))
    }

    /**
     * This call triggers a bulk setting of every cost item which has no cost item element configuration yet, assigning
     * the given cost item element configuration to each of them
     * @return the list view
     */
    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def setAllCostItems() {
        def cie = genericOIDService.resolveOID(params.cie)
        Org org = contextService.getOrg()
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
