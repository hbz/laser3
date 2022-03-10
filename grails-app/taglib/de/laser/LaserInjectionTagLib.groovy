package de.laser

class LaserInjectionTagLib {

    static namespace = "laser"

    // <laser:serviceInjection/>
    def serviceInjection = { attrs, body ->

        g.set( var:'accessService',             bean:'accessService' )
        g.set( var:'auditService',              bean:'auditService' )
        g.set( var:'cacheService',              bean:'cacheService' )
        g.set( var:'contextService',            bean:'contextService' )
        g.set( var:'controlledListService',     bean:'controlledListService' )
        g.set( var:'escapeService',             bean:'escapeService')
        g.set( var:'filterService',             bean:'filterService' )
        g.set( var:'formService',               bean:'formService' )
        g.set( var:'genericOIDService',         bean:'genericOIDService' )
        g.set( var:'gokbService',               bean:'gokbService' )
        g.set( var:'instAdmService',            bean:'instAdmService' )
        g.set( var:'identifierService',         bean:'identifierService' )
        g.set( var:'linksGenerationService',    bean:'linksGenerationService' )
        g.set( var:'docstoreService',           bean:'docstoreService' )
        g.set( var:'packageService',            bean:'packageService')
        g.set( var:'pendingChangeService',      bean:'pendingChangeService')
        g.set( var:'subscriptionsQueryService', bean:'subscriptionsQueryService' )
        g.set( var:'subscriptionService',       bean:'subscriptionService' )
        g.set( var:'surveyService',             bean:'surveyService' )
        g.set( var:'systemService',             bean:'systemService' )
        g.set( var:'taskService',               bean:'taskService' )
        g.set( var:'yodaService',               bean:'yodaService' )
    }

    // TODO: in progress

    def templateInfo = { attrs, body ->

        out << '<!-- \n'
        if (attrs.source) {
            out << attrs.source.getProperties().get('groovyPageFileName') + '\n'
        }
        out << '\n --!>'
    }
}
