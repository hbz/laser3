package de.laser.stats

import de.laser.base.AbstractReport
import groovy.util.logging.Slf4j

/**
 * A COUNTER report representation according to the COUNTER format, revision 5 (https://www.projectcounter.org/code-of-practice-five-sections/5-delivery-counter-reports/)
 */
@Slf4j
class Counter5Report extends AbstractReport {

    static final String PLATFORM_MASTER_REPORT          = "pr"
    static final String PLATFORM_USAGE                  = "pr_p1"
    static final String DATABASE_MASTER_REPORT          = "dr"
    static final String DATABASE_SEARCH_AND_ITEM_USAGE  = "dr_d1"
    static final String DATABASE_ACCESS_DENIED          = "dr_d2"
    static final String TITLE_MASTER_REPORT             = "tr"
    static final String BOOK_REQUESTS                   = "tr_b1"
    static final String BOOK_ACCESS_DENIED              = "tr_b2"
    static final String BOOK_USAGE_BY_ACCESS_TYPE       = "tr_b3"
    static final String JOURNAL_REQUESTS                = "tr_j1"
    static final String JOURNAL_ACCESS_DENIED           = "tr_j2"
    static final String JOURNAL_USAGE_BY_ACCESS_TYPE    = "tr_j3"
    static final String JOURNAL_REQUESTS_BY_YOP         = "tr_j4"
    static final String ITEM_MASTER_REPORT              = "ir"
    static final String JOURNAL_ARTICLE_REQUESTS        = "ir_a1"
    static final String MULTIMEDIA_ITEM_REQUESTS        = "ir_m1"
    /**
     * ex Counter5ApiSource, these are the report types supported by COUNTER Revision 5
     */
    static List<String> COUNTER_5_TITLE_REPORTS         = [TITLE_MASTER_REPORT, BOOK_REQUESTS, BOOK_ACCESS_DENIED, BOOK_USAGE_BY_ACCESS_TYPE, JOURNAL_REQUESTS, JOURNAL_ACCESS_DENIED, JOURNAL_USAGE_BY_ACCESS_TYPE, JOURNAL_REQUESTS_BY_YOP,
                                                           ITEM_MASTER_REPORT, JOURNAL_ARTICLE_REQUESTS, MULTIMEDIA_ITEM_REQUESTS]
    static List<String> COUNTER_5_PLATFORM_REPORTS      = [PLATFORM_MASTER_REPORT, PLATFORM_USAGE]
    static List<String> COUNTER_5_DATABASE_REPORTS      = [DATABASE_MASTER_REPORT, DATABASE_SEARCH_AND_ITEM_USAGE, DATABASE_ACCESS_DENIED]
    static List<String> COUNTER_5_REPORTS               = COUNTER_5_TITLE_REPORTS+COUNTER_5_PLATFORM_REPORTS+COUNTER_5_DATABASE_REPORTS


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
        TR_J1 ('Total_Item_Requests; Unique_Item_Requests', 'Data_Type=Journal; Access_Type=Controlled, Others_Free_To_Read; Access_Method=Regular', ''),
        TR_J2 ('Limit_Exceeded; No_License', 'Data_Type=Journal; Access_Method=Regular', ''),
        TR_J3 ('Total_Item_Investigations; Total_Item_Requests; Unique_Item_Investigations; Unique_Item_Requests', 'Data_Type=Journal; Access_Method=Regular' , ''),
        TR_J4 ('Total_Item_Requests; Unique_Item_Requests', 'Data_Type=Journal; Access_Type=Controlled, Others_Free_To_Read; Access_Method=Regular', ''),
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
        TR_B1 (['Title', 'Publisher', 'Publisher_ID', 'Platform', 'DOI', 'Proprietary_ID', 'ISBN', 'Print_ISSN', 'Online_ISSN', 'URI', 'YOP', 'Access_Type', 'Metric_Type', 'Reporting_Period_Total'] as LinkedHashSet<String>),
        TR_B2 (['Title', 'Publisher', 'Publisher_ID', 'Platform', 'DOI', 'Proprietary_ID', 'ISBN', 'Print_ISSN', 'Online_ISSN', 'URI', 'YOP', 'Access_Type', 'Metric_Type', 'Reporting_Period_Total'] as LinkedHashSet<String>),
        TR_B3 (['Title', 'Publisher', 'Publisher_ID', 'Platform', 'DOI', 'Proprietary_ID', 'ISBN', 'Print_ISSN', 'Online_ISSN', 'URI', 'YOP', 'Access_Type', 'Metric_Type', 'Reporting_Period_Total'] as LinkedHashSet<String>),
        TR_J1 (['Title', 'Publisher', 'Publisher_ID', 'Platform', 'DOI', 'Proprietary_ID', 'Print_ISSN', 'Online_ISSN', 'URI', 'YOP', 'Access_Type', 'Metric_Type', 'Reporting_Period_Total'] as LinkedHashSet<String>),
        TR_J2 (['Title', 'Publisher', 'Publisher_ID', 'Platform', 'DOI', 'Proprietary_ID', 'Print_ISSN', 'Online_ISSN', 'URI', 'YOP', 'Access_Type', 'Metric_Type', 'Reporting_Period_Total'] as LinkedHashSet<String>),
        TR_J3 (['Title', 'Publisher', 'Publisher_ID', 'Platform', 'DOI', 'Proprietary_ID', 'Print_ISSN', 'Online_ISSN', 'URI', 'YOP', 'Access_Type', 'Metric_Type', 'Reporting_Period_Total'] as LinkedHashSet<String>),
        TR_J4 (['Title', 'Publisher', 'Publisher_ID', 'Platform', 'DOI', 'Proprietary_ID', 'Print_ISSN', 'Online_ISSN', 'URI', 'YOP', 'Metric_Type', 'Reporting_Period_Total'] as LinkedHashSet<String>),
        IR (['Item', 'Publisher', 'Publisher_ID', 'Platform', 'DOI', 'Proprietary_ID', 'ISBN', 'Print_ISSN', 'Online_ISSN', 'URI', 'Metric_Type', 'Reporting_Period_Total'] as LinkedHashSet<String>),
        IR_A1 (['Item', 'Publisher', 'Publisher_ID', 'Platform', 'Authors', 'Publication_Date', 'Article_Version', 'DOI', 'Proprietary_ID', 'Print_ISSN', 'Online_ISSN', 'URI', 'Parent_Title', 'Parent_Authors', 'Parent_Article_Version', 'Parent_DOI', 'Parent_Proprietary_ID', 'Parent_Print_ISSN', 'Parent_Print_ISBN', 'Parent_URI', 'Access_Type', 'Metric_Type', 'Reporting_Period_Total'] as LinkedHashSet<String>),
        IR_M1 (['Item', 'Publisher', 'Publisher_ID', 'Platform', 'DOI', 'Proprietary_ID', 'URI', 'Metric_Type', 'Reporting_Period_Total'] as LinkedHashSet<String>)

        COLUMN_HEADERS(LinkedHashSet<String> headers) {
            this.headers = headers
        }

        public LinkedHashSet<String> headers
    }

    static enum METRIC_TYPES {
        PR (['Searches_Platform', 'Total_Item_Investigations', 'Total_Item_Requests', 'Unique_Item_Investigations', 'Unique_Item_Requests', 'Unique_Title_Investigations', 'Unique_Title_Requests'] as LinkedHashSet<String>),
        //PR_P1 (['Searches_Platform', 'Total_Item_Requests', 'Unique_Item_Requests', 'Unique_Title_Requests'] as LinkedHashSet<String>),
        DR (['Searches_Automated', 'Searches_Federated', 'Searches_Regular', 'Total_Item_Investigations', 'Total_Item_Requests', 'Unique_Item_Investigations', 'Unique_Item_Requests', 'Unique_Title_Investigations', 'Unique_Title_Requests', 'Limit_Exceeded', 'No_License'] as LinkedHashSet<String>),
        /*
        DR_D1 (['Searches_Automated', 'Searches_Federated', 'Searches_Regular', 'Total_Item_Investigations', 'Total_Item_Requests'] as LinkedHashSet<String>),
        DR_D2 (['Limit_Exceeded', 'No_License'] as LinkedHashSet<String>),
        */
        TR (['Total_Item_Investigations', 'Total_Item_Requests', 'Unique_Item_Investigations', 'Unique_Item_Requests', 'Unique_Title_Investigations', 'Unique_Title_Requests', 'Limit_Exceeded', 'No_License'] as LinkedHashSet<String>),
        /*
        TR_B1 (['Total_Item_Requests', 'Unique_Item_Requests'] as LinkedHashSet<String>),
        TR_B2 (['Limit_Exceeded', 'No_License'] as LinkedHashSet<String>),
        TR_B3 (['Total_Item_Investigations', 'Total_Item_Requests', 'Unique_Item_Investigations', 'Unique_Item_Requests', 'Unique_Title_Investigations', 'Unique_Title_Requests'] as LinkedHashSet<String>),
        TR_J1 (['Total_Item_Requests', 'Unique_Item_Requests'] as LinkedHashSet<String>),
        TR_J2 (['Limit_Exceeded', 'No_License'] as LinkedHashSet<String>),
        TR_J3 (['Total_Item_Investigations', 'Total_Item_Requests', 'Unique_Item_Investigations', 'Unique_Item_Requests'] as LinkedHashSet<String>),
        TR_J4 (['Total_Item_Requests', 'Unique_Item_Requests'] as LinkedHashSet<String>),
        */
        IR (['Total_Item_Investigations', 'Total_Item_Requests', 'Unique_Item_Investigations', 'Unique_Item_Requests', 'Limit_Exceeded', 'No_License'] as LinkedHashSet<String>)
        /*
        IR_A1 (['Total_Item_Requests', 'Unique_Item_Requests'] as LinkedHashSet<String>),
        IR_M1 (['Total_Item_Requests'] as LinkedHashSet<String>)
        */

        METRIC_TYPES(LinkedHashSet<String> metricTypes) {
            this.metricTypes = metricTypes
        }

        public LinkedHashSet<String> metricTypes
    }

    static enum ACCESS_METHODS {
        PR (['Regular', 'TDM'] as LinkedHashSet<String>),
        //PR_P1 (['Regular'] as LinkedHashSet<String>),
        DR (['Regular', 'TDM'] as LinkedHashSet<String>),
        /*
        DR_D1 (['Regular'] as LinkedHashSet<String>),
        DR_D2 (['Regular'] as LinkedHashSet<String>),
        */
        TR (['Regular', 'TDM'] as LinkedHashSet<String>),
        /*
        TR_B1 (['Regular'] as LinkedHashSet<String>),
        TR_B2 (['Regular'] as LinkedHashSet<String>),
        TR_B3 (['Regular'] as LinkedHashSet<String>),
        TR_J1 (['Regular'] as LinkedHashSet<String>),
        TR_J2 (['Regular'] as LinkedHashSet<String>),
        TR_J3 (['Regular'] as LinkedHashSet<String>),
        TR_J4 (['Regular'] as LinkedHashSet<String>),
        */
        IR (['Regular', 'TDM'] as LinkedHashSet<String>)
        /*
        IR_A1 (['Regular'] as LinkedHashSet<String>),
        IR_M1 (['Regular'] as LinkedHashSet<String>)
        */

        ACCESS_METHODS(LinkedHashSet<String> accessMethods) {
            this.accessMethods = accessMethods
        }

        public LinkedHashSet<String> accessMethods
    }

    static enum ACCESS_TYPES {
        TR (['Controlled', 'OA_Gold'] as LinkedHashSet<String>),
        /*
        TR_B1 (['Controlled'] as LinkedHashSet<String>),
        TR_J1 (['Controlled'] as LinkedHashSet<String>),
        TR_J4 (['Controlled'] as LinkedHashSet<String>),
        */
        IR (['Controlled', 'OA_Gold', 'Other_Free_To_Read'] as LinkedHashSet<String>)
        /*
        IR_A1 (['Total_Item_Requests', 'Unique_Item_Requests'] as LinkedHashSet<String>),
        IR_M1 (['Total_Item_Requests'] as LinkedHashSet<String>)
        */

        ACCESS_TYPES(LinkedHashSet<String> accessTypes) {
            this.accessTypes = accessTypes
        }

        public LinkedHashSet<String> accessTypes
    }

    String accessType
    String accessMethod
    String dataType

    static mapWith = "none"

    /*
    domain mapping unused as usage data is being fetched on-the-fly

    static mapping = {
        datasource           'storage'
        id                      column: 'c5r_id'
        version                 column: 'c5r_version'
        onlineIdentifier        column: 'c5r_online_identifier', index: 'c5r_online_identifier_idx'
        printIdentifier         column: 'c5r_print_identifier', index: 'c5r_print_identifier_idx'
        doi                     column: 'c5r_doi', index: 'c5r_doi_idx'
        isbn                    column: 'c5r_isbn', index: 'c5r_isbn_idx'
        proprietaryIdentifier   column: 'c5r_proprietary_identifier', index: 'c5r_prop_ident_idx'
        identifierHash          column: 'c5r_identifier_hash', type: 'text', index: 'c5r_idhash_idx'
        databaseName            column: 'c5r_database_name', type: 'text'
        publisher               column: 'c5r_publisher', type: 'text'
        platformUID             column: 'c5r_platform_guid', index: 'c5r_plat_idx'
        reportInstitutionUID    column: 'c5r_report_institution_guid', index: 'c5r_ri_idx'
        reportType              column: 'c5r_report_type', index: 'c5r_rt_idx'
        dataType                column: 'c5r_data_type', index: 'c5r_dt_idx'
        accessType              column: 'c5r_access_type', index: 'c5r_access_type_idx'
        accessMethod            column: 'c5r_access_method', index: 'c5r_access_method_idx'
        metricType              column: 'c5r_metric_type', index: 'c5r_metric_type_idx'
        reportFrom              column: 'c5r_report_from', index: 'c5r_report_from_idx'
        reportTo                column: 'c5r_report_to', index: 'c5r_report_to_idx '
        yop                     column: 'c5r_yop', index: 'c5r_yop_idx'
        reportCount             column: 'c5r_report_count'
    }

    static constraints = {
        onlineIdentifier        (nullable: true) //because of platform reports!
        printIdentifier         (nullable: true) //because of platform reports!
        doi                     (nullable: true) //because of platform reports!
        isbn                    (nullable: true) //because of platform reports!
        proprietaryIdentifier   (nullable: true) //because of platform reports!
        identifierHash          (nullable: true) //because of platform reports!
        publisher               (nullable: true, blank: false) //because of platform reports!
        databaseName            (nullable: true) //because used only for database reports!
        dataType                (nullable: true, blank: false)
        accessType              (nullable: true, blank: false)
        accessMethod            (nullable: true, blank: false)
        yop                     (nullable: true) //YOP is only used in tr_j4
        //unique constraints need to be defined manually per dbm changeset because of partial null values
    }

    static transients = ['platform', 'reportInstitution']
    */
}
