<laser:htmlStart message="${g.message(code: "financials.editCost")}"
                 serviceInjection="true"/>


<ui:breadcrumbs>
    <g:if test="${costItem.sub}">
        <ui:crumb controller="subscription" action="show" id="${costItem.sub.id}" text="${costItem.sub.name}"/>
        <ui:crumb controller="finance" action="subFinancialData" params="${[sub:costItem.sub.id]}" message="subscription.details.financials.label"/>
    </g:if>

    <ui:crumb class="active" text="${g.message(code: "financials.editCost")}"/>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon type="finance">
    ${g.message(code: "financials.editCost")}:
    <g:if test="${showVisibilitySettings && costItem.isVisibleForSubscriber}">
        <div class="ui orange label">
            <strong>${costItem.sub.getSubscriber()}</strong>
        </div>
    </g:if>
    <g:elseif test="${subscription}">
        <div class="ui orange label">
            <strong>${subscription.getSubscriber().name}</strong>
        </div>
    </g:elseif>
</ui:h1HeaderWithIcon>

<ui:objectStatus object="${costItem}" status="${costItem.costItemStatus}"/>
<ui:messages data="${flash}"/>

<div class="ui stackable grid">
    <div class="sixteen wide column">
        <div class="ui blue right right floated mini button la-js-clickButton" data-position="top center"
             data-title="${costItem.globalUID}"><g:message code="globalUID.label"/></div>
        <laser:script file="${this.getGroovyPageFileName()}">
            $('.la-js-clickButton').popup({
                on: 'click'
            });
        </laser:script>
        <br>
        <br>

        <g:form class="ui small form clearing segment la-form" name="editCost_${idSuffix}" url="${formUrl}">
            <laser:render template="costItemInput"/>

            <g:if test="${editable}">
                <input type="submit" class="ui button green" name="save"
                       value="${g.message(code: 'default.button.save.label')}"/>
            </g:if>
        </g:form>

    </div>
</div>

<laser:htmlEnd/>
