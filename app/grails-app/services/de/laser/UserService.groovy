package de.laser

import com.k_int.kbplus.auth.User
import de.laser.helper.RDStore
import org.codehaus.groovy.grails.commons.GrailsApplication
import com.k_int.kbplus.UserSettings

class UserService {

    GrailsApplication grailsApplication

    void initMandatorySettings(User user) {
        log.debug('initMandatorySettings for user #' + user.id)


        user.getSetting(UserSettings.KEYS.IS_NOTIFICATION_BY_EMAIL, RDStore.YN_YES)
        user.getSetting(UserSettings.KEYS.IS_NOTIFICATION_FOR_SURVEYS_START, RDStore.YN_YES)

// called after
// new User.save()
// or
// every successful login
}
}
