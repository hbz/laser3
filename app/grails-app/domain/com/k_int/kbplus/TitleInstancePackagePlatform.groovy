package com.k_int.kbplus

import com.k_int.ClassUtils
import de.laser.domain.AbstractBaseDomain
import de.laser.domain.IssueEntitlementCoverage
import de.laser.domain.TIPPCoverage
import de.laser.helper.RDStore
import de.laser.helper.RefdataAnnotation
import de.laser.traits.AuditableTrait
import groovy.time.TimeCategory
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.context.i18n.LocaleContextHolder

import javax.persistence.Transient
import java.text.SimpleDateFormat

class TitleInstancePackagePlatform extends AbstractBaseDomain /*implements AuditableTrait*/ {
  @Transient
  def grailsLinkGenerator

  @Transient
  def grailsApplication
  
  @Transient
  def messageSource

  @Transient
  def changeNotificationService

  static Log static_logger = LogFactory.getLog(TitleInstancePackagePlatform)

    // AuditableTrait
    //static auditable = true
    static controlledProperties = ['status', 'platform','accessStartDate','accessEndDate','coverages']

    /*
    Date startDate
    String startVolume
    String startIssue
    Date endDate
    String endVolume
    String endIssue
    String embargo
    String coverageDepth
    String coverageNote
     */
    Date accessStartDate
    Date accessEndDate
    Date coreStatusStart
    Date coreStatusEnd
    String rectype="so"
    String gokbId
    //URL originEditUrl

    @RefdataAnnotation(cat = 'TIPP Status')
    RefdataValue status

    @RefdataAnnotation(cat = '?')
    RefdataValue option

    @RefdataAnnotation(cat = '?')
    RefdataValue delayedOA

    @RefdataAnnotation(cat = '?')
    RefdataValue hybridOA

    @RefdataAnnotation(cat = '?')
    RefdataValue statusReason

    @RefdataAnnotation(cat = '?')
    RefdataValue payment

    String hostPlatformURL

    Date dateCreated
    Date lastUpdated

  //TitleInstancePackagePlatform derivedFrom
  //TitleInstancePackagePlatform masterTipp

  static mappedBy = [ids: 'tipp'/*, additionalPlatforms: 'tipp'*/]
  static hasMany = [ids: Identifier,
                    //additionalPlatforms: PlatformTIPP,
                    coverages: TIPPCoverage]


  static belongsTo = [
    pkg:Package,
    platform:Platform,
    title:TitleInstance,
    sub:Subscription
  ]

  static mapping = {
                id column:'tipp_id'
         globalUID column:'tipp_guid'
           rectype column:'tipp_rectype'
           version column:'tipp_version'
               pkg column:'tipp_pkg_fk',    index: 'tipp_idx'
          platform column:'tipp_plat_fk',   index: 'tipp_idx'
             title column:'tipp_ti_fk',     index: 'tipp_idx'
            gokbId column:'tipp_gokb_id',       type:'text'
     //originEditUrl column:'tipp_origin_edit_url'
            status column:'tipp_status_rv_fk'
         delayedOA column:'tipp_delayedoa_rv_fk'
          hybridOA column:'tipp_hybridoa_rv_fk'
      statusReason column:'tipp_status_reason_rv_fk'
           payment column:'tipp_payment_rv_fk'
            option column:'tipp_option_rv_fk'
   hostPlatformURL column:'tipp_host_platform_url', type: 'text'
               sub column:'tipp_sub_fk'
       //derivedFrom column:'tipp_derived_from'
      accessStartDate column:'tipp_access_start_date'
      accessEndDate column:'tipp_access_end_date'
      coreStatusStart column:'tipp_core_status_start_date'
      coreStatusEnd column:'tipp_core_status_end_date'
      /*startDate column:'tipp_start_date',    index: 'tipp_dates_idx'
      startVolume column:'tipp_start_volume'
      startIssue column:'tipp_start_issue'
      endDate column:'tipp_end_date',      index: 'tipp_dates_idx'
      endVolume column:'tipp_end_volume'
      endIssue column:'tipp_end_issue'
      embargo column:'tipp_embargo'
      coverageDepth column:'tipp_coverage_depth'
      coverageNote column:'tipp_coverage_note', type: 'text'*/

      ids                   batchSize: 10
    //additionalPlatforms   batchSize: 10
      coverages             batchSize: 10, sort: 'startDate', order: 'asc'

      dateCreated column: 'tipp_date_created'
      lastUpdated column: 'tipp_last_updated'
  }

    static constraints = {
        globalUID(nullable:true, blank:false, unique:true, maxSize:255)
        gokbId (nullable:true, blank:false)
        //originEditUrl(nullable:true, blank:false)
        status(nullable:true, blank:false)
        delayedOA(nullable:true, blank:false)
        hybridOA(nullable:true, blank:false)
        statusReason(nullable:true, blank:false)
        payment(nullable:true, blank:false)
        option(nullable:true, blank:false)
        sub(nullable:true, blank:false)
        hostPlatformURL(nullable:true, blank:true, maxSize:2048)
        //derivedFrom(nullable:true, blank:true)
        accessStartDate(nullable:true, blank:true)
        accessEndDate(nullable:true, blank:true)
        coreStatusStart(nullable:true, blank:true)
        coreStatusEnd(nullable:true, blank:true)
        /*
        startDate(nullable:true, blank:true)
        startVolume(nullable:true, blank:true)
        startIssue(nullable:true, blank:true)
        endDate(nullable:true, blank:true)
        endVolume(nullable:true, blank:true)
        endIssue(nullable:true, blank:true)
        embargo(nullable:true, blank:true)
        coverageDepth(nullable:true, blank:true)
        coverageNote(nullable:true, blank:true)
         */

        // Nullable is true, because values are already in the database
        lastUpdated (nullable: true, blank: false)
        dateCreated (nullable: true, blank: false)
    }

    @Override
    def beforeUpdate(){
        touchPkgLastUpdated()

        super.beforeUpdate()
    }
    @Override
    def beforeInsert() {
        touchPkgLastUpdated()

        super.beforeInsert()
    }

  @Transient
  void touchPkgLastUpdated() {
    if(pkg!=null){
      use(TimeCategory) {
        pkg.lastUpdated += 1.seconds
      }
      pkg.save(failOnError:true)
    }
  }

    /*
  String getHostPlatform() {
    String result = null;
    additionalPlatforms.each { p ->
      if ( p.rel == 'host' ) {
        result = p.titleUrl
      }
    }
    result
  }
    */

  String getIdentifierValue(idtype) {
      String result
    ids?.each { ident ->
      if ( ident.ns?.ns?.toLowerCase() == idtype.toLowerCase() )
        result = ident.value
    }
    result
  }

  @Transient
  /*
  def onChange = { oldMap,newMap ->

    log.debug("onChange Tipp")

    controlledProperties.each { cp ->
      log.debug("checking ${cp}")
        if(cp != 'coverages') {
            if ( oldMap[cp] != newMap[cp] ) {
                raisePendingChange(oldMap,newMap,cp)
            }
        }
    }
    log.debug("onChange completed")
  }
    */

  void raisePendingChange(oldMap,newMap,cp) {
      def domain_class = grailsApplication.getArtefact('Domain','com.k_int.kbplus.TitleInstancePackagePlatform')
      def prop_info = domain_class.getPersistentProperty(cp)

      def oldLabel = stringify(oldMap[cp])
      def newLabel = stringify(newMap[cp])

      if ( prop_info.isAssociation() ) {
          log.debug("Convert object reference into OID")
          oldMap[cp]= oldMap[cp] != null ? "${ClassUtils.deproxy(oldMap[cp]).class.name}:${oldMap[cp].id}" : null
          newMap[cp]= newMap[cp] != null ? "${ClassUtils.deproxy(newMap[cp]).class.name}:${newMap[cp].id}" : null
      }

      log.debug("notify change event")
      changeNotificationService.fireEvent([
              OID:"${this.class.name}:${this.id}",
              event:'TitleInstancePackagePlatform.updated',
              prop:cp,
              old:oldMap[cp],
              oldLabel:oldLabel,
              new:newMap[cp],
              newLabel:newLabel
      ])
  }

    private String stringify(obj) {
      String result
    if ( obj != null ) {
      if ( obj instanceof Date ) {
          SimpleDateFormat df = new SimpleDateFormat('yyyy-MM-dd');
        result = df.format(obj);
      }
      else {
        result = obj.toString()
      }
    }
        result
    }

    /*
  @Transient
  def onSave = {

    log.debug("onSave")

    def changeNotificationService = grailsApplication.mainContext.getBean("changeNotificationService")

    changeNotificationService.fireEvent([
                                                 OID:"${this.class.name}:${this.id}",
                                                 event:'TitleInstancePackagePlatform.added',
                                                 linkedTitle:title.title,
                                                 linkedTitleId:title.id,
                                                 linkedPackage:pkg.name,
                                                 linkedPlatform:platform.name
                                                ])

  }

  @Transient
  def onDelete = {

    log.debug("onDelete")
    def changeNotificationService = grailsApplication.mainContext.getBean("changeNotificationService")

    changeNotificationService.fireEvent([
                                                 OID:"${this.class.name}:${this.id}",
                                                 event:'TitleInstancePackagePlatform.deleted',
                                                 linkedTitle:title.title,
                                                 linkedTitleId:title.id,
                                                 linkedPackage:pkg.name,
                                                 linkedPlatform:platform.name
                                                ])
  }
    */

    /*
  @Transient
  def notifyDependencies_trait(changeDocument) {
    log.debug("notifyDependencies_trait(${changeDocument})")
    changeNotificationService.broadcastEvent("com.k_int.kbplus.Package:${pkg.id}", changeDocument)
    changeNotificationService.broadcastEvent("${this.class.name}:${this.id}", changeDocument)
    Locale locale = LocaleContextHolder.getLocale()

    RefdataValue deleted_tipp_status = RDStore.TIPP_DELETED
    String deleted_tipp_status_oid = "com.k_int.kbplus.RefdataValue:${deleted_tipp_status.id}".toString()
    // Tipp Property Change Event.. notify any dependent IEs
    List<IssueEntitlement> dep_ies = IssueEntitlement.findAllByTipp(this)

    if ( ( changeDocument.event=='TitleInstancePackagePlatform.updated' ) && 
         ( changeDocument.prop == 'status' ) && 
         ( changeDocument.new == deleted_tipp_status_oid ) ) {

      log.debug("TIPP STATUS CHANGE:: Broadcast pending change to IEs based on this tipp new status: ${changeDocument.new}");
      dep_ies.each { dep_ie ->
        def sub = ClassUtils.deproxy(dep_ie.subscription)
        log.debug("Notify dependent ie ${dep_ie.id} whos sub is ${sub.id} and subscriber is ${sub.getSubscriber()}");

        if ( sub.getSubscriber() == null ) {
          // SO - Ignore!
        }
        else {
          changeNotificationService.registerPendingChange(
                  PendingChange.PROP_SUBSCRIPTION,
                                                          dep_ie.subscription,
                  // pendingChange.message_TP01
                                                          "Der Paketeintrag für den Titel \"${this.title.title}\" wurde gelöscht. Wenden Sie diese Änderung an, um die entsprechende Problembenachrichtigung aus dieser Lizenz zu entfernen",
                                                          sub.getSubscriber(),
                                                          [
                                                            changeType:PendingChangeService.EVENT_TIPP_DELETE,
                                                            tippId:"${this.class.name}:${this.id}",
                                                            subId:"${sub.id}"
                                                          ])
        }
      }
    }
    else if ( (changeDocument.event=='TitleInstancePackagePlatform.updated') && ( changeDocument.new != changeDocument.old ) ) {
        ContentItem contentItemDesc = ContentItem.findByKeyAndLocale("kbplus.change.tipp."+changeDocument.prop, locale.toString())
        String description = messageSource.getMessage('default.accept.change.ie',null,locale)
        if(contentItemDesc){
            description = contentItemDesc.content
        }
        else {
            ContentItem defaultMsg =  ContentItem.findByKeyAndLocale("kbplus.change.tipp.default",locale.toString())
            if(defaultMsg)
                description = defaultMsg.content
        }
        dep_ies.each { dep_ie ->
        def sub = ClassUtils.deproxy(dep_ie.subscription)
        if(dep_ie.subscription && sub) {
        def titleLink = grailsLinkGenerator.link(controller: 'title', action: 'show', id: this.title.id, absolute: true)
        def pkgLink =  grailsLinkGenerator.link(controller: 'package', action: 'show', id: this.pkg.id, absolute: true)
        changeNotificationService.registerPendingChange(
                PendingChange.PROP_SUBSCRIPTION,
                                                        dep_ie.subscription,
                // pendingChange.message_TP02
                                                        "Die Information vom Titel <a href=\"${titleLink}\">${this.title.title}</a> haben sich im Paket <a href=\"${pkgLink}\">${this.pkg.name}</a> geändert. " +
                                                                "<b>${messageSource.getMessage("tipp.${changeDocument.prop}",null,locale)?:changeDocument.prop}</b> wurde aktualisiert von <b>\"${changeDocument.oldLabel}\"</b>(${changeDocument.old}) zu <b>\"${changeDocument.newLabel}\"</b>" +
                                                                "(${changeDocument.new}). "+description,
                                                        sub?.getSubscriber(),
                                                        [
                                                          changeTarget:"com.k_int.kbplus.IssueEntitlement:${dep_ie.id}",
                                                          changeType:PendingChangeService.EVENT_PROPERTY_CHANGE,
                                                          changeDoc:changeDocument
                                                        ])
          
        }else{
          log.error("Something went terribly wrong, IssueEntitlement.subscription returned null.This can be DB issue.")
        }
      }
    }
    else if(changeDocument.event.contains('TitleInstancePackagePlatform.coverage')) {
        String coverageEvent = changeDocument.event.split('.coverage.')[1]
        String titleLink = grailsLinkGenerator.link(controller: 'title', action: 'show', id: this.title.id,absolute:true)
        String pkgLink =  grailsLinkGenerator.link(controller: 'package', action: 'show', id: this.pkg.id, absolute: true)
        dep_ies.each { ie ->
            if(changeDocument.affectedCoverage) {
                IssueEntitlementCoverage equivalentIECoverage = changeDocument.affectedCoverage.findEquivalent(ie.coverages)
                if(equivalentIECoverage) {
                    switch(coverageEvent) {
                        case 'deleted': String coverageRangeString = "${equivalentIECoverage.startDate?.format(messageSource.getMessage('default.date.format.notime',null,locale))} (Band ${equivalentIECoverage.startVolume}, Ausgabe ${equivalentIECoverage.startIssue}) - ${equivalentIECoverage.endDate?.format(messageSource.getMessage('default.date.format.notime',null,locale))} (Band ${equivalentIECoverage.endVolume}, Ausgabe ${equivalentIECoverage.endIssue})"
                            changeNotificationService.registerPendingChange(
                                    PendingChange.PROP_SUBSCRIPTION,
                                    ie.subscription,
                                    "Ein Lizenzzeitraum für den Titel <a href='${titleLink}'>${this.title.title}</a> des Pakets <a href='${pkgLink}'>${this.pkg.name}</a> wurde gelöscht: ${coverageRangeString}",
                                    ie.subscription.getSubscriber(),
                                    [changeTarget: "${equivalentIECoverage.class.name}:${equivalentIECoverage.id}",
                                     changeType: PendingChangeService.EVENT_COVERAGE_DELETE,
                                     changeDoc: changeDocument])
                            break
                        case 'updated': changeNotificationService.registerPendingChange(
                                PendingChange.PROP_SUBSCRIPTION,
                                ie.subscription,
                                "Ein Lizenzzeitraum für den Titel <a href='${titleLink}'>${this.title.title}</a> des Pakets <a href='${pkgLink}'>${this.pkg.name}</a> wurde geändert. Das Feld <strong>${changeDocument.propLabel}</strong> wurde geändert von ${changeDocument.old} in ${changeDocument.new}",
                                ie.subscription.getSubscriber(),
                                [changeTarget: "${equivalentIECoverage.class.name}:${equivalentIECoverage.id}",
                                 changeType: PendingChangeService.EVENT_COVERAGE_UPDATE,
                                 changeDoc: changeDocument])
                            break
                    }
                }
            }
            else if(changeDocument.coverageData) {
                Map newCov = changeDocument.coverageData
                String newCovStart = "${newCov.startDate} (Band ${newCov.startVolume}, Ausgabe ${newCov.startIssue})"
                String newCovEnd = "${newCov.endDate} (Band ${newCov.endVolume}, Ausgabe ${newCov.endIssue})"
                String newCovNotes = "(Umfang: ${newCov.coverageDepth}, Anmerkung: ${newCov.coverageNotes}, Embargo: ${newCov.embargo})"
                changeNotificationService.registerPendingChange(
                        PendingChange.PROP_SUBSCRIPTION,
                        ie.subscription,
                        "Ein Lizenzzeitraum für den Titel <a href='${titleLink}'>${this.title.title}</a> des Pakets <a href='${pkgLink}'>${this.pkg.name}</a> wurde hinzugefügt: ${newCovStart} - ${newCovEnd} ${newCovNotes}",
                        ie.subscription.getSubscriber(),
                        [changeTarget: "${ie.class.name}:${ie.id}",
                         changeType: PendingChangeService.EVENT_COVERAGE_ADD,
                         changeDoc: newCov])
            }
        }
    }
    //If the change is in a controller property, store it up and note it against subs
  }
    */

  Date getDerivedAccessStartDate() {
    accessStartDate ? accessStartDate : pkg?.startDate
  }

  Date getDerivedAccessEndDate() {
    accessEndDate ? accessEndDate : pkg?.endDate
  }

  RefdataValue getAvailabilityStatus() {
    return getAvailabilityStatus(new Date());
  }
  
  String getAvailabilityStatusAsString() {
	  String result
	  def loc = LocaleContextHolder.locale?.toString()
	  Date as_at = new Date()
      Date tipp_access_start_date = getDerivedAccessStartDate()
      Date tipp_access_end_date = getDerivedAccessEndDate()
	  
	  if ( tipp_access_end_date == null ) {
		result = RefdataValue.getByValueAndCategory("Current(*)", "TIPP Access Status").getI10n("value")
	  }
	  else if ( as_at < tipp_access_start_date ) {
		// expected
		result = RefdataValue.getByValueAndCategory("Expected", "TIPP Access Status").getI10n("value")
	  }
	  else if ( as_at > tipp_access_end_date ) {
		// expired
		result = RefdataValue.getByValueAndCategory("Expired", "TIPP Access Status").getI10n("value")
	  }
	  else {
		result = RefdataValue.getByValueAndCategory("Current", "TIPP Access Status").getI10n("value")
	  }
	  result
  }
  

  RefdataValue getAvailabilityStatus(Date as_at) {
      RefdataValue result
    // If StartDate <= as_at <= EndDate - Current
    // if Date < StartDate - Expected
    // if Date > EndDate - Expired
      Date tipp_access_start_date = getDerivedAccessStartDate()
      Date tipp_access_end_date = getDerivedAccessEndDate()

    // if ( ( accessEndDate == null ) && ( as_at > tipp_access_end_date ) ) {
    if ( tipp_access_end_date == null ) {
      result = RefdataValue.getByValueAndCategory('Current(*)', 'TIPP Access Status')
    }
    else if ( as_at < tipp_access_start_date ) {
      // expected
      result = RefdataValue.getByValueAndCategory('Expected', 'TIPP Access Status')
    }
    else if ( as_at > tipp_access_end_date ) {
      // expired
      result = RefdataValue.getByValueAndCategory('Expired', 'TIPP Access Status')
    }
    else {
      result = RefdataValue.getByValueAndCategory('Current', 'TIPP Access Status')
    }
    result
  }

    String getAvailabilityStatusExplanation() {
        return getAvailabilityStatusExplanation(new Date());
    }

    String getAvailabilityStatusExplanation(Date as_at) {
        StringWriter sw = new StringWriter()
        sw.write("This tipp is ${getAvailabilityStatus(as_at).value} as at ${as_at} because the date specified was between the start date (${getDerivedAccessStartDate()} ${accessStartDate ? 'Set explicitly on this TIPP' : 'Defaulted from package start date'}) and the end date (${getDerivedAccessEndDate()} ${accessEndDate ? 'Set explicitly on this TIPP' : 'Defaulted from package end date'})");
        return sw.toString();
    }
  /**
   * Compare the controlledPropertie of two tipps
  **/
  int compare(TitleInstancePackagePlatform tippB){
      if(!tippB) return -1;
      boolean noChange = true
      controlledProperties.each{ noChange &= this."${it}" == tippB."${it}" }
      
      if( noChange ) return 0;      
      return 1;
  }  

    @Deprecated
  static def expunge(tipp_id) {
    try {
      static_logger.debug("  -> TIPPs");
      def deleted_ie = RDStore.TIPP_DELETED
      IssueEntitlement.executeUpdate("delete from IssueEntitlement ie where ie.tipp.id=:tipp_id and ie.status=:ie_del",[tipp_id:tipp_id,ie_del:deleted_ie])
      TitleInstancePackagePlatform.executeUpdate('delete from TitleInstancePackagePlatform tipp where tipp.id = ?',[tipp_id])
    }
    catch ( Exception e ) {
      static_logger.error("Problem expunging title",e);
    }
  }

}
