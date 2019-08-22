package de.laser

import com.k_int.kbplus.auth.User
import org.codehaus.groovy.grails.commons.GrailsApplication

class UserService {

    GrailsApplication grailsApplication

    void initMandatorySettings(User user) {
        log.debug('initMandatorySettings for user #' + user.id)

// called after
// new User.save()
// or
// every successful login
}
}
