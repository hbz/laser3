package com.k_int.kbplus

import de.laser.helper.DebugAnnotation
import org.springframework.dao.DataIntegrityViolationException
import grails.converters.*
import org.elasticsearch.groovy.common.xcontent.*
import groovy.xml.MarkupBuilder
import grails.plugin.springsecurity.annotation.Secured // 2.0
import com.k_int.kbplus.auth.*;

@Secured(['IS_AUTHENTICATED_FULLY'])
class DocController {

	def springSecurityService

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

	@Secured(['ROLE_USER'])
    def index() {
        redirect action: 'list', params: params
    }

	@Secured(['ROLE_USER'])
    def list() {
      	def result = [:]
      	result.user = User.get(springSecurityService.principal.id)

		params.max = params.max ?: result.user?.getDefaultPageSize()

      	result.docInstanceList = Doc.list(params)
      	result.docInstanceTotal = Doc.count()
      	result
    }

	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
	@Secured(closure = { ctx.springSecurityService.getCurrentUser().hasAffiliation("INST_EDITOR") })
    def create() {
		switch (request.method) {
		case 'GET':
        	[docInstance: new Doc(params)]
			break
		case 'POST':
	        def docInstance = new Doc(params)
	        if (!docInstance.save(flush: true)) {
	            render view: 'create', model: [docInstance: docInstance]
	            return
	        }

			flash.message = message(code: 'default.created.message', args: [message(code: 'doc.label', default: 'Doc'), docInstance.id])
	        redirect action: 'show', id: docInstance.id
			break
		}
    }

	@Secured(['ROLE_USER'])
    def show() {
        def docInstance = Doc.get(params.id)
        if (!docInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'doc.label', default: 'Doc'), params.id])
            redirect action: 'list'
            return
        }

        [docInstance: docInstance]
    }

	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
	@Secured(closure = { ctx.springSecurityService.getCurrentUser().hasAffiliation("INST_EDITOR") })
    def edit() {
		switch (request.method) {
		case 'GET':
	        def docInstance = Doc.get(params.id)
	        if (!docInstance) {
	            flash.message = message(code: 'default.not.found.message', args: [message(code: 'doc.label', default: 'Doc'), params.id])
	            redirect action: 'list'
	            return
	        }

	        [docInstance: docInstance]
			break
		case 'POST':
	        def docInstance = Doc.get(params.id)
	        if (!docInstance) {
	            flash.message = message(code: 'default.not.found.message', args: [message(code: 'doc.label', default: 'Doc'), params.id])
	            redirect action: 'list'
	            return
	        }

	        if (params.version) {
	            def version = params.version.toLong()
	            if (docInstance.version > version) {
	                docInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
	                          [message(code: 'doc.label', default: 'Doc')] as Object[],
	                          "Another user has updated this Doc while you were editing")
	                render view: 'edit', model: [docInstance: docInstance]
	                return
	            }
	        }

	        docInstance.properties = params

	        if (!docInstance.save(flush: true)) {
	            render view: 'edit', model: [docInstance: docInstance]
	            return
	        }

			flash.message = message(code: 'default.updated.message', args: [message(code: 'doc.label', default: 'Doc'), docInstance.id])
	        redirect action: 'show', id: docInstance.id
			break
		}
    }

	@DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
	@Secured(closure = { ctx.springSecurityService.getCurrentUser().hasAffiliation("INST_EDITOR") })
    def delete() {
        def docInstance = Doc.get(params.id)
        if (!docInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'doc.label', default: 'Doc'), params.id])
            redirect action: 'list'
            return
        }

        try {
            docInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'doc.label', default: 'Doc'), params.id])
            redirect action: 'list'
        }
        catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'doc.label', default: 'Doc'), params.id])
            redirect action: 'show', id: params.id
        }
    }
}
