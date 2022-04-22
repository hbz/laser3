package de.laser.helper

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class MigrationHelper {

    static LocalDate dateToLocalDate(Date date) {
        if (!date) {
            //println 'MigrationHelper.dateToLocalDate( NULL )'
            return null
        }
        LocalDate.ofInstant( date.toInstant(), ZoneId.systemDefault())
    }

    static LocalDateTime dateToLocalDateTime(Date date) {
        if (!date) {
            //println 'MigrationHelper.dateToLocalDateTime( NULL )'
            return null
        }
        LocalDateTime.ofInstant( date.toInstant(), ZoneId.systemDefault())
    }

    static Date localDateToSqlDate(LocalDate localDate) {
        if (!localDate) {
            //println 'MigrationHelper.localDateToSqlDate( NULL )'
            return null
        }
        java.sql.Date.valueOf(localDate) //java.sql.Date extends java.util.Date
    }
}
