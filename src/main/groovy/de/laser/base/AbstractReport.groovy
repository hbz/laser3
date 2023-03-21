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
    String identifierHash
    String reportType
    String publisher
    String metricType
    String platformUID
    String reportInstitutionUID
    Date reportFrom
    Date reportTo
    Integer reportCount
    //only for Journal Report 5 in COUNTER 4 resp. tr_j4 in COUNTER 5
    Date yop

    Platform getPlatform() {
        return platformUID ? Platform.findByGlobalUID(platformUID) : null
    }

    void setPlatform(Platform platform) {
        this.platformUID = platform.globalUID
    }

    Org getReportInstitution() {
        return reportInstitutionUID ? Org.findByGlobalUID(reportInstitutionUID) : null
    }

    void setReportInstitution(Org reportInstitution) {
        this.reportInstitutionUID = reportInstitution.globalUID
    }

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
