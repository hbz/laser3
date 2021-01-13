import com.k_int.kbplus.auth.User
import de.laser.helper.ConfigUtils
import groovy.util.logging.Log4j

@Log4j
class LaserFilters {

    def springSecurityService

    def filters = {

        // TODO: grails-upgrade: http://docs.grails.org/latest/guide/theWebLayer.html#interceptors

        cacheControlFilter(controller:'*', action:'*') {

            before = {
                response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate")
                response.setHeader("Pragma", "no-cache")
                response.setHeader("Expires", "0")
            }
        }

        globalUIDFilter(controller:'*', action:'*') {

            before = {
                if (params.id?.contains(':')) {
                    try {
                        def objName  = params.id.split(':')[0]
                        def objClass = Class.forName("de.laser.${objName.capitalize()}")

                        if (objClass) {
                            def match = objClass.findByGlobalUID(params.id)
                            if (match) {
                                log.debug("LaserFilters.globalUIDFilter(): ${params.controller} # ${params.id} -> ${match.id}")
                                params.id = match.getId()
                            }
                            else {
                                params.id = 0
                            }
                        }

                    }
                    catch (Exception e) {
                        params.id = 0
                    }
                }
            }
        }

        setPrefsFilter(controller:'*', action:'*') {
            before = {
                if ( springSecurityService.principal instanceof String ) {
                    log.debug("User is string: ${springSecurityService.principal}");
                }
                    // TODO
                else if ( springSecurityService.principal?.username == '__grails.anonymous.user__' ) {
                    log.debug("User is string: ${springSecurityService.principal}");
                }
                else if (springSecurityService.principal?.id != null ) {
                    log.debug("Set request.user to ${springSecurityService.principal.id}");
                    request.user = User.get(springSecurityService.principal.id);
                }

                if ( session.sessionPreferences == null ) {
                    session.sessionPreferences = ConfigUtils.getAppDefaultPrefs()
                }
                else {
                }
            }
            after = {
                log.debug("After")
            }
        }
    }
}