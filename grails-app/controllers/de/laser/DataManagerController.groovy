package de.laser


import de.laser.helper.SwissKnife
import de.laser.remote.ApiSource
import de.laser.titles.TitleInstance
import de.laser.auth.Role
import de.laser.auth.User
import de.laser.auth.UserOrg
 
import de.laser.helper.DateUtils
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.cache.SessionCacheWrapper
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured
import org.codehaus.groovy.grails.plugins.orm.auditable.AuditLogEvent

import javax.servlet.ServletOutputStream
import java.text.SimpleDateFormat

/**
 * This controller is destined to handle workflows limited to admins in order to perform data managing actions.
 * Some of them are bulk cleanup actions; handle them with care!
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class DataManagerController  {

    ContextService contextService
    ExportService exportService
    GenericOIDService genericOIDService
    GokbService gokbService
    YodaService yodaService

  @Secured(['ROLE_ADMIN'])
  def index() { 
      Map<String, Object> result = [:]
      RefdataValue pending_change_pending_status = RefdataValue.getByValueAndCategory('Pending', RDConstants.PENDING_CHANGE_STATUS)

        result.pendingChanges = PendingChange.executeQuery(
                "select pc from PendingChange as pc where pc.pkg is not null and ( pc.status is null or pc.status = :status ) order by ts desc",
                [status: pending_change_pending_status]
        )

        result
    }

    @Deprecated
    @Secured(['ROLE_ADMIN'])
    def changeLog() {

        log.debug("changeLog ${params}")
        Map<String, Object> result = [:]
        SimpleDateFormat formatter = DateUtils.getLocalizedSDF_noTime()
        boolean exporting = params.format == 'csv'

    if ( exporting ) {
      result.max = 10000
      params.max = 10000
      result.offset = 0
    }
    else {
        User user = contextService.getUser()
        SwissKnife.setPaginationParams(result, params, user)
        params.max = result.max
    }

    if ( params.startDate == null ) {
      GregorianCalendar cal = new java.util.GregorianCalendar()
      cal.setTimeInMillis(System.currentTimeMillis())
      cal.set(Calendar.DAY_OF_MONTH,1)
      params.startDate=formatter.format(cal.getTime())
    }
    if ( params.endDate == null ) { params.endDate = formatter.format(new Date()) }
    if ( ( params.creates == null ) && ( params.updates == null ) ) {
      params.creates='Y'
    }
    if(params.startDate > params.endDate){
      flash.error = message(code:'datamanager.changeLog.error.dates') as String
      return
    }
    String base_query = "from org.codehaus.groovy.grails.plugins.orm.auditable.AuditLogEvent as e where e.className in (:l) AND e.lastUpdated >= :s AND e.lastUpdated <= :e AND e.eventName in (:t)"

        List<String> types_to_include = []
        if ( params.packages=="Y" ) types_to_include.add( Package.class.name )
        if ( params.licenses=="Y" ) types_to_include.add( License.class.name )
        if ( params.titles=="Y" ) types_to_include.add( TitleInstance.class.name )
        if ( params.tipps=="Y" ) types_to_include.add( TitleInstancePackagePlatform.class.name )
        // de.laser.Subscription

        List<String> events_to_include = []
        if ( params.creates=="Y" ) events_to_include.add('INSERT');
        if ( params.updates=="Y" ) events_to_include.add('UPDATE');

        result.actors = []
        List actors_dms = []
        List actors_users = []

        List<String> all_types = [ Package.class.name, License.class.name, TitleInstance.class.name, TitleInstancePackagePlatform.class.name ]

        // Get a distinct list of actors
        List auditActors = AuditLogEvent.executeQuery('select distinct(al.actor) from AuditLogEvent as al where al.className in ( :l  )',[l:all_types])

    Role formal_role = Role.findByAuthority('INST_ADM')
     
    // From the list of users, extract and who have the INST_ADM role
    List rolesMa = []
    if ( auditActors )
      rolesMa = UserOrg.executeQuery(
        'select distinct(userorg.user.username) from UserOrg as userorg ' +
        'where userorg.formalRole = (:formal_role) and userorg.user.username in (:actors)',
        [formal_role:formal_role,actors:auditActors])

    auditActors.each {
      User u = User.findByUsername(it)
      
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
            types_to_include.add( Package.class.name )
            params.packages="Y"
        }

    Date start_date = formatter.parse(params.startDate)
    Date end_date = formatter.parse(params.endDate)

        final long hoursInMillis = 60L * 60L * 1000L;
        end_date = new Date(end_date.getTime() + (24L * hoursInMillis - 2000L));

        def query_params = ['l':types_to_include,'s':start_date,'e':end_date, 't':events_to_include]


        //def filterActors = params.findAll{it.key.startsWith("change_actor_")}
        def filterActors = params.change_actors

    if(filterActors) {
      boolean multipleActors = false
      String condition = "AND ( "
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

                Map<String, Object> line_to_add = [ lastUpdated: hl.lastUpdated,
                                    actor: User.findByUsername(hl.actor),
                                    propertyName: hl.propertyName,
                                    oldValue: hl.oldValue,
                                    newValue: hl.newValue
                ]
                def linetype = null

        switch(hl.className) {
          case License.class.name:
            License license_object = License.get(hl.persistedObjectId);
            if (license_object) {
                String license_name = license_object.reference ?: '**No reference**'
                line_to_add.link = createLink(controller:'license', action: 'show', id:hl.persistedObjectId) as String
                line_to_add.name = license_name
            }
            linetype = 'License'
            break;

          case Subscription.class.name :
            break;

          case Package.class.name :
            Package package_object = Package.get(hl.persistedObjectId);
            if (package_object) {
                line_to_add.link = createLink(controller:'package', action: 'show', id:hl.persistedObjectId) as String
                line_to_add.name = package_object.name
            }
            linetype = 'Package'
            break;

          case TitleInstancePackagePlatform.class.name :
            TitleInstancePackagePlatform tipp_object = TitleInstancePackagePlatform.get(hl.persistedObjectId);
            if ( tipp_object != null ) {
                line_to_add.link = createLink(controller:'tipp', action: 'show', id:hl.persistedObjectId) as String
                line_to_add.name = tipp_object.title?.title + ' / ' + tipp_object.pkg?.name
            }
            linetype = 'TIPP'
            break;

          case TitleInstance.class.name :
            TitleInstance title_object = TitleInstance.get(hl.persistedObjectId);
            if (title_object) {
                line_to_add.link = createLink(controller:'title', action: 'show', id:hl.persistedObjectId) as String
                line_to_add.name = title_object.title
            }
            linetype = 'Title'
            break;

          case Identifier.class.name :
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

                ServletOutputStream out = response.outputStream
                List<String> actors_list = []

                if (params.change_actors) {
                    params.change_actors.each{ ca ->

                        if (ca == "change_actor_PEOPLE" ) {
                            actors_list.add("All Real Users")
                        }
                        if (ca == "change_actor_ALL" ) {
                            actors_list.add("All Including System")
                        }

                        if (ca != 'change_actor_ALL' && ca != 'change_actor_PEOPLE') {
                            String username = ca.split("change_actor_")[1]
                            User user = User.findByUsername(username)
                            if (user){
                                actors_list.add(user.displayName)
                            }
                        }
                    }
                }

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

    @Deprecated
  @Secured(['ROLE_ADMIN'])
  def deletedTitles() {
    Map<String, Object> result = [:]

        result.user = contextService.getUser()
        SwissKnife.setPaginationParams(result, params, (User) result.user)

        String base_qry = " from TitleInstance as t where ( t.status = :status )"
        if (params.sort?.length() > 0) {
            base_qry += " order by " + params.sort

            if (params.order?.length() > 0) {
                base_qry += " " + params.order
            }
        }
        Map<String, Object> qry_params = [status: RDStore.TITLE_STATUS_DELETED]

        result.titleInstanceTotal = Subscription.executeQuery( "select t.id " + base_qry, qry_params ).size()
        result.titleList = Subscription.executeQuery( "select t " + base_qry, qry_params, [max:result.max, offset:result.offset] )

        result
    }

    @Deprecated
    @Secured(['ROLE_ORG_MANAGER', 'ROLE_ADMIN'])
    def deletedOrgs() {
        Map<String, Object> result = [:]

        result.user = contextService.getUser()
        result.editable = true
        SwissKnife.setPaginationParams(result, params, (User) result.user)

        String query = " from Org as o where ( o.status = :status )"
        if (params.sort?.length() > 0) {
            query += " order by " + params.sort

            if (params.order?.length() > 0) {
                query += " " + params.order
            }
        }
        Map<String, Object> qry_params = [status: RDStore.O_STATUS_DELETED]

        result.orgTotal = Org.executeQuery( "select o.id " + query, qry_params ).size()
        result.orgList = Org.executeQuery( "select o " + query, qry_params, [max:result.max, offset:result.offset] )

        result
    }

    /**
     * Cleanup method to depollute the database from platform duplicates
     */
    @Secured(['ROLE_YODA'])
    Map<String,Object> listPlatformDuplicates() {
        log.debug("listPlatformDuplicates ...")
        SessionCacheWrapper sessionCache = contextService.getSessionCache()
        Map<String, Object> result = sessionCache.get("DataManagerController/listPlatformDuplicates/result")
        if(!result){
            result = yodaService.listPlatformDuplicates()
            sessionCache.put("DataManagerController/listDeletedTIPPS/result",result)
        }

        log.debug("listPlatformDuplicates ... returning")
        result
    }

    /**
     * Executes the deduplicating of platforms. Is a very dangerous procedure, please handle it with extreme care!
     */
    @Secured(['ROLE_YODA'])
    def executePlatformCleanup() {
        log.debug("WARNING: bulk deletion of platforms triggered! Start nuking!")
        SessionCacheWrapper sessionCache = contextService.getSessionCache()
        Map<String,Object> result = (Map<String,Object>) sessionCache.get("DataManagerController/listDeletedTIPPS/result")
        if(result) {
            yodaService.executePlatformCleanup(result)
        }
        else {
            log.warn("Data missing. Please rebuild data ...")
        }
        redirect(controller: 'platform',action: 'list')
    }

    /**
     * Lists titles which have been marked as deleted; gets result from cache if it is set
     */
    @Secured(['ROLE_YODA'])
    Map<String,Object> listDeletedTIPPS() {
        log.debug("listDeletedTIPPS ...")
        SessionCacheWrapper sessionCache = contextService.getSessionCache()
        Map<String,Object> result = sessionCache.get("DataManagerController/listDeletedTIPPS/result")
        if(!result){
            result = yodaService.listDeletedTIPPs()
            sessionCache.put("DataManagerController/listDeletedTIPPS/result",result)
        }
        log.debug("listDeletedTIPPS ... returning")
        result
    }

    /**
     * Executes the cleanup among the titles and returns those which need to be reported because a title has
     * current issue entitlements hanging on false title records and cannot be remapped
     * @return a CSV table with holdings whose subscribers must be notified
     */
    @Secured(['ROLE_YODA'])
    def executeTIPPCleanup() {
        log.debug("WARNING: bulk deletion of TIPP entries triggered! Start nuking!")
        SessionCacheWrapper sessionCache = contextService.getSessionCache()
        Map<String,Object> result = (Map<String,Object>) sessionCache.get("DataManagerController/listDeletedTIPPS/result")
        if(result) {
            List<List<String>> reportRows = yodaService.executeTIPPCleanup(result)
            sessionCache.remove("DataManagerController/listDeletedTIPPS/result")
            List<String> colHeaders = ['Konsortium','Teilnehmer','Lizenz','Paket','Titel','Grund']
            String csvTable = exportService.generateSeparatorTableString(colHeaders,reportRows,'\t')
            withFormat {
                html {

                }
                csv {
                    response.setHeader("Content-disposition","attachment; filename=\"Subscriptions_to_report.csv\"")
                    response.outputStream.withWriter { writer ->
                        writer.write(csvTable)
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

  @Deprecated
  @Secured(['ROLE_ADMIN'])
  def checkPackageTIPPs() {
    Map<String, Object> result = [:]
    result.user = contextService.getUser()
    params.max =  params.max ?: result.user.getDefaultPageSize()

        List gokbRecords = []

      ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true).each { api ->
            gokbRecords << gokbService.getPackagesMap(api, params.q, false).records
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

        result.resultsTotal2 = result.records?.size()

        Integer start = params.offset ? params.int('offset') : 0
        Integer end = params.offset ? params.int('max') + params.int('offset') : params.int('max')
        end = (end > result.records?.size()) ? result.records?.size() : end

        result.records = result.records?.subList(start, end)

        result
    }

    /**
     * Lists the current email templates in the app
     */
  @Secured(['ROLE_ADMIN'])
  def listMailTemplates() {
    Map<String, Object> result = [:]
        result.mailTemplates = MailTemplate.getAll()
        result
    }

    /**
     * Creates a new email template with the given parameters
     * @return the updated list view
     */
    @Transactional
    @Secured(['ROLE_ADMIN'])
    def createMailTemplate() {

        MailTemplate mailTemplate = new MailTemplate(params)

        if(mailTemplate.save()) {
            flash.message = message(code: 'default.created.message', args: [message(code: 'mailTemplate.label'), mailTemplate.name]) as String
        }
        else {
            flash.error = message(code: 'default.save.error.message', args: [message(code: 'mailTemplate.label')]) as String
        }

        redirect(action: 'listMailTemplates')
    }

    /**
     * Updates the given email template
     * @return
     */
    @Transactional
    @Secured(['ROLE_ADMIN'])
    def editMailTemplate() {

        MailTemplate mailTemplate = (MailTemplate) genericOIDService.resolveOID(params.target)

        if(mailTemplate) {
            mailTemplate.name = params.name
            mailTemplate.subject = params.subject
            mailTemplate.text = params.text
            mailTemplate.language = params.language ? RefdataValue.get(params.language) : mailTemplate.language
            mailTemplate.type = params.type ? RefdataValue.get(params.type) : mailTemplate.type
        }

        if(mailTemplate.save()) {
            flash.message = message(code: 'default.updated.message', args: [message(code: 'mailTemplate.label'), mailTemplate.name]) as String
        }
        else{
            flash.error = message(code: 'default.not.updated.message', args: [message(code: 'mailTemplate.label'), mailTemplate.name]) as String
        }

        redirect(action: 'listMailTemplates')
    }

}
