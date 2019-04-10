import geb.spock.GebSpec
import grails.test.mixin.integration.Integration

@SuppressWarnings('MethodName')
@Integration
class DefaultHomePageSpec extends GebSpec {

    def 'first try ..'() {
        when:
            browser.go '/api'

        then:
            browser.page.title == "LAS:eR - API"

    }

    def 'second try ..'() {
        when:
            browser.go '/api'

        then:
            browser.page.title  == "narf"

    }
}