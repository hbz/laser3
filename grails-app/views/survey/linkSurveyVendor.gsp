<%@ page import="de.laser.helper.Icons; de.laser.utils.DateUtils; de.laser.Org; de.laser.finance.CostItem; de.laser.Subscription; de.laser.Platform; de.laser.Package; java.text.SimpleDateFormat; de.laser.PendingChangeConfiguration; de.laser.RefdataCategory; de.laser.RefdataValue; de.laser.storage.RDConstants; de.laser.storage.RDStore;" %>
<laser:htmlStart message="surveyVendors.linkVendor.plural" serviceInjection="true"/>

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
    <ui:linkWithIcon icon="${Icons.SUBSCRIPTION} bordered inverted orange la-object-extended"
                     href="${createLink(action: 'show', controller: 'subscription', id: surveyConfig.subscription.id)}"/>
</g:if>

<laser:render template="nav"/>

<ui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>

<h2> <g:message code="surveyVendors.linkVendor"/>:</h2>
<br>

<g:link class="ui button right floated" controller="survey" action="surveyVendors"
        params="${[id: params.id, surveyConfigID: surveyConfig.id]}"><g:message code="default.button.back"/></g:link>
<br>
<br>
<h2 class="ui left floated aligned icon header la-clear-before">${message(code: 'surveyVendors.label')}
<ui:totalNumber total="${surveyVendorsCount}/${vendorListTotal}"/>
</h2>

<ui:messages data="${flash}"/>

<g:render template="/templates/survey/vendors" model="[
        processController: 'survey',
        processAction: 'linkSurveyVendor',
        tmplShowCheckbox: editable,

        tmplConfigShow: ['lineNumber', 'sortname', 'name', 'isWekbCurated', 'linkSurveyVendor']]"/>

<laser:htmlEnd />
