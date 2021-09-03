package de.laser.stats

import de.laser.base.AbstractReport
import de.laser.exceptions.CreationException
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory


class Counter5Report extends AbstractReport {

    String accessType
    String accessMethod
    static Log static_logger = LogFactory.getLog(Counter5Report)

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
        reportFrom          column: 'c5r_report_from'
        reportTo            column: 'c5r_report_to'
        reportCount         column: 'c5r_report_count'
    }

    static constraints = {
        title               (nullable: true) //because of platform reports!
        publisher           (nullable: true, blank: false) //because of platform reports!
        accessType          (nullable: true, blank: false)
        accessMethod        (nullable: true, blank: false)
        title(unique: ['platform', 'reportInstitution', 'metricType', 'reportFrom', 'reportTo', 'reportType'])
    }

    static Counter5Report construct(Map<String, Object> configMap) throws CreationException {
        Counter5Report c5report
        if(configMap.title)
            c5report = Counter5Report.findByReportInstitutionAndTitleAndPlatformAndReportTypeAndReportFromAndReportToAndMetricType(
                    configMap.reportInstitution, configMap.title, configMap.platform, configMap.reportType, configMap.reportFrom, configMap.reportTo, configMap.metricType
            )
        else c5report = Counter5Report.findByReportInstitutionAndPlatformAndReportTypeAndReportFromAndReportToAndMetricType(
                configMap.reportInstitution, configMap.platform, configMap.reportType, configMap.reportFrom, configMap.reportTo, configMap.metricType
        )
        boolean changed = false
        if(!c5report) {
            c5report = new Counter5Report(configMap)
            changed = true
        }
        if(c5report.publisher != configMap.publisher) {
            c5report.publisher = configMap.publisher
            changed = true
        }
        if(c5report.accessType != configMap.accessType) {
            c5report.accessType = configMap.accessType
            changed = true
        }
        if(c5report.accessMethod != configMap.accessMethod) {
            c5report.accessMethod = configMap.accessMethod
            changed = true
        }
        if(c5report.metricType != configMap.metricType) {
            c5report.metricType = configMap.metricType
            changed = true
        }
        if(c5report.reportCount != configMap.reportCount) {
            c5report.reportCount = configMap.reportCount
            changed = true
        }
        if(changed) {
            if(!c5report.save()) {
                throw new CreationException("error on creating counter 5 report: ${c5report.errors.getAllErrors().toListString()}")
            }
        }
        else {
            String entityName = c5report.title ? c5report.title.name : c5report.platform.name
            static_logger.debug("no change registered for ${c5report.reportInstitution}/${entityName}/${c5report.reportFrom}/${c5report.reportTo}")
        }
        c5report
    }
}
