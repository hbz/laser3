<%@ page import="de.laser.ui.Icon; de.laser.survey.SurveyConfig;de.laser.RefdataCategory;de.laser.properties.PropertyDefinition;de.laser.storage.RDStore;" %>
<laser:htmlStart text="${message(code: 'survey.label')} (${message(code: 'surveyParticipants.label')})" />

<ui:breadcrumbs>
    <ui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code:'menu.my.surveys')}" />
    <g:if test="${surveyInfo}">
%{--        <ui:crumb controller="survey" action="show" id="${surveyInfo.id}" params="[surveyConfigID: surveyConfig.id]" text="${surveyConfig.getConfigNameShort()}" />--}%
        <ui:crumb class="active" text="${surveyConfig.getConfigNameShort()}" />
    </g:if>
%{--    <ui:crumb message="surveyParticipants.label" class="active"/>--}%
</ui:breadcrumbs>

<ui:controlButtons>
    <laser:render template="exports"/>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon text="${surveyInfo.name}" type="Survey"/>

<uiSurvey:statusWithRings object="${surveyInfo}" surveyConfig="${surveyConfig}" controller="survey" action="${actionName}"/>

<g:if test="${surveyConfig.subscription}">
 <ui:buttonWithIcon style="vertical-align: super;" message="${message(code: 'button.message.showLicense')}" variation="tiny" icon="${Icon.SUBSCRIPTION}" href="${createLink(action: 'show', controller: 'subscription', id: surveyConfig.subscription.id)}"/>
</g:if>

<laser:render template="nav"/>

<ui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>

<ui:messages data="${flash}"/>

<br />

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
    : ${message(code: 'surveyParticipants.label')}
</h2>

<br />

<g:if test="${surveyConfig}">
    <div class="ui grid">
        <div class="sixteen wide stretched column">
            <div class="ui top attached stackable tabular la-tab-with-js menu">

                <g:if test="${surveyConfig.subscription}">
                    <g:link class="item ${params.tab == 'selectedSubParticipants' ? 'active' : ''}"
                            controller="survey" action="surveyParticipants"
                            id="${surveyConfig.surveyInfo.id}"
                            params="[surveyConfigID: surveyConfig.id, tab: 'selectedSubParticipants']">
                        ${message(code: 'surveyParticipants.selectedSubParticipants')}
                        <ui:bubble float="true" count="${selectedSubParticipantsCount}"/>
                    </g:link>
                </g:if>

                <g:if test="${surveyConfig.type != SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT}">
                    <g:link class="item ${params.tab == 'selectedParticipants' ? 'active' : ''}"
                            controller="survey" action="surveyParticipants"
                            id="${surveyConfig.surveyInfo.id}"
                            params="[surveyConfigID: surveyConfig.id, tab: 'selectedParticipants']">
                        ${message(code: 'surveyParticipants.selectedParticipants')}
                        <ui:bubble float="true" count="${selectedParticipantsCount}"/>
                    </g:link>


                    <g:if test="${surveyInfo.status in [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY, RDStore.SURVEY_SURVEY_STARTED]}">
                        <g:link class="item ${params.tab == 'consortiaMembers' ? 'active' : ''}"
                                controller="survey" action="surveyParticipants"
                                id="${surveyConfig.surveyInfo.id}"
                                params="[surveyConfigID: surveyConfig.id, tab: 'consortiaMembers']">
                            ${message(code: 'surveyParticipants.consortiaMembers')}
                            <ui:bubble float="true" count="${consortiaMembersCount}"/>
                        </g:link>
                    </g:if>
                </g:if>
            </div>

            <div class="ui bottom attached tab segment active">
                <g:if test="${params.tab == 'selectedSubParticipants'}">
                    <laser:render template="selectedSubParticipants"/>
                </g:if>

                <g:if test="${params.tab == 'selectedParticipants'}">
                    <laser:render template="selectedParticipants"/>
                </g:if>

                <g:if test="${params.tab == 'consortiaMembers'}">

                    <laser:render template="consortiaMembers"/>

                </g:if>
            </div>
        </div>
    </div>
</g:if>
<g:else>
    <p><strong>${message(code: 'surveyConfigs.noConfigList')}</strong></p>
</g:else>

<laser:htmlEnd />
