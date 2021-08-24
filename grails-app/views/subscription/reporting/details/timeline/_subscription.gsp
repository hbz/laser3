<%@ page import="de.laser.helper.RDStore; de.laser.Org;" %>
<laser:serviceInjection />

<g:render template="/subscription/reporting/details/timeline/base.part1" />

<div class="ui segment">
    <table class="ui table la-table compact">
        <thead>
        <tr>
            <th></th>
            <th>${message(code:'subscription.label')}</th>
            <th>${message(code:'consortium.subscriber')}</th>
            <th>${message(code:'subscription.startDate.label')}</th>
            <th>${message(code:'subscription.endDate.label')}</th>
        </tr>
        </thead>
        <tbody>
            <g:each in="${list}" var="sub" status="i">
                <tr>
                    <td style="text-align: center">${i + 1}.</td>
                    <td>
                        <g:link controller="subscription" action="show" id="${sub.id}" target="_blank">${sub.name}</g:link>
                    </td>
                    <td>
                        <%
                            Org.executeQuery('select oo.org from Subscription s join s.orgRelations oo where s = :sub and oo.roleType in :subscriberRoleTypes',
                                    [sub: sub, subscriberRoleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]]
                            ).each { o ->
                                println g.link( o.name, controller: 'organisation', action: 'show', id: o.id, ) + '<br />'
                            }
                        %>
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

<g:render template="/subscription/reporting/export/detailsModal" model="[modalID: 'detailsExportModal', token: token]" />