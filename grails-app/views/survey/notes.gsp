<%@ page import="de.laser.survey.SurveyConfig;" %>
<laser:htmlStart message="default.notes.label" />

<laser:render template="breadcrumb" model="${[params: params]}"/>

<semui:controlButtons>
    <laser:render template="actions"/>
</semui:controlButtons>

<semui:h1HeaderWithIcon type="Survey">
<semui:xEditable owner="${surveyInfo}" field="name"
                 overwriteEditable="${surveyInfo.isSubscriptionSurvey ? false : editable}"/>
</semui:h1HeaderWithIcon>
<semui:surveyStatusWithRings object="${surveyInfo}" surveyConfig="${surveyConfig}" controller="survey" action="show"/>


<laser:render template="nav"/>

<semui:messages data="${flash}"/>


<br/>

<h2 class="ui icon header la-clear-before la-noMargin-top">
    <g:if test="${surveyConfig.type in [SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION, SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT]}">
        <i class="icon clipboard outline la-list-icon"></i>
        <g:link controller="subscription" action="show" id="${surveyConfig.subscription?.id}">
            ${surveyConfig.subscription?.name}
        </g:link>

    </g:if>
    <g:else>
        ${surveyConfig.getConfigNameShort()}
    </g:else>
    : ${message(code: 'default.notes.label')}
</h2>

<laser:render template="/templates/notes/table" model="${[instance: surveyConfig, redirect: 'notes']}"/>

<laser:htmlEnd />
