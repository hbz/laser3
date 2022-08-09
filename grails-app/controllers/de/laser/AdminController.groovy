package de.laser

import de.laser.config.ConfigDefaults
import de.laser.utils.AppUtils
import de.laser.helper.DatabaseInfo
import de.laser.utils.LocaleUtils
import de.laser.cache.EhcacheWrapper
import de.laser.utils.SwissKnife
import de.laser.remote.FTControl
import de.laser.titles.BookInstance
import de.laser.titles.TitleInstance
import de.laser.auth.Role
import de.laser.auth.User
import de.laser.auth.UserOrg
import de.laser.auth.UserRole
import de.laser.properties.PropertyDefinition
import de.laser.properties.PropertyDefinitionGroup
import de.laser.properties.PropertyDefinitionGroupItem
import de.laser.api.v0.ApiToolkit

import de.laser.storage.RDStore
import de.laser.system.SystemAnnouncement
import de.laser.system.SystemEvent
import de.laser.system.SystemMessage
import de.laser.workflow.WfConditionPrototype
import de.laser.workflow.WfWorkflowPrototype
import de.laser.workflow.WfTaskPrototype
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import grails.plugins.mail.MailService
import groovy.sql.Sql
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.query.NativeQuery
import org.springframework.web.multipart.commons.CommonsMultipartFile
import de.laser.config.ConfigMapper

import java.nio.file.Files
import java.nio.file.Path

/**
 * This controller contains methods which are at least ROLE_ADMIN secured. Those are among the
 * dangerous calls the less dangerous ones. The really dangerous methods are located in the
 * {@link YodaController}; those are bulk operations which are - once done - irreversible
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class AdminController  {

    CacheService cacheService
    ChangeNotificationService changeNotificationService
    ContextService contextService
    DataConsistencyService dataConsistencyService
    DataloadService dataloadService
    DeletionService deletionService
    FilterService filterService
    GenericOIDService genericOIDService
    GlobalSourceSyncService globalSourceSyncService
    MailService mailService
    PropertyService propertyService
    RefdataService refdataService
    SessionFactory sessionFactory
    StatsSyncService statsSyncService
    WorkflowService workflowService

    /**
     * Empty call, loads empty admin dashboard
     */
    @Secured(['ROLE_ADMIN'])
    @Transactional
    def index() {
        List dbmQuery = (sessionFactory.currentSession.createSQLQuery(
                'SELECT filename, id, dateexecuted from databasechangelog order by orderexecuted desc limit 1'
        )).list()

        Map<String, Object> result = [
                database: [
                        dbmVersion  : dbmQuery.size() > 0 ? dbmQuery.first() : ['unkown', 'unkown', 'unkown'],
                        default: [
                                dbName : ConfigMapper.getConfig('dataSource.url', String).split('/').last(),
                        ],
                        storage: [
                                dbName : ConfigMapper.getConfig('dataSources.storage.url', String).split('/').last(),
                        ]
                ],
                events      : SystemEvent.list([max: 10, sort: 'created', order: 'desc']),
                docStore    : AppUtils.getDocumentStorageInfo()
        ]

        result
    }

    /**
     * This method manages system-wide announcements. Those are made if system-relevant messages need to be transmit to every
     * registered account like maintenance shutdowns or general announcements are being made which may concern every account, too,
     * like training course availabilities or new releases of the software. Those messages can be sent via mail as well and it is
     * possible to retire a message from publishing; if a system announcement is being published, it will be displayed on the landing
     * page of the webapp
     * @return a view containing the all system announcements
     */
    @Secured(['ROLE_ADMIN'])
    @Transactional
    def systemAnnouncements() {
        Map<String, Object> result = [:]

        result.mailDisabled = ConfigMapper.getConfig('grails.mail.disabled', Boolean)

        if (params.id) {
            SystemAnnouncement sa = SystemAnnouncement.get(params.long('id'))

            if (sa) {
                if (params.cmd == 'edit') {
                    result.currentAnnouncement = sa
                }
                else if (params.cmd == 'publish') {
                    if (result.mailDisabled) {
                        flash.error = message(code: 'system.config.mail.disabled') as String
                    }
                    else if (sa.publish()) {
                        flash.message = message(code: 'announcement.published') as String
                    }
                    else {
                        flash.error = message(code: 'announcement.published_error') as String
                    }
                }
                else if (params.cmd == 'undo') {
                    sa.isPublished = false
                    if (sa.save()) {
                        flash.message = message(code: 'announcement.undo') as String
                    }
                    else {
                        flash.error = message(code: 'announcement.undo_error') as String
                    }
                }
                else if (params.cmd == 'delete') {
                    if (sa.delete()) {
                        flash.message = message(code: 'default.success') as String
                    }
                    else {
                        flash.error = message(code: 'default.delete.error.general.message') as String
                    }
                }
            }
        }
        result.numberOfCurrentRecipients = SystemAnnouncement.getRecipients().size()
        result.announcements = SystemAnnouncement.list(sort: 'lastUpdated', order: 'desc')
        result
    }

    @Secured(['ROLE_ADMIN'])
    @Transactional
    def testMailSending() {
        Map<String, Object> result = [:]

        result.mailDisabled = ConfigMapper.getConfig('grails.mail.disabled', Boolean)

        if (params.sendTestMail == 'Send Test Mail' && params.mailAddress) {
            if (result.mailDisabled == true) {
                flash.error = 'Failed due grails.mail.disabled = true'
            }else {
                String currentServer = AppUtils.getCurrentServer()
                String subjectSystemPraefix = (currentServer == AppUtils.PROD) ? "LAS:eR - " : (ConfigMapper.getLaserSystemId() + " - ")
                String mailSubject = subjectSystemPraefix + params.subject

                    mailService.sendMail {
                        to      params.mailAddress
                        from    ConfigMapper.getNotificationsEmailFrom()
                        subject mailSubject
                        body    params.content
                    }
                flash.message = "Test email was sent successfully"
            }

        }
        result
    }

    /**
     * This controller processes data and creates a new system announcement; if an ID is being provided,
     * a record matching the ID will be retrieved and if successful, the data is being updated
     */
    @Secured(['ROLE_ADMIN'])
    @Transactional
    def createSystemAnnouncement() {
        if (params.saTitle && params.saContent) {
            SystemAnnouncement sa
            boolean isNew = false

            if (params.saId) {
                sa = SystemAnnouncement.get(params.long('saId'))
            }
            if (!sa) {
                sa = new SystemAnnouncement()
                isNew = true
            }

            sa.title = params.saTitle
            sa.content = params.saContent
            sa.user = contextService.getUser()
            sa.isPublished = false

            if (sa.save()) {
                flash.message = isNew ? message(code: 'announcement.created') : message(code: 'announcement.updated') as String
            }
            else {
                flash.error = message(code: 'default.save.error.message', args: [sa]) as String
            }
        }
        else {
            flash.error = message(code: 'default.error') as String
        }
        redirect(action: 'systemAnnouncements')
    }

    /**
     * Enumerates the types and counts of deleted objects in the system
     */
    @Secured(['ROLE_ADMIN'])
    def manageDeletedObjects() {
        Map<String, Object> result = [:]
        result.stats = [:]

        List jobList = [
                'DocContext',
                //['GlobalRecordInfo', 'globalRecordInfoStatus'],
                'IssueEntitlement',
                'License',
                'Org',
                ['Package', 'packageStatus'],
                'Platform',
                'Subscription',
                'TitleInstance',
                'TitleInstancePackagePlatform',
                'Combo'
                //'Doc'
        ]
        result.jobList = jobList

        jobList.each { job ->
            if (job instanceof String) {
                log.info('processing: ' + job)
                String query = "select count(obj) from ${job} obj join obj.status s where lower(s.value) like 'deleted'"
                result.stats."${job}" = Org.executeQuery( query )
            }
            else {
                log.info('processing: ' + job[0])
                String query = "select count(obj) from ${job[0]} obj join obj.${job[1]} s where lower(s.value) like 'deleted'"
                result.stats."${job[0]}" = Org.executeQuery( query )
            }
        }
        result
    }

    @Deprecated
    @Secured(['ROLE_ADMIN'])
    def hardDeletePkgs(){
        Map<String, Object> result = [:]
        //If we make a search while paginating return to start
        if(params.search == "yes"){
            params.offset = 0
            params.search = null
        }
        result.user = contextService.getUser()
        SwissKnife.setPaginationParams(result, params, (User) result.user)

    if(params.id){
        Package pkg = Package.get(params.id)
        List conflicts_list = []

      if(pkg.documents){
        def document_map = [:]
        document_map.name = "Documents"
        document_map.details = []
        pkg.documents.each{
          document_map.details += ['text':it.owner.title]
        }
        document_map.action = ['actionRequired':false,'text':"References will be deleted"]
        conflicts_list += document_map
      }
      if(pkg.subscriptions){
        def subscription_map = [:]
        subscription_map.name = "Subscriptions"
        subscription_map.details = []
        pkg.subscriptions.each{
          subscription_map.details += ['link':createLink(controller:'subscription', action: 'show', id:it.subscription.id), 'text': it.subscription.name]
        }
        subscription_map.action = ['actionRequired':true,'text':"Unlink subscriptions. (IEs will be removed as well)"]
        if(subscription_map.details){
          conflicts_list += subscription_map
        }
      }
      if(pkg.tipps){
        def tipp_map = [:]
        tipp_map.name = "TIPPs"
        def totalIE = 0
        pkg.tipps.each{
          totalIE += IssueEntitlement.countByTipp(it)
        }
        tipp_map.details = [['text':"Number of TIPPs: ${pkg.tipps.size()}"],
                ['text':"Number of IEs: ${totalIE}"]]
        tipp_map.action = ['actionRequired':false,'text':"TIPPs and IEs will be deleted"]
        conflicts_list += tipp_map
      }
      result.conflicts_list = conflicts_list
      result.pkg = pkg

            render(template: "hardDeleteDetails",model:result)
        }else{

            def criteria = Package.createCriteria()
            result.pkgs = criteria.list(max: result.max, offset:result.offset){
                if(params.pkg_name){
                    ilike("name","${params.pkg_name}%")
                }
                order("name", params.order?:'asc')
            }
        }

        result
    }

    @Deprecated
  @Secured(['ROLE_ADMIN'])
  def performPackageDelete(){
   if (request.method == 'POST'){
      Package pkg = Package.get(params.id)
      Package.withTransaction { status ->
        log.info("Deleting Package ")
        log.info("${pkg.id}::${pkg}")
        pkg.pendingChanges.each{
          it.delete()
        }
        pkg.documents.each{
          it.delete()
        }
        pkg.orgs.each{
          it.delete()
        }

                pkg.subscriptions.each{
                    it.delete()
                }
                pkg.tipps.each{
                    it.delete()
                }
                pkg.delete()
            }
            log.info("Delete Complete.")
        }
        redirect controller: 'admin', action:'hardDeletePkgs'

    }

    @Deprecated
    @Secured(['ROLE_ADMIN'])
    @Transactional
    def userMerge(){
        log.debug("AdminController :: userMerge :: ${params}")
        def usrMrgId = params.userToMerge == "null"?null:params.userToMerge
        def usrKeepId = params.userToKeep == "null"?null:params.userToKeep
        Map<String, Object> result = [:]

        try {
            log.debug("Determine user merge operation : ${request.method}")
            switch (request.method) {
                case 'GET':
                    if(usrMrgId && usrKeepId ){
                        User usrMrg = User.get(usrMrgId)
                        User usrKeep =  User.get(usrKeepId)
                        log.debug("Selected users : ${usrMrg}, ${usrKeep}")

                        result.userRoles = usrMrg.getAuthorities()
                        result.userAffiliations =  usrMrg.affiliations
                        result.userMerge = usrMrg
                        result.userKeep = usrKeep
                    }
                    else{
                        log.error("Missing keep/merge userid ${params}")
                        flash.error = "Please select'user to keep' and 'user to merge' from the dropdown."
                    }
                    log.debug("Get processing completed")
                    break;
                case 'POST':
                    log.debug("Post...")
                    if(usrMrgId && usrKeepId){
                        User usrMrg = User.get(usrMrgId)
                        User usrKeep =  User.get(usrKeepId)
                        boolean success = false
                        try{
                            log.debug("Copying user roles... from ${usrMrg} to ${usrKeep}")
                            success = copyUserRoles(usrMrg, usrKeep)
                            log.debug("Result of copyUserRoles : ${success}")
                        }
                        catch(Exception e){
                            log.error("Exception while copying user roles.",e)
                        }
                        if(success){
                            log.debug("Success")
                            usrMrg.enabled = false
                            log.debug("Save disable and save merged user")
                            usrMrg.save(failOnError:true)
                            flash.message = "Rights copying successful. User '${usrMrg.displayName}' is now disabled."
                        }else{
                            flash.error = "An error occured before rights transfer was complete."
                        }
                    }else{
                        flash.error = "Please select 'user to keep' and 'user to merge' from the dropdown."
                    }
                    break
                default:
                    break;
            }

            log.debug("Get all users");
            result.usersAll = User.list(sort:"display", order:"asc")

            log.debug("Get active users");
            String activeHQL = " from User as usr where usr.enabled=true or usr.enabled=null order by display asc"
            result.usersActive = User.executeQuery(activeHQL)
        }
        catch ( Exception e ) {
            log.error("Problem in user merge", e)
        }

        log.debug("Returning ${result}")
        result
    }

    @Deprecated
    @Secured(['ROLE_ADMIN'])
    @Transactional
    def copyUserRoles(User usrMrg, User usrKeep){
        Set<Role> mergeRoles = usrMrg.getAuthorities()
        Set<UserOrg> mergeAffil = usrMrg.affiliations
        Set<Role> currentRoles = usrKeep.getAuthorities()
        Set<UserOrg> currentAffil = usrKeep.affiliations

        mergeRoles.each{ role ->
            if (!currentRoles.contains(role) && role.authority != "ROLE_YODA") {
                UserRole.create(usrKeep,role)
            }
        }
        mergeAffil.each{affil ->
            if(!currentAffil.contains(affil)){
                // We should check that the new role does not already exist
                UserOrg existing_affil_check = UserOrg.findByOrgAndUserAndFormalRole(affil.org,usrKeep,affil.formalRole)

        if ( existing_affil_check == null ) {
            log.debug("No existing affiliation")
            UserOrg newAffil = new UserOrg(org:affil.org,user:usrKeep,formalRole:affil.formalRole)
            if(!newAffil.save(failOnError:true)){
                log.error("Probem saving user roles")
                newAffil.errors.each { e ->
                    log.error( e.toString() )
                }
                return false
            }
        }
      }
    }
    log.debug("copyUserRoles returning true");
    return true
  }

    /**
     * Gets the current workflows and returns a dashboard-like overview of the outstanding tasks
     */
    @Secured(['ROLE_ADMIN'])
    def manageWorkflows() {
        Map<String, Object> result = [:]

        if (params.cmd) {
            result = workflowService.cmd(params)
        }
        if (params.tab) {
            result.tab = params.tab
        }

        result.wfpIdTable = [:] as Map<Long, Integer>
        result.tpIdTable  = [:] as Map<Long, Integer>
        result.cpIdTable  = [:] as Map<Long, Integer>

        result.wfpList = WfWorkflowPrototype.executeQuery('select wfp from WfWorkflowPrototype wfp order by id')
        result.tpList  = WfTaskPrototype.executeQuery('select tp from WfTaskPrototype tp order by id')
        result.cpList  = WfConditionPrototype.executeQuery('select cp from WfConditionPrototype cp order by id')

        result.wfpList.eachWithIndex { wfp, i -> result.wfpIdTable.put( wfp.id, i+1 ) }
        result.tpList.eachWithIndex { tp, i -> result.tpIdTable.put( tp.id, i+1 ) }
        result.cpList.eachWithIndex { cp, i -> result.cpIdTable.put( cp.id, i+1 ) }

        EhcacheWrapper cache = cacheService.getTTL1800Cache('admin/manageWorkflows')
        cache.put( 'wfpIdTable', result.wfpIdTable )
        cache.put( 'tpIdTable', result.tpIdTable )
        cache.put( 'cpIdTable', result.cpIdTable )

        log.debug( result.toMapString() )
        result
    }

    /**
     * Shows recorded system events; default count is 100 last entries.
     * The record listing may be filtered
     * @see SystemEvent
     */
    @Secured(['ROLE_ADMIN'])
    def systemEvents() {
        Map<String, Object> result = [:]

        params.filter_limit = params.filter_limit ?: 100

        if (params.filter_category) { result.put('filter_category', params.filter_category) }
        if (params.filter_relevance){ result.put('filter_relevance', params.filter_relevance) }
        if (params.filter_source)   { result.put('filter_source', params.filter_source) }
        if (params.filter_exclude)  { result.put('filter_exclude', params.filter_exclude) }
        if (params.filter_limit)    { result.put('filter_limit', params.filter_limit) }

        result.events = SystemEvent.list([max: params.filter_limit, sort: 'created', order: 'desc'])
        result
    }
    
    /**
     * Enumerates the database collations currently used in the tables
     */
    @Secured(['ROLE_ADMIN'])
    def databaseCollations() {

        Map<String, Object> result = [:]

        Sql sql = GlobalService.obtainSqlConnection()

        result.allTables = DatabaseInfo.getAllTablesWithCollations()

        result.collate_current = DatabaseInfo.getDatabaseCollate()
        result.collate_de = 'de_DE.UTF-8'
        result.collate_en = 'en_US.UTF-8'
        result.current_de = 'current_de'
        result.current_en = 'current_en'
        int limit = 500;

        String query1de = "select rdv.rdv_value_de from refdata_value rdv, refdata_category rdc where rdv.rdv_owner = rdc.rdc_id and rdc.rdc_description = 'country'"
        String query2de = "select rdv.rdv_value_de from refdata_value rdv, refdata_category rdc where rdv.rdv_owner = rdc.rdc_id and rdc.rdc_description = 'ddc'"

        String query1en = "select rdv.rdv_value_en from refdata_value rdv, refdata_category rdc where rdv.rdv_owner = rdc.rdc_id and rdc.rdc_description = 'country'"
        String query2en = "select rdv.rdv_value_en from refdata_value rdv, refdata_category rdc where rdv.rdv_owner = rdc.rdc_id and rdc.rdc_description = 'ddc'"

        String query3 = "select org_name from org"
        String query4 = "select ti_title from title_instance"

        result.examples = [
                country : [
                        'de_DE.UTF-8' : sql.rows( query1de + ' order by rdv.rdv_value_de COLLATE "de_DE" limit ' + limit ).collect{ it.rdv_value_de },
                        'en_US.UTF-8' : sql.rows( query1en + ' order by rdv.rdv_value_en COLLATE "en_US" limit ' + limit ).collect{ it.rdv_value_en },
                        'current_de'  : RefdataValue.executeQuery( "select value_de from RefdataValue where owner.desc = 'country' order by value_de", [max: limit] ),
                        'current_en'  : RefdataValue.executeQuery( "select value_en from RefdataValue where owner.desc = 'country' order by value_en", [max: limit] )
                ],
                ddc : [
                        'de_DE.UTF-8' : sql.rows( query2de + ' order by rdv.rdv_value_de COLLATE "de_DE" limit ' + limit ).collect{ it.rdv_value_de },
                        'en_US.UTF-8' : sql.rows( query2en + ' order by rdv.rdv_value_en COLLATE "en_US" limit ' + limit ).collect{ it.rdv_value_en },
                        'current_de'  : RefdataValue.executeQuery( "select value_de from RefdataValue where owner.desc = 'ddc' order by value_de", [max: limit] ),
                        'current_en'  : RefdataValue.executeQuery( "select value_en from RefdataValue where owner.desc = 'ddc' order by value_en", [max: limit] )
                ],
                org : [
                        'de_DE.UTF-8' : sql.rows( query3 + ' order by org_name COLLATE "de_DE" limit ' + limit ).collect{ it.org_name },
                        'en_US.UTF-8' : sql.rows( query3 + ' order by org_name COLLATE "en_US" limit ' + limit ).collect{ it.org_name },
                        'current_de'  : RefdataValue.executeQuery( "select name from Org order by name", [max: limit] ),
                        'current_en'  : RefdataValue.executeQuery( "select name from Org order by name", [max: limit] )
                ],
                title : [
                        'de_DE.UTF-8' : sql.rows( query4 + ' order by ti_title COLLATE "de_DE" limit ' + limit ).collect{ it.ti_title },
                        'en_US.UTF-8' : sql.rows( query4 + ' order by ti_title COLLATE "en_US" limit ' + limit ).collect{ it.ti_title },
                        'current_de'  : RefdataValue.executeQuery( "select title from TitleInstance order by title", [max: limit] ),
                        'current_en'  : RefdataValue.executeQuery( "select title from TitleInstance order by title", [max: limit] )
                ]
        ]

        String de_x_icu = DatabaseInfo.DE_U_CO_PHONEBK_X_ICU
        result.examples['country'].putAt( de_x_icu, sql.rows( query1de + ' order by rdv.rdv_value_de COLLATE "' + de_x_icu + '" limit ' + limit ).collect{ it.rdv_value_de } )
        result.examples[    'ddc'].putAt( de_x_icu, sql.rows( query2de + ' order by rdv.rdv_value_de COLLATE "' + de_x_icu + '" limit ' + limit ).collect{ it.rdv_value_de } )
        result.examples[    'org'].putAt( de_x_icu, sql.rows( query3 + ' order by org_name COLLATE "' + de_x_icu + '" limit ' + limit ).collect{ it.org_name } )
        result.examples[  'title'].putAt( de_x_icu, sql.rows( query4 + ' order by ti_title COLLATE "' + de_x_icu + '" limit ' + limit ).collect{ it.ti_title } )

        String en_x_icu = DatabaseInfo.EN_US_U_VA_POSIX_X_ICU
        result.examples['country'].putAt( en_x_icu, sql.rows( query1en + ' order by rdv.rdv_value_en COLLATE "' + en_x_icu + '" limit ' + limit ).collect{ it.rdv_value_en } )
        result.examples[    'ddc'].putAt( en_x_icu, sql.rows( query2en + ' order by rdv.rdv_value_en COLLATE "' + en_x_icu + '" limit ' + limit ).collect{ it.rdv_value_en } )
        result.examples[    'org'].putAt( en_x_icu, sql.rows( query3 + ' order by org_name COLLATE "' + en_x_icu + '" limit ' + limit ).collect{ it.org_name } )
        result.examples[  'title'].putAt( en_x_icu, sql.rows( query4 + ' order by ti_title COLLATE "' + en_x_icu + '" limit ' + limit ).collect{ it.ti_title } )

        result
    }

    @Secured(['ROLE_ADMIN'])
    def databaseInfo() {

        Session hibSess = sessionFactory.currentSession
        List dbmQuery = (hibSess.createSQLQuery(
                'SELECT filename, id, dateexecuted from databasechangelog order by orderexecuted desc limit 1'
        )).list()

        Map<String, Object> result = [
            dbmVersion       : dbmQuery.size() > 0 ? dbmQuery.first() : ['unkown', 'unkown', 'unkown'],
            dbmUpdateOnStart : ConfigMapper.getPluginConfig('databasemigration.updateOnStart', Boolean),

            default: [
                    dbName           : ConfigMapper.getConfig('dataSource.url', String).split('/').last(),
                    dbmDbCreate      : ConfigMapper.getConfig('dataSource.dbCreate', String),
                    defaultCollate   : DatabaseInfo.getDatabaseCollate(),
                    dbConflicts      : DatabaseInfo.getDatabaseConflicts(),
                    dbSize           : DatabaseInfo.getDatabaseSize(),
                    dbStatistics     : DatabaseInfo.getDatabaseStatistics(),
                    dbActivity       : DatabaseInfo.getDatabaseActivity(),
                    dbUserFunctions  : DatabaseInfo.getDatabaseUserFunctions(),
                    dbTableUsage     : DatabaseInfo.getAllTablesUsageInfo()
            ],
            storage: [
                    dbName           : ConfigMapper.getConfig('dataSources.storage.url', String).split('/').last(), // TODO
                    dbmDbCreate      : ConfigMapper.getConfig('dataSources.storage.dbCreate', String), // TODO
                    defaultCollate   : DatabaseInfo.getDatabaseCollate( DatabaseInfo.DATASOURCE_STORAGE ),
                    dbConflicts      : DatabaseInfo.getDatabaseConflicts( DatabaseInfo.DATASOURCE_STORAGE ),
                    dbSize           : DatabaseInfo.getDatabaseSize( DatabaseInfo.DATASOURCE_STORAGE ),
                    dbStatistics     : DatabaseInfo.getDatabaseStatistics( DatabaseInfo.DATASOURCE_STORAGE ),
                    dbActivity       : DatabaseInfo.getDatabaseActivity( DatabaseInfo.DATASOURCE_STORAGE ),
                    dbUserFunctions  : DatabaseInfo.getDatabaseUserFunctions( DatabaseInfo.DATASOURCE_STORAGE ),
                    dbTableUsage     : DatabaseInfo.getAllTablesUsageInfo( DatabaseInfo.DATASOURCE_STORAGE )
            ]
        ]

        [dbInfo: result]
    }

    /**
     * Reveals duplicate objects, i.e. different objects named equally
     */
    @Secured(['ROLE_ADMIN'])
    def dataConsistency() {
        Map<String, Object> result = [:]

        if (params.task) {
            List objIds = params.list('objId')

            if (params.task == 'merge' && params.objType == 'Org') {
                log.debug('dataConsistency( merge, ' + params.objType + ', ' + objIds + ' )')

                Org replacement = Org.get(objIds.first())
                for (int i = 1; i < objIds.size(); i++) {
                    deletionService.deleteOrganisation( Org.get(objIds[i]), replacement, false )
                }
            }
            if (params.task == 'delete' && params.objType == 'Org') {
                log.debug('dataConsistency( delete, ' + params.objType + ', ' + objIds + ' )')

                for (int i = 0; i < objIds.size(); i++) {
                    deletionService.deleteOrganisation( Org.get(objIds[i]), null, false )
                }
            }
            params.remove('task')
            params.remove('objType')
            params.remove('objId')

            redirect controller: 'admin', action: 'dataConsistency'
            return
        }

        result.titles    = dataConsistencyService.checkTitles()

        result
    }

    /**
     * Checks the state of the files in the data storage, namely if the files have a database record and if there
     * are files matching the UUIDs from the database and if the database records are attached to an object
     * @see Doc
     * @see DocContext
     */
    @Secured(['ROLE_ADMIN'])
    def fileConsistency() {
        Map<String, Object> result = [:]

        result.filePath = ConfigMapper.getDocumentStorageLocation() ?: ConfigDefaults.DOCSTORE_LOCATION_FALLBACK

        Closure fileCheck = { Doc doc ->

            try {
                File test = new File("${result.filePath}/${doc.uuid}")
                if (test.exists() && test.isFile()) {
                    return true
                }
            }
            catch (Exception e) {
                return false
            }
        }

        // files

        result.listOfFiles = []
        result.listOfFilesMatchingDocs = []
        result.listOfFilesOrphaned = []

        result.listOfDocsInUse = []
        result.listOfDocsInUseOrphaned = []

        result.listOfDocsNotInUse= []
        result.listOfDocsNotInUseOrphaned = []

        try {
            File folder = new File("${result.filePath}")

            if (folder.exists()) {
                result.listOfFiles = folder.listFiles().collect{it.getName()}

                result.listOfFilesMatchingDocs = Doc.executeQuery(
                        'select doc from Doc doc where doc.contentType = :ct and doc.uuid in (:files)',
                        [ct: Doc.CONTENT_TYPE_FILE, files: result.listOfFiles]
                )
                List<String> matches = result.listOfFilesMatchingDocs.collect{ it.uuid }

                result.listOfFiles.each { ff ->
                    if (! matches.contains(ff)) {
                        result.listOfFilesOrphaned << ff
                    }
                }
                result.listOfFilesOrphaned.toSorted()
            }
        }
        catch (Exception e) {}

        // docs

        List<Doc> listOfDocs = Doc.executeQuery(
                'select doc from Doc doc where doc.contentType = :ct order by doc.id',
                [ct: Doc.CONTENT_TYPE_FILE]
        )

        result.listOfDocsInUse = Doc.executeQuery(
                'select distinct(doc) from DocContext dc join dc.owner doc where doc.contentType = :ct order by doc.id',
                [ct: Doc.CONTENT_TYPE_FILE]
        )

        result.listOfDocsNotInUse = listOfDocs - result.listOfDocsInUse

        result.listOfDocsInUse.each{ doc ->
            if (! fileCheck(doc)) {
                result.listOfDocsInUseOrphaned << doc
            }
        }
        result.listOfDocsNotInUse.each{ doc ->
            if (! fileCheck(doc)) {
                result.listOfDocsNotInUseOrphaned << doc
            }
        }

        // doc contexts

        result.numberOfDocContextsInUse = DocContext.executeQuery(
                'select distinct(dc) from DocContext dc join dc.owner doc where doc.contentType = :ct and (dc.status is null or dc.status != :del)',
                [ct: Doc.CONTENT_TYPE_FILE, del: RDStore.DOC_CTX_STATUS_DELETED]
        ).size()

        result.numberOfDocContextsDeleted = DocContext.executeQuery(
                'select distinct(dc) from DocContext dc join dc.owner doc where doc.contentType = :ct and dc.status = :del',
                [ct: Doc.CONTENT_TYPE_FILE, del: RDStore.DOC_CTX_STATUS_DELETED]
        ).size()

        result
    }

    /**
     * Searches for a given document context if the file is missing and proposes an alternative based on database record matching
     */
    @Secured(['ROLE_ADMIN'])
    def recoveryDoc() {
        Map<String, Object> result = [:]

        result.filePath = ConfigMapper.getDocumentStorageLocation() ?: ConfigDefaults.DOCSTORE_LOCATION_FALLBACK

        Closure fileCheck = { Doc doc ->

            try {
                File test = new File("${result.filePath}/${doc.uuid}")
                if (test.exists() && test.isFile()) {
                    return true
                }
            }
            catch (Exception e) {
                return false
            }
        }

        Doc doc = Doc.get(Long.parseLong(params.docID))

        if (!fileCheck(doc)) {
            result.doc = doc

            List docs = Doc.findAllWhere(
                    status: doc.status,
                    type: doc.type,
                    content: doc.content,
                    contentType: doc.contentType,
                    title: doc.title,
                    filename: doc.filename,
                    mimeType: doc.mimeType,
                    migrated: doc.migrated,
                    owner: doc.owner
            )
            result.docsToRecovery = docs
        }
        result

    }

    /**
     * Restores the file by copying back a matched alternative
     */
    @Secured(['ROLE_ADMIN'])
    def processRecoveryDoc() {
        Map<String, Object> result = [:]

        result.filePath = ConfigMapper.getDocumentStorageLocation() ?: ConfigDefaults.DOCSTORE_LOCATION_FALLBACK

        Closure fileCheck = { Doc doc ->

            try {
                File test = new File("${result.filePath}/${doc.uuid}")
                if (test.exists() && test.isFile()) {
                    return true
                }
            }
            catch (Exception e) {
                return false
            }
        }

        Doc docWithoutFile = Doc.get(Long.parseLong(params.sourceDoc))
        Doc docWithFile = Doc.get(Long.parseLong(params.targetDoc))

        if (!fileCheck(docWithoutFile) && fileCheck(docWithFile)) {

            Path source = new File("${result.filePath}/${docWithFile.uuid}").toPath()
            Path target = new File("${result.filePath}/${docWithoutFile.uuid}").toPath()
            Files.copy(source, target)

            if(fileCheck(docWithoutFile)){
                flash.message = "Datei erfolgreich wiederhergestellt!"
            }else{
                flash.error = "Datei nicht erfolgreich wiederhergestellt!"
            }
        }else {
            flash.error = "Keine Quell-Datei gefunden um Wiederherzustellen!"
        }
        redirect(action:'recoveryDoc', params: ['docID': docWithoutFile.id])

    }

    @Deprecated
    @Secured(['ROLE_ADMIN'])
    def forceSendNotifications() {
        changeNotificationService.aggregateAndNotifyChanges()
        redirect(controller:'home')
    }

    /**
     * Lists all organisations (i.e. institutions, providers, agencies), their customer types, GASCO entry, legal information and API information
     */
    @Secured(['ROLE_ADMIN'])
    @Transactional
    def manageOrganisations() {
        Map<String, Object> result = [:]

        if (params.cmd == 'changeApiLevel') {
            Org target = (Org) genericOIDService.resolveOID(params.target)

            if (ApiToolkit.getAllApiLevels().contains(params.apiLevel)) {
                ApiToolkit.setApiLevel(target, params.apiLevel)
            }
            else if (params.apiLevel == 'Kein Zugriff') {
                ApiToolkit.removeApiLevel(target)
            }
            target.lastUpdated = new Date()
            target.save()
        }
        else if (params.cmd == 'deleteCustomerType') {
            Org target = (Org) genericOIDService.resolveOID(params.target)
            def oss = OrgSetting.get(target, OrgSetting.KEYS.CUSTOMER_TYPE)
            if (oss != OrgSetting.SETTING_NOT_FOUND) {
                oss.delete()
            }
            target.lastUpdated = new Date()
            target.save()
        }
        else if (params.cmd == 'changeCustomerType') {
            Org target = (Org) genericOIDService.resolveOID(params.target)
            Role customerType = Role.get(params.customerType)

            def osObj = OrgSetting.get(target, OrgSetting.KEYS.CUSTOMER_TYPE)

            if (osObj != OrgSetting.SETTING_NOT_FOUND) {
                OrgSetting oss = (OrgSetting) osObj
                oss.roleValue = customerType
                oss.save()

                params.remove('customerType') // unwanted parameter for filter query
            }
            else {
                OrgSetting.add(target, OrgSetting.KEYS.CUSTOMER_TYPE, customerType)
            }
            target.lastUpdated = new Date()
            target.save()
        }
        else if (params.cmd == 'changeGascoEntry') {
            Org target = (Org) genericOIDService.resolveOID(params.target)
            RefdataValue option = (RefdataValue) genericOIDService.resolveOID(params.gascoEntry)

            if (target && option) {
                def oss = OrgSetting.get(target, OrgSetting.KEYS.GASCO_ENTRY)

                if (oss != OrgSetting.SETTING_NOT_FOUND) {
                    oss.rdValue = option
                    oss.save()
                } else {
                    OrgSetting.add(target, OrgSetting.KEYS.GASCO_ENTRY, option)
                }
            }
            target.lastUpdated = new Date()
            target.save()
        }
        else if (params.cmd == 'changeLegalInformation') {
            Org target = (Org) genericOIDService.resolveOID(params.target)

            if (target) {
                target.createdBy = Org.get(params.createdBy)
                target.legallyObligedBy = Org.get(params.legallyObligedBy)
            }
            target.lastUpdated = new Date()
            target.save()
        }

        Map<String, Object> fsq = filterService.getOrgQuery(params)
        result.orgList = Org.executeQuery(fsq.query, fsq.queryParams, params)
        result.orgListTotal = result.orgList.size()

        result.allConsortia = Org.executeQuery(
                "select o from OrgSetting os join os.org o where os.key = 'CUSTOMER_TYPE' and os.roleValue.authority  = 'ORG_CONSORTIUM' order by o.sortname, o.name"
        )
        result
    }

    /**
     * Lists the identifier namespaces along with their attributes and usages. If a such command is being passed, a new identifier namespace is being created with the given
     * parameters. Note: identifier namespaces created by frontend do not persist database resets and are not present instance-wide. To hard-code identifier namespaces,
     * use {@link BootStrapService#setIdentifierNamespace()} instead
     */
    @Secured(['ROLE_ADMIN'])
    @Transactional
    def manageNamespaces() {
        IdentifierNamespace idnsInstance = new IdentifierNamespace(params)
        Map detailsStats = [:]

        switch (request.method) {
            case 'GET':
                idnsInstance = (IdentifierNamespace) genericOIDService.resolveOID(params.oid)

                if (params.cmd == 'deleteNamespace') {
                    if (idnsInstance && Identifier.countByNs(idnsInstance) == 0) {
                        try {
                            idnsInstance.delete()
                            flash.message = "Namensraum ${idnsInstance.ns} wurde gelöscht."
                        }
                        catch (Exception e) {
                            flash.message = "Namensraum ${idnsInstance.ns} konnte nicht gelöscht werden."
                        }
                    }
                }
                else if (params.cmd == 'details') {

                    if (idnsInstance) {
                        detailsStats.putAt(g.createLink(controller:'license', action:'show'),
                                IdentifierNamespace.executeQuery(
                                        "select i.value, i.lic.id from Identifier i join i.ns idns where idns = :idns and i.lic is not null order by i.value, i.lic.id", [idns: idnsInstance]))
                        detailsStats.putAt(g.createLink(controller:'org', action:'show'),
                                IdentifierNamespace.executeQuery(
                                        "select i.value, i.org.id from Identifier i join i.ns idns where idns = :idns and i.org is not null order by i.value, i.org.id", [idns: idnsInstance]))
                        detailsStats.putAt(g.createLink(controller:'package', action:'show'),
                                IdentifierNamespace.executeQuery(
                                        "select i.value, i.pkg.id from Identifier i join i.ns idns where idns = :idns and i.pkg is not null order by i.value, i.pkg.id", [idns: idnsInstance]))
                        detailsStats.putAt(g.createLink(controller:'subscription', action:'show'),
                                IdentifierNamespace.executeQuery(
                                        "select i.value, i.sub.id from Identifier i join i.ns idns where idns = :idns and i.sub is not null order by i.value, i.sub.id", [idns: idnsInstance]))
                        detailsStats.putAt(g.createLink(controller:'tipp', action:'show'),
                                IdentifierNamespace.executeQuery(
                                        "select i.value, i.tipp.id from Identifier i join i.ns idns where idns = :idns and i.tipp is not null order by i.value, i.tipp.id", [idns: idnsInstance]))
                    }
                }
                break

            case 'POST':
                idnsInstance.isFromLaser = true
                if (IdentifierNamespace.findByNsIlike(params.ns) || ! idnsInstance.save()) {

                    if(IdentifierNamespace.findByNsIlike(params.ns)) {
                        flash.error = message(code: 'identifier.namespace.exist', args:[params.ns]) as String
                        break
                    }
                    return
                }
                else {
                    flash.message = message(code: 'default.created.message', args: [message(code: 'identifier.namespace.label'), idnsInstance.ns]) as String
                }
                break
        }

        NativeQuery sqlQuery = (sessionFactory.currentSession).createSQLQuery("""
SELECT * FROM (
      SELECT idns.idns_ns,
             idns.idns_id,
             sum(CASE WHEN i.id_lic_fk is null THEN 0 ELSE 1 END)  lic,
             sum(CASE WHEN i.id_org_fk is null THEN 0 ELSE 1 END)  org,
             sum(CASE WHEN i.id_pkg_fk is null THEN 0 ELSE 1 END)  pkg,
             sum(CASE WHEN i.id_sub_fk is null THEN 0 ELSE 1 END)  sub,
             sum(CASE WHEN i.id_tipp_fk is null THEN 0 ELSE 1 END) tipp
      FROM identifier i
               JOIN identifier_namespace idns ON idns.idns_id = i.id_ns_fk
      GROUP BY idns.idns_ns, idns.idns_id
      order by idns.idns_ns
) sq WHERE (
    CASE WHEN sq.lic > 0 THEN 1 ELSE 0 END +
    CASE WHEN sq.org > 0 THEN 1 ELSE 0 END +
    CASE WHEN sq.pkg > 0 THEN 1 ELSE 0 END +
    CASE WHEN sq.sub > 0 THEN 1 ELSE 0 END +
    CASE WHEN sq.tipp > 0 THEN 1 ELSE 0 END
    ) > 1; """)

        List globalNamespaceStats = sqlQuery.with { list() }

        render view: 'manageNamespaces', model: [
                editable: true, // TODO check role and editable !!!
                cmd: params.cmd,
                identifierNamespaceInstance: idnsInstance,
                globalNamespaceStats: globalNamespaceStats,
                detailsStats: detailsStats,
                currentLang: LocaleUtils.getCurrentLang()
        ]
    }

    /**
     * Lists all public property definitions in the system. If a command is being supplied, the following actions
     * may be done:
     * <ul>
     *     <li>switch mandatory on/off</li>
     *     <li>switch multiple occurrence on/off</li>
     *     <li>delete a property definition</li>
     *     <li>replace a property definition by another</li>
     * </ul>
     * @return a list of property definitions with commands
     * @see de.laser.ajax.AjaxController#addCustomPropertyType()
     */
    @Secured(['ROLE_ADMIN'])
    @Transactional
    def managePropertyDefinitions() {

        if (params.cmd){
            PropertyDefinition pd = (PropertyDefinition) genericOIDService.resolveOID(params.pd)
            switch(params.cmd) {
                case 'toggleMandatory':
                    if(pd) {
                        pd.mandatory = !pd.mandatory
                        pd.save()
                    }
                    break
                case 'toggleMultipleOccurrence':
                    if(pd) {
                        pd.multipleOccurrence = !pd.multipleOccurrence
                        pd.save()
                    }
                    break
                case 'deletePropertyDefinition':
                    if (pd) {
                        if (! pd.isHardData) {
                            try {
                                pd.delete()
                                flash.message = message(code:'propertyDefinition.delete.success',[pd.name_de]) as String
                            }
                            catch(Exception e) {
                                flash.error = message(code:'propertyDefinition.delete.failure.default',[pd.name_de]) as String
                            }
                        }
                    }
                    break
                case 'replacePropertyDefinition':
                    if(params.xcgPdTo) {
                        PropertyDefinition pdFrom = (PropertyDefinition) genericOIDService.resolveOID(params.xcgPdFrom)
                        PropertyDefinition pdTo = (PropertyDefinition) genericOIDService.resolveOID(params.xcgPdTo)
                        String oldName = pdFrom.tenant ? "${pdFrom.getI10n("name")} (priv.)" : pdFrom.getI10n("name")
                        String newName = pdTo.tenant ? "${pdTo.getI10n("name")} (priv.)" : pdTo.getI10n("name")
                        if (pdFrom && pdTo) {
                            try {
                                int count = propertyService.replacePropertyDefinitions(pdFrom, pdTo, params.overwrite == 'on', true)
                                flash.message = message(code: 'menu.institutions.replace_prop.changed', args: [count, oldName, newName]) as String
                            }
                            catch (Exception e) {
                                e.printStackTrace()
                                flash.error = message(code: 'menu.institutions.replace_prop.error', args: [oldName, newName]) as String
                            }
                        }
                    }
                    break
            }
        }

        Map<String,Object> propDefs = [:]
        PropertyDefinition.AVAILABLE_CUSTOM_DESCR.each { String it ->
            Set<PropertyDefinition> itResult = PropertyDefinition.findAllByDescrAndTenant(it, null, [sort: 'name_de']) // NO private properties!
            propDefs.putAt( it, itResult )
        }

        def (usedPdList, attrMap, multiplePdList) = propertyService.getUsageDetails() // [List<Long>, Map<String, Object>, List<Long>]

        render view: 'managePropertyDefinitions', model: [
                editable    : true,
                propertyDefinitions: propDefs,
                attrMap     : attrMap,
                usedPdList  : usedPdList,
                multiplePdList : multiplePdList
        ]
    }

    /**
     * Copies a subscription property type into a survey property type
     */
    @Secured(['ROLE_ADMIN'])
    @Transactional
    def transferSubPropToSurProp() {

        PropertyDefinition propertyDefinition = PropertyDefinition.get(params.propertyDefinition)

        if(!PropertyDefinition.findByNameAndDescrAndTenant(propertyDefinition.name, PropertyDefinition.SVY_PROP, null)){
            PropertyDefinition surveyProperty = new PropertyDefinition(
                    name: propertyDefinition.name,
                    name_de: propertyDefinition.name_de,
                    name_en: propertyDefinition.name_en,
                    expl_de: propertyDefinition.expl_de,
                    expl_en: propertyDefinition.expl_en,
                    type: propertyDefinition.type,
                    refdataCategory: propertyDefinition.refdataCategory,
                    descr: PropertyDefinition.SVY_PROP
            )

            if (surveyProperty.save()) {
                flash.message = message(code: 'propertyDefinition.copySubPropToSurProp.created.sucess') as String
            }
            else {
                flash.error = message(code: 'propertyDefinition.copySubPropToSurProp.created.fail') as String
            }
        }

        redirect(action: 'managePropertyDefinitions')
    }

    @Deprecated
    @Secured(['ROLE_ADMIN'])
    def managePropertyGroups() {
        Map<String, Object> result = [:]
        result.editable = true // true, because action is protected

        if (params.cmd == 'new') {
            result.formUrl = g.createLink([controller: 'admin', action: 'managePropertyGroups'])
            render template: '/templates/properties/propertyGroupModal', model: result
            return
        }
        else if (params.cmd == 'edit') {
            result.pdGroup = genericOIDService.resolveOID(params.oid)
            result.formUrl = g.createLink([controller: 'admin', action: 'managePropertyGroups'])

            render template: '/templates/properties/propertyGroupModal', model: result
            return
        }
        else if (params.cmd == 'delete') {
            def pdg = genericOIDService.resolveOID(params.oid)
            try {
                pdg.delete()
                flash.message = "Die Gruppe ${pdg.name} wurde gelöscht."
            }
            catch (e) {
                flash.error = "Die Gruppe ${params.oid} konnte nicht gelöscht werden."
            }
        }
        else if (params.cmd == 'processing') {
            def valid
            def propDefGroup
            String ownerType = PropertyDefinition.getDescrClass(params.prop_descr)

            if (params.oid) {
                propDefGroup = genericOIDService.resolveOID(params.oid)
                propDefGroup.name = params.name ?: propDefGroup.name
                propDefGroup.description = params.description
                propDefGroup.ownerType = ownerType

                if (propDefGroup.save()) {
                    valid = true
                }
            }
            else {
                if (params.name && ownerType) {
                    propDefGroup = new PropertyDefinitionGroup(
                            name: params.name,
                            description: params.description,
                            tenant: null,
                            ownerType: ownerType,
                            isVisible: true
                    )
                    if (propDefGroup.save()) {
                        valid = true
                    }
                }
            }

            if (valid) {
                PropertyDefinitionGroupItem.executeUpdate(
                        "DELETE PropertyDefinitionGroupItem pdgi WHERE pdgi.propDefGroup = :pdg",
                        [pdg: propDefGroup]
                )

                params.list('propertyDefinition')?.each { pd ->

                    new PropertyDefinitionGroupItem(
                            propDef: pd,
                            propDefGroup: propDefGroup
                    ).save()
                }
            }
        }

        result.propDefGroups = PropertyDefinitionGroup.findAllWhere(
                tenant: null
        )
        result
    }

    /**
     * Lists all reference data values, grouped by their categories
     */
    @Secured(['ROLE_ADMIN'])
    @Transactional
    def manageRefdatas() {

        if (params.cmd == 'deleteRefdataValue') {
            RefdataValue rdv = (RefdataValue) genericOIDService.resolveOID(params.rdv)

            if (rdv) {
                if (! rdv.isHardData) {
                    try {
                        rdv.delete()
                        flash.message = "${params.rdv} wurde gelöscht."
                    }
                    catch(Exception e) {
                        flash.error = "${params.rdv} konnte nicht gelöscht werden."
                    }
                }
            }
        }
        else if (params.cmd == 'replaceRefdataValue') {
            if (SpringSecurityUtils.ifAnyGranted('ROLE_YODA')) {
                RefdataValue rdvFrom = (RefdataValue) genericOIDService.resolveOID(params.xcgRdvFrom)
                RefdataValue rdvTo = (RefdataValue) genericOIDService.resolveOID(params.xcgRdvTo)

                boolean check = false

                if (! rdvFrom) {
                    check = false
                }
                else if (rdvTo && rdvTo.owner == rdvFrom.owner) {
                    check = true
                }
//                else if (! rdvTo && params.xcgRdvGlobalTo) {
//
//                    List<String> pParts = params.xcgRdvGlobalTo.split(':')
//                    if (pParts.size() == 2) {
//                        RefdataCategory rdvToCat = RefdataCategory.getByDesc(pParts[0].trim())
//                        RefdataValue rdvToRdv = RefdataValue.getByValueAndCategory(pParts[1].trim(), pParts[0].trim())
//
//                        if (rdvToRdv && rdvToRdv.owner == rdvToCat ) {
//                            rdvTo = rdvToRdv
//                            check = true
//                        }
//                    }
//                }

                if (check) {
                    try {
                        int count = refdataService.replaceRefdataValues(rdvFrom, rdvTo)

                        flash.message = "${count} Vorkommen von ${params.xcgRdvFrom} wurden durch ${params.xcgRdvTo} ersetzt."
                    }
                    catch (Exception e) {
                        log.error( e.toString() )
                        flash.error = "${params.xcgRdvFrom} konnte nicht durch ${params.xcgRdvTo} ersetzt werden."
                    }

                }
            }
            else {
                flash.error = "Keine ausreichenden Rechte!"
            }
        }

        def (usedRdvList, attrMap) = refdataService.getUsageDetails()

        Map integrityCheckResult = refdataService.integrityCheck()

        render view: 'manageRefdatas', model: [
                editable    : true,
                rdCategories: RefdataCategory.where{}.sort('desc_' + LocaleUtils.getCurrentLang()),
                attrMap     : attrMap,
                usedRdvList : usedRdvList,
                integrityCheckResult : integrityCheckResult
        ]
    }

    @Deprecated
    @Secured(['ROLE_ADMIN'])
    @Transactional
    def titleEnrichment() {
        Map<String, Object> result = [:]

        if(params.kbartPreselect) {
            CommonsMultipartFile kbartFile = params.kbartPreselect
            Integer count = 0
            Integer countChanges = 0
            InputStream stream = kbartFile.getInputStream()
            ArrayList<String> rows = stream.text.split('\n')
            Map<String,Integer> colMap = [publicationTitleCol:-1,zdbCol:-1, onlineIdentifierCol:-1, printIdentifierCol:-1, doiTitleCol:-1, seriesTitleCol:-1]
            //read off first line of KBART file
            rows[0].split('\t').eachWithIndex { headerCol, int c ->
                switch(headerCol.toLowerCase().trim()) {
                    case "zdb_id": colMap.zdbCol = c
                        break
                    case "print_identifier": colMap.printIdentifierCol = c
                        break
                    case "online_identifier": colMap.onlineIdentifierCol = c
                        break
                    case "publication_title": colMap.publicationTitleCol = c
                        break
                    case "series_name": colMap.seriesNameTitleCol = c
                        break
                    case "monograph_parent_collection_title": colMap.seriesNameTitleCol = c
                        break
                    case "subject_reference": colMap.subjectReferenceTitleCol = c
                        break
                    case "summary_of_content": colMap.summaryOfContentTitleCol = c
                        break
                    case "doi_identifier": colMap.doiTitleCol = c
                        break
                }
            }
            //after having read off the header row, pop the first row
            rows.remove(0)
            //now, assemble the identifiers available to highlight
            Map<String,IdentifierNamespace> namespaces = [zdb:IdentifierNamespace.findByNs('zdb'),
                                                          eissn:IdentifierNamespace.findByNs('eissn'),isbn:IdentifierNamespace.findByNs('isbn'),
                                                          issn:IdentifierNamespace.findByNs('issn'),pisbn:IdentifierNamespace.findByNs('pisbn'),
                                                          doi: IdentifierNamespace.findByNs('doi')]
            rows.eachWithIndex { row, int i ->
                log.debug("now processing entitlement ${i}")
                ArrayList<String> cols = row.split('\t')
                Map idCandidate
                if(colMap.zdbCol >= 0 && cols[colMap.zdbCol]) {
                    idCandidate = [namespaces:[namespaces.zdb],value:cols[colMap.zdbCol]]
                }
                if(colMap.onlineIdentifierCol >= 0 && cols[colMap.onlineIdentifierCol]) {
                    idCandidate = [namespaces:[namespaces.eissn,namespaces.isbn],value:cols[colMap.onlineIdentifierCol]]
                }
                if(colMap.printIdentifierCol >= 0 && cols[colMap.printIdentifierCol]) {
                    idCandidate = [namespaces:[namespaces.issn,namespaces.pisbn],value:cols[colMap.printIdentifierCol]]
                }
                if(colMap.doiTitleCol >= 0 && cols[colMap.doiTitleCol]) {
                    idCandidate = [namespaces:[namespaces.doi],value:cols[colMap.doiTitleCol]]
                }
                if(((colMap.zdbCol >= 0 && cols[colMap.zdbCol].trim().isEmpty()) || colMap.zdbCol < 0) &&
                        ((colMap.onlineIdentifierCol >= 0 && cols[colMap.onlineIdentifierCol].trim().isEmpty()) || colMap.onlineIdentifierCol < 0) &&
                        ((colMap.printIdentifierCol >= 0 && cols[colMap.printIdentifierCol].trim().isEmpty()) || colMap.printIdentifierCol < 0)) {
                }
                else {
                    List<TitleInstance> tiObj = TitleInstance.executeQuery('select ti from TitleInstance ti join ti.ids ident where ident.ns in :namespaces and ident.value = :value', [namespaces:idCandidate.namespaces, value:idCandidate.value])
                    if(tiObj) {

                        tiObj.each { titleInstance ->
                            count++
                            if(titleInstance instanceof BookInstance) {
                                if(colMap.summaryOfContentTitleCol && (cols[colMap.summaryOfContentTitleCol] != null || cols[colMap.summaryOfContentTitleCol] != "") && (cols[colMap.summaryOfContentTitleCol].trim() != titleInstance.summaryOfContent) ){
                                    countChanges++
                                    titleInstance.summaryOfContent = cols[colMap.summaryOfContentTitleCol].trim()
                                    titleInstance.save()
                                }
                            }

                                if(colMap.seriesNameTitleCol && (cols[colMap.seriesNameTitleCol] != null || cols[colMap.seriesNameTitleCol] != "") && (cols[colMap.seriesNameTitleCol].trim() != titleInstance.seriesName) ){
                                    countChanges++
                                    titleInstance.seriesName = cols[colMap.seriesNameTitleCol].trim()
                                    titleInstance.save()
                                }

                                if(colMap.subjectReferenceTitleCol && (cols[colMap.subjectReferenceTitleCol] != null || cols[colMap.subjectReferenceTitleCol] != "") && (cols[colMap.subjectReferenceTitleCol].trim() != titleInstance.subjectReference) ){
                                    countChanges++
                                    titleInstance.subjectReference = cols[colMap.subjectReferenceTitleCol].trim()
                                    titleInstance.save()
                                }
                        }
                    }
                }
            }

            flash.message = "Verbearbeitet: ${count} /Geändert ${countChanges}"
            params.remove("kbartPreselct")
        }

    }

    /**
     * Lists all system messages in the system
     * @see SystemMessage
     */
    @Secured(['ROLE_ADMIN'])
    @Transactional
    def systemMessages() {

        Map<String, Object> result = [:]
        result.user = contextService.getUser()

        if (params.create){
            SystemMessage sm = new SystemMessage(
                    content_de: params.content_de ?: '',
                    content_en: params.content_en ?: '',
                    type: params.type,
                    isActive: false)

            if (sm.save()){
                flash.message = 'Systemmeldung erstellt'
            } else {
                flash.error = 'Systemmeldung wurde nicht erstellt'
            }
        }

        result.systemMessages = SystemMessage.executeQuery('select sm from SystemMessage sm order by sm.isActive desc, sm.lastUpdated desc')
        result.editable = true
        result
    }

    /**
     * Deletes the given {@link SystemMessage}
     * @param id the system message to delete
     */
    @Secured(['ROLE_ADMIN'])
    @Transactional
    def deleteSystemMessage(Long id) {

        if (SystemMessage.get(id)){
            SystemMessage.get(id).delete()
            flash.message = 'Systemmeldung wurde gelöscht'
        }

        redirect(action: 'systemMessages')
    }

    @Secured(['ROLE_ADMIN'])
    def appInfo() {
        Map<String, Object> result = [
                docStore: AppUtils.getDocumentStorageInfo()
        ]

        result.globalSourceSync = [
                running: globalSourceSyncService.running
                ]
        result.dataload = [
                running: dataloadService.update_running,
                lastFTIndexUpdateInfo: dataloadService.getLastFTIndexUpdateInfo()
        ]
        result.statsSync = [
                running: statsSyncService.running,
                submitCount: statsSyncService.submitCount,
                completedCount: statsSyncService.completedCount,
                newFactCount: statsSyncService.newFactCount,
                totalTime: statsSyncService.totalTime,
                threads: statsSyncService.THREAD_POOL_SIZE,
                queryTime: statsSyncService.queryTime,
                activityHistogram: statsSyncService.activityHistogram,
                syncStartTime: statsSyncService.syncStartTime,
                syncElapsed: statsSyncService.syncElapsed
                ]
        result.ftcInfos = FTControl.list()

        List dbmQuery = (sessionFactory.currentSession.createSQLQuery(
                'SELECT filename, id, dateexecuted from databasechangelog order by orderexecuted desc limit 1'
        )).list()

        result.dbInfo = [
                dbmVersion : dbmQuery.size() > 0 ? dbmQuery.first() : ['unkown', 'unkown', 'unkown'],
                dbmUpdateOnStart : ConfigMapper.getPluginConfig('databasemigration.updateOnStart', Boolean),

                default: [
                        dbName           : ConfigMapper.getConfig('dataSource.url', String).split('/').last(),
                        dbmDbCreate      : ConfigMapper.getConfig('dataSource.dbCreate', String),
                        defaultCollate   : DatabaseInfo.getDatabaseCollate(),
                ],
                storage: [
                        dbName           : ConfigMapper.getConfig('dataSources.storage.url', String).split('/').last(), // TODO
                        dbmDbCreate      : ConfigMapper.getConfig('dataSources.storage.dbCreate', String), // TODO
                        defaultCollate   : DatabaseInfo.getDatabaseCollate( DatabaseInfo.DATASOURCE_STORAGE ),
                ]
        ]

        result
    }
}
