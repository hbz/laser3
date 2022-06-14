<%@ page import="de.laser.Links; de.laser.Subscription; de.laser.helper.RDStore; de.laser.Org;" %>
<laser:serviceInjection />

<g:render template="/subscription/reporting/details/timeline/base.part1" />

<%
    List<Long,Long> orgSubList = []
    if (list) {
        orgSubList = Org.executeQuery(
                    'select org.id, sub.id from OrgRole oo join oo.org org join oo.sub sub where sub in (:list) and oo.roleType in :subscriberRoleTypes ' +
                    'order by org.sortname, org.name, sub.name, sub.startDate, sub.endDate',
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
            <th scope="col">${message(code:'org.sortname.label')}</th>
            <th scope="col">${message(code:'default.status.label')}</th>
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
                                print g.link([controller: 'subscription', action: 'show', id: prevSub], '<i class="icon arrow left"></i>' )
                            }
                            navMap.nextLink.each { nextSub ->
                                print g.link([controller: 'subscription', action: 'show', id: nextSub], '<i class="icon arrow right"></i>' )
                            }
                        %>
                    </td>
                    <td>
                        <g:link controller="organisation" action="show" id="${org.id}" target="_blank">${org.name}</g:link>
                    </td>
                    <td>
                        ${org.sortname}
                    </td>
                    <td>
                        ${sub.status.getI10n('value')}
                    </td>
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

<g:render template="/subscription/reporting/details/loadJavascript"  />
<g:render template="/subscription/reporting/export/detailsModal" model="[modalID: 'detailsExportModal', token: token]" />