package de.laser.base

import de.laser.Org
import de.laser.Platform
import de.laser.TitleInstancePackagePlatform

abstract class AbstractReport {

    TitleInstancePackagePlatform title
    String reportType
    String publisher
    String metricType
    Platform platform
    Org reportInstitution
    Date reportFrom
    Date reportTo
    Integer reportCount

}
