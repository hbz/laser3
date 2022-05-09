package de.laser


import de.laser.auth.User
import de.laser.helper.ConfigMapper
import de.laser.helper.SwissKnife
import de.laser.properties.PlatformProperty
import de.laser.properties.PropertyDefinition
import de.laser.annotations.DebugInfo
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.transaction.TransactionStatus

/**
 * This controller handles issue entitlement detail calls
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class IssueEntitlementController  {

    ContextService contextService
    FactService factService

   static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    /**
     * @deprecated The method {@link SubscriptionController#processAddEntitlements()} does the entitlement adding;
     * use {@link SubscriptionController#addEntitlements()} resp. {@link SubscriptionService#issueEntitlementEnrichment(java.io.InputStream, java.util.Set, de.laser.Subscription, boolean, boolean)}
     * instead
     */
    @Deprecated
    @DebugInfo(test='hasAffiliation("INST_EDITOR")', wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def create() {
        redirect controller: 'issueEntitlement', action: 'show', params: params
    }

    /**
     * Shows the given issue entitlement details
     * @return
     */
    @DebugInfo(test = 'hasAffiliation("INST_USER")', wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def show() {
      Map<String, Object> result = [:]

      result.user = contextService.getUser()
      result.issueEntitlementInstance = IssueEntitlement.get(params.id)
      result.sub = result.issueEntitlementInstance.subscription

      if(result.sub.getSubscriber().id == contextService.getOrg().id || result.sub.getConsortia().id == contextService.getOrg().id){
          result.isMySub = true
      }

      params.max = Math.min(params.max ? params.int('max') : 10, 100)

      SwissKnife.setPaginationParams(result, params, (User) result.user)

      result.editable = result.issueEntitlementInstance.subscription.isEditableBy(result.user)

      // Get usage statistics
      def title_id = result.issueEntitlementInstance.tipp.id
      def org = result.issueEntitlementInstance.subscription.getSubscriber() // TODO
      def supplier =  result.issueEntitlementInstance.tipp.platform
      def supplier_id = supplier?.id

      if (title_id != null &&
           org != null &&
           supplier_id != null && ConfigMapper.getShowStatsInfo()) {
          def platform = PlatformProperty.findByOwnerAndType(Platform.get(supplier_id),
              PropertyDefinition.getByNameAndDescr('NatStat Supplier ID', PropertyDefinition.PLA_PROP))
          result.natStatSupplierId = platform?.stringValue ?: null
          def fsresult = factService.generateUsageData(org.id, supplier_id, result.issueEntitlementInstance.subscription, title_id)
          def fsLicenseResult = factService.generateUsageDataForSubscriptionPeriod(org.id, supplier_id, result.issueEntitlementInstance.subscription, title_id)
          result.institutional_usage_identifier = OrgSetting.get(org, OrgSetting.KEYS.NATSTAT_SERVER_REQUESTOR_ID)
          if (result.institutional_usage_identifier instanceof OrgSetting && fsresult.usage) {
              result.statsWibid = org.getIdentifierByType('wibid')?.value
              result.usageMode = org.hasPerm("ORG_CONSORTIUM") ? 'package' : 'institution'
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

      if (!result.issueEntitlementInstance) {
        flash.message = message(code: 'default.not.found.message', args: [message(code: 'issueEntitlement.label'), params.id])
        redirect action: 'list'
        return
      }

     /* String base_qry = "from TitleInstancePackagePlatform as tipp where tipp = :tipp and tipp.status != :status"
      Map<String,Object> qry_params = [tipp:result.issueEntitlementInstance.tipp,status:RDStore.TIPP_STATUS_DELETED]

      if ( params.filter ) {
        base_qry += " and genfunc_filter_matcher(tipp.pkg.name,:pkgName) = true "
        qry_params.put("pkgName","%${params.filter.trim().toLowerCase()}%")
      }

      if ( params.endsAfter && params.endsAfter.length() > 0 ) {
        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
        Date d = sdf.parse(params.endsAfter)
        base_qry += " and (select max(tc.endDate) from TIPPCoverage tc where tc.tipp = tipp) >= :maxDate"
        qry_params.put("maxDate",d)
      }

      if ( params.startsBefore && params.startsBefore.length() > 0 ) {
          SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
          Date d = sdf.parse(params.startsBefore)
        base_qry += " and (select min(tc.startDate) from TIPPCoverage tc where tc.tipp = tipp) <= :minDate"
        qry_params.add("minDate",d)
      }

      if ( ( params.sort != null ) && ( params.sort.length() > 0 ) ) {
        base_qry += " order by lower(${params.sort}) ${params.order}"
      }
      else {
        base_qry += " order by lower(tipp.name) asc"
      }*/

      // log.debug("Base qry: ${base_qry}, params: ${qry_params}, result:${result}");
      // result.tippList = TitleInstancePackagePlatform.executeQuery("select tipp "+base_qry, qry_params, [max:result.max, offset:result.offset]);
      // DMs report that this list is limited to 10
      //result.tippList = TitleInstancePackagePlatform.executeQuery("select tipp "+base_qry, qry_params, [max:300, offset:0]);
      //result.num_tipp_rows = TitleInstancePackagePlatform.executeQuery("select tipp.id "+base_qry, qry_params ).size()

      result

    }

    @DebugInfo(test='hasAffiliation("INST_EDITOR")', wtc = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def edit() {
        redirect controller: 'issueEntitlement', action: 'show', params: params
    }

    @DebugInfo(test='hasAffiliation("INST_EDITOR")', wtc = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def delete() {
        IssueEntitlement.withTransaction { TransactionStatus ts ->
            IssueEntitlement issueEntitlementInstance = IssueEntitlement.get(params.id)
            if (!issueEntitlementInstance) {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'issueEntitlement.label'), params.id])
                redirect action: 'list'
                return
            }
            try {
                issueEntitlementInstance.delete()
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'issueEntitlement.label'), params.id])
                redirect action: 'list'
                return
            }
            catch (DataIntegrityViolationException e) {
                flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'issueEntitlement.label'), params.id])
                redirect action: 'show', id: params.id
                return
            }
        }

    }
}
