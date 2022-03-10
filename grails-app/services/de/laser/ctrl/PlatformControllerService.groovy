package de.laser.ctrl

import de.laser.ContextService
import de.laser.Platform
import de.laser.SubscriptionPackage
import de.laser.oap.OrgAccessPoint
import de.laser.oap.OrgAccessPointLink
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

/**
 * This service serves as mirror for the {@link de.laser.PlatformController}, containing its data manipulating
 * methods
 */
@Transactional
class PlatformControllerService {

    static final int STATUS_OK = 0
    static final int STATUS_ERROR = 1

    MessageSource messageSource
    ContextService contextService

    //-------------------------------------------- derivation section --------------------------------------------------

    /**
     * Creates an OrgAccessPointLink with
     * subscriptionPackage=passed subscriptionPackage | Platform = passed Platform | AccessPoint = null.
     * This is a kind of marker to indicate that a subscriptionPackage specific access point configuration and no
     * AccessPoint derivation from Platform is used
     * @param params the parameter map containing the link details
     * @return OK with the new link on success, false otherwise
     */
    Map<String,Object> addDerivation(GrailsParameterMap params) {
        Map<String,Object> check = checkDerivationParams(params), result = [:]
        if(check.status == STATUS_ERROR)
            check
        else {
            OrgAccessPointLink oapl = new OrgAccessPointLink()
            oapl.subPkg = check.subPkg
            oapl.platform = check.platform
            oapl.active = true
            oapl.save()
            [result:result,status:STATUS_OK]
        }
    }

    /**
     * Deletes all OrgAccessPointLinks markers for the given platform und SubscriptionPackage
     * The marker (OrgAccessPoint=null), which indicates that want to overwrite platform specific AccessPoint links,
     * gets deleted too
     * @param params the parameter mao containing the link details
     * @return OK if the deletion was successful, false otherwise
     */
    Map<String,Object> removeDerivation(GrailsParameterMap params) {
        Map<String,Object> check = checkDerivationParams(params), result = [:]
        if(check.status == STATUS_ERROR)
            check
        else {
            String hql = "delete from OrgAccessPointLink oapl where oapl.platform=:platform_id and oapl.subPkg =:subPkg and oapl.active=true"
            OrgAccessPointLink.executeUpdate(hql, [platform_id:check.platform, subPkg:check.subPkg])
            [result:result,status:STATUS_OK]
        }
    }

    /**
     * Validates the given input parameters checks if subscription package and platform are given
     * @param params the parameter map whose input should be validated
     * @return OK if the parameters are present and loaded, ERROR otherwise
     */
    Map<String,Object> checkDerivationParams(GrailsParameterMap params) {
        Map<String,Object> result = [:]
        Locale locale = LocaleContextHolder.getLocale()
        if (!params.sp) {
            result.error = messageSource.getMessage('subscription.details.linkAccessPoint.missingSubPkg.message',null,locale)
        }
        SubscriptionPackage subPkg = SubscriptionPackage.get(params.sp)
        if (!subPkg){
            result.error = messageSource.getMessage('subscription.details.linkAccessPoint.subPkgNotFound.message',null,locale)
        }
        else result.subPkg = subPkg
        if (!params.platform_id) {
            result.error = messageSource.getMessage('subscription.details.linkAccessPoint.missingPlatform.message',null,locale)
        }
        Platform platform = Platform.get(params.platform_id)
        if (!platform){
            result.error = messageSource.getMessage('subscription.details.linkAccessPoint.platformNotFound.message',null,locale)
        }
        else result.platform = platform
        if(result.error)
            [result:result,status:STATUS_ERROR]
        else [result:result,status:STATUS_OK]
    }

    //------------------------------------------ access point linking section ------------------------------------------

    /**
     * Links the given access point to the given platform
     * @param params the parameter map containing the link data
     * @param apInstance the access point to link
     * @return OK if the linking was successful and no duplicated is linked, ERROR otherwise
     */
    Map<String,Object> linkAccessPoint(GrailsParameterMap params, OrgAccessPoint apInstance) {
        Map<String,Object> result = [:]
        // save link
        OrgAccessPointLink oapl = new OrgAccessPointLink()
        oapl.active = true
        oapl.oap = apInstance
        oapl.platform = Platform.get(params.platform_id)
        List<OrgAccessPointLink> existingActiveAP = []
        if (params.subscriptionPackage_id){
            SubscriptionPackage sp = SubscriptionPackage.get(params.subscriptionPackage_id)
            if (sp) {
                oapl.subPkg = sp
            }
            existingActiveAP.addAll(OrgAccessPointLink.findAllByActiveAndPlatformAndOapAndSubPkgIsNotNull(true, oapl.platform, apInstance))
        }
        else {
            existingActiveAP.addAll(OrgAccessPointLink.findAllByActiveAndPlatformAndOapAndSubPkgIsNull(true, oapl.platform, apInstance))
        }
        if (!existingActiveAP.isEmpty()){
            result.error = "Existing active AccessPoint for platform"
            [result:result,status:STATUS_ERROR]
        }
        if (! oapl.save()) {
            result.error = "Existing active AccessPoint for platform"
            [result:result,status:STATUS_ERROR]
        }
        [result:result,status:STATUS_OK]
    }

    /**
     * Deactivates the given access point link
     * @param params the access point link to deactivate
     * @return OK if the updating was successful, ERROR otherwise
     */
    Map<String,Object> removeAccessPoint(GrailsParameterMap params) {
        Map<String,Object> result = [:]
        // update active aopl, set active=false
        OrgAccessPointLink aoplInstance = OrgAccessPointLink.get(params.oapl_id)
        aoplInstance.active = false
        if (! aoplInstance.save()) {
            result.error = "Error updateing AccessPoint for platform"
            log.debug(aoplInstance.errors.toString())
            [result:result,status:STATUS_ERROR]
        }
        [result:result,status:STATUS_OK]
    }

    //--------------------------------------------- helper section -------------------------------------------------

    /**
     * Sets parameters used by various controller calls
     * @param params unused
     * @return a map containing the context user and institution
     */
    Map<String, Object> getResultGenerics(GrailsParameterMap params) {

        Map<String, Object> result = [:]

        result.user = contextService.getUser()
        result.institution = contextService.getOrg()
        result.contextOrg = result.institution //temp fix

        result
    }

}