package de.laser

import de.laser.annotations.UnstableFeature
import de.laser.cache.EhcacheWrapper
import de.laser.config.ConfigMapper
import de.laser.exceptions.NativeSqlException
import de.laser.helper.DatabaseInfo
import de.laser.storage.BeanStore
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.springframework.web.multipart.MultipartFile

import javax.sql.DataSource

/**
 * A container service for methods used widespread in the system
 */
@Transactional
class GlobalService {

    static final long LONG_PROCESS_LIMBO = 30 //30 seconds, other values are for debug only, do not commit!

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
    static String obtainTmpFileLocation() {
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
    static Sql obtainSqlConnection() throws NativeSqlException {
        DataSource dataSource = BeanStore.getDataSource()
        Sql sql = new Sql(dataSource)
        List<GroovyRowResult> activeConnections = sql.rows("select count(*) from pg_stat_activity where datname = current_database() and state != 'idle' and usename ilike '%laser%'")
        int max = Integer.valueOf(DatabaseInfo.getMaxConnections())
        if (activeConnections[0]['count'] < (max * 0.65)) // 65%
            sql
        else {
            throw new NativeSqlException("too many active connections, please wait until connections are free!")
        }
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

    Map<String, Object> readCsvFile(MultipartFile tsvFile, String encoding) {
        //TODO [ticket=6315]
        InputStream fileContent = tsvFile.getInputStream()
        List<String> rows = fileContent.getText(encoding).split('\n')
        List<String> headerRow = rows.remove(0).split('\t')
        [headerRow: headerRow, rows: rows]
    }

    Map<String, Object> readExcelFile(MultipartFile excelFile) {
        //continue here with testing
        List<String> headerRow = []
        List<List<String>> rows = []
        XSSFWorkbook workbook = new XSSFWorkbook(excelFile.getInputStream())
        XSSFSheet sheet = workbook.getSheetAt(0)
        for(Cell cell in sheet.getRow(0).cellIterator()) {
            headerRow << cell.stringCellValue
        }
        boolean headerFlag = true
        for(Row row in sheet.rowIterator()) {
            if(headerFlag) {
                headerFlag = false
                continue
            }
            def value
            List<String> readRow = []
            for(Cell cell in row.cellIterator()) {
                switch(cell.getCellTypeEnum()) {
                    case CellType.NUMERIC: value = cell.numericCellValue
                        break
                    case CellType.STRING: value = cell.stringCellValue
                        break
                }
                readRow << value
            }
            rows << readRow
        }
        [headerRow: headerRow, rows: rows]
    }

    /**
     * Currently disused, method and procedure flow under development
     * Notifies a user that a background process (e.g. linking of a large package to many subscriptions) has been terminated
     * @param userId the user who triggered the process and is now going to be notified
     * @param cacheKey the cache entry under which the finish message is going to be saved
     * @param mess the message string being displayed
     */
    @UnstableFeature
    void notifyBackgroundProcessFinish(long userId, String cacheKey, String mess) {
        EhcacheWrapper cache = cacheService.getTTL1800Cache("finish_${userId}")
        cache.put(cacheKey, mess)
    }
}