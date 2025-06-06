<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.config.ConfigMapper" %>
<laser:htmlStart message="default.usage.adminPage.mainHeader" />

<ui:breadcrumbs>
    <ui:crumb message="menu.admin" controller="admin" action="index"/>
    <ui:crumb message="default.usage.adminPage.mainHeader" class="active"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="default.usage.adminPage.mainHeader" type="admin"/>

<ui:messages data="${flash}" />

<h2 class="ui header">${message(code: 'default.usage.adminPage.formHeader')}</h2>
<ui:filter simple="true">
    <g:form action="index" controller="usage" method="get" class="ui small form">
        <div class="three fields">
            <div class="field">
                <label for="supplier">${message(code: 'platform.label')}</label>
                <g:select class="ui dropdown clearable " id="supplier" name="supplier"
                              from="${providerList}"
                              optionKey="id"
                              optionDisabled="optionDisabled"
                              optionValue="name"
                              value="${params.supplier}"
                              noSelection="${[null: message(code: 'default.select.choose.label')]}"/>
            </div>
            <div class="field">
                <label for="institution">${message(code: 'default.usage.adminPage.institutionLabel')}</label>
                <g:select class="ui dropdown clearable " id="institution" name="institution"
                          from="${institutionList}"
                          optionKey="id"
                          optionValue="name"
                          value="${params.institution}"
                          noSelection="${[null: message(code: 'default.select.choose.label')]}"/>
            </div>
        </div>
            <div class="fields">
                <div class="field">
                    <g:actionSubmit action="fetchSelection" class="${Btn.PRIMARY}" value="${message(code: 'default.usage.adminPage.button.fetchSelection')}" onclick="return confirm('${message(code:'confirm.start.StatsSync')}')"/>
                </div>
                <div class="field">
                    <g:actionSubmit action="deleteSelection" class="${Btn.PRIMARY}" value="${message(code: 'default.usage.adminPage.button.deleteSelection')}" onclick="return confirm('${message(code:'confirm.start.StatsDeleteSelection')}')"/>
                </div>
                <div class="field">
                    <g:actionSubmit action="deleteAll" value="${message(code: 'default.usage.adminPage.button.deleteAll')}" class="${Btn.NEGATIVE}" onclick="return confirm('${message(code:'confirm.start.StatsDelete')}')"/>
                </div>
                <g:if test="${statsSyncService.running}">
                    <div class="field">
                        <g:actionSubmit action="abort" value="${message(code: 'default.usage.adminPage.button.abortProcess')}" class="${Btn.NEGATIVE}" onclick="return confirm('${message(code:'confirm.start.StatsAbort')}')"/>
                    </div>
                </g:if>
            </div>
    </g:form>
</ui:filter>
<div class="ui mini message">
    <i class="close icon"></i>
    <ul class="list">
        <li>Platformen sind nur auswählbar, wenn zu der Platform das Merkmal "NatStat Anbieter" konfiguriert wurde</li>
        <li>Einrichtungen sind nur auswählbar, wenn ein wibid Identifier dafür gespeichert ist</li>
        <li>Das "NatStat Anbieter" Merkmal und die WIBID müssen für einen erfolgreichen Abruf zu den IDs im Statistikserver passen (Vendor/WIBID)</li>
        <li>Das Matching der Titel erfolgt über die Titel ZDB ID oder bei Büchern über den DOI. Diese IDs müssen sowohl im Statistikserver als auch in LAS:eR existieren</li>
        <li>Für den Abruf von Statistiken ist pro Einrichtung eine Requestor ID und ein API Key erforderlich</li>
    </ul>
</div>
<h3 class="ui header">${message(code: 'default.usage.adminPage.infoHeader')}</h3>
<table class="ui celled la-js-responsive-table la-table table compact">
    <tr><td>SUSHI API Url</td><td>
        <g:if test="${ConfigMapper.getStatsApiUrl()}">
            ${ConfigMapper.getStatsApiUrl()}
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

<ui:filter simple="true">
    <g:form action="index" controller="usage" method="get" class="ui small form">

        <div class="two fields">
            <div class="field">
                <label for="supplier">${message(code: 'default.usage.adminPage.natStatSupplierLabel')}</label>
                <g:select class="ui dropdown clearable" id="supplier" name="supplier"
                          from="${natstatProviders}"
                          value="${params.supplier}"
                          noSelection="${['': message(code: 'default.select.choose.label')]}"/>
            </div>
            <div class="field">
                <label for="institution">${message(code: 'default.usage.adminPage.institutionLabel')}</label>
                <g:select class="ui dropdown clearable" name="institution"
                          from="${natstatInstitutions}"
                          optionKey="${{it.last().value}}"
                          optionValue="${{it.first().name + ' (' + it.last().value + ')'}}"
                          value="${params.institution}"
                          noSelection="${['': message(code: 'default.select.choose.label')]}"/>
            </div>
        </div>

        <div class="field">
            <div class="field la-filter-search">
                <a href="${request.forwardURI}" class="${Btn.SECONDARY} reset">${message(code:'default.button.reset.label')}</a>
                <input type="submit" class="${Btn.PRIMARY}" value="${message(code:'default.button.filter.label')}">
            </div>
        </div>
    </g:form>
</ui:filter>

<table class="ui sortable celled la-js-responsive-table la-table table compact">
  <thead>
  <tr>
    <g:sortableColumn property="customerId" title="Customer" params="${params}"/>
    <g:sortableColumn property="supplierId" title="${message(code: 'default.usage.adminPage.natStatSupplierLabel')}" params="${params}"/>
    <g:sortableColumn property="availFrom" title="Von" params="${params}"/>
    <g:sortableColumn property="availTo" title="Bis" params="${params}"/>
    <g:sortableColumn property="numFacts" title="Fact Count" params="${params}"/>
    <g:sortableColumn property="factType" title="Report" params="${params}"/>
    <th>Errors</th>
  </tr>
  </thead>
  <tbody>
  <g:each in="${availStatsRanges}" var="asr" status="i">
    <g:set var="fs" bean="factService"/>
    <g:set var="statsError" value="${fs.getStatsErrors(asr)}"/>

    <tr class="stats-error-row-${i}">
      <td>${asr.customerId}</td>
      <td>${asr.supplierId}</td>
      <td>${asr.availFrom}</td>
      <td>${asr.availTo}</td>
      <td>${asr.numFacts}</td>
      <td>${asr.factType.value}</td>

    <g:if test="${statsError.size()!=0}">
      <td class="x">
        <button class="${Btn.ICON.SIMPLE}" data-target="stats-error-content-${i}">
          <i class="${Icon.UI.INFO}"></i>
        </button>
      </td>
    </g:if>
    </tr>
    <g:if test="${statsError.size()!=0}">
    <tr class="stats-error-content-${i}" style="display:none">
      <td colspan="5">
        <div class="ui relaxed list">
            <div class="header">Fehler</div>
               <div class="item">
                 ${statsError.jerror.unique()}
               </div>
        </div>
      </td>
    </tr>
    </g:if>
  </g:each>
  </tbody>
</table>
<ui:paginate action="index" controller="usage" params="${params}" max="${max}" total="${num_stc_rows}" />

<h3 class="ui header">${message(code: 'default.usage.adminPage.serviceInfoHeader')}</h3>
<table class="ui celled la-js-responsive-table la-table table compact">
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
                ${ah.key}:${ah.value}<br />
            </g:each>
        </td></tr>
</table>
<laser:script file="${this.getGroovyPageFileName()}">
    $('tr[class*=stats-error-row] .button').click( function(){
      $('.' + $(this).attr('data-target')).toggle()
    })
</laser:script>
<laser:htmlEnd />