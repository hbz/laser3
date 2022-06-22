<%@ page import="de.laser.helper.RDStore; de.laser.Org;" %>
<laser:serviceInjection />

<g:render template="/subscription/reporting/details/timeline/base.part1" />

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
            </tr>
            </thead>
            <tbody>
                <g:each in="${list}" var="org" status="i">
                    <g:if test="${plusList.contains(org)}">
                        <tr>
                            <td class="center aligned"><span class="ui label circular green">${i + 1}</span></td>
                    </g:if>
                    <g:else>
                        <tr>
                            <td class="center aligned">${i + 1}</td>
                    </g:else>
                        <td>${org.sortname}</td>
                        <td>
                            <g:link controller="organisation" action="show" id="${org.id}" target="_blank">${org.name}</g:link>
                        </td>
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
                    </tr>
                </g:each>
            </tbody>
        </table>
    </div>
</g:if>

<g:render template="/subscription/reporting/details/loadJavascript"  />
<g:render template="/subscription/reporting/export/detailsModal" model="[modalID: 'detailsExportModal', token: token]" />