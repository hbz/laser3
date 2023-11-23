package de.laser.reporting.export.base

/**
 * Interface keeping methods both for local and global query export
 */
abstract class BaseQueryExport {

    /**
     * Retrieves the queried report data from the respective cache
     * @return a {@link Map} containing the report headers with the respective data
     */
    abstract Map<String, Object> getQueriedData()
}
