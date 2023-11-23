<%@ page import="de.laser.CustomerTypeService; de.laser.RefdataValue; de.laser.RefdataCategory; de.laser.Subscription; de.laser.finance.CostItem" %>
<laser:htmlStart message="subscription.details.manageDiscountScale.label" />

<laser:serviceInjection/>

<ui:breadcrumbs>
    <ui:crumb controller="myInstitution" action="currentSubscriptions" text="${message(code: 'myinst.currentSubscriptions.label')}"/>
    <ui:crumb controller="subscription" action="subTransfer" id="${subscription.id}" text="${subscription.name}"/>
    <ui:crumb class="active" text="${message(code: 'subscription.details.manageDiscountScale.label')}"/>
</ui:breadcrumbs>

<ui:controlButtons>
    <g:if test="${contextService.isInstEditor_or_ROLEADMIN(CustomerTypeService.PERMS_PRO)}">
        <ui:actionsDropdown>
            <ui:actionsDropdownItem controller="subscription" action="copyDiscountScales" params="${[id: params.id]}"
                                    message="subscription.details.copyDiscountScales.label"/>
        </ui:actionsDropdown>
    </g:if>
</ui:controlButtons>

<ui:h1HeaderWithIcon referenceYear="${subscription?.referenceYear}">
    <ui:xEditable owner="${subscription}" field="name" />
</ui:h1HeaderWithIcon>
<g:if test="${editable}">
    <ui:auditButton auditable="[subscription, 'name']" />
</g:if>

<ui:anualRings object="${subscription}" controller="subscription" action="manageDiscountScale" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

<h2 class="ui left aligned icon header la-clear-before">${message(code: 'subscription.details.manageDiscountScale.label')}</h2>
<ui:messages data="${flash}"/>

<g:if test="${editable}">
    <input class="ui button" value="${message(code: 'subscription.details.createDiscountScale.label')}"
           data-ui="modal" data-href="#createDiscountScaleModal" type="submit">
</g:if>

<ui:greySegment>
    <table class="ui sortable celled la-js-responsive-table la-table table">
        <thead>
        <tr>
            <th>${message(code: 'sidewide.number')}</th>
            <th>${message(code: 'default.name.label')}</th>
            <th>${message(code: 'default.discount.label')}</th>
            <th>${message(code: 'default.note.label')}</th>
            <th></th>
        </tr>
        </thead>
        <g:each in="${discountScales.sort{it.name}}" var="discountScale" status="i">
            <tr>
                <td>${i + 1}</td>
                <td><ui:xEditable owner="${discountScale}" field="name"/></td>
                <td><ui:xEditable owner="${discountScale}" field="discount"/></td>
                <td><ui:xEditable owner="${discountScale}" field="note" type="textarea"/></td>
                <td class="x">
                    <g:if test="${editable}">
                        <g:if test="${!Subscription.findByDiscountScale(discountScale)}">
                            <g:link action="manageDiscountScale" class="ui icon negative button"
                                    params="${[cmd: 'removeDiscountScale', discountScaleId: discountScale.id, id: subscription.id]}"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                <i class="trash alternate outline icon"></i>
                            </g:link>
                        </g:if>
                        <g:else>
                            <div class="ui icon negative buttons la-popup-tooltip" data-content="${message(code:'subscription.details.manageDiscountScale.linkWithSub')}">
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
</ui:greySegment>


<ui:modal id="createDiscountScaleModal" message="subscription.details.createDiscountScale.label">

    <g:form action="manageDiscountScale" controller="subscription" params="[id: params.id, cmd: 'createDiscountScale']"
            method="post" class="ui form">
        <div class="field required ">
            <label>${message(code: 'default.name.label')} <g:message code="messageRequiredField" /></label>
            <input type="text" name="name" placeholder="" value="${params.name}" required/>
        </div>

        <div class="field required ">
            <label>${message(code: 'default.discount.label')} <g:message code="messageRequiredField" /></label>
            <input type="text" name="discount" placeholder="" value="${params.discount}" required/>
        </div>

        <div class="field ">
            <label>${message(code: 'default.note.label')}</label>

            <textarea name="note">${params.note}</textarea>
        </div>

    </g:form>
</ui:modal>

<laser:htmlEnd />
