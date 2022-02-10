package de.laser

import de.laser.helper.SwissKnife
import de.laser.titles.TitleHistoryEvent
import de.laser.titles.TitleInstance
import de.laser.auth.User
 
import de.laser.helper.DateUtils
import de.laser.helper.RDConstants
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured

import java.text.SimpleDateFormat

/**
 * This controller manages calls for title listing
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class TitleController  {

    def contextService
    def ESSearchService

    /**
     * Call to the list of all title instances recorded in the system
     * @return the result of {@link #list()}
     */
    @Secured(['ROLE_USER'])
    def index() {
        redirect controller: 'title', action: 'list', params: params
    }

    /**
     * Lists all recorded title in the app; the result may be filtered
     * @return a list of {@link TitleInstancePackagePlatform}s
     */
    @Secured(['ROLE_USER'])
    def list() {
        log.debug("titleSearch : ${params}")

        // TODO: copied from index() because no list() given | DB_QUERY

        Map<String, Object> result = [:]

            params.rectype = "TitleInstancePackagePlatform" // Tells ESSearchService what to look for
            //params.showAllTitles = true
            result.user = contextService.getUser()
            params.max = params.max ?: result.user.getDefaultPageSize()


            if (params.search.equals("yes")) {
                //when searching make sure results start from first page
                params.offset = params.offset ? params.int('offset') : 0
                params.remove("search")
            }

            def old_q = params.q
            def old_sort = params.sort


            params.sort = params.sort ?: "name.keyword"

            if (params.filter != null && params.filter != '') {
                params.put(params.filter, params.q)
            }else{
                params.q = params.q ?: null
            }

            result =  ESSearchService.search(params)
            //Double-Quoted search strings wont display without this
            params.q = old_q

            if(! old_q ) {
                params.remove('q')
            }
            if(! old_sort ) {
                params.remove('sort')
            }

        if (old_q) {
            result.filterSet = true
        }


        result.editable = SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')

        result
    }

    @Deprecated
    @Secured(['ROLE_USER'])
    Map<String,Object> show() {
        Map<String, Object> result = [:]

        result.editable = SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')

        result.ti = TitleInstancePackagePlatform.get(params.id)
        if (! result.ti) {
            flash.error = message(code:'titleInstance.error.notFound.es')
            redirect action: 'list'
            return
        }

        result.titleHistory = TitleHistoryEvent.executeQuery("select distinct thep.event from TitleHistoryEventParticipant as thep where thep.participant = :participant", [participant: result.tipp] )

        result
    }

    @Deprecated
    @Secured(['ROLE_ADMIN'])
    @Transactional
  def batchUpdate() {
        log.debug( params.toMapString() )
        SimpleDateFormat formatter = DateUtils.getSDF_NoTime()
        User user = contextService.getUser()

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
             tipp_to_bulk_edit.save()
        }
      }
    }

    redirect(controller:'title', action:'show', id:params.id);
  }

    /**
     * Shows the history events related to the given title
     * @return a list of history events
     */
  @Secured(['ROLE_USER'])
  def history() {
    Map<String, Object> result = [:]
    boolean exporting = params.format == 'csv'

    if ( exporting ) {
      result.max = 9999999
      params.max = 9999999
      result.offset = 0
    }
    else {
        User user = contextService.getUser()
        SwissKnife.setPaginationParams(result, params, user)
        params.max = result.max
    }

    result.titleInstance = TitleInstance.get(params.id)
    String base_query = 'from org.codehaus.groovy.grails.plugins.orm.auditable.AuditLogEvent as e where ( e.className = :instCls and e.persistedObjectId = :instId )'

    def limits = (!params.format||params.format.equals("html"))?[max:result.max, offset:result.offset]:[offset:0]

    def query_params = [ instCls: TitleInstance.class.name, instId: params.id]

    log.debug("base_query: ${base_query}, params:${query_params}, limits:${limits}");

    result.historyLines = org.codehaus.groovy.grails.plugins.orm.auditable.AuditLogEvent.executeQuery('select e '+base_query+' order by e.lastUpdated desc', query_params, limits);
    result.num_hl = org.codehaus.groovy.grails.plugins.orm.auditable.AuditLogEvent.executeQuery('select e.id '+base_query, query_params).size()
    result.formattedHistoryLines = []


    result.historyLines.each { hl ->

        def line_to_add = [:]
        def linetype = null

        switch(hl.className) {
          case TitleInstance.class.name :
              TitleInstance instance_obj = TitleInstance.get(hl.persistedObjectId);
            line_to_add = [ link: createLink(controller:'title', action: 'show', id:hl.persistedObjectId),
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

    @Deprecated
  @Secured(['ROLE_ADMIN'])
  def dmIndex() {
    log.debug("dmIndex ${params}");

    if(params.search == "yes"){
      params.offset = 0
      params.remove("search")
    }
      Map<String, Object> result = [:]
      User user = contextService.getUser()
      SwissKnife.setPaginationParams(result, params, user)

    result.availableStatuses = RefdataCategory.getAllRefdataValues(RDConstants.TITLE_STATUS)
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
