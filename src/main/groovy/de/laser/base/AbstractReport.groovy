package de.laser.base

import de.laser.Org
import de.laser.Platform
import de.laser.TitleInstancePackagePlatform

import java.time.Year

/**
 * Abstract class for central properties of every revision of COUNTER reports.
 */
abstract class AbstractReport implements Comparable<AbstractReport> {

    Long id
    String onlineIdentifier
    String printIdentifier
    String doi
    String isbn
    String proprietaryIdentifier
    //only to use for database reports
    String databaseName
    String reportType
    String publisher
    String metricType
    Platform platform
    Org reportInstitution
    Date reportFrom
    Date reportTo
    Integer reportCount
    //only for Journal Report 5 in COUNTER 4 resp. tr_j4 in COUNTER 5
    Date yop

    static final String COUNTER_4 = 'counter4'
    static final String COUNTER_5 = 'counter5'

    //implementation base for ERMS-4813
    static Map ERROR_CODES = [:]
    static final String API_AUTH_CUSTOMER_REQUESTOR_API = "Requestor Key+Customer ID+central API Key"
    static final String API_AUTH_REQUESTOR_API = "Requestor Key+API Key"
    static final String API_AUTH_CUSTOMER_API = "Customer Key+API Key"
    static final String API_AUTH_CUSTOMER_REQUESTOR = "Requestor Key+Customer ID"
    static final String API_IP_WHITELISTING = "IP whitelisting"

    @Override
    int compareTo(AbstractReport that) {
        int result
        result = this.reportFrom <=> that.reportFrom
        if(result == 0)
            result = this.reportType <=> that.reportType
        if(result == 0)
            result = this.metricType <=> that.metricType
        if(result == 0)
            result = this.yop <=> that.yop
        if(result == 0)
            result = this.id <=> that.id
        result
    }
}
