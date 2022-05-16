package de.laser.stats

import de.laser.TitleInstancePackagePlatform
import de.laser.base.AbstractReport
import de.laser.exceptions.CreationException
import groovy.util.logging.Slf4j

/**
 * A COUNTER report representation according to the COUNTER format, revision 4 (https://www.projectcounter.org/code-of-practice-sections/usage-reports/)
 */
@Slf4j
class Counter4Report extends AbstractReport {

    /**
     * These are the header parameters for each COUNTER 4 report
     */
    static enum EXPORTS {
        JR1 (['Journal Report 1 (R4)', 'Number of Successful Full-Text Article Requests by Month and Journal'] as LinkedHashSet<String>),
        JR1GOA (['Journal Report 1 GOA (R4)', 'Number of Successful Gold Open Access Full-Text Article Requests by Month and Journal'] as LinkedHashSet<String>),
        JR2 (['Journal Report 2 (R4)', 'Access Denied to Full-Text Articles by Month, Journal and Category'] as LinkedHashSet<String>),
        JR3 (['Journal Report 3 (R4)', 'Number of Successful Item Requests by Month, Journal and Page-type'] as LinkedHashSet<String>),
        JR4 (['Journal Report 4 (R4)', 'Total Searches Run By Month and Collection'] as LinkedHashSet<String>),
        JR5 (['Journal Report 5 (R4)', 'Number of Successful Full-Text Article Requests by Year-of-Publication (YOP) and Journal'] as LinkedHashSet<String>),
        DR1 (['Database Report 1 (R4)', 'Total Searches, Result Clicks and Record Views by Month and Database'] as LinkedHashSet<String>),
        DR2 (['Database Report 2 (R4)', 'Access Denied by Month, Database and Category'] as LinkedHashSet<String>),
        PR1 (['Platform Report 1 (R4)', 'Total Searches, Result Clicks and Record Views by Month and Platform'] as LinkedHashSet<String>),
        BR1 (['Book Report 1 (R4)', 'Number of Successful Title Requests by Month and Title'] as LinkedHashSet<String>),
        BR2 (['Book Report 2 (R4)', 'Number of Successful Section Requests by Month and Title'] as LinkedHashSet<String>),
        BR3 (['Book Report 3 (R4)', 'Access Denied to Content Items by Month, Title and Category'] as LinkedHashSet<String>),
        BR4 (['Book Report 4 (R4)', 'Access Denied to Content Items by Month, Platform and Category'] as LinkedHashSet<String>),
        BR5 (['Book Report 5 (R4)', 'Total Searches by Month and Title'] as LinkedHashSet<String>)

        EXPORTS(LinkedHashSet<String> header) {
            this.header = header
        }

        public LinkedHashSet<String> header
    }

    /**
     * This are the column headers which are mandatory for the respective COUNTER 4 report
     */
    static enum COLUMN_HEADERS {
        JR1 (['Journal', 'Publisher', 'Platform', 'Journal DOI', 'Proprietary Identifier', 'Print ISSN', 'Online ISSN', 'Reporting Period Total', 'Reporting Period HTML', 'Reporting Period PDF'] as LinkedHashSet<String>),
        JR1GOA (['Journal', 'Publisher', 'Platform', 'Journal DOI', 'Proprietary Identifier', 'Print ISSN', 'Online ISSN', 'Reporting Period Total', 'Reporting Period HTML', 'Reporting Period PDF'] as LinkedHashSet<String>),
        JR2 (['Journal', 'Publisher', 'Platform', 'Journal DOI', 'Proprietary Identifier', 'Print ISSN', 'Online ISSN', 'Reporting Period Total', 'Access Denied Category'] as LinkedHashSet<String>),
        JR3 (['Journal', 'Publisher', 'Platform', 'Journal DOI', 'Proprietary Identifier', 'Print ISSN', 'Online ISSN', 'Page type', 'Reporting Period Total'] as LinkedHashSet<String>),
        JR4 ([' ', ' ', 'Reporting Period Total'] as LinkedHashSet<String>),
        JR5 (['Journal', 'Publisher', 'Platform', 'Journal DOI', 'Proprietary Identifier', 'Print ISSN', 'Online ISSN', 'Articles in Press'] as LinkedHashSet<String>),
        DR1 (['Database', 'Publisher', 'Platform', 'User Activity', 'Reporting Period Total'] as LinkedHashSet<String>),
        DR2 (['Database', 'Publisher', 'Platform', 'Access denied category', 'Reporting Period Total'] as LinkedHashSet<String>),
        PR1 (['Platform', 'Publisher', 'User Activity', 'Reporting Period Total'] as LinkedHashSet<String>),
        BR1 ([' ', 'Publisher', 'Platform', 'Book DOI', 'Proprietary Identifier', 'ISBN', 'ISSN', 'Reporting Period Total'] as LinkedHashSet<String>),
        BR2 ([' ', 'Publisher', 'Platform', 'Book DOI', 'Proprietary Identifier', 'ISBN', 'ISSN', 'Reporting Period Total'] as LinkedHashSet<String>),
        BR3 ([' ', 'Publisher', 'Platform', 'Book DOI', 'Proprietary Identifier', 'ISBN', 'ISSN', 'Access Denied Category', 'Reporting Period Total'] as LinkedHashSet<String>),
        BR4 ([' ', 'Publisher', 'Platform', 'Proprietary Identifier', 'Access Denied Category', 'Reporting Period Total'] as LinkedHashSet<String>),
        BR5 ([' ', 'Publisher', 'Platform', 'Book DOI', 'Proprietary Identifier', 'ISBN', 'ISSN', 'User activity', 'Reporting Period Total'] as LinkedHashSet<String>)

        COLUMN_HEADERS(LinkedHashSet<String> headers) {
            this.headers = headers
        }

        public LinkedHashSet<String> headers
    }

    String category

    static mapping = {
        id                  column: 'c4r_id', index: 'c4r_id_idx'
        version             column: 'c4r_version'
        title               column: 'c4r_title_fk', index: 'c4r_title_idx, c4r_report_when_idx'
        publisher           column: 'c4r_publisher', type: 'text'
        platform            column: 'c4r_platform_fk', index: 'c4r_plat_idx'
        reportInstitution   column: 'c4r_report_institution_fk', index: 'c4r_ri_idx, c4r_report_when_idx'
        reportType          column: 'c4r_report_type', index: 'c4r_rt_idx, c4r_report_when_idx'
        category            column: 'c4r_category'
        metricType          column: 'c4r_metric_type', index: 'c4r_metric_type_idx, c4r_report_when_idx'
        reportFrom          column: 'c4r_report_from', index: 'c4r_report_from_idx, c4r_report_when_idx' //for JR5, this will be the start of YOP
        reportTo            column: 'c4r_report_to', index: 'c4r_report_to_idx, c4r_report_when_idx' //for JR5, this will be the end of YOP
        reportCount         column: 'c4r_report_count'
    }

    static constraints = {
        title               (nullable: true) //because of platform reports!
        title(unique: ['reportType', 'platform', 'reportInstitution', 'metricType', 'reportFrom', 'reportTo'])
    }

    /**
     * Was implemented to create reports by GORM; as this has proven very unperformant, COUNTER reports are now inserted by native SQL. See StatsSyncService for that.
     * @see de.laser.StatsSyncService
     */
    @Deprecated
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
        else log.debug("no change registered for ${c4report.reportInstitution}/${c4report.title.name}/${c4report.reportFrom}/${c4report.reportTo}")
        c4report
    }

}
