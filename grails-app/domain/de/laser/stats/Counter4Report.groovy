package de.laser.stats

import de.laser.TitleInstancePackagePlatform
import de.laser.base.AbstractReport
import de.laser.exceptions.CreationException
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

class Counter4Report extends AbstractReport {

    String category
    static Log static_logger = LogFactory.getLog(Counter4Report)

    static mapping = {
        id                  column: 'c4r_id', index: 'c4r_id_idx'
        version             column: 'c4r_version'
        title               column: 'c4r_title_fk', index: 'c4r_title_idx'
        publisher           column: 'c4r_publisher', type: 'text'
        platform            column: 'c4r_platform_fk'
        reportInstitution   column: 'c4r_report_institution_fk', index: 'c4r_ri_idx'
        reportType          column: 'c4r_report_type', index: 'c4r_rt_idx'
        category            column: 'c4r_category'
        metricType          column: 'c4r_metric_type'
        reportFrom          column: 'c4r_report_from'
        reportTo            column: 'c4r_report_to'
        reportCount         column: 'c4r_report_count'
    }

    static constraints = {
        title(unique: ['reportType', 'platform', 'reportInstitution', 'metricType', 'reportFrom', 'reportTo'])
    }

    static Counter4Report construct(Map<String, Object> configMap) throws CreationException {
        Counter4Report c4report
        //is to save performance
        boolean changed = false
        if(configMap.incremental) {
            List<Counter4Report> check = Counter4Report.executeQuery('select c4r from Counter4Report c4r where c4r.reportInstitution = :reportInstitution and c4r.title = :title and c4r.platform = :platform and c4r.reportType = :reportType and c4r.reportFrom = :reportFrom and c4r.reportTo = :reportTo and c4r.category = :category and c4r.metricType = :metricType',
                    [reportInstitution: configMap.reportInstitution,
                     title: configMap.title,
                     platform: configMap.platform,
                     reportType: configMap.reportType,
                     reportFrom: configMap.reportFrom,
                     reportTo: configMap.reportTo,
                     category: configMap.category,
                     metricType: configMap.metricType]
            )
            if(check) {
                c4report = check[0]
            }
        }
        if(c4report == null) {
            c4report = new Counter4Report(configMap)
            c4report.title = configMap.title as TitleInstancePackagePlatform
            changed = true
        }
        if(c4report.publisher != configMap.publisher) {
            c4report.publisher = configMap.publisher
            changed = true
        }
        if(c4report.reportCount != configMap.reportCount) {
            c4report.reportCount = configMap.reportCount
            changed = true
        }
        if(changed) {
            if(!c4report.save())
                throw new CreationException("error on creating counter 4 report: ${c4report.errors.getAllErrors().toListString()}")
        }
        else static_logger.debug("no change registered for ${c4report.reportInstitution}/${c4report.title.name}/${c4report.reportFrom}/${c4report.reportTo}")
        c4report
    }

}
