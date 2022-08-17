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

    static final String JOURNAL_REPORT_1        = "JR1"
    static final String JOURNAL_REPORT_1_GOA    = "JR1GOA"
    static final String JOURNAL_REPORT_2        = "JR2"
    //JR3-4 are optional
    static final String JOURNAL_REPORT_5        = "JR5"
    static final String DATABASE_REPORT_1       = "DR1"
    static final String DATABASE_REPORT_2       = "DR2"
    static final String PLATFORM_REPORT_1       = "PR1"
    static final String BOOK_REPORT_1           = "BR1"
    static final String BOOK_REPORT_2           = "BR2"
    //BR3-4 are optional
    static final String BOOK_REPORT_3           = "BR3"
    static final String BOOK_REPORT_4           = "BR4"
    static final String BOOK_REPORT_5           = "BR5"
    /**
     * ex Counter4ApiSource; these are the report types supported by COUNTER Revision 4
     */
    static List<String> COUNTER_4_TITLE_REPORTS = [JOURNAL_REPORT_1, JOURNAL_REPORT_1_GOA, JOURNAL_REPORT_2, JOURNAL_REPORT_5,
                                                   DATABASE_REPORT_1, DATABASE_REPORT_2,
                                                   BOOK_REPORT_1, BOOK_REPORT_2, BOOK_REPORT_3, BOOK_REPORT_4, BOOK_REPORT_5]
    static List<String> COUNTER_4_REPORTS       = COUNTER_4_TITLE_REPORTS+PLATFORM_REPORT_1

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
        datasource           'storage'
        id                   column: 'c4r_id'
        version              column: 'c4r_version'
        titleUID             column: 'c4r_title_guid', index: 'c4r_title_idx, c4r_report_when_idx'
        publisher            column: 'c4r_publisher', type: 'text'
        platformUID          column: 'c4r_platform_guid', index: 'c4r_plat_idx'
        reportInstitutionUID column: 'c4r_report_institution_guid', index: 'c4r_ri_idx, c4r_report_when_idx'
        reportType           column: 'c4r_report_type', index: 'c4r_rt_idx, c4r_report_when_idx'
        category             column: 'c4r_category'
        metricType           column: 'c4r_metric_type', index: 'c4r_metric_type_idx, c4r_report_when_idx'
        reportFrom           column: 'c4r_report_from', index: 'c4r_report_from_idx, c4r_report_when_idx' //for JR5, this will be the start of YOP
        reportTo             column: 'c4r_report_to', index: 'c4r_report_to_idx, c4r_report_when_idx' //for JR5, this will be the end of YOP
        reportCount          column: 'c4r_report_count'
    }

    static constraints = {
        titleUID             (nullable: true) //because of platform reports!
        publisher            (nullable: true) //because of platform reports!
        title(unique: ['reportType', 'platform', 'reportInstitution', 'metricType', 'reportFrom', 'reportTo'])
    }

    static transients = ['title', 'platform', 'reportInstitution']


}
