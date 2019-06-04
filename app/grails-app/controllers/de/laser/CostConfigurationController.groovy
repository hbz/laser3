package de.laser

import com.k_int.kbplus.*
import com.k_int.kbplus.auth.Role
import de.laser.helper.DebugAnnotation
import grails.plugin.springsecurity.annotation.Secured

class CostConfigurationController {

    def contextService
    def accessService
    def genericOIDService
    private final def user_role        = Role.findByAuthority('INST_ADM')

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_ADM", specRole="ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_ADM", "ROLE_ADMIN")
    })
    def index() {
        if(params) {
            CostItemElementConfiguration ciec = new CostItemElementConfiguration()
            ciec.costItemElement = genericOIDService.resolveOID(params.cie)
            ciec.elementSign = genericOIDService.resolveOID(params.sign)
            ciec.forOrganisation = (Org) contextService.getOrg()
            if(!ciec.validate()) {
                ciec.errors.allErrors.collect {
                    log.error("Error occurred: ${it.properties.field} has erroneous value ${it.properties.rejectedValue}, error code: ${it.properties.code}")
                }
            }
            else ciec.save(flush:true)
        }
        getCurrentConfigurations()
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_ADM")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_ADM") })
    def getCurrentConfigurations() {
        def result = [:]

        def org = contextService.getOrg()
        def user = contextService.getUser()
        def costItemElementConfigurations = []
        def costItemElements = RefdataCategory.getAllRefdataValues('CostItemElement')

        costItemElements.each { cie ->
            def currentSetting = CostItemElementConfiguration.findByCostItemElementAndForOrganisation(RefdataValue.getByValueAndCategory(cie,'CostItemElement'),org)
            if(currentSetting) {
                costItemElementConfigurations.add(currentSetting)
            }
        }

        result.editable    =  accessService.checkMinUserOrgRole(user, org, user_role)
        result.costItemElementConfigurations = costItemElementConfigurations
        result.costItemElements = costItemElements
        result.institution = org

        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_ADM")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_ADM") })
    def createNewConfiguration() {
        def result = [editable:true] //the user clicking here is already authenticated
        def costItemElements = RefdataValue.findAllByOwner(RefdataCategory.findByDesc('CostItemElement'))
        def elementsAlreadyTaken = []
        def org = contextService.getOrg()

        costItemElements.each { cie ->
            def currentSetting = CostItemElementConfiguration.findByCostItemElementAndForOrganisation(RefdataValue.getByValueAndCategory(cie,'CostItemElement'),org)
            if(currentSetting) {
                elementsAlreadyTaken.add(cie)
            }
        }
        costItemElements.removeAll(elementsAlreadyTaken)

        result.formUrl = g.createLink([controller:'costConfiguration',action:'index'])
        result.costItemElements = costItemElements
        result.elementSigns = RefdataValue.findAllByOwner(RefdataCategory.findByDesc('Cost configuration'))
        result.institution = org

        render template: '/templates/newCostItemElementConfiguration', model: result
        return
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_ADM")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_ADM") })
    def setAllCostItems() {
        def cie = genericOIDService.resolveOID(params.cie)
        def org = contextService.org
        def concernedCostItems = CostItem.findAllByOwnerAndCostItemElementAndCostItemElementConfiguration(org,cie,null).collect {it -> it.id}
        def ciec = CostItemElementConfiguration.findByCostItemElementAndForOrganisation(cie,org)
        if(concernedCostItems) {
            CostItem.executeUpdate('UPDATE CostItem ci SET ci.costItemElementConfiguration = :ciec WHERE ci.id IN (:cci)',[ciec:ciec.elementSign,cci:concernedCostItems])
            flash.message = message(code:'costConfiguration.configureAllCostItems.done')
        }
        else flash.message = message(code:'costConfiguration.configureAllCostItems.nothingToDo')
        redirect(url: request.getHeader('referer'))
    }

}
