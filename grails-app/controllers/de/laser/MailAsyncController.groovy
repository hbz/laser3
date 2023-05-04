package de.laser

import grails.plugin.asyncmail.AsynchronousMailMessage
import grails.plugin.asyncmail.enums.MessageStatus
import grails.plugin.springsecurity.annotation.Secured

@Secured(['IS_AUTHENTICATED_FULLY'])
class MailAsyncController {
    static defaultAction = 'index'

    static allowedMethods = [update: 'POST']

    /**
     * List messages.
     */
    @Secured(['ROLE_YODA'])
    def index() {
        params.max = Math.min(params.max ? params.max.toInteger() : 10, 100)
        params.sort = params.sort ?: 'createDate'
        params.order = params.order ?: 'desc'
        [resultList: AsynchronousMailMessage.list(params)]
    }

    private withMessage(Closure cl) {
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
        withMessage {AsynchronousMailMessage message ->
            return [message: message]
        }
    }

  /*  *//**
     * Edit message data.
     *//*
    @Secured(['ROLE_YODA'])
    def edit() {
        withMessage {AsynchronousMailMessage message ->
            return [message: message]
        }
    }*/

    /**
     * Update message.
     *//*
    @Secured(['ROLE_YODA'])
    def update() {
        withMessage {AsynchronousMailMessage message ->
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
        withMessage {AsynchronousMailMessage message ->
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
        withMessage {AsynchronousMailMessage message ->
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
