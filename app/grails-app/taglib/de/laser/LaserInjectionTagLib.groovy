package de.laser

class LaserInjectionTagLib {

    def springSecurityService
    def contextService
    def accessService
    def yodaService
    def subscriptionsQueryService

    static namespace = "laser"

    def serviceInjection = { attrs, body ->

        out << "<!-- serviceInjection: springSecurityService, contextService, accessService, yodaService, subscriptionsQueryService -->"

        g.set( var:'springSecurityService', bean:'springSecurityService' )
        g.set( var:'contextService', bean:'contextService' )
        g.set( var:'accessService', bean:'accessService' )
        g.set( var:'yodaService', bean:'yodaService' )
        g.set( var:'subscriptionsQueryService', bean:'subscriptionsQueryService' )
    }
}
