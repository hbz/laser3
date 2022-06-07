package de.laser

import de.laser.helper.AppUtils

import javax.servlet.http.HttpServletRequest

/**
 * The Bootstrap container, keeping closures for app startup
 */
class BootStrap {

    BootStrapService bootStrapService

    /**
     * Triggers initialisation of global settings
     */
    def init = { servletContext ->

        HttpServletRequest.metaClass.isXhr = { ->
            'XMLHttpRequest' == delegate.getHeader('X-Requested-With')
        }

        bootStrapService.init( AppUtils.isRestartedByDevtools() )
    }

    /**
     * App destructor
     */
    def destroy = {
        bootStrapService.destroy()
    }
}