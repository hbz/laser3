package de.laser

import com.k_int.kbplus.GenericOIDService
import de.laser.storage.BeanStore
import de.laser.properties.PlatformProperty
import de.laser.properties.PropertyDefinitionGroup
import de.laser.oap.OrgAccessPoint
import de.laser.oap.OrgAccessPointLink
import de.laser.base.AbstractBaseWithCalculatedLastUpdated
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.annotations.RefdataInfo
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import grails.web.servlet.mvc.GrailsParameterMap

/**
 * This class represents a platform record. A platform is a portal where providers offer access to titles subscribed via packages.
 * This class is a mirror of the we:kb-implementation of Platform, <a href="https://github.com/hbz/wekb/blob/wekb-dev/server/gokbg3/grails-app/domain/org/gokb/cred/Platform.groovy">cf. with the we:kb-implementation</a>
 */
class Platform extends AbstractBaseWithCalculatedLastUpdated {

  static Log static_logger = LogFactory.getLog(Platform)

  String gokbId
  String name
  String normname
  String primaryUrl
  String provenance
  String titleNamespace

  @RefdataInfo(cat = '?')
  RefdataValue type

  @RefdataInfo(cat = RDConstants.PLATFORM_STATUS)
  RefdataValue status

  @RefdataInfo(cat = RDConstants.Y_N, i18n = 'platform.serviceProvider')
  RefdataValue serviceProvider

  @RefdataInfo(cat = RDConstants.Y_N, i18n = 'platform.softwareProvider')
  RefdataValue softwareProvider

  Date dateCreated
  Date lastUpdated
  Date lastUpdatedCascading

  Org org

  SortedSet altnames

  static mappedBy = [tipps: 'platform', altnames: 'platform']

  static hasMany = [
          tipps      : TitleInstancePackagePlatform,
          oapp       : OrgAccessPointLink,
          propertySet: PlatformProperty,
          altnames   : AlternativeName
  ]

  static transients = ['currentTipps', 'calculatedPropDefGroups'] // mark read-only accessor methods

  static mapping = {
                id column:'plat_id'
         globalUID column:'plat_guid'
           version column:'plat_version'
            gokbId column:'plat_gokb_id', type:'text'
              name column:'plat_name'
          normname column:'plat_normalised_name'
        provenance column:'plat_data_provenance'
    titleNamespace column:'plat_title_namespace', type: 'text'
        primaryUrl column:'plat_primary_url'
              type column:'plat_type_rv_fk'
            status column:'plat_status_rv_fk'
   serviceProvider column:'plat_servprov_rv_fk'
  softwareProvider column:'plat_softprov_rv_fk'
              org  column: 'plat_org_fk', index: 'plat_org_idx'
    lastUpdatedCascading column: 'plat_last_updated_cascading'
             tipps batchSize: 10
            oapp batchSize: 10
    propertySet sort:'type', order:'desc', batchSize: 10
  }

  static constraints = {
    globalUID(nullable:true, blank:false, unique:true, maxSize:255)
    primaryUrl(nullable:true, blank:false)
    provenance(nullable:true, blank:false)
    titleNamespace(nullable:true, blank:false)
    type            (nullable:true)
    serviceProvider (nullable:true)
    softwareProvider(nullable:true)
    gokbId (blank:false, unique: true, maxSize:511)
    org             (nullable:true)
    lastUpdatedCascading (nullable: true)
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

  @Deprecated
  static Platform lookupOrCreatePlatform(Map params=[:]) {

    withTransaction {

    Platform platform
    List<Platform> platform_candidates = []

    if ( params.gokbId && params.gokbId.trim().length() > 0) {
      platform = Platform.findByGokbId(params.gokbId)

    }

    if ( !platform && params.name && (params.name.trim().length() > 0)  ) {

      String norm_name = params.name.trim().toLowerCase();
        //TODO: Dieser Zweig passieert nicht bei we:kb Sync
      if( params.primaryUrl && (params.primaryUrl.length() > 0) ){

        platform_candidates = Platform.executeQuery("from Platform where normname = :nname or primaryUrl = :url", [nname: norm_name, url: params.primaryUrl])

        if(platform_candidates && platform_candidates.size() == 1){
          platform = platform_candidates[0]
        }
      }
      else {

        platform_candidates = Platform.executeQuery("from Platform where normname = :nname or primaryUrl = :nname",[nname: norm_name])

        if(platform_candidates && platform_candidates.size() == 1){
          platform = platform_candidates[0]
        }
      }

      if ( !platform && !platform_candidates) {
        platform = new Platform(gokbId: params.gokbId?.length() > 0 ? params.gokbId : null,
                                name: params.name,
                                normname: norm_name,
                                provenance: (params.provenance ?: null),
                                primaryUrl: (params.primaryUrl ?: null),
                                lastmod: System.currentTimeMillis()).save()

      }
    }

    if (platform && params.gokbId  && platform.gokbId != params.gokbId) {
      platform.gokbId = params.gokbId
      platform.save()
    }

    if(platform && params.primaryUrl && platform.primaryUrl != params.primaryUrl)
    {
      platform.primaryUrl = params.primaryUrl
      platform.save()
    }

    if(platform && params.name && platform.name != params.name)
    {
      platform.name = params.name
      platform.save()
    }

        platform
      }
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
   * @param contextOrg unused
   * @param subscriptionPackage the subscription (represented by the {@link SubscriptionPackage} link) whose configurations should be verified
   * @return true if there are access point configurations linked to this platform and the given subscription package, false otherwise
   */
  boolean usesPlatformAccessPoints(contextOrg, subscriptionPackage){
    // TODO do we need the contextOrg?
    // look for OrgAccessPointLinks for this platform and a given subscriptionPackage, if we can find that "marker",
    // we know the AccessPoints are not derived from the AccessPoints configured for the platform
    String hql = "select oapl from OrgAccessPointLink oapl where oapl.platform=${this.id} and oapl.subPkg = ${subscriptionPackage.id} and oapl.oap is null"
    def result = OrgAccessPointLink.executeQuery(hql)
    (result) ? false : true
  }

  /**
   * Called from currentPlatforms.gsp
   * Gets all access point configurations for this platform and the given institution
   * @param contextOrg the context {@link Org} whose configurations should be retrieved
   * @return a {@link List} of {@link OrgAccessPoint}s pointing to this platform and defined by the given institution
   */
  def getContextOrgAccessPoints(contextOrg) {
    String hql = "select oap from OrgAccessPoint oap " +
        "join oap.oapp as oapp where oap.org=:org and oapp.active = true and oapp.platform.id =${this.id} and oapp.subPkg is null order by LOWER(oap.name)"
    def result = OrgAccessPoint.executeQuery(hql, ['org': contextOrg])
    return result
  }

  @Deprecated
  def getNotActiveAccessPoints(org){
    String notActiveAPLinkQuery = "select oap from OrgAccessPoint oap where oap.org =:institution "
    notActiveAPLinkQuery += "and not exists ("
    notActiveAPLinkQuery += "select 1 from oap.oapp as oapl where oapl.oap=oap and oapl.active=true "
    notActiveAPLinkQuery += "and oapl.platform.id = ${id}) order by lower(oap.name)"
    OrgAccessPoint.executeQuery(notActiveAPLinkQuery, [institution : org])
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
    //def obj = ClassUtils.deproxy(o)
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
   * Gets the titles provided on this platform marked as current
   * @return a {@link Set} of {@link TitleInstancePackagePlatform}s, linked to this platform and marked as current
   */
  def getCurrentTipps() {
    def result = this.tipps?.findAll{it?.status?.id == RDStore.TIPP_STATUS_CURRENT.id}

    result
  }
}
