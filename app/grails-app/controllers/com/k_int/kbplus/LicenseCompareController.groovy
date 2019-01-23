package com.k_int.kbplus

import de.laser.controller.AbstractDebugController
import de.laser.helper.DebugAnnotation
import de.laser.helper.RDStore
import grails.plugin.springsecurity.annotation.Secured
import com.k_int.kbplus.auth.User

@Secured(['IS_AUTHENTICATED_FULLY'])
class LicenseCompareController extends AbstractDebugController {

    def springSecurityService
    def exportService
    def contextService
    def controlledListService

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
  def index() {
        def result = [:]
        result.user = User.get(springSecurityService.principal.id)
        result.institution = contextService.getOrg()
        result.availableLicenses = controlledListService.getLicenses(params)
        result
  }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
  def compare(){
    log.debug("compare ${params}")
    def result = [:]
    result.institution = contextService.getOrg()
    def licenses = params.list("availableLicenses").collect{
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