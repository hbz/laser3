<%@ page import="de.laser.helper.DatabaseUtils; de.laser.storage.BeanStorage; de.laser.system.SystemSetting; de.laser.helper.AppUtils; grails.util.Metadata; de.laser.reporting.report.ElasticSearchHelper; de.laser.helper.DateUtils; grails.util.Environment; de.laser.helper.ConfigMapper" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'menu.admin.appInfo')}</title>
</head>

<body>

    <semui:breadcrumbs>
        <semui:crumb message="menu.admin" controller="admin" action="index"/>
        <semui:crumb message="menu.admin.appInfo" class="active"/>
    </semui:breadcrumbs>

    <h1 class="ui left aligned icon header la-noMargin-top"><semui:headerIcon />${message(code:'menu.admin.appInfo')}</h1>

    <table class="ui celled la-js-responsive-table la-table la-hover-table table compact">
        <thead>
            <tr><th class="seven wide">Application</th><th class="nine wide"></th></tr>
        </thead>
        <tbody>
            <tr><td>App name</td><td> ${AppUtils.getMeta('info.app.name')}</td></tr>
            <tr><td>App version</td><td> ${AppUtils.getMeta('info.app.version')}</td></tr>
            <tr><td>Grails version</td><td> ${AppUtils.getMeta('info.app.grailsVersion')}</td></tr>
            <tr><td>Groovy (currently running)</td><td> ${GroovySystem.getVersion()}</td></tr>
            <tr><td>Java (currently running)</td><td> ${System.getProperty('java.version')}</td></tr>
            <tr><td>Configuration file</td><td> ${ConfigMapper.getCurrentConfigFile(this.applicationContext.getEnvironment()).name}</td></tr>
            <tr><td>Environment</td><td> ${Metadata.getCurrent().getEnvironment()}</td></tr>
            <tr><td>Session timeout</td><td> ${(session.getMaxInactiveInterval() / 60)} Minutes</td></tr>
            <tr><td>Last quartz heartbeat</td><td>${ConfigMapper.getQuartzHeartbeat()}</td></tr>
        </tbody>
    </table>

    <table class="ui celled la-js-responsive-table la-table la-hover-table table compact">
        <thead>
            <tr><th class="seven wide">Build</th><th class="nine wide"></th></tr>
        </thead>
        <tbody>
            <tr><td>Build date</td><td> ${AppUtils.getMeta('info.app.build.date')}</td></tr>
            <tr><td>Build host</td><td> ${AppUtils.getMeta('info.app.build.host')}</td></tr>
            <tr><td>Build profile</td><td> ${AppUtils.getMeta('info.app.build.profile')}</td></tr>
            <tr><td>Build java version</td><td> ${AppUtils.getMeta('info.app.build.javaVersion')}</td></tr>
        </tbody>
    </table>

    <table class="ui celled la-js-responsive-table la-table la-hover-table table compact">
        <thead>
            <tr><th class="seven wide">Database</th><th class="nine wide"></th></tr>
        </thead>
        <tbody>
            <tr><td>Database</td><td> ${ConfigMapper.getConfig('dataSource.url').split('/').last()}</td></tr>
            <tr><td>DBM version</td><td> ${dbmVersion[0]} <br/> ${dbmVersion[1]} <br/> ${DateUtils.getSDF_NoZ().format(dbmVersion[2])}</td></tr>
            <tr><td>DBM updateOnStart</td><td> ${ConfigMapper.getPluginConfig('databasemigration.updateOnStart')}</td></tr>
            <tr><td>Collations</td><td>
                <%
                    Set collations = []
                    DatabaseUtils.getAllTablesCollationInfo().each { it ->
                        List c = it.value['collation'].findAll()
                        if (! c.isEmpty()) { collations.addAll(c) }
                    }
                    collations.each { print it + '<br/>' }
                %>
            </td></tr>
            <tr><td>Postgresql server</td><td> ${DatabaseUtils.getServerInfo()}</td></tr>
        <tbody>
    </table>

    <table class="ui celled la-js-responsive-table la-table la-hover-table table compact">
        <thead>
            <tr><th class="seven wide">ES Index</th><th class="nine wide"></th></tr>
        </thead>
        <tbody>
            <tr><td>Currently running</td><td>${dataloadService.update_running}</td></tr>
            <tr><td>Last update run</td><td>${dataloadService.lastIndexUpdate}</td></tr>
            <tr><td>Current indicies</td><td>${BeanStorage.getESWrapperService().es_indices}</td></tr>
            <g:each in="${esinfos}" var="es">
                <tr><td>DomainClass: ${es.domainClassName}</td><td>DB Elements: ${es.dbElements}, ES Elements: ${es.esElements}<br /> Last Update: ${new Date(es.lastTimestamp)}</td></tr>
            </g:each>
        </tbody>
    </table>

    <table class="ui celled la-js-responsive-table la-table la-hover-table table compact">
        <thead>
            <tr><th class="seven wide">Global Data Sync</th><th class="nine wide"></th></tr>
        </thead>
        <tbody>
            <tr><td>Currently running</td><td>${globalSourceSyncService.running}</td></tr>
        </tbody>
    </table>

    <g:if test="${ConfigMapper.getConfig('reporting.elasticSearch')}">
        <table class="ui celled la-js-responsive-table la-table la-hover-table table compact">
            <thead>
            <tr><th class="seven wide">Reporting</th><th class="nine wide"></th></tr>
            </thead>
            <tbody>
            <tr>
                <td>ElasticSearch url</td>
                <td><a href="${ConfigMapper.getConfig('reporting.elasticSearch.url') + '/_cat/indices?v'}" target="_blank">${ConfigMapper.getConfig('reporting.elasticSearch.url')}</a></td>
            </tr>
            <tr>
                <td>ElasticSearch indicies</td>
                <td>
                    <g:if test="${ConfigMapper.getConfig('reporting.elasticSearch.indicies')}">
                        <g:each in="${ConfigMapper.getConfig('reporting.elasticSearch.indicies')}" var="k, v">
                            <a href="${ConfigMapper.getConfig('reporting.elasticSearch.url') + '/' + v + '/_search'}" target="_blank">${v} (${k})</a><br />
                        </g:each>
                    </g:if>
                </td>
            </tr>
            <tr>
                <td>ElasticSearch apiSource</td>
                <td>
                    <g:set var="eshApiSource" value="${ElasticSearchHelper.getCurrentApiSource()}" />
                    <g:if test="${eshApiSource}">
                        <a href="${eshApiSource.baseUrl}" target="_blank">${eshApiSource.baseUrl}</a> (${eshApiSource.name})
                    </g:if>
                </td>
            </tr>
            </tbody>
        </table>
    </g:if>

    <table class="ui celled la-js-responsive-table la-table la-hover-table table compact">
        <thead>
            <tr><th class="seven wide">STATS Sync Service</th><th class="nine wide"></th></tr>
        </thead>
        <tbody>
            <tr><td>Currently running</td><td>${statsSyncService.running}</td></tr>
            <tr><td>Completed count</td><td>${statsSyncService.completedCount}</td></tr>
            <tr><td>New fact count</td><td>${statsSyncService.newFactCount}</td></tr>
            <tr><td>Total time (all threads)</td><td>${statsSyncService.totalTime} (ms)</td></tr>
            <tr><td>Total time elapsed</td><td>${statsSyncService.syncElapsed} (ms)</td></tr>
            <tr><td>Thread pool size</td><td>${statsSyncService.threads}</td></tr>
            <tr><td>Last start time</td>
            <td>
                <g:if test="${statsSyncService.syncStartTime != 0}">
                    <g:formatDate date="${new Date(statsSyncService.syncStartTime)}" format="yyyy-MM-dd hh:mm"/>
                </g:if>
                <g:else>
                    Not started yet
                </g:else>
            </tr>
            <tr><td>Initial query time</td><td>${statsSyncService.queryTime} (ms)</td></tr>

            <g:if test="${((statsSyncService.completedCount != 0) && (statsSyncService.totalTime != 0))}">
                <tr><td>Average time per STATS triple (current/last run)</td><td>${statsSyncService.totalTime/statsSyncService.completedCount} (ms)</td></tr>
            </g:if>
            <tr><td>Activity histogram</td>
            <td>
                <g:each in="${statsSyncService.activityHistogram}" var="ah">
                    ${ah.key}:${ah.value}<br />
                </g:each>
            </td></tr>
        </tbody>
    </table>
</body>
</html>
