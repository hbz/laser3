package de.laser.stats

import de.laser.base.AbstractReport
import de.laser.exceptions.CreationException
import groovy.util.logging.Slf4j

/**
 * A COUNTER report representation according to the COUNTER format, revision 5 (https://www.projectcounter.org/code-of-practice-five-sections/5-delivery-counter-reports/)
 */
@Slf4j
class Counter5Report extends AbstractReport {

    /**
     * These are the header parameters for each COUNTER 5 report
     */
    static enum EXPORT_HEADERS {
        PR ('Platform Master Report'),
        PR_P1 ('Platform Usage'),
        DR ('Database Master Report'),
        DR_D1 ('Database Search and Item Usage'),
        DR_D2 ('Database Access Denied'),
        TR ('Title Master Report'),
        TR_B1 ('Book Requests (Excluding OA_Gold)'),
        TR_B2 ('Book Access Denied'),
        TR_B3 ('Book Usage by Access Type'),
        TR_J1 ('Journal Requests (Excluding OA_Gold)'),
        TR_J2 ('Journal Access Denied'),
        TR_J3 ('Journal Usage by Access Type'),
        TR_J4 ('Journal Requests by YOP (Excluding OA_Gold)'),
        IR ('Item Master Report'),
        IR_A1 ('Journal Article Requests'),
        IR_M1 ('Multimedia Item Requests')

        EXPORT_HEADERS(String header) {
            this.header = header
        }

        String header
    }

    /**
     * These are the filter lists being displayed on the COUNTER 5 export
     */
    static enum EXPORT_CONTROLLED_LISTS {
        PR ('as selected', 'as selected', 'as selected'),
        PR_P1 ('Searches_Platform; Total_Item_Requests; Unique_Item_Requests; Unique_Title_Requests', 'Access_Method=Regular', ''), //when multiple platforms are being supported, consider: If a Platform filter is used (see Section 3.3.8 for details), it MUST be included in Report_Filters.
        DR ('as selected', 'as selected', 'as selected'),
        DR_D1 ('Searches_Automated; Searches_Federated; Searches_Regular; Total_Item_Investigations; Total_Item_Requests', 'Access_Method=Regular', ''),
        DR_D2 ('Limit_Exceeded; No_License', 'Access_Method=Regular', ''),
        TR ('as selected', 'as selected', 'as selected'),
        TR_B1 ('Total_Item_Requests; Unique_Title_Requests', 'Data_Type=Book; Access_Type=Controlled; Access_Method=Regular', ''),
        TR_B2 ('Limit_Exceeded; No_License', 'Data_Type=Book; Access_Method=Regular', ''),
        TR_B3 ('Total_Item_Investigations; Total_Item_Requests; Unique_Item_Investigations; Unique_Item_Requests; Unique_Title_Investigations; Unique_Title_Requests', 'Data_Type=Book; Access_Method=Regular',''),
        TR_J1 ('Total_Item_Requests; Unique_Item_Requests', 'Data_Type=Journal; Access_Type=Controlled; Access_Method=Regular', ''),
        TR_J2 ('Limit_Exceeded; No_License', 'Data_Type=Journal; Access_Method=Regular', ''),
        TR_J3 ('Total_Item_Investigations; Total_Item_Requests; Unique_Item_Investigations; Unique_Item_Requests', 'Data_Type=Journal; Access_Method=Regular' , ''),
        TR_J4 ('Total_Item_Requests; Unique_Item_Requests', 'Data_Type=Journal; Access_Type=Controlled; Access_Method=Regular', ''),
        IR ('as selected', 'as selected', 'as selected'),
        IR_A1 ('Total_Item_Requests; Unique_Items_Requests', 'Data_Type=Article; Parent_Data_Type=Journal; Access_Method=Regular', ''),
        IR_M1 ('Total_Item_Requests', 'Data_Type=Multimedia; Access_Method=Regular', '')

        EXPORT_CONTROLLED_LISTS(String metricTypes, String reportFilters, String reportAttributes) {
            this.metricTypes = metricTypes
            this.reportFilters = reportFilters
            this.reportAttributes = reportAttributes
        }

        String metricTypes
        String reportFilters
        String reportAttributes
    }

    /**
     * These are the column headers which are mandatory for the respective COUNTER 5 report
     */
    static enum COLUMN_HEADERS {
        PR (['Platform', 'Metric_Type', 'Reporting_Period_Total'] as LinkedHashSet<String>),
        PR_P1 (['Platform', 'Metric_Type', 'Reporting_Period_Total'] as LinkedHashSet<String>),
        DR (['Database', 'Publisher', 'Publisher_ID', 'Platform', 'Proprietary_ID', 'Metric_Type', 'Reporting_Period_Total'] as LinkedHashSet<String>),
        DR_D1 (['Database', 'Publisher', 'Publisher_ID', 'Platform', 'Proprietary_ID', 'Metric_Type', 'Reporting_Period_Total'] as LinkedHashSet<String>),
        DR_D2 (['Database', 'Publisher', 'Publisher_ID', 'Platform', 'Proprietary_ID', 'Metric_Type', 'Reporting_Period_Total'] as LinkedHashSet<String>),
        TR (['Title', 'Publisher', 'Publisher_ID', 'Platform', 'DOI', 'Proprietary_ID', 'ISBN', 'Print_ISSN', 'Online_ISSN', 'URI', 'Metric_Type', 'Reporting_Period_Total'] as LinkedHashSet<String>),
        TR_B1 (['Title', 'Publisher', 'Publisher_ID', 'Platform', 'DOI', 'Proprietary_ID', 'ISBN', 'Print_ISSN', 'Online_ISSN', 'URI', 'YOP', 'Metric_Type', 'Reporting_Period_Total'] as LinkedHashSet<String>),
        TR_B2 (['Title', 'Publisher', 'Publisher_ID', 'Platform', 'DOI', 'Proprietary_ID', 'ISBN', 'Print_ISSN', 'Online_ISSN', 'URI', 'YOP', 'Metric_Type', 'Reporting_Period_Total'] as LinkedHashSet<String>),
        TR_B3 (['Title', 'Publisher', 'Publisher_ID', 'Platform', 'DOI', 'Proprietary_ID', 'ISBN', 'Print_ISSN', 'Online_ISSN', 'URI', 'YOP', 'Access_Type', 'Metric_Type', 'Reporting_Period_Total'] as LinkedHashSet<String>),
        TR_J1 (['Title', 'Publisher', 'Publisher_ID', 'Platform', 'DOI', 'Proprietary_ID', 'Print_ISSN', 'Online_ISSN', 'URI', 'Metric_Type', 'Reporting_Period_Total'] as LinkedHashSet<String>),
        TR_J2 (['Title', 'Publisher', 'Publisher_ID', 'Platform', 'DOI', 'Proprietary_ID', 'Print_ISSN', 'Online_ISSN', 'URI', 'Metric_Type', 'Reporting_Period_Total'] as LinkedHashSet<String>),
        TR_J3 (['Title', 'Publisher', 'Publisher_ID', 'Platform', 'DOI', 'Proprietary_ID', 'Print_ISSN', 'Online_ISSN', 'URI', 'Access_Type', 'Metric_Type', 'Reporting_Period_Total'] as LinkedHashSet<String>),
        TR_J4 (['Title', 'Publisher', 'Publisher_ID', 'Platform', 'DOI', 'Proprietary_ID', 'Print_ISSN', 'Online_ISSN', 'URI', 'YOP', 'Metric_Type', 'Reporting_Period_Total'] as LinkedHashSet<String>),
        IR (['Item', 'Publisher', 'Publisher_ID', 'Platform', 'DOI', 'Proprietary_ID', 'ISBN', 'Print_ISSN', 'Online_ISSN', 'URI', 'Metric_Type', 'Reporting_Period_Total'] as LinkedHashSet<String>),
        IR_A1 (['Item', 'Publisher', 'Publisher_ID', 'Platform', 'Authors', 'Publication_Date', 'Article_Version', 'DOI', 'Proprietary_ID', 'Print_ISSN', 'Online_ISSN', 'URI', 'Parent_Title', 'Parent_Authors', 'Parent_Article_Version', 'Parent_DOI', 'Parent_Proprietary_ID', 'Parent_Print_ISSN', 'Parent_Print_ISBN', 'Parent_URI', 'Access_Type', 'Metric_Type', 'Reporting_Period_Total'] as LinkedHashSet<String>),
        IR_M1 (['Item', 'Publisher', 'Publisher_ID', 'Platform', 'DOI', 'Proprietary_ID', 'URI', 'Metric_Type', 'Reporting_Period_Total'] as LinkedHashSet<String>)

        COLUMN_HEADERS(LinkedHashSet<String> headers) {
            this.headers = headers
        }

        public LinkedHashSet<String> headers
    }

    String accessType
    String accessMethod

    static mapping = {
        id                  column: 'c5r_id', index: 'c5r_id_idx'
        version             column: 'c5r_version'
        title               column: 'c5r_title_fk', index: 'c5r_title_idx, c5r_report_when_idx'
        publisher           column: 'c5r_publisher', type: 'text'
        platform            column: 'c5r_platform_fk', index: 'c5r_plat_idx'
        reportInstitution   column: 'c5r_report_institution_fk', index: 'c5r_ri_idx, c5r_report_when_idx'
        reportType          column: 'c5r_report_type', index: 'c5r_rt_idx, c5r_report_when_idx'
        accessType          column: 'c5r_access_type', index: 'c5r_access_type_idx'
        accessMethod        column: 'c5r_access_method', index: 'c5r_access_method_idx'
        metricType          column: 'c5r_metric_type', index: 'c5r_metric_type_idx, c5r_report_when_idx'
        reportFrom          column: 'c5r_report_from', index: 'c5r_report_from_idx, c5r_report_when_idx'
        reportTo            column: 'c5r_report_to', index: 'c5r_report_to_idx, c5r_report_when_idx'
        reportCount         column: 'c5r_report_count'
    }

    static constraints = {
        title               (nullable: true) //because of platform reports!
        publisher           (nullable: true, blank: false) //because of platform reports!
        accessType          (nullable: true, blank: false)
        accessMethod        (nullable: true, blank: false)
        title(unique: ['platform', 'reportInstitution', 'metricType', 'reportFrom', 'reportTo', 'reportType'])
    }

    /**
     * Was implemented to create reports by GORM; as this has proven very unperformant, COUNTER reports are now inserted by native SQL. See StatsSyncService for that.
     * @see de.laser.StatsSyncService
     */
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
            log.debug("no change registered for ${c5report.reportInstitution}/${entityName}/${c5report.reportFrom}/${c5report.reportTo}")
        }
        c5report
    }
}
