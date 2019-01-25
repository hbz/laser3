package com.k_int.kbplus

import com.k_int.properties.PropertyDefinitionGroup
import com.k_int.properties.PropertyDefinitionGroupBinding
import de.laser.controller.AbstractDebugController
import de.laser.domain.I10nTranslation
import de.laser.helper.DebugAnnotation
import de.laser.helper.RDStore
import grails.plugin.springsecurity.annotation.Secured
import com.k_int.kbplus.auth.User
import org.jfree.ui.about.Licences
import org.springframework.context.i18n.LocaleContextHolder

@Secured(['IS_AUTHENTICATED_FULLY'])
class LicenseCompareController extends AbstractDebugController {

    def springSecurityService
    def exportService
    def contextService
    def controlledListService
    def genericOIDService

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
  Map compare(){
    LinkedHashMap result = [groupedProperties:[:],orphanedProperties:[:]]
    Org org = contextService.getOrg()
    List licenses = params.availableLicenses.split(',').collect{
      genericOIDService.resolveOID(it)
    }
    licenses.each{ lic ->
      /*
        Back to square one:
        I need the following groupings:

        global
        local
        member
        orphaned

        global, local and member are group groups while orphaned is just a list (so already on level three)

        global/local/member are maps, consider example for licenses 51 vs. 57:

        Intended:
        groupedProperties {
          Archivkopie {
            Archivkopie: Kosten{51: {value: null,binding: ?}, 57: {value: Free, binding: ?}},
            Archivkopie: Form{51: {value: null,binding: ?}, 57: {value: Data, binding: ?}},
            Archivkopie: Recht{51: {value: null,binding: ?}, 57: [value: Yes, binding: ?}}
          }
          Gerichtsstand {
            Signed{51: {value: Yes,binding: ?}, 57: {value: Yes, binding: ?}},
            Anzuwendes Recht{51: {value: Dt. Recht, binidng: ?}, 57: {value: null, binding: ?}},
            Gerichtsstand{51: {value: Berlin, binding: ?}, 57: {value: null, binidng: ?}}
          }
        }
      */
      def allPropDefGroups = lic.getCalculatedPropDefGroups(org)
      allPropDefGroups.entrySet().each { propDefGroupWrapper ->
        /*
          group group level
          There are: global, local, member (consortium@subscriber) property *groups* and orphaned *properties* which is ONE group
         */
        switch(propDefGroupWrapper.getKey()) {
          case "global":
          case "local":
          case "member":
            /*
              group level
              Each group may have different property groups
            */
            LinkedHashMap groupedProperties = result.groupedProperties
            propDefGroupWrapper.getValue().each { propDefGroup ->
              PropertyDefinitionGroup groupKey
              PropertyDefinitionGroupBinding groupBinding
              //distinct between propDefGroup objects who have a binding and those who do not
              if(propDefGroup instanceof List) {
                println "processing local or member scope"
                try {
                  groupKey = (PropertyDefinitionGroup) propDefGroup.get(0)
                  groupBinding = (PropertyDefinitionGroupBinding) propDefGroup.get(1)
                }
                catch (ClassCastException e) {
                  log.error("Erroneous values in calculated property definition group! Stack trace as follows:")
                  e.printStackTrace()
                }
              }
              else if(propDefGroup instanceof PropertyDefinitionGroup) {
                println "processing global scope"
                groupKey = propDefGroup
                groupBinding = null
              }
              //check if there is already a mapping for the current property definition group
              def group = groupedProperties.get(propDefGroup)
              if(group == null) {
                group = new TreeMap()
              }
              //get the current properties within each group for each license
              ArrayList<LicenseCustomProperty> licenceProps = groupKey.getCurrentProperties(lic)
              licenceProps.each { prop ->
                //property level - check if the group contains already a mapping for the current property
                def propertyMap = group.get(prop.type.getI10n("name"))
                if(propertyMap == null)
                  propertyMap = [:]
                propertyMap.put(lic,[prop:prop,binding:groupBinding])
                group.put(prop.type.getI10n("name"),propertyMap)
              }
              groupedProperties[propDefGroup] = group
            }
            result.groupedProperties = groupedProperties
          break
          case "orphanedProperties":
            LinkedHashMap orphanedProperties = result.orphanedProperties
            propDefGroupWrapper.getValue().each { prop ->
              //property level - check if the group contains already a mapping for the current property
              def propertyMap = orphanedProperties.get(prop.type.getI10n("name"))
              if(propertyMap == null)
                propertyMap = [:]
              propertyMap.put(lic,[prop:prop,binding:null])
              orphanedProperties.put(prop.type.getI10n("name"),propertyMap)
            }
            result.orphanedProperties = orphanedProperties
          break
        }
      }
    }
    result.licenses = licenses
    result.institution = org
    String filename = "license_compare_${result.institution.name}"
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