package de.laser

import de.laser.auth.User
import de.laser.reporting.export.GlobalExportHelper
import de.laser.reporting.report.ReportingCache
import grails.converters.JSON
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.grails.web.json.JSONElement

@Slf4j
class ReportingFilter {

    User   owner
    String token
    String title
    String description
    String filter
    String filterMap
    String labels

    Date dateCreated
    Date lastUpdated

    static mapping = {
        id              column: 'rf_id'
        version         column: 'rf_version'
        owner           column: 'rf_owner_fk'
        token           column: 'rf_token'
        title           column: 'rf_title'
        description     column: 'rf_description', type: 'text'
        filter          column: 'rf_filter'
        filterMap       column: 'rf_filter_map', type: 'text'
        labels          column: 'rf_labels', type: 'text'
        lastUpdated     column: 'rf_last_updated'
        dateCreated     column: 'rf_date_created'
    }

    static constraints = {
        token           (blank: false)
        title           (blank: false)
        description     (nullable: true)
        filter          (blank: false)
        filterMap       (blank: false)
        labels          (nullable: true)
        lastUpdated     (nullable: true)
        dateCreated     (nullable: true)
    }

    static ReportingFilter construct(ReportingCache rCache, User owner, String title, String description) {

        withTransaction {
            ReportingFilter rf = ReportingFilter.findByToken( rCache.token )

            if (! rf) {
                rf = new ReportingFilter(
                        owner: owner,
                        token: rCache.token
                )
            }

            Map<String, Object> meta = rCache.readMeta()
            Map<String, Object> filterCache = rCache.readFilterCache()

            rf.title        = title
            rf.description  = description
            rf.filter       = meta.filter
            rf.filterMap    = JsonOutput.toJson( filterCache.map )
            rf.labels       = JsonOutput.toJson( GlobalExportHelper.getCachedFilterLabels( rCache.token ) )
            rf.save()

            rf
        }
    }

    JSONElement getParsedFilterMap() {
        JSONElement json
        try {
            json = JSON.parse(filterMap)
        }
        catch(Exception e) {
            json = JSON.parse('{}')
        }
        json
    }

    Map<String, Object> getParsedLabels() {
        Map<String, Object> map
        try {
            map = new JsonSlurper().parseText(labels) as Map
        }
        catch(Exception e) {
            map = [:]
        }
        map
    }
}
