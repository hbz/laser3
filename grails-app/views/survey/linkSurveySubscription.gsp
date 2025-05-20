<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.utils.DateUtils; de.laser.Org; de.laser.finance.CostItem; de.laser.Subscription; de.laser.wekb.Platform; de.laser.wekb.Package; java.text.SimpleDateFormat; de.laser.PendingChangeConfiguration; de.laser.RefdataCategory; de.laser.RefdataValue; de.laser.storage.RDConstants; de.laser.storage.RDStore;" %>
<laser:htmlStart message="surveySubscriptions.linkSubscription.plural"/>

<laser:render template="breadcrumb" model="${[params: params]}"/>

<ui:controlButtons>
    <laser:render template="exports"/>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon type="Survey">
    <ui:xEditable owner="${surveyInfo}" field="name"/>
</ui:h1HeaderWithIcon>
<uiSurvey:statusWithRings object="${surveyInfo}" surveyConfig="${surveyConfig}" controller="survey" action="${actionName}"/>

<g:if test="${surveyConfig.subscription}">
    <ui:linkWithIcon icon="${Icon.SUBSCRIPTION} bordered inverted orange la-object-extended"
                     href="${createLink(action: 'show', controller: 'subscription', id: surveyConfig.subscription.id)}"/>
</g:if>

<ui:objectStatus object="${surveyInfo}"/>

<h2><g:message code="surveySubscriptions.linkSubscription.plural"/></h2>
<br>
<br>
<g:link class="${Btn.SIMPLE} right floated" controller="survey" action="surveyPackages"
        params="${[id: params.id, surveyConfigID: surveyConfig.id]}"><g:message code="default.button.back"/></g:link>

<h2 class="ui left floated aligned icon header la-clear-before">${message(code: 'subscription.plural')}
<ui:totalNumber total="${surveySubscriptionsCount}/${num_sub_rows}"/>
</h2>
<br>
<br>
<ui:messages data="${flash}"/>

<g:render template="/templates/subscription/subscriptionFilter"/>

<g:form controller="survey" action="processLinkSurveySubscription"
        id="${surveyInfo.id}"
        params="${params}" method="post" class="ui form">

    <g:render template="/survey/subscriptionTableForOwner" model="[tmplShowCheckbox: editable]"/>


    <g:if test="${editable}">
        <br>

        <div class="field">
            <button name="processOption" value="linkSubscriptions" type="submit"
                    class="${Btn.SIMPLE_CLICKCONTROL}">${message(code: 'surveySubscriptions.linkSubscription.plural')}</button>
        </div>
    </g:if>

</g:form>

<ui:paginate action="$actionName" controller="$controllerName" params="${params}"
             max="${max}" total="${num_sub_rows}"/>

<laser:htmlEnd/>
