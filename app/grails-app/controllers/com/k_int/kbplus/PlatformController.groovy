package com.k_int.kbplus

import com.k_int.kbplus.auth.User
import de.laser.AccessService
import de.laser.controller.AbstractDebugController
import de.laser.helper.DebugAnnotation
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import org.apache.lucene.index.DocIDMerger
import org.springframework.dao.DataIntegrityViolationException

@Secured(['IS_AUTHENTICATED_FULLY'])
class PlatformController extends AbstractDebugController {

    def springSecurityService
    def contextService
    def filterService
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
        result.max = params.max ?: result.user.getDefaultPageSizeTMP()

        result.offset = params.offset ?: 0

        def deleted_platform_status = RefdataValue.getByValueAndCategory( 'Deleted', RDConstants.PLATFORM_STATUS)
        def qry_params = [delStatus: deleted_platform_status]

        def base_qry = " from Platform as p left join p.org o where ((p.status is null) OR (p.status = :delStatus)) "

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
        log.debug(qry_params)

        result.platformInstanceTotal = Subscription.executeQuery("select p.id " + base_qry, qry_params ).size()
        result.platformInstanceList = Subscription.executeQuery("select p ${base_qry}", qry_params, [max:result.max, offset:result.offset]);

      result
    }

    //@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    //@Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    @Secured(['ROLE_ADMIN'])
    def create() {
    switch (request.method) {
    case 'GET':
          [platformInstance: new Platform(params)]
      break
    case 'POST':
          def platformInstance = new Platform(params)
          if (!platformInstance.save(flush: true)) {
              render view: 'create', model: [platformInstance: platformInstance]
              return
          }

      flash.message = message(code: 'default.created.message', args: [message(code: 'platform.label', default: 'Platform'), platformInstance.id])
          redirect action: 'show', id: platformInstance.id
      break
    }
    }

    @Secured(['ROLE_USER'])
    def show() {
      Map result = setResultGenerics()
      def editable
      def platformInstance = Platform.get(params.id)
      if (!platformInstance) {
        flash.message = message(code: 'default.not.found.message', 
                                args: [message(code: 'platform.label', default: 'Platform'), params.id])
        redirect action: 'list'
        return
      }

      editable = accessService.checkMinUserOrgRole(result.user, result.orgInstance, 'INST_EDITOR') || SpringSecurityUtils.ifAllGranted('ROLE_ADMIN')

        result = [platformInstance: platformInstance, editable: editable, user: springSecurityService.getCurrentUser()]
     /*// Build up a crosstab array of title-platforms under this package
      def packages = [:]
      def package_list = []
      def titles = [:]
      def title_list = []
      int pkg_count = 0;
      int title_count = 0;

      log.debug("Adding packages");
      // Find all platforms
      platformInstance.tipps.each{ tipp ->
        // log.debug("Consider ${tipp.title.title}")
        if ( !packages.keySet().contains(tipp.pkg.id) ) {
          package_list.add(tipp.pkg)
          packages[tipp.pkg.id] = [position:pkg_count++, pkg:tipp.pkg]
        }
      }

      // Find all titles
      platformInstance.tipps.each{ tipp ->
        if ( !titles.keySet().contains(tipp.title.id) ) {
          title_list.add([title:tipp.title])
          titles[tipp.title.id] = [:]
        }
      }

      title_list.sort{it.title.title}
      title_list.each { t ->
        // log.debug("Add title ${t.title.title}")
        t.position = title_count
        titles[t.title.id].position = title_count++
      }

      def crosstab = new Object[title_list.size()][package_list.size()]

      // Now iterate through all tipps, puttint them in the right cell
      platformInstance.tipps.each{ tipp ->
        int pkg_col = packages[tipp.pkg.id].position
        int title_row = titles[tipp.title.id].position
        if ( crosstab[title_row][pkg_col] != null ) {
          log.error("Warning - already a value in this cell.. it needs to be a list!!!!!");
        }
        else {
          crosstab[title_row][pkg_col] = tipp;
        }
      }

      [platformInstance: platformInstance, packages:package_list, crosstab:crosstab, titles:title_list, editable: editable, tipps: plattformTipps]
      */



        List currentSubIds = orgTypeService.getCurrentSubscriptions(contextService.getOrg()).collect{ it.id }

        //"    SubscriptionPackage subPkg join subPkg.subscriptionPackageOrgAccessPoint  spoap join spoap.orgAccessPoint ap" +

        String qry = ""
        qry = "select distinct(ap) , spoap.id" +
                " from " +
                "    TitleInstancePackagePlatform tipp join tipp.platform platf join tipp.pkg pkg, " +
                "    SubscriptionPackage subPkg join subPkg.subscriptionPackageOrgAccessPoint spoap join spoap.orgAccessPoint ap " +
                " where " +
                "    subPkg.pkg = pkg and "+
                "    platf.id =  (:platformId) and " +
                "    subPkg.subscription.id in (:currentSubIds)"

        def qryParams = [
                platformId : platformInstance.id,
                currentSubIds : currentSubIds
        ]
        //List orgAccessPointList = OrgAccessPoint.executeQuery(qry, qryParams)

        //Map<String, Object> result = [:]
        def selectedInstitution = contextService.getOrg()

        def authorizedOrgs = contextService.getUser().getAuthorizedOrgs()
        def hql = "select oapl from OrgAccessPointLink oapl join oapl.oap as ap "
        hql += "where ap.org =:institution and oapl.active=true and oapl.platform.id=${platformInstance.id}"
        List orgAccessPointList = OrgAccessPointLink.executeQuery(hql,[institution : selectedInstitution])

        def notActiveAPLinkQuery = "select oap from OrgAccessPoint oap where oap.org =:institution "
        notActiveAPLinkQuery += "and not exists ("
        notActiveAPLinkQuery += "select 1 from oap.oapp as oapl where oapl.oap=oap and oapl.active=true "
        notActiveAPLinkQuery += "and oapl.platform.id = ${platformInstance.id}) order by lower(oap.name)"

        def accessPointList = OrgAccessPoint.executeQuery(notActiveAPLinkQuery, [institution : selectedInstitution])

        [platformInstance: platformInstance, editable: editable, orgAccessPointList: orgAccessPointList, accessPointList: accessPointList,
         institution: authorizedOrgs, selectedInstitution: selectedInstitution.id
        ]

    }
    @Secured(['ROLE_USER'])
    def platformTipps() {
        def editable
        def platformInstance = Platform.get(params.id)
        if (!platformInstance) {
            flash.message = message(code: 'default.not.found.message',
                    args: [message(code: 'platform.label', default: 'Platform'), params.id])
            redirect action: 'list'
            return
        }

        editable = SpringSecurityUtils.ifAllGranted('ROLE_ADMIN')
        Map result = [platformInstance: platformInstance, editable: editable, user: springSecurityService.getCurrentUser()]

        result.max = params.max ? Integer.parseInt(params.max) : result.user.getDefaultPageSizeTMP().intValue()
        params.max = result.max
        result.offset = params.offset ? Integer.parseInt(params.offset) : 0

        def qry_params = [platInstance: platformInstance]
        def date_filter = params.mode == 'advanced' ? null : new Date()
        def query = filterService.generateBasePackageQuery(params, qry_params, false, date_filter, "Platform")
        List<TitleInstancePackagePlatform> platformTipps = TitleInstancePackagePlatform.executeQuery("select tipp ${query.base_qry}",query.qry_params)

        result.countTipps = platformTipps.size()
        result.tipps = platformTipps.drop(result.offset).take(result.max)

        result
    }

    //@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    //@Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    @Secured(['ROLE_ADMIN'])
    def edit() {
    switch (request.method) {
    case 'GET':
          def platformInstance = Platform.get(params.id)
          if (!platformInstance) {
              flash.message = message(code: 'default.not.found.message', args: [message(code: 'platform.label', default: 'Platform'), params.id])
              redirect action: 'list'
              return
          }

          [platformInstance: platformInstance]
      break
    case 'POST':
          def platformInstance = Platform.get(params.id)
          if (!platformInstance) {
              flash.message = message(code: 'default.not.found.message', args: [message(code: 'platform.label', default: 'Platform'), params.id])
              redirect action: 'list'
              return
          }

          if (params.version) {
              def version = params.version.toLong()
              if (platformInstance.version > version) {
                  platformInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
                            [message(code: 'platform.label', default: 'Platform')] as Object[],
                            "Another user has updated this Platform while you were editing")
                  render view: 'edit', model: [platformInstance: platformInstance]
                  return
              }
          }

          platformInstance.properties = params

          if (!platformInstance.save(flush: true)) {
              render view: 'edit', model: [platformInstance: platformInstance]
              return
          }

      flash.message = message(code: 'default.updated.message', args: [message(code: 'platform.label', default: 'Platform'), platformInstance.id])
          redirect action: 'show', id: platformInstance.id
      break
    }
    }

    //@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    //@Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    @Secured(['ROLE_ADMIN'])
    def delete() {
        def platformInstance = Platform.get(params.id)
        if (!platformInstance) {
      flash.message = message(code: 'default.not.found.message', args: [message(code: 'platform.label', default: 'Platform'), params.id])
            redirect action: 'list'
            return
        }

        try {
            platformInstance.delete(flush: true)
      flash.message = message(code: 'default.deleted.message', args: [message(code: 'platform.label', default: 'Platform'), params.id])
            redirect action: 'list'
        }
        catch (DataIntegrityViolationException e) {
      flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'platform.label', default: 'Platform'), params.id])
            redirect action: 'show', id: params.id
        }
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def accessMethods() {
        def editable
        def platformInstance = Platform.get(params.id)
        if (!platformInstance) {
            flash.message = message(code: 'default.not.found.message',
                    args: [message(code: 'platform.label', default: 'Platform'), params.id])
            redirect action: 'list'
            return
        }

        def platformAccessMethodList = PlatformAccessMethod.findAllByPlatf(platformInstance, [sort: ["accessMethod": 'asc', "validFrom" : 'asc']])

        [platformInstance: platformInstance, platformAccessMethodList: platformAccessMethodList, editable: editable, params: params]
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def link() {
        Map<String, Object> result = [:]
        def platformInstance = Platform.get(params.id)
        if (!platformInstance) {
            flash.message = message(code: 'default.not.found.message',
                args: [message(code: 'platform.label', default: 'Platform'), params.id])
            redirect action: 'list'
            return
        }
        def selectedInstitution = contextService.getOrg()

        def authorizedOrgs = contextService.getUser().getAuthorizedOrgs()
        def hql = "select oapl from OrgAccessPointLink oapl join oapl.oap as ap "
            hql += "where ap.org =:institution and oapl.active=true and oapl.platform.id=${platformInstance.id}"
        def results = OrgAccessPointLink.executeQuery(hql,[institution : selectedInstitution])

        def notActiveAPLinkQuery = "select oap from OrgAccessPoint oap where oap.org =:institution "
            notActiveAPLinkQuery += "and not exists ("
            notActiveAPLinkQuery += "select 1 from oap.oapp as oapl where oapl.oap=oap and oapl.active=true "
            notActiveAPLinkQuery += "and oapl.platform.id = ${platformInstance.id})"

        def accessPointList = OrgAccessPoint.executeQuery(notActiveAPLinkQuery, [institution : selectedInstitution])

        result.accessPointLinks = results
        result.platformInstance = platformInstance
        result.institution = authorizedOrgs
        result.accessPointList = accessPointList
        result.selectedInstitution = selectedInstitution.id
        result
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def dynamicApLink(){
        Map<String, Object> result = [:]
        def platformInstance = Platform.get(params.platform_id)
        if (!platformInstance) {
            flash.message = message(code: 'default.not.found.message',
                args: [message(code: 'platform.label', default: 'Platform'), params.platform_id])
            redirect action: 'list'
            return
        }
        def authorizedOrgs = contextService.getUser().getAuthorizedOrgs()
        def selectedInstitution =  contextService.getOrg()
        if (params.institution_id){
            selectedInstitution = Org.get(params.institution_id)
        }
        def hql = "select oapl from OrgAccessPointLink oapl join oapl.oap as ap "
        hql += "where ap.org =:institution and oapl.active=true and oapl.platform.id=${platformInstance.id}"
        def results = OrgAccessPointLink.executeQuery(hql,[institution : selectedInstitution])
        def notActiveAPLinkQuery = "select oap from OrgAccessPoint oap where oap.org =:institution "
        notActiveAPLinkQuery += "and not exists ("
        notActiveAPLinkQuery += "select 1 from oap.oapp as oapl where oapl.oap=oap and oapl.active=true "
        notActiveAPLinkQuery += "and oapl.platform.id = ${platformInstance.id})"

        def accessPointList = OrgAccessPoint.executeQuery(notActiveAPLinkQuery, [institution : selectedInstitution])

        result.accessPointLinks = results
        result.platformInstance = platformInstance
        result.institution = authorizedOrgs
        result.accessPointList = accessPointList
        result.selectedInstitution = selectedInstitution.id
        render(view: "_apLinkContent", model: result)
    }

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
        def hql = "delete from OrgAccessPointLink oapl where oapl.platform=:platform_id and oapl.subPkg =:subPkg and oapl.active=true"
        def oapl = OrgAccessPointLink.executeUpdate(hql, [platform_id:platform, subPkg:subPkg])

        redirect(url: request.getHeader('referer'))
    }
    
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
        oapl.save()
        redirect(url: request.getHeader('referer'))
    }

    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def linkAccessPoint() {
        def apInstance = null
        if (params.AccessPoints){
            apInstance = OrgAccessPoint.get(params.AccessPoints)
            if (!apInstance) {
                flash.error = 'No valid Accesspoint id given'
                redirect action: 'link', params: [id:params.id]
                return
            }
        }
        // save link
        def oapl = new OrgAccessPointLink()
        oapl.active = true
        oapl.oap = apInstance
        oapl.platform = Platform.get(params.platform_id)
        if (params.subscriptionPackage_id){
            def sp = SubscriptionPackage.get(params.subscriptionPackage_id)
            if (sp) {
                oapl.subPkg = sp
            }
        }
        def existingActiveAP = OrgAccessPointLink.findAllByActiveAndPlatformAndOapAndSubPkgIsNotNull(
            true, oapl.platform, apInstance
        )
        if (!existingActiveAP.isEmpty()){
            flash.error = "Existing active AccessPoint for platform"
            redirect(url: request.getHeader('referer'))
            return
        }
        if (! oapl.save()) {
            flash.error = "Existing active AccessPoint for platform"
        }

        redirect(url: request.getHeader('referer'))
    }

    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_BASIC_MEMBER,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def removeAccessPoint() {
        // update active aopl, set active=false
        def aoplInstance = OrgAccessPointLink.get(params.oapl_id)
        aoplInstance.active = false
        if (! aoplInstance.save()) {
            log.debug("Error updateing AccessPoint for platform")
            log.debug(aopl.errors)
            // TODO flash
        }
        redirect action: 'link', params: [id:params.id]
    }

    private Map setResultGenerics() {
        Map<String, Object> result = [:]
        result.user = User.get(springSecurityService.principal.id)
        result.orgInstance = contextService.org
        result
    }

}
