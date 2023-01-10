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
