import com.k_int.kbplus.auth.User
import groovy.util.logging.Log4j

@Log4j
class LaserFilters {

    def springSecurityService

    def filters = {

        globalUIDFilter(controller:'*', action:'*') {

            before = {
                if (params.id?.contains(':')) {
                    try {
                        def objName  = params.id.split(':')[0]
                        def objClass = Class.forName("com.k_int.kbplus.${objName.capitalize()}")

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
                else if (springSecurityService.principal?.id != null ) {
                    log.debug("Set request.user to ${springSecurityService.principal?.id}");
                    request.user = User.get(springSecurityService.principal.id);

                    // Just set the user preferences equal to those of the current user.
                    if ( session.userPereferences == null ) {
                        session.userPereferences = request.user.getUserPreferences()
                    }
                }

                if ( session.sessionPreferences == null ) {
                    session.sessionPreferences = grailsApplication.config.appDefaultPrefs
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