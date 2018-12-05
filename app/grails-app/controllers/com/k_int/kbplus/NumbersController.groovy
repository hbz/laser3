package com.k_int.kbplus


import com.k_int.kbplus.auth.User
import de.laser.controller.AbstractDebugController
import de.laser.helper.DebugAnnotation
import org.springframework.dao.DataIntegrityViolationException
import grails.plugin.springsecurity.annotation.Secured

@Secured(['IS_AUTHENTICATED_FULLY'])
class NumbersController extends AbstractDebugController {

	def springSecurityService
	def contextService

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
	@Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def create() {
		switch (request.method) {
		case 'GET':
        	[numbersInstance: new Numbers(params)]
			break
		case 'POST':

			def sdf = new java.text.SimpleDateFormat(message(code:'default.date.format.notime', default:'yyyy-MM-dd'))

			if (params.endDate)
				params.endDate = sdf.parse(params.endDate)

			if (params.startDate)
				params.startDate = sdf.parse(params.startDate)

			params.org = Org.get(params.orgid)
	        def numbersInstance = new Numbers(params)
	        if (! numbersInstance.save(flush: true)) {
				flash.error = message(code: 'default.not.created.message', args: [message(code: 'numbers.number.label', default: 'Number')])
                render view: 'create', model: [numbersInstance: numbersInstance]
	            return
	        }

			flash.message = message(code: 'default.created.message', args: [message(code: 'numbers.label', default: 'Number'), numbersInstance.id])
			redirect(url: request.getHeader('referer'))
			break
		}
    }

	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
	@Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def edit() {
		switch (request.method) {
		case 'GET':
	        def numbersInstance = Numbers.get(params.id)
	        if (! numbersInstance) {
	            flash.message = message(code: 'default.not.found.message', args: [message(code: 'numbers.label', default: 'Numbers'), params.id])
				redirect(url: request.getHeader('referer'))
	            return
	        }

	        [numbersInstance: numbersInstance]
			break
		case 'POST':
	        def numbersInstance = Numbers.get(params.id)
	        if (! numbersInstance) {
	            flash.message = message(code: 'default.not.found.message', args: [message(code: 'numbers.label', default: 'Numbers'), params.id])
				redirect(url: request.getHeader('referer'))
	            return
	        }

	        if (params.version) {
	            def version = params.version.toLong()
	            if (numbersInstance.version > version) {
	                numbersInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
	                          [message(code: 'numbers.label', default: 'Numbers')] as Object[],
	                          "Another user has updated this Numbers while you were editing")
	                render view: 'edit', model: [numbersInstance: numbersInstance]
	                return
	            }
	        }

	        numbersInstance.properties = params

	        if (! numbersInstance.save(flush: true)) {
	            render view: 'edit', model: [numbersInstance: numbersInstance]
	            return
	        }

			flash.message = message(code: 'default.updated.message', args: [message(code: 'numbers.label', default: 'Numbers'), numbersInstance.id])
			redirect(url: request.getHeader('referer'))
			break
		}
    }
	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
	@Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def delete() {
        def numbersInstance = Numbers.get(params.id)
        if (! numbersInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'numbers.label', default: 'Numbers'), params.id])
			redirect(url: request.getHeader('referer'))
            return
        }

        try {
            numbersInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'numbers.label', default: 'Numbers'), params.id])
			redirect(url: request.getHeader('referer'))
        }
        catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'numbers.label', default: 'Numbers'), params.id])
			redirect(url: request.getHeader('referer'))
        }
    }
}
