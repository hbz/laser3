package de.laser.sse

import de.laser.ContextService
import de.laser.SystemService
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.rx.web.RxController
import rx.Observable

import java.util.concurrent.TimeUnit

@Secured(['permitAll'])
class SSEController implements RxController {

    ContextService contextService
    SystemService systemService

    final static int HEARTBEAT_IN_SECONDS = 5

    final static String STATUS_ES = '/SSE/status'

    @Secured(['permitAll'])
    def status() {
        println 'SSEController INDEX'

        int prevHashCode = -1
        rx.stream(
            Observable.interval(HEARTBEAT_IN_SECONDS, TimeUnit.SECONDS).map {
                try {
                    // rx.event(new JSON(systemService.getStatusMessage(++i)).toString())
                    String message = new JSON(systemService.getStatusMessage()).toString()
                    println ' -> #' + it + ' : ' + message.hashCode()
                    if (message.hashCode() != prevHashCode) {
                        prevHashCode = message.hashCode()
                        return rx.render(message)
                    }
                } catch(Exception e) {
                    println e
                    log.debug e.getMessage()
                }
            }
        )
    }
}
