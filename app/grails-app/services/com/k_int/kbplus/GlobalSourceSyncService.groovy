package com.k_int.kbplus

import de.laser.SystemEvent
import de.laser.oai.OaiClient
import com.k_int.kbplus.auth.User
import de.laser.oai.OaiClientLaser
import org.springframework.transaction.annotation.*

import java.text.SimpleDateFormat

/*
 *  Implementing new rectypes..
 *  the reconciler closure is responsible for reconciling the previous version of a record and the latest version
 *  the converter is responsible for creating the map structure passed to the reconciler. It needs to return a [:] sorted appropriate
 *  to the work the reconciler will need to do (Often this includes sorting lists)
 */

class GlobalSourceSyncService {


  def dataloadService
  public static boolean running = false;
  def genericOIDService
  def executorService
  def sessionFactory
  def propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP
  def grailsApplication
  def changeNotificationService
  boolean parallel_jobs = false
  def messageSource

  def titleReconcile = { grt ,oldtitle, newtitle ->
    log.debug("Reconcile grt: ${grt} oldtitle:${oldtitle} newtitle:${newtitle}");

    // DOes the remote title have a publisher (And is ours blank)
    def title_instance = genericOIDService.resolveOID(grt.localOid)

    if ( title_instance == null ) {
      log.debug("Failed to resolve ${grt.localOid} - Exiting");
      return
    }

    if (grailsApplication.config.globalDataSync.replaceLocalImpIds.TitleInstance && newtitle.gokbID) {
      title_instance.impId = newtitle.impId
      title_instance.gokbId = newtitle.gokbID
    }

    title_instance.status = RefdataValue.loc(RefdataCategory.TI_STATUS, [en: 'Deleted', de: 'Gelöscht'])

    if (newtitle.status == 'Current') {
      title_instance.status = RefdataValue.loc(RefdataCategory.TI_STATUS, [en: 'Current', de: 'Aktuell'])
    } else if (newtitle.status == 'Retired') {
      title_instance.status = RefdataValue.loc(RefdataCategory.TI_STATUS, [en: 'Retired', de: 'im Ruhestand'])
    }

    newtitle.identifiers.each {
      log.debug("Checking title has ${it.namespace}:${it.value}");
      title_instance.checkAndAddMissingIdentifier(it.namespace, it.value);
    }
    title_instance.save(flush: true);

    if ( newtitle.publishers != null ) {
      newtitle.publishers.each { pub ->
//         def publisher_identifiers = pub.identifiers
        def publisher_identifiers = []
        def orgSector = RefdataValue.loc('OrgSector',  [en: 'Publisher', de: 'Veröffentlicher']);
        def publisher = Org.lookupOrCreate(pub.name, orgSector, null, publisher_identifiers, null, pub.uuid)
        def pub_role = RefdataValue.loc('Organisational Role',  [en: 'Publisher', de: 'Veröffentlicher']);
        def sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        def start_date
        def end_date

        if(pub.startDate){
          start_date = sdf.parse(pub.startDate);
        }
        if(pub.endDate){
          end_date = sdf.parse(pub.endDate);
        }

        log.debug("Asserting ${publisher} ${title_instance} ${pub_role}");
        OrgRole.assertOrgTitleLink(publisher, title_instance, pub_role, ( pub.startDate ? start_date : null), ( pub.endDate ? end_date : null))
      }
    }

    // Title history!!
    newtitle.history.each { historyEvent ->
      log.debug("Processing title history event");
      // See if we already have a reference
      def fromset = []
      def toset = []

      historyEvent.from.each { he ->
        def participant = TitleInstance.lookupOrCreate(he.ids,he.title,newtitle.titleType,he.uuid)
        fromset.add(participant)
      }

      historyEvent.to.each { he ->
        def participant = TitleInstance.lookupOrCreate(he.ids,he.title,newtitle.titleType,he.uuid)
        toset.add(participant)
      }

      // Now - See if we can find a title history event for data and these particiapnts.
      // Title History Events are IMMUTABLE - so we delete them rather than updating them.
      def base_query = "select the from TitleHistoryEvent as the where"
      // Need to parse date...
      def sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
      def query_params = []

      if (historyEvent.date && historyEvent.date.trim().length() > 0) {
        query_params.add(sdf.parse(historyEvent.date))
        base_query += " the.eventDate = ? "
      }

      fromset.each {
        if (query_params.size() > 0) {
          base_query += "and"
        }

        base_query += " exists ( select p from the.participants as p where p.participant = ? and p.participantRole = 'from' ) "
        query_params.add(it)
      }
      toset.each {
        if (query_params.size() > 0) {
          base_query += "and"
        }

        base_query += " exists ( select p from the.participants as p where p.participant = ? and p.participantRole = 'to' ) "
        query_params.add(it)
      }

      def existing_title_history_event = TitleHistoryEvent.executeQuery(base_query,query_params);
      log.debug("Result of title history event lookup : ${existing_title_history_event}");

      if ( existing_title_history_event.size() == 0  ) {
        log.debug("Create new history event");
        def he = new TitleHistoryEvent(eventDate:query_params[0]).save(flush:true)
        fromset.each {
          new TitleHistoryEventParticipant(event:he, participant:it, participantRole:'from').save(flush:true)
        }
        toset.each {
          new TitleHistoryEventParticipant(event:he, participant:it, participantRole:'to').save(flush:true)
        }
      }
    }
  }

  def titleConv = { md, synctask ->
    log.debug("titleConv.... ${md}");
    def result = [:]
    result.parsed_rec = [:]
    result.parsed_rec.identifiers = []
    result.parsed_rec.history = []
    result.parsed_rec.publishers = []
    result.parsed_rec.status = md.gokb.title.status.text()
    result.parsed_rec.medium = md.gokb.title.medium.text()
    result.parsed_rec.titleType = md.gokb.title.type?.text() ?: null

    //Ebooks Fields
    result.parsed_rec.editionNumber = md.gokb.title.editionNumber?.text() ?: null
    result.parsed_rec.editionDifferentiator = md.gokb.title.editionDifferentiator?.text() ?: null
    result.parsed_rec.editionStatement = md.gokb.title.editionStatement?.text() ?: null
    result.parsed_rec.volumeNumber = md.gokb.title.volumeNumber?.text() ?: null
    result.parsed_rec.dateFirstInPrint = md.gokb.title.dateFirstInPrint?.text() ?: null
    result.parsed_rec.dateFirstOnline = md.gokb.title.dateFirstOnline?.text() ?: null

    //Ebooks Fields
    result.parsed_rec.firstAuthor = md.gokb.title.firstAuthor?.text() ?: null
    result.parsed_rec.firstEditor = md.gokb.title.firstEditor?.text() ?: null


    result.title = md.gokb.title.name.text()
    result.parsed_rec.title = md.gokb.title.name.text()

    if (md.gokb.title.'@uuid'?.text()) {
      result.parsed_rec.impId = md.gokb.title.'@uuid'.text()
    }

    if (md.gokb.title.'@uuid'?.text()) {
      result.parsed_rec.gokbID = md.gokb.title.'@uuid'.text()
    }

    md.gokb.title.publishers?.publisher.each{ pub ->
      def publisher = [:]
      publisher.name = pub.name.text()
      publisher.status = pub.status.text()
      publisher.uuid = pub.'@uuid'?.text()?.length() > 0 ? pub.'@uuid'?.text() : null
//       if ( pub.identifiers)
//         publisher.identifiers = []
//
//         pub.identifiers.identifier.each { pub_id ->
//           publisher.identifiers.add(id.'@namespace'.text():id.'@value'.text())
//         }

      if(pub.startDate){
        publisher.startDate = pub.startDate.text()
      }

      if(pub.endDate){
        publisher.endDate = pub.endDate.text()
      }

      result.parsed_rec.publishers.add(publisher)
    }
    md.gokb.title.identifiers.identifier.each { id ->
      result.parsed_rec.identifiers.add([namespace:id.'@namespace'.text(), value:id.'@value'.text()])
    }
    result.parsed_rec.identifiers.add([namespace:'uri',value:md.gokb.title.'@id'.text()]);

    md.gokb.title.history?.historyEvent.each { he ->
      def history_statement = [:]
      history_statement.internalId = he.'@id'.text()
      history_statement.date = he.date.text()
      history_statement.from = []
      history_statement.to = []

      he.from.each { hef ->
        def new_history_statement = [:]
        new_history_statement.title=hef.title.text()
        new_history_statement.ids = []
        new_history_statement.uuid = hef.'@uuid'?.text() ?: null
        hef.identifiers.identifier.each { i ->
          new_history_statement.ids.add([namespace:i.'@namespace'.text(), value:i.'@value'.text()])
        }
        new_history_statement.ids.add([namespace:'uri',value:hef.internalId.text()]);
        history_statement.from.add(new_history_statement);
      }

      he.to.each { het ->
        def new_history_statement = [:]
        new_history_statement.title=het.title.text()
        new_history_statement.ids = []
        new_history_statement.uuid = het.'@uuid'?.text() ?: null
        het.identifiers.identifier.each { i ->
          new_history_statement.ids.add([namespace:i.'@namespace'.text(), value:i.'@value'.text()])
        }
        new_history_statement.ids.add([namespace:'uri',value:het.internalId.text()]);
        history_statement.to.add(new_history_statement);
      }

      result.parsed_rec.history.add(history_statement)
    }

    log.debug(result);
    result
  }
  //tracker, old_rec_info, new_record_info)
  def packageReconcile = { grt ,oldpkg, newpkg ->
    def pkg = null;
    boolean auto_accept_flag = false

    log.debug("Reconciling new Package!")

    def scope = RefdataValue.loc(RefdataCategory.PKG_SCOPE,      [en: (newpkg?.scope)?:'Unknown']);
    def listStatus = RefdataValue.loc(RefdataCategory.PKG_LIST_STAT, [en: (newpkg?.listStatus)?:'Unknown']);
    def breakable = RefdataValue.loc(RefdataCategory.PKG_BREAKABLE, [en: (newpkg?.breakable)?:'Unknown']);
    def consistent = RefdataValue.loc(RefdataCategory.PKG_CONSISTENT, [en: (newpkg?.consistent)?:'Unknown']);
    def fixed = RefdataValue.loc(RefdataCategory.PKG_FIXED, [en: (newpkg?.fixed)?:'Unknown']);
    def paymentType = RefdataValue.loc(RefdataCategory.PKG_PAYMENTTYPE,[en: (newpkg?.paymentType)?:'Unknown']);
    def global = RefdataValue.loc(RefdataCategory.PKG_GLOBAL,[en: (newpkg?.global)?:'Unknown']);
    def isPublic = RefdataValue.loc('YN',   [en: 'Yes', de: 'Ja'])
    def ref_pprovider = RefdataValue.loc('Organisational Role',  [en: 'Content Provider', de: 'Anbieter']);

    // Firstly, make sure that there is a package for this record
    if ( grt.localOid != null ) {
      pkg = genericOIDService.resolveOID(grt.localOid)

      if( pkg && newpkg.status != 'Current' ){
        def pkg_del_status = RefdataValue.loc('Package Status', [en: 'Deleted', de: 'Gelöscht'])
        if (newpkg.status == 'Retired') {
          pkg_del_status = RefdataValue.loc('Package Status', [en: 'Retired', de: 'im Ruhestand'])
        }

        pkg.packageStatus = pkg_del_status
      }

      if( pkg ) {
        if (newpkg.gokbId && newpkg.gokbId != pkg.gokbId) {
          if(pkg.impId.startsWith('org.gokb.cred')) {
            pkg.impId = newpkg.impId
            pkg.gokbId = newpkg.gokbId
          }
          else {
            log.warn("Record tracker ${grt.id} for ${newpkg.name} pointed to a record with another import uuid: ${grt.localOid}!")

            if(grailsApplication.config.globalDataSync.replaceLocalImpIds.Package) {
              pkg.impId = newpkg.impId
              pkg.gokbId = newpkg.gokbId
            }
          }
        }else {
          if(grailsApplication.config.globalDataSync.replaceLocalImpIds.Package) {
            pkg.impId = newpkg.impId
            pkg.gokbId = newpkg.gokbId
          }
        }

        newpkg.identifiers.each {
          log.debug("Checking package has ${it.namespace}:${it.value}");
          pkg.checkAndAddMissingIdentifier(it.namespace, it.value);
        }

        if ( newpkg.packageProvider && grailsApplication.config.globalDataSync.replaceLocalImpIds.Org) {

          def orgSector = RefdataValue.getByValueAndCategory('Publisher','OrgSector')
          def orgType = RefdataValue.getByValueAndCategory('Provider','OrgRoleType')
          def orgRole = RefdataValue.loc('Organisational Role',  [en: 'Content Provider', de: 'Anbieter']);
          def provider = Org.lookupOrCreate2(newpkg.packageProvider , orgSector , null, [:], null, orgType, newpkg.packageProviderUuid?: null)

        }

      }
      //oldpkg is the pkg in Laser
      oldpkg = pkg ? pkg.toComparablePackage() : oldpkg;
      pkg.save()
    }
    else {
      // create a new package
      log.debug("Creating new Package..")

      def packageStatus = RefdataValue.loc('Package Status', [en: 'Deleted', de: 'Gelöscht'])

      if (newpkg.status == 'Current') {
        packageStatus = RefdataValue.loc('Package Status', [en: 'Current', de: 'Aktuell'])
      } else if (newpkg.status == 'Retired') {
        packageStatus = RefdataValue.loc('Package Status', [en: 'Retired', de: 'im Ruhestand'])
      }

      // Auto accept everything whilst we load the package initially
      auto_accept_flag = true;

      pkg = new Package(
              identifier: grt.identifier,
              name: grt.name,
              gokbId: grt.owner.uuid ?: grt.owner.identifier,
              autoAccept: false,
              packageType: null,
              packageStatus: packageStatus,
              packageListStatus: listStatus,
              breakable: breakable,
              consistent: consistent,
              fixed: fixed,
              isPublic: isPublic,
              packageScope: scope
      )


      if ( pkg.save() ) {
        log.debug("Saved Package as com.k_int.kbplus.Package:${pkg.id}!")

        newpkg.identifiers.each {
          log.debug("Checking package has ${it.namespace}:${it.value}");
          pkg.checkAndAddMissingIdentifier(it.namespace, it.value);
        }

        if ( newpkg.packageProvider ) {

          def orgSector = RefdataValue.getByValueAndCategory('Publisher','OrgSector')
          def orgType = RefdataValue.getByValueAndCategory('Provider','OrgRoleType')
          def orgRole = RefdataValue.loc('Organisational Role',  [en: 'Content Provider', de: 'Anbieter']);
          def provider = Org.lookupOrCreate2(newpkg.packageProvider , orgSector , null, [:], null, orgType, newpkg.packageProviderUuid?: null)

          OrgRole.assertOrgPackageLink(provider, pkg, orgRole)
        }

        grt.localOid = "com.k_int.kbplus.Package:${pkg.id}"
        grt.save()


      }
    }

    def onNewTipp = { ctx, tipp, auto_accept ->
      def sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
      log.debug("new tipp: ${tipp}");
      log.debug("identifiers: ${tipp.title.identifiers}");

      def title_instance = TitleInstance.findByGokbId(tipp.title?.gokbId)

      if(!title_instance) {
        title_instance = TitleInstance.lookupOrCreate(tipp.title.identifiers, tipp.title.name, tipp.title.titleType, tipp.title.gokbId)
      }
      println("Result of lookup or create for ${tipp.title.name} with identifiers ${tipp.title.identifiers} is ${title_instance}");

      /*if (grailsApplication.config.globalDataSync.replaceLocalImpIds.TitleInstance &&
              title_instance &&  tipp.title.gokbId &&
              (title_instance.gokbId !=  tipp.title.gokbId || !title_instance.gokbId)) {
        title_instance.impId = tipp.title.gokbId
        title_instance.gokbId = tipp.title.gokbId
        title_instance.save(flush: true)
      }*/

      def origin_uri = null
      tipp.title.identifiers.each { i ->
        if (i.namespace.toLowerCase() == 'uri') {
          origin_uri = i.value
        }
      }
      updatedTitleafterPackageReconcile(grt, origin_uri, title_instance.id)

      def plat_instance = Platform.lookupOrCreatePlatform([name:tipp.platform, gokbId: tipp.platformUuid]);
      def tipp_status_str = tipp.status ? tipp.status.capitalize():'Current'
      def tipp_status = RefdataCategory.lookupOrCreate(RefdataCategory.TIPP_STATUS,tipp_status_str);

      if ( auto_accept ) {
        TitleInstancePackagePlatform new_tipp = new TitleInstancePackagePlatform()
        new_tipp.pkg = ctx;
        new_tipp.platform = plat_instance;
        new_tipp.title = title_instance;
        new_tipp.status = tipp_status;
        new_tipp.impId = tipp.tippUuid ?: tipp.tippId;
        new_tipp.gokbId = tipp.tippUuid ?: null;
        new_tipp.accessStartDate = tipp.accessStart
        new_tipp.accessEndDate = tipp.accessEnd

        // We rely upon there only being 1 coverage statement for now, it seems likely this will need
        // to change in the future.
        // tipp.coverage.each { cov ->
        def cov = tipp.coverage[0]
        new_tipp.startDate = cov.startDate
        new_tipp.startVolume = cov.startVolume;
        new_tipp.startIssue = cov.startIssue;
        new_tipp.endDate = cov.endDate
        new_tipp.endVolume = cov.endVolume;
        new_tipp.endIssue = cov.endIssue;
        new_tipp.embargo = cov.embargo;
        new_tipp.coverageDepth = cov.coverageDepth;
        new_tipp.coverageNote = cov.coverageNote;
        // }
        new_tipp.hostPlatformURL=tipp.url;

        new_tipp.save(flush:true, failOnError:true);

        if (tipp.tippId){
          def tipp_id = Identifier.lookupOrCreateCanonicalIdentifier('uri', tipp.tippId)

          if(tipp_id){
            def tipp_io = new IdentifierOccurrence(identifier:tipp_id, tipp:new_tipp).save(flush:true)
          }else{
            log.error("Error creating identifier instance for new TIPP!")
          }
        }
      }
      else {
        log.debug("Register new tipp event for user to accept or reject");

        def locale = org.springframework.context.i18n.LocaleContextHolder.getLocale()
        def sdf2 = new SimpleDateFormat(messageSource.getMessage('default.date.format.notime', null, 'yyyy-MM-dd', locale));
        def datetoday = sdf2.format(new Date(System.currentTimeMillis()))

        def cov = tipp.coverage[0]
        def change_doc = [
                pkg          : [id: ctx.id],
                platform     : [id: plat_instance?.id],
                title        : [id: title_instance?.id],
                impId        : tipp.tippUuid ?: tipp.tippId,
                gokbId       : tipp.tippUuid ?: null,
                status       : [id: tipp_status.id],
                startDate    : cov.startDate,
                startVolume  : cov.startVolume,
                startIssue   : cov.startIssue,
                endDate      : cov.endDate,
                endVolume    : cov.endVolume,
                endIssue     : cov.endIssue,
                embargo      : cov.embargo,
                coverageDepth: cov.coverageDepth,
                coverageNote : cov.coverageNote,
                accessStartDate  : tipp.accessStart,
                accessEndDate    : tipp.accessEnd,
                hostPlatformURL: tipp.url
        ]

        changeNotificationService.registerPendingChange(
                PendingChange.PROP_PKG,
                ctx,
                // pendingChange.message_GS01
                "Eine neue Verknüpfung (TIPP) für den Titel ${title_instance.title} mit der Plattform ${plat_instance.name} (${datetoday})",
                null,
                [
                        newObjectClass: "com.k_int.kbplus.TitleInstancePackagePlatform",
                        changeType    : PendingChangeService.EVENT_OBJECT_NEW,
                        changeDoc     : change_doc
                ])

      }
    }

    def onUpdatedTipp = { ctx, tipp, oldtipp, changes, auto_accept ->
      log.debug("updated tipp, ctx = ${ctx.toString()}");

      // Find title with ID tipp... in package ctx

      def title_of_tipp_to_update = TitleInstance.findByGokbId(tipp.title.gokbId)

      if(!title_of_tipp_to_update) {
        title_of_tipp_to_update = TitleInstance.lookupOrCreate(tipp.title.identifiers, tipp.title.name, tipp.title.titleType, tipp.title.gokbId)
      }

      /*if (grailsApplication.config.globalDataSync.replaceLocalImpIds.TitleInstance &&
              title_of_tipp_to_update &&  tipp.title.gokbId &&
              (title_of_tipp_to_update?.gokbId !=  tipp.title.gokbId || !title_of_tipp_to_update?.gokbId))
      {
        title_of_tipp_to_update.impId = tipp.title.gokbId
        title_of_tipp_to_update.gokbId = tipp.title.gokbId
        title_of_tipp_to_update.save(flush: true)
      }*/


      def origin_uri = null
      tipp.title.identifiers.each { i ->
        if (i.namespace.toLowerCase() == 'uri') {
          origin_uri = i.value
        }
      }
      updatedTitleafterPackageReconcile(grt, origin_uri, title_of_tipp_to_update.id)

      def db_tipp = null

      if(tipp.tippUuid) {
        db_tipp = ctx.tipps.find { it.gokbId == tipp.tippUuid }
      }

      if(!db_tipp) {
        db_tipp = ctx.tipps.find { it.impId == tipp.tippUuid}

      }

      if(!db_tipp) {
        db_tipp = ctx.tipps.find { it.impId == tipp.tippId }
      }

      if ( db_tipp != null) {

        def tippStatus = RefdataValue.loc(RefdataCategory.TIPP_STATUS, [en: 'Deleted', de: 'Gelöscht'])

        if (tipp.status == 'Current') {
          tippStatus = RefdataValue.loc(RefdataCategory.TIPP_STATUS, [en: 'Current', de: 'Aktuell'])
        } else if (tipp.status == 'Retired') {
          tippStatus = RefdataValue.loc(RefdataCategory.TIPP_STATUS, [en: 'Retired', de: 'im Ruhestand'])
        }

        def changetext
        def change_doc = [:]

        def contextObject = genericOIDService.resolveOID("Package:${ctx.id}");
        def locale = org.springframework.context.i18n.LocaleContextHolder.getLocale()
        def announcement_content_changeTitle = "<p>${messageSource.getMessage('announcement.title.ChangeTitle', null, "Change Title in Package ", locale)}  ${contextObject.getURL() ? "<a href=\"${contextObject.getURL()}\">${contextObject.name}</a>" : "${contextObject.name}"} ${new Date().toString()}</p><p><ul>"
        def changeTitle = false

        changes.each { chg ->

          if ("${chg.field}" == "accessStart") {
            changetext = changetext ? changetext + ", accessStartDate: (vorher: ${oldtipp?.accessStart}, nachher: ${tipp?.accessStart})" : "accessStartDate: (vorher: ${oldtipp?.accessStart}, nachher: ${tipp?.accessStart})"
            change_doc.put("accessStartDate", tipp.accessStart)
          }
          if ("${chg.field}" == "accessEnd") {
            changetext = changetext ? changetext + ", accessEndDate: (vorher: ${oldtipp?.accessEnd}, nachher: ${tipp?.accessEnd})" : "accessEndDate: (vorher: ${oldtipp?.accessEnd}, nachher: ${tipp?.accessEnd})"
            change_doc.put("accessEndDate", tipp.accessEnd)

          }
          if ("${chg.field}" == "coverage") {
            changetext = changetext ?
                    changetext + ", Coverage: (Start Date:${tipp.coverage[0].startDate}, Start Volume:${tipp.coverage[0].startVolume}, Start Issue:${tipp.coverage[0].startIssue}, End Date:${tipp.coverage[0].endDate} " +
                            ", End Volume:${tipp.coverage[0].endVolume}, End Issue:${tipp.coverage[0].endIssue}, Embargo:${tipp.coverage[0].embargo}, Coverage Depth:${tipp.coverage[0].coverageDepth}, Coverage Note:${tipp.coverage[0].coverageNote})" :
                            "Coverage: (Start Date:${tipp.coverage[0].startDate}, Start Volume:${tipp.coverage[0].startVolume}, Start Issue:${tipp.coverage[0].startIssue}, End Date:${tipp.coverage[0].endDate} , End Volume:${tipp.coverage[0].endVolume}, End Issue:${tipp.coverage[0].endIssue}, Embargo:${tipp.coverage[0].embargo}, Coverage Depth:${tipp.coverage[0].coverageDepth}, Coverage Note:${tipp.coverage[0].coverageNote})"
            change_doc.put("startDate", tipp.coverage[0].startDate)
            change_doc.put("startVolume", tipp.coverage[0].startVolume)
            change_doc.put("startIssue", tipp.coverage[0].startIssue)
            change_doc.put("endDate", tipp.coverage[0].endDate)
            change_doc.put("endVolume", tipp.coverage[0].endVolume)
            change_doc.put("endIssue", tipp.coverage[0].endIssue)
            change_doc.put("embargo", tipp.coverage[0].embargo)
            change_doc.put("coverageDepth", tipp.coverage[0].coverageDepth)
            change_doc.put("coverageNote", tipp.coverage[0].coverageNote)
          }
          if ("${chg.field}" == "hostPlatformURL") {
            changetext = changetext ? changetext + ", URL: (vorher: ${oldtipp.url}, nachher: ${tipp.url})" : "URL: (vorher: ${oldtipp.url}, nachher: ${tipp.url})"
            change_doc.put("hostPlatformURL", tipp.url)

          }
          if ("${chg.field}" == "titleName") {
            changeTitle = true
            announcement_content_changeTitle += "<li>${messageSource.getMessage("announcement.title.TitleChange", [chg.oldValue, chg.newValue] as Object[],"Title was change from {0} to {1}.", locale)}</li>"
            title_of_tipp_to_update.title = tipp?.title?.name
            title_of_tipp_to_update.save(flush:true)
          }

        }

        if(changeTitle){
          def announcement_type = RefdataValue.getByValueAndCategory('Announcement','Document Type')
          def newAnnouncement = new Doc(title:'Automated Announcement',
                  type:announcement_type,
                  content:announcement_content_changeTitle+"</ul></p>",
                  dateCreated:new Date(),
                  user: User.findByUsername('admin')).save(flush:true);
        }

        if (change_doc) {
          changeNotificationService.registerPendingChange(
                  PendingChange.PROP_PKG,
                  ctx,
                  // pendingChange.message_GS02
                  "Eine TIPP/Coverage Änderung für den Titel \"${title_of_tipp_to_update?.title}\", ${changetext}, Status: ${tippStatus}",
                  null,
                  [
                          changeTarget: "com.k_int.kbplus.TitleInstancePackagePlatform:${db_tipp.id}",
                          changeType  : PendingChangeService.EVENT_OBJECT_UPDATE,
                          changeDoc   : change_doc
                  ])
        } else {
          throw new RuntimeException("changeDoc is empty. ctx:${ctx}, tipp:${tipp}");
        }
      }
      else {
        throw new RuntimeException("Unable to locate TIPP for update. ctx:${ctx}, tipp:${tipp}");
      }
    }

    def onDeletedTipp = { ctx, tipp, auto_accept ->

      // Find title with ID tipp... in package ctx

      def title_of_tipp_to_update = TitleInstance.findByGokbId(tipp.title.gokbId)

      if(!title_of_tipp_to_update) {
        title_of_tipp_to_update = TitleInstance.lookupOrCreate(tipp.title.identifiers, tipp.title.name, tipp.title.titleType, tipp.title.gokbId)
      }

      /*if (grailsApplication.config.globalDataSync.replaceLocalImpIds.TitleInstance &&
              title_of_tipp_to_update &&  tipp.title.gokbId &&
              (title_of_tipp_to_update?.gokbId !=  tipp.title.gokbId || !title_of_tipp_to_update?.gokbId)) {
        title_of_tipp_to_update.impId = tipp.title.gokbId
        title_of_tipp_to_update.gokbId = tipp.title.gokbId
        title_of_tipp_to_update.save(flush: true)
      }*/

      def TippStatus = RefdataValue.loc(RefdataCategory.TIPP_STATUS, [en: 'Deleted', de: 'Gelöscht'])

      if (tipp.status == 'Retired') {
          TippStatus = RefdataValue.loc(RefdataCategory.TIPP_STATUS, [en: 'Retired', de: 'im Ruhestand'])
      }

      def db_tipp = null

      if(tipp.tippUuid) {
        db_tipp = ctx.tipps.find { it.gokbId == tipp.tippUuid }
      }
      if(!db_tipp) {
        db_tipp = ctx.tipps.find { it.impId == tipp.tippUuid}

      }
      if(!db_tipp) {
        db_tipp = ctx.tipps.find { it.impId == tipp.tippId }
      }

      if (db_tipp != null && !(db_tipp.status.equals(TippStatus))) {

        def change_doc = [status: TippStatus]

        changeNotificationService.registerPendingChange(
                PendingChange.PROP_PKG,
                ctx,
                // pendingChange.message_GS03
                "Eine Statusänderung für den TIPP mit dem Titel \"${title_of_tipp_to_update.title}\", Status: ${TippStatus}",
                null,
                [
                        changeTarget: "com.k_int.kbplus.TitleInstancePackagePlatform:${db_tipp.id}",
                        changeType  : PendingChangeService.EVENT_OBJECT_UPDATE,
                        changeDoc   : change_doc
                ])
        log.debug("deleted tipp");
      }

    }

    def onPkgPropChange = { ctx, propname, value, auto_accept ->
      def oldvalue
      def announcement_content
      switch (propname) {
        case 'title':
          def contextObject = genericOIDService.resolveOID("Package:${ctx.id}");
          oldvalue = ctx.name
          ctx.name = value
          def locale = org.springframework.context.i18n.LocaleContextHolder.getLocale()
          announcement_content = "<p>${messageSource.getMessage('announcement.package.ChangeTitle', null, "Change Package Title on ", locale)}  ${contextObject.getURL() ? "<a href=\"${contextObject.getURL()}\">${ctx.name}</a>" : "${ctx.name}"} ${new Date().toString()}</p>"
          announcement_content += "<p><ul><li>${messageSource.getMessage("announcement.package.TitleChange", [oldvalue, value] as Object[],"Package Title was change from {0} to {1}.", locale)}</li></ul></p>"
          log.debug("updated pkg prop");
          break;
        default:
          log.debug("Not updated pkg prop");
          break;
      }

      if(auto_accept){
        ctx.save(flush:true)

        def announcement_type = RefdataValue.getByValueAndCategory('Announcement','Document Type')
        def newAnnouncement = new Doc(title:'Automated Announcement',
              type:announcement_type,
              content:announcement_content,
              dateCreated:new Date(),
              user: User.findByUsername('admin')).save(flush:true);
      }

    }

    def onTippUnchanged = {ctx, tippa ->
    }

    com.k_int.kbplus.GokbDiffEngine.diff(pkg, oldpkg, newpkg, onNewTipp, onUpdatedTipp, onDeletedTipp, onPkgPropChange, onTippUnchanged, auto_accept_flag)

  }

  def testTitleCompliance = { json_record ->
    log.debug("testTitleCompliance:: ${json_record}");

    def result = RefdataValue.loc('YNO',  [en: 'No', de: 'Nein'])

    if ( json_record.identifiers?.size() > 0 ) {
      result = RefdataValue.loc('YNO',  [en: 'Yes', de: 'Ja'])
    }

    result
  }

  // def testKBPlusCompliance = { json_record ->
  def testPackageCompliance = { json_record ->
    // Iterate through all titles..
    def error = false
    def result = null
    def problem_titles = []

    log.debug(json_record.packageName);
    log.debug(json_record.packageId);

    // GOkb records containing titles with no identifiers are not valid in KB+ land
    json_record?.tipps.each { tipp ->
      log.debug(tipp.title.name);
      // tipp.title.identifiers
      if ( tipp.title?.identifiers?.size() > 0 ) {
        // No problem
      }
      else {
        problem_titles.add(tipp.title.titleId)
        error = true
      }

      // tipp.titleid
      // tipp.platform
      // tipp.platformId
      // tipp.coverage
      // tipp.url
      // tipp.identifiers
    }

    if ( error ) {
      result = RefdataValue.loc('YNO',  [en: 'No', de: 'Nein'])
    }
    else {
      result = RefdataValue.loc('YNO',  [en: 'Yes', de: 'Ja'])
    }

    result
  }
  def packageConv = { md, synctask ->
    log.debug("Package conv...");
    // Convert XML to internal structure and return
    def result = [:]
    // result.parsed_rec = xml.text().getBytes();
    result.title = md.gokb.package.name.text()

    result.parsed_rec = [:]
    result.parsed_rec.packageName = md.gokb.package.name.text()
    result.parsed_rec.packageId = md.gokb.package.'@id'.text()
    result.parsed_rec.impId = md.gokb.package.'@uuid'?.text() ?: null
    result.parsed_rec.gokbId = md.gokb.package.'@uuid'?.text() ?: null
    result.parsed_rec.packageProvider = md.gokb.package.nominalProvider.name.text()
    result.parsed_rec.packageProviderUuid = md.gokb.package.nominalProvider.'@uuid'?.text() ?: null
    result.parsed_rec.tipps = []
    result.parsed_rec.identifiers = []
    result.parsed_rec.status = md.gokb.package.status.text()
    result.parsed_rec.scope = md.gokb.package.scope.text()
    result.parsed_rec.listStatus = md.gokb.package.listStatus.text()
    result.parsed_rec.breakable = md.gokb.package.breakable.text()
    result.parsed_rec.consistent = md.gokb.package.consistent.text()
    result.parsed_rec.fixed = md.gokb.package.fixed.text()
    result.parsed_rec.global = md.gokb.package.global.text()
    result.parsed_rec.paymentType = md.gokb.package.paymentType.text()
    result.parsed_rec.nominalPlatform = md.gokb.package.nominalPlatform.name.text()
    result.parsed_rec.nominalPlatformUuid = md.gokb.package.nominalPlatform.'@uuid'?.text() ?: null
    result.parsed_rec.nominalPlatformPrimaryUrl = md.gokb.package.nominalPlatform.primaryUrl.text() ?: null

    md.gokb.package.identifiers.identifier.each { id ->
      result.parsed_rec.identifiers.add([namespace:id.'@namespace'.text(), value:id.'@value'.text()])
    }
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    int ctr=0
    md.gokb.package.TIPPs.TIPP.each { tip ->
      log.debug("Processing tipp ${ctr++} from package ${result.parsed_rec.packageId} - ${result.title} (source:${synctask.uri})");
      def title_simplename = tip.title.mediumByTypClass?.text() ?: (tip.title.type?.text() ?: null)

      def newtip = [
              title      : [
                      name       : tip.title.name.text(),
                      identifiers: [],
                      impId: tip.title.'@uuid'?.text() ?: null,
                      gokbId: tip.title.'@uuid'?.text() ?: null,
                      titleType: title_simplename ?: null
              ],
              status      : tip.status?.text() ?: 'Current',
              titleId     : tip.title.'@id'.text(),
              titleUuid   : tip.title.'@uuid'?.text() ?: null,
              platform    : tip.platform.name.text(),
              platformId  : tip.platform.'@id'.text(),
              platformUuid: tip.platform.'@uuid'?.text() ?: null,
              coverage    : [],
              url         : tip.url.text() ?: '',
              identifiers : [],
              tippId      : tip.'@id'.text(),
              tippUuid    : tip.'@uuid'?.text()?: '',
              accessStart : tip.access.'@start'.text() ? sdf.parse(tip.access.'@start'.text()).format('yyyy-MM-dd HH:mm:ss.S') : null,
              accessEnd   : tip.access.'@end'.text() ? sdf.parse(tip.access.'@end'.text()).format('yyyy-MM-dd HH:mm:ss.S') : null,
              medium      : tip.medium.text()
      ];

      tip.coverage.each { cov ->
        newtip.coverage.add([
                startDate    : cov.'@startDate'.text() ? sdf.parse(cov.'@startDate'.text()).format('yyyy-MM-dd HH:mm:ss.S') : null,
                endDate      : cov.'@endDate'.text() ? sdf.parse(cov.'@endDate'.text()).format('yyyy-MM-dd HH:mm:ss.S') : null,
                startVolume  : cov.'@startVolume'.text() ?: '',
                endVolume    : cov.'@endVolume'.text() ?: '',
                startIssue   : cov.'@startIssue'.text() ?: '',
                endIssue     : cov.'@endIssue'.text() ?: '',
                coverageDepth: cov.'@coverageDepth'.text() ?: '',
                coverageNote : cov.'@coverageNote'.text() ?: '',
                embargo      : cov.'@embargo'.text() ?: ''
        ]);
      }

      tip.title.identifiers.identifier.each { id ->
        newtip.title.identifiers.add([namespace:id.'@namespace'.text(), value:id.'@value'.text()]);
      }
      newtip.title.identifiers.add([namespace:'uri',value:newtip.titleId]);

      newtip.identifiers.add([namespace:'uri',value:newtip.tippId]);

      //log.debug("Harmonise identifiers");
      //harmoniseTitleIdentifiers(newtip);

      result.parsed_rec.tipps.add(newtip)
    }

    result.parsed_rec.tipps.sort{it.tippId}
    log.debug("Rec conversion for package returns object with title ${result.parsed_rec.title} and ${result.parsed_rec.tipps?.size()} tipps");
    return result
  }


  // We always match a remote title against a local one, or create a local one to mirror the remote
  // definition. Having created the remote title, we synchronize the other details (Title History for example)
  // using the standard reconciler with the new info and null as the old info - essentially a full update the first time.
  def onNewTitle = { global_record_info, newtitle ->

    log.debug("onNewTitle.... ${global_record_info} ${newtitle} ");

    // We need to create a new global record tracker. If there is already a local title for this remote title, link to it,
    // otherwise create a new title and link to it. See if we can locate a title.
    def title_type = null

    if(newtitle.titleType) {
      title_type = newtitle.titleType
    }else{
      title_type = newtitle.medium + "Instance"
    }


    def title_instance = TitleInstance.lookupOrCreate(newtitle.identifiers,newtitle.title, title_type, newtitle.impId)

    if ( title_instance != null ) {

      title_instance.refresh()

      // merge in any new identifiers we have
      newtitle.identifiers.each {
        log.debug("Checking title has ${it.namespace}:${it.value}");
        title_instance.checkAndAddMissingIdentifier(it.namespace, it.value);
      }
      title_instance.save(flush: true)


      log.debug("Creating new global record tracker... for title ${title_instance}");


      def grt = new GlobalRecordTracker(
              owner: global_record_info,
              localOid: title_instance.class.name + ':' + title_instance.id,
              identifier: java.util.UUID.randomUUID().toString(),
              name: newtitle.title
      ).save(flush:true)

      log.debug("call title reconcile");
      titleReconcile(grt, null, newtitle)
    }
    else {
      log.error("Unable to lookup or create title... ids:${newtitle.identifiers}, title:${newtitle.title}");
    }
  }


  // Main configuration map
  def rectypes = [
          [name: 'Package', converter: packageConv, reconciler: packageReconcile, newRemoteRecordHandler: null, complianceCheck: testPackageCompliance],
          [name: 'Title', converter: titleConv, reconciler: titleReconcile, newRemoteRecordHandler: onNewTitle, complianceCheck: testTitleCompliance],
  ]

  def runAllActiveSyncTasks() {

    if ( running == false ) {
      def future = executorService.submit({ internalRunAllActiveSyncTasks() } as java.util.concurrent.Callable)
    }
    else {
      log.warn("Not starting duplicate OAI thread");
    }
  }

  def internalRunAllActiveSyncTasks() {

    running = true;

    def jobs = GlobalRecordSource.findAll()

    jobs.each { sync_job ->
      log.debug(sync_job);
      // String identifier
      // String name
      // String type
      // Date haveUpTo
      // String uri
      // String listPrefix
      // String fullPrefix
      // String principal
      // String credentials
      switch (sync_job.type) {
        case 'OAI':
          log.debug("start internal sync");
          this.doOAISync(sync_job)
          log.debug("this.doOAISync has returned...");
          break;
        default:
          log.error("Unhandled sync job type: ${sync_job.type}");
          break;
      }
    }
    running = false
  }

  def private doOAISync(sync_job) {
    log.debug("doOAISync");
    if ( parallel_jobs ) {
      def future = executorService.submit({ intOAI(sync_job.id) } as java.util.concurrent.Callable)
    }
    else {
      intOAI(sync_job.id)
    }
    log.debug("doneOAISync");
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  def intOAI(sync_job_id) {

    log.debug("internalOAI processing ${sync_job_id}");

    // TODO: remove due SystemEvent
    new EventLog(event:'kbplus.doOAISync', message:"internalOAI processing ${sync_job_id}", tstp:new Date(System.currentTimeMillis())).save(flush:true)

      SystemEvent.createEvent('GSSS_OAI_START', ['jobId': sync_job_id])

    def sync_job = GlobalRecordSource.get(sync_job_id)
    int rectype = sync_job.rectype.longValue()
    def cfg = rectypes[rectype]
    def olddate = sync_job.haveUpTo

    Thread.currentThread().setName("GlobalDataSync");

    try {

      log.debug("Rectype: ${rectype} == config ${cfg}");

      log.debug("internalOAISync records from [job ${sync_job_id}] ${sync_job.uri} since ${sync_job.haveUpTo} using ${sync_job.fullPrefix}");

      if (cfg == null) {
        throw new RuntimeException("Unable to resolve config for ID ${sync_job.rectype}");
      }

      def sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

      def date = sync_job.haveUpTo

      log.debug("upto: ${date} uri:${sync_job.uri} prefix:${sync_job.fullPrefix}");

      def oai_client = new OaiClient(host: sync_job.uri)
      def max_timestamp = 0
      def ctr = 0

      log.debug("Collect ${cfg.name} changes since ${date}");

      oai_client.getChangesSince(date, sync_job.fullPrefix) { rec ->

        def sync_obj = GlobalRecordSource.get(sync_job_id)
        log.debug("Got OAI Record ${rec.header.identifier} datestamp: ${rec.header.datestamp} job:${sync_obj.id} url:${sync_obj.uri} cfg:${cfg.name}")
        def rec_uuid = rec.header.uuid?.text() ?: null
        def rec_identifier = rec.header.identifier.text()
        def qryparams = [sync_obj.id, rec_identifier, rec_uuid ?: "0"]
        def record_timestamp = sdf.parse(rec.header.datestamp.text())
        def existing_record_info = null

        def found_record_info = GlobalRecordInfo.executeQuery('select r from GlobalRecordInfo as r where (r.source.id = ? and r.identifier = ?) OR (r.uuid IS NOT NULL AND r.uuid = ?)', qryparams);

        if (found_record_info.size() == 1) {
          existing_record_info = found_record_info[0]
        }else if (found_record_info.size() > 1) {

          found_record_info.each { fri ->
            if(rec_uuid) {
              if (fri.uuid && fri.uuid == rec_uuid){
                existing_record_info = fri
              }
            } else {
              if (!existing_record_info) {
                existing_record_info = found_record_info[0]
              }else {
                log.warn("Found multiple record infos with identifier ${rec_identifier}!")
              }
            }
          }
        }

        if (existing_record_info) {
          log.debug("convert xml into json - config is ${cfg} ");
          def parsed_rec = cfg.converter.call(rec.metadata, sync_obj)

          // Deserialize
          def bais = new ByteArrayInputStream((byte[]) (existing_record_info.record))
          def ins = new ObjectInputStream(bais);
          def old_rec_info = ins.readObject()
          ins.close()
          def new_record_info = parsed_rec.parsed_rec

          if(!existing_record_info.uuid && rec_uuid) {
            existing_record_info.uuid = rec_uuid
          }

          // For each tracker we need to update the local object which reflects that remote record
          existing_record_info.trackers.each { tracker ->
            cfg.reconciler.call(tracker, old_rec_info, new_record_info)
          }

          log.debug("Calling compliance check, cfg name is ${cfg.name}");
          existing_record_info.kbplusCompliant = cfg.complianceCheck.call(parsed_rec.parsed_rec)
          log.debug("Result of compliance check: ${existing_record_info.kbplusCompliant}");

          // Finally, update our local copy of the remote object
          def baos = new ByteArrayOutputStream()
          def out = new ObjectOutputStream(baos)
          out.writeObject(new_record_info)
          out.close()
          existing_record_info.record = baos.toByteArray();
          existing_record_info.desc = "Package ${parsed_rec.title} consisting of ${parsed_rec.parsed_rec.tipps?.size()} titles"


          def status = RefdataValue.loc("${cfg.name} Status", [en: 'Deleted', de: 'Gelöscht'])

          if (parsed_rec.parsed_rec.status == 'Current') {
            status = RefdataValue.loc("${cfg.name} Status", [en: 'Current', de: 'Aktuell'])
          } else if (parsed_rec.parsed_rec.status == 'Retired') {
            status = RefdataValue.loc("${cfg.name} Status", [en: 'Retired', de: 'im Ruhestand'])
          }

          existing_record_info.globalRecordInfoStatus = status
          existing_record_info.save()
        } else {
          log.debug("First time we have seen this record - converting ${cfg.name}");
          def parsed_rec = cfg.converter.call(rec.metadata, sync_obj)
          log.debug("Converter thinks this rec has title :: ${parsed_rec.title}");

          // Evaluate the incoming record to see if it meets KB+ stringent data quality standards
          log.debug("Calling compliance check, cfg name is ${cfg.name}");
          def kbplus_compliant = cfg.complianceCheck.call(parsed_rec.parsed_rec)
          // RefdataCategory.lookupOrCreate("YNO","No")
          log.debug("Result of compliance [new] check: ${kbplus_compliant}");

          def baos = new ByteArrayOutputStream()
          def out = new ObjectOutputStream(baos)
          log.debug("write object ${parsed_rec.parsed_rec}");
          out.writeObject(parsed_rec.parsed_rec)

          log.debug("written, closed...");

          out.close()

          log.debug("Create new GlobalRecordInfo");

          def status = RefdataValue.loc("${cfg.name} Status", [en: 'Deleted', de: 'Gelöscht'])
          if (parsed_rec.parsed_rec.status == 'Current') {
            status = RefdataValue.loc("${cfg.name} Status", [en: 'Current', de: 'Aktuell'])
          } else if (parsed_rec.parsed_rec.status == 'Retired') {
            status = RefdataValue.loc("${cfg.name} Status", [en: 'Retired', de: 'im Ruhestand'])
          }

          // Because we don't know about this record, we can't possibly be already tracking it. Just create a local tracking record.
          existing_record_info = new GlobalRecordInfo(
                  ts: record_timestamp,
                  name: parsed_rec.title,
                  identifier: rec.header.identifier.text(),
                  uuid: rec_uuid,
                  desc: "${parsed_rec.title}",
                  source: sync_obj,
                  rectype: sync_obj.rectype,
                  record: baos.toByteArray(),
                  kbplusCompliant: kbplus_compliant,
                  globalRecordInfoStatus: status);

          if (existing_record_info.save(flush: true)) {
            log.debug("existing_record_info created ok");
          } else {
            log.error("Problem saving record info: ${existing_record_info.errors}");
          }

          if (kbplus_compliant?.value == 'Yes') {
            if (cfg.newRemoteRecordHandler != null) {
              log.debug("Calling new remote record handler...");
              cfg.newRemoteRecordHandler.call(existing_record_info, parsed_rec.parsed_rec)
              log.debug("Call completed");
            } else {
              log.debug("No new record handler");
            }
          } else {
            log.debug("Skip record - not KBPlus compliant");
          }
        }

        if (record_timestamp?.getTime() > max_timestamp) {
          max_timestamp = record_timestamp?.getTime()
          log.debug("Max timestamp is now ${record_timestamp}");
        }

        log.debug("Updating sync job max timestamp");
        sync_obj.haveUpTo = new Date(max_timestamp)
        sync_obj.save(flush: true);

        if (rectype == 'Package') {
          sleep(3000);
          cleanUpGorm()
        }
        else if (ctr++ % 200 == 0) {
          cleanUpGorm()
        }
      }
    }
    catch ( Exception e ) {
      log.error("Problem",e);
      log.error("Problem running job ${sync_job_id}, conf=${cfg}",e);
      // TODO: remove due SystemEvent
      new EventLog(event:'kbplus.doOAISync', message:"Problem running job ${sync_job_id}, conf=${cfg}", tstp:new Date(System.currentTimeMillis())).save(flush:true)

        SystemEvent.createEvent('GSSS_OAI_ERROR', ['jobId': sync_job_id, 'conf': cfg])?.save(flush:true)

      log.debug("Reset sync job haveUpTo");
      def sync_object = GlobalRecordSource.get(sync_job_id)
      sync_object.haveUpTo = olddate
      sync_object.save(flush: true);
    }
    finally {
      log.debug("internalOAISync completed for job ${sync_job_id}");

      SystemEvent.createEvent('GSSS_OAI_COMPLETE', ['jobId': sync_job_id])

      // TODO: remove due SystemEvent
      new EventLog(event:'kbplus.doOAISync', message:"internalOAISync completed for job ${sync_job_id}", tstp:new Date(System.currentTimeMillis())).save(flush:true)
    }
  }

  def parseDate(datestr, possible_formats) {
    def parsed_date = null;
    if ( datestr && ( datestr.toString().trim().length() > 0 ) ) {
      for(Iterator i = possible_formats.iterator(); ( i.hasNext() && ( parsed_date == null ) ); ) {
        try {
          parsed_date = i.next().parse(datestr.toString());
        }
        catch ( Exception e ) {
        }
      }
    }
    parsed_date
  }

  def dumpPkgRec(pr) {
    log.debug(pr);
  }

  def initialiseTracker(grt) {

    def newrecord = reloadAndSaveRecordofPackage(grt)

    int rectype = grt.owner.rectype.longValue()
    def cfg = rectypes[rectype]

    def oldrec = [:]
    oldrec.tipps=[]
    def bais = new ByteArrayInputStream((byte[])(grt.owner.record))
    def ins = new ObjectInputStream(bais);
    def newrec = ins.readObject()
    ins.close()

    def record = newrecord.parsed_rec ?: newrec

    cfg.reconciler.call(grt,oldrec,record)
  }

  def initialiseTracker(grt, localPkgOID) {
    int rectype = grt.owner.rectype.longValue()
    def cfg = rectypes[rectype]
    def localPkg = genericOIDService.resolveOID(localPkgOID)

    def oldrec = localPkg.toComparablePackage()

    def bais = new ByteArrayInputStream((byte[])(grt.owner.record))
    def ins = new ObjectInputStream(bais);
    def newrec = ins.readObject()
    ins.close()

    cfg.reconciler.call(grt,oldrec,newrec)
  }

  /**
   *  When this system sees a title from a remote source, we need to try and find a common canonical identifier. We will use the
   *  GoKB TitleID for this. Each time a title is seen we make sure that we locally know what the GoKB Title ID is for that remote
   *  record.
   */
  def harmoniseTitleIdentifiers(titleinfo) {
    // println("harmoniseTitleIdentifiers");
    // println("Remote Title ID: ${titleinfo.titleId}");
    // println("Identifiers: ${titleinfo.title.identifiers}");
    //def title_instance = TitleInstance.lookupOrCreate(titleinfo.title.identifiers,titleinfo.title.name, true)
  }

  def diff(localPackage, globalRecordInfo) {

    def result = []

    def oldpkg = localPackage ? localPackage.toComparablePackage() : [tipps:[]];

    def bais = new ByteArrayInputStream((byte[])(globalRecordInfo.record))
    def ins = new ObjectInputStream(bais);
    def newpkg = ins.readObject()
    ins.close()

    def onNewTipp = { ctx, tipp, auto_accept -> ctx.add([tipp:tipp, action:'i']); }
    def onUpdatedTipp = { ctx, tipp, oldtipp, changes, auto_accept -> ctx.add([tipp:tipp, action:'u', changes:changes, oldtipp:oldtipp]); }
    def onDeletedTipp = { ctx, tipp, auto_accept  -> ctx.add([oldtipp:tipp, action:'d']); }
    def onPkgPropChange = { ctx, propname, value, auto_accept -> null; }
    def onTippUnchanged = { ctx, tipp -> ctx.add([tipp:tipp, action:'-']);  }

    com.k_int.kbplus.GokbDiffEngine.diff(result, oldpkg, newpkg, onNewTipp, onUpdatedTipp, onDeletedTipp, onPkgPropChange, onTippUnchanged, false)

    return result
  }

  def updatedTitleafterPackageReconcile = { grt, title_id, local_id ->
    //rectype = 2 = Title
    def cfg = rectypes[2]

    def uri = GlobalRecordSource.get(GlobalRecordInfo.get(grt.owner.id).source.id).uri
    def record_uuid = grt.owner.uuid

    uri = uri.replaceAll("packages", "")

    if(title_id == null)
    {
      return
    }

    def oai = new OaiClientLaser()
    def titlerecord = null

    if(record_uuid) {
      titlerecord = oai.getRecord(uri, 'titles', record_uuid)
    }

    if(!titlerecord) {
      titlerecord = oai.getRecord(uri, 'titles', 'org.gokb.cred.TitleInstance:'+title_id)
    }

    if(titlerecord == null)
    {
      return
    }
    def titleinfo = titleConv(titlerecord.metadata, null)

    log.debug("TitleRecord:" + titleinfo)

    def kbplus_compliant = testTitleCompliance(titleinfo.parsed_rec)

    if (kbplus_compliant?.value == 'No') {
      log.debug("Skip record - not KBPlus compliant");
    } else {

              def title_instance = TitleInstance.get(local_id)

              if (title_instance == null) {
                log.debug("Failed to resolve ${local_id} - Exiting");
                return
              }

              if (title_instance instanceof BookInstance)
              {
                  title_instance.editionNumber = titleinfo.editionNumber
                  title_instance.editionDifferentiator = titleinfo.editionDifferentiator
                  title_instance.editionStatement = titleinfo.editionStatement
                  title_instance.volume = titleinfo.volumeNumber
                  title_instance.dateFirstInPrint = titleinfo.dateFirstInPrint
                  title_instance.dateFirstOnline = titleinfo.dateFirstOnline

                  title_instance.firstAuthor = titleinfo.firstAuthor
                  title_instance.firstEditor = titleinfo.firstEditor
              }

              if (titleinfo.status == 'Current') {
                title_instance.status = RefdataValue.loc(RefdataCategory.TI_STATUS, [en: 'Current', de: 'Aktuell'])
              } else if (titleinfo.status == 'Retired') {
                title_instance.status = RefdataValue.loc(RefdataCategory.TI_STATUS, [en: 'Retired', de: 'im Ruhestand'])
              } else if (titleinfo.status == 'Deleted') {
                title_instance.status = RefdataValue.loc(RefdataCategory.TI_STATUS, [en: 'Deleted', de: 'Gelöscht'])
              }

              titleinfo.identifiers.each {
                log.debug("Checking title has ${it.namespace}:${it.value}");
                title_instance.checkAndAddMissingIdentifier(it.namespace, it.value);
              }
              title_instance.save(flush: true);

              if (titleinfo.publishers != null) {
                titleinfo.publishers.each { pub ->

                  def publisher_identifiers = []
                  def orgSector = RefdataValue.loc('OrgSector', [en: 'Publisher', de: 'Veröffentlicher']);
                  def publisher = Org.lookupOrCreate(pub.name, orgSector, null, publisher_identifiers, null, pub.uuid)
                  def pub_role = RefdataValue.loc('Organisational Role', [en: 'Publisher', de: 'Veröffentlicher']);
                  def sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                  def start_date
                  def end_date

                  if (pub.startDate) {
                    start_date = sdf.parse(pub.startDate);
                  }
                  if (pub.endDate) {
                    end_date = sdf.parse(pub.endDate);
                  }

                  log.debug("Asserting ${publisher} ${title_instance} ${pub_role}");
                  OrgRole.assertOrgTitleLink(publisher, title_instance, pub_role, (pub.startDate ? start_date : null), (pub.endDate ? end_date : null))
                }
              }

              // Title history!!
              titleinfo.history.each { historyEvent ->
                log.debug("Processing title history event");
                // See if we already have a reference
                def fromset = []
                def toset = []

                historyEvent.from.each { he ->
                  def participant = TitleInstance.lookupOrCreate(he.ids, he.title, titleinfo.titleType, he.uuid)
                  fromset.add(participant)
                }

                historyEvent.to.each { he ->
                  def participant = TitleInstance.lookupOrCreate(he.ids, he.title, titleinfo.titleType, he.uuid)
                  toset.add(participant)
                }

                // Now - See if we can find a title history event for data and these particiapnts.
                // Title History Events are IMMUTABLE - so we delete them rather than updating them.
                def base_query = "select the from TitleHistoryEvent as the where"
                // Need to parse date...
                def sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                def query_params = []

                if (historyEvent.date && historyEvent.date.trim().length() > 0) {
                  query_params.add(sdf.parse(historyEvent.date))
                  base_query += " the.eventDate = ? "
                }

                fromset.each {
                  if (query_params.size() > 0) {
                    base_query += "and"
                  }

                  base_query += " exists ( select p from the.participants as p where p.participant = ? and p.participantRole = 'from' ) "
                  query_params.add(it)
                }
                toset.each {
                  if (query_params.size() > 0) {
                    base_query += "and"
                  }

                  base_query += " exists ( select p from the.participants as p where p.participant = ? and p.participantRole = 'to' ) "
                  query_params.add(it)
                }

                def existing_title_history_event = TitleHistoryEvent.executeQuery(base_query, query_params);
                log.debug("Result of title history event lookup : ${existing_title_history_event}");

                if (existing_title_history_event.size() == 0) {
                  log.debug("Create new history event");
                  def he = new TitleHistoryEvent(eventDate: query_params[0]).save(flush: true)
                  fromset.each {
                    new TitleHistoryEventParticipant(event: he, participant: it, participantRole: 'from').save(flush: true)
                  }
                  toset.each {
                    new TitleHistoryEventParticipant(event: he, participant: it, participantRole: 'to').save(flush: true)
                  }
                }
              }
            }
  }

  def reloadAndSaveRecordofPackage(grt)
  {
    def gli = GlobalRecordInfo.get(grt.owner.id)
    def grs = GlobalRecordSource.get(gli.source.id)
    def uri = grs.uri.replaceAll("packages", "")
    def oai = new OaiClientLaser()
    def record = null

    if(grt.owner.uuid) {
      record = oai.getRecord(uri, 'packages', grt.owner.uuid)
    }
    if (!record) {
      record = oai.getRecord(uri, 'packages', grt.owner.identifier)
    }

    def newrecord = record ? packageConv(record.metadata, grs) : null

    if(newrecord) {
      def baos = new ByteArrayOutputStream()
      def out = new ObjectOutputStream(baos)
      log.debug("write object ${newrecord?.parsed_rec}");
      out.writeObject(newrecord?.parsed_rec)
      out.close()

      gli.record = baos.toByteArray()
      gli.uuid = gli.uuid ?: newrecord?.parsed_rec.gokbId
      gli.save()
    }

    return newrecord

  }

/*  def initialiseTrackerNew(grt) {

    def newrecord = reloadAndSaveRecordofPackage(grt)

    int rectype = grt.owner.rectype.longValue()
    def cfg = rectypes[rectype]

    def oldrec = [:]
    oldrec.tipps=[]
    def bais = new ByteArrayInputStream((byte[])(grt.owner.record))
    def ins = new ObjectInputStream(bais);
    def newrec = ins.readObject()
    ins.close()

    def record = newrecord.parsed_rec ?: newrec

    cfg.reconciler.call(grt,oldrec,record)
  }*/

  def cleanUpGorm() {
      log.debug("Clean up GORM")

      def session = sessionFactory.currentSession
      session.flush()
      session.clear()
      propertyInstanceMap.get().clear()
  }

}
