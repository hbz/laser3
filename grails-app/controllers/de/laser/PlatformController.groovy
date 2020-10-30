package de.laser


import de.laser.auth.User
 
import de.laser.helper.DebugAnnotation
import de.laser.helper.RDConstants
import de.laser.oap.OrgAccessPoint
import de.laser.oap.OrgAccessPointLink
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.dao.DataIntegrityViolationException

@Secured(['IS_AUTHENTICATED_FULLY'])
class PlatformController  {

    def springSecurityService
    def contextService
    def orgTypeService
    def accessService

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    @Secured(['ROLE_USER'])
    def index() {
        redirect action: 'list', params: params
    }

    @Secured(['ROLE_USER'])
    def list() {
        Map<String, Object> result = [:]
        result.user = User.get(springSecurityService.principal.id)
        result.max = params.max ?: result.user.getDefaultPageSize()

        result.offset = params.offset ?: 0

        RefdataValue deleted_platform_status = RefdataValue.getByValueAndCategory( 'Deleted', RDConstants.PLATFORM_STATUS)
        Map<String, Object> qry_params = [delStatus: deleted_platform_status]

        String base_qry = " from Platform as p left join p.org o where ((p.status is null) OR (p.status = :delStatus)) "

        if ( params.q?.length() > 0 ) {

            base_qry += "and ("
            base_qry += "  genfunc_filter_matcher(p.normname, :query ) = true or "
            base_qry += "  genfunc_filter_matcher(p.primaryUrl, :query) = true or ( "
            base_qry += "    genfunc_filter_matcher(o.name, :query) = true or "
            base_qry += "    genfunc_filter_matcher(o.sortname, :query) = true or "
            base_qry += "    genfunc_filter_matcher(o.shortname, :query) = true "
            base_qry += "  ) "
            base_qry += ")"

            qry_params.put('query', "${params.q}")
        }
        else {
            base_qry += "order by p.normname asc"
            //qry_params.add("%");
        }

        log.debug(base_qry)
        log.debug(qry_params.toMapString())

        result.platformInstanceTotal = Subscription.executeQuery( "select p.id " + base_qry, qry_params ).size()
        result.platformInstanceList = Subscription.executeQuery( "select p " + base_qry, qry_params, [max:result.max, offset:result.offset] )

      result
    }

    @Secured(['ROLE_USER'])
    def show() {
      Map<String, Object> result = setResultGenerics()
        Platform platformInstance = Platform.get(params.id)
      if (!platformInstance) {
        flash.message = message(code: 'default.not.found.message', 
                                args: [message(code: 'platform.label'), params.id])
        redirect action: 'list'
        return
      }

        result.platformInstance = platformInstance
        result.editable = accessService.checkPermAffiliationX('ORG_BASIC_MEMBER,ORG_CONSORTIUM','INST_EDITOR','ROLE_ADMIN')

        List currentSubIds = orgTypeService.getCurrentSubscriptionIds(result.contextOrg)

        String qry = "select distinct(ap) , spoap.id" +
                " from " +
                "    TitleInstancePackagePlatform tipp join tipp.platform platf join tipp.pkg pkg, " +
                "    SubscriptionPackage subPkg join subPkg.subscriptionPackageOrgAccessPoint spoap join spoap.orgAccessPoint ap " +
                " where " +
                "    subPkg.pkg = pkg and "+
                "    platf.id =  (:platformId) and " +
                "    subPkg.subscription.id in (:currentSubIds)"

        Map<String,Object> qryParams = [
                platformId : platformInstance.id,
                currentSubIds : currentSubIds
        ]
        //List orgAccessPointList = OrgAccessPoint.executeQuery(qry, qryParams)

        //Map<String, Object> result = [:]

        String hql = "select oapl from OrgAccessPointLink oapl join oapl.oap as ap "
        hql += "where ap.org =:institution and oapl.active=true and oapl.platform.id=${platformInstance.id} and oapl.subPkg is null order by LOWER(ap.name)"
        result.orgAccessPointList = OrgAccessPointLink.executeQuery(hql,[institution : result.contextOrg])

        String notActiveAPLinkQuery = "select oap from OrgAccessPoint oap where oap.org =:institution "
        notActiveAPLinkQuery += "and not exists ("
        notActiveAPLinkQuery += "select 1 from oap.oapp as oapl where oapl.oap=oap and oapl.active=true "
        notActiveAPLinkQuery += "and oapl.platform.id = ${platformInstance.id} and oapl.subPkg is null) order by lower(oap.name)"

        result.accessPointList = OrgAccessPoint.executeQuery(notActiveAPLinkQuery, [institution : result.contextOrg])
        result.selectedInstitution = result.contextOrg.id
        result

    }

    //@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    //@Secured(closure = { principal.user?.hasAffiliation("INST_EDITOR") })
    @Secured(['ROLE_ADMIN'])
    def delete() {
        Platform platformInstance = Platform.get(params.id)
        if (!platformInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'platform.label'), params.id])
            redirect action: 'list'
            return
        }

        try {
            platformInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'platform.label'), params.id])
            redirect action: 'list'
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'platform.label'), params.id])
            redirect action: 'show', id: params.id
        }
    }

    @Secured(['ROLE_USER'])
    def accessMethods() {
        // TODO: editable is undefined
        def editable
        Platform platformInstance = Platform.get(params.id)
        if (!platformInstance) {
            flash.message = message(code: 'default.not.found.message',
                    args: [message(code: 'platform.label'), params.id])
            redirect action: 'list'
            return
        }

        List<PlatformAccessMethod> platformAccessMethodList = PlatformAccessMethod.findAllByPlatf(platformInstance, [sort: ["accessMethod": 'asc', "validFrom" : 'asc']])

        [platformInstance: platformInstance, platformAccessMethodList: platformAccessMethodList, editable: editable, params: params]
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { principal.user?.hasAffiliation("INST_EDITOR") })
    def link() {
        Map<String, Object> result = [:]
        Platform platformInstance = Platform.get(params.id)
        if (!platformInstance) {
            flash.message = message(code: 'default.not.found.message',
                args: [message(code: 'platform.label'), params.id])
            redirect action: 'list'
            return
        }
        Org selectedInstitution = contextService.getOrg()
        List<Org> authorizedOrgs = contextService.getUser().getAuthorizedOrgs()

        String hql = "select oapl from OrgAccessPointLink oapl join oapl.oap as ap "
            hql += "where ap.org =:institution and oapl.active=true and oapl.platform.id=${platformInstance.id}"
        List<OrgAccessPointLink> results = OrgAccessPointLink.executeQuery(hql,[institution : selectedInstitution])

        String notActiveAPLinkQuery = "select oap from OrgAccessPoint oap where oap.org =:institution "
            notActiveAPLinkQuery += "and not exists ("
            notActiveAPLinkQuery += "select 1 from oap.oapp as oapl where oapl.oap=oap and oapl.active=true "
            notActiveAPLinkQuery += "and oapl.platform.id = ${platformInstance.id})"

        List<OrgAccessPoint> accessPointList = OrgAccessPoint.executeQuery(notActiveAPLinkQuery, [institution : selectedInstitution])

        result.accessPointLinks = results
        result.platformInstance = platformInstance
        result.institution = authorizedOrgs
        result.accessPointList = accessPointList
        result.selectedInstitution = selectedInstitution.id
        result
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { principal.user?.hasAffiliation("INST_EDITOR") })
    def dynamicApLink(){
        Map<String, Object> result = [:]
        Platform platformInstance = Platform.get(params.platform_id)
        if (!platformInstance) {
            flash.message = message(code: 'default.not.found.message',
                args: [message(code: 'platform.label'), params.platform_id])
            redirect action: 'list'
            return
        }
        List<Org> authorizedOrgs = contextService.getUser().getAuthorizedOrgs()
        Org selectedInstitution =  contextService.getOrg()
        if (params.institution_id){
            selectedInstitution = Org.get(params.institution_id)
        }
        String hql = "select oapl from OrgAccessPointLink oapl join oapl.oap as ap "
        hql += "where ap.org =:institution and oapl.active=true and oapl.platform.id=${platformInstance.id}"
        List<OrgAccessPointLink> results = OrgAccessPointLink.executeQuery(hql,[institution : selectedInstitution])

        String notActiveAPLinkQuery = "select oap from OrgAccessPoint oap where oap.org =:institution "
        notActiveAPLinkQuery += "and not exists ("
        notActiveAPLinkQuery += "select 1 from oap.oapp as oapl where oapl.oap=oap and oapl.active=true "
        notActiveAPLinkQuery += "and oapl.platform.id = ${platformInstance.id})"

        List<OrgAccessPoint> accessPointList = OrgAccessPoint.executeQuery(notActiveAPLinkQuery, [institution : selectedInstitution])

        result.accessPointLinks = results
        result.platformInstance = platformInstance
        result.institution = authorizedOrgs
        result.accessPointList = accessPointList
        result.selectedInstitution = selectedInstitution.id
        render(view: "_apLinkContent", model: result)
    }

    @DebugAnnotation(perm="ORG_BASIC_MEMBER,ORG_CONSORTIUM", affil="INST_EDITOR")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def addDerivation() {
        if (!params.sp) {
            flash.message = message(code: 'subscription.details.linkAccessPoint.missingSubPkg.message')
            redirect(url: request.getHeader('referer'))
            return
        }
        def subPkg = SubscriptionPackage.get(params.sp)
        if (!subPkg){
            flash.message = message(code: 'subscription.details.linkAccessPoint.subPkgNotFound.message')
            redirect(url: request.getHeader('referer'))
            return
        }
        if (!params.platform_id) {
            flash.message = message(code: 'subscription.details.linkAccessPoint.missingPlatform.message')
            redirect(url: request.getHeader('referer'))
            return
        }
        def platform = Platform.get(params.platform_id)
        if (!platform){
            flash.message = message(code: 'subscription.details.linkAccessPoint.platformNotFound.message')
            redirect(url: request.getHeader('referer'))
            return
        }
        // delete marker all OrgAccessPointLinks for the given platform und SubscriptionPackage
        // The marker (OrgAccessPoint=null), which indicates that want to overwrite platform specific AccessPoint links,
        // gets deleted too
        String hql = "delete from OrgAccessPointLink oapl where oapl.platform=:platform_id and oapl.subPkg =:subPkg and oapl.active=true"
        OrgAccessPointLink.executeUpdate(hql, [platform_id:platform, subPkg:subPkg])

        redirect(url: request.getHeader('referer'))
    }

    @DebugAnnotation(perm="ORG_BASIC_MEMBER,ORG_CONSORTIUM", affil="INST_EDITOR")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def removeDerivation() {
        // create an OrgAccessPointLink with
        // subscriptionPackage=passed subscriptionPackage | Platform = passed Platform | AccessPoint = null
        // this is a kind of marker to indicate that a subscriptionPackage specific AP configuration and no
        // AccessPoint derivation from Platform is used
        if (!params.sp) {
            flash.message = message(code: 'subscription.details.linkAccessPoint.missingSubPkg.message')
            redirect(url: request.getHeader('referer'))
            return
        }
        def subPkg = SubscriptionPackage.get(params.sp)
        if (!subPkg){
            flash.message = message(code: 'subscription.details.linkAccessPoint.subPkgNotFound.message')
            redirect(url: request.getHeader('referer'))
            return
        }
        if (!params.platform_id) {
            flash.message = message(code: 'subscription.details.linkAccessPoint.missingPlatform.message')
            redirect(url: request.getHeader('referer'))
            return
        }
        def platform = Platform.get(params.platform_id)
        if (!platform){
            flash.message = message(code: 'subscription.details.linkAccessPoint.platformNotFound.message')
            redirect(url: request.getHeader('referer'))
            return
        }

        def oapl = new OrgAccessPointLink()
        oapl.subPkg = subPkg
        oapl.platform = platform
        oapl.active = true
        oapl.save(flush:true)
        redirect(url: request.getHeader('referer'))
    }

    @DebugAnnotation(perm="ORG_BASIC_MEMBER,ORG_CONSORTIUM", affil="INST_EDITOR")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def linkAccessPoint() {
        OrgAccessPoint apInstance
        if (params.AccessPoints){
            apInstance = OrgAccessPoint.get(params.AccessPoints)
            if (!apInstance) {
                flash.error = 'No valid Accesspoint id given'
                redirect action: 'link', params: [id:params.id]
                return
            }
        }
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
            existingActiveAP = OrgAccessPointLink.findAllByActiveAndPlatformAndOapAndSubPkgIsNotNull(
                true, oapl.platform, apInstance
            )
        } else {
            existingActiveAP = OrgAccessPointLink.findAllByActiveAndPlatformAndOapAndSubPkgIsNull(
                true, oapl.platform, apInstance
            )
        }

        if (!existingActiveAP.isEmpty()){
            flash.error = "Existing active AccessPoint for platform"
            redirect(url: request.getHeader('referer'))
            return
        }
        if (! oapl.save(flush:true)) {
            flash.error = "Existing active AccessPoint for platform"
        }

        redirect(url: request.getHeader('referer'))
    }

    @DebugAnnotation(perm="ORG_BASIC_MEMBER,ORG_CONSORTIUM", affil="INST_EDITOR")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def removeAccessPoint() {
        // update active aopl, set active=false
        OrgAccessPointLink aoplInstance = OrgAccessPointLink.get(params.oapl_id)
        aoplInstance.active = false
        if (! aoplInstance.save(flush:true)) {
            log.debug("Error updateing AccessPoint for platform")
            log.debug(aopl.errors.toString())
            // TODO flash
        }
        redirect action: 'link', params: [id:params.id]
    }

    private Map<String, Object> setResultGenerics() {
        Map<String, Object> result = [:]
        result.user = User.get(springSecurityService.principal.id)
        result.institution = contextService.org
        result.contextOrg = result.institution //temp fix
        result
    }

}
