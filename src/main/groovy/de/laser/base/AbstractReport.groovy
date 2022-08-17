package de.laser.base

import de.laser.Org
import de.laser.Platform
import de.laser.TitleInstancePackagePlatform

/**
 * Abstract class for central properties of every revision of COUNTER reports.
 */
abstract class AbstractReport {

    String titleUID
    String reportType
    String publisher
    String metricType
    String platformUID
    String reportInstitutionUID
    Date reportFrom
    Date reportTo
    Integer reportCount

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
}
