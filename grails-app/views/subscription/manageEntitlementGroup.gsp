<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.Subscription; de.laser.finance.CostItem" %>
<laser:htmlStart message="subscription.details.manageEntitlementGroup.label" />

<ui:breadcrumbs>
    <ui:crumb controller="myInstitution" action="currentSubscriptions" text="${message(code: 'myinst.currentSubscriptions.label')}"/>
    <ui:crumb controller="subscription" action="index" id="${subscription.id}" text="${subscription.name}"/>
    <ui:crumb class="active" text="${message(code: 'subscription.details.manageEntitlementGroup.label')}"/>
</ui:breadcrumbs>

<ui:controlButtons>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon referenceYear="${subscription?.referenceYear}">
    <ui:xEditable owner="${subscription}" field="name" />
</ui:h1HeaderWithIcon>
<g:if test="${editable}">
    <ui:auditButton auditable="[subscription, 'name']" />
</g:if>

<ui:anualRings object="${subscription}" controller="subscription" action="manageEntitlementGroup" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

<h2 class="ui left aligned icon header la-clear-before">${message(code: 'subscription.details.manageEntitlementGroup.label')}</h2>
<ui:messages data="${flash}"/>

<g:if test="${editable}">
    <input class="${Btn.SIMPLE}" value="${message(code: 'subscription.details.createEntitlementGroup.label')}"
           data-ui="modal" data-href="#createEntitlementGroupModal" type="submit">
</g:if>

<ui:greySegment>
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
                <td><ui:xEditable owner="${titleGroup}" field="name"/>
                    <g:if test="${titleGroup.surveyConfig}">
                        <br>
                        <g:if test="${(contextService.getOrg().isCustomerType_Consortium())}">
                                <g:link controller="survey" action="evaluationParticipant"
                                        params="[id: titleGroup.surveyConfig.surveyInfo.id, surveyConfigID: titleGroup.surveyConfig.id, participant: institution.id]">
                                    (<g:message code="survey.label"/>: ${titleGroup.surveyConfig.surveyInfo.name})
                                </g:link>
                        </g:if>
                        <g:else>
                                <g:link controller="myInstitution" action="surveyInfos" id="${titleGroup.surveyConfig.surveyInfo.id}"
                                        params="[surveyConfigID: titleGroup.surveyConfig.id]">
                                    (<g:message code="survey.label"/>: ${titleGroup.surveyConfig.surveyInfo.name})
                                </g:link>
                        </g:else>
                    </g:if>
                </td>
                <td><ui:xEditable owner="${titleGroup}" field="description"/></td>
                <td>
                    <g:link action="index" id="${params.id}" params="[titleGroup: titleGroup.id]">
                    ${titleGroup.countCurrentTitles()}
                    </g:link>
                </td>
                <td class="x">

                    <g:link action="index" class="${Btn.MODERN.SIMPLE}" id="${params.id}"
                            params="[titleGroup: titleGroup.id]"
                            role="button"
                            aria-label="${message(code: 'ariaLabel.change.universal')}">
                        <i class="${Icon.CMD.EDIT}"></i>
                    </g:link>
                    <g:if test="${editable}">
                        <g:set var="hasCostItems" value="${CostItem.executeQuery('select ci from CostItem ci where ci.issueEntitlementGroup = :titleGroup',[titleGroup:titleGroup])}"/>
                        <g:if test="${!hasCostItems}">
                            <g:link action="removeEntitlementGroup" class="${Btn.ICON.NEGATIVE}"
                                    params="${[titleGroup: titleGroup.id, sub: subscription.id]}"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                <i class="${Icon.CMD.DELETE}"></i>
                            </g:link>
                        </g:if>
                        <g:else>
                            <div class="la-popup-tooltip" data-content="${message(code:'issueEntitlementGroup.delete.existingCostItems')}">
                                <button class="${Btn.MODERN.NEGATIVE} disabled la-selectable-button"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                    <i class="${Icon.CMD.DELETE}"></i>
                                </button>
                            </div>
                        </g:else>
                    </g:if>
                </td>
            </tr>
        </g:each>
    </table>
</ui:greySegment>


<ui:modal id="createEntitlementGroupModal" message="subscription.details.createEntitlementGroup.label">

    <g:form action="processCreateEntitlementGroup" controller="subscription" params="[id: params.id]"
            method="post" class="ui form">
        <div class="field required">
            <label>${message(code: 'default.name.label')} <g:message code="messageRequiredField" /></label>
            <input type="text" name="name" placeholder="" value="${params.name}" required/>
        </div>

        <div class="field">
            <label>${message(code: 'default.description.label')}</label>

            <textarea name="description">${params.description}</textarea>
        </div>

    </g:form>
</ui:modal>

<laser:htmlEnd />
