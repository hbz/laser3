<%@ page import="de.laser.helper.ConfigUtils" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'menu.admin.appInfo')}</title>
</head>

<body>

    <semui:breadcrumbs>
        <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
        <semui:crumb message="menu.admin.appInfo" class="active"/>
    </semui:breadcrumbs>

    <h1 class="ui left aligned icon header la-noMargin-top"><semui:headerIcon />${message(code:'menu.admin.appInfo')}</h1>

    <table class="ui celled la-js-responsive-table la-table table compact">
        <thead>
            <tr><th colspan="2"><h2 class="ui header">App</h2></th></tr>
        </thead>
        <tbody>
        <tr><td>App name</td><td> <g:meta name="info.app.name"/></td></tr>
        <tr><td>App version</td><td> <g:meta name="info.app.version"/></td></tr>
        <tr><td>Grails version</td><td> <g:meta name="info.app.grailsVersion"/></td></tr>
        <tr><td>Groovy (currently running)</td><td> ${GroovySystem.getVersion()}</td></tr>
        <tr><td>Java (currently running)</td><td> ${System.getProperty('java.version')}</td></tr>
        <tr><td>Session Timeout</td><td> ${(session.getMaxInactiveInterval() / 60)} Minutes</td></tr>
        <tr><td>Reloading active</td><td> ${grails.util.Environment.reloadingAgentEnabled}</td></tr>
        <tr><td>Last Quartz Heartbeat</td><td>${ConfigUtils.getQuartzHeartbeat()}</td></tr>
        </tbody>
    </table>

    <table class="ui celled la-js-responsive-table la-table table compact">
        <thead>
            <tr><th colspan="2"><h2 class="ui header">Build</h2></th></tr>
        </thead>
        <tbody>
        <tr><td>Build Date</td><td> <g:meta name="info.app.build.date"/></td></tr>
        <tr><td>Build Host</td><td> <g:meta name="info.app.build.host"/></td></tr>
        <tr><td>Build Profile</td><td> <g:meta name="info.app.build.profile"/></td></tr>
        <tr><td>Build Java Version</td><td> <g:meta name="info.app.build.javaVersion"/></td></tr>
        </tbody>
    </table>

    <table class="ui celled la-js-responsive-table la-table table compact">
        <thead>
            <tr><th colspan="2"><h2 class="ui header">Database</h2></th></tr>
        </thead>
        <tbody>
        <tr><td>DBM version</td><td> ${dbmVersion[0]} : ${dbmVersion[1]} </td></tr>
        <tr><td>DBM updateOnStart</td><td> ${grailsApplication.config.grails.plugin.databasemigration.updateOnStart}</td></tr>
        <tr><td>DataSource.dbCreate</td><td> ${grailsApplication.config.dataSource.dbCreate}</td></tr>
        <tbody>
    </table>

    <table class="ui celled la-js-responsive-table la-table table compact">
        <thead>
            <tr><th colspan="2"><h2 class="ui header">ES Index Update</h2></th></tr>
        </thead>
        <tbody>
        <tr><td>Currently Running</td><td>${dataloadService.update_running}</td></tr>
        <tr><td>Last update run</td><td>${dataloadService.lastIndexUpdate}</td></tr>
        <g:each in="${esinfos}" var="es">
            <tr><td>DomainClass: ${es.domainClassName}</td><td>DB Elements: ${es.dbElements}, ES Elements: ${es.esElements}<br /> Last Update: ${new Date(es.lastTimestamp)}</td></tr>
        </g:each>
        </tbody>
    </table>

    <table class="ui celled la-js-responsive-table la-table table compact">
        <thead>
            <tr><th colspan="2"><h2 class="ui header">Global Data Sync</h2></th></tr>
        </thead>
        <tbody>
            <tr><td>Currently Running</td><td>${globalSourceSyncService.running}</td></tr>
        </tbody>
    </table>

    <table class="ui celled la-js-responsive-table la-table table compact">
        <thead>
            <tr><th colspan="2"><h2 class="ui header">STATS Sync Service</h2></th></tr>
        </thead>
        <tbody>
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
                    ${ah.key}:${ah.value}<br />
                </g:each>
            </td></tr>
        </tbody>
    </table>

    <table class="ui celled la-js-responsive-table la-table table compact">
        <thead>
            <tr><th colspan="2"><h2 class="ui header">HttpServletRequest.getAttributeNames()</h2></th></tr>
        </thead>
        <tbody>
        <tr>
            <td>
                <div class="ui relaxed divided list">
                    <g:each in="${request.getAttributeNames()}" var="an">
                        <div class="item">${an} = ${request.getAttribute(an)}</div>
                    </g:each>
                </div>
            </td>
        </tr>
        </tbody>
    </table>

</body>
</html>
