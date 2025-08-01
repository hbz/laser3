<%@ page import="de.laser.remote.Wekb; de.laser.ui.Icon; de.laser.reporting.report.ElasticSearchHelper; de.laser.reporting.report.myInstitution.base.BaseDetails; de.laser.properties.OrgProperty; de.laser.IdentifierNamespace; de.laser.Identifier; de.laser.storage.RDStore; de.laser.Org; de.laser.properties.PropertyDefinition;" %>
<laser:serviceInjection />

<laser:render template="/myInstitution/reporting/details/details_top" />

<div class="ui segment">
    <table class="ui table la-js-responsive-table la-table compact">
        <thead>
        <tr>
            <th></th>
            <th>${message(code:'vendor.abbreviatedName.label')}</th>
            <th>${labels.first().trim() in ['Verteilung', 'Distribution'] ? 'Name' : labels.first().trim()}</th>%{-- TODO --}%
            <g:if test="${query == 'vendor-x-property'}">
                <th>${message(code:'reporting.details.property.value')}</th>
            </g:if>
            <th>${message(code:'vendor.homepage.label')}</th>
            <th>${message(code:'wekb')}</th>
        </tr>
        </thead>
        <tbody>
            <g:each in="${list}" var="vendor" status="i">
                <tr>
                    <td>${i + 1}.</td>
                    <td>${vendor.abbreviatedName}</td>
                    <td>
                        <g:link controller="vendor" action="show" id="${vendor.id}" target="_blank">${vendor.name}</g:link>
                    </td>
                    <g:if test="${query == 'vendor-x-property'}">
                        <td>
                            <uiReporting:objectProperties owner="${vendor}" tenant="${contextService.getOrg()}" propDefId="${id}" />
                        </td>
                    </g:if>
                    <td>
                        <g:if test="${vendor.homepage}">
                            <a href="${vendor.homepage}" target="_blank"> ${vendor.homepage} </a>
                        </g:if>
                    </td>
                    <td>
                        <g:if test="${vendor.gokbId}">
                            <a href="${Wekb.getURL() + '/resource/show/' + vendor.gokbId}" target="_blank">
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
