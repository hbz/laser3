package de.laser

import javax.servlet.http.HttpServletRequest

class BootStrap {

    def bootStrapService

    def init = { servletContext ->

        HttpServletRequest.metaClass.isXhr = { ->
            'XMLHttpRequest' == delegate.getHeader('X-Requested-With')
        }

        bootStrapService.init(servletContext)
    }

    def destroy = {
        bootStrapService.destroy()
    }
}