package de.laser

import de.laser.helper.AppUtils

import javax.servlet.http.HttpServletRequest

class BootStrap {

    BootStrapService bootStrapService

    def init = { servletContext ->

        HttpServletRequest.metaClass.isXhr = { ->
            'XMLHttpRequest' == delegate.getHeader('X-Requested-With')
        }

        bootStrapService.init( AppUtils.isRestartedByDevtools() )
    }

    def destroy = {
        bootStrapService.destroy()
    }
}