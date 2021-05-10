<%@ page import="de.laser.reporting.myInstitution.base.BaseDetails; de.laser.Org; de.laser.IdentifierNamespace; de.laser.Identifier; de.laser.helper.RDStore; de.laser.Subscription; de.laser.properties.PropertyDefinition; de.laser.properties.SubscriptionProperty;" %>
<laser:serviceInjection />

<g:render template="/myInstitution/reporting/details/base.part1" />

<div class="ui segment">
    <table class="ui table la-table compact">
        <thead>
        <tr>
            <th></th>
            <th>${message(code:'subscription.label')}</th>
            <g:if test="${query == 'subscription-x-property'}">
                <th>${message(code:'reporting.details.property.value')}</th>
            </g:if>
            <g:elseif test="${query == 'subscription-x-platform'}">
                <th>${message(code:'default.provider.label')}</th>
            </g:elseif>
            <g:elseif test="${query == 'subscription-x-identifier'}">
                <th>${message(code:'identifier.label')}</th>
            </g:elseif>
            <g:elseif test="${query == 'subscription-x-subscription'}">
                <th>${message(code:'subscription.details.consortiaMembers.label')}</th>
            </g:elseif>
            <g:else>
                <g:if test="${contextService.getOrg().getCustomerType() == 'ORG_CONSORTIUM'}">
                    <th>${message(code:'subscription.details.consortiaMembers.label')}</th>
                </g:if>
                <g:elseif test="${contextService.getOrg().getCustomerType() == 'ORG_INST'}">
                </g:elseif>
            </g:else>
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

                    <g:if test="${query == 'subscription-x-property'}">
                        <td>
                            <%
                                List<SubscriptionProperty> properties = BaseDetails.getPropertiesGeneric(sub, id as Long, contextService.getOrg()) as List<SubscriptionProperty>

                                println properties.collect { sp ->
                                    String result = (sp.type.tenant?.id == contextService.getOrg().id) ? '<i class="icon shield alternate"></i>' : ''

                                    if (sp.getType().isRefdataValueType()) {
                                        result += (sp.getRefValue() ? sp.getRefValue().getI10n('value') : '')
                                    } else {
                                        result += (sp.getValue() ?: '')
                                    }
                                    result
                                }.sort().findAll().join(' ,<br/>') // removing empty and null values
                            %>
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
                    <g:elseif test="${query == 'subscription-x-subscription'}">
                        <td>
                        <%
                            Org.executeQuery('select oo.org from Subscription s join s.orgRelations oo where s = :sub and oo.roleType in :subscriberRoleTypes',
                                    [sub: sub, subscriberRoleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]]
                            ).each { o ->
                                println g.link( o.name, controller: 'organisation', action: 'show', id: o.id, ) + '<br />'
                            }
                        %>
                        </td>
                    </g:elseif>
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

<g:render template="/myInstitution/reporting/export/chartDetailsModal" model="[modalID: 'chartDetailsExportModal', token: token]" />


