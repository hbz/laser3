class BootStrap {

    def bootStrapService

    def init = { servletContext ->
        bootStrapService.init(servletContext)
    }

    def destroy = {
        bootStrapService.destroy()
    }
}
