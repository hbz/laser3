<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.utils.DateUtils; de.laser.Org; de.laser.finance.CostItem; de.laser.Subscription; de.laser.wekb.Platform; de.laser.wekb.Package; java.text.SimpleDateFormat; de.laser.PendingChangeConfiguration; de.laser.RefdataCategory; de.laser.RefdataValue; de.laser.storage.RDConstants; de.laser.storage.RDStore;" %>
<laser:htmlStart message="surveyVendors.label" serviceInjection="true"/>

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

<laser:render template="nav"/>

<ui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>


<ui:messages data="${flash}"/>
<br>

<h2 class="ui left floated aligned icon header la-clear-before">${message(code: 'surveyVendors.label')}
<ui:totalNumber total="${vendorListTotal}"/>
</h2>

<g:if test="${editable}">
    <g:link class="${Btn.SIMPLE} right floated" controller="survey" action="linkSurveyVendor"
            id="${surveyInfo.id}"
            params="[surveyConfigID: surveyConfig.id, initial: true]"><g:message code="surveyVendors.linkVendor"/></g:link>
    <br>
    <br>
</g:if>

<g:render template="/templates/survey/vendors" model="[
        processController: 'survey',
        processAction: 'surveyVendors',
        tmplShowCheckbox: editable,

        tmplConfigShow: ['lineNumber', 'sortname', 'name', 'isWekbCurated', 'unLinkSurveyVendor']]"/>

<laser:htmlEnd/>
