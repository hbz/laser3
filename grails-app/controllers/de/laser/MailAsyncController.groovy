package de.laser

import de.laser.auth.User
import de.laser.utils.DateUtils
import grails.plugin.asyncmail.AsynchronousMailMessage
import grails.plugin.asyncmail.enums.MessageStatus
import grails.plugin.springsecurity.annotation.Secured

import java.text.SimpleDateFormat

/**
 * This controller takes care of the asynchronously sent mails (or mails about to be sent)
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class MailAsyncController {

    ContextService contextService

    static defaultAction = 'index'

    static allowedMethods = [update: 'POST']

    /**
     * List messages.
     */
    @Secured(['ROLE_YODA'])
    def index() {
        User user = contextService.getUser()
        params.max    = params.max    ? Integer.parseInt(params.max.toString()) : user.getPageSizeOrDefault()
        params.offset = params.offset ? Integer.parseInt(params.offset.toString()) : 0
        params.sort = params.sort ?: 'createDate'
        params.order = params.order ?: 'desc'

        List resultListe = AsynchronousMailMessage.list(params)
        int resultCount = AsynchronousMailMessage.count()

        if(params.filter || params.createDate || params.sentDate){

            List wherePa = []
            Map whereMap = [:]
            SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()

            if(params.filter){
                wherePa << 'subject like :filter'
                whereMap.filter = "%${params.filter}%"
            }

            if(params.filterBody){
                wherePa << 'text like :filterBody'
                whereMap.filterBody = "%${params.filterBody}%"
            }

            if(params.createDate){
                wherePa << 'createDate = :createDate'
                whereMap.createDate = sdf.parse(params.createDate)
            }

            if(params.sentDate){
                wherePa << 'sentDate  = :sentDate'
                whereMap.sentDate = sdf.parse(params.sentDate)
            }

            println(whereMap)

            String query = "select amm from AsynchronousMailMessage as amm where ${wherePa.join(' and ')}"
            String countQuery = "select count(*) from AsynchronousMailMessage as amm where ${wherePa.join(' and ')}"

            resultListe = AsynchronousMailMessage.executeQuery(query, whereMap, params)
            resultCount = AsynchronousMailMessage.executeQuery(countQuery, whereMap, params)[0]
        }

        [resultList: resultListe, resultCount: resultCount]
    }

    /**
     * Gets the desired mail message with the given ID and passes it to the closure specified
     * @param cl the action in which the message should be used
     * @return redirects back to the index page
     */
    private _withMessage(Closure cl) {
        AsynchronousMailMessage message = AsynchronousMailMessage.get(params.id)
        if (message) {
            return cl(message)
        }

        flash.message = "The message ${params.id} was not found."
        flash.error = true
        redirect(action: 'index')
    }

    /**
     * Show message data.
     */
    @Secured(['ROLE_YODA'])
    def show() {
        _withMessage { AsynchronousMailMessage message ->
            return [message: message]
        }
    }

  /*  *//**
     * Edit message data.
     *//*
    @Secured(['ROLE_YODA'])
    def edit() {
        _withMessage {AsynchronousMailMessage message ->
            return [message: message]
        }
    }*/

    /**
     * Update message.
     *//*
    @Secured(['ROLE_YODA'])
    def update() {
        _withMessage {AsynchronousMailMessage message ->
            bindData(
                    message, params,
                    [include:
                            [
                                    'status',
                                    'beginDate',
                                    'endDate',
                                    'maxAttemptsCount',
                                    'attemptInterval',
                                    'priority',
                                    'markDelete'
                            ]
                    ]
            )
            message.attemptsCount = 0
            if (!message.hasErrors() && message.save(flush: true)) {
                flash.message = "The message ${params.id} was updated."
                redirect(action: 'show', id: message.id)
            } else {
                render(view: 'edit', model: [message: message])
            }
        }
    }*/

    /**
     * Abort message sending.
     */
    @Secured(['ROLE_YODA'])
    def abort() {
        _withMessage { AsynchronousMailMessage message ->
            if (message.abortable) {
                message.status = MessageStatus.ABORT
                if (message.save(flush: true)) {
                    flash.message = "The message ${message.id} was aborted."
                } else {
                    flash.message = "Can't abort the message ${message.id}."
                    flash.error = true
                }
            } else {
                flash.message = "Can't abort the message ${message.id} with the status ${message.status}."
                flash.error = true
            }
            redirect(action: 'index')
        }
    }

    /**
     * Delete message.
     */
    @Secured(['ROLE_YODA'])
    def delete() {
        _withMessage { AsynchronousMailMessage message ->
            try {
                message.delete(flush: true)
                flash.message = "The message ${message.id} was deleted."
                redirect(action: 'index')
            } catch (Exception e) {
                def errorMessage = "Can't delete the message with the id ${message.id}.";
                log.error(errorMessage, e)
                flash.message = errorMessage
                flash.error = true
                redirect(action: 'show', id: message.id)
            }
        }
    }
}
