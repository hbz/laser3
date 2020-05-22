<%@ page import="com.k_int.kbplus.RefdataValue; com.k_int.kbplus.RefdataCategory; com.k_int.kbplus.Subscription; com.k_int.kbplus.CostItem" %>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser')} : ${message(code: 'subscription.details.manageEntitlementGroup.label')}</title>
</head>

<body>
<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="currentSubscriptions"
                 text="${message(code: 'myinst.currentSubscriptions.label')}"/>
    <semui:crumb controller="subscription" action="index" id="${subscriptionInstance.id}"
                 text="${subscriptionInstance.name}"/>
    <semui:crumb class="active"
                 text="${message(code: 'subscription.details.manageEntitlementGroup.label')}"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <g:render template="actions"/>
</semui:controlButtons>

<h1 class="ui left floated aligned icon header la-clear-before"><semui:headerIcon/>
<g:inPlaceEdit domain="${Subscription.class.name}" pk="${subscriptionInstance.id}" field="name" id="name"
               class="newipe">${subscriptionInstance?.name}</g:inPlaceEdit>
</h1>

<h2 class="ui left aligned icon header la-clear-before">${message(code: 'subscription.details.manageEntitlementGroup.label')}</h2>
<semui:messages data="${flash}"/>

<g:if test="${editable}">
    <input class="ui button" value="${message(code: 'subscription.details.createEntitlementGroup.label')}"
           data-semui="modal" data-href="#createEntitlementGroupModal" type="submit">
</g:if>

<semui:form>
    <table class="ui sortable celled la-table table">
        <thead>
        <tr>
            <th>${message(code: 'sidewide.number')}</th>
            <th>${message(code: 'issueEntitlementGroup.name.label')}</th>
            <th>${message(code: 'issueEntitlementGroup.description.label')}</th>
            <th>${message(code: 'issueEntitlementGroup.items.label')}</th>
            <th></th>
        </tr>
        </thead>
        <g:each in="${titleGroups.sort{it.name}}" var="titleGroup" status="i">
            <tr>
                <td>${i + 1}</td>
                <td><semui:xEditable owner="${titleGroup}" field="name"/></td>
                <td><semui:xEditable owner="${titleGroup}" field="description"/></td>
                <td>${titleGroup.items.size()}</td>
                <td class="x">
                    <g:if test="${editable}">
                        <g:set var="hasCostItems" value="${CostItem.executeQuery('select ci from CostItem ci where ci.issueEntitlementGroup = :titleGroup',[titleGroup:titleGroup])}"/>
                        <g:if test="${!hasCostItems}">
                            <g:link action="removeEntitlementGroup" class="ui icon negative button"
                                    params="${[titleGroup: titleGroup.id, sub: subscriptionInstance.id]}">
                                <i class="trash alternate icon"></i>
                            </g:link>
                        </g:if>
                        <g:else>
                            <div class="ui icon negative buttons la-popup-tooltip" data-content="${message(code:'issueEntitlementGroup.delete.existingCostItems')}">
                                <button class="ui disabled button la-selectable-button">
                                    <i class="trash alternate icon"></i>
                                </button>
                            </div>
                        </g:else>
                    </g:if>
                </td>
            </tr>
        </g:each>
    </table>
</semui:form>


<semui:modal id="createEntitlementGroupModal" message="subscription.details.createEntitlementGroup.label">

    <g:form action="processCreateEntitlementGroup" controller="subscription" params="[id: params.id]"
            method="post" class="ui form">

        <div class="field required ">
            <label>${message(code: 'issueEntitlementGroup.name.label')}</label>
            <input type="text" name="name" placeholder="" value="${params.name}" required/>
        </div>

        <div class="field ">
            <label>${message(code: 'issueEntitlementGroup.description.label')}</label>

            <textarea name="description">${params.description}</textarea>
        </div>

    </g:form>
</semui:modal>

</body>
</html>
