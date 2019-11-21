package com.k_int.kbplus

import de.laser.controller.AbstractDebugController
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured

@Secured(['IS_AUTHENTICATED_FULLY'])
class SpotlightController extends AbstractDebugController {
  def ESSearchService
  def springSecurityService
  def dataloadService
  def contextService

  def g = new org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib()

    @Secured(['ROLE_USER'])
  def index() { 
    log.debug("spotlight::index");
  }

    @Secured(['ROLE_USER'])
  def search() { 
    log.debug("spotlight::search");
    def result = [:]
    def filtered
    def query = "${params.query}"
    result.user = springSecurityService.getCurrentUser()
    //params.max = result.user.getDefaultPageSizeTMP() ?: 15
    params.max = 50

        if (!query) {
      return result
    }

    if (springSecurityService.isLoggedIn()) {
      if (query.startsWith("\$") && query.length() > 2 && query.indexOf(" ") != -1 ) {
        def filter = query.substring(0,query.indexOf(" "))
        switch (filter) {
          case "\$t":
            params.type = "title"
            query = query.replace("\$t  ","")
            filtered = "Title Instance"
            break
          case "\$pa":
            params.type = "package"
            query = query.replace("\$pa ","")
            filtered = "Package"
            break
          case "\$p":
            params.type = "package"
            query = query.replace("\$p ","")
            filtered = "Package"
            break
          case "\$pl":
            params.type = "platform"
            query = query.replace("\$pl ","")
            filtered = "Platform"
            break;
          case "\$s":
            params.type = "subscription"
            query = query.replace("\$s ","")     
            filtered = "Subscription"     
            break
          case "\$o":
            params.type = "organisation"
            query = query.replace("\$o ","")
            filtered = "Organisation"
            break
          case "\$l":
            params.type = "license"
            query = query.replace("\$l ","")
            filtered = "License"
            break
        }

      }
      params.q = query
      //From the available orgs, see if any belongs to a consortium, and add consortium ID too
      //TMP Bugfix, restrict for now to context org! A proper solution has to be found later!
      params.availableToOrgs = [contextService.org.id]

      if(query.startsWith("\$")){
        if( query.length()> 2){
        result = ESSearchService.search(params)
        }
      }else{
        result = ESSearchService.search(params)
      }
      result.filtered = filtered
        // result?.facets?.type?.pop()?.term
    }
    result
  }

    private def getAvailableOrgs(orgs) {
    def orgsWithConsortia = []
    for (org in orgs) {
      if(org.outgoingCombos){
        for(combo in org.outgoingCombos){
          if(combo.type.value.equals("Consortium")){
            println "ORG IN CONSORTIUM"
            if(!orgsWithConsortia.contains(combo.toOrg.id)){
              orgsWithConsortia.add(combo.toOrg.id)
            }
            break
          }
        }
      }
      if(!orgsWithConsortia.contains(org.id)){
        orgsWithConsortia.add(org.id)
      }
    }
    return orgsWithConsortia
  }

    private def getActionLinks(q) {
    def result = []
    result = allActions
    return result;
  }
}
