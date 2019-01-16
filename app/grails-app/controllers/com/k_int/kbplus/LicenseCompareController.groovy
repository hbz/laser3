package com.k_int.kbplus

import de.laser.controller.AbstractDebugController
import de.laser.helper.DebugAnnotation
import de.laser.helper.RDStore
import grails.plugin.springsecurity.annotation.Secured
import com.k_int.kbplus.auth.User

@Secured(['IS_AUTHENTICATED_FULLY'])
class LicenseCompareController extends AbstractDebugController {
  
    static String INSTITUTIONAL_LICENSES_QUERY = " from License as l where exists ( select ol from OrgRole as ol where ol.lic = l AND ol.org = ? and ol.roleType = ? ) AND l.status.value != 'Deleted'"
    def springSecurityService
    def exportService
    def accessService
    def contextService

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
  def index() {
        def result = [:]
        result.user = User.get(springSecurityService.principal.id)
        //result.institution = Org.findByShortcode(params.shortcode)
        result.institution = contextService.getOrg()

        if (! accessService.checkUserIsMember(result.user, result.institution)) {
            flash.error = "You do not have permission to view ${result.institution.name}. Please request access on the profile page";
            response.sendError(401)
            return;
        }

        // TODO: find by 'Licensee' AND 'Licensee_Consortial'
        // ajax/lookup
        // License.refdataFind()

        result.isPublic = RefdataValue.getByValueAndCategory('Yes','YN')

        result.licensee_role = RDStore.OR_LICENSEE
        result.licensee_cons_role = RDStore.OR_LICENSEE_CONS
        result.licensing_cons_role = RDStore.OR_LICENSING_CONSORTIUM

        result
  }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
  def compare(){
    log.debug("compare ${params}")
    def result = [:]
    result.institution = Org.get(params.institution)

    def licenses = params.list("selectedLicenses").collect{
      License.get(it.toLong())
    }
    log.debug("IDS: ${licenses}")
    def comparisonMap = new TreeMap()
    licenses.each{ lic ->
      lic.customProperties.each{prop ->
        def point = [:]
        if(prop.getValue()|| prop.getNote()){
          point.put(lic.reference,prop)
          if(comparisonMap.containsKey(prop.type.name)){
            comparisonMap[prop.type.name].putAll(point)
          }else{
            comparisonMap.put(prop.type.name,point)
          }
        }
      }
    }
    result.map = comparisonMap
    result.licenses = licenses
    def filename = "license_compare_${result.institution.name}"
  	withFormat{
      html result
      csv{
        response.setHeader("Content-disposition", "attachment; filename=\"${filename}.csv\"")
        response.contentType = "text/csv"
        def out = response.outputStream
        exportService.StreamOutLicenseCSV(out, result,result.licenses)
        out.close()
      }
    }

  }
}