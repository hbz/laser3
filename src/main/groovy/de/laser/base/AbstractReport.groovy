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
    String titleUID
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

    TitleInstancePackagePlatform getTitle() {
        return titleUID ? TitleInstancePackagePlatform.findByGlobalUID(titleUID) : null
    }

    void setTitle(TitleInstancePackagePlatform title) {
        this.titleUID = title.globalUID
    }

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
        result = this.titleUID <=> that.titleUID
        if(result == 0)
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
