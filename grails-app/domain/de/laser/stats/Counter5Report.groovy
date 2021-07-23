package de.laser.stats

import de.laser.TitleInstancePackagePlatform
import de.laser.base.AbstractReport

class Counter5Report extends AbstractReport {

    TitleInstancePackagePlatform title
    String accessType
    String accessMethod
    String metricType

    static mapping = {
        id                  column: 'c5r_id', index: 'c5r_id_idx'
        version             column: 'c5r_version'
        title               column: 'c5r_title_fk', index: 'c5r_title_idx'
        publisher           column: 'c5r_publisher', type: 'text'
        platform            column: 'c5r_platform_fk'
        reportInstitution   column: 'c5r_report_institution_fk', index: 'c5r_ri_idx'
        reportType          column: 'c5r_report_type', index: 'c5r_rt_idx'
        accessType          column: 'c5r_access_type'
        accessMethod        column: 'c5r_access_method'
        metricType          column: 'c5r_metric_type'
        year                column: 'c5r_year'
        month               column: 'c5r_month'
        count               column: 'c5r_count'
    }

    static constraints = {
        accessMethod        (nullable: true, blank: false)
    }

}
