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
        <g:form action="copyElementsIntoSubscription" controller="subscription" id="${params.id ?: params.sourceSubscriptionId}"
                params="[workFlowPart: workFlowPart, sourceSubscriptionId: sourceSubscriptionId, targetSubscriptionId: targetSubscriptionId]" method="post" class="ui form newLicence">


                <%
                    List subscriptions = [Subscription.get(sourceSubscriptionId)]
                    if (targetSubscriptionId) subscriptions.add(Subscription.get(targetSubscriptionId))
                %>

                <g:set var="subscriptionsCount" value="${subscriptions?.size()}"/>

                    <g:if test="${customProperties.size() > 0}">
                        <table class="ui celled table la-table">
                            <g:render template="propertyComparisonTableRow" model="[group:customProperties,key:message(code:'license.properties'),subscriptions:subscriptions]" />
                        </table>
                    </g:if>


                    <g:if test="${privateProperties.size() > 0}">
                        <table class="ui celled table la-table">
                            <g:render template="propertyComparisonTableRow" model="[group:privateProperties,key:message(code:'license.properties.private')+' '+contextService.getOrg().name,subscriptions:subscriptions]" />
                        </table>
                    </g:if>



            <div class="sixteen wide field" style="text-align: right;">
                <input type="submit" class="ui button js-click-control" value="Ausgewählte Merkmale in Ziellizenz kopieren" onclick="return jsConfirmation() "/>
            </div>
    </g:form>
    </semui:form>
    </body>
</html>
