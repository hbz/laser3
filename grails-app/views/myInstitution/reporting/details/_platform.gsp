<%@ page import="de.laser.helper.DateUtils; de.laser.reporting.export.GlobalExportHelper; de.laser.reporting.report.myInstitution.base.BaseConfig; de.laser.reporting.report.myInstitution.base.BaseFilter; de.laser.ApiSource; de.laser.helper.RDStore; de.laser.reporting.report.myInstitution.base.BaseDetails;" %>
<laser:serviceInjection />

<g:render template="/myInstitution/reporting/details/top" />

<g:set var="filterCache" value="${GlobalExportHelper.getFilterCache(token)}"/>
<g:set var="esRecords" value="${filterCache.data.platformESRecords}"/>
<g:set var="esRecordIds" value="${esRecords.keySet().collect{Long.parseLong(it)} ?: []}"/>
<g:set var="wekb" value="${ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)}"/>

<div class="ui segment">
    <table class="ui table la-table compact">
        <thead>
        <tr>
            <th></th>
            <th>${message(code:'platform.label')}</th>
            <th>${message(code:'platform.provider')}</th>
            <th>${message(code:'package.lastUpdated.label')}</th>
            <th>${message(code:'wekb')}</th>
        </tr>
        </thead>
        <tbody>
            <g:each in="${list}" var="plt" status="i">
                <tr>
                    <td>${i + 1}.</td>
                    <td>
                        <g:link controller="platform" action="show" id="${plt.id}" target="_blank">${plt.name}</g:link>
                    </td>
                    <td>
                        <g:if test="${plt.org}">
                            <g:link controller="org" action="show" id="${plt.org.id}" target="_blank">${plt.org.sortname ?: plt.org.name}</g:link>
                        </g:if>
                    </td>
                    <td>
                        <g:if test="${esRecordIds.contains(plt.id)}">
                            <g:formatDate format="${message(code:'default.date.format.notime')}" date="${DateUtils.parseDateGeneric(esRecords.getAt(plt.id.toString()).lastUpdatedDisplay)}" />
                        </g:if>
                        <g:else>
                            <g:formatDate format="${message(code:'default.date.format.notime')}" date="${plt._getCalculatedLastUpdated()}" />
                        </g:else>
                    </td>
                    <td class="ui center aligned">
                        <g:if test="${wekb?.baseUrl && plt.gokbId}">
                            <g:if test="${esRecordIds.contains(plt.id)}">
                                <a href="${wekb.baseUrl + '/public/platformContent/' + plt.gokbId}" target="_blank"><i class="icon external alternate"></i></a>
                            </g:if>
                            <g:else>
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-content="${message(code:'reporting.query.base.noCounterpart.label')}"
                                   data-position="top right">
                                    <i class="icon times grey"></i>
                                </span>
                            </g:else>
                        </g:if>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>
</div>

%{-- TODO <g:render template="/myInstitution/reporting/export/detailsModal" model="[modalID: 'detailsExportModal', token: token]" /> --}%
