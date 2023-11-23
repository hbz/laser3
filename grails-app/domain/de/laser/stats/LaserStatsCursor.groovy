package de.laser.stats

import de.laser.Org
import de.laser.Platform
import de.laser.exceptions.CreationException

/**
 * This keeps track for each customer {@link Org}, {@link Platform} and report ID when the latest fetched report is dating.
 * It may refer to {@link StatsMissingPeriod}s; those are (currently unused) container objects to mark for which months there is no data available for which customer/platform/report combination.
 * @deprecated Disused as data is not saved in LAS:eR but retrieved in real-time from the provider
 */
@Deprecated
class LaserStatsCursor {

    Platform platform
    Org customer
    String reportID
    Date latestFrom
    Date latestTo

    static hasMany = {
        missingPeriods:     StatsMissingPeriod
    }

    static mapping = {
        id                  column: 'lsc_id'
        version             column: 'lsc_version'
        platform            column: 'lsc_platform_fk'
        customer            column: 'lsc_customer_fk'
        reportID            column: 'lsc_report_id', index: 'lsc_report_idx'
        latestFrom          column: 'lsc_latest_from_date', index: 'lsc_from_idx'
        latestTo            column: 'lsc_latest_to_date', index: 'lsc_to_idx'
    }

    static constraints = {
        reportID(unique: ['platform', 'customer'])
    }

    /**
     * Was used to set up a cursor for a report ID, customer {@link Org} and {@link Platform}
     */
    static LaserStatsCursor construct(Map<String, Object> configMap) throws CreationException {
        LaserStatsCursor result = LaserStatsCursor.findByPlatformAndCustomerAndReportID(configMap.platform, configMap.customer, configMap.reportID)
        if(!result)
            result = new LaserStatsCursor(platform: configMap.platform, customer: configMap.customer, reportID: configMap.reportID)
        result.latestFrom = configMap.latestFrom
        result.latestTo = configMap.latestTo
        if(!result.save())
            throw new CreationException(result.getErrors().getAllErrors().toListString())
        result
    }
}
