package de.laser

import de.laser.auth.User
import de.laser.reporting.export.GlobalExportHelper
import de.laser.reporting.report.ReportingCache
import grails.converters.JSON
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.grails.web.json.JSONElement

/**
 * A filter configuration, serving as bookmark for a user. A user may store his filter settings as bookmarks; the
 * current setting is then persisted as JSON string in {@link #filterMap}
 * @see User
 */
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

    /**
     * Creates or updates a filter bookmark with the given cached settings; if there is no configuration sored for the
     * given owner and token, it will be created
     * @param rCache the filter settings to be stored including a token which serves as a unique identifier for afterwards loading
     * @param owner the {@link de.laser.auth.User} whose configuration should be stored
     * @param title the name of the filter
     * @param description a description string describing the filter setting
     * @return the new or updated filter instance
     */
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

    /**
     * Parses the stored filter map and returns that parsed map; if no configuration is stored (yet), an empty map will be returned
     * @return the filter setting as JSON, if a value is persisted, an empty object otherwise
     */
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

    /**
     * Returns the filter labels as parsed map
     * @return the labels in a {@link Map}, an empty object, if an exception occurs
     */
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
