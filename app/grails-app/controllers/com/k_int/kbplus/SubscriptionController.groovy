package com.k_int.kbplus

import org.springframework.dao.DataIntegrityViolationException
import grails.converters.*
import org.elasticsearch.groovy.common.xcontent.*
import groovy.xml.MarkupBuilder
import grails.plugins.springsecurity.Secured
import com.k_int.kbplus.auth.*;


@Deprecated
class SubscriptionController {

    def springSecurityService

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def index() {
        redirect controller: 'subscriptionDetails', action: 'index', params: params
        return // ----- deprecated

        redirect action: 'list', params: params
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def list() {
        redirect controller: 'subscriptionDetails', action: 'list', params: params
        return // ----- deprecated

        def result = [:]
        result.user = User.get(springSecurityService.principal.id)
        
        params.max = params.max ?: result.user?.getDefaultPageSize()

        result.subscriptionInstanceList = Subscription.list(params)
        result.subscriptionInstanceTotal = Subscription.count()
        result
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def create() {
        redirect controller: 'subscriptionDetails', action: 'create', params: params
        return // ----- deprecated

        switch (request.method) {
        case 'GET':
              [subscriptionInstance: new Subscription(params)]
          break
        case 'POST':
              def subscriptionInstance = new Subscription(params)
              if (!subscriptionInstance.save(flush: true)) {
                  render view: 'create', model: [subscriptionInstance: subscriptionInstance]
                  return
              }

          flash.message = message(code: 'default.created.message', args: [message(code: 'subscription.label', default: 'Subscription'), subscriptionInstance.id])
              redirect action: 'show', id: subscriptionInstance.id
          break
        }
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def show() {
        redirect controller: 'subscriptionDetails', action: 'show', params: params
        return // ----- deprecated

        def subscriptionInstance = Subscription.get(params.id)
        if (!subscriptionInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'subscription.label', default: 'Subscription'), params.id])
            redirect action: 'list'
            return
        }

        withFormat { 
            html { [subscriptionInstance: subscriptionInstance] }
            csv { 
                response.setContentType('text/csv') 
                response.setHeader('Content-Disposition', "attachment; filename=\"subscription-${subscriptionInstance.id}.csv\"") 
                def outs = response.outputStream 

                def cols = [:] 
                outs << "City,V-Coord,H-Coord\n" 
            
                // cityRc.each() { 
                  //Here is where I have the issue grabbing the data. 
                //   outs << cityRc.city + "," + cityRc.VCoorNb + "," + cityRc.VCoorNb 
                //   outs << "\n" 
                // } 
                outs.flush() 
                outs.close() 
            } 
        } 
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def edit() {
        redirect controller: 'subscriptionDetails', action: 'edit', params: params
        return // ----- deprecated

        switch (request.method) {
        case 'GET':
              def subscriptionInstance = Subscription.get(params.id)
              if (!subscriptionInstance) {
                  flash.message = message(code: 'default.not.found.message', args: [message(code: 'subscription.label', default: 'Subscription'), params.id])
                  redirect action: 'list'
                  return
              }

              [subscriptionInstance: subscriptionInstance]
          break
        case 'POST':
              def subscriptionInstance = Subscription.get(params.id)
              if (!subscriptionInstance) {
                  flash.message = message(code: 'default.not.found.message', args: [message(code: 'subscription.label', default: 'Subscription'), params.id])
                  redirect action: 'list'
                  return
              }

              if (params.version) {
                  def version = params.version.toLong()
                  if (subscriptionInstance.version > version) {
                      subscriptionInstance.errors.rejectValue('version', 'default.optimistic.locking.failure',
                                [message(code: 'subscription.label', default: 'Subscription')] as Object[],
                                "Another user has updated this Subscription while you were editing")
                      render view: 'edit', model: [subscriptionInstance: subscriptionInstance]
                      return
                  }
              }

              subscriptionInstance.properties = params

              if (!subscriptionInstance.save(flush: true)) {
                  render view: 'edit', model: [subscriptionInstance: subscriptionInstance]
                  return
              }

          flash.message = message(code: 'default.updated.message', args: [message(code: 'subscription.label', default: 'Subscription'), subscriptionInstance.id])
              redirect action: 'show', id: subscriptionInstance.id
          break
        }
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def delete() {
        def subscriptionInstance = Subscription.get(params.id)
        if (!subscriptionInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'subscription.label', default: 'Subscription'), params.id])
            redirect action: 'list'
            return
        }

        try {
            subscriptionInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'subscription.label', default: 'Subscription'), params.id])
            redirect action: 'list'
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'subscription.label', default: 'Subscription'), params.id])
            redirect action: 'show', id: params.id
        }
    }
}
