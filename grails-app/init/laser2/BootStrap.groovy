package laser2

import javax.servlet.http.HttpServletRequest

/**
 * The Bootstrap container, keeping closures for app startup
 */
class BootStrap {

    def bootStrapService

    /**
     * Triggers initialisation of global settings
     */
    def init = { servletContext ->

        HttpServletRequest.metaClass.isXhr = { ->
            'XMLHttpRequest' == delegate.getHeader('X-Requested-With')
        }

        bootStrapService.init(servletContext)
    }

    /**
     * App destructor
     */
    def destroy = {
        bootStrapService.destroy()
    }
}