<%@ page import="de.laser.survey.SurveyConfig;de.laser.RefdataCategory;de.laser.properties.PropertyDefinition;de.laser.storage.RDStore;" %>
<laser:htmlStart text="${message(code: 'survey.label')} (${message(code: 'surveyParticipants.label')})" serviceInjection="true"/>

<ui:breadcrumbs>
    <ui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code:'menu.my.surveys')}" />
    <g:if test="${surveyInfo}">
        <ui:crumb controller="survey" action="show" id="${surveyInfo.id}" params="[surveyConfigID: surveyConfig.id]" text="${surveyConfig.getConfigNameShort()}" />
    </g:if>
    <ui:crumb message="surveyParticipants.label" class="active"/>
</ui:breadcrumbs>

<ui:controlButtons>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon type="Survey">
<ui:xEditable owner="${surveyInfo}" field="name"/>
</ui:h1HeaderWithIcon>
<uiSurvey:statusWithRings object="${surveyInfo}" surveyConfig="${surveyConfig}" controller="survey" action="surveyParticipants"/>


<laser:render template="nav"/>

<ui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>

<ui:messages data="${flash}"/>

<br />

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
    : ${message(code: 'surveyParticipants.label')}
</h2>

<br />

<g:if test="${surveyConfig}">
    <div class="ui grid">
        <div class="sixteen wide stretched column">
            <div class="ui top attached stackable tabular la-tab-with-js menu">

                <g:if test="${surveyConfig.type in [SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION, SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT]}">
                    <g:link class="item ${params.tab == 'selectedSubParticipants' ? 'active' : ''}"
                            controller="survey" action="surveyParticipants"
                            id="${surveyConfig.surveyInfo.id}"
                            params="[surveyConfigID: surveyConfig.id, tab: 'selectedSubParticipants']">
                        ${message(code: 'surveyParticipants.selectedSubParticipants')}
                        <div class="ui floating circular label">${selectedSubParticipants.size() ?: 0}</div>
                    </g:link>
                </g:if>

                <g:if test="${surveyConfig.type != SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT}">
                    <g:link class="item ${params.tab == 'selectedParticipants' ? 'active' : ''}"
                            controller="survey" action="surveyParticipants"
                            id="${surveyConfig.surveyInfo.id}"
                            params="[surveyConfigID: surveyConfig.id, tab: 'selectedParticipants']">
                        ${surveyConfig.type in [SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION, SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT] ? message(code: 'surveyParticipants.selectedParticipants') : message(code: 'surveyParticipants.selectedParticipants2')}
                        <div class="ui floating circular label">${selectedParticipants.size() ?: 0}</div></g:link>
                </g:if>

                <g:if test="${surveyInfo.status in [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY, RDStore.SURVEY_SURVEY_STARTED]}">
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
                    <laser:render template="selectedSubParticipants"/>
                </div>
            </g:if>


            <g:if test="${params.tab == 'selectedParticipants'}">
                <div class="ui bottom attached tab segment active">
                    <laser:render template="selectedParticipants"/>
                </div>
            </g:if>


            <g:if test="${params.tab == 'consortiaMembers'}">
                <div class="ui bottom attached tab segment active">
                    <laser:render template="consortiaMembers"/>

                </div>
            </g:if>
        </div>
    </div>
</g:if>
<g:else>
    <p><strong>${message(code: 'surveyConfigs.noConfigList')}</strong></p>
</g:else>

<laser:htmlEnd />
