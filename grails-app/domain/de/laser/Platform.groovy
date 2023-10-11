package de.laser

import de.laser.annotations.RefdataInfo
import de.laser.auth.User
import de.laser.base.AbstractBaseWithCalculatedLastUpdated
import de.laser.convenience.Marker
import de.laser.interfaces.MarkerSupport
import de.laser.oap.OrgAccessPoint
import de.laser.oap.OrgAccessPointLink
import de.laser.properties.PlatformProperty
import de.laser.properties.PropertyDefinitionGroup
import de.laser.storage.BeanStore
import de.laser.storage.RDConstants
import grails.web.servlet.mvc.GrailsParameterMap
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

/**
 * This class represents a platform record. A platform is a portal where providers offer access to titles subscribed via packages.
 * This class is a mirror of the we:kb-implementation of Platform, <a href="https://github.com/hbz/wekb2/blob/dev/grails-app/domain/wekb/Platform.groovy">cf. with the we:kb-implementation</a>
 */
class Platform extends AbstractBaseWithCalculatedLastUpdated implements Comparable<Platform>, MarkerSupport {

  String gokbId
  String name
  String normname
  String primaryUrl
  String provenance
  String titleNamespace
  String centralApiKey

  @RefdataInfo(cat = RDConstants.PLATFORM_STATUS)
  RefdataValue status

  @RefdataInfo(cat = RDConstants.Y_N, i18n = 'platform.serviceProvider')
  RefdataValue serviceProvider

  @RefdataInfo(cat = RDConstants.Y_N, i18n = 'platform.softwareProvider')
  RefdataValue softwareProvider

  Date dateCreated
  Date lastUpdated
  Date lastUpdatedCascading
  Date counter4LastRun
  Date counter5LastRun

  Org org

  SortedSet altnames

  static mappedBy = [tipps: 'platform', altnames: 'platform']

  static hasMany = [
          tipps      : TitleInstancePackagePlatform,
          oapp       : OrgAccessPointLink,
          propertySet: PlatformProperty,
          altnames   : AlternativeName
  ]

  static transients = ['calculatedPropDefGroups'] // mark read-only accessor methods

  static mapping = {
                id column:'plat_id'
         globalUID column:'plat_guid'
           version column:'plat_version'
            gokbId column:'plat_gokb_id', type:'text'
              name column:'plat_name'
          normname column:'plat_normalised_name'
        provenance column:'plat_data_provenance'
    titleNamespace column:'plat_title_namespace', type: 'text'
     centralApiKey column:'plat_central_api_key', type: 'text'
        primaryUrl column:'plat_primary_url'
            status column:'plat_status_rv_fk'
   serviceProvider column:'plat_servprov_rv_fk'
  softwareProvider column:'plat_softprov_rv_fk'
              org  column: 'plat_org_fk', index: 'plat_org_idx'
             dateCreated column: 'plat_date_created'
             lastUpdated column: 'plat_last_updated'
    lastUpdatedCascading column: 'plat_last_updated_cascading'
    counter4LastRun column: 'plat_c4_last_run'
    counter5LastRun column: 'plat_c5_last_run'
             tipps batchSize: 10
            oapp batchSize: 10
    propertySet sort:'type', order:'desc', batchSize: 10
  }

  static constraints = {
    globalUID(nullable:true, blank:false, unique:true, maxSize:255)
    primaryUrl(nullable:true, blank:false)
    provenance(nullable:true, blank:false)
    titleNamespace(nullable:true, blank:false)
    centralApiKey(nullable:true, blank:false)
    serviceProvider (nullable:true)
    softwareProvider(nullable:true)
    gokbId (blank:false, unique: true, maxSize:511)
    org             (nullable:true)
    lastUpdatedCascading (nullable: true)
    counter4LastRun (nullable: true)
    counter5LastRun (nullable: true)
  }

  @Override
  int compareTo(Platform that) {
    int result = 0
    if(this.org && that.org) {
      if(this.org.sortname && that.org.sortname)
        result = this.org.sortname <=> that.org.sortname
      else result = this.org.name <=> that.org.name
    }
    if(result == 0)
        result = this.name <=> that.name
    result
  }

  @Override
  def afterDelete() {
    super.afterDeleteHandler()

    BeanStore.getDeletionService().deleteDocumentFromIndex(this.globalUID, this.class.simpleName)
  }
  @Override
  def afterInsert() {
    super.afterInsertHandler()
  }
  @Override
  def afterUpdate() {
    super.afterUpdateHandler()
  }
  @Override
  def beforeInsert() {
    super.beforeInsertHandler()
  }
  @Override
  def beforeUpdate() {
    super.beforeUpdateHandler()
  }
  @Override
  def beforeDelete() {
    super.beforeDeleteHandler()
  }

  /**
   * Retrieves the property definition groups defined by the given institution for this platform
   * @param contextOrg the {@link Org} whose property definition groups should be retrieved
   * @return a {@link Map} of {@link PropertyDefinitionGroup}s, ordered by sorted, global, local and orphaned property definitions
   */
    Map<String, Object> getCalculatedPropDefGroups(Org contextOrg) {
      BeanStore.getPropertyService().getCalculatedPropDefGroups(this, contextOrg)
    }

  /**
   * Checks whether this platform uses access points defined for the given subscription package
   * @param subscriptionPackage the subscription (represented by the {@link SubscriptionPackage} link) whose configurations should be verified
   * @return true if there are access point configurations linked to this platform and the given subscription package, false otherwise
   */
  boolean usesPlatformAccessPoints(SubscriptionPackage subscriptionPackage){
    // look for OrgAccessPointLinks for this platform and a given subscriptionPackage, if we can find that "marker",
    // we know the AccessPoints are not derived from the AccessPoints configured for the platform
    String hql = "select oapl from OrgAccessPointLink oapl where oapl.platform=${this.id} and oapl.subPkg = ${subscriptionPackage.id} and oapl.oap is null"
    List<OrgAccessPointLink> result = OrgAccessPointLink.executeQuery(hql)
    (result) ? false : true
  }

  /**
   * Called from currentPlatforms.gsp
   * Gets all access point configurations for this platform and the given institution
   * @param contextOrg the context {@link Org} whose configurations should be retrieved
   * @return a {@link List} of {@link OrgAccessPoint}s pointing to this platform and defined by the given institution
   */
  List<OrgAccessPoint> getContextOrgAccessPoints(Org contextOrg) {
    String hql = "select oap from OrgAccessPoint oap " +
        "join oap.oapp as oapp where oap.org=:org and oapp.active = true and oapp.platform.id =${this.id} and oapp.subPkg is null order by LOWER(oap.name)"
    OrgAccessPoint.executeQuery(hql, ['org': contextOrg])
  }

  /**
   * Gets a list of platform records for a dropdown display. The records may be filtered by the given parameter map
   * @param params the parameter map which contains the filter parameters
   * @return a {@link List} of {@link Map}s in the format [id: id, text: text], containing the selectable records
   */
  static def refdataFind(GrailsParameterMap params) {
    GenericOIDService genericOIDService = BeanStore.getGenericOIDService()

    genericOIDService.getOIDMapList( Platform.findAllByNameIlike("${params.q}%", params), 'name' )
  }

  @Override
  boolean equals (Object o) {
    def obj = GrailsHibernateUtil.unwrapIfProxy(o)
    if (obj != null) {
      if ( obj instanceof Platform ) {
        return obj.id == id
      }
    }
    return false
  }

  @Override
  String toString() {
    name
  }

  /**
   * Checks if the platform is being marked for the given user with the given marker type
   * @param user the {@link User} whose watchlist should be checked
   * @param type the {@link Marker.TYPE} of the marker to check
   * @return true if the platform is marked, false otherwise
   */
  @Override
  boolean isMarked(User user, Marker.TYPE type) {
    Marker.findByPltAndUserAndType(this, user, type) ? true : false
  }

  /**
   * Sets the marker for the platform for given user of the given type
   * @param user the {@link User} for which the platform should be marked
   * @param type the {@link Marker.TYPE} of marker to record
   */
  @Override
  void setMarker(User user, Marker.TYPE type) {
    if (!isMarked(user, type)) {
      Marker m = new Marker(plt: this, user: user, type: type)
      m.save()
    }
  }

  /**
   * Removes the given marker with the given type for the platform from the user's watchlist
   * @param user the {@link User} from whose watchlist the platform marker should be removed
   * @param type the {@link Marker.TYPE} of marker to remove
   */
  @Override
  void removeMarker(User user, Marker.TYPE type) {
    withTransaction {
      Marker.findByPltAndUserAndType(this, user, type).delete(flush:true)
    }
  }
}
