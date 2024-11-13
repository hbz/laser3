package de.laser

import de.laser.annotations.Check404
import de.laser.auth.User
import de.laser.config.ConfigMapper
import de.laser.utils.SwissKnife
import de.laser.annotations.DebugInfo
import de.laser.wekb.Platform
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.transaction.TransactionStatus

/**
 * This controller handles issue entitlement detail calls
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class IssueEntitlementController {

    ContextService contextService
    FactService factService
    SurveyService surveyService

    //-----

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    public static final Map<String, String> CHECK404_ALTERNATIVES = [ : ]

    //-----

    /**
     * Shows the given issue entitlement details
     */
    @DebugInfo(isInstUser_denySupport = [], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport()
    })
    @Check404()
    def show() {
      Map<String, Object> result = [:]

      result.user = contextService.getUser()
      result.issueEntitlementInstance = IssueEntitlement.get(params.id)
      result.sub = result.issueEntitlementInstance.subscription

      if(result.sub.getSubscriberRespConsortia().id == contextService.getOrg().id || (result.sub.getConsortium() && result.sub.getConsortium().id == contextService.getOrg().id)){
          result.isMySub = true
      }

      params.max = Math.min(params.max ? params.int('max') : 10, 100)

      SwissKnife.setPaginationParams(result, params, (User) result.user)

      result.editable = result.issueEntitlementInstance.subscription.isEditableBy(result.user)

      // Get usage statistics
      Long title_id = result.issueEntitlementInstance.tipp.id
      Org org = result.issueEntitlementInstance.subscription.getSubscriberRespConsortia() // TODO
      Platform supplier =  result.issueEntitlementInstance.tipp.platform
      Long supplier_id = supplier?.id

      if (title_id != null &&
           org != null &&
           supplier_id != null && ConfigMapper.getShowStatsInfo()) {
          result.natStatSupplierId = supplier.natstatSupplierID
          def fsresult = factService.generateUsageData(org.id, supplier_id, result.issueEntitlementInstance.subscription, title_id)
          def fsLicenseResult = factService.generateUsageDataForSubscriptionPeriod(org.id, supplier_id, result.issueEntitlementInstance.subscription, title_id)
          result.institutional_usage_identifier = OrgSetting.get(org, OrgSetting.KEYS.NATSTAT_SERVER_REQUESTOR_ID)
          if (result.institutional_usage_identifier instanceof OrgSetting && fsresult.usage) {
              result.statsWibid = org.getIdentifierByType('wibid')?.value
              result.usageMode = org.isCustomerType_Consortium() ? 'package' : 'institution'
              result.usage = fsresult?.usage
              result.x_axis_labels = fsresult?.x_axis_labels
              result.y_axis_labels = fsresult?.y_axis_labels
              if (fsLicenseResult.usage) {
                  result.lusage = fsLicenseResult?.usage
                  result.l_x_axis_labels = fsLicenseResult?.x_axis_labels
                  result.l_y_axis_labels = fsLicenseResult?.y_axis_labels
              }
          }
      }

        result.participantPerpetualAccessToTitle = []

        result.participantPerpetualAccessToTitle = surveyService.listParticipantPerpetualAccessToTitle(result.issueEntitlementInstance.subscription.getSubscriberRespConsortia(), result.issueEntitlementInstance.tipp)

      result
    }

    @DebugInfo(isInstEditor_denySupport = [], withTransaction = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
    @Deprecated
    def delete() {
        IssueEntitlement.withTransaction { TransactionStatus ts ->
            IssueEntitlement issueEntitlementInstance = IssueEntitlement.get(params.id)
            if (!issueEntitlementInstance) {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'issueEntitlement.label'), params.id]) as String
                redirect action: 'list'
                return
            }
            try {
                issueEntitlementInstance.delete()
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'issueEntitlement.label'), params.id]) as String
                redirect action: 'list'
                return
            }
            catch (DataIntegrityViolationException e) {
                flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'issueEntitlement.label'), params.id]) as String
                redirect action: 'show', id: params.id
                return
            }
        }

    }
}
