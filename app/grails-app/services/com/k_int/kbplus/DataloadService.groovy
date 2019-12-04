package com.k_int.kbplus


import de.laser.SystemEvent
import de.laser.helper.RDStore
import de.laser.interfaces.TemplateSupport
import groovy.json.JsonOutput
import org.elasticsearch.ElasticsearchException
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.action.admin.indices.flush.FlushRequest
import org.elasticsearch.action.admin.indices.flush.FlushResponse
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.support.replication.ReplicationResponse
import org.elasticsearch.action.DocWriteResponse
import org.elasticsearch.client.indices.*
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.client.*
import org.elasticsearch.rest.*
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.hibernate.ScrollMode

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
    def propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP
    def grailsApplication

    def es_index
    def dataload_running=false
    def dataload_stage=-1
    def dataload_message=''
    boolean update_running = false
    def lastIndexUpdate = null

    @javax.annotation.PostConstruct
    def init () {
        es_index = ESWrapperService.getESSettings().indexName
    }

    def updateFTIndexes() {
        //log.debug("updateFTIndexes ${this.hashCode()}")

        SystemEvent.createEvent('FT_INDEX_UPDATE_START')

        def future = executorService.submit({
            doFTUpdate()
        } as java.util.concurrent.Callable)
        log.debug("updateFTIndexes returning")
    }

    boolean doFTUpdate() {

        synchronized(this) {
            if ( update_running ) {
                return false
                log.debug("Exiting FT update - one already running");
            }
            else {
                update_running = true;
            }
        }
        log.debug("doFTUpdate: Execute IndexUpdateJob starting at ${new Date()}");

        def start_time = System.currentTimeMillis();

        RestHighLevelClient esclient = ESWrapperService.getClient()

        updateES(esclient, com.k_int.kbplus.Org.class) { org ->
            def result = [:]

                result._id = org.globalUID
                result.priority = 30
                result.dbId = org.id

                result.gokbId = org.gokbId
                result.guid = org.globalUID ?: ''

                result.name = org.name
                result.shortname = org.shortname
                result.sortname = org.sortname

                result.identifiers = []
                org.ids?.each { ident ->
                    try {
                        result.identifiers.add([type: ident.ns.ns, value: ident.value])
                    } catch (Exception e) {
                        log.error(e)
                    }
                }

                result.platforms = []
                org.platforms?.each { platform ->
                    try {
                        result.platforms.add([dbId: platform.id, name: platform.name])
                    } catch (Exception e) {
                        log.error(e)
                    }
                }
                result.rectype = 'Organisation'
                result.sector = org.sector?.value
                result.status = org.status?.value
                result.statusId = org.status?.id
                result.visible = 'Public'

            result
        }

        updateES(esclient, com.k_int.kbplus.TitleInstance.class) { ti ->

            def result = [:]

                if (ti.title != null) {
                    def new_key_title = com.k_int.kbplus.TitleInstance.generateKeyTitle(ti.title)
                    if (ti.keyTitle != new_key_title) {
                        ti.normTitle = com.k_int.kbplus.TitleInstance.generateNormTitle(ti.title)
                        ti.keyTitle = com.k_int.kbplus.TitleInstance.generateKeyTitle(ti.title)
                        //
                        // This alone should trigger before update to do the necessary...
                        //
                        ti.save()
                    } else {
                    }

                    result._id = ti.globalUID
                    result.priority = 20
                    result.dbId = ti.id

                    result.gokbId = ti.gokbId
                    result.guid = ti.globalUID ?: ''

                    result.identifiers = []
                    ti.ids?.each { ident ->
                        try {
                            result.identifiers.add([type: ident.ns.ns, value: ident.value])
                        } catch (Exception e) {
                            log.error(e)
                        }
                    }
                    //result.keyTitle = ti.keyTitle
                    //result.normTitle = ti.normTitle
                    result.publisher = ti.getPublisher()?.name ?: ''
                    result.rectype = 'Title'
                    result.sortTitle = ti.sortTitle
                    result.status = ti.status?.value
                    result.statusId = ti.status?.id
                    result.typTitle = ti.type?.value
                    result.name = ti.title
                    result.visible = 'Public'
                } else {
                    log.warn("Title with no title string - ${ti.id}")
                }

            result
        }

        updateES(esclient, com.k_int.kbplus.Package.class) { pkg ->
            def result = [:]

                result._id = pkg.globalUID
                result.priority = 30
                result.dbId = pkg.id

                result.gokbId = pkg.gokbId
                result.guid = pkg.globalUID ?: ''

                result.consortiaId = pkg.getConsortia()?.id
                result.consortiaName = pkg.getConsortia()?.name
                result.providerId = pkg.getContentProvider()?.id
                result.providerName = pkg.getContentProvider()?.name

                result.nominalPlatformId = pkg.nominalPlatform?.id
                result.nominalPlatformName = pkg.nominalPlatform?.name

                result.identifiers = []
                pkg.ids?.each { ident ->
                    try {
                        result.identifiers.add([type: ident.ns.ns, value: ident.value])
                    } catch (Exception e) {
                        log.error(e)
                    }
                }
                //result.identifiers = pkg.ids.collect{"${it?.identifier?.ns?.ns} : ${it?.identifier?.value}"}
                result.isPublic = (pkg?.isPublic) ? 'Yes' : 'No'
                result.endDate = pkg.endDate
                def lastmod = pkg.lastUpdated ?: pkg.dateCreated
                if (lastmod != null) {
                    result.lastModified = lastmod
                }
                result.name = "${pkg.name}"

                result.rectype = 'Package'
                result.sortname = pkg.sortName
                result.startDate = pkg.startDate
                result.status = pkg.packageStatus?.value
                result.statusId = pkg.packageStatus?.id
                result.titleCount = pkg.tipps.size()?:0
                result.titleCountCurrent = pkg.getCurrentTipps().size()?:0

                result.visible = 'Public'

/*                if (pkg.startDate) {
                    GregorianCalendar c = new GregorianCalendar()
                    c.setTime(pkg.startDate)
                    result.startYear = "${c.get(Calendar.YEAR)}"
                    result.startYearAndMonth = "${c.get(Calendar.YEAR)}-${(c.get(Calendar.MONTH)) + 1}"
                }

                if (pkg.endDate) {
                    GregorianCalendar c = new GregorianCalendar()
                    c.setTime(pkg.endDate)
                    result.endYear = "${c.get(Calendar.YEAR)}"
                    result.endYearAndMonth = "${c.get(Calendar.YEAR)}-${(c.get(Calendar.MONTH)) + 1}"
                }*/
            result
        }

        updateES(esclient, com.k_int.kbplus.License.class) { lic ->
            def result = [:]

            result._id = lic.globalUID
            result.priority = 40
            result.dbId = lic.id
            result.guid = lic.globalUID ?:''
            switch(lic.getCalculatedType()) {
                case TemplateSupport.CALCULATED_TYPE_CONSORTIAL:
                    result.availableToOrgs = lic.orgLinks.findAll{it.roleType?.value in ["Licensing Consortium"]}?.org?.id
                    break
                case TemplateSupport.CALCULATED_TYPE_PARTICIPATION:
                    result.availableToOrgs = lic.orgLinks.findAll{it.roleType?.value in ["Licensee_Consortial"]}?.org?.id
                    break
                default:
                    result.availableToOrgs = lic.orgLinks.findAll{it.roleType?.value in ["Licensee"]}?.org?.id
                    break
            }
            result.name = lic.reference
            result.rectype = 'License'
            result.status = lic.status?.value
            result.statusId = lic.status?.id
            result.endDate = lic.endDate
            result.startDate = lic.startDate
            result.members = License.findAllByInstanceOf(lic).size()

            result.consortiaId = lic.getLicensor()?.id
            result.consortiaName = lic.getLicensor()?.name

            result.identifiers = []
            lic.ids?.each { ident ->
                try {
                    result.identifiers.add([type: ident.ns.ns, value: ident.value])
                } catch (Exception e) {
                    log.error(e)
                }
            }

            if (lic.startDate) {
                GregorianCalendar c = new GregorianCalendar()
                c.setTime(lic.startDate)
                result.startYear = "${c.get(Calendar.YEAR)}"
            }

            if (lic.endDate) {
                GregorianCalendar c = new GregorianCalendar()
                c.setTime(lic.endDate)
                result.endYear = "${c.get(Calendar.YEAR)}"
            }

            result.visible = 'Private'
            result
        }

        updateES(esclient, com.k_int.kbplus.Platform.class) { plat ->
            def result = [:]

                result._id = plat.globalUID
                result.priority = 30
                result.dbId = plat.id

                result.gokbId = plat.gokbId
                result.guid = plat.globalUID ?: ''

                result.name = plat.name
                result.normname = plat.normname
                result.primaryUrl = plat.primaryUrl
                result.rectype = 'Platform'
                result.status = plat.status?.value
                result.statusId = plat.status?.id
                result.orgId = plat.org?.id
                result.orgName = plat.org?.name
                result.primaryUrl = plat.primaryUrl
                result.titleCountCurrent = plat.getCurrentTipps().size()?:0

                result.visible = 'Public'

            result
        }

        updateES(esclient, com.k_int.kbplus.Subscription.class) { sub ->
            def result = [:]

                result._id = sub.globalUID
                result.priority = 50
                result.dbId = sub.id
                result.guid = sub.globalUID ?: ''
                switch (sub.getCalculatedType()) {
                    case TemplateSupport.CALCULATED_TYPE_CONSORTIAL:
                        result.availableToOrgs = sub.orgRelations.find {
                            it.roleType?.value in ["Subscription Consortia"]
                        }?.org?.id
                        break
                    case TemplateSupport.CALCULATED_TYPE_PARTICIPATION:
                        result.availableToOrgs = sub.orgRelations.find {
                            it.roleType.value in ["Subscriber_Consortial"]
                        }?.org?.id
                        break
                    default:
                        result.availableToOrgs = sub.orgRelations.find {
                            it.roleType?.value in ["Subscriber", "Subscriber_Consortial", "Subscription Consortia"]
                        }?.org?.id
                        break
                }
                result.consortiaId = sub.getConsortia()?.id
                result.consortiaName = sub.getConsortia()?.name
                result.name = sub.name
                //result.identifier = sub.identifier
                result.packages = []
                // There really should only be one here? So think od this as SubscriptionOrg, but easier
                // to leave it as availableToOrgs I guess.
                result.rectype = 'Subscription'
                result.endDate = sub.endDate
                result.startDate = sub.startDate
                result.status = sub.status?.value
                result.statusId = sub.status?.id
                result.subtype = sub.type?.value
                result.members = Subscription.findAllByInstanceOf(sub).size()
                result.visible = 'Private'

                result.identifiers = []
                sub.ids?.each { ident ->
                        try {
                            result.identifiers.add([type: ident.ns.ns, value: ident.value])
                        } catch (Exception e) {
                            log.error(e)
                        }
                    }

                if (sub.startDate) {
                    GregorianCalendar c = new GregorianCalendar()
                    c.setTime(sub.startDate)
                    result.startYear = "${c.get(Calendar.YEAR)}"
                }

                if (sub.endDate) {
                    GregorianCalendar c = new GregorianCalendar()
                    c.setTime(sub.endDate)
                    result.endYear = "${c.get(Calendar.YEAR)}"
                }

                sub.packages.each { sp ->
                    def pgkinfo = [:]
                    if (sp.pkg != null) {
                        // Defensive - it appears that there can be a SP without a package.
                        pgkinfo.pkgname = sp.pkg.name
                        pgkinfo.pkgid = sp.pkg.id
                        pgkinfo.providerName = sp.pkg.contentProvider?.name
                        pgkinfo.providerId = sp.pkg.contentProvider?.id
                        result.packages.add(pgkinfo);
                    }
                }
                //result.suggest = [input: [sub.name.split(' ')]]

            result
        }

        updateES(esclient, com.k_int.kbplus.SurveyConfig.class) { surveyConfig ->
            def result = [:]

            result._id = surveyConfig.getClass().getSimpleName().toLowerCase()+":"+surveyConfig.id
            result.priority = 50
            result.dbId = surveyConfig.id
            result.availableToOrgs = surveyConfig.surveyInfo.owner?.id
            result.name = surveyConfig.getSurveyName()
            result.status= surveyConfig.surveyInfo.status?.value
            result.statusId= surveyConfig.surveyInfo.status?.id

            if (surveyConfig.surveyInfo.startDate) {
                GregorianCalendar c = new GregorianCalendar()
                c.setTime(surveyConfig.surveyInfo.startDate)
                result.startYear = "${c.get(Calendar.YEAR)}"
            }

            if (surveyConfig.surveyInfo.endDate) {
                GregorianCalendar c = new GregorianCalendar()
                c.setTime(surveyConfig.surveyInfo.endDate)
                result.endYear = "${c.get(Calendar.YEAR)}"
            }

            result.members = surveyConfig.orgs?.size() ?: 0
            result.visible = 'Private'

            result.rectype = 'Survey'

            result
        }

        updateES(esclient, com.k_int.kbplus.SurveyOrg.class) { surOrg ->
            def result = [:]

            result._id = surOrg.getClass().getSimpleName().toLowerCase()+":"+surOrg.id
            result.priority = 50
            result.dbId = surOrg.id
            result.availableToOrgs = (surOrg.surveyConfig.surveyInfo.status != RDStore.SURVEY_IN_PROCESSING) ? [surOrg.org.id] : []
            result.name = surOrg.surveyConfig.getSurveyName()
            result.status= surOrg.surveyConfig.surveyInfo.status?.value
            result.statusId= surOrg.surveyConfig.surveyInfo.status?.id

            if (surOrg.surveyConfig.surveyInfo.startDate) {
                GregorianCalendar c = new GregorianCalendar()
                c.setTime(surOrg.surveyConfig.surveyInfo.startDate)
                result.startYear = "${c.get(Calendar.YEAR)}"
            }

            if (surOrg.surveyConfig.surveyInfo.endDate) {
                GregorianCalendar c = new GregorianCalendar()
                c.setTime(surOrg.surveyConfig.surveyInfo.endDate)
                result.endYear = "${c.get(Calendar.YEAR)}"
            }

            result.visible = 'Private'

            result.rectype = 'ParticipantSurvey'

            result
        }

        updateES(esclient, com.k_int.kbplus.Task.class) { task ->
            def result = [:]

            result._id = task.getClass().getSimpleName().toLowerCase()+":"+task.id
            result.priority = 35
            result.dbId = task.id
            result.availableToOrgs = [task.responsibleOrg?.id]
            result.name = task.title
            result.description = task.description
            result.status= task.status?.value
            result.statusId= task.status?.id
            result.endDate= task.endDate

            result.visible = 'Private'

            result.rectype = 'Task'

            if(task.subscription){
                result.objectId = task.subscription.id
                result.objectName = task.subscription.name
                result.objectType = task.subscription.getClass().getSimpleName().toLowerCase()
            }

            if(task.org){
                result.objectId = task.org.id
                result.objectName = task.org.name
                result.objectType = task.org.getClass().getSimpleName().toLowerCase()
            }

            if(task.license){
                result.objectId = task.license.id
                result.objectName = task.license.reference
                result.objectType = task.license.getClass().getSimpleName().toLowerCase()
            }

            if(task.surveyConfig){
                result.objectId = task.surveyConfig.id
                result.objectName = task.surveyConfig.name
                result.objectType = task.surveyConfig.getClass().getSimpleName().toLowerCase()
            }

            result
        }

        updateES(esclient, com.k_int.kbplus.DocContext.class) { docCon ->
            def result = [:]

            result._id = docCon.getClass().getSimpleName().toLowerCase()+":"+docCon.id
            result.priority = 35
            result.dbId = docCon.id
            result.availableToOrgs = [docCon.owner?.owner.id ?: 0]
            result.name = docCon.owner?.title
            result.description = docCon.owner?.content
            result.status= docCon.status?.value
            result.statusId= docCon.status?.id

            result.visible = 'Private'

            result.rectype = (docCon.owner?.contentType == 0) ? 'Note' : 'Document'

            if(docCon.subscription){
                result.objectId = docCon.subscription.id
                result.objectName = docCon.subscription.name
                result.objectType = docCon.subscription.getClass().getSimpleName().toLowerCase()
            }

            if(docCon.org){
                result.objectId = docCon.org.id
                result.objectName = docCon.org.name
                result.objectType = docCon.org.getClass().getSimpleName().toLowerCase()
            }

            if(docCon.license){
                result.objectId = docCon.license.id
                result.objectName = docCon.license.reference
                result.objectType = docCon.license.getClass().getSimpleName().toLowerCase()
            }

            if(docCon.surveyConfig){
                result.objectId = docCon.surveyConfig.id
                result.objectName = docCon.surveyConfig.name
                result.objectType = docCon.surveyConfig.getClass().getSimpleName().toLowerCase()
            }

            result
        }

        updateES(esclient, com.k_int.kbplus.DocContext.class) { docCon ->
            def result = [:]

            result._id = docCon.getClass().getSimpleName().toLowerCase()+":"+docCon.id
            result.priority = 35
            result.dbId = docCon.id
            result.availableToOrgs = [docCon.owner?.owner.id ?: 0]
            result.name = docCon.owner?.title
            result.description = docCon.owner?.content
            result.status= docCon.status?.value
            result.statusId= docCon.status?.id

            result.visible = 'Private'

            result.rectype = (docCon.owner?.contentType == 0) ? 'Note' : 'Document'

            if(docCon.subscription){
                result.objectId = docCon.subscription.id
                result.objectName = docCon.subscription.name
                result.objectType = docCon.subscription.getClass().getSimpleName().toLowerCase()
            }

            if(docCon.org){
                result.objectId = docCon.org.id
                result.objectName = docCon.org.name
                result.objectType = docCon.org.getClass().getSimpleName().toLowerCase()
            }

            if(docCon.license){
                result.objectId = docCon.license.id
                result.objectName = docCon.license.reference
                result.objectType = docCon.license.getClass().getSimpleName().toLowerCase()
            }

            if(docCon.surveyConfig){
                result.objectId = docCon.surveyConfig.id
                result.objectName = docCon.surveyConfig.name
                result.objectType = docCon.surveyConfig.getClass().getSimpleName().toLowerCase()
            }

            result
        }

        esclient = ESWrapperService.getClient()
        update_running = false
        def elapsed = System.currentTimeMillis() - start_time;
        lastIndexUpdate = new Date(System.currentTimeMillis())
        FlushRequest request = new FlushRequest(es_index);
        FlushResponse flushResponse = esclient.indices().flush(request, RequestOptions.DEFAULT)

        log.debug("IndexUpdateJob completed in ${elapsed}ms at ${new Date()} ")

        ESWrapperService.clusterHealth()

        esclient.close()

        return true
    }

    def updateES(esclient, domain, recgen_closure) {

    def count = 0;

    try {
        //log.debug("updateES - ${domain.name}")

        def highest_timestamp = 0;
        def highest_id = 0;

        def latest_ft_record = FTControl.findByDomainClassNameAndActivity(domain.name,'ESIndex')


        if (! latest_ft_record) {
            latest_ft_record=new FTControl(domainClassName:domain.name,activity:'ESIndex', lastTimestamp:0)
        } else {
            highest_timestamp = latest_ft_record.lastTimestamp
            //log.debug("Got existing ftcontrol record for ${domain.name} max timestamp is ${highest_timestamp} which is ${new Date(highest_timestamp)}");
        }

        //log.debug("result of findByDomain: ${latest_ft_record}")

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

        //log.debug("Query completed .. processing rows ..")

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

            IndexRequest request = new IndexRequest(es_index);
            request.id(recid);
            String jsonString = JsonOutput.toJson(idx_record)
            request.source(jsonString, XContentType.JSON)

            IndexResponse indexResponse = esclient.index(request, RequestOptions.DEFAULT);

            String index = indexResponse.getIndex();
            String id = indexResponse.getId();
            if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
                println("CREATED ${domain.name}")
            } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
                println("UPDATED ${domain.name}")
            }
            ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
            if (shardInfo.getTotal() != shardInfo.getSuccessful()) {

            }
            if (shardInfo.getFailed() > 0) {
                for (ReplicationResponse.ShardInfo.Failure failure :
                        shardInfo.getFailures()) {
                    String reason = failure.reason();
                    println(reason)
                }
            }

          //latest_ft_record.lastTimestamp = r.lastUpdated?.getTime()
          if (r.lastUpdated?.getTime() > highest_timestamp) {
              highest_timestamp = r.lastUpdated?.getTime();
          }

          count++
          total++
          if ( count == 100 ) {
            count = 0;
            log.debug("processed ${total} records (${domain.name})")
              latest_ft_record.lastTimestamp = highest_timestamp
            latest_ft_record.save(flush:true);
            cleanUpGorm();
          }
        }
        results.close();

        log.debug("Processed ${total} records for ${domain.name}")

        // update timestamp
        latest_ft_record.lastTimestamp = highest_timestamp
        latest_ft_record.esElements = total
        latest_ft_record.dbElements = 0
        latest_ft_record.save(flush:true);

        checkESElementswithDBElements(domain, latest_ft_record)
    }
    catch ( Exception e ) {
      log.error("Problem with FT index", e)

        SystemEvent.createEvent('FT_INDEX_UPDATE_ERROR', ["index": domain.name])
    }
    finally {
      log.debug("Completed processing on ${domain.name} - saved ${count} records")

    }
  }

    def lookupOrCreateCanonicalIdentifier(ns, value) {
        // TODO [ticket=1789]
        log.debug("lookupOrCreateCanonicalIdentifier(${ns},${value})");
        //def namespace = IdentifierNamespace.findByNs(ns) ?: new IdentifierNamespace(ns:ns).save();
        //Identifier.findByNsAndValue(namespace,value) ?: new Identifier(ns:namespace, value:value).save();
        Identifier.construct([value:value, reference:null, namespace:ns])
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
      
      log.debug("Rows_updated:: ${rows_updated} ${num_rows_updated}");

      cleanUpGorm()
    }

    log.debug("Completed normalisation step... updated ${rows_updated} rows in ${System.currentTimeMillis()-sort_str_start_time}ms");

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
        RestHighLevelClient client = ESWrapperService.getClient()

        try {
            // Drop any existing kbplus index
            log.debug("Dropping old ES index ..")
            DeleteIndexRequest deleteRequest = new DeleteIndexRequest(es_index)
            def deleteIndexResponse = client.indices().delete(deleteRequest, RequestOptions.DEFAULT)
            boolean acknowledged = deleteIndexResponse.isAcknowledged()
            if (acknowledged) {
                log.debug("Drop old ES index completed OK")
            }
            else {
                log.error("Index wasn't deleted")
            }
        }
        catch ( ElasticsearchException  e ) {
            if (e.status() == RestStatus.NOT_FOUND) {
                log.warn("index does not exist ..")
            }else {
                log.warn("Problem deleting index ..", e)
            }

            SystemEvent.createEvent('FT_INDEX_CLEANUP_ERROR', ["index": es_index])
        }

        log.debug("Create new ES index ..")
        //def createResponse = client.admin().indices().prepareCreate(es_index).get()
        CreateIndexRequest createRequest = new CreateIndexRequest(es_index)

        def es_mapping = ESWrapperService.getESMapping()
        //println(es_mapping)

        createRequest.mapping(JsonOutput.toJson(es_mapping), XContentType.JSON)

        CreateIndexResponse createIndexResponse = client.indices().create(createRequest, RequestOptions.DEFAULT)
        boolean acknowledgedCreate = createIndexResponse.isAcknowledged()
        if (acknowledgedCreate) {
            log.debug("Create ES index completed OK")
        }
        else {
            log.error("Index wasn't created")
        }

        log.debug("Clear down and init ES completed...")
        client.close()
    }

    def checkESElementswithDBElements(domain, ft_record) {

        //Datenbank Abfrage
        def c = domain.createCriteria()
        def ResultsinDB = c.list(){}

        //ES Abfrage
        def rectype = ""
        if(domain.name == 'com.k_int.kbplus.Subscription')
        {
            rectype = "Subscription"
        }else if(domain.name == 'com.k_int.kbplus.Org')
        {
            rectype = "Organisation"
        }else if (domain.name == 'com.k_int.kbplus.TitleInstance')
        {
            rectype = "Title"
        }
        else if (domain.name == 'com.k_int.kbplus.Package')
        {
            rectype = "Package"
        }else if (domain.name == 'com.k_int.kbplus.License')
        {
            rectype = "License"
        }
        else if (domain.name == 'com.k_int.kbplus.Platform')
        {
            rectype = "Platform"
        }
        else if (domain.name == 'com.k_int.kbplus.SurveyConfig')
        {
            rectype = "ParticipantSurvey"
        }

        else if (domain.name == 'com.k_int.kbplus.SurveyOrg')
        {
            rectype = "Survey"
        }

        else if (domain.name == 'com.k_int.kbplus.Task')
        {
            rectype = "Task"
        }

        def query_str = "rectype: '${rectype}'"

        def index = ESWrapperService.getESSettings().indexName
        RestHighLevelClient esclient = ESWrapperService.getClient()

        SearchRequest searchRequest = new SearchRequest(index)
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
        searchSourceBuilder.query(QueryBuilders.queryStringQuery(query_str))

        searchRequest.source(searchSourceBuilder)
        //searchRequest.scroll(TimeValue.timeValueMinutes(1L))

        SearchResponse searchResponse = esclient.search(searchRequest, RequestOptions.DEFAULT)

            def resultsTotal =  searchResponse ? searchResponse.getHits().getTotalHits().value : 0

            ft_record.dbElements = ResultsinDB.size()?:0
            ft_record.esElements = resultsTotal ? resultsTotal.toInteger() :0
            ft_record.save(flush: true)
            if(ft_record.dbElements != ft_record.esElements) {
                log.debug("****ES NOT COMPLETE FOR ${rectype}: ES Results = ${resultsTotal}, DB Results = ${ResultsinDB.size()}****")
            }
        esclient.close()

    }
}
