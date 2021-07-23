package de.laser.stats

import de.laser.TitleInstancePackagePlatform
import de.laser.base.AbstractReport

class Counter4Report extends AbstractReport {

    TitleInstancePackagePlatform title

    static mapping = {
        id                  column: 'c4r_id', index: 'c4r_id_idx'
        version             column: 'c4r_version'
        title               column: 'c4r_title_fk', index: 'c4r_title_idx'
        publisher           column: 'c4r_publisher', type: 'text'
        platform            column: 'c4r_platform_fk'
        reportInstitution   column: 'c4r_report_institution_fk', index: 'c4r_ri_idx'
        reportType          column: 'c4r_report_type', index: 'c4r_rt_idx'
        year                column: 'c4r_year'
        month               column: 'c4r_month'
        count               column: 'c4r_count'
    }

    static constraints = {

    }

}
