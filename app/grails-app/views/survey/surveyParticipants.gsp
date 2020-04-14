<%@ page import="com.k_int.kbplus.RefdataCategory;com.k_int.properties.PropertyDefinition;com.k_int.kbplus.SurveyConfig" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser')} : ${message(code: 'survey.label')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="survey" action="currentSurveysConsortia" text="${message(code:'menu.my.surveys')}" />
    <g:if test="${surveyInfo}">
        <semui:crumb controller="survey" action="show" id="${surveyInfo.id}" params="[surveyConfigID: surveyConfig]" text="${surveyConfig.getConfigNameShort()}" />
    </g:if>
    <semui:crumb message="surveyParticipants.label" class="active"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <g:render template="actions"/>
</semui:controlButtons>

<h1 class="ui icon header"><semui:headerTitleIcon type="Survey"/>
<semui:xEditable owner="${surveyInfo}" field="name"/>
<semui:surveyStatus object="${surveyInfo}"/>
</h1>

<g:render template="nav"/>

<semui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>

<semui:messages data="${flash}"/>

<br>

<h2 class="ui icon header la-clear-before la-noMargin-top">
    <g:if test="${surveyConfig.type == 'Subscription'}">
        <i class="icon clipboard outline la-list-icon"></i>
        <g:link controller="subscription" action="show" id="${surveyConfig.subscription?.id}">
            ${surveyConfig.subscription?.name}
        </g:link>
    </g:if>
    <g:else>
        ${surveyConfig.getConfigNameShort()}
    </g:else>
    : ${message(code: 'surveyParticipants.label')}
</h2>

<br>

<g:if test="${surveyConfig}">
    <div class="ui grid">
        <div class="sixteen wide stretched column">
            <div class="ui top attached tabular menu">

                <g:if test="${surveyConfig.type == 'Subscription' || surveyConfig.type == 'IssueEntitlementsSurvey'}">
                    <g:link class="item ${params.tab == 'selectedSubParticipants' ? 'active' : ''}"
                            controller="survey" action="surveyParticipants"
                            id="${surveyConfig.surveyInfo.id}"
                            params="[surveyConfigID: surveyConfig.id, tab: 'selectedSubParticipants']">
                        ${message(code: 'surveyParticipants.selectedSubParticipants')}
                        <div class="ui floating circular label">${selectedSubParticipants.size() ?: 0}</div>
                    </g:link>
                </g:if>

                <g:if test="${surveyConfig.type != 'IssueEntitlementsSurvey'}">
                    <g:link class="item ${params.tab == 'selectedParticipants' ? 'active' : ''}"
                            controller="survey" action="surveyParticipants"
                            id="${surveyConfig.surveyInfo.id}"
                            params="[surveyConfigID: surveyConfig.id, tab: 'selectedParticipants']">
                        ${surveyConfig.type == 'Subscription' ? message(code: 'surveyParticipants.selectedParticipants') : message(code: 'surveyParticipants.label')}
                        <div class="ui floating circular label">${selectedParticipants.size() ?: 0}</div></g:link>
                </g:if>

                <g:if test="${editable}">
                    <g:link class="item ${params.tab == 'consortiaMembers' ? 'active' : ''}"
                            controller="survey" action="surveyParticipants"
                            id="${surveyConfig.surveyInfo.id}"
                            params="[surveyConfigID: surveyConfig.id, tab: 'consortiaMembers']">
                        ${message(code: 'surveyParticipants.consortiaMembers')}
                        <div class="ui floating circular label">${consortiaMembers.size() ?: 0}</div>
                    </g:link>
                </g:if>
            </div>

            <g:if test="${params.tab == 'selectedSubParticipants'}">
                <div class="ui bottom attached tab segment active">
                    <g:render template="selectedSubParticipants"/>
                </div>
            </g:if>


            <g:if test="${params.tab == 'selectedParticipants'}">
                <div class="ui bottom attached tab segment active">
                    <g:render template="selectedParticipants"/>
                </div>
            </g:if>


            <g:if test="${params.tab == 'consortiaMembers'}">
                <div class="ui bottom attached tab segment active">
                    <g:render template="consortiaMembers"/>

                </div>
            </g:if>
        </div>
    </div>
</g:if>
<g:else>
    <p><b>${message(code: 'surveyConfigs.noConfigList')}</b></p>
</g:else>

</body>
</html>
