package com.k_int.kbplus

import com.k_int.kbplus.auth.User
import de.laser.DeletionService
import de.laser.YodaService
import de.laser.controller.AbstractDebugController
import de.laser.domain.MailTemplate
import de.laser.helper.RDConstants
import de.laser.helper.SessionCacheWrapper
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import org.codehaus.groovy.grails.plugins.orm.auditable.AuditLogEvent
import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

@Secured(['IS_AUTHENTICATED_FULLY'])
class DataManagerController extends AbstractDebugController {

    def springSecurityService
    def GOKbService
    def contextService
    def genericOIDService
    YodaService yodaService
    ExportService exportService
    DeletionService deletionService
    GlobalSourceSyncService globalSourceSyncService
    ChangeNotificationService changeNotificationService
    MessageSource messageSource
    LinkGenerator grailsLinkGenerator

  @Secured(['ROLE_ADMIN'])
  def index() { 
    def result =[:]
    def pending_change_pending_status = RefdataValue.getByValueAndCategory('Pending', RDConstants.PENDING_CHANGE_STATUS)

        result.pendingChanges = PendingChange.executeQuery("select pc from PendingChange as pc where pc.pkg is not null and ( pc.status is null or pc.status = ? ) order by ts desc", [pending_change_pending_status]);

        result
    }

    @Secured(['ROLE_ADMIN'])
    def changeLog() {

        def result =[:]
        log.debug("changeLog ${params}");
        def formatter = new java.text.SimpleDateFormat(message(code:'default.date.format.notime', default:'yyyy-MM-dd'))

        def exporting = params.format == 'csv' ? true : false

        if ( exporting ) {
            result.max = 10000
            params.max = 10000
            result.offset = 0
        }
        else {
            def user = User.get(springSecurityService.principal.id)
            result.max = params.max ?: user.getDefaultPageSizeTMP()
            params.max = result.max
            result.offset = params.offset ? Integer.parseInt(params.offset) : 0;
        }

        if ( params.startDate == null ) {
            def cal = new java.util.GregorianCalendar()
            cal.setTimeInMillis(System.currentTimeMillis())
            cal.set(Calendar.DAY_OF_MONTH,1)
            params.startDate=formatter.format(cal.getTime())
        }
        if ( params.endDate == null ) { params.endDate = formatter.format(new Date()) }
        if ( ( params.creates == null ) && ( params.updates == null ) ) {
            params.creates='Y'
        }
        if(params.startDate > params.endDate){
            flash.error = message(code:'datamanager.changeLog.error.dates', default:'From Date cannot be after To Date.')
            return
        }
        def base_query = "from org.codehaus.groovy.grails.plugins.orm.auditable.AuditLogEvent as e where e.className in (:l) AND e.lastUpdated >= :s AND e.lastUpdated <= :e AND e.eventName in (:t)"

        def types_to_include = []
        if ( params.packages=="Y" ) types_to_include.add('com.k_int.kbplus.Package');
        if ( params.licenses=="Y" ) types_to_include.add('com.k_int.kbplus.License');
        if ( params.titles=="Y" ) types_to_include.add('com.k_int.kbplus.TitleInstance');
        if ( params.tipps=="Y" ) types_to_include.add('com.k_int.kbplus.TitleInstancePackagePlatform');
        // com.k_int.kbplus.Subscription                 |
        // com.k_int.kbplus.IdentifierOccurrence         |

        def events_to_include=[]
        if ( params.creates=="Y" ) events_to_include.add('INSERT');
        if ( params.updates=="Y" ) events_to_include.add('UPDATE');

        result.actors = []
        def actors_dms = []
        def actors_users = []

        def all_types = [ 'com.k_int.kbplus.Package','com.k_int.kbplus.License','com.k_int.kbplus.TitleInstance','com.k_int.kbplus.TitleInstancePackagePlatform' ]

        // Get a distinct list of actors
        def auditActors = AuditLogEvent.executeQuery('select distinct(al.actor) from AuditLogEvent as al where al.className in ( :l  )',[l:all_types])

        def formal_role = com.k_int.kbplus.auth.Role.findByAuthority('INST_ADM')

        // From the list of users, extract and who have the INST_ADM role
        def rolesMa = []
        if ( auditActors )
            rolesMa = com.k_int.kbplus.auth.UserOrg.executeQuery(
                    'select distinct(userorg.user.username) from UserOrg as userorg ' +
                            'where userorg.formalRole = (:formal_role) and userorg.user.username in (:actors)',
                    [formal_role:formal_role,actors:auditActors])

        auditActors.each {
            def u = User.findByUsername(it)

            if ( u != null ) {
                if(rolesMa.contains(it)){
                    actors_dms.add([it, u.displayName])
                }else{
                    actors_users.add([it, u.displayName])
                }
            }
        }

        // Sort the list of data manager users
        actors_dms.sort{it[1]}

        // Sort the list of ordinary users
        actors_users.sort{it[1]}

        result.actors = actors_dms.plus(actors_users)

        log.debug("${params}");
        if ( types_to_include.size() == 0 ) {
            types_to_include.add('com.k_int.kbplus.Package')
            params.packages="Y"
        }

        def start_date = formatter.parse(params.startDate)
        def end_date = formatter.parse(params.endDate)

        final long hoursInMillis = 60L * 60L * 1000L;
        end_date = new Date(end_date.getTime() + (24L * hoursInMillis - 2000L));

        def query_params = ['l':types_to_include,'s':start_date,'e':end_date, 't':events_to_include]


        //def filterActors = params.findAll{it.key.startsWith("change_actor_")}
        def filterActors = params.change_actors

    if(filterActors) {
      boolean multipleActors = false
      def condition = "AND ( "
      filterActors.each{        
          if (multipleActors) {
            condition = "OR"
          }
          if ( it == "change_actor_PEOPLE" ) {
            base_query += " ${condition} e.actor <> \'system\' AND e.actor <> \'anonymousUser\' "
            multipleActors = true
          }
          else if(it != 'change_actor_ALL' && it != 'change_actor_PEOPLE') {
            def paramKey = it.replaceAll("[^A-Za-z]", "") //remove things that can cause problems in sql
            base_query += " ${condition} e.actor = :${paramKey} "
            query_params."${paramKey}" = it.split("change_actor_")[1]
            multipleActors = true
          }     
      } 
      base_query += " ) "  
    }
  
  

        if ( types_to_include.size() > 0 ) {

            def limits = (!params.format||params.format.equals("html"))?[max:result.max, offset:result.offset]:[max:result.max,offset:0]
            result.historyLines = AuditLogEvent.executeQuery('select e '+base_query+' order by e.lastUpdated desc',
                    query_params, limits);
            result.num_hl = AuditLogEvent.executeQuery('select e.id '+base_query,
                    query_params).size()
            result.formattedHistoryLines = []
            result.historyLines.each { hl ->

                def line_to_add = [ lastUpdated: hl.lastUpdated,
                                    actor: User.findByUsername(hl.actor),
                                    propertyName: hl.propertyName,
                                    oldValue: hl.oldValue,
                                    newValue: hl.newValue
                ]
                def linetype = null

                switch(hl.className) {
                    case 'com.k_int.kbplus.License':
                        def license_object = License.get(hl.persistedObjectId);
                        if (license_object) {
                            def license_name = license_object.licenseType ? license_object.licenseType+': ' : ''
                            license_name += license_object.reference ?: '**No reference**'
                            line_to_add.link = createLink(controller:'license', action: 'show', id:hl.persistedObjectId)
                            line_to_add.name = license_name
                        }
                        linetype = 'License'
                        break;
                    case 'com.k_int.kbplus.Subscription':
                        break;
                    case 'com.k_int.kbplus.Package':
                        def package_object = Package.get(hl.persistedObjectId);
                        if (package_object) {
                            line_to_add.link = createLink(controller:'package', action: 'show', id:hl.persistedObjectId)
                            line_to_add.name = package_object.name
                        }
                        linetype = 'Package'
                        break;
                    case 'com.k_int.kbplus.TitleInstancePackagePlatform':
                        def tipp_object = TitleInstancePackagePlatform.get(hl.persistedObjectId);
                        if ( tipp_object != null ) {
                            line_to_add.link = createLink(controller:'tipp', action: 'show', id:hl.persistedObjectId)
                            line_to_add.name = tipp_object.title?.title + ' / ' + tipp_object.pkg?.name
                        }
                        linetype = 'TIPP'
                        break;
                    case 'com.k_int.kbplus.TitleInstance':
                        def title_object = TitleInstance.get(hl.persistedObjectId);
                        if (title_object) {
                            line_to_add.link = createLink(controller:'title', action: 'show', id:hl.persistedObjectId)
                            line_to_add.name = title_object.title
                        }
                        linetype = 'Title'
                        break;
                    case 'com.k_int.kbplus.Identifier':
                        break;
                    default:
                        log.error("Unexpected event class name found ${hl.className}")
                        break;
                }

                switch ( hl.eventName ) {
                    case 'INSERT':
                        line_to_add.eventName= "New ${linetype}"
                        break;
                    case 'UPDATE':
                        line_to_add.eventName= "Updated ${linetype}"
                        break;
                    case 'DELETE':
                        line_to_add.eventName= "Deleted ${linetype}"
                        break;
                    default:
                        line_to_add.eventName= "Unknown ${linetype}"
                        break;
                }

                if(line_to_add.eventName.contains('null')){
                    log.error("We have a null line in DM change log and we exclude it from output...${hl}");
                }else{
                    result.formattedHistoryLines.add(line_to_add);
                }
            }

        }
        else {
            result.num_hl = 0
        }


        withFormat {
            html {
                result
            }
            csv {
                if(result.formattedHistoryLines.size() == 10000 ){
                    //show some error somehow
                }
                response.setHeader("Content-disposition", "attachment; filename=\"DMChangeLog.csv\"")
                response.contentType = "text/csv"
                def out = response.outputStream
                def actors_list = getActorNameList(params)
                out.withWriter { w ->
                    w.write('Start Date, End Date, Change Actors, Packages, Licenses, Titles, TIPPs, New Items, Updates\n')
                    w.write("\"${params.startDate}\", \"${params.endDate}\", \"${actors_list}\", ${params.packages}, ${params.licenses}, ${params.titles}, ${params.tipps}, ${params.creates}, ${params.updates} \n")
                    w.write('Timestamp,Name,Event,Property,Actor,Old,New,Link\n')
                    result.formattedHistoryLines.each { c ->
                        if(c.eventName){
                            def line = "\"${c.lastUpdated}\",\"${c.name}\",\"${c.eventName}\",\"${c.propertyName}\",\"${c.actor?.displayName}\",\"${c.oldValue}\",\"${c.newValue}\",\"${c.link}\"\n"
                            w.write(line)

                        }
                    }
                }
                out.close()
            }
        }
    }

    private def getActorNameList(params) {
        def actors = []
        //def filterActors = params.findAll{it.key.startsWith("change_actor_")}
        def filterActors = params.change_actors

        if(filterActors) {

            filterActors.each{

                if (it == "change_actor_PEOPLE" ) {
                    actors += "All Real Users"
                }
                if (it == "change_actor_ALL" ) {
                    actors += "All Including System"
                }

                if(it != 'change_actor_ALL' && it != 'change_actor_PEOPLE') {
                    def paramKey = it.replaceAll("[^A-Za-z]", "")//remove things that can cause problems in sql
                    def username = it.split("change_actor_")[1]
                    def user = User.findByUsername(username)
                    if (user){
                        actors += user.displayName
                    }
                }
            }
        }
        return actors
    }

  @Secured(['ROLE_ADMIN'])
  def deletedTitles() {
    Map<String, Object> result = [:]

        result.user = User.get(springSecurityService.principal.id)
        result.max = params.max ? Integer.parseInt(params.max): result.user?.getDefaultPageSizeTMP()

        def paginate_after = params.paginate_after ?: ( (2*result.max)-1);
        result.offset = params.offset ? Integer.parseInt(params.offset) : 0;

    def deleted_title_status =  RefdataValue.getByValueAndCategory('Deleted', RDConstants.TITLE_STATUS)
    def qry_params = [deleted_title_status]

        def base_qry = " from TitleInstance as t where ( t.status = ? )"

        if (params.sort?.length() > 0) {
            base_qry += " order by " + params.sort

            if (params.order?.length() > 0) {
                base_qry += " " + params.order
            }
        }

        result.titleInstanceTotal = Subscription.executeQuery("select t.id "+base_qry, qry_params ).size()
        result.titleList = Subscription.executeQuery("select t ${base_qry}", qry_params, [max:result.max, offset:result.offset]);

        result
    }

    @Secured(['ROLE_ORG_MANAGER', 'ROLE_ADMIN'])
    def deletedOrgs() {
        Map<String, Object> result = [:]

        result.user = User.get(springSecurityService.principal.id)
        result.max = params.max ? Integer.parseInt(params.max): result.user?.getDefaultPageSizeTMP()
        result.editable = true

        def paginate_after = params.paginate_after ?: ( (2*result.max)-1);
        result.offset = params.offset ? Integer.parseInt(params.offset) : 0;

        def delStatus =  RefdataValue.getByValueAndCategory('Deleted', RDConstants.ORG_STATUS)

        def qry_params = [delStatus]
        def query = " from Org as o where ( o.status = ? )"

        if (params.sort?.length() > 0) {
            query += " order by " + params.sort

            if (params.order?.length() > 0) {
                query += " " + params.order
            }
        }

        result.orgTotal = Org.executeQuery("select o.id " + query, qry_params ).size()
        result.orgList = Org.executeQuery("select o ${query}", qry_params, [max:result.max, offset:result.offset])

        result
    }

    @Secured(['ROLE_YODA'])
    Map<String,Object> listPlatformDuplicates() {
        log.debug("listPlatformDuplicates ...")
        Map<String, Object> result = [:]

        log.debug("listPlatformDuplicates ... returning")
        result
    }

    @Secured(['ROLE_YODA'])
    Map<String,Object> listDeletedTIPPS() {
        log.debug("listDeletedTIPPS ...")
        SessionCacheWrapper sessionCache = contextService.getSessionCache()
        Map<String,Object> result = sessionCache.get("DataManagerController/listDeletedTIPPS/result")
        if(!result){
            result = yodaService.processDeletedTIPPs()
            sessionCache.put("DataManagerController/listDeletedTIPPS/result",result)
        }
        log.debug("listDeletedTIPPS ... returning")
        result
    }

    @Secured(['ROLE_YODA'])
    def executeTIPPCleanup() {
        log.debug("WARNING: bulk deletion of TIPP entries triggered! Start nuking!")
        SessionCacheWrapper sessionCache = contextService.getSessionCache()
        Map<String,Object> result = (Map<String,Object>) sessionCache.get("DataManagerController/listDeletedTIPPS/result")
        if(result) {
            //first: merge duplicate entries
            result.mergingTIPPs.each { mergingTIPP ->
                IssueEntitlement.withTransaction { status ->
                    try {
                        IssueEntitlement.executeUpdate('update IssueEntitlement ie set ie.tipp.id = :mergeTarget where ie.id in (:iesToMerge)',[mergeTarget:mergingTIPP.mergeTarget,iesToMerge:mergingTIPP.iesToMerge])
                        status.flush()
                    }
                    catch (Exception e) {
                        log.error("failure on merging TIPPs ... rollback!")
                        status.setRollbackOnly()
                    }
                }
            }
            log.debug("remapping done, purge now duplicate entries ...")
            globalSourceSyncService.cleanUpGorm()
            List<String> colHeaders = ['Konsortium','Teilnehmer','Lizenz','Paket','Titel','Grund']
            List<List<String>> reportRows = []
            Map<RefdataValue,Set<String>> pendingChangeSetupMap = [:]
            Set<String> alreadyProcessed = []

            result.deletedWithoutGOKbRecord.each { entry ->
                entry.each { delTIPP,issueEntitlements ->
                    issueEntitlements.each { ieDetails ->
                        IssueEntitlement ie = (IssueEntitlement) ieDetails.ie
                        switch(ieDetails.action) {
                            case "deleteCascade":
                                //mark as deleted!
                                log.debug("deletion cascade: deleting ${ie}, deleting ${ie.subscription}")
                                //deletionService.deleteIssueEntitlement(ie)
                                //deletionService.deleteSubscription(ie.subscription,false)
                                break
                            case "report": reportRows << [ieDetails.report.consortium,ieDetails.report.subscriber,ieDetails.report.subscription,ieDetails.report.package,ieDetails.report.title,ieDetails.report.cause]
                                break
                            case "remap": if(!alreadyProcessed.contains(delTIPP.gokbId)){
                                //mark obsolete ones as deleted!
                                deletionService.deleteTIPP(delTIPP,TitleInstancePackagePlatform.findByGokbId(ieDetails.target))
                                alreadyProcessed << delTIPP.gokbId
                                }
                                break
                        }
                    }
                }
            }
            result.deletedWithGOKbRecord.each { row ->
                row.each { delTIPP, tippDetails ->
                    Set<Long> tippsToUpdate = pendingChangeSetupMap[tippDetails.status]
                    if(!tippsToUpdate)
                        tippsToUpdate = []
                    tippsToUpdate << delTIPP
                    pendingChangeSetupMap[tippDetails.status] = tippsToUpdate
                }
            }
            pendingChangeSetupMap.each { RefdataValue status, Set<String> tippsToUpdate ->
                log.debug("updating ${tippsToUpdate} to status ${status}")
                TitleInstancePackagePlatform.executeUpdate('update TitleInstancePackagePlatform tipp set tipp.status = :status where tipp.globalUID in :tippsToUpdate',[status:status,tippsToUpdate:tippsToUpdate])
                //hook up pending changes
                Locale locale = LocaleContextHolder.locale
                String defaultAcceptChange = messageSource.getMessage('default.accept.change.ie',null,locale)
                List<IssueEntitlement> iesToNotify = IssueEntitlement.executeQuery('select ie from IssueEntitlement ie where ie.tipp.globalUID in :tippKeys',[tippKeys:tippsToUpdate])
                if(iesToNotify) {
                    tippsToUpdate.each { tippKey ->
                        TitleInstancePackagePlatform tipp = genericOIDService.resolveOID(tippKey)
                        String titleLink = grailsLinkGenerator.link(controller: 'title', action: 'show', id: tipp.title.id)
                        String pkgLink = grailsLinkGenerator.link(controller: 'package', action: 'show', id: tipp.pkg.id)
                        iesToNotify.each { IssueEntitlement ie ->
                            log.debug("notifying subscription ${ie.subscription}")
                            String changeDesc = ""
                            Map<String, Object> changeMap = [:]
                            Object[] args = [titleLink,tipp.title.title,pkgLink,tipp.pkg.name,messageSource.getMessage("tipp.status",null,locale),ie.status,status,defaultAcceptChange]
                            changeDesc = messageSource.getMessage('pendingChange.message_TP02',args,locale)
                            changeMap.changeTarget = "${ie.class.name}:${ie.id}"
                            changeMap.changeType = PendingChangeService.EVENT_PROPERTY_CHANGE
                            changeMap.changeDoc = [prop: 'status', fieldType: RefdataValue.class.name, refdataCategory: RefdataCategory.TIPP_STATUS, 'new': status, 'old': ie.status]
                            changeNotificationService.registerPendingChange(PendingChange.PROP_SUBSCRIPTION,ie.subscription,ie.subscription.getSubscriber(),changeMap,null,null,changeDesc)
                        }
                    }
                }
                else log.debug("no issue entitlements depending!")
            }
            Set<TitleInstancePackagePlatform> tippsToDelete = TitleInstancePackagePlatform.findAllByGokbIdInListAndIdNotInList(result.duplicateTIPPKeys,result.excludes)
            //this is correct; only the duplicates should be deleted!
            if(tippsToDelete)
                deletionService.deleteTIPPsCascaded(tippsToDelete)
            sessionCache.remove("DataManagerController/listDeletedTIPPS/result")
            log.debug("Cleanup finished!")
            withFormat {
                html {

                }
                csv {
                    response.setHeader("Content-disposition","attachment; filename=\"Subscriptions_to_report.csv\"")
                    response.outputStream.withWriter { writer ->
                        writer.write(exportService.generateSeparatorTableString(colHeaders,reportRows,'\t'))
                    }
                    response.outputStream.flush()
                    response.outputStream.close()
                }
            }
        }
        else {
            log.error("data missing, rebuilding data")
        }
    }

  @Secured(['ROLE_ADMIN'])
  def checkPackageTIPPs() {
    Map<String, Object> result = [:]
    result.user = springSecurityService.getCurrentUser()
    params.max =  params.max ?: result.user.getDefaultPageSizeTMP()


        def gokbRecords = []

        ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true).each { api ->
            gokbRecords << GOKbService.getPackagesMap(api, params.q, false).records
        }


        params.sort = params.sort ?: 'name'
        params.order = params.order ?: 'asc'

        result.records = gokbRecords ? gokbRecords.flatten().sort() : null

        result.records?.sort { x, y ->
            if (params.order == 'desc') {
                y."${params.sort}".toString().compareToIgnoreCase x."${params.sort}".toString()
            } else {
                x."${params.sort}".toString().compareToIgnoreCase y."${params.sort}".toString()
            }
        }

        /*if(params.onlyNotEqual) {
          result.tippsNotEqual = []
          result.records.each { hit ->
            if (com.k_int.kbplus.Package.findByGokbId(hit.uuid)) {
              if (com.k_int.kbplus.Package.findByGokbId(hit.uuid)?.tipps?.size() != hit.titleCount && hit.titleCount != 0) {
                result.tippsNotEqual << hit
              }
            }
          }
          result.records = result.tippsNotEqual
        }*/

        result.resultsTotal2 = result.records?.size()

        Integer start = params.offset ? params.int('offset') : 0
        Integer end = params.offset ? params.int('max') + params.int('offset') : params.int('max')
        end = (end > result.records?.size()) ? result.records?.size() : end

        result.records = result.records?.subList(start, end)


        result
    }

    @Secured(['ROLE_ADMIN'])
    def listMailTemplates() {
        def result =[:]

        result.mailTemplates = MailTemplate.getAll()

        result
    }

    @Secured(['ROLE_ADMIN'])
    def createMailTemplate() {

        def mailTemplate = new MailTemplate(params)

        if(mailTemplate.save(flush: true))
        {
            flash.message = message(code: 'default.created.message', args: [message(code: 'mailTemplate.label'), mailTemplate.name])
        }
        else{
            flash.error = message(code: 'default.save.error.message', args: [message(code: 'mailTemplate.label')])
        }

        redirect(action: 'listMailTemplates')
    }

    @Secured(['ROLE_ADMIN'])
    def editMailTemplate() {

        MailTemplate mailTemplate = genericOIDService.resolveOID(params.target)

        if(mailTemplate) {
            mailTemplate.name = params.name
            mailTemplate.subject = params.subject
            mailTemplate.text = params.text
            mailTemplate.language = params.language ? RefdataValue.get(params.language) : mailTemplate.language
            mailTemplate.type = params.type ? RefdataValue.get(params.type) : mailTemplate.type
        }

        if(mailTemplate.save(flush: true))
        {
            flash.message = message(code: 'default.updated.message', args: [message(code: 'mailTemplate.label'), mailTemplate.name])
        }
        else{
            flash.error = message(code: 'default.not.updated.message', args: [message(code: 'mailTemplate.label'), mailTemplate.name])
        }

        redirect(action: 'listMailTemplates')
    }

}
