package com.k_int.kbplus

import org.springframework.dao.DataIntegrityViolationException
import grails.converters.*
import org.elasticsearch.groovy.common.xcontent.*
import groovy.xml.MarkupBuilder
import grails.plugins.springsecurity.Secured
import com.k_int.kbplus.auth.*
import org.springframework.security.access.annotation.Secured;

@Deprecated
class LicenseController {

    def springSecurityService

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def index() {
        redirect controller: 'licenseDetails', action: 'index', params: params
        return // ----- deprecated

        redirect action: 'list', params: params
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def list() {
        redirect controller: 'licenseDetails', action: 'list', params: params
        return // ----- deprecated

        def result = [:]
        result.user = User.get(springSecurityService.principal.id)

        params.max = params.max ?: result.user?.getDefaultPageSize()

        result.licenseInstanceList = License.list(params)
        result.licenseInstanceTotal = License.count()
        result
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def create() {
        redirect controller: 'licenseDetails', action: 'create', params: params
        return // ----- deprecated

    switch (request.method) {
    case 'GET':
      [licenseInstance: new License(params)]
      break
    case 'POST':
      def licenseInstance = new License(params)
      if (!licenseInstance.save(flush: true)) {
        render view: 'create', model: [licenseInstance: licenseInstance]
        return
      }

      flash.message = message(code: 'default.created.message', args: [message(code: 'license', default: 'License'), licenseInstance.id])
      redirect action: 'show', id: licenseInstance.id
      break
    }
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def show() {
        redirect controller: 'licenseDetails', action: 'show', params: params
        return // ----- deprecated

        def licenseInstance = License.get(params.id)
        if (!licenseInstance) {
      flash.message = message(code: 'default.not.found.message', args: [message(code: 'license', default: 'License'), params.id])
            redirect action: 'list'
            return
        }

        [licenseInstance: licenseInstance]
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def edit() {
        redirect controller: 'licenseDetails', action: 'edit', params: params
        return // ----- deprecated

    switch (request.method) {
      case 'GET':
            def licenseInstance = License.get(params.id)
            if (!licenseInstance) {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'license', default: 'License'), params.id])
                redirect action: 'list'
                return
            }
  
            [licenseInstance: licenseInstance]
        break
      case 'POST':
            def licenseInstance = License.get(params.id)
            if (!licenseInstance) {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'license', default: 'License'), params.id])
                redirect action: 'list'
                return
            }
  
            if (params.version) {
                def version = params.version.toLong()
                if (licenseInstance.version > version) {
                    licenseInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
                              [message(code: 'license', default: 'License')] as Object[],
                              "Another user has updated this License while you were editing")
                    render view: 'edit', model: [licenseInstance: licenseInstance]
                    return
                }
            }

            licenseInstance.properties = params
  
            if (!licenseInstance.save(flush: true)) {
                render view: 'edit', model: [licenseInstance: licenseInstance]
                return
            }
  
        flash.message = message(code: 'default.updated.message', args: [message(code: 'license', default: 'License'), licenseInstance.id])
            redirect action: 'show', id: licenseInstance.id
        break
      }
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def delete() {
        redirect controller: 'licenseDetails', action: 'delete', params: params
        return // ----- deprecated

        def licenseInstance = License.get(params.id)
        if (!licenseInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'license', default: 'License'), params.id])
            redirect action: 'list'
            return
        }

        try {
            licenseInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'license', default: 'License'), params.id])
            redirect action: 'list'
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'license', default: 'License'), params.id])
            redirect action: 'show', id: params.id
        }
    }
}
