package de.laser.ctrl

import com.k_int.kbplus.GenericOIDService
import de.laser.*
import de.laser.auth.User
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

@Transactional
class OrganisationControllerService {

    static final int STATUS_OK = 0
    static final int STATUS_ERROR = 1

    AccessService accessService
    ContextService contextService
    FormService formService
    MessageSource messageSource
    GenericOIDService genericOIDService

    //--------------------------------------------- member section -------------------------------------------------

    Map<String,Object> createMember(OrganisationController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(controller,params)
        Org orgInstance
        Locale locale = LocaleContextHolder.getLocale()
        if(formService.validateToken(params)) {
            try {
                // createdBy will set by Org.beforeInsert()
                orgInstance = new Org(name: params.institution, sector: RDStore.O_SECTOR_HIGHER_EDU, status: RDStore.O_STATUS_CURRENT)
                orgInstance.save()
                Combo newMember = new Combo(fromOrg:orgInstance,toOrg:result.institution,type: RDStore.COMBO_TYPE_CONSORTIUM)
                newMember.save()
                orgInstance.setDefaultCustomerType()
                orgInstance.addToOrgType(RDStore.OT_INSTITUTION) //RDStore adding causes a DuplicateKeyException - RefdataValue.getByValueAndCategory('Institution', RDConstants.ORG_TYPE)
                result.orgInstance = orgInstance
                Object[] args = [messageSource.getMessage('org.institution.label',null,locale), orgInstance.name]
                result.message = messageSource.getMessage('default.created.message', args, locale)
                [result:result,status:STATUS_OK]
            }
            catch (Exception e) {
                log.error("Problem creating institution")
                log.error(e.printStackTrace())
                Object[] args = [orgInstance ? orgInstance.errors : 'unbekannt']
                result.message = messageSource.getMessage("org.error.createInstitutionError", args, locale)
                [result:result,status:STATUS_ERROR]
            }
        }
        else [result:null,status:STATUS_ERROR]
    }

    Map<String, Object> toggleCombo(OrganisationController controller, GrailsParameterMap params) {
        Locale locale = LocaleContextHolder.getLocale()
        Map<String, Object> result = getResultGenericsAndCheckAccess(controller, params)
        if (!result) {
            return [result:null, status:STATUS_ERROR]
        }
        if (!params.direction) {
            result.error = messageSource.getMessage('org.error.noToggleDirection',null,locale)
            return [result:result, status:STATUS_ERROR]
        }
        switch(params.direction) {
            case 'add':
                Map map = [toOrg: result.institution, fromOrg: Org.get(params.fromOrg), type: RDStore.COMBO_TYPE_CONSORTIUM]
                if (! Combo.findWhere(map)) {
                    Combo cmb = new Combo(map)
                    cmb.save()
                }
                break
            case 'remove':
                if(Subscription.executeQuery("from Subscription as s where exists ( select o from s.orgRelations as o where o.org in (:orgs) )", [orgs: [result.institution, Org.get(params.fromOrg)]])){
                    result.error = messageSource.getMessage('org.consortiaToggle.remove.notPossible.sub',null,locale)
                    return [result:result, status:STATUS_ERROR]
                }
                else if(License.executeQuery("from License as l where exists ( select o from l.orgRelations as o where o.org in (:orgs) )", [orgs: [result.institution, Org.get(params.fromOrg)]])){
                    result.error = messageSource.getMessage('org.consortiaToggle.remove.notPossible.lic',null,locale)
                    return [result:result, status:STATUS_ERROR]
                }
                else {
                    Combo cmb = Combo.findWhere(toOrg: result.institution,
                            fromOrg: Org.get(params.fromOrg),
                            type:RDStore.COMBO_TYPE_CONSORTIUM)
                    cmb.delete()
                }
                break
        }
        [result:result, status:STATUS_OK]
    }

    //--------------------------------------------- identifier section -------------------------------------------------

    Map<String,Object> deleteCustomerIdentifier(OrganisationController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(controller,params)
        Locale locale = LocaleContextHolder.getLocale()
        CustomerIdentifier ci = (CustomerIdentifier) genericOIDService.resolveOID(params.deleteCI)
        Org owner = ci.owner
        if (ci && owner.id == result.institution.id) {
            ci.delete()
            log.debug("CustomerIdentifier deleted: ${params}")
            [result:result,status:STATUS_OK]
        } else {
            if ( ! ci ) {
                Object[] args = [messageSource.getMessage('org.customerIdentifier',null,locale), params.deleteCI]
                result.error = messageSource.getMessage('default.not.found.message', args, locale)
            } else {
                result.error = messageSource.getMessage('org.customerIdentifier.delete.norights',null,locale)
            }
            log.error("CustomerIdentifier NOT deleted: ${params}; CustomerIdentifier not found or ContextOrg is not " +
                    "owner of this CustomerIdentifier and has no rights to delete it!")
            [result:result,status:STATUS_ERROR]
        }
    }

    //--------------------------------------------- helper section -------------------------------------------------

    Map<String, Object> getResultGenericsAndCheckAccess(OrganisationController controller, GrailsParameterMap params) {

        User user = contextService.getUser()
        Org org = contextService.getOrg()
        Map<String, Object> result = [user:user,
                                      institution:org,
                                      inContextOrg:true,
                                      institutionalView:false,
                                      isGrantedOrgRoleAdminOrOrgEditor: SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR'),
                                      isGrantedOrgRoleAdmin: SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN'),
                                      contextCustomerType:org.getCustomerType()]

        //if(result.contextCustomerType == 'ORG_CONSORTIUM')

        result.availableConfigs = RefdataCategory.getAllRefdataValuesWithOrder(RDConstants.SHARE_CONFIGURATION)
        if(result.contextCustomerType == "ORG_CONSORTIUM"){
            result.availableConfigs-RDStore.SHARE_CONF_CONSORTIUM
        }

        if (params.id) {
            result.orgInstance = Org.get(params.id)
            result.isProviderOrAgency = RDStore.OT_PROVIDER.id in result.allOrgTypeIds || RDStore.OT_AGENCY.id in result.allOrgTypeIds
            result.editable = controller.checkIsEditable(user, result.orgInstance)
            result.inContextOrg = result.orgInstance.id == org.id
            //this is a flag to check whether the page has been called for a consortia or inner-organisation member
            Combo checkCombo = Combo.findByFromOrgAndToOrg(result.orgInstance,org)
            if (checkCombo && checkCombo.type == RDStore.COMBO_TYPE_CONSORTIUM) {
                result.institutionalView = true
            }
            //restrictions hold if viewed org is not the context org
            if (!result.inContextOrg && !accessService.checkPerm("ORG_CONSORTIUM") && !SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN, ROLE_ORG_EDITOR")) {
                //restrictions further concern only single users, not consortia
                if (accessService.checkPerm("ORG_INST") && result.orgInstance.getCustomerType() == "ORG_INST") {
                    return null
                }
            }
        }
        else {
            result.editable = controller.checkIsEditable(user, org)
            result.orgInstance = result.institution
            result.inContextOrg = true
        }
        result.targetCustomerType = result.orgInstance.getCustomerType()
        result.allOrgTypeIds = result.orgInstance.getAllOrgTypeIds()

        result
    }
}