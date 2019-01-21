package com.k_int.kbplus

import de.laser.controller.AbstractDebugController
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured
import com.k_int.kbplus.auth.*
import jdk.nashorn.internal.scripts.JO;
import org.apache.log4j.*
import java.text.SimpleDateFormat
import com.k_int.kbplus.*;
import grails.plugin.springsecurity.SpringSecurityUtils

@Secured(['IS_AUTHENTICATED_FULLY'])
class TitleDetailsController extends AbstractDebugController {

    def springSecurityService
    def ESSearchService

    @Secured(['ROLE_USER'])
    def index() {
        redirect controller: 'titleDetails', action: 'list', params: params
        return // ----- deprecated

        log.debug("titleSearch : ${params}");

        def result=[:]

        if (springSecurityService.isLoggedIn()) {
            params.rectype = "Title" // Tells ESSearchService what to look for
            result.user = springSecurityService.getCurrentUser()
            params.max = result.user.getDefaultPageSizeTMP()


            if(params.search.equals("yes")){
                //when searching make sure results start from first page
                params.offset = 0
                params.remove("search")
            }

            def old_q = params.q
            if(!params.q ){
                params.remove('q');
                if(!params.sort){
                    params.sort = "sortTitle"
                }
            }

            if(params.filter) params.q ="${params.filter}:${params.q}";

            result =  ESSearchService.search(params)
            //Double-Quoted search strings wont display without this
            params.q = old_q?.replace("\"","&quot;")
        }

        result.editable=SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')

        log.debug(result);

        result
    }

    @Secured(['ROLE_USER'])
    def list() {
        log.debug("titleSearch : ${params}");

        // TODO: copied from index() because no list() given | DB_QUERY

        def result=[:]

        if (springSecurityService.isLoggedIn()) {
            params.rectype = "Title" // Tells ESSearchService what to look for
            result.user = springSecurityService.getCurrentUser()
            params.max = result.user.getDefaultPageSizeTMP()

            if (params.search.equals("yes")) {
                //when searching make sure results start from first page
                params.offset = 0
                params.remove("search")
            }

            def old_q = params.q
            def old_sort = params.sort

            params.q = params.q ?: "*"
            params.sort = params.sort ?: "sortTitle"

            if (params.filter) {
                params.q = "${params.filter}:${params.q}"
            }

            result =  ESSearchService.search(params)
            //Double-Quoted search strings wont display without this
            params.q = old_q?.replace("\"","&quot;")

            if(! old_q ) {
                params.remove('q')
            }
            if(! old_sort ) {
                params.remove('sort')
            }
        }

        result.editable = SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')

        log.debug(result)

        result
    }

  @Secured(['ROLE_DATAMANAGER'])
  def findTitleMatches() { 
    // find all titles by n_title proposedTitle
    def result=[:]
    if ( params.proposedTitle ) {
      // def proposed_title_key = com.k_int.kbplus.TitleInstance.generateKeyTitle(params.proposedTitle)
      // result.titleMatches=com.k_int.kbplus.TitleInstance.findAllByKeyTitle(proposed_title_key)
      def normalised_title = com.k_int.kbplus.TitleInstance.generateNormTitle(params.proposedTitle)
      result.titleMatches=com.k_int.kbplus.TitleInstance.findAllByNormTitleLike("${normalised_title}%")
    }
    result
  }

  @Secured(['ROLE_DATAMANAGER'])
  def createTitle() {
    log.debug("Create new title for ${params.title}");
    //def new_title = new TitleInstance(title:params.title, impId:java.util.UUID.randomUUID().toString()
    def ti_status = RefdataValue.loc(RefdataCategory.TI_STATUS, [en: 'Current', de: 'Aktuell'])
    def new_title =  ((params.typ=='Ebook') ? new BookInstance(title:params.title, impId:java.util.UUID.randomUUID().toString(), status: ti_status, type: RefdataValue.loc(RefdataCategory.TI_TYPE, [en: 'EBook', de: 'EBook'])) :
              (params.typ=='Database' ? new DatabaseInstance(title:params.title, impId:java.util.UUID.randomUUID().toString(), status: ti_status, type: RefdataValue.loc(RefdataCategory.TI_TYPE, [en: 'Database', de: 'Database'])) : new JournalInstance(title:params.title, impId:java.util.UUID.randomUUID().toString(), status: ti_status, type: RefdataValue.loc(RefdataCategory.TI_TYPE, [en: 'Journal', de: 'Journal']))))

    if ( new_title.save(flush:true) ) {
        new_title.impId = new_title.globalUID
        new_title.save(flush:true)
        log.debug("New title id is ${new_title.id}");
        redirect ( action:'edit', id:new_title.id);
    }
    else {
      log.error("Problem creating title: ${new_title.errors}");
      flash.message = "Problem creating title: ${new_title.errors}"
      redirect ( action:'findTitleMatches' )
    }
  }

    @Deprecated
    @Secured(['ROLE_ADMIN'])
    def edit() {
        redirect controller: 'titleDetails', action: 'show', params: params
        return // ----- deprecated

        def result = [:]

        result.editable = SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')

        result.ti = TitleInstance.get(params.id)
        result.duplicates = reusedIdentifiers(result.ti);
        result
    }

  @Secured(['ROLE_USER'])
  def show() {
    def result = [:]

    result.editable = SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')

    result.ti = TitleInstance.get(params.id)
    result.duplicates = reusedIdentifiers(result.ti);
    result.titleHistory = TitleHistoryEvent.executeQuery("select distinct thep.event from TitleHistoryEventParticipant as thep where thep.participant = ?",[result.ti]);

    result
  }

    private def reusedIdentifiers(title) {
    // Test for identifiers that are used accross multiple titles
    def duplicates = [:]
    def identifiers = title.ids?.collect{it.identifier}
    identifiers.each{ident ->
      ident.occurrences.each{
        if(it.ti != title && it.ti!=null && it.ti.status?.value == 'Current'){
          if(duplicates."${ident.ns.ns}:${ident.value}"){
            duplicates."${ident.ns.ns}:${ident.value}" += [it.ti]
          }else{
            duplicates."${ident.ns.ns}:${ident.value}" = [it.ti]
          }
        }
      }
    }
    return duplicates
  }

    @Secured(['ROLE_ADMIN'])
  def batchUpdate() {
    log.debug(params);
    def formatter = new java.text.SimpleDateFormat(message(code:'default.date.format.notime', default:'yyyy-MM-dd'))
    def user = User.get(springSecurityService.principal.id)

      params.each { p ->
      if ( p.key.startsWith('_bulkflag.')&& (p.value=='on'))  {
        def tipp_id_to_edit = p.key.substring(10);
        def tipp_to_bulk_edit = TitleInstancePackagePlatform.get(tipp_id_to_edit)
        boolean changed = false

        if ( tipp_to_bulk_edit != null ) {
            def bulk_fields = [
                    [ formProp:'start_date', domainClassProp:'startDate', type:'date'],
                    [ formProp:'start_volume', domainClassProp:'startVolume'],
                    [ formProp:'start_issue', domainClassProp:'startIssue'],
                    [ formProp:'end_date', domainClassProp:'endDate', type:'date'],
                    [ formProp:'end_volume', domainClassProp:'endVolume'],
                    [ formProp:'end_issue', domainClassProp:'endIssue'],
                    [ formProp:'coverage_depth', domainClassProp:'coverageDepth'],
                    [ formProp:'coverage_note', domainClassProp:'coverageNote'],
                    [ formProp:'hostPlatformURL', domainClassProp:'hostPlatformURL']
            ]

            bulk_fields.each { bulk_field_defn ->
                if ( params["clear_${bulk_field_defn.formProp}"] == 'on' ) {
                    log.debug("Request to clear field ${bulk_field_defn.formProp}");
                    tipp_to_bulk_edit[bulk_field_defn.domainClassProp] = null
                    changed = true
                }
                else {
                    def proposed_value = params['bulk_'+bulk_field_defn.formProp]
                    if ( ( proposed_value != null ) && ( proposed_value.length() > 0 ) ) {
                        log.debug("Set field ${bulk_field_defn.formProp} to ${proposed_value}");
                        if ( bulk_field_defn.type == 'date' ) {
                            tipp_to_bulk_edit[bulk_field_defn.domainClassProp] = formatter.parse(proposed_value)
                        }
                        else {
                            tipp_to_bulk_edit[bulk_field_defn.domainClassProp] = proposed_value
                        }
                        changed = true
                    }
                }
            }
          if (changed)
             tipp_to_bulk_edit.save();
        }
      }
    }

    redirect(controller:'titleDetails', action:'show', id:params.id);
  }

  @Secured(['ROLE_USER'])
  def history() {
    def result = [:]
    def exporting = params.format == 'csv' ? true : false

    if ( exporting ) {
      result.max = 9999999
      params.max = 9999999
      result.offset = 0
    }
    else {
      def user = User.get(springSecurityService.principal.id)
      result.max = params.max ? Integer.parseInt(params.max) : user.getDefaultPageSizeTMP()
      params.max = result.max
      result.offset = params.offset ? Integer.parseInt(params.offset) : 0;
    }

    result.titleInstance = TitleInstance.get(params.id)
    def base_query = 'from org.codehaus.groovy.grails.plugins.orm.auditable.AuditLogEvent as e where ( e.className = :instCls and e.persistedObjectId = :instId )'

    def limits = (!params.format||params.format.equals("html"))?[max:result.max, offset:result.offset]:[offset:0]

    def query_params = [ instCls:'com.k_int.kbplus.TitleInstance', instId:params.id]

    log.debug("base_query: ${base_query}, params:${query_params}, limits:${limits}");

    result.historyLines = org.codehaus.groovy.grails.plugins.orm.auditable.AuditLogEvent.executeQuery('select e '+base_query+' order by e.lastUpdated desc', query_params, limits);
    result.num_hl = org.codehaus.groovy.grails.plugins.orm.auditable.AuditLogEvent.executeQuery('select e.id '+base_query, query_params).size()
    result.formattedHistoryLines = []


    result.historyLines.each { hl ->

        def line_to_add = [:]
        def linetype = null

        switch(hl.className) {
          case 'com.k_int.kbplus.TitleInstance':
            def instance_obj = TitleInstance.get(hl.persistedObjectId);
            line_to_add = [ link: createLink(controller:'titleDetails', action: 'show', id:hl.persistedObjectId),
                            name: instance_obj.title,
                            lastUpdated: hl.lastUpdated,
                            propertyName: hl.propertyName,
                            actor: User.findByUsername(hl.actor),
                            oldValue: hl.oldValue,
                            newValue: hl.newValue
                          ]
            linetype = 'TitleInstance'
            break;
        }
        switch ( hl.eventName ) {
          case 'INSERT':
            line_to_add.eventName= "New ${linetype}"
            break;
          case 'UPDATE':
            line_to_add.eventName= "Updated ${linetype}"
            break;
          case 'DELETE':
            line_to_add.eventName= "Deleted ${linetype}"
            break;
          default:
            line_to_add.eventName= "Unknown ${linetype}"
            break;
        }
        result.formattedHistoryLines.add(line_to_add);
    }

    result
  }

  @Secured(['ROLE_ADMIN'])
  def availability() {
    def result = [:]
    result.ti = TitleInstance.get(params.id)
    result.availability = IssueEntitlement.executeQuery("select ie from IssueEntitlement as ie where ie.tipp.title = ?",[result.ti]);

    result
  }

  @Secured(['ROLE_DATAMANAGER'])
  def dmIndex() {
    log.debug("dmIndex ${params}");

    if(params.search == "yes"){
      params.offset = 0
      params.remove("search")
    }
    def user = User.get(springSecurityService.principal.id)
    def result = [:]
    result.max = params.max ? Integer.parseInt(params.max) : user.getDefaultPageSizeTMP()
    result.offset = params.offset ? Integer.parseInt(params.offset) : 0;
    
    def ti_cat = RefdataCategory.findByDesc(RefdataCategory.TI_STATUS)
    result.availableStatuses = RefdataValue.findAllByOwner(ti_cat)
    def ti_status = null
    
    if(params.status){
      ti_status = result.availableStatuses.find { it.value == params.status }
    }
    
    def criteria = TitleInstance.createCriteria()
    result.hits = criteria.list(max: result.max, offset:result.offset){
        if(params.q){
          ilike("title","${params.q}%")
        }
        if(ti_status){
          eq('status',ti_status)
        }
        order("sortTitle", params.order?:'asc')
    }

    result.totalHits = result.hits.totalCount

    result
  }

}
