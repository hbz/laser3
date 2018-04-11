package com.k_int.kbplus

import com.k_int.kbplus.*
import org.hibernate.ScrollMode
import java.nio.charset.Charset
import java.util.GregorianCalendar
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.action.admin.indices.flush.FlushRequest
import org.elasticsearch.client.Client
import org.elasticsearch.action.admin.indices.create.*

class DataloadService {

    def stats = [:]

    def update_stages = [
        'Organisations Data',
        'Subscriptions Offered Data',
        'Subscriptions Taken Data',
        'License Data'
    ]

    def executorService
    def ESWrapperService
    def sessionFactory
    def edinaPublicationsAPIService
    def propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP
    def grailsApplication

    def es_index
    def dataload_running=false
    def dataload_stage=-1
    def dataload_message=''
    def update_running = false
    def lastIndexUpdate = null

    @javax.annotation.PostConstruct
    def init () {
        es_index = grailsApplication.config.aggr_es_index ?: ESWrapperService.ES_INDEX
    }

    def updateFTIndexes() {
        log.debug("updateFTIndexes ${this.hashCode()}")
        new EventLog(event:'kbplus.updateFTIndexes', message:'Update FT indexes', tstp:new Date(System.currentTimeMillis())).save(flush:true)

        def future = executorService.submit({
            doFTUpdate()
        } as java.util.concurrent.Callable)
        log.debug("updateFTIndexes returning")
    }

    def doFTUpdate() {

        synchronized(this) {
            if ( update_running == true ) {
                return
            }
            else {
                log.debug("Exiting FT update - one already running");
                update_running = true;
            }
        }

        log.debug("doFTUpdate");

        log.debug("Execute IndexUpdateJob starting at ${new Date()}");
        updateSiteMapping()

        def start_time = System.currentTimeMillis();

        def esclient = ESWrapperService.getClient()

        updateES(esclient, com.k_int.kbplus.Org.class) { org ->
            def result = [:]
            //result._id = org.impId
            result._id = org.globalUID
            result.dbId = org.id
            result.guid = org.globalUID ?:''

            result.name = org.name
            result.rectype = 'Organisation'
            result.sector = org.sector?.value
            result.status = org.status?.value
            result.visible = ['Public']
            result
        }

        updateES(esclient, com.k_int.kbplus.TitleInstance.class) { ti ->
            def result = [:]
            if (ti.title != null) {
                def new_key_title =  com.k_int.kbplus.TitleInstance.generateKeyTitle(ti.title)
                if (ti.keyTitle != new_key_title) {
                    ti.normTitle = com.k_int.kbplus.TitleInstance.generateNormTitle(ti.title)
                    ti.keyTitle = com.k_int.kbplus.TitleInstance.generateKeyTitle(ti.title)
                    //
                    // This alone should trigger before update to do the necessary...
                    //
                    ti.save()
                }
                else {
                }

                //result._id = ti.impId
                result._id = ti.globalUID
                result.dbId = ti.id
                result.guid = ti.globalUID ?:''

                result.identifiers = []
                ti.ids?.each { id ->
                    try{
                        result.identifiers.add([type:id.identifier.ns.ns, value:id.identifier.value])
                    } catch(Exception e) {
                        log.error(e)
                    }
                }
                result.keyTitle = ti.keyTitle
                result.normTitle = ti.normTitle
                result.publisher = ti.getPublisher()?.name ?:''
                result.rectype = 'Title'
                result.sortTitle = ti.sortTitle
                result.status = ti.status?.value
                result.typTitle = ti.type?.value
                result.title = ti.title
                result.visible = ['Public']
            }
            else {
                log.warn("Title with no title string - ${ti.id}")
            }
            result
        }

        updateES(esclient, com.k_int.kbplus.Package.class) { pkg ->
            def result = [:]
            //result._id = pkg.impId
            result._id = pkg.globalUID
            result.dbId = pkg.id
            result.guid = pkg.globalUID ?:''

            result.consortiaId = pkg.getConsortia()?.id
            result.consortiaName = pkg.getConsortia()?.name
            result.cpid = pkg.getContentProvider()?.id
            result.cpname = pkg.getContentProvider()?.name
            result.identifiers = pkg.ids.collect{"${it?.identifier?.ns?.ns} : ${it?.identifier?.value}"}
            result.isPublic = pkg?.isPublic?.value?:'No'
            result.endDate = pkg.endDate
            def lastmod = pkg.lastUpdated ?: pkg.dateCreated
            if (lastmod != null) {
                result.lastModified = lastmod
            }
            result.name = "${pkg.name}"
            result.pkg_scope = pkg.packageScope?.value ?: 'Scope Undefined'
            result.rectype = 'Package'
            result.sortname = pkg.sortName
            result.startDate = pkg.startDate
            result.status = pkg.packageStatus?.value
            result.titleCount = pkg.tipps.size()
            result.tokname = result.name.replaceAll(':',' ')
            result.visible = ['Public']

            if (pkg.startDate) {
                GregorianCalendar c = new GregorianCalendar()
                c.setTime(pkg.startDate)
                result.startYear = "${c.get(Calendar.YEAR)}"
                result.startYearAndMonth = "${c.get(Calendar.YEAR)}-${(c.get(Calendar.MONTH))+1}"
            }

            if (pkg.endDate) {
                GregorianCalendar c = new GregorianCalendar()
                c.setTime(pkg.endDate)
                result.endYear = "${c.get(Calendar.YEAR)}"
                result.endYearAndMonth = "${c.get(Calendar.YEAR)}-${(c.get(Calendar.MONTH))+1}"
            }
            result
        }

        updateES(esclient, com.k_int.kbplus.License.class) { lic ->
            def result = [:]
            //result._id = lic.impId
            result._id = lic.globalUID
            result.dbId = lic.id
            result.guid = lic.globalUID ?:''
            //TODO: Überarbeiten availableToOrgs
            result.availableToOrgs = lic.orgLinks.find{it.roleType?.value == "Licensee" || it.roleType?.value == "Licensing Consortium"}?.org?.id
            result.name = lic.reference
            result.rectype = 'License'
            result.status = lic.status?.value
            result.endDate = lic.endDate
            result.startDate = lic.startDate
            result.visible = ['Public']
            result
        }

        updateES(esclient, com.k_int.kbplus.Platform.class) { plat ->
            def result = [:]
            //result._id = plat.impId
            result._id = plat.globalUID
            result.dbId = plat.id
            result.guid = plat.globalUID ?:''

            result.name = plat.name
            result.rectype = 'Platform'
            result.status = plat.status?.value
            result.visible = ['Public']
            result
        }

        updateES(esclient, com.k_int.kbplus.Subscription.class) { sub ->
            def result = [:]
            //result._id = sub.impId
            result._id = sub.globalUID
            result.dbId = sub.id
            result.guid = sub.globalUID ?:''
            //TODO: Überarbeiten availableToOrgs
            result.availableToOrgs = sub.orgRelations.find{it.roleType?.value == "Subscriber" || it.roleType?.value == "Subscription Consortia" }?.org?.id
            result.consortiaId = sub.getConsortia()?.id
            result.consortiaName = sub.getConsortia()?.name
            result.name = sub.name
            result.identifier = sub.identifier
            result.packages = []
            // There really should only be one here? So think od this as SubscriptionOrg, but easier
            // to leave it as availableToOrgs I guess.
            result.rectype = 'Subscription'
            result.endDate = sub.endDate
            result.startDate = sub.startDate
            result.status = sub.status?.value
            result.subtype = sub.type?.value
            result.visible = ['Public']

            if (sub.startDate) {
                GregorianCalendar c = new GregorianCalendar()
                c.setTime(sub.startDate)
                result.startYear = "${c.get(Calendar.YEAR)}"
                result.startYearAndMonth = "${c.get(Calendar.YEAR)}-${(c.get(Calendar.MONTH))+1}"
            }

            sub.packages.each { sp ->
                def pgkinfo = [:]
                if ( sp.pkg != null ) {
                    // Defensive - it appears that there can be a SP without a package.
                    pgkinfo.pkgname = sp.pkg.name
                    pgkinfo.pkgidstr= sp.pkg.identifier
                    pgkinfo.pkgid= sp.pkg.id
                    pgkinfo.cpname = sp.pkg.contentProvider?.name
                    pgkinfo.cpid = sp.pkg.contentProvider?.id
                    result.packages.add(pgkinfo);
                }
            }

            if (sub.subscriber) {
                result.visible.add(sub.subscriber.shortcode)
            }

            result
        }

        update_running = false;
        def elapsed = System.currentTimeMillis() - start_time;
        lastIndexUpdate = new Date(System.currentTimeMillis())
        esclient.admin().indices().flush(new FlushRequest(es_index)).actionGet()

        log.debug("IndexUpdateJob completed in ${elapsed}ms at ${new Date()} ")
    }

    def updateSiteMapping() {
        log.debug("Updating ES site mapping ..")
        def esclient = ESWrapperService.getClient()

        SitePage.findAll().each{ site ->
            def result = [:]

            result.alias = site.alias
            result.action = site.action
            result.controller = site.controller
            result.rectype = site.rectype

            def recid = site.globalUID.toString()

            def future = esclient.indexAsync {
                index es_index
                type site.class.name
                id recid
                source result
            }

        }
    }

    def updateES(esclient, domain, recgen_closure) {

    def count = 0;
    try {
        log.debug("updateES - ${domain.name}")

        def highest_timestamp = 0;
        def highest_id = 0;

        def latest_ft_record = FTControl.findByDomainClassNameAndActivity(domain.name,'ESIndex')

        log.debug("result of findByDomain: ${latest_ft_record}")
        if (! latest_ft_record) {
            latest_ft_record=new FTControl(domainClassName:domain.name,activity:'ESIndex', lastTimestamp:0)
        } else {
            highest_timestamp = latest_ft_record.lastTimestamp
            log.debug("Got existing ftcontrol record for ${domain.name} max timestamp is ${highest_timestamp} which is ${new Date(highest_timestamp)}");
        }

        log.debug("updateES ${domain.name} since ${latest_ft_record.lastTimestamp}")
        def total = 0;
        Date from = new Date(latest_ft_record.lastTimestamp)
        // def qry = domain.findAllByLastUpdatedGreaterThan(from,[sort:'lastUpdated'])

        def c = domain.createCriteria()
        c.setReadOnly(true)
        c.setCacheable(false)
        c.setFetchSize(Integer.MIN_VALUE)

        c.buildCriteria{
            or {
                gt('lastUpdated', from)
                and {
                    gt('dateCreated', from)
                    isNull('lastUpdated')
                }
            }
            order("lastUpdated", "asc")
        }

        def results = c.scroll(ScrollMode.FORWARD_ONLY)

        log.debug("Query completed .. processing rows ..")

      while (results.next()) {
        Object r = results.get(0);
        def idx_record = recgen_closure(r)
        def future
        if(idx_record['_id'] == null) {
          log.error("******** Record without an ID: ${idx_record} Obj:${r} ******** ")
          continue
        }

          def recid = idx_record['_id'].toString()
          idx_record.remove('_id');

          future = esclient.indexAsync {
              index es_index
              type domain.name
              id recid
              source idx_record
          }

//        if ( idx_record?.status?.toLowerCase() == 'deleted' ) {
//            future = esclient.delete {
//              index es_index
//              type domain.name
//              id idx_record['_id']
//            }.actionGet()
//        }
//        else {
//          future = esclient.index {
//            index es_index
//            type domain.name
//            id idx_record['_id']
//            source idx_record
//          }.actionGet()
//        }

          //latest_ft_record.lastTimestamp = r.lastUpdated?.getTime()
          if (r.lastUpdated?.getTime() > highest_timestamp) {
              highest_timestamp = r.lastUpdated?.getTime();
          }

        count++
        total++
        if ( count > 100 ) {
          count = 0;
          log.debug("processed ${++total} records (${domain.name})")
            latest_ft_record.lastTimestamp = highest_timestamp
          latest_ft_record.save(flush:true);
          cleanUpGorm();
        }
      }
      results.close();

      log.debug("Processed ${total} records for ${domain.name}")

      // update timestamp
        latest_ft_record.lastTimestamp = highest_timestamp
      latest_ft_record.save(flush:true);
    }
    catch ( Exception e ) {
      log.error("Problem with FT index", e)
    }
    finally {
      log.debug("Completed processing on ${domain.name} - saved ${count} records")
    }
  }

    def getReconStatus() {
    
        def result = [
          active:   dataload_running,
          stage:    dataload_stage,
          stats:    stats
        ]
        result
    }



  def possibleNote(content, type, license, note_title) {
    if ( content && content.toString().length() > 0 ) {
      def doc_content = new Doc(content:content.toString(), 
                                title: "${type} note",
                                type: lookupOrCreateRefdataEntry('Doc Type','Note') ).save()
      def doc_context = new DocContext(license:license,
                                       domain:type,
                                       owner:doc_content,
                                       doctype:lookupOrCreateRefdataEntry('Document Type','Note')).save();
    }
  }

  def nvl(val,defval) {
    def result = defval
    if ( ( val ) && ( val.toString().trim().length() > 0 ) )
      result = val

    result
  }

  def lookupOrCreateCanonicalIdentifier(ns, value) {
    log.debug("lookupOrCreateCanonicalIdentifier(${ns},${value})");
    def namespace = IdentifierNamespace.findByNs(ns) ?: new IdentifierNamespace(ns:ns).save();
    Identifier.findByNsAndValue(namespace,value) ?: new Identifier(ns:namespace, value:value).save();
  }


  def lookupOrCreateRefdataEntry(refDataCategory, refDataCode) {
    def category = RefdataCategory.findByDesc(refDataCategory) ?: new RefdataCategory(desc:refDataCategory).save(flush:true)
    def result = RefdataValue.findByOwnerAndValue(category, refDataCode) ?: new RefdataValue(owner:category,value:refDataCode).save(flush:true)
    result;
  }

  def assertOrgTitleLink(porg, ptitle, prole, pstart, pend) {
    // def link = OrgRole.findByTitleAndOrgAndRoleType(ptitle, porg, prole) ?: new OrgRole(title:ptitle, org:porg, roleType:prole).save();
    def link = OrgRole.find{ title==ptitle && org==porg && roleType==prole }
    if ( ! link ) {
      link = new OrgRole(title:ptitle, org:porg, roleType:prole)
      if ( !porg.links )
        porg.links = [link]
      else
        porg.links.add(link)

      porg.save();
    }
  }

  def assertOrgPackageLink(porg, ppkg, prole) {
    // def link = OrgRole.findByPkgAndOrgAndRoleType(pkg, org, role) ?: new OrgRole(pkg:pkg, org:org, roleType:role).save();
    log.debug("assertOrgPackageLink()");
    def link = OrgRole.find{ pkg==ppkg && org==porg && roleType==prole }
    if ( ! link ) {
      link = new OrgRole(pkg:ppkg, org:porg, roleType:prole);
      if ( !porg.links )
        porg.links = [link]
      else
        porg.links.add(link)
      porg.save();
    }
    log.debug("assertOrgPackageLink() complete");
  }

  def assertOrgLicenseLink(porg, plic, prole) {
    def link = OrgRole.find{ lic==plic && org==porg && roleType==prole }
    if ( ! link ) {
      link = new OrgRole(lic:plic, org:porg, roleType:prole);
      if ( !porg.links )
        porg.links = [link]
      else
        porg.links.add(link)
      porg.save();
    }

  }



  def dataCleanse() {
    log.debug("dataCleanse");
    def future = executorService.submit({
      doDataCleanse()
    } as java.util.concurrent.Callable)
    log.debug("dataCleanse returning");
  }

  def doDataCleanse() {
    log.debug("dataCleansing");
    // 1. Find all packages that do not have a nominal platform
    Package.findAllByNominalPlatformIsNull().each { p ->
      def platforms = [:]
      p.tipps.each{ tipp ->
        if ( !platforms.keySet().contains(tipp.platform.id) ) {
          platforms[tipp.platform.id] = [count:1, platform:tipp.platform]
        }
        else {
          platforms[tipp.platform.id].count++
        }
      }

      def selected_platform = null;
      def largest = 0;
      platforms.values().each { pl ->
        log.debug("Processing ${pl}");
        if ( pl['count'] > largest ) {
          selected_platform = pl['platform']
        }
      }

      log.debug("Nominal platform is ${selected_platform} for ${p.id}");
      p.nominalPlatform = selected_platform
      p.save(flush:true)


    }

    // Fill out any missing sort keys on titles, packages or licenses
    def num_rows_updated = 0
    def sort_str_start_time = System.currentTimeMillis()
    def rows_updated = true

    while ( rows_updated ) {
      rows_updated = false

      TitleInstance.findAllBySortTitle(null,[max:100]).each {
        log.debug("Normalise Title ${it.title}");
        it.sortTitle = it.generateSortTitle(it.title) ?: 'AAA_Error'
        if ( it.sortTitle != null ) {
          it.save(flush:true, failOnError:true)
          num_rows_updated++;
          rows_updated = true
        }
      }

      log.debug("Generate Missing Sort Package Names Rows_updated:: ${rows_updated} ${num_rows_updated}");
      Package.findAllBySortName(null,[max:100]).each {
        log.debug("Normalise Package Name ${it.name}");
        it.sortName = it.generateSortName(it.name) ?: 'AAA_Error'
        if ( it.sortName != null ) {
          it.save(flush:true, failOnError:true)
          num_rows_updated++;
          rows_updated = true
        }
      }

      log.debug("Generate Missing Sortable License References Rows_updated:: ${rows_updated} ${num_rows_updated}");
      License.findAllBySortableReference(null,[max:100]).each {
        log.debug("Normalise License Reference Name ${it.reference}");
        it.sortableReference = it.generateSortableReference(it.reference) ?: 'AAA_Error'
        if( it.sortableReference != null ) {
          it.save(flush:true, failOnError:true)
          num_rows_updated++;
          rows_updated = true
        }
      }
      
      println("Rows_updated:: ${rows_updated} ${num_rows_updated}");

      cleanUpGorm()
    }

    log.debug("Completed normalisation step... updated ${rows_updated} rows in ${System.currentTimeMillis()-sort_str_start_time}ms");

  }

  def titleAugment() {
    // edinaPublicationsAPIService.lookup('Acta Crystallographica. Section F, Structural Biology and Crystallization Communications');
    def future = executorService.submit({
      doTitleAugment()
    } as java.util.concurrent.Callable)
    log.debug("titleAugment returning");
  }

  def doTitleAugment() {
    TitleInstance.findAll().each { ti ->
      if ( ti.getIdentifierValue('SUNCAT' ) == null ) {
        def lookupResult = edinaPublicationsAPIService.lookup(ti.title)
        if ( lookupResult ) {
          def record = lookupResult.records.record
          if ( record ) {
            boolean matched = false;
            def suncat_identifier = null;
            record.modsCollection.mods.identifier.each { id ->
              if ( id.text().equalsIgnoreCase(ti.getIdentifierValue('ISSN')) || id.text().equalsIgnoreCase(ti.getIdentifierValue('eISSN'))  ) {
                matched = true
              }

              if ( id.@type == 'suncat' ) {
                suncat_identifier = id.text();
              }
            }

            if ( matched && suncat_identifier ) {
              log.debug("set suncat identifier to ${suncat_identifier}");
              def canonical_identifier = Identifier.lookupOrCreateCanonicalIdentifier('SUNCAT',suncat_identifier);
              ti.ids.add(new IdentifierOccurrence(identifier:canonical_identifier, ti:ti));
              ti.save(flush:true);
            }
            else {
              log.debug("No match for title ${ti.title}, ${ti.id}");
            }
          }
          else {
          }
        }
        else {
        }
        synchronized(this) {
          Thread.sleep(250);
        }
      }
    }
  }

    def cleanUpGorm() {
        log.debug("Clean up GORM")

        def session = sessionFactory.currentSession
        session.flush()
        session.clear()
        propertyInstanceMap.get().clear()
    }

    def clearDownAndInitES() {
        log.debug("Clear down and init ES");
        Client client = ESWrapperService.getClient()

        try {
            // Drop any existing kbplus index
            log.debug("Dropping old ES index ..")
            DeleteIndexResponse delete = client.admin().indices().delete(new DeleteIndexRequest(es_index)).actionGet()
            if (delete.acknowledged) {
                log.debug("Drop old ES index completed OK")
            }
            else {
                log.error("Index wasn't deleted")
            }
        }
        catch ( Exception e ) {
            log.warn("Problem deleting index ..", e)
        }

        log.debug("Create new ES index ..")

        CreateIndexResponse createResponse = client.admin().indices().create(new CreateIndexRequest(es_index)).actionGet()

        log.debug("Clear down and init ES completed... AS OF 4.1 MAPPINGS -MUST- Be installed in ESHOME/mappings/kbplus")
    }
}
