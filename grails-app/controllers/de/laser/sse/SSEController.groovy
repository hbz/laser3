package de.laser.sse

import de.laser.ContextService
import de.laser.SystemService
import de.laser.helper.DateUtils
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.rx.web.RxController
import org.grails.plugins.rx.web.sse.SseResult
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
        try {
            int prevHashCode = -1

            Observable<SseResult> obsSser = Observable.interval(HEARTBEAT_IN_SECONDS, TimeUnit.SECONDS).map {
                try {
                    // rx.event(new JSON(systemService.getStatusMessage(++i)).toString())
                    String message = new JSON(systemService.getStatusMessage()).toString()
                    //println DateUtils.getLocalizedSDF_onlyTime().format(new Date()) + ' -> #' + it.toString() + ' : ' + message.hashCode()

                    if (message.hashCode() != prevHashCode) {
                        prevHashCode = message.hashCode()
                        SseResult result = rx.event(message)
                        return result
                    }
                    else {
                        SseResult result = rx.event(['ping': 'pong'])
                        return result
                    }
                }
                catch (Exception e) {
                    log.warn 'SSEController.index() -> rx.stream{} -> Observable.interval().map{}'
                    log.debug e.getMessage()
                }
            }
            rx.stream( obsSser )
        }
        catch (Exception e) {
            log.warn 'SSEController.index() -> rx.stream()'
            log.debug e.getMessage()
        }
    }
}
