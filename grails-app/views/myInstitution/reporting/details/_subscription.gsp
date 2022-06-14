<%@ page import="de.laser.reporting.report.myInstitution.base.BaseDetails; de.laser.Org; de.laser.IdentifierNamespace; de.laser.Identifier; de.laser.helper.RDStore; de.laser.Subscription; de.laser.properties.PropertyDefinition; de.laser.properties.SubscriptionProperty;" %>
<laser:serviceInjection />

<g:render template="/myInstitution/reporting/details/top" />

<div class="ui segment">
    <table class="ui table la-js-responsive-table la-table compact">
        <thead>
        <tr>
            <th></th>
            <th>${message(code:'subscription.label')}</th>
            <g:if test="${query.startsWith('subscription-x-member') || query.startsWith('memberSubscription-')}">
                <th>${message(code:'subscription.details.consortiaMembers.label')}</th>
            </g:if>
            <g:else>
                <g:if test="${contextService.getOrg().getCustomerType() == 'ORG_CONSORTIUM'}">
                    <th>${message(code:'subscription.details.consortiaMembers.label')}</th>
                </g:if>
                <g:elseif test="${contextService.getOrg().getCustomerType() == 'ORG_INST'}">
                </g:elseif>
            </g:else>
            <g:if test="${query in [ 'subscription-x-property', 'subscription-x-memberSubscriptionProperty' ]}">
                <th>${message(code:'reporting.details.property.value')}</th>
            </g:if>
            <g:elseif test="${query == 'subscription-x-platform'}">
                <th>${message(code:'default.provider.label')}</th>
            </g:elseif>
            <g:elseif test="${query == 'subscription-x-identifier'}">
                <th>${message(code:'identifier.label')}</th>
            </g:elseif>
            <g:elseif test="${query == 'subscription-x-memberProvider'}">
                <th>${message(code:'default.provider.label')}</th>
            </g:elseif>
            <th>${message(code:'subscription.startDate.label')}</th>
            <th>${message(code:'subscription.endDate.label')}</th>
        </tr>
        </thead>
        <tbody>
            <g:each in="${list}" var="sub" status="i">
                <tr>
                    <td>${i + 1}.</td>
                    <td>
                        <g:link controller="subscription" action="show" id="${sub.id}" target="_blank">${sub.name}</g:link>
                    </td>

                    <g:if test="${query.startsWith('subscription-x-member') || query.startsWith( 'memberSubscription-')}">
                        <td>
                            <%
                                Org.executeQuery('select oo.org from Subscription s join s.orgRelations oo where s = :sub and oo.roleType in :subscriberRoleTypes',
                                        [sub: sub, subscriberRoleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]]
                                ).each { o ->
                                    println g.link( o.name, controller: 'organisation', action: 'show', id: o.id, ) + '<br />'
                                }
                            %>
                        </td>
                    </g:if>
                    <g:else>
                        <g:if test="${contextService.getOrg().getCustomerType() == 'ORG_CONSORTIUM'}">
                            <td>
                                <%
                                    println Subscription.executeQuery('select count(s) from Subscription s join s.orgRelations oo where s.instanceOf = :parent and oo.roleType in :subscriberRoleTypes',
                                            [parent: sub, subscriberRoleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]]
                                    )[0]
                                %>
                            </td>
                        </g:if>
                        <g:elseif test="${contextService.getOrg().getCustomerType() == 'ORG_INST'}">
                        </g:elseif>
                    </g:else>

                    <g:if test="${query in [ 'subscription-x-property', 'subscription-x-memberSubscriptionProperty' ]}">
                        <td>
                            <laser:reportObjectProperties owner="${sub}" tenant="${contextService.getOrg()}" propDefId="${id}" />
                        </td>
                    </g:if>
                    <g:elseif test="${query == 'subscription-x-platform'}">
                    <td>
                        <%
                            Org.executeQuery('select ro.org from OrgRole ro where ro.sub.id = :id and ro.roleType in (:roleTypes)',
                                    [id: sub.id, roleTypes: [RDStore.OR_PROVIDER]]
                            ).each { p ->
                                println g.link( p.name, controller: 'organisation', action: 'show', id: p.id, ) + '<br />'
                            }
                        %>
                    </td>
                    </g:elseif>
                    <g:elseif test="${query == 'subscription-x-identifier'}">
                        <td>
                            <%
                                List<Identifier> identList = Identifier.findAllBySubAndNs(sub, IdentifierNamespace.get(id))
                                println identList.collect{ it.value ?: null }.findAll().join(' ,<br/>') // removing empty and null values
                            %>
                        </td>
                    </g:elseif>
                    <g:elseif test="${query == 'subscription-x-memberProvider'}">
                        <td>
                            <%
                                Org.executeQuery('select ro.org from OrgRole ro where ro.sub.id = :id and ro.roleType in (:roleTypes)',
                                        [id: sub.id, roleTypes: [RDStore.OR_PROVIDER]]
                                ).each { p ->
                                    println g.link( p.name, controller: 'organisation', action: 'show', id: p.id, ) + '<br />'
                                }
                            %>
                        </td>
                    </g:elseif>

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

<g:render template="/myInstitution/reporting/export/detailsModal" model="[modalID: 'detailsExportModal', token: token]" />


