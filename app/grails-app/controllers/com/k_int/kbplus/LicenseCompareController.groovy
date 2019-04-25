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
    def comparisonService

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
  def index() {
        def result = [:]
        result.user = User.get(springSecurityService.principal.id)
        result.institution = contextService.getOrg()
        result.availableLicenses = controlledListService.getLicenses(params)
        result
  }

  @DebugAnnotation(perm="ORG_BASIC,ORG_CONSORTIUM", affil="INST_USER")
  @Secured(closure = {
    ctx.accessService.checkPermAffiliation("ORG_BASIC,ORG_CONSORTIUM", "INST_USER")
  })
  Map compare(){
    LinkedHashMap result = [groupedProperties:[:],orphanedProperties:[:],privateProperties:[:]]
    Org org = contextService.getOrg()
    List licKeys = params.availableLicenses.split(",")
    List licenses = licKeys.collect{ it ->
      (License) genericOIDService.resolveOID(it)
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
            Archivkopie: Kosten{51: null, 57: Free},
            Archivkopie: Form{51: null, 57: Data},
            Archivkopie: Recht{51: null, 57: Yes}
          }, binding: ?
          Gerichtsstand {
            Signed{51: Yes, 57: Yes},
            Anzuwendes Recht{51: Dt. Recht, 57: null},
            Gerichtsstand{51: Berlin, 57: null}
          }, binding: ?
        },
        orphanedProperties {
          ...
        },
        privateProperties {
          ...
        }
      */
      def allPropDefGroups = lic.getCalculatedPropDefGroups(org)
      allPropDefGroups.entrySet().each { propDefGroupWrapper ->
        /*
          group group level
          There are: global, local, member (consortium@subscriber) property *groups* and orphaned *properties* which is ONE group
         */
        String wrapperKey = propDefGroupWrapper.getKey()
        if(wrapperKey.equals("orphanedProperties")) {
          TreeMap orphanedProperties = result.orphanedProperties
          orphanedProperties = comparisonService.buildComparisonTree(orphanedProperties,lic,propDefGroupWrapper.getValue())
          result.orphanedProperties = orphanedProperties
        }
        else {
          LinkedHashMap groupedProperties = result.groupedProperties
          /*
            group level
            Each group may have different property groups
          */
          propDefGroupWrapper.getValue().each { propDefGroup ->
            PropertyDefinitionGroup groupKey
            PropertyDefinitionGroupBinding groupBinding
            switch(wrapperKey) {
              case "global":
                groupKey = (PropertyDefinitionGroup) propDefGroup
                if(groupKey.visible == RDStore.YN_YES)
                  groupedProperties.put(groupKey,comparisonService.getGroupedPropertyTrees(groupedProperties,groupKey,null,lic))
                break
              case "local":
                try {
                  groupKey = (PropertyDefinitionGroup) propDefGroup.get(0)
                  groupBinding = (PropertyDefinitionGroupBinding) propDefGroup.get(1)
                  if(groupBinding.visible == RDStore.YN_YES) {
                    groupedProperties.put(groupKey,comparisonService.getGroupedPropertyTrees(groupedProperties,groupKey,groupBinding,lic))
                  }
                }
                catch (ClassCastException e) {
                  log.error("Erroneous values in calculated property definition group! Stack trace as follows:")
                  e.printStackTrace()
                }
                break
              case "member":
                try {
                  groupKey = (PropertyDefinitionGroup) propDefGroup.get(0)
                  groupBinding = (PropertyDefinitionGroupBinding) propDefGroup.get(1)
                  if(groupBinding.visible == RDStore.YN_YES && groupBinding.visibleForConsortiaMembers == RDStore.YN_YES) {
                    groupedProperties.put(groupKey,comparisonService.getGroupedPropertyTrees(groupedProperties,groupKey,groupBinding,lic))
                  }
                }
                catch (ClassCastException e) {
                  log.error("Erroneous values in calculated property definition group! Stack trace as follows:")
                  e.printStackTrace()
                }
                break
            }
          }
          result.groupedProperties = groupedProperties
        }
      }
      TreeMap privateProperties = result.privateProperties
      privateProperties = comparisonService.buildComparisonTree(privateProperties,lic,lic.privateProperties)
      result.privateProperties = privateProperties
    }
    result.licenses = licenses
    result.institution = org
    String filename = "license_compare_${result.institution.name}"
  	withFormat{
      html result
      csv{
        /*
        done when processing ERMS-998
        response.setHeader("Content-disposition", "attachment; filename=\"${filename}.csv\"")
        response.contentType = "text/csv"
        def out = response.outputStream
        exportService.StreamOutLicenseCSV(out, result,result.licenses)
        out.close()*/
      }
    }
  }

}