package de.laser

import de.laser.cache.EhcacheWrapper
import de.laser.config.ConfigDefaults
import de.laser.finance.CostInformationDefinition
import de.laser.mail.MailTemplate
import de.laser.storage.BeanStore
import de.laser.storage.RDConstants
import de.laser.system.SystemActivityProfiler
import de.laser.system.SystemProfiler
import de.laser.utils.AppUtils
import de.laser.helper.DatabaseInfo
import de.laser.utils.CodeUtils
import de.laser.utils.DateUtils
import de.laser.utils.FileUtils
import de.laser.utils.LocaleUtils
import de.laser.utils.SwissKnife
import de.laser.remote.FTControl
import de.laser.auth.Role
import de.laser.properties.PropertyDefinition
import de.laser.api.v0.ApiToolkit

import de.laser.storage.RDStore
import de.laser.system.ServiceMessage
import de.laser.system.SystemEvent
import de.laser.system.SystemMessage
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import grails.plugins.mail.MailService
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.sql.Sql
import org.grails.web.json.JSONElement
import org.hibernate.SessionFactory
import org.hibernate.query.NativeQuery
import de.laser.config.ConfigMapper

import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.sql.Timestamp
import java.time.LocalDate

/**
 * This controller contains methods which are at least ROLE_ADMIN secured. Those are among the
 * dangerous calls the less dangerous ones. The really dangerous methods are located in the
 * {@link YodaController}; those are bulk operations which are - once done - irreversible
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class AdminController  {

    ContextService contextService
    DataConsistencyService dataConsistencyService
    DataloadService dataloadService
    DeletionService deletionService
    FileCryptService fileCryptService
    FilterService filterService
    GenericOIDService genericOIDService
    GlobalSourceSyncService globalSourceSyncService
    MailService mailService
    PackageService packageService
    PropertyService propertyService
    ProviderService providerService
    RefdataService refdataService
    SessionFactory sessionFactory
    StatsSyncService statsSyncService
    VendorService vendorService

    /**
     * Empty call, loads empty admin dashboard
     */
    @Secured(['ROLE_ADMIN'])
    @Transactional
    def index() {

        Map<String, Object> result = [
            database: [
                default: [
                    dbName     : ConfigMapper.getConfig(ConfigDefaults.DATASOURCE_DEFAULT + '.url', String).split('/').last(),
                    dbmVersion : DatabaseInfo.getDbmVersion()
                ],
                storage: [
                    dbName     : ConfigMapper.getConfig(ConfigDefaults.DATASOURCE_STORAGE + '.url', String).split('/').last(),
                    dbmVersion : DatabaseInfo.getDbmVersion( DatabaseInfo.DS_STORAGE )
                ]
            ],
            events      : SystemEvent.executeQuery(
                    'select se from SystemEvent se where se.created >= :limit and se.relevance in (:rList) order by se.created desc',
                    [
                        limit: DateUtils.localDateToSqlDate(LocalDate.now().minusDays(1)),
                        rList: [SystemEvent.RELEVANCE.ERROR, SystemEvent.RELEVANCE.WARNING]
                    ]
            ),
            docStore    : AppUtils.getDocumentStorageInfo()
        ]
        result
    }

    /**
     * This method manages system-wide messages. Those are made if system-relevant messages need to be transmit to every
     * registered account like maintenance shutdowns or general info which may concern every account, too,
     * like training course availabilities or new releases of the software. Those messages can be sent via mail as well and it is
     * possible to retire a message from publishing; if a service message is being published, it will be displayed on the landing
     * page of the webapp
     * @return a view containing the all service messages
     */
    @Secured(['ROLE_ADMIN'])
    @Transactional
    def serviceMessages() {
        Map<String, Object> result = [:]

        result.mailDisabled = ConfigMapper.getConfig('grails.mail.disabled', Boolean)

        if (params.id) {
            ServiceMessage msg = ServiceMessage.get(params.long('id'))

            if (msg) {
                if (params.cmd == 'edit') {
                    result.currentServiceMessage = msg
                }
                else if (params.cmd == 'publish') {
                    if (result.mailDisabled) {
                        flash.error = message(code: 'system.config.mail.disabled') as String
                    }
                    else if (msg.publish()) {
                        flash.message = message(code: 'serviceMessage.published') as String
                    }
                    else {
                        flash.error = message(code: 'serviceMessage.published_error') as String
                    }
                }
                else if (params.cmd == 'undo') {
                    msg.isPublished = false
                    if (msg.save()) {
                        flash.message = message(code: 'serviceMessage.undo') as String
                    }
                    else {
                        flash.error = message(code: 'serviceMessage.undo_error') as String
                    }
                }
                else if (params.cmd == 'delete') {
                    try {
                        msg.delete()
                        flash.message = message(code: 'default.success') as String
                    }
                    catch (Exception e) {
                        log.error(e.getMessage())
                        flash.error = message(code: 'default.delete.error.general.message') as String
                    }
                }
            }
        }
        result.numberOfCurrentRecipients = ServiceMessage.getRecipients().size()
        result.serviceMessages = ServiceMessage.list(sort: 'lastUpdated', order: 'desc')
        result
    }

    /**
     * Call to test mail sending
     */
    @Secured(['ROLE_ADMIN'])
    @Transactional
    def sendMail() {
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
     * This controller processes data and creates a new service message; if an ID is being provided,
     * a record matching the ID will be retrieved and if successful, the data is being updated
     */
    @Secured(['ROLE_ADMIN'])
    @Transactional
    def createServiceMessage() {
        if (params.saTitle && params.saContent) {
            ServiceMessage msg
            boolean isNew = false

            if (params.saId) {
                msg = ServiceMessage.get(params.long('saId'))
            }
            if (!msg) {
                msg = new ServiceMessage()
                isNew = true
            }

            msg.title = params.saTitle
            msg.content = params.saContent
            msg.user = contextService.getUser()
            msg.isPublished = false

            if (msg.save()) {
                flash.message = isNew ? message(code: 'serviceMessage.created') : message(code: 'serviceMessage.updated') as String
            }
            else {
                flash.error = message(code: 'default.save.error.message', args: [msg]) as String
            }
        }
        else {
            flash.error = message(code: 'default.error') as String
        }
        redirect(action: 'serviceMessages')
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
//                'Org',
                ['Package', 'packageStatus'],
                'Platform',
                'Subscription',
                'TitleInstancePackagePlatform',
                'Combo'
                //'Doc'
        ]
        result.jobList = jobList

        jobList.each { job ->
            if (job instanceof String) {
                log.info('processing: ' + job)
                String query = "select count(*) from ${job} obj join obj.status s where lower(s.value) like 'deleted'"
                result.stats."${job}" = Org.executeQuery( query )
            }
            else {
                log.info('processing: ' + job[0])
                String query = "select count(*) from ${job[0]} obj join obj.${job[1]} s where lower(s.value) like 'deleted'"
                result.stats."${job[0]}" = Org.executeQuery( query )
            }
        }
        result
    }

    /**
     * Shows recorded system events; default maximum age is 14 days
     * The record listing may be filtered
     * @see SystemEvent
     */
    @Secured(['ROLE_ADMIN'])
    def systemEvents() {
        Map<String, Object> result = [:]

        result.filter_limit = params.long('filter_limit') ?: 14
        Date limit = DateUtils.localDateToSqlDate( LocalDate.now().minusDays(result.filter_limit as long) )

        if (params.filter_category) { result.put('filter_category', params.filter_category) }
        if (params.filter_relevance){ result.put('filter_relevance', params.filter_relevance) }
        if (params.filter_source)   { result.put('filter_source', params.filter_source) }
        if (params.filter_exclude)  { result.put('filter_exclude', params.filter_exclude) }
        if (params.filter_limit)    { result.put('filter_limit', params.filter_limit) }

        result.events = SystemEvent.executeQuery('select se from SystemEvent se where se.created >= :limit order by se.created desc', [limit: limit])
        result
    }

    /**
     * Shows recorded access warnings of the last 30 days, if not defined otherwise.
     * The result contains additional data about the attempted access
     * @see SystemEvent
     */
    @Secured(['ROLE_ADMIN'])
    def systemEventsX() {
        Map<String, Object> result = [:]

        result.limit = params.long('limit') ?: 30
        Date limit = DateUtils.localDateToSqlDate( LocalDate.now().minusDays(result.limit as long) )

        result.events = SystemEvent.executeQuery(
                'select se from SystemEvent se where se.created >= :limit and se.token = :token order by se.created desc',
                [token: 'LOGIN_WARNING', limit: limit]
        )

        result.data = []
        result.events.each { it ->
            JSONElement je = new JSON().parse(it.payload) as JSONElement
            result.data << [
                    id: it.id,
                    created: it.created,
                    host: je.headers[0],
                    url: je.url,
                    remote: je.remote,
                    useragent: je.headers[1] ?: '',
                    x_date: DateUtils.getLocalizedSDF_noTime().format(it.created),
                    x_time: DateUtils.getSDF_onlyTime().format(it.created)
            ]
        }
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
        String query4 = "select tipp_name from title_instance_package_platform"

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
                        'de_DE.UTF-8' : sql.rows( query4 + ' order by tipp_name COLLATE "de_DE" limit ' + limit ).collect{ it.tipp_name },
                        'en_US.UTF-8' : sql.rows( query4 + ' order by tipp_name COLLATE "en_US" limit ' + limit ).collect{ it.tipp_name },
                        'current_de'  : RefdataValue.executeQuery( "select name from TitleInstancePackagePlatform order by name", [max: limit] ),
                        'current_en'  : RefdataValue.executeQuery( "select name from TitleInstancePackagePlatform order by name", [max: limit] )
                ]
        ]

        String de_x_icu = DatabaseInfo.DE_U_CO_PHONEBK_X_ICU
        result.examples['country'][de_x_icu] = sql.rows(query1de + ' order by rdv.rdv_value_de COLLATE "' + de_x_icu + '" limit ' + limit).collect { it.rdv_value_de }
        result.examples['ddc'    ][de_x_icu] = sql.rows(query2de + ' order by rdv.rdv_value_de COLLATE "' + de_x_icu + '" limit ' + limit).collect { it.rdv_value_de }
        result.examples['org'    ][de_x_icu] = sql.rows(query3 + ' order by org_name COLLATE "' + de_x_icu + '" limit ' + limit).collect { it.org_name }
        result.examples['title'  ][de_x_icu] = sql.rows(query4 + ' order by tipp_name COLLATE "' + de_x_icu + '" limit ' + limit).collect { it.tipp_name }

        String en_x_icu = DatabaseInfo.EN_US_U_VA_POSIX_X_ICU
        result.examples['country'][en_x_icu] = sql.rows(query1en + ' order by rdv.rdv_value_en COLLATE "' + en_x_icu + '" limit ' + limit).collect { it.rdv_value_en }
        result.examples['ddc'    ][en_x_icu] = sql.rows(query2en + ' order by rdv.rdv_value_en COLLATE "' + en_x_icu + '" limit ' + limit).collect { it.rdv_value_en }
        result.examples['org'    ][en_x_icu] = sql.rows(query3 + ' order by org_name COLLATE "' + en_x_icu + '" limit ' + limit).collect { it.org_name }
        result.examples['title'  ][en_x_icu] = sql.rows(query4 + ' order by tipp_name COLLATE "' + en_x_icu + '" limit ' + limit).collect { it.tipp_name }

        result
    }

    /**
     * Delivers current information about the executed database changesets
     */
    @Secured(['ROLE_ADMIN'])
    def databaseInfo() {

        Map<String, Object> result = [
            dbmUpdateOnStart : ConfigMapper.getPluginConfig('databasemigration.updateOnStart', Boolean),

            default: [
                    dbName           : ConfigMapper.getConfig(ConfigDefaults.DATASOURCE_DEFAULT + '.url', String).split('/').last(),
                    dbmDbCreate      : ConfigMapper.getConfig(ConfigDefaults.DATASOURCE_DEFAULT + '.dbCreate', String),
                    defaultCollate   : DatabaseInfo.getDatabaseCollate(),
                    dbConflicts      : DatabaseInfo.getDatabaseConflicts(),
                    dbMaxConnections : DatabaseInfo.getMaxConnections(),
                    dbStmtTimeout    : DatabaseInfo.getStatementTimeout(),
                    dbSize           : DatabaseInfo.getDatabaseSize(),
                    dbStatistics     : DatabaseInfo.getDatabaseStatistics(),
                    dbActivity       : DatabaseInfo.getDatabaseActivity(),
                    dbUserFunctions  : DatabaseInfo.getDatabaseUserFunctions(),
                    dbTableUsage     : DatabaseInfo.getAllTablesUsageInfo(),
                    dbmVersion       : DatabaseInfo.getDbmVersion()
            ],
            storage: [
                    dbName           : ConfigMapper.getConfig(ConfigDefaults.DATASOURCE_STORAGE + '.url', String).split('/').last(), // TODO
                    dbmDbCreate      : ConfigMapper.getConfig(ConfigDefaults.DATASOURCE_STORAGE + '.dbCreate', String), // TODO
                    defaultCollate   : DatabaseInfo.getDatabaseCollate( DatabaseInfo.DS_STORAGE ),
                    dbConflicts      : DatabaseInfo.getDatabaseConflicts( DatabaseInfo.DS_STORAGE ),
                    dbMaxConnections : DatabaseInfo.getMaxConnections( DatabaseInfo.DS_STORAGE ),
                    dbStmtTimeout    : DatabaseInfo.getStatementTimeout( DatabaseInfo.DS_STORAGE ),
                    dbSize           : DatabaseInfo.getDatabaseSize( DatabaseInfo.DS_STORAGE ),
                    dbStatistics     : DatabaseInfo.getDatabaseStatistics( DatabaseInfo.DS_STORAGE ),
                    dbActivity       : DatabaseInfo.getDatabaseActivity( DatabaseInfo.DS_STORAGE ),
                    dbUserFunctions  : DatabaseInfo.getDatabaseUserFunctions( DatabaseInfo.DS_STORAGE ),
                    dbTableUsage     : DatabaseInfo.getAllTablesUsageInfo( DatabaseInfo.DS_STORAGE ),
                    dbmVersion       : DatabaseInfo.getDbmVersion( DatabaseInfo.DS_STORAGE )
            ]
        ]

        [dbInfo: result]
    }

    @Secured(['ROLE_ADMIN'])
    def databaseIndices() {

        Map<String, Object> result = [
                indices: DatabaseInfo.getAllTablesWithGORMIndices(),
                counts: [:]
                ]

        CodeUtils.getAllDomainClasses().each { cls ->
            String css = cls.getName()
            try {
                result.counts.putAt(css, Org.executeQuery('select count(*) from ' + cls.simpleName)[0])
            } catch (Exception e) {
                result.counts.putAt(css, '?')
            }
        }
        result
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

        result.duplicates = dataConsistencyService.checkDuplicates()

        result
    }

    /**
     * Runs a check through all {@link Identifier}s whose namespaces have a validation regex defined and checks whether the values match the defined validation patterns
     * @return a {@link Map} containing for each concerned {@link IdentifierNamespace} the
     * <ul>
     *     <li>total count of identifiers</li>
     *     <li>count of valid identifiers</li>
     *     <li>count of invalid identifiers</li>
     * </ul>
     */
    @Secured(['ROLE_ADMIN'])
    def identifierValidation() {
        Map<String, Object> result = [
                nsList          : IdentifierNamespace.executeQuery('select ns from IdentifierNamespace ns where ns.validationRegex is not null order by ns.ns, ns.nsType'),
                iMap            : [:],
                currentLang     : LocaleUtils.getCurrentLang()
        ]

        result.nsList.each { ns ->
                List<Identifier> iList = Identifier.executeQuery('select i from Identifier i where i.ns = :ns', [ns: ns])
                result.iMap[ns.id] = [
                        count   : iList.size(),
                        valid   : [],
                        invalid : []
                ]

                iList.each { obj ->
                    def pattern = ~/${ns.validationRegex}/
                    if (pattern.matcher(obj.value).matches()) {
                        result.iMap[ns.id].valid << obj
                    }
                    else {
                        result.iMap[ns.id].invalid << obj
                    }
                }
        }
        result
    }

    @Secured(['ROLE_ADMIN'])
    def simpleShareConfCheck() {
        log.debug('simpleShareConfCheck')
    }

    @Secured(['ROLE_ADMIN'])
    def simpleDocsCheck() {
        log.debug('simpleDocsCheck')
        String dsl = ConfigMapper.getDocumentStorageLocation() ?: ConfigDefaults.DOCSTORE_LOCATION_FALLBACK

        Map<String, Object> result = [
                dsPath          : dsl,
                orphanedDocs    : []
        ]

        result.orphanedDocs = Doc.executeQuery(
                'select doc from Doc doc where doc.contentType = :ctf' +
                        ' and not exists (select dc.id from DocContext dc where dc.owner = doc) order by doc.id',
                [ctf: Doc.CONTENT_TYPE_FILE]
        )

        if (params.deleteOrphanedDocs) {
            List<Long> deletedDocs = []
            result.orphanedDocs.each { doc ->
                try {
                    Long did = doc.id
                    doc.delete()
                    deletedDocs << did
                }
                catch (Exception e) {
                    log.error 'Error: ' + e.getMessage()
                }
            }
            log.info 'removed orphaned docs: ' + deletedDocs.size()
            SystemEvent.createEvent('DOCSTORE_DEL_ORPHANED_DOCS', [
                    server: AppUtils.getCurrentServer(),
                    count: deletedDocs.size(),
                    docs: deletedDocs
            ])

            redirect controller: 'admin', action: 'simpleDocsCheck'
            return
        }

        result
    }

    @Secured(['ROLE_ADMIN'])
    def simpleFilesCheck() {
        log.debug('simpleFilesCheck')
        String dsl = ConfigMapper.getDocumentStorageLocation() ?: ConfigDefaults.DOCSTORE_LOCATION_FALLBACK

        Map<String, Object> result = [
           dsPath       : dsl,
           dsFiles      : [],
           xxPath       : dsl + '_outdated',
           xxFiles      : [],
           validDocs    : [],
           validFiles       : [],
           validFilesRaw    : [],
           invalidFiles     : []
        ]

        File ds = new File("${result.dsPath}")
        File xx = new File("${result.xxPath}")
        if (!xx.exists()) {
            xx.mkdirs()
        }

        try {
            if (ds.exists()) {
                result.dsFiles = ds.listFiles().collect{it.getName()}

                result.validDocs = Doc.executeQuery(
                        'select doc from Doc doc where doc.contentType = :ctf and doc.uuid in (:files)',
                        [ctf: Doc.CONTENT_TYPE_FILE, files: result.dsFiles]
                )
                Set<String> docs    = result.validDocs.findAll{   it.ckey }.collect{ it.uuid as String }
                Set<String> docsRaw = result.validDocs.findAll{ ! it.ckey }.collect{ it.uuid as String }

                result.dsFiles.each { fn ->
                    if      (docs.contains(fn))     { result.validFiles << fn }
                    else if (docsRaw.contains(fn))  { result.validFilesRaw << fn }
                    else                            { result.invalidFiles << fn }
                }
                result.validFiles.toSorted()
                result.validFilesRaw.toSorted()
                result.invalidFiles.toSorted()
            }

            if (xx.exists()) {
                result.xxFiles = xx.listFiles().collect { it.getName() }
            }
        }
        catch (Exception e) {
            log.error 'Error: ' + e.getMessage()
        }

        if (params.encryptRawFiles) {
            List<String> encryptedFiles = []
            List<String> ignored = []

            result.validFilesRaw.take(250).each { uuid ->
                try {
                    File raw = new File("${result.dsPath}/${uuid}")
                    Doc doc  = Doc.findByUuidAndContentType(uuid, Doc.CONTENT_TYPE_FILE)
                    if (raw && doc && !doc.ckey) {
                        fileCryptService.encryptRawFileAndUpdateDoc(raw, doc)
                        encryptedFiles << uuid
                    }
                    else {
                        ignored << uuid
                    }
                }
                catch (Exception e) {
                    log.error 'Error: ' + e.getMessage()
                }
            }
            log.info 'encrypted raw files: ' + encryptedFiles.size() + ', path:' + result.dsPath
            SystemEvent.createEvent('DOCSTORE_ENC_RAW_FILES', [
                    server: AppUtils.getCurrentServer(),
                    count: encryptedFiles.size(),
                    files: encryptedFiles,
                    ignored: ignored
            ])

            redirect controller: 'admin', action: 'simpleFilesCheck'
            return
        }

        if (params.moveOutdatedFiles) {
            List<String> movedFiles = []
            String pk = DateUtils.getSDF_yyyyMMdd().format(new Date())

            result.invalidFiles.take(1000).each { uuid ->
                try {
                    String pkid = uuid + '-' + pk
                    File src = new File("${result.dsPath}/${uuid}")
                    File dst = new File("${result.xxPath}/${pkid}")
                    if (src.exists() && src.isFile()) {
                        Files.move(src.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING)
                        movedFiles << pkid
                    }
                }
                catch (Exception e) {
                    log.error 'Error: ' + e.getMessage()
                }
            }
            log.info 'moved outdated files: ' + movedFiles.size() + ', pk: ' + pk + ', path:' + result.xxPath
            SystemEvent.createEvent('DOCSTORE_MOV_OUTDATED_FILES', [
                    server: AppUtils.getCurrentServer(),
                    count: movedFiles.size(),
                    files: movedFiles
            ])

            redirect controller: 'admin', action: 'simpleFilesCheck'
            return
        }

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
        log.debug('fileConsistency - start')

        Map<String, Object> result = [
            listOfDocsInUse: [],
            listOfDocsInUseOrphaned: [],

            listOfDocsNotInUse : [],
            listOfDocsNotInUseOrphaned: [],

            listOfFiles: [],
            listOfFilesMatchingDocs: [],
            listOfFilesOrphaned: []
        ]

        result.filePath = ConfigMapper.getDocumentStorageLocation() ?: ConfigDefaults.DOCSTORE_LOCATION_FALLBACK

        // files

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

        result.listOfDocsInUse = Doc.executeQuery(
                'select distinct(doc) from DocContext dc join dc.owner doc where doc.contentType = :ct order by doc.id', [ct: Doc.CONTENT_TYPE_FILE]
        )
        result.listOfDocsNotInUseDoc = Doc.executeQuery(
                'select doc from Doc doc where doc.contentType = :ct and doc not in ' +
                    '(select distinct(doc2) from DocContext dc join dc.owner doc2 where doc2.contentType = :ct) ' +
                'order by doc.id', [ct: Doc.CONTENT_TYPE_FILE]
        )

        result.listOfDocsInUse.each { Doc doc ->
            if (! FileUtils.fileCheck("${result.filePath}/${doc.uuid}")) {
                result.listOfDocsInUseOrphaned << doc
            }
        }
        result.listOfDocsNotInUse.each { Doc doc ->
            if (! FileUtils.fileCheck("${result.filePath}/${doc.uuid}")) {
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

        log.debug('fileConsistency - done')
        result
    }

    /**
     * Lists all organisations (i.e. institutions, providers, agencies), their customer types, GASCO entry, legal information and API information
     */
    @Secured(['ROLE_ADMIN'])
    @Transactional
    def manageOrganisations() {
        Map<String, Object> result = [:]

        // modal params removed
        GrailsParameterMap fp = params.clone() as GrailsParameterMap
        fp.removeAll { it.key.startsWith('cmd') }
        result.filteredParams = fp

        SwissKnife.setPaginationParams(result, fp, contextService.getUser())

        Org target = params.cmd_target ? Org.get(params.cmd_target) : null

        if (params.cmd == 'changeApiLevel') {
            if (ApiToolkit.getAllApiLevels().contains(params.cmd_apiLevel)) {
                ApiToolkit.setApiLevel(target, params.cmd_apiLevel)
            }
            else if (params.cmd_apiLevel == 'Kein Zugriff') {
                ApiToolkit.removeApiLevel(target)
            }
            target.lastUpdated = new Date()
            target.save()
        }
        else if (params.cmd == 'deleteCustomerType') {
            def oss = OrgSetting.get(target, OrgSetting.KEYS.CUSTOMER_TYPE)
            if (oss != OrgSetting.SETTING_NOT_FOUND) {
                oss.delete()
            }
            target.lastUpdated = new Date()
            target.save()

            ApiToolkit.removeApiLevel(target)
        }
        else if (params.cmd == 'changeCustomerType') {
            Role customerType = Role.get(params.cmd_customerType)

            def osObj = OrgSetting.get(target, OrgSetting.KEYS.CUSTOMER_TYPE)

            if (osObj != OrgSetting.SETTING_NOT_FOUND) {
                OrgSetting oss = (OrgSetting) osObj
                oss.roleValue = customerType
                oss.save()
            }
            else {
                OrgSetting.add(target, OrgSetting.KEYS.CUSTOMER_TYPE, customerType)
            }
            target.lastUpdated = new Date()
            target.save()

            if (target.isCustomerType_Pro()) {
                ApiToolkit.setApiLevel(target, ApiToolkit.API_LEVEL_READ)
            }
            else if (target.isCustomerType_Basic()){
                ApiToolkit.removeApiLevel(target)
            }
        }
        else if (params.cmd == 'changeIsBetaTester') {
            if (target) {
                target.isBetaTester = (params.long('cmd_isBetaTester') == RDStore.YN_YES.id)
            }
            target.lastUpdated = new Date()
            target.save()
        }
        else if (params.cmd == 'changeLegalInformation') {
            if (target) {
                target.createdBy = Org.get(params.cmd_createdBy)
                target.legallyObligedBy = Org.get(params.cmd_legallyObligedBy)
            }
            target.lastUpdated = new Date()
            target.save()
        }

        FilterService.Result fsr = filterService.getOrgQuery(fp)
        result.orgList      = Org.executeQuery(fsr.query, fsr.queryParams + [max: result.max, offset: result.offset])
        result.orgListTotal = Org.executeQuery('select o.id ' + fsr.query, fsr.queryParams).size()

        result.allConsortia = Org.executeQuery(
                "select o from OrgSetting os join os.org o where os.key = 'CUSTOMER_TYPE' and (os.roleValue.authority  = 'ORG_CONSORTIUM_BASIC' or os.roleValue.authority  = 'ORG_CONSORTIUM_PRO') order by o.sortname, o.name"
        )
        result
    }

    /**
     * Call to view a list of providers which may be merged
     * because of possible conflicts with the user data registered to institutions
     */
    @Secured(['ROLE_ADMIN'])
    def mergeProviders() {
        Map<String, Object> result = [:]
        if(params.containsKey('source') && params.containsKey('target')) {
            result = providerService.mergeProviders(genericOIDService.resolveOID(params.source), genericOIDService.resolveOID(params.target), false)
        }
        result
    }

    /**
     * Call to view a list of providers which may be merged
     * because of possible conflicts with the user data registered to institutions
     */
    @Secured(['ROLE_ADMIN'])
    def mergeVendors() {
        Map<String, Object> result = [:]
        if(params.containsKey('source') && params.containsKey('target')) {
            result = vendorService.mergeVendors(genericOIDService.resolveOID(params.source), genericOIDService.resolveOID(params.target), false)
        }
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
                idnsInstance = IdentifierNamespace.get(params.long('ns'))

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
                        detailsStats.putAt(g.createLink(controller:'license', action:'show') as String,
                                IdentifierNamespace.executeQuery(
                                        "select i.value, i.lic.id from Identifier i join i.ns idns where idns = :idns and i.lic is not null order by i.value, i.lic.id", [idns: idnsInstance]))
                        detailsStats.putAt(g.createLink(controller:'org', action:'show') as String,
                                IdentifierNamespace.executeQuery(
                                        "select i.value, i.org.id from Identifier i join i.ns idns where idns = :idns and i.org is not null order by i.value, i.org.id", [idns: idnsInstance]))
                        detailsStats.putAt(g.createLink(controller:'package', action:'show') as String,
                                IdentifierNamespace.executeQuery(
                                        "select i.value, i.pkg.id from Identifier i join i.ns idns where idns = :idns and i.pkg is not null order by i.value, i.pkg.id", [idns: idnsInstance]))
                        detailsStats.putAt(g.createLink(controller:'subscription', action:'show') as String,
                                IdentifierNamespace.executeQuery(
                                        "select i.value, i.sub.id from Identifier i join i.ns idns where idns = :idns and i.sub is not null order by i.value, i.sub.id", [idns: idnsInstance]))
                        detailsStats.putAt(g.createLink(controller:'tipp', action:'show') as String,
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
     */
    @Secured(['ROLE_ADMIN'])
    @Transactional
    def managePropertyDefinitions() {

        if (params.cmd){
            PropertyDefinition pd = PropertyDefinition.get(params.long('pd'))

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
                                flash.message = message(code:'propertyDefinition.delete.success') as String
                            }
                            catch(Exception e) {
                                flash.error = message(code:'propertyDefinition.delete.failure.default') as String
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
                                Map<String, Integer> counts = propertyService.replacePropertyDefinitions(pdFrom, pdTo, params.overwrite == 'on', true)
                                if(counts.success == 0 && counts.failures == 0) {
                                    String instanceType
                                    switch(pdFrom.descr) {
                                        case PropertyDefinition.LIC_PROP: instanceType = message(code: 'menu.institutions.replace_prop.licenses')
                                            break
                                        case PropertyDefinition.SUB_PROP: instanceType = message(code: 'menu.institutions.replace_prop.subscriptions')
                                            break
                                        case PropertyDefinition.SVY_PROP: instanceType = message(code: 'menu.institutions.replace_prop.surveys')
                                            break
                                        default: instanceType = message(code: 'menu.institutions.replace_prop.default')
                                            break
                                    }
                                    flash.message = message(code: 'menu.institutions.replace_prop.noChanges', args: [instanceType]) as String
                                }
                                else
                                    flash.message = message(code: 'menu.institutions.replace_prop.changed', args: [counts.success, counts.failures, oldName, newName]) as String
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
        String sort = params.containsKey('sort') ? params.sort : 'name_de',
        order = params.containsKey('order') ? params.order : 'asc'
        PropertyDefinition.AVAILABLE_PUBLIC_DESCR.each { String it ->
            Set<PropertyDefinition> itResult = PropertyDefinition.findAllByDescrAndTenant(it, null, [sort: sort, order: order]) // NO private properties!
            propDefs.putAt( it, itResult )
        }
        Set<CostInformationDefinition> costInformationDefs = CostInformationDefinition.findAllByTenantIsNull([sort: sort, order: order]) // NO private cost informations!
        propDefs.put(CostInformationDefinition.COST_INFORMATION, costInformationDefs)

        def (usedPdList, attrMap, multiplePdList) = propertyService.getUsageDetails() // [List<Long>, Map<String, Object>, List<Long>]

        // ERMS-6306
        multiplePdList.unique().each {
            PropertyDefinition pd = PropertyDefinition.get(it)
            if (! pd.multipleOccurrence) {
                println 'ERMS-6306 -> #' + pd.id + ' ' + pd.descr + ' - ' + pd.name + ' (' + pd.name_de + '), hardData: ' + pd.isHardData
            }
        }

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
                if (check) {
                    try {
                        int count = refdataService.replaceRefdataValues(rdvFrom, rdvTo)
                        if(count == 0)
                            flash.message = "Es mussten keine Referenzwerte ausgetauscht werden."
                        else
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

        String sort = params.containsKey('sort') ? params.sort : 'desc_' + LocaleUtils.getCurrentLang()

        [
            editable    : true,
            rdCategories: RefdataCategory.findAll(sort: sort),
            attrMap     : attrMap,
            usedRdvList : usedRdvList
        ]
    }

    /**
     * Call to load the reference data integrity check
     * @see RefdataService#integrityCheck()
     */
    @Secured(['ROLE_ADMIN'])
    @Transactional
    def manageRefdataIntegrityCheck() {

        [ integrityCheck : refdataService.integrityCheck() ]
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

        SystemMessage sm
        if (params.cmd == 'create') {
            sm = new SystemMessage( isActive: false )
        }
        else if (params.cmd == 'edit') {
            sm = SystemMessage.get(params.id)
        }

        if (sm) {
            sm.content_de = params.content_de ?: ''
            sm.content_en = params.content_en ?: ''
            sm.type = params.type
            sm.condition = params.condition ?: null

            if (sm.save()){
                flash.message = 'System-Ankündigung erfolgreich gespeichert'
            }
            else {
                if (params.cmd == 'create') {
                    flash.error = 'System-Ankündigung wurde nicht erstellt'
                } else {
                    flash.error = 'System-Ankündigung konnte nicht gespeichert werden'
                }
            }
            log.debug 'SystemMessage #' + sm.id + ' -> ' + params.cmd
        }

        result.systemMessages = SystemMessage.executeQuery('select sm from SystemMessage sm order by sm.isActive desc, sm.lastUpdated desc')
        result.editable = true
        result
    }

    @Secured(['ROLE_ADMIN'])
    def editSystemMessage(Long id) {
        render template: 'systemMessageModal', model: [msg: SystemMessage.get(id)]
    }

    @Secured(['ROLE_ADMIN'])
    @Transactional
    def deleteSystemMessage(Long id) {

        if (SystemMessage.get(id)){
            SystemMessage.get(id).delete()
            flash.message = 'System-Ankündigung wurde gelöscht'
        }

        redirect(action: 'systemMessages')
    }

    /**
     * Delivers information about the current application health state
     */
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

        result.dbInfo = [
                dbmUpdateOnStart : ConfigMapper.getPluginConfig('databasemigration.updateOnStart', Boolean),

                default: [
                        dbName           : ConfigMapper.getConfig(ConfigDefaults.DATASOURCE_DEFAULT + '.url', String).split('/').last(),
                        dbmDbCreate      : ConfigMapper.getConfig(ConfigDefaults.DATASOURCE_DEFAULT + '.dbCreate', String),
                        defaultCollate   : DatabaseInfo.getDatabaseCollate(),
                        dbmVersion       : DatabaseInfo.getDbmVersion()
                ],
                storage: [
                        dbName           : ConfigMapper.getConfig(ConfigDefaults.DATASOURCE_STORAGE + '.url', String).split('/').last(), // TODO
                        dbmDbCreate      : ConfigMapper.getConfig(ConfigDefaults.DATASOURCE_STORAGE + '.dbCreate', String), // TODO
                        defaultCollate   : DatabaseInfo.getDatabaseCollate( DatabaseInfo.DS_STORAGE ),
                        dbmVersion       : DatabaseInfo.getDbmVersion( DatabaseInfo.DS_STORAGE )
                ]
        ]

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

        if (mailTemplate.save()) {
            flash.message = message(code: 'default.created.message', args: [message(code: 'mailTemplate.label'), mailTemplate.name]) as String
        }
        else {
            flash.error = message(code: 'default.save.error.message', args: [message(code: 'mailTemplate.label')]) as String
        }
        redirect(action: 'listMailTemplates')
    }

    /**
     * Updates the given email template
     * @return the updated list view
     */
    @Transactional
    @Secured(['ROLE_ADMIN'])
    def editMailTemplate() {
        MailTemplate mailTemplate = (MailTemplate) genericOIDService.resolveOID(params.target)

        if (mailTemplate) {
            mailTemplate.name = params.name
            mailTemplate.subject = params.subject
            mailTemplate.text = params.text
            mailTemplate.language = params.language ? RefdataValue.get(params.language) : mailTemplate.language
            mailTemplate.type = params.type ? RefdataValue.get(params.type) : mailTemplate.type
        }

        if (mailTemplate.save()) {
            flash.message = message(code: 'default.updated.message', args: [message(code: 'mailTemplate.label'), mailTemplate.name]) as String
        }
        else{
            flash.error = message(code: 'default.not.updated.message', args: [message(code: 'mailTemplate.label'), mailTemplate.name]) as String
        }
        redirect(action: 'listMailTemplates')
    }

    /**
     * Loads every {@link Subscription} where {@link PermanentTitle} records are supposed to be by definition but have not been generated
     * @see Subscription#hasPerpetualAccess
     * @see IssueEntitlement
     */
    @Secured(['ROLE_ADMIN'])
    @Transactional
    def missingPermantTitlesInSubs() {

        Map<String, Object> result = [:]
        result.user = contextService.getUser()

        result.subs = Subscription.executeQuery('''
        select s from IssueEntitlement ie left join ie.subscription s left join s.orgRelations orgR 
        where ie.status != :removed and s.hasPerpetualAccess = true and orgR.roleType = :orgRole and ie.tipp.status != :removed
        and ie.tipp.id not in (select pt.tipp.id from PermanentTitle pt where pt.owner = orgR.org and pt.tipp = ie.tipp.id)
        group by s order by s.name''', [orgRole: RDStore.OR_SUBSCRIBER, removed: RDStore.TIPP_STATUS_REMOVED])

        result.editable = true
        result
    }

    /**
     * Lists current packages in the we:kb ElasticSearch index.
     * @return Data from we:kb ES
     */
    @Secured(['ROLE_ADMIN'])
    def packageLaserVsWekb() {
        Map<String, Object> result = [:], configMap = params.clone()
        result.user = contextService.getUser()
        configMap.putAll(SwissKnife.setPaginationParams(result, params, result.user))
        result.filterConfig = [['q', 'pkgStatus'],
                               ['provider', 'ddc', 'curatoryGroup'],
                               ['curatoryGroupType', 'automaticUpdates']]
        result.tableConfig = ['lineNumber', 'name', 'status', 'counts', 'curatoryGroup', 'automaticUpdates', 'lastUpdatedDisplay']
        if(SpringSecurityUtils.ifAnyGranted('ROLE_YODA'))
            result.tableConfig << 'yodaActions'
        result.ddcs = RefdataCategory.getAllRefdataValuesWithOrder(RDConstants.DDC)
        result.languages = RefdataCategory.getAllRefdataValuesWithOrder(RDConstants.LANGUAGE_ISO)
        result.putAll(packageService.getWekbPackages(configMap))
        result
    }

    @Secured(['ROLE_ADMIN'])
    def profilerCurrent() {
        Map result = [:]

        EhcacheWrapper ttl1800 = BeanStore.getCacheService().getTTL1800Cache( SystemActivityProfiler.CACHE_KEY_ACTIVE_USER )
        result.users = SystemActivityProfiler.getActiveUsers(1000 * 60 * 10).collect { u -> ttl1800.get(u) }.sort { it[1] }.reverse()

        render view: '/admin/profiler/current', model: result
    }

    /**
     * Dumps the registered counts of users over time
     * @return a list of graphs showing when how many users were recorded
     */
    @Secured(['ROLE_ADMIN'])
    def profilerActivity() {
        Map result = [:]

        Map<String, Object> activity = [:]

        // gathering data

        List<Timestamp> dayDates = SystemActivityProfiler.executeQuery(
                "select date_trunc('day', dateCreated) as day from SystemActivityProfiler group by date_trunc('day', dateCreated), dateCreated order by dateCreated desc"
        )
        dayDates.unique().take(30).each { it ->
//            List<Timestamp, Timestamp, Timestamp, Integer, Integer, Double> slots = SystemActivityProfiler.executeQuery(
            List slots = SystemActivityProfiler.executeQuery(
                    "select date_trunc('hour', dateCreated), min(dateCreated), max(dateCreated), min(userCount), max(userCount), avg(userCount) " +
                            "  from SystemActivityProfiler where date_trunc('day', dateCreated) = :day " +
                            " group by date_trunc('hour', dateCreated) order by min(dateCreated), max(dateCreated)",
                    [day: it])

            String dayKey = (DateUtils.getLocalizedSDF_noTime()).format(new Date(it.getTime()))
            activity.put(dayKey, [])

            slots.each { hour ->
                activity[dayKey].add([
                        (DateUtils.getSDF_onlyTime()).format(new Date(hour[0].getTime())),   // time.start
                        (DateUtils.getSDF_onlyTime()).format(new Date(hour[1].getTime())),   // time.min
                        (DateUtils.getSDF_onlyTime()).format(new Date(hour[2].getTime())),   // time.max
                        hour[3],    // user.min
                        hour[4],    // user.max
                        hour[5]     // user.avg
                ])
            }
        }

        // precalc

        Map<String, Object> activityMatrix = [:]
        activityMatrix.put('Ø', null)

        List averages = (0..23).collect{ 0 }
        List labels   = (0..23).collect{ "${it < 10 ? '0' + it : it}:00:00" }

        activity.each{ dayKey, values ->
            List series1 = (0..23).collect{ 0 }
            List series2 = (0..23).collect{ 0 }

            values.each { val ->
                int indexOf = labels.findIndexOf{it == val[0]}
                if (indexOf >= 0) {
                    series1.putAt(indexOf, val[3])          // [0] = min
                    //series2.putAt(indexOf, val[4])          // [1] = max
                    series2.putAt(indexOf, val[4]- val[3])  // stackBars: true - max-min -> [1] = diff
                    averages[indexOf] = averages[indexOf] + val[5]
                }
            }
            activityMatrix.put(dayKey, [series1, series2])
        }

        // averages

        for(int i=0; i<averages.size(); i++) {
            averages[i] = (averages[i]/activity.size())
        }

        activityMatrix.putAt('Ø', [averages, averages])

        result.labels = labels
        result.activity = activityMatrix

        render view: '/admin/profiler/activity', model: result
    }

    /**
     * Dumps the average loading times for the app's routes during certain time points
     * @return a table showing when which call needed how much time in average
     * @see SystemActivityProfiler
     */
    @Secured(['ROLE_ADMIN'])
    def profilerLoadtime() {
        Map<String, Object> result = [:]

        result.globalMatrix = [:]
        result.globalMatrixSteps = [0, 2000, 4000, 8000, 12000, 20000, 30000, 45000, 60000]

        result.archive = params.archive ?: SystemProfiler.getCurrentArchive()
        result.allArchives = SystemProfiler.executeQuery('select distinct(archive) from SystemProfiler').collect{ it ->
            [it,  SystemProfiler.executeQuery('select count(*) from SystemProfiler where archive =: archive', [archive: it])[0]]
        }

        List<String> allUri = SystemProfiler.executeQuery('select distinct(uri) from SystemProfiler')

        allUri.each { uri ->
            result.globalMatrix["${uri}"] = [:]
            result.globalMatrixSteps.eachWithIndex { step, i ->
                String sql = 'select count(sp.uri) from SystemProfiler sp where sp.archive = :arc and sp.uri =:uri and sp.ms > :currStep'
                Map sqlParams = [uri: uri, currStep: step, arc: result.archive]

                if (i < result.globalMatrixSteps.size() - 1) {
                    sql += ' and sp.ms < :nextStep'
                    sqlParams = [uri: uri, currStep: step, nextStep: result.globalMatrixSteps[i+1], arc: result.archive]
                }
                result.globalMatrix["${uri}"]["${step}"] = SystemProfiler.executeQuery(sql, sqlParams).get(0)
            }
        }

        result.globalStats = SystemProfiler.executeQuery(
                "select sp.uri, max(sp.ms) as max, avg(sp.ms) as avg, count(sp.ms) as counter from SystemProfiler sp where sp.archive = :arc group by sp.uri order by counter desc",
                [arc: result.archive]
        )

        result.contextStats = SystemProfiler.executeQuery(
                "select sp.uri, max(sp.ms) as max, avg(sp.ms) as avg, ctx.id, count(ctx.id) as counter from SystemProfiler sp join sp.context as ctx where sp.archive = :arc and ctx is not null group by sp.uri, ctx.id order by counter desc",
                [arc: result.archive]
        )
        
        List<BigDecimal> hmw = [ -0.05, 0.1, 0.2, 0.4, 0.6, 0.8, 1.0, 1.5, 2.0 ]
        Map<String, List> heatMap = [:]

        result.globalStats.each{ gs ->
            String uri = gs[0]
            Map<String, Long> counts = result.globalMatrix[uri]
            BigDecimal heat = 0.0

            result.globalMatrixSteps.eachWithIndex { c, idx ->
                heat += (counts.get(c.toString()) * hmw[idx])
            }
            heatMap.putAt(uri, [ heat.doubleValue() * (Math.sqrt(gs[3]) / gs[3]), gs[1], gs[2], gs[3] ]) // max, avg, count
        }
        result.globalHeatMap = heatMap.findAll {it.value[0] > 0 }.sort {e, f -> f.value[0] <=> e.value[0] }.take(20)

        render view: '/admin/profiler/loadtime', model: result
    }

    /**
     * Dumps the call counts on the app's different routes over time
     * @return a listing of graphs when which page has been called how many times
     */
    @Secured(['ROLE_ADMIN'])
    def profilerTimeline() {
        Map<String, Object> result = [:]

        List<String> allUri = SystemProfiler.executeQuery('select distinct(uri) from SystemProfiler')

        result.globalTimeline           = [:]
        result.globalTimelineStartDate  = DateUtils.localDateToSqlDate( LocalDate.now().minusDays(30) )
        result.globalTimelineDates      = (25..0).collect{ (DateUtils.getLocalizedSDF_noTime()).format( DateUtils.localDateToSqlDate( LocalDate.now().minusDays(it) ) )}

        Map<String, Integer> ordered = [:]

        allUri.each { uri ->
            result.globalTimeline[uri] = (25..0).collect { 0 }

            String sql = "select to_char(sp.dateCreated, 'dd.mm.yyyy'), count(*) from SystemProfiler sp where sp.uri = :uri and sp.dateCreated >= :dCheck group by to_char(sp.dateCreated, 'dd.mm.yyyy')"
            List hits = SystemProfiler.executeQuery(sql, [uri: uri, dCheck: result.globalTimelineStartDate])

            int count = 0
            hits.each { hit ->
                int indexOf = result.globalTimelineDates.findIndexOf { it == hit[0] }
                if (indexOf >= 0) {
                    result.globalTimeline[uri][indexOf] = hit[1]
                    count = count + hit[1]
                }
            }

            if (count > 0) {
                ordered[uri] = count
            }
        }
        result.globalTimelineOrder = ordered.sort{ it.key }

        render view: '/admin/profiler/timeline', model: result
    }
}
