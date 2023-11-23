package de.laser.stats

import de.laser.Org
import de.laser.Platform

/**
 * This class was originally designed for retain missing periods of data and is now used
 * as temporary entity to mark periods which could not be retrieved due to a server fault and whose
 * request should be retried later
 * @deprecated Disused as data is not saved in LAS:eR but retrieved in real-time from the provider
 */
@Deprecated
class StatsMissingPeriod implements Comparable {

    Platform platform
    Org customer
    String reportID
    Date from
    Date to

    static mapping = {
        id              column: 'smp_id'
        version         column: 'smp_version'
        customer        column: 'smp_customer_fk'
        platform        column: 'smp_platform_fk'
        reportID        column: 'smp_report_id', index: 'smp_report_idx'
        from            column: 'smp_from_date', index: 'smp_from_idx'
        to              column: 'smp_to_date', index: 'smp_to_idx'
    }

    static constraints = {
        reportID(unique: ['from', 'to', 'customer', 'platform'])
    }

    @Override
    int compareTo(Object o) {
        StatsMissingPeriod period2 = (StatsMissingPeriod) o
        int result
        result = from.compareTo(period2.from)
        if(result == 0)
            result = to.compareTo(period2.to)
        if(result == 0)
            result = cursor.reportID.compareTo(period2.cursor.reportID)
        result
    }
}
