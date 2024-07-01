<%@ page import="de.laser.helper.Icons; de.laser.utils.DateUtils; de.laser.Org; de.laser.finance.CostItem; de.laser.Subscription; de.laser.Platform; de.laser.Package; java.text.SimpleDateFormat; de.laser.PendingChangeConfiguration; de.laser.RefdataCategory; de.laser.RefdataValue; de.laser.storage.RDConstants; de.laser.storage.RDStore;" %>
<laser:htmlStart message="package.plural" serviceInjection="true"/>

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


<ui:messages data="${flash}"/>
<br>

<h2 class="ui left floated aligned icon header la-clear-before">${message(code: 'surveyPackages.label')}
<ui:totalNumber total="${recordsCount}"/>
</h2>

<g:if test="${editable}">
    <g:link class="ui icon button right floated" controller="survey" action="linkSurveyPackage"
            id="${surveyInfo.id}"
            params="[surveyConfigID: surveyConfig.id]"><g:message code="surveyPackages.linkPackage"/></g:link>
    <br>
    <br>
</g:if>

<g:render template="/templates/survey/packages" model="[
        processController: 'survey',
        processAction: 'surveyPackages',
        tmplShowCheckbox: editable,
        linkSurveyPackage: true,
        tmplConfigShow: ['lineNumber', 'name', 'status', 'titleCount', 'provider', 'platform', 'curatoryGroup', 'automaticUpdates', 'lastUpdatedDisplay', 'unLinkSurveyPackage']]"/>

<laser:htmlEnd/>
