package de.laser

import de.laser.ui.Icon
import de.laser.oap.OrgAccessPointLink
import de.laser.storage.RDStore
import de.laser.wekb.Package
import de.laser.wekb.TitleInstancePackagePlatform
import grails.web.servlet.mvc.GrailsParameterMap

import javax.persistence.Transient

/**
 * This class represents the links between subscriptions and packages, i.e. marks that a package has been subscribed by a subscription tenant.
 * There are certain settings linked to the subscription-package connection, namely those which control the holding behavior (how should package
 * changes being reflected) and which are related to the package and depending on the subscription such as platform access configurations
 * @see OrgAccessPointLink
 * @see PendingChangeConfiguration
 * @see IssueEntitlement
 * @see de.laser.wekb.Package
 */
class SubscriptionPackage implements Comparable {

  Subscription subscription
  Package pkg
  Date finishDate

  Date dateCreated
  Date lastUpdated

  public static final String FREEZE_HOLDING = "freezeHolding"

  static transients = ['issueEntitlementsofPackage', 'IEandPackageSize', 'currentTippsofPkg', 'packageName'] // mark read-only accessor methods

  static mapping = {
                id column:'sp_id'
           version column:'sp_version'
      subscription column:'sp_sub_fk',  index: 'sp_sub_idx, sp_sub_pkg_idx'
               pkg column:'sp_pkg_fk',  index: 'sp_pkg_idx, sp_sub_pkg_idx'
        finishDate column:'sp_finish_date'

    dateCreated column: 'sp_date_created'
    lastUpdated column: 'sp_last_updated'
  }

  static hasMany = [
    oapls: OrgAccessPointLink,
    pendingChangeConfig: PendingChangeConfiguration
  ]

  static belongsTo = [
      pkg: Package,
      subscription: Subscription
  ]

  static mappedBy = [
    oapls: 'subPkg',
    pendingChangeConfig: 'subscriptionPackage'
  ]

  static constraints = {
    subscription(nullable:true)
    pkg         (nullable:true)
    finishDate  (nullable:true)

    // Nullable is true, because values are already in the database
    lastUpdated (nullable: true)
    dateCreated (nullable: true)
    subscription(unique: ['pkg'])
  }

  /**
   * Compares this subscription package to another on the base of the package's name
   * @param o the other instance to compare with
   * @return the comparison result (-1, 0, 1)
   */
  @Override
  int compareTo(Object o) {
    return this.pkg.name.compareTo(o.pkg.name)
  }

  /**
   * Constructs a dropdown list of packages linked to the given subscription. Filterable by subscription ID
   * @param params the parameter map containing the subscription ID to filter
   * @return a {@link List} of {@link Map}s of structure [id: oid, text: subscription name/package name]
   */
  @Transient
  static def refdataFind(GrailsParameterMap params) {
    List<Map<String, Object>> result = []
    Map<String, Object> hqlParams = [:]
    String hqlString = "select sp from SubscriptionPackage as sp"

    if ( params.subFilter ) {
      hqlString += ' where sp.subscription.id = :sid'
      hqlParams.put('sid', params.long('subFilter'))
    }

    List<SubscriptionPackage> results = SubscriptionPackage.executeQuery(hqlString,hqlParams)

    results?.each { t ->
      String resultText = t.subscription.name + '/' + t.pkg.name
      result.add([id:"${t.class.name}:${t.id}",text:resultText])
    }

    result
  }

  /**
   * Retrieves all current issue entitlements which are definitively in the package
   * @return a {@link Set} of {@link IssueEntitlement}s in the {@link Subscription}'s holding which are current and accepted
   */
  @Deprecated
  Set getIssueEntitlementsofPackage(){
    this.subscription.issueEntitlements.findAll{(it.status?.id == RDStore.TIPP_STATUS_CURRENT.id)}
  }

  /**
   * Counts the issue entitlements of this subscription in the given package which have not been marked as deleted
   * @return the count of {@link IssueEntitlement}s of the holding which is not marked as deleted
   */
  @Deprecated
  int getIssueEntitlementCountOfPackage(){
    IssueEntitlement.executeQuery('select count(*) from IssueEntitlement ie join ie.tipp tipp where tipp.pkg = :pkg and ie.subscription = :sub and ie.status != :removed', [sub: this.subscription, pkg: this.pkg, removed: RDStore.TIPP_STATUS_REMOVED])[0]
  }

  /**
   * Retrieves the current titles of the global level of the given package - this method is NOT delivering the current holding of the subscription!
   * @return a {@link Set} of {@link de.laser.wekb.TitleInstancePackagePlatform}s in the subscribed package (on global level!)
   */
  @Deprecated
  Set getCurrentTippsofPkg()
  {
    this.pkg.tipps?.findAll{ TitleInstancePackagePlatform tipp -> tipp.status?.value == 'Current' }
  }

  /**
   * Gets the name of the subscribed package
   * @return the name of the {@link Package} subscribed
   */
  String getPackageName() {
    return this.pkg.name
  }

  /**
   * Used in _linkPackages.gsp
   * Gets the package name with the count of current titles in the package on global level
   * @return a concatenated string of the {@link Package} name and the count of current {@link TitleInstancePackagePlatform}s
   */
  String getPackageNameWithCurrentTippsCount() {
    return this.pkg.name + ' ('+ IssueEntitlement.executeQuery('select count(*) from IssueEntitlement ie where ie.subscription = :subscription and ie.tipp.pkg = :pkg and ie.status = :current', [subscription: this.subscription, pkg: this.pkg, current: RDStore.TIPP_STATUS_CURRENT])[0] +' / '+ TitleInstancePackagePlatform.executeQuery("select count(*) from TitleInstancePackagePlatform where pkg = :pkg and status = :status", [pkg: this.pkg, status: RDStore.TIPP_STATUS_CURRENT])[0] +')'
  }

  /**
   * Used by /subscription/show.gsp
   * Gets the pending change configuration for this package subscription and the given config key
   * @param config the config key to check how this subscription package should behave
   * @return the {@link PendingChangeConfiguration} for the given config key and this subscription package
   */
  PendingChangeConfiguration getPendingChangeConfig(String config) {
    if(this.subscription.instanceOf) {
      if(AuditConfig.getConfig(this.subscription.instanceOf, config))
        PendingChangeConfiguration.executeQuery('select pcc from PendingChangeConfiguration pcc join pcc.subscriptionPackage sp where sp.subscription.instanceOf = :sub and sp.settingKey = :config', [sub: this.subscription.instanceOf, config: config]).get(0)
    }
    else
      PendingChangeConfiguration.findBySubscriptionPackageAndSettingKey(this,config)
  }
}
