<%@ page import="de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.Subscription; de.laser.finance.CostItem" %>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'subscription.details.manageEntitlementGroup.label')}</title>
</head>

<body>
<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="currentSubscriptions"
                 text="${message(code: 'myinst.currentSubscriptions.label')}"/>
    <semui:crumb controller="subscription" action="index" id="${subscription.id}"
                 text="${subscription.name}"/>
    <semui:crumb class="active"
                 text="${message(code: 'subscription.details.manageEntitlementGroup.label')}"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <laser:render template="actions"/>
</semui:controlButtons>

<h1 class="ui icon header la-noMargin-top"><semui:headerIcon />
<semui:xEditable owner="${subscription}" field="name" />
</h1>
<g:if test="${editable}">
    <semui:auditButton auditable="[subscription, 'name']" />
</g:if>

<semui:anualRings object="${subscription}" controller="subscription" action="manageEntitlementGroup" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

<h2 class="ui left aligned icon header la-clear-before">${message(code: 'subscription.details.manageEntitlementGroup.label')}</h2>
<semui:messages data="${flash}"/>

<g:if test="${editable}">
    <input class="ui button" value="${message(code: 'subscription.details.createEntitlementGroup.label')}"
           data-semui="modal" data-href="#createEntitlementGroupModal" type="submit">
</g:if>

<semui:form>
    <table class="ui sortable celled la-js-responsive-table la-table table">
        <thead>
        <tr>
            <th>${message(code: 'sidewide.number')}</th>
            <th>${message(code: 'default.name.label')}</th>
            <th>${message(code: 'default.description.label')}</th>
            <th>${message(code: 'issueEntitlementGroup.items.label')}</th>
            <th></th>
        </tr>
        </thead>
        <g:each in="${titleGroups.sort{it.name}}" var="titleGroup" status="i">
            <tr>
                <td>${i + 1}</td>
                <td><semui:xEditable owner="${titleGroup}" field="name"/></td>
                <td><semui:xEditable owner="${titleGroup}" field="description"/></td>
                <td>
                    <g:link action="index" id="${params.id}" params="[titleGroup: titleGroup.id]">
                    ${titleGroup.items.size()}
                    </g:link>
                </td>
                <td class="x">

                    <g:link action="index" class="ui icon blue button la-modern-button" id="${params.id}"
                            params="[titleGroup: titleGroup.id]"
                            role="button"
                            aria-label="${message(code: 'ariaLabel.change.universal')}">
                        <i class="pencil icon"></i>
                    </g:link>
                    <g:if test="${editable}">
                        <g:set var="hasCostItems" value="${CostItem.executeQuery('select ci from CostItem ci where ci.issueEntitlementGroup = :titleGroup',[titleGroup:titleGroup])}"/>
                        <g:if test="${!hasCostItems}">
                            <g:link action="removeEntitlementGroup" class="ui icon negative button"
                                    params="${[titleGroup: titleGroup.id, sub: subscription.id]}"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                <i class="trash alternate outline icon"></i>
                            </g:link>
                        </g:if>
                        <g:else>
                            <div class="ui icon negative buttons la-popup-tooltip" data-content="${message(code:'issueEntitlementGroup.delete.existingCostItems')}">
                                <button class="ui disabled button la-modern-button  la-selectable-button"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                    <i class="trash alternate outline icon"></i>
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
            <label>${message(code: 'default.name.label')} <g:message code="messageRequiredField" /></label>
            <input type="text" name="name" placeholder="" value="${params.name}" required/>
        </div>

        <div class="field ">
            <label>${message(code: 'default.description.label')}</label>

            <textarea name="description">${params.description}</textarea>
        </div>

    </g:form>
</semui:modal>

</body>
</html>
