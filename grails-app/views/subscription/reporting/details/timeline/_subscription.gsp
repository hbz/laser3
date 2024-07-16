<%@ page import="de.laser.ui.Icon; de.laser.Links; de.laser.Subscription; de.laser.storage.RDStore; de.laser.Org;" %>
<laser:serviceInjection />

<laser:render template="/subscription/reporting/details/timeline/base.part1" />

<%
    List orgSubList = []
    if (list) {
        orgSubList = Org.executeQuery(
                    'select org.id, sub.id from OrgRole oo join oo.org org join oo.sub sub where sub in (:list) and oo.roleType in :subscriberRoleTypes ' +
                    'order by org.sortname, org.name, sub.name, sub.startDate, sub.referenceYear, sub.endDate',
                    [list: list, subscriberRoleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]]
                )
    }
%>

<div class="ui segment">
    <table class="ui table la-js-responsive-table la-table compact">
        <thead>
        <tr>
            <th scope="col" class="center aligned">
                ${message(code:'sidewide.number')}
            </th>
            <th scope="col">${message(code:'subscription.label')}</th>
            <th scope="col"></th>
            <th scope="col">${message(code:'consortium.subscriber')}</th>
            <th scope="col">${message(code:'default.status.label')}</th>
            <g:if test="${query == 'timeline-annualMember-subscription'}">
                <th scope="col">${message(code:'subscription.referenceYear.label.shy')}</th>
            </g:if>
            <th scope="col">${message(code:'subscription.startDate.label')}</th>
            <th scope="col">${message(code:'subscription.endDate.label')}</th>
        </tr>
        </thead>
        <tbody>
            <g:each in="${orgSubList}" var="orgSub" status="i">
                <g:set var="org" value="${Org.get(orgSub[0])}" />
                <g:set var="sub" value="${Subscription.get(orgSub[1])}" />
                <tr>
                    <td class="center aligned">${i + 1}</td>
                    <td>
                        <g:link controller="subscription" action="show" id="${sub.id}" target="_blank">${sub.name}</g:link>
                    </td>
                    <td>
                        <%
                            Map<String, List> navMap = linksGenerationService.generateNavigation(sub, false)
                            navMap.prevLink.each { prevSub ->
                                print g.link([controller: 'subscription', action: 'show', id: prevSub], '<i class="' + Icon.LNK.PREV + '"></i>' )
                            }
                            navMap.nextLink.each { nextSub ->
                                print g.link([controller: 'subscription', action: 'show', id: nextSub], '<i class="' + Icon.LNK.NEXT + '"></i>' )
                            }
                        %>
                    </td>
                    <td>
                        <g:link controller="organisation" action="show" id="${org.id}" target="_blank">${org.sortname ?: org.name}</g:link>
                    </td>
                    <td>
                        ${sub.status.getI10n('value')}
                    </td>
                    <g:if test="${query == 'timeline-annualMember-subscription'}">
                        <td>
                            ${sub.referenceYear}
                        </td>
                    </g:if>
                    <td>
                        <g:formatDate format="${message(code:'default.date.format.notime')}" date="${sub.startDate}" />
                    </td>
                    <td>
                        <g:formatDate format="${message(code:'default.date.format.notime')}" date="${sub.endDate}" />
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>
</div>

<laser:render template="/subscription/reporting/export/detailsModal" model="[modalID: 'detailsExportModal', token: token]" />