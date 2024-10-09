<%@ page import="de.laser.storage.RDStore; de.laser.Org;" %>
<laser:serviceInjection />

<laser:render template="/subscription/reporting/details/timeline/base.part1" />

<g:if test="${minusList}">
    <div class="ui top attached stackable tabular la-tab-with-js menu">
        <a data-tab="positive" class="item active">${message(code:'reporting.local.subscription.timeline.chartLabel.member.3')}</a>
        <a data-tab="minus" class="item">${message(code:'reporting.local.subscription.timeline.chartLabel.member.1')}</a>
    </div>
    <div data-tab="positive" class="ui bottom attached tab segment active">
</g:if>
<g:else>
    <div class="ui segment">
</g:else>

        <table class="ui table la-js-responsive-table la-table compact">
            <thead>
            <tr>
                <th scope="col" class="center aligned">
                    ${message(code:'sidewide.number')}
                </th>
                <th scope="col">${message(code:'org.sortname.label')}</th>
                <th scope="col">${message(code:'default.name.label')}</th>
                <th scope="col">${message(code:'org.libraryType.label')}</th>
                <th scope="col">${message(code:'org.libraryNetwork.label')}</th>
%{--                <th scope="col">${message(code:'org.orgType.label')}</th>--}%
                <th scope="col">${message(code:'org.customerType.label')}</th>
            </tr>
            </thead>
            <tbody>
                <g:each in="${list}" var="org" status="i">
                    <tr>
                        <g:if test="${plusList.contains(org)}">
                            <td class="center aligned"><span class="ui label circular green">${i + 1}</span></td>
                        </g:if>
                        <g:else>
                            <td class="center aligned">${i + 1}</td>
                        </g:else>
                        <td>${org.sortname}</td>
                        <td>
                            <g:link controller="organisation" action="show" id="${org.id}" target="_blank">${org.name}</g:link>
                        </td>
                        <td>${org.libraryType?.getI10n('value')}</td>
                        <td>${org.libraryNetwork?.getI10n('value')}</td>
%{--                        <td>--}%
%{--${org.orgType_new?.getI10n("value")}--}%
%{--                        </td>--}%
                        <td>${org.getCustomerTypeI10n()}</td>
                    </tr>
                </g:each>
            </tbody>
        </table>
    </div>

<g:if test="${minusList}">

    <div data-tab="minus" class="ui bottom attached tab segment">
        <table class="ui table la-js-responsive-table la-table compact">
            <thead>
            <tr>
                <th scope="col" class="center aligned">
                    ${message(code:'sidewide.number')}
                </th>
                <th scope="col">${message(code:'org.sortname.label')}</th>
                <th scope="col">${message(code:'default.name.label')}</th>
                <th scope="col">${message(code:'org.libraryType.label')}</th>
                <th scope="col">${message(code:'org.libraryNetwork.label')}</th>
%{--                <th scope="col">${message(code:'org.orgType.label')}</th>--}%
                <th scope="col">${message(code:'org.customerType.label')}</th>
            </tr>
            </thead>
            <tbody>
                <g:each in="${minusList}" var="org" status="i">
                    <tr>
                        <td class="center aligned"><span class="ui label circular red">${i + 1}</span></td>
                        <td>${org.sortname}</td>
                        <td>
                            <g:link controller="organisation" action="show" id="${org.id}" target="_blank">${org.name}</g:link>
                        </td>
                        <td>${org.libraryType?.getI10n('value')}</td>
                        <td>${org.libraryNetwork?.getI10n('value')}</td>
%{--                        <td>--}%
%{--${org.orgType_new?.getI10n("value")}--}%
%{--                        </td>--}%
                        <td>${org.getCustomerTypeI10n()}</td>
                    </tr>
                </g:each>
            </tbody>
        </table>
    </div>
</g:if>

<laser:render template="/subscription/reporting/export/detailsModal" model="[modalID: 'detailsExportModal', token: token]" />