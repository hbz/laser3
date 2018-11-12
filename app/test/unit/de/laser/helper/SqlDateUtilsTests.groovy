package de.laser.helper

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.junit.Test
import spock.lang.Specification
import spock.lang.Unroll

import java.sql.Date

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
//@TestMixin(GrailsUnitTestMixin)
class SqlDateUtilsTests extends Specification{


    @Test
    @Unroll
    def "isDateBetween"() {
        given:
        Date dateToTest = new Date(2018, 7, 1)

        when:
        def expected = SqlDateUtils.isDateBetween(dateToTest, dateFrom, dateTo)

        expect:
        result == expected

        where:
        dateFrom                               | dateTo                                  || result
        new Date(2005, 1, 1) | new Date(2020, 12, 31) || true
        new Date(2005, 1, 1) | new Date(2060, 12, 31) || false
        new Date(2005, 1, 1) | new Date(2020, 12, 31) || true
    }
}
