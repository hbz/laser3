package de.laser

import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.sql.Sql
import org.hibernate.Session

/**
 * A container service for methods used widespread in the system
 */
@Transactional
class GlobalService {

    def sessionFactory

    /**
     * Clears the session from residual objects. Necessary for bulk operations which slow down
     * when the GORM container fills up
     */
    void cleanUpGorm() {
        log.debug("Clean up GORM")

        Session session = sessionFactory.currentSession
        session.flush()
        session.clear()
    }

    /**
     * Returns an SQL connection object for performing queries in native SQL instead of HQL.
     * Implemented static because of usage in static context
     * @return a connection to the database
     */
    static Sql obtainSqlConnection() {
        def dataSource = Holders.grailsApplication.mainContext.getBean('dataSource')
        new Sql(dataSource)
    }
}