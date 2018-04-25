<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} Manage Usage Stats</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
    <semui:crumb text="Stats" class="active"/>
</semui:breadcrumbs>

<h1 class="ui header"><semui:headerIcon/>Manage Usage Stats</h1>

<semui:messages data="${flash}" />

<h3 class="ui header">Cached Usage Data</h3>
<semui:filter>
    <g:form action="index" controller="usage" method="get" class="form-inline ui small form">
        <div class="three fields">
            <div class="field fieldcontain">
                <label>Anbieter</label>
                <g:select class="ui dropdown" name="provider"
                              from="${providerList}"
                              optionKey="id"
                              optionValue="name"
                              value="${params.provider}"
                              noSelection="${[null: message(code: 'default.select.choose.label')]}"/>
            </div>
            <div class="field fieldcontain">
                <label>Einrichtung</label>
                <g:select class="ui dropdown" name="institution"
                          from="${institutionList}"
                          optionKey="id"
                          optionValue="name"
                          value="${params.institution}"
                          noSelection="${[null: message(code: 'default.select.choose.label')]}"/>
            </div>
        </div>
            <div class="fields">
                <div class="field">
                    <g:actionSubmit action="fetchSelection" class="ui primary button" value="Hole Daten für Auswahl" onclick="return confirm('${message(code:'confirm.start.StatsSync')}')"/>
                </div>
                <div class="field">
                    <g:actionSubmit action="deleteSelection" class="ui secondary button" value="Daten für Auswahl löschen" onclick="return confirm('${message(code:'confirm.start.StatsDeleteSelection')}')"/>
                </div>
                <div class="field">
                    <g:actionSubmit action="deleteAll" value="Alle Daten löschen" class="ui button red" onclick="return confirm('${message(code:'confirm.start.StatsDelete')}')"/>
                </div>
                <g:if test="${statsSyncService.running}">
                    <div class="field">
                        <g:actionSubmit action="abort" value="Prozess abbrechen" class="ui button red" onclick="return confirm('${message(code:'confirm.start.StatsAbort')}')"/>
                    </div>
                </g:if>
            </div>
    </g:form>
</semui:filter>
<h3 class="ui header">Konfiguration</h3>
<table class="ui celled la-table table">
    <tr><td>SUSHI API Url</td><td>${grailsApplication.config.statsApiUrl}</td></tr>
    <tr><td>Provider mit statssid</td><td></td></tr>
    <tr><td>Kontexteinrichtung für API Abruf freigeschaltet</td><td></td></tr>
    <tr><td>Einrichtungskontext</td><td>Einrichtung</td></tr>
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
        <tr><td>Average Time Per STATS Triple (Current/Last Run)</td><td>${statsSyncService.totalTime / statsSyncService.completedCount} (ms)</td>
        </tr>
    </g:if>
    <tr><td>Activity Histogram</td>
        <td>
            <g:each in="${statsSyncService.activityHistogram}" var="ah">
                ${ah.key}:${ah.value}<br/>
            </g:each>
        </td></tr>
</table>

</body>
</html>