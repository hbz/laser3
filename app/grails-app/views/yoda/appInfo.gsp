<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : App Info</title>
</head>

<body>

    <semui:breadcrumbs>
        <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
        <semui:crumb text="Application Info" class="active"/>
    </semui:breadcrumbs>

    <h1 class="ui header"><semui:headerIcon />Application Info</h1>

    <table class="ui celled la-table table">
        <tr><td>Build Date</td><td> <g:meta name="app.buildDate"/></td></tr>
        <tr><td>Build Number</td><td> <g:meta name="app.buildNumber"/></td></tr>
        <tr><td>Build Profile</td><td> <g:meta name="app.buildProfile"/></td></tr>
        <tr><td>App version</td><td> <g:meta name="app.version"/></td></tr>
        <tr><td>Grails version</td><td> <g:meta name="app.grails.version"/></td></tr>
        <tr><td>Groovy version</td><td> ${GroovySystem.getVersion()}</td></tr>
        <tr><td>JVM version</td><td> ${System.getProperty('java.version')}</td></tr>
        <tr><td>Reloading active</td><td> ${grails.util.Environment.reloadingAgentEnabled}</td></tr>
        <tr><td>Session Timeout</td><td> ${(session.getMaxInactiveInterval() / 60)} Minutes</td></tr>
        <tr><td>Last Quartz Heartbeat</td><td>${grailsApplication.config.quartzHeartbeat}</td></tr>
    </table>

    <h2 class="ui header">Background task status</h2>

    <h3 class="ui header">ES Index Update</h3>
    <table class="ui celled la-table table">
        <tr><td>Currently Running</td><td>${dataloadService.update_running}</td></tr>
        <tr><td>Last update run</td><td>${dataloadService.lastIndexUpdate}</td></tr>
        <g:each in="${esinfos}" var="es">
            <tr><td>DomainClass: ${es.domainClassName}</td><td>DB Elements: ${es.dbElements}, ES Elements: ${es.esElements}<br> Last Update: ${new Date(es.lastTimestamp)}</td></tr>
        </g:each>
    </table>

    <h3 class="ui header">Global Data Sync</h3>
    <table class="ui celled la-table table">
        <tr><td>Currently Running</td><td>${globalSourceSyncService.running}</td></tr>
    </table>

    <h3 class="ui header">STATS Sync Service</h3>
    <table class="ui celled la-table table">
        <tr><td>Currently Running</td><td>${statsSyncService.running}</td></tr>
        <tr><td>Completed Count</td><td>${statsSyncService.completedCount}</td></tr>
        <tr><td>New Fact Count</td><td>${statsSyncService.newFactCount}</td></tr>
        <tr><td>Total Time (All Threads)</td><td>${statsSyncService.totalTime} (ms)</td></tr>
        <tr><td>Total Time Elapsed</td><td>${statsSyncService.syncElapsed} (ms)</td></tr>
        <tr><td>Thread Pool Size</td><td>${statsSyncService.threads}</td></tr>
        <tr><td>Last Start Time</td>
            <td>
                <g:if test="${statsSyncService.syncStartTime != 0}">
                    <g:formatDate date="${new Date(statsSyncService.syncStartTime)}" format="yyyy-MM-dd hh:mm"/>
                </g:if>
                <g:else>
                    Not started yet
                </g:else>
        </tr>
        <tr><td>Initial Query Time</td><td>${statsSyncService.queryTime} (ms)</td></tr>

        <g:if test="${((statsSyncService.completedCount != 0) && (statsSyncService.totalTime != 0))}">
            <tr><td>Average Time Per STATS Triple (Current/Last Run)</td><td>${statsSyncService.totalTime/statsSyncService.completedCount} (ms)</td></tr>
        </g:if>
        <tr><td>Activity Histogram</td>
            <td>
                <g:each in="${statsSyncService.activityHistogram}" var="ah">
                    ${ah.key}:${ah.value}<br/>
                </g:each>
            </td></tr>
    </table>

    <h2 class="ui header">Request</h2>

    <div class="ui relaxed divided list">
        <g:each in="${request.getAttributeNames()}" var="an">
            <div class="item">${an} = ${request.getAttribute(an)}</div>
        </g:each>
    <%--<li> authenticationMethodObject = ${request.getAttribute('Shib-Authentication-Method')}</li>
    <li> identityProviderObject = ${request.getAttribute('Shib-Identity-Provider')}</li>
    <li> principalUsernameObject = (${grailsApplication.config.grails.plugins.springsecurity.shibboleth.principalUsername.attribute})
        ${request.getAttribute(grailsApplication.config.grails.plugins.springsecurity.shibboleth.principalUsername.attribute)}</li>
        <li> authenticationInstantObject = ${request.getAttribute('Shib-Authentication-Instant')}</li>--%>
        <div class="item"> usernameObject = (EPPN) ${request.getAttribute('EPPN')}</div>
        <div class="item"> eduPersonPrincipalName = ${request.getAttribute('eduPersonPrincipalName')}</div>
        <div class="item"> eduPersonScopedAffiliation = ${request.getAttribute('eduPersonScopedAffiliation')}</div>
        <div class="item"> eduPersonPrincipalName = ${request.getAttribute('eduPersonPrincipalName')}</div>
        <div class="item"> eduPersonEntitlement = ${request.getAttribute('eduPersonEntitlement')}</div>
        <div class="item"> uid = ${request.getAttribute('uid')}</div>
        <div class="item"> mail = ${request.getAttribute('mail')}</div>
        <div class="item"> affiliation = ${request.getAttribute('affiliation')}</div>
        <div class="item"> entitlement = ${request.getAttribute('entitlement')}</div>
        <div class="item"> persistent-id = ${request.getAttribute('persistent-id')}</div>
        <div class="item"> authInstitutionName = ${request.getAttribute('authInstitutionName')}</div>
        <div class="item"> eduPersonTargetedID = ${request.getAttribute('eduPersonTargetedID')}</div>
        <div class="item"> authInstitutionAddress = ${request.getAttribute('authInstitutionAddress')}</div>
        <div class="item"> targeted-id = ${request.getAttribute('targeted-id')}</div>
        <div class="item"> uid = ${request.getAttribute('uid')}</div>
        <div class="item"> REMOTE_USER = ${request.getAttribute('REMOTE_USER')}</div>
        <div class="item"> REMOTE_USER (fm) = ${request.getRemoteUser()}</div>
    </div>

</body>
</html>
