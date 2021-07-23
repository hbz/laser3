package de.laser.base

import de.laser.Org
import de.laser.Platform

import java.time.Month
import java.time.Year

abstract class AbstractReport {

    String reportType
    String publisher
    Platform platform
    Org reportInstitution
    Year year
    Month month
    int count

}
