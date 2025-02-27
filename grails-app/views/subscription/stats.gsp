<%@ page import="de.laser.properties.SubscriptionProperty; de.laser.storage.PropertyStore; java.text.SimpleDateFormat; grails.converters.JSON; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataValue; de.laser.utils.DateUtils; de.laser.Subscription; de.laser.Platform; de.laser.stats.Counter4Report; de.laser.stats.Counter5Report; de.laser.interfaces.CalculatedType; de.laser.base.AbstractReport; de.laser.finance.CostItem" %>
<laser:htmlStart message="subscription.details.stats.label" serviceInjection="true"/>
    <laser:javascript src="echarts.js"/>
        <ui:debugInfo>
            <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
        </ui:debugInfo>
        <laser:render template="breadcrumb" model="${[ params:params ]}"/>
        <ui:controlButtons>
            <laser:render template="actions" />
        </ui:controlButtons>

        <ui:h1HeaderWithIcon referenceYear="${subscription.referenceYear}" visibleProviders="${providerRoles}">
            <laser:render template="iconSubscriptionIsChild"/>
            ${subscription.name}
        </ui:h1HeaderWithIcon>
        <ui:anualRings object="${subscription}" controller="subscription" action="stats" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

        <laser:render template="nav" />

        <ui:objectStatus object="${subscription}" status="${subscription.status}" />
        <laser:render template="message" />
        <ui:messages data="${flash}" />

        <g:if test="${showConsortiaFunctions && !subscription.instanceOf}">
            <g:each in="${platformInstanceRecords.values()}" var="platform">
                <div class="ui segment">
                    <laser:render template="/platform/platformStatsDetails" model="[wekbServerUnavailable: wekbServerUnavailable, platformInstanceRecord: platform]"/>
                    <g:set var="statsInfo" value="${SubscriptionProperty.executeQuery('select sp from SubscriptionProperty sp where sp.owner = :subscription and sp.type = :statsAccess', [statsAccess: PropertyStore.SUB_PROP_STATS_ACCESS, subscription: subscription])}"/>
                    <g:if test="${statsInfo}">
                        <ui:msg icon="ui info icon" class="info" noClose="true"><%-- on remerge to DEV: header="${message(code: 'default.stats.info.header')}" --%>
                            ${statsInfo[0]}<br>
                            <g:message code="default.stats.noCounterSupport"/><br>
                            <%--<g:message code="default.stats.wekbContact"/><ui:wekbIconLink type="org" gokbId="${platform.providerUuid}"/>--%>
                        </ui:msg>
                    </g:if>
                    <g:if test="${platform.statisticsFormat.contains('COUNTER')}">
                        <%
                            Map<String, Object> platformSushiConfig = exportService.prepareSushiCall(platform, 'stats')
                        %>
                        <table class="ui celled table">
                            <tr>
                                <th><g:message code="default.number"/></th>
                                <th><g:message code="default.institution"/></th>
                                <th>Customer ID</th>
                                <th>Requestor ID/API-Key</th>
                                <th><g:message code="default.usage.sushiCallCheck.header"/></th>
                                <th><g:message code="default.actions.label"/></th>
                            </tr>
                            <g:each in="${Subscription.executeQuery('select new map(sub.id as memberSubId, org.sortname as memberName, org.id as memberId, ci as customerIdentifier) from CustomerIdentifier ci, OrgRole oo join oo.org org join oo.sub sub where ci.customer = org and sub.instanceOf = :parent and oo.roleType in (:subscrRoles) and ci.platform.gokbId = :platform order by ci.customer.sortname asc', [parent: subscription, platform: platform.uuid, subscrRoles: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]])}" var="row" status="i">
                                <tr>
                                    <td>${i+1}</td>
                                    <td>
                                        <g:link controller="organisation" action="show" id="${row.memberId}">${row.memberName}</g:link>
                                    </td>
                                    <td>
                                        <ui:xEditable owner="${row.customerIdentifier}" field="value"/>
                                    </td>
                                    <td>
                                        <ui:xEditable owner="${row.customerIdentifier}" field="requestorKey"/>
                                    </td>
                                    <td id="${genericOIDService.getHtmlOID(row.customerIdentifier)}" class="sushiConnectionCheck" data-org="${row.memberId}" data-platform="${platform.id}" data-customerId="${row.customerIdentifier.value}" data-requestorId="${row.customerIdentifier.requestorKey}">

                                    </td>
                                    <td>
                                        <g:link class="ui icon primary button la-modern-button" action="stats" id="${row.memberSubId}" role="button" aria-label="${message(code: 'default.usage.consortiaTableHeader')}"><i class="icon chart bar outline"></i></g:link>
                                    </td>
                                </tr>
                            </g:each>
                        </table>
                    </g:if>
                </div>
            </g:each>
        </g:if>
        <g:else>
            <g:render template="/templates/stats/stats"/>
        </g:else>

<laser:htmlEnd />
