package de.laser.stats

import de.laser.Org
import de.laser.Platform
import de.laser.exceptions.CreationException

class LaserStatsCursor {

    Platform platform
    Org customer
    String reportID
    Date latestFrom
    Date latestTo
    SortedSet<StatsMissingPeriod> missingPeriods

    static hasMany = {
        missingPeriods:     StatsMissingPeriod
    }

    static mapping = {
        id                  column: 'lsc_id', index: 'lsc_id_idx'
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
