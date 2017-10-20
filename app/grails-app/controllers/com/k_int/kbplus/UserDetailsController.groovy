package com.k_int.kbplus

import org.springframework.dao.DataIntegrityViolationException
import grails.converters.*
import org.elasticsearch.groovy.common.xcontent.*
import groovy.xml.MarkupBuilder
import grails.plugins.springsecurity.Secured
import com.k_int.kbplus.auth.*;
import grails.gorm.*

import java.security.MessageDigest

class UserDetailsController {

    def springSecurityService
    def genericOIDService

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    @Secured(['ROLE_ADMIN', 'IS_AUTHENTICATED_FULLY'])
    def index() {
        redirect action: 'list', params: params
    }

    @Secured(['ROLE_ADMIN', 'IS_AUTHENTICATED_FULLY'])
    def list() {

        def result = [:]
        result.user = User.get(springSecurityService.principal.id)
        if (! params.max) {
            params.max = result.user?.getDefaultPageSize()
        }

        def results = null;
        def count = null;

      if(params.authority == "null") 
        params.authority=null;

      def criteria = new DetachedCriteria(User).build {
        if ( params.name && params.name != '' ) {
          or {
            ilike('username',"%${params.name}%")
            ilike('display',"%${params.name}%")
            ilike('instname',"%${params.name}%")
          }
        }
        if(params.authority){
          def filter_role = Role.get(params.authority.toLong())
          if(filter_role){
              roles{
                eq('role',filter_role)
              }
          }
        }
      }

      result.users = criteria.list(params)
      result.total = criteria.count()

      result
    }

  @Secured(['ROLE_ADMIN', 'IS_AUTHENTICATED_FULLY'])
  def edit() {
    def result = [:]
    result.user = User.get(springSecurityService.principal.id)
    def userInstance = User.get(params.id)
    if (!userInstance) {
        flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'Org'), params.id])
        redirect action: 'list'
        return
    }
    else {
        // check if api key and secret are existing
        def readRole  = UserRole.findAllWhere(user: userInstance, role: Role.findByAuthority('ROLE_API_READER'))
        def writeRole = UserRole.findAllWhere(user: userInstance, role: Role.findByAuthority('ROLE_API_WRITER'))
        if((readRole || writeRole)){
            if(! userInstance.apikey){
                def seed1 = Math.abs(new Random().nextFloat()).toString().getBytes()
                userInstance.apikey = MessageDigest.getInstance("MD5").digest(seed1).encodeHex().toString().take(16)
            }
            if(! userInstance.apisecret){
                def seed2 = Math.abs(new Random().nextFloat()).toString().getBytes()
                userInstance.apisecret = MessageDigest.getInstance("MD5").digest(seed2).encodeHex().toString().take(16)
            }
        }
    }
    result.ui = userInstance
    result
  }    

  @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
  def pub() {
    def result = [:]
    result.user = User.get(springSecurityService.principal.id)
    def userInstance = User.get(params.id)
    result.ui = userInstance
    result
  }    

  @Secured(['ROLE_ADMIN', 'IS_AUTHENTICATED_FULLY'])
  def create() {
    switch (request.method) {
      case 'GET':
        [orgInstance: new Org(params)]
        break
      case 'POST':
        def userInstance = new User(params)
        if (!userInstance.save(flush: true)) {
          render view: 'create', model: [userInstance: userInstance]
          return
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'user.label', default: 'User'), userInstance.id])
        redirect action: 'show', id: userInstance.id
        break
    }
  }
  
  
}
