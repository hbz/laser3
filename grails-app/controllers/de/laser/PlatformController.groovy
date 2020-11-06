package de.laser


import de.laser.auth.User
import de.laser.ctrl.PlatformControllerService
import de.laser.helper.DebugAnnotation
import de.laser.helper.RDConstants
import de.laser.oap.OrgAccessPoint
import de.laser.oap.OrgAccessPointLink
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.dao.DataIntegrityViolationException

@Secured(['IS_AUTHENTICATED_FULLY'])
class PlatformController  {

    def springSecurityService
    def contextService
    def orgTypeService
    def accessService
    PlatformControllerService platformControllerService

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
        Map<String, Object> result = platformControllerService.getResultGenerics(params)
        Platform platformInstance = Platform.get(params.id)

        if (!platformInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'platform.label'), params.id])
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
    @Transactional
    def delete() {
        Platform platformInstance = Platform.get(params.id)
        if (!platformInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'platform.label'), params.id])
            redirect action: 'list'
            return
        }

        try {
            platformInstance.delete()
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

    @DebugAnnotation(perm="ORG_BASIC_MEMBER,ORG_CONSORTIUM", affil="INST_EDITOR", ctrlService = 2)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def addDerivation() {
        Map<String,Object> ctrlResult = platformControllerService.addDerivation(params)
        if(ctrlResult.status == PlatformControllerService.STATUS_ERROR) {
            flash.error = ctrlResult.result.error
        }
        redirect(url: request.getHeader('referer'))
    }

    @DebugAnnotation(perm="ORG_BASIC_MEMBER,ORG_CONSORTIUM", affil="INST_EDITOR", ctrlService = 2)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def removeDerivation() {
        Map<String,Object> ctrlResult = platformControllerService.removeDerivation(params)
        if(ctrlResult.status == PlatformControllerService.STATUS_ERROR) {
            flash.error = ctrlResult.result.error
        }
        redirect(url: request.getHeader('referer'))
    }

    @DebugAnnotation(perm="ORG_BASIC_MEMBER,ORG_CONSORTIUM", affil="INST_EDITOR", ctrlService = 2)
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
            }
            else {
                Map<String,Object> ctrlResult = platformControllerService.linkAccessPoint(params, apInstance)
                if(ctrlResult.status == PlatformControllerService.STATUS_ERROR) {
                    flash.error = ctrlResult.result.error
                }
                redirect(url: request.getHeader('referer'))
            }
        }
    }

    @DebugAnnotation(perm="ORG_BASIC_MEMBER,ORG_CONSORTIUM", affil="INST_EDITOR", ctrlService = 2)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def removeAccessPoint() {
        Map<String,Object> ctrlResult = platformControllerService.removeAccessPoint(params)
        if(ctrlResult.status == PlatformControllerService.STATUS_ERROR) {
            flash.error = ctrlResult.result.error
        }
        redirect action: 'link', params: [id:params.id]
    }
}
