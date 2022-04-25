<%@ page import="de.laser.SurveyConfig;" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'default.notes.label')}</title>
</head>

<body>
<laser:render template="breadcrumb" model="${[params: params]}"/>

<semui:controlButtons>
    <laser:render template="actions"/>
</semui:controlButtons>

<h1 class="ui icon header"><semui:headerTitleIcon type="Survey"/>
<semui:xEditable owner="${surveyInfo}" field="name"
                 overwriteEditable="${surveyInfo.isSubscriptionSurvey ? false : editable}"/>
</h1>
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

</body>
</html>
