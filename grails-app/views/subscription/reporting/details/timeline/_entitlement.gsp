<%@ page import="de.laser.IssueEntitlement; de.laser.helper.RDStore; de.laser.TitleInstancePackagePlatform;" %>
<laser:serviceInjection />

<g:render template="/subscription/reporting/details/timeline/base.part1" />

<g:if test="${minusList}">
    <div class="ui top attached stackable tabular la-tab-with-js menu">
        <a data-tab="positive" class="item active">${message(code:'reporting.local.subscription.timeline.chartLabel.entitlement.3')}</a>
        <a data-tab="minus" class="item">${message(code:'reporting.local.subscription.timeline.chartLabel.entitlement.1')}</a>
    </div>
    <div data-tab="positive" class="ui bottom attached tab segment active">
</g:if>
<g:else>
    <div class="ui segment">
</g:else>

        <g:set var="plusListNames" value="${plusList.collect{ it.name }}" />

        <table class="ui table la-js-responsive-table la-table compact">
            <thead>
            <tr>
                <th scope="col" class="center aligned">
                    ${message(code:'sidewide.number')}
                </th>
                <th scope="col">${message(code:'tipp.name')}</th>
                <th scope="col">${message(code:'tipp.titleType')} / ${message(code:'tipp.medium')}</th>
            </tr>
            </thead>
            <tbody>
                <g:each in="${list}" var="tipp" status="i">
                    <g:if test="${plusListNames.contains(tipp.name)}">
                        <tr>
                            <td class="center aligned"><span class="ui label circular green">${i + 1}</span></td>
                    </g:if>
                    <g:else>
                        <td class="center aligned">${i + 1}</td>
                    </g:else>
                        <td>
                            <%
                                Long ieId = IssueEntitlement.executeQuery(
                                    'select ie.id from IssueEntitlement ie where ie.subscription.id = :id and ie.tipp = :tipp',
                                    [id: id, tipp: tipp]
                                )[0]
                            %>
                            <g:link controller="issueEntitlement" action="show" id="${ieId}" target="_blank">${tipp.name}</g:link>
                        </td>
                        <td>
                            ${tipp.titleType}
                            <g:if test="${tipp.medium}">
                                <g:if test="${tipp.titleType}"> / </g:if>
                                ${tipp.medium.getI10n('value')}
                            </g:if>
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
                <th scope="col">${message(code:'tipp.name')}</th>
                <th scope="col">${message(code:'tipp.titleType')} / ${message(code:'tipp.medium')}</th>
            </tr>
            </thead>
            <tbody>
                <g:each in="${minusList}" var="tipp" status="i">
                    <tr>
                        <td class="center aligned"><span class="ui label circular red">${i + 1}</span></td>
                        <td>
                            <%
                                Long ieId2 = IssueEntitlement.executeQuery(
                                        'select ie.id from IssueEntitlement ie where ie.subscription.id = :id and ie.tipp = :tipp',
                                        [id: id, tipp: tipp]
                                )[0]
                            %>
                            <g:link controller="issueEntitlement" action="show" id="${ieId2}" target="_blank">${tipp.name}</g:link>
                        </td>
                        <td>
                            ${tipp.titleType}
                            <g:if test="${tipp.medium}">
                                <g:if test="${tipp.titleType}"> / </g:if>
                                ${tipp.medium.getI10n('value')}
                            </g:if>
                        </td>
                    </tr>
                </g:each>
            </tbody>
        </table>
    </div>
</g:if>

<g:render template="/subscription/reporting/details/loadJavascript"  />
<g:render template="/subscription/reporting/export/detailsModal" model="[modalID: 'detailsExportModal', token: token]" />