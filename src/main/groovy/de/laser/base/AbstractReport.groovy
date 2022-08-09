package de.laser.base

import de.laser.Org
import de.laser.Platform
import de.laser.TitleInstancePackagePlatform

/**
 * Abstract class for central properties of every revision of COUNTER reports.
 */
abstract class AbstractReport {

    Long titleId
    String reportType
    String publisher
    String metricType
    Long platformId
    Long reportInstitutionId
    Date reportFrom
    Date reportTo
    Integer reportCount

    TitleInstancePackagePlatform getTitle() {
        return titleId ? TitleInstancePackagePlatform.get(titleId) : null
    }

    void setTitle(TitleInstancePackagePlatform title) {
        this.titleId = title.id
    }

    Platform getPlatform() {
        return platformId ? Platform.get(platformId) : null
    }

    void setPlatform(Platform platform) {
        this.platformId = platform.id
    }

    Org getReportInstitution() {
        return reportInstitutionId ? Org.get(reportInstitutionId) : null
    }

    void setReportInstitution(Org reportInstitution) {
        this.reportInstitutionId = reportInstitution.id
    }
}
