package com.k_int.kbplus

import de.laser.helper.DebugAnnotation
import org.springframework.dao.DataIntegrityViolationException
import grails.converters.*
import org.elasticsearch.groovy.common.xcontent.*
import groovy.xml.MarkupBuilder
import grails.plugin.springsecurity.annotation.Secured
import com.k_int.kbplus.auth.*;

@Deprecated
@Secured(['IS_AUTHENTICATED_FULLY'])
class TitleInstancePackagePlatformController {

    def springSecurityService
    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    @Secured(['ROLE_USER'])
    def index() {
        redirect controller: 'tipp', action: 'index', params: params
        return // ----- deprecated

        redirect action: 'list', params: params
    }

    @Secured(['ROLE_USER'])
    def list() {
        redirect controller: 'tipp', action: 'list', params: params
        return // ----- deprecated

        def result=[:]
        result.user = User.get(springSecurityService.principal.id)

        params.max = params.max ?: result.user?.getDefaultPageSize()

        result.titleInstancePackagePlatformInstanceList=TitleInstancePackagePlatform.list(params)
        result.titleInstancePackagePlatformInstanceTotal=TitleInstancePackagePlatform.count()
        result
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def create() {
        redirect controller: 'tipp', action: 'create', params: params
        return // ----- deprecated

        switch (request.method) {
        case 'GET':
              [titleInstancePackagePlatformInstance: new TitleInstancePackagePlatform(params)]
          break
        case 'POST':
              def titleInstancePackagePlatformInstance = new TitleInstancePackagePlatform(params)
              if (! titleInstancePackagePlatformInstance.save(flush: true)) {
                  render view: 'create', model: [titleInstancePackagePlatformInstance: titleInstancePackagePlatformInstance]
                  return
              }

        flash.message = message(code: 'default.created.message', args: [message(code: 'titleInstancePackagePlatform.label', default: 'TitleInstancePackagePlatform'), titleInstancePackagePlatformInstance.id])
              redirect action: 'show', id: titleInstancePackagePlatformInstance.id
          break
        }
    }

    @Secured(['ROLE_USER'])
    def show() {
        redirect controller: 'tipp', action: 'show', params: params
        return // ----- deprecated

        def titleInstancePackagePlatformInstance = TitleInstancePackagePlatform.get(params.id)
        if (! titleInstancePackagePlatformInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'titleInstancePackagePlatform.label', default: 'TitleInstancePackagePlatform'), params.id])
            redirect action: 'list'
            return
        }

        [titleInstancePackagePlatformInstance: titleInstancePackagePlatformInstance]
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def edit() {
        redirect controller: 'tipp', action: 'edit', params: params
        return // ----- deprecated

        switch (request.method) {
        case 'GET':
              def titleInstancePackagePlatformInstance = TitleInstancePackagePlatform.get(params.id)
              if (! titleInstancePackagePlatformInstance) {
                  flash.message = message(code: 'default.not.found.message', args: [message(code: 'titleInstancePackagePlatform.label', default: 'TitleInstancePackagePlatform'), params.id])
                  redirect action: 'list'
                  return
              }

              [titleInstancePackagePlatformInstance: titleInstancePackagePlatformInstance]
          break
        case 'POST':
              def titleInstancePackagePlatformInstance = TitleInstancePackagePlatform.get(params.id)
              if (! titleInstancePackagePlatformInstance) {
                  flash.message = message(code: 'default.not.found.message', args: [message(code: 'titleInstancePackagePlatform.label', default: 'TitleInstancePackagePlatform'), params.id])
                  redirect action: 'list'
                  return
              }

              if (params.version) {
                  def version = params.version.toLong()
                  if (titleInstancePackagePlatformInstance.version > version) {
                      titleInstancePackagePlatformInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
                                [message(code: 'titleInstancePackagePlatform.label', default: 'TitleInstancePackagePlatform')] as Object[],
                                "Another user has updated this TitleInstancePackagePlatform while you were editing")
                      render view: 'edit', model: [titleInstancePackagePlatformInstance: titleInstancePackagePlatformInstance]
                      return
                  }
              }

              titleInstancePackagePlatformInstance.properties = params

              if (! titleInstancePackagePlatformInstance.save(flush: true)) {
                  render view: 'edit', model: [titleInstancePackagePlatformInstance: titleInstancePackagePlatformInstance]
                  return
              }

          flash.message = message(code: 'default.updated.message', args: [message(code: 'titleInstancePackagePlatform.label', default: 'TitleInstancePackagePlatform'), titleInstancePackagePlatformInstance.id])
              redirect action: 'show', id: titleInstancePackagePlatformInstance.id
          break
        }
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def delete() {
        redirect controller: 'tipp', action: 'delete', params: params
        return // ----- deprecated

        def titleInstancePackagePlatformInstance = TitleInstancePackagePlatform.get(params.id)
        if (!titleInstancePackagePlatformInstance) {
          flash.message = message(code: 'default.not.found.message', args: [message(code: 'titleInstancePackagePlatform.label', default: 'TitleInstancePackagePlatform'), params.id])
          redirect action: 'list'
          return
        }

        try {
          titleInstancePackagePlatformInstance.delete(flush: true)
          flash.message = message(code: 'default.deleted.message', args: [message(code: 'titleInstancePackagePlatform.label', default: 'TitleInstancePackagePlatform'), params.id])
          redirect action: 'list'
        }
        catch (DataIntegrityViolationException e) {
          flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'titleInstancePackagePlatform.label', default: 'TitleInstancePackagePlatform'), params.id])
          redirect action: 'show', id: params.id
        }
    }
}
