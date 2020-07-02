package com.k_int.kbplus

import com.k_int.properties.PropertyDefinitionGroup
import com.k_int.properties.PropertyDefinitionGroupBinding
import de.laser.base.AbstractBaseWithCalculatedLastUpdated
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.helper.RefdataAnnotation
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil

import javax.persistence.Transient

class Platform extends AbstractBaseWithCalculatedLastUpdated {

  @Transient
  def grailsApplication
  @Transient
  def propertyService
  @Transient
  def deletionService

  static Log static_logger = LogFactory.getLog(Platform)

  String gokbId
  String name
  String normname
  String primaryUrl
  //URL originEditUrl
  String provenance

  @RefdataAnnotation(cat = '?')
  RefdataValue type

  @RefdataAnnotation(cat = RDConstants.PLATFORM_STATUS)
  RefdataValue status // TODO: not in Bootstrap

  @RefdataAnnotation(cat = RDConstants.Y_N)
  RefdataValue serviceProvider

  @RefdataAnnotation(cat = RDConstants.Y_N)
  RefdataValue softwareProvider

  Date dateCreated
  Date lastUpdated
  Date lastUpdatedCascading

  Org org


  static mappedBy = [tipps: 'platform']
  static hasMany = [
      tipps: TitleInstancePackagePlatform,
      oapp: OrgAccessPointLink,
      customProperties:   PlatformCustomProperty,
  ]

  static mapping = {
                id column:'plat_id'
         globalUID column:'plat_guid'
           version column:'plat_version'
            gokbId column:'plat_gokb_id', type:'text'
              name column:'plat_name'
          normname column:'plat_normalised_name'
        provenance column:'plat_data_provenance'
        primaryUrl column:'plat_primary_url'
   //originEditUrl column:'plat_origin_edit_url'
              type column:'plat_type_rv_fk'
            status column:'plat_status_rv_fk'
   serviceProvider column:'plat_servprov_rv_fk'
  softwareProvider column:'plat_softprov_rv_fk'
              org  column: 'plat_org_fk', index: 'plat_org_idx'
    lastUpdatedCascading column: 'plat_last_updated_cascading'
             tipps sort: 'title.title', order: 'asc', batchSize: 10
            oapp batchSize: 10
    customProperties sort:'type', order:'desc', batchSize: 10
  }

  static constraints = {
    globalUID(nullable:true, blank:false, unique:true, maxSize:255)
    primaryUrl(nullable:true, blank:false)
  //originEditUrl(nullable:true, blank:false)
    provenance(nullable:true, blank:false)
    type(nullable:true, blank:false)
    status(nullable:true, blank:false)
    serviceProvider(nullable:true, blank:false)
    softwareProvider(nullable:true, blank:false)
    gokbId (blank:false, unique: true, maxSize:511)
    org (nullable:true, blank:false)
    lastUpdatedCascading (nullable: true, blank: false)
  }

  @Override
  def afterDelete() {
    super.afterDeleteHandler()

    deletionService.deleteDocumentFromIndex(this.globalUID)
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

  @Deprecated
  static Platform lookupOrCreatePlatform(Map params=[:]) {

    Platform platform
    List<Platform> platform_candidates = []

/*    if ( params.impId && params.impId.trim().length() > 0) {
      platform = Platform.findByImpId(params.impId)
    }*/

    if ( params.gokbId && params.gokbId.trim().length() > 0) {
      platform = Platform.findByGokbId(params.gokbId)

    }

    if ( !platform && params.name && (params.name.trim().length() > 0)  ) {

      String norm_name = params.name.trim().toLowerCase();
        //TODO: Dieser Zweig passieert nicht bei GOKB Sync
      if( params.primaryUrl && (params.primaryUrl.length() > 0) ){

        platform_candidates = Platform.executeQuery("from Platform where normname = ? or primaryUrl = ?",[norm_name, params.primaryUrl])

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
                                lastmod: System.currentTimeMillis()).save(flush:true)

      }
    }

    if (platform && params.gokbId  && platform.gokbId != params.gokbId) {
      platform.gokbId = params.gokbId
      platform.save(flush:true)
    }

    if(platform && params.primaryUrl && platform.primaryUrl != params.primaryUrl)
    {
      platform.primaryUrl = params.primaryUrl
      platform.save(flush:true)
    }

    if(platform && params.name && platform.name != params.name)
    {
      platform.name = params.name
      platform.save(flush:true)
    }

    platform
  }

  Map<String, Object> getCalculatedPropDefGroups(Org contextOrg) {
    Map<String, Object> result = [ 'sorted':[], 'global':[], 'local':[], 'orphanedProperties':[] ]

    // ALL type depending groups without checking tenants or bindings
    List<PropertyDefinitionGroup> groups = PropertyDefinitionGroup.findAllByOwnerType(Platform.class.name, [sort:'name', order:'asc'])
    groups.each{ it ->

      PropertyDefinitionGroupBinding binding = PropertyDefinitionGroupBinding.findByPropDefGroupAndOrg(it, this)

      if (it.tenant == null || it.tenant?.id == contextOrg?.id) {
        if (binding) {
          result.local << [it, binding] // TODO: remove
          result.sorted << ['local', it, binding]
        }
        else {
          result.global << it // TODO: remove
          result.sorted << ['global', it, null]
        }
      }
    }

    // storing properties without groups
    result.orphanedProperties = propertyService.getOrphanedProperties(this, result.sorted)

    result
  }

  boolean usesPlatformAccessPoints(contextOrg, subscriptionPackage){
    // TODO do we need the contextOrg?
    // look for OrgAccessPointLinks for this platform and a given subscriptionPackage, if we can find that "marker",
    // we know the AccessPoints are not derived from the AccessPoints configured for the platform
    String hql = "select oapl from OrgAccessPointLink oapl where oapl.platform=${this.id} and oapl.subPkg = ${subscriptionPackage.id} and oapl.oap is null"
    def result = OrgAccessPointLink.executeQuery(hql)
    (result) ? false : true
  }

  def getContextOrgAccessPoints(contextOrg) {
    String hql = "select oap from OrgAccessPoint oap " +
        "join oap.oapp as oapp where oap.org=:org and oapp.active = true and oapp.platform.id =${this.id} and oapp.subPkg is null order by LOWER(oap.name)"
    def result = OrgAccessPoint.executeQuery(hql, ['org' : contextOrg])
    return result
  }

  def getNotActiveAccessPoints(org){
    String notActiveAPLinkQuery = "select oap from OrgAccessPoint oap where oap.org =:institution "
    notActiveAPLinkQuery += "and not exists ("
    notActiveAPLinkQuery += "select 1 from oap.oapp as oapl where oapl.oap=oap and oapl.active=true "
    notActiveAPLinkQuery += "and oapl.platform.id = ${id}) order by lower(oap.name)"
    OrgAccessPoint.executeQuery(notActiveAPLinkQuery, [institution : org])
  }

  static def refdataFind(params) {
    def result = [];
    def ql = null;
    ql = Platform.findAllByNameIlike("${params.q}%",params)

    if ( ql ) {
      ql.each { t ->
        result.add([id:"${t.class.name}:${t.id}",text:"${t.name}"])
      }
    }

    result
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

  def getCurrentTipps() {
    def result = this.tipps?.findAll{it?.status?.id == RDStore.TIPP_STATUS_CURRENT.id}

    result
  }
}
