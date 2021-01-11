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

@Transactional
class PlatformControllerService {

    static final int STATUS_OK = 0
    static final int STATUS_ERROR = 1

    MessageSource messageSource
    ContextService contextService

    //-------------------------------------------- derivation section --------------------------------------------------

    Map<String,Object> addDerivation(GrailsParameterMap params) {
        // create an OrgAccessPointLink with
        // subscriptionPackage=passed subscriptionPackage | Platform = passed Platform | AccessPoint = null
        // this is a kind of marker to indicate that a subscriptionPackage specific AP configuration and no
        // AccessPoint derivation from Platform is used
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

    Map<String,Object> removeDerivation(GrailsParameterMap params) {
        // delete marker all OrgAccessPointLinks for the given platform und SubscriptionPackage
        // The marker (OrgAccessPoint=null), which indicates that want to overwrite platform specific AccessPoint links,
        // gets deleted too
        Map<String,Object> check = checkDerivationParams(params), result = [:]
        if(check.status == STATUS_ERROR)
            check
        else {
            String hql = "delete from OrgAccessPointLink oapl where oapl.platform=:platform_id and oapl.subPkg =:subPkg and oapl.active=true"
            OrgAccessPointLink.executeUpdate(hql, [platform_id:check.platform, subPkg:check.subPkg])
            [result:result,status:STATUS_OK]
        }
    }

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

    Map<String, Object> getResultGenerics(GrailsParameterMap params) {

        Map<String, Object> result = [:]

        result.user = contextService.getUser()
        result.institution = contextService.getOrg()
        result.contextOrg = result.institution //temp fix

        result
    }

}