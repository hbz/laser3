package com.k_int.kbplus


import de.laser.controller.AbstractDebugController
import de.laser.helper.DebugAnnotation
import org.springframework.dao.DataIntegrityViolationException
import grails.plugin.springsecurity.annotation.Secured

import java.text.SimpleDateFormat

@Secured(['IS_AUTHENTICATED_FULLY'])
class ReaderNumberController extends AbstractDebugController {

	def springSecurityService
	def contextService

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
	@Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def create() {
		switch (request.method) {
		case 'GET':
        	[numbersInstance: new ReaderNumber(params)]
			break
		case 'POST':

			SimpleDateFormat sdf = new SimpleDateFormat(message(code:'default.date.format.notime', default:'yyyy-MM-dd'))

			if (params.dueDate)
				params.dueDate = sdf.parse(params.dueDate)

			if (params.startDate)
				params.startDate = sdf.parse(params.startDate)

			params.org = Org.get(params.orgid)
			params.referenceGroup = params.referenceGroup.isLong() ? RefdataValue.findById(Long.parseLong(params.referenceGroup)).getI10n('value') : params.referenceGroup
	        ReaderNumber numbersInstance = new ReaderNumber(params)
	        if (! numbersInstance.save(flush: true)) {
				flash.error = message(code: 'default.not.created.message', args: [message(code: 'readerNumber.number.label')])
                render view: 'create', model: [numbersInstance: numbersInstance]
	            return
	        }

			flash.message = message(code: 'default.created.message', args: [message(code: 'readerNumber.number.label'), numbersInstance.value])
			redirect(url: request.getHeader('referer'))
			break
		}
    }

	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
	@Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def edit() {
		ReaderNumber numbersInstance = ReaderNumber.get(params.id)
		switch (request.method) {
		case 'GET':
	        if (! numbersInstance) {
	            flash.error = message(code: 'default.not.found.message', args: [message(code: 'readerNumber.label'), params.id])
				redirect(url: request.getHeader('referer'))
	            return
	        }

	        [numbersInstance: numbersInstance]
			break
		case 'POST':
	        if (! numbersInstance) {
				flash.message = message(code: 'default.not.found.message', args: [message(code: 'readerNumber.label'), params.id])
				redirect(url: request.getHeader('referer'))
				return
			}
			SimpleDateFormat sdf = new SimpleDateFormat(message(code:'default.date.format.notime', default:'yyyy-MM-dd'))
	        if (params.version) {
	            def version = params.version.toLong()
	            if (numbersInstance.version > version) {
	                numbersInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
	                          [message(code: 'readerNumber.label')] as Object[],
	                          "Another user has updated this ReaderNumber while you were editing")
	                render view: 'edit', model: [numbersInstance: numbersInstance]
	                return
	            }
	        }
			params.referenceGroup = params.referenceGroup.isLong() ? RefdataValue.findById(Long.parseLong(params.referenceGroup)).getI10n('value') : params.referenceGroup
			params.dueDate = sdf.parse(params.dueDate)
	        numbersInstance.properties = params

	        if (! numbersInstance.save(flush: true)) {
				flash.error = message(code:'default.not.updated.message', args: [message(code: 'readerNumber.label'), numbersInstance.id])
				log.error(numbersInstance.getErrors())
	            redirect(url: request.getHeader('referer'))
	            return
	        }

			flash.message = message(code: 'default.updated.message', args: [message(code: 'readerNumber.label'), numbersInstance.id])
			redirect(url: request.getHeader('referer'))
			break
		}
    }
	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
	@Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def delete() {
        ReaderNumber numbersInstance = ReaderNumber.get(params.id)
        if (! numbersInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'readerNumber.label'), params.id])
			redirect(url: request.getHeader('referer'))
            return
        }

        try {
            numbersInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'readerNumber.label'), params.id])
			redirect(url: request.getHeader('referer'))
        }
        catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'readerNumber.label'), params.id])
			redirect(url: request.getHeader('referer'))
        }
    }
}
