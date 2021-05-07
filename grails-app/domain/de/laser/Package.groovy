package de.laser

import de.laser.exceptions.CreationException
import de.laser.finance.CostItem
import de.laser.finance.PriceItem
import de.laser.oap.OrgAccessPointLink
import de.laser.base.AbstractBaseWithCalculatedLastUpdated
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.annotations.RefdataAnnotation
import de.laser.interfaces.ShareSupport
import de.laser.traits.ShareableTrait
import grails.converters.JSON
import grails.web.servlet.mvc.GrailsParameterMap
import org.codehaus.groovy.runtime.InvokerHelper

import javax.persistence.Transient
import java.text.Normalizer
import java.text.SimpleDateFormat

class Package extends AbstractBaseWithCalculatedLastUpdated {

    def accessService

    String name
    String sortName
    String gokbId
    String vendorURL
    String cancellationAllowances

    @RefdataAnnotation(cat = RDConstants.PACKAGE_CONTENT_TYPE)
    RefdataValue contentType

    @RefdataAnnotation(cat = RDConstants.PACKAGE_STATUS)
    RefdataValue packageStatus

    @RefdataAnnotation(cat = RDConstants.PACKAGE_BREAKABLE)
    RefdataValue breakable

    @RefdataAnnotation(cat = RDConstants.PACKAGE_CONSISTENT)
    RefdataValue consistent

    @RefdataAnnotation(cat = RDConstants.PACKAGE_FILE)
    RefdataValue file

    @RefdataAnnotation(cat = RDConstants.PACKAGE_SCOPE)
    RefdataValue scope

    boolean isPublic = false

    Platform nominalPlatform
    Date startDate
    Date endDate
    Set pendingChanges
    SortedSet ids
    SortedSet ddcs
    Boolean autoAccept = false

    Date dateCreated
    Date lastUpdated
    Date lastUpdatedCascading

static hasMany = [  tipps:     TitleInstancePackagePlatform,
                    orgs:      OrgRole,
                    prsLinks:  PersonRole,
                    documents: DocContext,
                    subscriptions:  SubscriptionPackage,
                    pendingChanges: PendingChange,
                    ids: Identifier,
                    ddcs: DeweyDecimalClassification]

  static mappedBy = [tipps:     'pkg',
                     orgs:      'pkg',
                     prsLinks:  'pkg',
                     documents: 'pkg',
                     subscriptions: 'pkg',
                     pendingChanges: 'pkg',
                     ids:       'pkg',
                     ddcs:      'pkg']


  static mapping = {
                    sort sortName: 'asc'
                      id column:'pkg_id'
                 version column:'pkg_version'
               globalUID column:'pkg_guid'
                    name column:'pkg_name'
                sortName column:'pkg_sort_name'
                  gokbId column:'pkg_gokb_id'
             contentType column:'pkg_content_type_rv_fk'
           packageStatus column:'pkg_status_rv_fk'
                    file column:'pkg_file_rv_fk'
               breakable column:'pkg_breakable_rv_fk'
              consistent column:'pkg_consistent_rv_fk'
         nominalPlatform column:'pkg_nominal_platform_fk'
               startDate column:'pkg_start_date',   index:'pkg_dates_idx'
                 endDate column:'pkg_end_date',     index:'pkg_dates_idx'
                isPublic column:'pkg_is_public'
            scope column:'pkg_scope_rv_fk'
               vendorURL column:'pkg_vendor_url'
  cancellationAllowances column:'pkg_cancellation_allowances', type:'text'
                     tipps batchSize: 10
            pendingChanges sort:'ts', order: 'asc', batchSize: 10

        lastUpdatedCascading column: 'pkg_last_updated_cascading'

            orgs            batchSize: 10
            prsLinks        batchSize: 10
            documents       batchSize: 10
            subscriptions   batchSize: 10
            ids             sort: 'ns', batchSize: 10
            ddcs            batchSize: 10
  }

  static constraints = {
                 globalUID(nullable:true, blank:false, unique:true, maxSize:255)
               contentType (nullable:true)
             packageStatus (nullable:true)
           nominalPlatform (nullable:true)
                 breakable (nullable:true)
                consistent (nullable:true)
                 startDate (nullable:true)
                   endDate (nullable:true)
                     scope (nullable:true)
                    gokbId(blank:false, unique: true, maxSize: 511)
                 vendorURL(nullable:true, blank:false)
    cancellationAllowances(nullable:true, blank:false)
                  sortName(nullable:true, blank:false)
      lastUpdatedCascading (nullable:true)
  }

    @Override
    def afterDelete() {
        super.afterDeleteHandler()

        //deletionService.deleteDocumentFromIndex(this.globalUID) ES not connected, reactivate as soon as ES works again
    }
    @Override
    def afterInsert() {
        super.afterInsertHandler()
    }
    @Override
    def afterUpdate() {
        super.afterUpdateHandler()
    }

    boolean checkSharePreconditions(ShareableTrait sharedObject) {
        false // NO SHARES
    }

    boolean showUIShareButton() {
        false // NO SHARES
    }

    void updateShare(ShareableTrait sharedObject) {
        false // NO SHARES
    }

    void syncAllShares(List<ShareSupport> targets) {
        false // NO SHARES
    }

  @Deprecated
  Org getConsortia() {
    Org result
    orgs.each { OrgRole or ->
      if ( or.roleType in [RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_PACKAGE_CONSORTIA] )
        result = or.org
    }
    result
  }

  @Transient
  Org getContentProvider() {
    Org result
    orgs.each { OrgRole or ->
      if ( or.roleType in [RDStore.OR_CONTENT_PROVIDER, RDStore.OR_PROVIDER] )
        result = or.org
    }
    result
  }

  @Transient
  void addToSubscription(subscription, createEntitlements) {
    // Add this package to the specified subscription
    // Step 1 - Make sure this package is not already attached to the sub
    // Step 2 - Connect
    List<SubscriptionPackage> dupe = SubscriptionPackage.executeQuery(
            "from SubscriptionPackage where subscription = :sub and pkg = :pkg", [sub: subscription, pkg: this])

    if (!dupe){
        new SubscriptionPackage(subscription:subscription, pkg:this).save()
      // Step 3 - If createEntitlements ...

      if ( createEntitlements ) {
          //explicit loading needed because of refreshing - after sync, GORM may be a bit behind
        TitleInstancePackagePlatform.findAllByPkg(this).each { TitleInstancePackagePlatform tipp ->
              IssueEntitlement new_ie = new IssueEntitlement(
                                              status: tipp.status,
                                              subscription: subscription,
                                              tipp: tipp,
                                              accessStartDate:tipp.accessStartDate,
                                              accessEndDate:tipp.accessEndDate,
                                              acceptStatus: RDStore.IE_ACCEPT_STATUS_FIXED)
              if(new_ie.save()) {
                  log.debug("${new_ie} saved")
                  TIPPCoverage.findAllByTipp(tipp).each { TIPPCoverage covStmt ->
                      IssueEntitlementCoverage ieCoverage = new IssueEntitlementCoverage(
                              startDate:covStmt.startDate,
                              startVolume:covStmt.startVolume,
                              startIssue:covStmt.startIssue,
                              endDate:covStmt.endDate,
                              endVolume:covStmt.endVolume,
                              endIssue:covStmt.endIssue,
                              embargo:covStmt.embargo,
                              coverageDepth:covStmt.coverageDepth,
                              coverageNote:covStmt.coverageNote,
                              issueEntitlement: new_ie
                      )
                      ieCoverage.save()
                  }
                  PriceItem.findAllByTipp(tipp).each { PriceItem pi ->
                      PriceItem localPrice = new PriceItem()
                      InvokerHelper.setProperties(localPrice, pi.properties)
                      localPrice.tipp = null
                      localPrice.globalUID = null
                      localPrice.issueEntitlement = new_ie
                      localPrice.setGlobalUID()
                      if(!localPrice.save())
                          log.error(localPrice.errors)
                  }
            }
        }
      }

    }
  }

    @Transient
    void addToSubscriptionCurrentStock(Subscription target, Subscription consortia) {

        // copy from: addToSubscription(subscription, createEntitlements) { .. }

        List<SubscriptionPackage> dupe = SubscriptionPackage.executeQuery(
                "from SubscriptionPackage where subscription = :sub and pkg = :pkg", [sub: target, pkg: this])

        if (! dupe){

            RefdataValue statusCurrent = RDStore.TIPP_STATUS_CURRENT

            new SubscriptionPackage(subscription:target, pkg:this).save()

            IssueEntitlement.executeQuery(
                "select ie from IssueEntitlement ie join ie.tipp tipp " +
                "where tipp.pkg = :pkg and ie.status = :current and ie.subscription = :consortia ", [
                      pkg: this, current: statusCurrent, consortia: consortia
            ]).each { IssueEntitlement ie ->
                IssueEntitlement newIe = new IssueEntitlement(
                        status: statusCurrent,
                        subscription: target,
                        tipp: ie.tipp,
                        accessStartDate: ie.tipp.accessStartDate,
                        accessEndDate: ie.tipp.accessEndDate,
                        acceptStatus: RDStore.IE_ACCEPT_STATUS_FIXED
                )
                if(newIe.save()) {
                    ie.tipp.coverages.each { TIPPCoverage covStmt ->
                        IssueEntitlementCoverage ieCoverage = new IssueEntitlementCoverage(
                                startDate: covStmt.startDate,
                                startVolume: covStmt.startVolume,
                                startIssue: covStmt.startIssue,
                                endDate: covStmt.endDate,
                                endVolume: covStmt.endVolume,
                                endIssue: covStmt.endIssue,
                                embargo: covStmt.embargo,
                                coverageDepth: covStmt.coverageDepth,
                                coverageNote: covStmt.coverageNote,
                                issueEntitlement: newIe
                        )
                        ieCoverage.save()
                    }
                    ie.priceItems.each { PriceItem pi ->
                        PriceItem priceItem = new PriceItem(
                                startDate: pi.startDate,
                                endDate: pi.endDate,
                                listPrice: pi.listPrice,
                                listCurrency: pi.listCurrency,
                                localPrice: pi.localPrice,
                                localCurrency: pi.localCurrency,
                                issueEntitlement: newIe
                        )
                        priceItem.save()
                    }
                }
            }
        }
    }

    @Transient
    boolean unlinkFromSubscription(Subscription subscription, Org contextOrg, deleteEntitlements) {
        SubscriptionPackage subPkg = SubscriptionPackage.findByPkgAndSubscription(this, subscription)

        //Not Exist CostItem with Package
        if(!CostItem.executeQuery('select ci from CostItem ci where ci.subPkg.subscription = :sub and ci.subPkg.pkg = :pkg and ci.owner = :context and ci.costItemStatus != :deleted',[pkg:this, deleted: RDStore.COST_ITEM_DELETED, sub:subscription, context: contextOrg])) {

            if (deleteEntitlements) {
                List<Long> subList = [subscription.id]
                Map<String,Object> queryParams = [sub: subList, pkg_id: this.id]
                //delete matches
                //IssueEntitlement.withSession { Session session ->
                    List deleteIdList = IssueEntitlement.executeQuery("select ie.id from IssueEntitlement ie, Package pkg where ie.subscription.id in (:sub) and pkg.id = :pkg_id and ie.tipp in ( select tipp from TitleInstancePackagePlatform tipp where tipp.pkg.id = :pkg_id ) ", queryParams)
                    removePackagePendingChanges(subList,true)
                    if (deleteIdList) {
                        deleteIdList.collate(32766).each { List chunk ->
                            IssueEntitlement.executeUpdate("update IssueEntitlement ie set ie.status = :delStatus where ie.id in (:delList)", [delList: chunk,delStatus:RDStore.TIPP_STATUS_DELETED])
                        }
                    }

                    SubscriptionPackage.executeQuery('select sp from SubscriptionPackage sp where sp.pkg = :pkg and sp.subscription.id in (:subList)',[subList:subList,pkg:this]).each { SubscriptionPackage delPkg ->
                        OrgAccessPointLink.executeUpdate("delete from OrgAccessPointLink oapl where oapl.subPkg = :subPkg", [subPkg:delPkg])
                        CostItem.executeUpdate('update CostItem ci set ci.costItemStatus = :deleted, ci.subPkg = null, ci.sub = :sub where ci.subPkg = :delPkg',[delPkg: delPkg, sub:delPkg.subscription, deleted: RDStore.COST_ITEM_DELETED])
                        PendingChangeConfiguration.executeUpdate("delete from PendingChangeConfiguration pcc where pcc.subscriptionPackage=:sp",[sp:delPkg])
                    }

                    SubscriptionPackage.executeUpdate("delete from SubscriptionPackage sp where sp.pkg=:pkg and sp.subscription.id in (:subList)", [pkg:this, subList:subList])
                    //log.debug("before flush")
                    //session.flush()
                    return true
                //}
            } else {

                if (subPkg) {
                    OrgAccessPointLink.executeUpdate("delete from OrgAccessPointLink oapl where oapl.subPkg = :sp", [sp: subPkg])
                    CostItem.findAllBySubPkg(subPkg).each { costItem ->
                        costItem.subPkg = null
                        costItem.costItemStatus = RDStore.COST_ITEM_DELETED
                        if (!costItem.sub) {
                            costItem.sub = subPkg.subscription
                        }
                        costItem.save()
                    }
                    PendingChangeConfiguration.executeUpdate("delete from PendingChangeConfiguration pcc where pcc.subscriptionPackage=:sp",[sp:subPkg])
                }

                SubscriptionPackage.executeUpdate("delete from SubscriptionPackage sp where sp.pkg = :pkg and sp.subscription = :sub ", [pkg: this, sub:subscription])
                return true
            }
        }else{
            log.error("!!! unlinkFromSubscription fail: CostItems are still linked -> [pkg:${this},sub:${subscription}]!!!!")
            return false
        }
    }

    def removePackagePendingChanges(List subIds, confirmed) {
        //log.debug("begin remove pending changes")
        String tipp_class = TitleInstancePackagePlatform.class.getName()
        List<Long> tipp_ids = TitleInstancePackagePlatform.executeQuery(
                "select tipp.id from TitleInstancePackagePlatform tipp where tipp.pkg.id = :pkgId", [pkgId: this.id]
        )
        List pendingChanges = subIds ? PendingChange.executeQuery(
                "select pc.id, pc.payload from PendingChange pc where pc.subscription.id in (:subIds)" , [subIds: subIds]
        ) : []

        List pc_to_delete = []
        pendingChanges.each { pc ->
            //log.debug("begin pending changes")
            if(pc[1]){
                def payload = JSON.parse(pc[1])
                if (payload.tippID) {
                    pc_to_delete << pc[0]
                }else if (payload.tippId) {
                    pc_to_delete << pc[0]
                } else if (payload.changeDoc) {
                    def (oid_class, ident) = payload.changeDoc.OID.split(":")
                    if (oid_class == tipp_class && tipp_ids.contains(ident.toLong())) {
                        pc_to_delete << pc[0]
                    }
                } else {
                    log.error("Could not decide if we should delete the pending change id:${pc[0]} - ${payload}")
                }
            }
            else {
                pc_to_delete << pc[0]
            }
        }
        if (confirmed && pc_to_delete) {
            String del_pc_query = "delete from PendingChange where id in (:del_list) "
            if(pc_to_delete.size() > 32766) { //cf. https://stackoverflow.com/questions/49274390/postgresql-and-hibernate-java-io-ioexception-tried-to-send-an-out-of-range-inte
                pc_to_delete.collate(32766).each { subList ->
                    log.debug("Deleting Pending Change Slice: ${subList}")
                    executeUpdate(del_pc_query,[del_list:subList])
                }
            }
            else {
                log.debug("Deleting Pending Changes: ${pc_to_delete}")
                executeUpdate(del_pc_query, [del_list: pc_to_delete])
            }
        } else {
            return pc_to_delete.size()
        }
    }

  String toString() {
    name ? "${name}" : "Package ${id}"
  }

    /*
 @Transient
  def onSave = {
    log.debug("onSave")
    def changeNotificationService = grailsApplication.mainContext.getBean("changeNotificationService")

    changeNotificationService.fireEvent([
                                                 OID:"${Package.class.name}:${id}",
                                                 event:'Package.created'
                                                ])

  }
    */
  /**
  * OPTIONS: startDate, endDate, hideIdent, inclPkgStartDate, hideDeleted
  */
    /*
  @Transient
  def notifyDependencies(changeDocument) {
    def changeNotificationService = grailsApplication.mainContext.getBean("changeNotificationService")
    if ( changeDocument.event=='Package.created' ) {
      changeNotificationService.broadcastEvent("${SystemObject.class.name}:1", changeDocument);
    }
  }
     */

  @Transient
  static def refdataFind(GrailsParameterMap params) {
    List<Map<String, Object>> result = []

    String hqlString = "select pkg from Package pkg where lower(pkg.name) like ? "
    def hqlParams = [((params.q ? params.q.toLowerCase() : '' ) + "%")]
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd")

    if(params.hasDate ){

      def startDate = params.startDate.length() > 1 ? sdf.parse(params.startDate) : null
      def endDate =  params.endDate.length() > 1 ? sdf.parse(params.endDate)  : null

      if(startDate) {
        hqlString += " AND pkg.startDate >= ?"
        hqlParams += startDate
      }
      if(endDate) {
        hqlString += " AND pkg.endDate <= ?"
        hqlParams += endDate
      }
    }

    if(params.hideDeleted == 'true'){
        hqlString += " AND pkg.packageStatus != ?"
        hqlParams += RDStore.PACKAGE_STATUS_DELETED
    }

    def queryResults = Package.executeQuery(hqlString,hqlParams);

    queryResults?.each { t ->
      def resultText = t.name
      def date = t.startDate? " (${sdf.format(t.startDate)})" :""
      resultText = params.inclPkgStartDate == "true" ? resultText + date : resultText
      resultText = params.hideIdent == "true" ? resultText : resultText+" (${t.identifier})"
      result.add([id:"${t.class.name}:${t.id}",text:resultText])
    }

    result
  }

  @Deprecated
  @Transient
  def toComparablePackage() {
    Map<String, Object> result = [:]
    println "converting old package to comparable package"
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    println "processing metadata"
    result.packageName = this.name
    result.packageId = this.identifier
    result.gokbId = this.gokbId

    result.tipps = []
    println "before tipps"
    this.tipps.each { tip ->
        println "Now processing TIPP ${tip}"
      //NO DELETED TIPPS because from only come no deleted tipps
      if(tip.status?.id != RDStore.TIPP_STATUS_DELETED.id){
      // Title.ID needs to be the global identifier, so we need to pull out the global id for each title
      // and use that.
          println "getting identifier value of title ..."
      def title_id = tip.title.getIdentifierValue('uri')?:"uri://KBPlus/localhost/title/${tip.title.id}"
          println "getting identifier value of TIPP ..."
      def tipp_id = tip.getIdentifierValue('uri')?:"uri://KBPlus/localhost/tipp/${tip.id}"

          println "preparing TIPP ..."
      def newtip = [
                     title: [
                       name:tip.title.title,
                       identifiers:[],
                       titleType: tip.title.class.name ?: null
                     ],
                     titleId:title_id,
                     titleUuid:tip.title.gokbId,
                     tippId:tipp_id,
                     tippUuid:tip.gokbId,
                     platform:tip.platform.name,
                     platformId:tip.platform.id,
                     platformUuid:tip.platform.gokbId,
                     platformPrimaryUrl:tip.platform.primaryUrl,
                     coverage:[],
                     url:tip.hostPlatformURL ?: '',
                     identifiers:[],
                     status: tip.status,
                     accessStart: tip.accessStartDate ?: null,
                     accessEnd: tip.accessEndDate ?: null
                   ];

          println "adding coverage ..."
      // Need to format these dates using correct mask
          tip.coverages.each { tc ->
              newtip.coverage.add([
                      startDate:tc.startDate ?: null,
                      endDate:tc.endDate ?: null,
                      startVolume:tc.startVolume ?: '',
                      endVolume:tc.endVolume ?: '',
                      startIssue:tc.startIssue ?: '',
                      endIssue:tc.endIssue ?: '',
                      coverageDepth:tc.coverageDepth ?: '',
                      coverageNote:tc.coverageNote ?: '',
                      embargo: tc.embargo ?: ''
              ])
          }

          println "processing IDs ..."
      tip?.title?.ids.each { id ->
          println "adding identifier ${id}"
        newtip.title.identifiers.add([namespace:id.ns.ns, value:id.value]);
      }

      result.tipps.add(newtip)
          }
    }

    result.tipps.sort{it.titleId}
    println "Rec conversion for package returns object with title ${result.title} and ${result.tipps?.size()} tipps"

    result
  }

    @Override
    def beforeInsert() {
        if ( name != null ) {
            sortName = generateSortName(name)
        }

        super.beforeInsertHandler()
    }

    @Override
    def beforeUpdate() {
        if ( name != null ) {
            sortName = generateSortName(name)
        }
        super.beforeUpdateHandler()
    }

    @Override
    def beforeDelete() {
        super.beforeDeleteHandler()
    }

  def checkAndAddMissingIdentifier(ns,value) {
    boolean found = false
    println "processing identifier ${value}"
    this.ids.each {
        println "processing identifier occurrence ${it}"
      if ( it.ns?.ns == ns && it.value == value ) {
          println "occurrence found"
        found = true
      }
    }

    if ( ! found && ns.toLowerCase() != 'originediturl' ) {
        // TODO [ticket=1789]
      //def id = Identifier.lookupOrCreateCanonicalIdentifier(ns, value)
      //  println "before execute query"
      //def id_occ = IdentifierOccurrence.executeQuery("select io from IdentifierOccurrence as io where io.identifier = ? and io.pkg = ?", [id,this])
      //  println "id_occ query executed"

      //if ( !id_occ || id_occ.size() == 0 ){
      //  println "Create new identifier occurrence for pid:${getId()} ns:${ns} value:${value}"
      //  new IdentifierOccurrence(identifier:id, pkg:this).save()
      //}
        Identifier.construct([value:value, reference:this, namespace:ns])
    }
    else if(ns.toLowerCase() == 'originediturl') {
        println "package identifier namespace for ${value} is deprecated originEditUrl ... ignoring."
    }
  }

  static String generateSortName(String input_title) {
    if (!input_title) return null
    String s1 = Normalizer.normalize(input_title, Normalizer.Form.NFKD).trim().toLowerCase()
    s1 = s1.replaceFirst('^copy of ','')
    s1 = s1.replaceFirst('^the ','')
    s1 = s1.replaceFirst('^a ','')
    s1 = s1.replaceFirst('^der ','')

    return s1.trim()

  }

    Identifier getIdentifierByType(String idtype) {
        Identifier result
        ids.each { ident ->
            if ( ident.ns.ns.equalsIgnoreCase(idtype) ) {
                result = ident
            }
        }
        result
    }

    Set<TitleInstancePackagePlatform> getCurrentTipps() {
        Set<TitleInstancePackagePlatform> result = []
        if (this.tipps) {
            result = this.tipps?.findAll{it?.status?.id == RDStore.TIPP_STATUS_CURRENT.id}
        }
        result
    }

    @Transient
    void addPendingChangeConfiguration(Subscription subscription, GrailsParameterMap grailsParameterMap) {

        SubscriptionPackage subscriptionPackage = SubscriptionPackage.findBySubscriptionAndPkg(subscription, this)
        if(subscriptionPackage) {
            PendingChangeConfiguration.SETTING_KEYS.each { String settingKey ->
                Map<String, Object> configMap = [subscriptionPackage: subscriptionPackage, settingKey: settingKey, withNotification: false]
                boolean auditable = false
                //Set because we have up to three keys in params with the settingKey
                Set<String> keySettings = grailsParameterMap.keySet().findAll { k -> k.contains(settingKey) }
                keySettings.each { key ->
                    List<String> settingData = key.split('!§!')
                    switch (settingData[1]) {
                        case 'setting': configMap.settingValue = RefdataValue.get(grailsParameterMap[key])
                            break
                        case 'notification': configMap.withNotification = grailsParameterMap[key] != null
                            break
                        case 'auditable': auditable = grailsParameterMap[key] != null
                            break
                    }
                }
                try {
                    PendingChangeConfiguration.construct(configMap)
                    boolean hasConfig = AuditConfig.getConfig(subscriptionPackage.subscription, settingKey) != null
                    if (auditable && !hasConfig) {
                        AuditConfig.addConfig(subscriptionPackage.subscription, settingKey)
                    } else if (!auditable && hasConfig) {
                        AuditConfig.removeConfig(subscriptionPackage.subscription, settingKey)
                    }
                }
                catch (CreationException e) {
                    log.error("ProcessLinkPackage -> PendingChangeConfiguration: " + e.message)
                }
            }
        }
    }
}
