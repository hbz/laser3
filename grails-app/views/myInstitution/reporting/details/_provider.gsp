<%@ page import="de.laser.reporting.report.ElasticSearchHelper; de.laser.reporting.report.myInstitution.base.BaseDetails; de.laser.properties.OrgProperty; de.laser.IdentifierNamespace; de.laser.Identifier; de.laser.storage.RDStore; de.laser.Org; de.laser.properties.PropertyDefinition;" %>
<laser:serviceInjection />
<g:set var="wekb" value="${ElasticSearchHelper.getCurrentApiSource()}"/>

<laser:render template="/myInstitution/reporting/details/details_top" />

<div class="ui segment">
    <table class="ui table la-js-responsive-table la-table compact">
        <thead>
        <tr>
            <th></th>
            <th>${message(code:'org.sortname.label')}</th>
            %{-- TODO --}%<th>${labels.first().trim() in ['Verteilung', 'Distribution'] ? 'Name' : labels.first().trim()}</th>%{-- TODO --}%
%{--            <g:if test="${query == 'org-country'}">--}%
%{--                <th>${message(code:'org.region.label')}</th>--}%
%{--            </g:if>--}%
            <g:if test="${query == 'org-x-property'}">
                <th>${message(code:'reporting.details.property.value')}</th>
            </g:if>
%{--            <g:elseif test="${query == 'org-x-identifier'}">--}%
%{--                <th>${message(code:'identifier.label')}</th>--}%
%{--            </g:elseif>--}%
            <g:if test="${query.startsWith('provider-')}">
                <th>${message(code:'org.platforms.label')}</th>
                <th>${message(code:'wekb')}</th>
            </g:if>
            %{--<th></th>--}%
        </tr>
        </thead>
        <tbody>
            <g:each in="${list}" var="org" status="i">
                <tr>
                    <td>${i + 1}.</td>
                    <td>${org.sortname}</td>
                    <td>
                        <g:link controller="provider" action="show" id="${org.id}" target="_blank">${org.name}</g:link>
                    </td>
%{--                    <g:if test="${query == 'org-country'}">--}%
%{--                        <td>--}%
%{--                            ${org.region?.getI10n('value')}--}%
%{--                        </td>--}%
%{--                    </g:if>--}%
                    <g:if test="${query == 'org-x-property'}">
                        <td>
                            <uiReporting:objectProperties owner="${org}" tenant="${contextService.getOrg()}" propDefId="${id}" />
                        </td>
                    </g:if>
%{--                    <g:elseif test="${query == 'org-x-identifier'}">--}%
%{--                        <td>--}%
%{--                            <%--}%
%{--                                List<Identifier> identList = Identifier.findAllByOrgAndNs(org, IdentifierNamespace.get(id))--}%
%{--                                println identList.collect{ it.value ?: null }.findAll().join(' ,<br/>') // removing empty and null values--}%
%{--                            %>--}%
%{--                        </td>--}%
%{--                    </g:elseif>--}%
                    <g:if test="${query.startsWith('provider-')}">
                        <td>
                            <g:each in="${org.platforms}" var="plt">
                                <g:link controller="platform" action="show" id="${plt.id}" target="_blank">${plt.name}</g:link><br/>
                            </g:each>
                        </td>
                        <td>
                            <g:if test="${wekb?.baseUrl && org.gokbId}">
                                <a href="${wekb.baseUrl + '/resource/show/' + org.gokbId}" target="_blank">
                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-content="${message(code:'reporting.chart.result.link.unchecked.label')}"
                                            data-position="top right">
                                        <i class="icon external alternate grey"></i>
                                    </span>
                                </a>
                            </g:if>
                        </td>
                    </g:if>
                </tr>
            </g:each>
        </tbody>
    </table>
</div>

%{--<laser:render template="/templates/copyEmailaddresses" model="[modalID: 'detailsCopyEmailModal', orgList: list]" />--}%

%{--<laser:render template="/myInstitution/reporting/export/detailsModal" model="[modalID: 'detailsExportModal', token: token]" />--}%
