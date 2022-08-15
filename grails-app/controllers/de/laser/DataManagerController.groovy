package de.laser


import de.laser.utils.SwissKnife
import de.laser.remote.ApiSource
import de.laser.auth.User

import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.cache.SessionCacheWrapper
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured

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
    params.max =  params.max ?: result.user.getPageSizeOrDefault()

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
