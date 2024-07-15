<%@ page import="de.laser.ui.Icon; de.laser.survey.SurveyConfig; de.laser.storage.RDStore;" %>
<laser:htmlStart message="default.notes.label" />

<laser:render template="breadcrumb" model="${[params: params]}"/>

<ui:controlButtons>
    <laser:render template="exports"/>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon text="${surveyInfo.name}" type="Survey"/>

<uiSurvey:statusWithRings object="${surveyInfo}" surveyConfig="${surveyConfig}" controller="survey" action="${actionName}"/>

<g:if test="${surveyConfig.subscription}">
    <ui:linkWithIcon icon="${Icon.SUBSCRIPTION} bordered inverted orange la-object-extended" href="${createLink(action: 'show', controller: 'subscription', id: surveyConfig.subscription.id)}"/>
</g:if>

<laser:render template="nav"/>

<ui:messages data="${flash}"/>

<br/>

<h2 class="ui icon header la-clear-before la-noMargin-top">
    <g:if test="${surveyConfig.subscription}">
        <i class="${Icon.SUBSCRIPTION} la-list-icon"></i>
        <g:link controller="subscription" action="show" id="${surveyConfig.subscription.id}">
            ${surveyConfig.getConfigNameShort()}
        </g:link>

    </g:if>
    <g:else>
        ${surveyConfig.getConfigNameShort()}
    </g:else>
    : ${message(code: 'default.notes.label')}
</h2>

<laser:render template="/templates/notes/table" model="${[instance: surveyConfig, redirect: 'notes']}"/>

<laser:htmlEnd />
