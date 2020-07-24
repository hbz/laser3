package com.k_int.kbplus

import de.laser.controller.AbstractDebugController
import de.laser.helper.DateUtil
import de.laser.helper.DebugAnnotation
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.dao.DataIntegrityViolationException

import java.text.SimpleDateFormat

@Secured(['IS_AUTHENTICATED_FULLY'])
class ReaderNumberController extends AbstractDebugController {

	def springSecurityService
	def contextService

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
	@Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def create() {
		SimpleDateFormat sdf = DateUtil.getSDF_NoTime()
		if (params.dueDate)
			params.dueDate = sdf.parse(params.dueDate)

		params.org = Org.get(params.orgid)
		params.referenceGroup = params.referenceGroup.isLong() ? RefdataValue.findById(Long.parseLong(params.referenceGroup)).getI10n('value') : params.referenceGroup
		ReaderNumber numbersInstance = new ReaderNumber(params)
		if (! numbersInstance.save()) {
			flash.error = message(code: 'default.not.created.message', args: [message(code: 'readerNumber.number.label')])
			log.error(numbersInstance.errors)
		}
		redirect controller: 'organisation', action: 'readerNumber', params: [id:params.orgid]
    }

	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
	@Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def edit() {
		ReaderNumber numbersInstance = ReaderNumber.get(params.id)
		if (! numbersInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'readerNumber.label'), params.id])
		}
		SimpleDateFormat sdf = DateUtil.getSDF_NoTime()
		params.referenceGroup = params.referenceGroup.isLong() ? RefdataValue.findById(Long.parseLong(params.referenceGroup)).getI10n('value') : params.referenceGroup
		if(params.dueDate)
			params.dueDate = sdf.parse(params.dueDate)
		numbersInstance.properties = params
		if (! numbersInstance.save()) {
			flash.error = message(code:'default.not.updated.message', args: [message(code: 'readerNumber.label'), numbersInstance.id])
			log.error(numbersInstance.getErrors())
		}
		redirect controller: 'organisation', action: 'readerNumber', params: [id:params.orgid]
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
