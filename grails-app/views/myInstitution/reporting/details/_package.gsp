<%@ page import="de.laser.reporting.export.GlobalExportHelper; de.laser.reporting.report.myInstitution.base.BaseConfig; de.laser.reporting.report.myInstitution.base.BaseFilter; de.laser.ApiSource; de.laser.helper.RDStore; de.laser.reporting.report.myInstitution.base.BaseDetails;" %>
<laser:serviceInjection />

<g:render template="/myInstitution/reporting/details/top" />

<g:set var="filterCache" value="${GlobalExportHelper.getFilterCache(token)}"/>
<g:set var="esRecordIds" value="${filterCache.data.packageESRecords.keySet().collect{Long.parseLong(it)}}"/>
<g:set var="wekb" value="${ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)}"/>

<div class="ui segment">
    <table class="ui table la-table compact">
        <thead>
        <tr>
            <th></th>
            <th>${message(code:'package.label')}</th>
            <th>${message(code:'package.content_provider')}</th>
            <th>${message(code:'package.nominalPlatform')}</th>
            <th>${message(code:'package.lastUpdated.label')}</th>
            <th>${message(code:'wekb')}</th>
        </tr>
        </thead>
        <tbody>
            <g:each in="${list}" var="pkg" status="i">
                <tr>
                    <td>${i + 1}.</td>
                    <td>
                        <g:link controller="package" action="show" id="${pkg.id}" target="_blank">${pkg.name}</g:link>
                    </td>
                    <td>
                        <g:each in="${pkg.orgs.findAll{ it.roleType in [ RDStore.OR_PROVIDER, RDStore.OR_CONTENT_PROVIDER ]}}" var="ro">
                            %{-- ${ro.roleType.id}  ${ro.roleType.getI10n('value')} --}%
                            <g:link controller="org" action="show" id="${ro.org.id}" target="_blank">${ro.org.shortname ?: ro.org.name}</g:link><br />
                        </g:each>
                    </td>
                    <td>
                        <g:if test="${pkg.nominalPlatform}">
                            <g:link controller="platform" action="show" id="${pkg.nominalPlatform.id}" target="_blank">${pkg.nominalPlatform.name}</g:link>
                        </g:if>
                    </td>
                    <td>
                        <g:formatDate format="${message(code:'default.date.format.notime')}" date="${pkg._getCalculatedLastUpdated()}" />
                    </td>
                    <td>
                        <g:if test="${wekb?.baseUrl && pkg.gokbId}">
                            <g:if test="${esRecordIds.contains(pkg.id)}">
                                <a href="${wekb.baseUrl + '/public/packageContent/' + pkg.gokbId}" target="_blank"><i class="icon external alternate"></i></a>
                            </g:if>
                            <g:else>
                                <p class="la-long-tooltip la-popup-tooltip la-delay" data-content="${message(code:'reporting.query.base.noCounterpart.label')}"
                                   data-position="top right">
                                    <i class="icon times red"></i>
                                </p>
                            </g:else>
                        </g:if>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>
</div>

<g:render template="/myInstitution/reporting/export/detailsModal" model="[modalID: 'detailsExportModal', token: token]" />
