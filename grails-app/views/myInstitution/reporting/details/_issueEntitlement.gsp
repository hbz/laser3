<%@ page import="de.laser.reporting.report.myInstitution.base.BaseDetails; de.laser.Org; de.laser.IdentifierNamespace; de.laser.Identifier; de.laser.storage.RDStore; de.laser.Subscription; de.laser.properties.PropertyDefinition; de.laser.properties.SubscriptionProperty;" %>
<laser:serviceInjection />

<laser:render template="/myInstitution/reporting/details/top" />

<div class="ui segment">
    <table class="ui table la-table compact">
        <thead>
        <tr>
            <th></th>
            <th>${message(code:'issueEntitlement.label')}</th>
            <th>${message(code:'package.label')}</th>
            <th>${message(code:'platform.label')}</th>
            <th>${message(code:'subscription.label')}</th>
            %{--<th>${message(code:'tipp.titleType')}</th>--}%
            <th>${message(code:'default.status.label')} (IE/TIPP)</th>
        </tr>
        </thead>
        <tbody>
            <g:each in="${list}" var="ie" status="i">
                <tr>
                    <td>${i + 1}.</td>
                    <td>
                        <g:link controller="issueEntitlement" action="show" id="${ie.id}" target="_blank">${ie.tipp.name}</g:link>
                        %{-- <g:link controller="tipp" action="show" id="${ie.tipp.id}" target="_blank">${ie.name}</g:link> --}%
                    </td>
                    <td>
                        <g:if test="${ie.tipp.pkg}">
                            <g:link controller="package" action="show" id="${ie.tipp.pkg.id}" target="_blank">${ie.tipp.pkg.name}</g:link>
                        </g:if>
                    </td>
                    <td>
                        <g:if test="${ie.tipp.pkg.nominalPlatform}">
                            <g:link controller="platform" action="show" id="${ie.tipp.pkg.nominalPlatform.id}" target="_blank">${ie.tipp.pkg.nominalPlatform.name}</g:link>
                        </g:if>
                    </td>
                    %{--<td>
                        ${ie.tipp.titleType}
                    </td>--}%
                    <td>
                        <g:if test="${ie.subscription}">
                            <g:link controller="subscription" action="show" id="${ie.subscription.id}" target="_blank">${ie.subscription.getLabel()}</g:link>
                        </g:if>
                    </td>
                    <td>
                        ${ie.status.getI10n('value')} / ${ie.tipp.status.getI10n('value')}
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>
</div>

%{-- <laser:render template="/myInstitution/reporting/export/detailsModal" model="[modalID: 'detailsExportModal', token: token]" /> --}%


