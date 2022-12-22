package de.laser

import de.laser.storage.BeanStore
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.sql.Sql
import org.hibernate.Session
import org.hibernate.SessionFactory

import javax.sql.DataSource

/**
 * A container service for methods used widespread in the system
 */
@Transactional
class GlobalService {

    SessionFactory sessionFactory

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

    static boolean isset(GrailsParameterMap params, String key) {
        if(params.get(key) instanceof String[])
            params.list(key).size() > 0
        else if(params.get(key) instanceof GrailsParameterMap) {
            params.get(key).size() > 0
        }
        else if(params.get(key) instanceof Boolean) {
            params.get(key) != null
        }
        else params.get(key)?.trim()?.length() > 0
    }

    /**
     * Returns an SQL connection object for performing queries in native SQL instead of HQL.
     * Implemented static because of usage in static context
     * @return a connection to the database
     */
    static Sql obtainSqlConnection() {
        DataSource dataSource = BeanStore.getDataSource()
        new Sql(dataSource)
    }

    static Sql obtainStorageSqlConnection() {
        DataSource dataSource = BeanStore.getStorageDataSource()
        new Sql(dataSource)
    }
}