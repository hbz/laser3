<%@ page import="de.laser.remote.Wekb; de.laser.ui.Icon; de.laser.reporting.report.ElasticSearchHelper; de.laser.reporting.report.myInstitution.base.BaseDetails; de.laser.properties.OrgProperty; de.laser.IdentifierNamespace; de.laser.Identifier; de.laser.storage.RDStore; de.laser.Org; de.laser.properties.PropertyDefinition;" %>
<laser:serviceInjection />

<laser:render template="/myInstitution/reporting/details/details_top" />

<div class="ui segment">
    <table class="ui table la-js-responsive-table la-table compact">
        <thead>
        <tr>
            <th></th>
            <th>${message(code:'provider.abbreviatedName.label')}</th>
            <th>${labels.first().trim() in ['Verteilung', 'Distribution'] ? 'Name' : labels.first().trim()}</th>%{-- TODO --}%
            <g:if test="${query == 'provider-x-property'}">
                <th>${message(code:'reporting.details.property.value')}</th>
            </g:if>
%{--            <th>${message(code:'org.platforms.label')}</th>--}%
            <th>${message(code:'vendor.homepage.label')}</th>
            <th>${message(code:'wekb')}</th>
            %{--<th></th>--}%
        </tr>
        </thead>
        <tbody>
            <g:each in="${list}" var="provider" status="i">
                <tr>
                    <td>${i + 1}.</td>
                    <td>${provider.abbreviatedName}</td>
                    <td>
                        <g:link controller="provider" action="show" id="${provider.id}" target="_blank">${provider.name}</g:link>
                    </td>
                    <g:if test="${query == 'provider-x-property'}">
                        <td>
                            <uiReporting:objectProperties owner="${provider}" tenant="${contextService.getOrg()}" propDefId="${id}" />
                        </td>
                    </g:if>
%{--                    <td>--}%
%{--                            <g:each in="${org.platforms}" var="plt">--}%
%{--                                <g:link controller="platform" action="show" id="${plt.id}" target="_blank">${plt.name}</g:link><br/>--}%
%{--                            </g:each>--}%
%{--                    </td>--}%
                    <td>
                        <g:if test="${provider.homepage}">
                            <a href="${provider.homepage}" target="_blank"> ${provider.homepage} </a>
                        </g:if>
                    </td>
                    <td>
                        <g:if test="${provider.gokbId}">
                            <a href="${Wekb.getURL() + '/resource/show/' + provider.gokbId}" target="_blank">
                                <span class="la-long-tooltip la-popup-tooltip" data-content="${message(code:'reporting.chart.result.link.unchecked.label')}"
                                        data-position="top right">
                                    <i class="${Icon.LNK.EXTERNAL} grey"></i>
                                </span>
                            </a>
                        </g:if>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>
</div>

<laser:render template="/templates/copyEmailaddresses" model="[modalID: 'detailsCopyEmailModal', orgList: list]" />

<laser:render template="/myInstitution/reporting/export/detailsModal" model="[modalID: 'detailsExportModal', token: token]" />
