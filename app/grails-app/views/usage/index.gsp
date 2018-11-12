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

<h1 class="ui left aligned icon header"><semui:headerIcon />${message(code: 'default.usage.adminPage.mainHeader')}</h1>

<semui:messages data="${flash}" />

<h3 class="ui header">${message(code: 'default.usage.adminPage.formHeader')}</h3>
<semui:filter>
    <g:form action="index" controller="usage" method="get" class="form-inline ui small form">
        <div class="three fields">
            <div class="field fieldcontain">
                <label>${message(code: 'default.usage.adminPage.supplierLabel')}</label>
                <g:select class="ui dropdown" name="supplier"
                              from="${providerList}"
                              optionKey="id"
                              optionValue="name"
                              value="${params.supplier}"
                              noSelection="${[null: message(code: 'default.select.choose.label')]}"/>
            </div>
            <div class="field fieldcontain">
                <label>${message(code: 'default.usage.adminPage.institutionLabel')}</label>
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
                    <g:actionSubmit action="fetchSelection" class="ui primary button" value="${message(code: 'default.usage.adminPage.button.fetchSelection')}" onclick="return confirm('${message(code:'confirm.start.StatsSync')}')"/>
                </div>
                <div class="field">
                    <g:actionSubmit action="deleteSelection" class="ui secondary button" value="${message(code: 'default.usage.adminPage.button.deleteSelection')}" onclick="return confirm('${message(code:'confirm.start.StatsDeleteSelection')}')"/>
                </div>
                <div class="field">
                    <g:actionSubmit action="deleteAll" value="${message(code: 'default.usage.adminPage.button.deleteAll')}" class="ui button red" onclick="return confirm('${message(code:'confirm.start.StatsDelete')}')"/>
                </div>
                <g:if test="${statsSyncService.running}">
                    <div class="field">
                        <g:actionSubmit action="abort" value="${message(code: 'default.usage.adminPage.button.abortProcess')}" class="ui button red" onclick="return confirm('${message(code:'confirm.start.StatsAbort')}')"/>
                    </div>
                </g:if>
            </div>
    </g:form>
</semui:filter>
<div class="ui mini message">
    <i class="close icon"></i>
    <ul class="list">
        <li>Anbieter sind nur auswählbar, wenn ein Anbieter mit konfiguriertem statssid Identifier existiert</li>
        <li>Einrichtungen sind nur auswählbar, wenn ein wibid Identifier dafür gespeichert ist</li>
        <li>statssid und WIBID müssen für einen erfolgreichen Abruf zu den IDs im Statistikserver passen</li>
        <li>Das Matching der Titel erfolgt über die Titel ZDB ID. Diese IDs müssen sowohl im Statistikserver als auch in LAS:eR existieren</li>
        <li>Für den Abruf von Statistiken ist pro Einrichtung eine Requestor ID und ein API Key erforderlich</li>
    </ul>
</div>
<h3 class="ui header">${message(code: 'default.usage.adminPage.infoHeader')}</h3>
<table class="ui celled la-table table compact">
    <tr><td>SUSHI API Url</td><td>
        <g:if test="${grailsApplication.config.statsApiUrl}">
            ${grailsApplication.config.statsApiUrl}
        </g:if>
        <g:else>
            <div class="ui red basic label">SUSHI API Url required</div>
        </g:else>
    </td></tr>
    <tr><td>${message(code: 'default.usage.adminPage.info.numCursor')}</td><td>
    <div class="ui relaxed divided list">
    <g:each in="${cursorCount}" var="cc">
        <div class="item">${cc[0]}: ${cc[1]}</div>
    </g:each>
    </div>
        </td></tr>
</table>

<semui:filter>
    <g:form action="index" controller="usage" method="get" class="form-inline ui small form">

        <div class="two fields">
            <div class="field fieldcontain">
                <label>${message(code: 'default.usage.adminPage.supplierLabel')}</label>
                <g:select class="ui dropdown" name="supplier"
                          from="${natstatProviders}"
                          value="${params.supplier}"
                          noSelection="${['': message(code: 'default.select.choose.label')]}"/>
            </div>
            <div class="field fieldcontain">
                <label>${message(code: 'default.usage.adminPage.institutionLabel')}</label>
                <g:select class="ui dropdown" name="institution"
                          from="${natstatInstitutions}"
                          value="${params.institution}"
                          noSelection="${['': message(code: 'default.select.choose.label')]}"/>
            </div>
        </div>

        <div class="field">
            <div class="field la-filter-search">
                <a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.reset.label')}</a>
                <input type="submit" class="ui secondary button" value="${message(code:'default.button.filter.label', default:'Filter')}">
            </div>
        </div>
    </g:form>
</semui:filter>

<table class="ui sortable celled la-table table compact">
  <thead>
  <tr>
    <g:sortableColumn property="customerId" title="Customer" params="${params}"/>
    <g:sortableColumn property="supplierId" title="Supplier" params="${params}"/>
    <g:sortableColumn property="haveUpTo" title="Until" params="${params}"/>
    <g:sortableColumn property="numFacts" title="Fact Count" params="${params}"/>
    <g:sortableColumn property="factType" title="Report" params="${params}"/>
  </tr>
  </thead>
  <tbody>
  <g:each in="${availStatsRanges}" var="asr" status="i">
    <tr>
      <td>${asr.customerId}</td>
      <td>${asr.supplierId}</td>
      <td>${asr.haveUpTo}</td>
      <td>${asr.numFacts}</td>
      <td>${asr.factType.value}</td>
    </tr>
  </g:each>
  </tbody>
</table>
<semui:paginate action="index" controller="usage" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" max="${max}" total="${num_stc_rows}" />

<h3 class="ui header">${message(code: 'default.usage.adminPage.serviceInfoHeader')}</h3>
<table class="ui celled la-table table compact">
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