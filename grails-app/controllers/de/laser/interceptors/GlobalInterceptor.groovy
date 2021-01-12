package de.laser.interceptors

class GlobalInterceptor implements grails.artefact.Interceptor {

    GlobalInterceptor() {
        matchAll()
    }

    boolean before() {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate")
        response.setHeader("Pragma","no-cache")
        response.setHeader("Expires","0")

        true
    }

    boolean after() {
        true
    }
}
