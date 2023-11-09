package de.laser

import de.laser.cache.EhcacheWrapper
import de.laser.config.ConfigMapper
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

    static final long LONG_PROCESS_LIMBO = 5 //2 minutes, other values are for debug only, do not commit!

    CacheService cacheService
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

    /**
     * Inspired by PHP's <a href="https://www.php.net/manual/en/function.isset">isset()</a> method, this
     * method checks if a request parameter key is defined and contains a value in the request parameter map
     * @param params the parameter map for the current request
     * @param key the key to check in the parameter map
     * @return true if the key exists in the map and a not null value is defined, false otherwise
     */
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
     * Gets the file storage location for temporary export files. The path is defined
     * in the local config and defaults to /usage
     * If there is no directory at the specified path, it will be created
     * @return a path to the temporary export save location
     */
    static String obtainFileStorageLocation() {
        String dir = ConfigMapper.getStatsReportSaveLocation() ?: '/usage'
        File folder = new File(dir)
        if (!folder.exists()) {
            folder.mkdir()
        }
        dir
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

    /**
     * Returns an SQL connection object for performing queries in native SQL instead of HQL.
     * The connection is being established with the storage database.
     * Implemented static because of usage in static context
     * @return a connection to the storage database
     */
    static Sql obtainStorageSqlConnection() {
        DataSource dataSource = BeanStore.getStorageDataSource()
        new Sql(dataSource)
    }

    void notifyBackgroundProcessFinish(long userId, String cacheKey, String mess) {
        EhcacheWrapper cache = cacheService.getTTL1800Cache("finish_${userId}")
        cache.put(cacheKey, mess)
    }
}