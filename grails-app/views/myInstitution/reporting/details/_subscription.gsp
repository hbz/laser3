<%@ page import="de.laser.wekb.Provider; de.laser.reporting.report.myInstitution.base.BaseDetails; de.laser.Org; de.laser.IdentifierNamespace; de.laser.Identifier; de.laser.storage.RDStore; de.laser.Subscription; de.laser.properties.PropertyDefinition; de.laser.properties.SubscriptionProperty;" %>
<laser:serviceInjection />

<laser:render template="/myInstitution/reporting/details/details_top" />

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
                <g:if test="${contextService.getOrg().isCustomerType_Consortium()}">
                    <th>${message(code:'subscription.details.consortiaMembers.label')}</th>
                </g:if>
                <g:elseif test="${contextService.getOrg().isCustomerType_Inst_Pro()}">
                </g:elseif>
            </g:else>
            <g:if test="${query in [ 'subscription-x-property', 'subscription-x-memberSubscriptionProperty' ]}">
                <th>${message(code:'reporting.details.property.value')}</th>
            </g:if>
            <g:elseif test="${query == 'subscription-x-platform'}">
                <th>${message(code:'provider.label')}</th>
            </g:elseif>
            <g:elseif test="${query == 'subscription-x-identifier'}">
                <th>${message(code:'identifier.label')}</th>
            </g:elseif>
            <g:elseif test="${query == 'subscription-x-memberProvider'}">
                <th>${message(code:'provider.label')}</th>
            </g:elseif>
            <g:if test="${! (query in ['subscription-x-referenceYear', 'subscription-x-memberReferenceYear'])}">
                <th>${message(code:'subscription.referenceYear.label')}</th>
            </g:if>
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
                                    println g.link( o.name, controller: 'organisation', action: 'show', id: o.id, target: '_blank') + '<br />'
                                }
                            %>
                        </td>
                    </g:if>
                    <g:else>
                        <g:if test="${contextService.getOrg().isCustomerType_Consortium()}">
                            <td>
                                <%
                                    println Subscription.executeQuery('select count(*) from Subscription s join s.orgRelations oo where s.instanceOf = :parent and oo.roleType in :subscriberRoleTypes',
                                            [parent: sub, subscriberRoleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]]
                                    )[0]
                                %>
                            </td>
                        </g:if>
                        <g:elseif test="${contextService.getOrg().isCustomerType_Inst_Pro()}">
                        </g:elseif>
                    </g:else>

                    <g:if test="${query in [ 'subscription-x-property', 'subscription-x-memberSubscriptionProperty' ]}">
                        <td>
                            <uiReporting:objectProperties owner="${sub}" tenant="${contextService.getOrg()}" propDefId="${id}" />
                        </td>
                    </g:if>
                    <g:elseif test="${query == 'subscription-x-platform'}">
                    <td>
                        <%
                            // todo: SubscriptionPackage -> Package -> Provider ?
                            // todo: SubscriptionPackage -> Package -> Platform -> Provider ?
                            Provider.executeQuery(
                                    'select pr.provider from ProviderRole pr where pr.subscription.id = :id order by pr.provider.sortname, pr.provider.name',
                                    [id: sub.id]
                            ).each { p ->
                                println g.link( p.name, controller: 'provider', action: 'show', id: p.id, target: '_blank') + '<br />'
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
                                // todo: SubscriptionPackage -> Package -> Provider ?
                                // todo: SubscriptionPackage -> Package -> Platform -> Provider ?
                                Provider.executeQuery(
                                        'select pr.provider from ProviderRole pr where pr.subscription.id = :id order by pr.provider.sortname, pr.provider.name',
                                        [id: sub.id]
                                ).each { p ->
                                    println g.link( p.name, controller: 'provider', action: 'show', id: p.id, target: '_blank') + '<br />'
                                }
                            %>
                        </td>
                    </g:elseif>

                    <g:if test="${! (query in ['subscription-x-referenceYear', 'subscription-x-memberReferenceYear'])}">
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

<laser:render template="/myInstitution/reporting/export/detailsModal" model="[modalID: 'detailsExportModal', token: token]" />


