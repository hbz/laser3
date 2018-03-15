package com.k_int.kbplus

import de.laser.helper.DebugAnnotation
import org.springframework.dao.DataIntegrityViolationException
import grails.converters.*
import org.elasticsearch.groovy.common.xcontent.*
import groovy.xml.MarkupBuilder
import grails.plugin.springsecurity.annotation.Secured // 2.0
import com.k_int.kbplus.auth.*;

@Deprecated
@Secured(['IS_AUTHENTICATED_FULLY'])
class TitleInstanceController {

    def springSecurityService
    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    @Secured(['ROLE_USER'])
    def index() {
		redirect controller: 'titleDetails', action: 'index', params: params
		return // ----- deprecated

        redirect action: 'list', params: params
    }

    @Secured(['ROLE_USER'])
    def list() {
		redirect controller: 'titleDetails', action: 'list', params: params
		return // ----- deprecated

      	def result = [:]
      	result.user = User.get(springSecurityService.principal.id)
		params.max = params.max ?: result.user?.getDefaultPageSize()
      	result.titleInstanceInstanceList = TitleInstance.list(params)
      	result.titleInstanceInstanceTotal = TitleInstance.count()
      	result
    }

	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
	@Secured(closure = { ctx.springSecurityService.getCurrentUser().hasAffiliation("INST_EDITOR") })
    def create() {
		redirect controller: 'titleDetails', action: 'create', params: params
		return // ----- deprecated

		switch (request.method) {
		case 'GET':
        	[titleInstanceInstance: new TitleInstance(params)]
			break
		case 'POST':
	        def titleInstanceInstance = new TitleInstance(params)
	        if (! titleInstanceInstance.save(flush: true)) {
	            render view: 'create', model: [titleInstanceInstance: titleInstanceInstance]
	            return
	        }

			flash.message = message(code: 'default.created.message', args: [message(code: 'titleInstance.label', default: 'TitleInstance'), titleInstanceInstance.id])
	        redirect action: 'show', id: titleInstanceInstance.id
			break
		}
    }

    @Secured(['ROLE_USER'])
    def show() {
		redirect controller: 'titleDetails', action: 'show', params: params
		return // ----- deprecated

        def titleInstanceInstance = TitleInstance.get(params.id)
        if (! titleInstanceInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'titleInstance.label', default: 'TitleInstance'), params.id])
            redirect action: 'list'
            return
        }

        [titleInstanceInstance: titleInstanceInstance]
    }

	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
	@Secured(closure = { ctx.springSecurityService.getCurrentUser().hasAffiliation("INST_EDITOR") })
    def edit() {
		redirect controller: 'titleDetails', action: 'edit', params: params
		return // ----- deprecated

		switch (request.method) {
		case 'GET':
	        def titleInstanceInstance = TitleInstance.get(params.id)
	        if (! titleInstanceInstance) {
	            flash.message = message(code: 'default.not.found.message', args: [message(code: 'titleInstance.label', default: 'TitleInstance'), params.id])
	            redirect action: 'list'
	            return
	        }

	        [titleInstanceInstance: titleInstanceInstance]
			break
		case 'POST':
	        def titleInstanceInstance = TitleInstance.get(params.id)
	        if (! titleInstanceInstance) {
	            flash.message = message(code: 'default.not.found.message', args: [message(code: 'titleInstance.label', default: 'TitleInstance'), params.id])
	            redirect action: 'list'
	            return
	        }

	        if (params.version) {
	            def version = params.version.toLong()
	            if (titleInstanceInstance.version > version) {
	                titleInstanceInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
	                          [message(code: 'titleInstance.label', default: 'TitleInstance')] as Object[],
	                          "Another user has updated this TitleInstance while you were editing")
	                render view: 'edit', model: [titleInstanceInstance: titleInstanceInstance]
	                return
	            }
	        }

	        titleInstanceInstance.properties = params

	        if (!titleInstanceInstance.save(flush: true)) {
	            render view: 'edit', model: [titleInstanceInstance: titleInstanceInstance]
	            return
	        }

			flash.message = message(code: 'default.updated.message', args: [message(code: 'titleInstance.label', default: 'TitleInstance'), titleInstanceInstance.id])
	        redirect action: 'show', id: titleInstanceInstance.id
			break
		}
    }

	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
	@Secured(closure = { ctx.springSecurityService.getCurrentUser().hasAffiliation("INST_EDITOR") })
    def delete() {
		redirect controller: 'titleDetails', action: 'delete', params: params
		return // ----- deprecated

        def titleInstanceInstance = TitleInstance.get(params.id)
        if (! titleInstanceInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'titleInstance.label', default: 'TitleInstance'), params.id])
            redirect action: 'list'
            return
        }

        try {
            titleInstanceInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'titleInstance.label', default: 'TitleInstance'), params.id])
            redirect action: 'list'
        }
        catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'titleInstance.label', default: 'TitleInstance'), params.id])
            redirect action: 'show', id: params.id
        }
    }
}
