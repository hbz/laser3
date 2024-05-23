<%@ page import="de.laser.survey.SurveyConfig; de.laser.storage.RDStore;" %>
<laser:htmlStart message="task.plural" />

<laser:render template="breadcrumb" model="${[params: params]}"/>

<ui:controlButtons>
    <laser:render template="exports"/>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon text="${surveyInfo.name}" type="Survey"/>

<uiSurvey:statusWithRings object="${surveyInfo}" surveyConfig="${surveyConfig}" controller="survey" action="${actionName}"/>

<g:if test="${surveyConfig.subscription}">
    <ui:linkWithIcon icon="bordered inverted orange clipboard la-object-extended" href="${createLink(action: 'show', controller: 'subscription', id: surveyConfig.subscription.id)}"/>
</g:if>


<laser:render template="nav"/>

<ui:messages data="${flash}"/>

<br />

<h2 class="ui icon header la-clear-before la-noMargin-top">
    <g:if test="${surveyConfig.subscription}">
        <i class="icon clipboard outline la-list-icon"></i>
        <g:link controller="subscription" action="show" id="${surveyConfig.subscription.id}">
            ${surveyConfig.getConfigNameShort()}
        </g:link>

    </g:if>
    <g:else>
        ${surveyConfig.getConfigNameShort()}
    </g:else>
    : ${message(code: 'task.plural')}
</h2>

<laser:render template="/templates/tasks/tables" model="${[cmbTaskInstanceList: cmbTaskInstanceList]}"/>

<laser:htmlEnd />
