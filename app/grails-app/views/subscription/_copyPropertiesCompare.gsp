<%@page import="com.k_int.kbplus.Subscription"%>
<laser:serviceInjection/>
<!doctype html>
<html>
    %{--<head>--}%
        %{--<meta name="layout" content="semanticUI" />--}%
        %{--<title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'menu.my.comp_lic')}</title>--}%
    %{--</head>--}%
    <body>
    <semui:form>
        <g:render template="selectSourceAndTargetSubscription" model="[
                sourceSubscription: sourceSubscription,
                targetSubscription: targetSubscription,
                allSubscriptions_readRights: allSubscriptions_readRights,
                allSubscriptions_writeRights: allSubscriptions_writeRights]"/>
        <hr>
        <g:form action="copyElementsIntoSubscription" controller="subscription" id="${params.id ?: params.sourceSubscriptionId}"
                params="[workFlowPart: workFlowPart, sourceSubscriptionId: sourceSubscriptionId, targetSubscriptionId: targetSubscriptionId]" method="post" class="ui form newLicence">
            <div class="ui grid">
                <table class="ui la-table la-table-small table">
                    <%
                        List subscriptions = [Subscription.get(sourceSubscriptionId)]
                        if (targetSubscriptionId) subscriptions.add(Subscription.get(targetSubscriptionId))
                    %>

                    <g:set var="subscriptionsCount" value="${subscriptions?.size()}"/>
                    <thead>
                        <th class="center aligned">${message(code: 'default.copy.label')}</th>
                        <th class="center aligned">${message(code: 'default.replace.label')}</th>
                        <th class="center aligned">${message(code: 'default.doNothing.label')}</th>
                        <th colspan="${subscriptionsCount}">${message(code:'property.table.property')}</th>
                    </thead>
                    <tbody>
                        <g:each in="${groupedProperties}" var="groupedProps">
                            <%-- leave it for debugging
                            <tr>
                                <td colspan="999">${groupedProps}</td>
                            </tr>--%>
                            <g:if test="${groupedProps.getValue()}">
                                <g:render template="propertyComparisonTableRow" model="[group:groupedProps.getValue().groupTree,key:groupedProps.getKey().name,propBinding:groupedProps.getValue().binding,subscriptions:subscriptions]" />
                            </g:if>
                        </g:each>
                        <g:if test="${orphanedProperties.size() > 0}">
                            <g:render template="propertyComparisonTableRow" model="[group:orphanedProperties,key:message(code:'license.properties'),subscriptions:subscriptions]" />
                        </g:if>
                        <g:if test="${privateProperties.size() > 0}">
                            <g:render template="propertyComparisonTableRow" model="[group:privateProperties,key:message(code:'license.properties.private')+' '+contextService.getOrg().name,subscriptions:subscriptions]" />
                        </g:if>
                    </tbody>
                </table>
            </div>
            <div class="sixteen wide field" style="text-align: right;">
                <input type="submit" class="ui button js-click-control" value="Ausgewählte Merkmale in Ziellizenz kopieren" onclick="return jsConfirmation() "/>
            </div>
    </g:form>
    </semui:form>
    </body>
</html>
