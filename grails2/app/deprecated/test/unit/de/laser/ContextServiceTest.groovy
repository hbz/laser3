package de.laser

import grails.test.mixin.TestFor

/*  WORK IN PROGRESS */

import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Ignore
import spock.lang.Specification

@Ignore
@TestFor(ContextService)
class ContextServiceTest extends Specification {

    //void setup() {
    //}

    //void cleanup() {
    //}
    /*
    def "SetOrg"() {
    }
    */

    def "GetOrg"() {

        when:
        def x = 1

        then:
        x == 1
    }
    /*
    def "GetUser"() {
    }

    def "GetMemberships"() {
    }

    def "GetCache"() {
    }
    */
}
