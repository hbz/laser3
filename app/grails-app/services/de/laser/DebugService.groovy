package de.laser

import de.laser.helper.DebugUtil

class DebugService {

    private debugUtil

    DebugUtil getDebugUtilAsSingleton() {
        if (! debugUtil) {
            debugUtil = new DebugUtil(DebugUtil.CK_PREFIX_GLOBAL_INTERCEPTOR)
        }

        debugUtil
    }
}
