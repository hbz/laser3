<%@ page import="com.k_int.kbplus.RefdataCategory;com.k_int.properties.PropertyDefinition;com.k_int.kbplus.SurveyConfig;com.k_int.kbplus.RefdataValue" %>
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
        <semui:crumb controller="survey" action="show" id="${surveyInfo.id}" params="[surveyConfigID: surveyConfig]" text="${surveyConfig?.getConfigNameShort()}" />
    </g:if>
    <semui:crumb message="surveyEvaluation.label" class="active"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <g:if test="${surveyInfo.status != de.laser.helper.RDStore.SURVEY_IN_PROCESSING}">
        <semui:exportDropdown>
            <semui:exportDropdownItem>
                <g:link class="item" action="exportParticipantResult" id="${surveyInfo.id}"
                        params="[exportXLS: true, exportConfigs: true]">${message(code: 'survey.exportResultsConfigs')}</g:link>
                <g:link class="item" action="exportParticipantResult" id="${surveyInfo.id}"
                        params="[exportXLS: true]">${message(code: 'survey.exportResultsOrgs')}</g:link>
            </semui:exportDropdownItem>
        </semui:exportDropdown>
    </g:if>

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

<g:if test="${surveyInfo.status == de.laser.helper.RDStore.SURVEY_IN_PROCESSING}">
    <b>${message(code: 'surveyEvaluation.notOpenSurvey')}</b>
</g:if>
<g:else>
    <h2 class="ui icon header la-clear-before la-noMargin-top">
        <g:if test="${surveyConfig?.type == 'Subscription'}">
            <i class="icon clipboard outline la-list-icon"></i>
            <g:link controller="subscription" action="show" id="${surveyConfig?.subscription?.id}">
                ${surveyConfig?.subscription?.name}
            </g:link>
        </g:if>
        <g:else>
            ${surveyConfig?.getConfigNameShort()}
        </g:else>: ${message(code: 'surveyEvaluation.label')}
    </h2>
    <br>

    <div class="ui grid">

        <div class="sixteen wide stretched column">
            <div class="ui top attached tabular menu">

                <g:link class="item ${params.tab == 'surveyConfigsView' ? 'active' : ''}"
                        controller="survey" action="surveyEvaluation"
                        params="[id: params.id, surveyConfigID: surveyConfig?.id, tab: 'surveyConfigsView']">
                    ${message(code: 'surveyEvaluation.label')}
                </g:link>

                <g:link class="item ${params.tab == 'participantsViewAllFinish' ? 'active' : ''}"
                        controller="survey" action="surveyEvaluation"
                        params="[id: params.id, surveyConfigID: surveyConfig?.id, tab: 'participantsViewAllFinish']">
                    ${message(code: 'surveyEvaluation.participantsViewAllFinish')}
                    <div class="ui floating circular label">${participantsFinish?.size() ?: 0}</div>
                </g:link>

                <g:link class="item ${params.tab == 'participantsViewAllNotFinish' ? 'active' : ''}"
                        controller="survey" action="surveyEvaluation"
                        params="[id: params.id, surveyConfigID: surveyConfig?.id, tab: 'participantsViewAllNotFinish']">
                    ${message(code: 'surveyEvaluation.participantsViewAllNotFinish')}
                    <div class="ui floating circular label">${participantsNotFinish?.size() ?: 0}</div>
                </g:link>

                <g:link class="item ${params.tab == 'participantsView' ? 'active' : ''}"
                        controller="survey" action="surveyEvaluation"
                        params="[id: params.id, surveyConfigID: surveyConfig?.id, tab: 'participantsView']">
                    ${message(code: 'surveyEvaluation.participantsView')}
                    <div class="ui floating circular label">${participants?.size() ?: 0}</div>
                </g:link>

            </div>

            <g:if test="${params.tab == 'surveyConfigsView'}">
                <div class="ui bottom attached tab segment active">

                    <g:if test="${surveyConfig}">

                        <g:if test="${surveyConfig.type == "Subscription"}">
                            <g:render template="evaluationSubscription" />
                        </g:if>

                        <g:if test="${surveyConfig.type == "GeneralSurvey"}">
                            <g:render template="evaluationGeneralSurvey" />
                        </g:if>

                    </g:if>
                </div>
            </g:if>

            <g:if test="${params.tab == 'participantsView'}">
                <div class="ui bottom attached tab segment active">
                    <g:render template="evaluationParticipantsView"/>
                </div>
            </g:if>

            <g:if test="${params.tab == 'participantsViewAllFinish'}">
                <div class="ui bottom attached tab segment active">
                    <g:render template="evaluationParticipantsView"/>
                </div>
            </g:if>

            <g:if test="${params.tab == 'participantsViewAllNotFinish'}">
                <div class="ui bottom attached tab segment active">
                    <g:render template="evaluationParticipantsView"/>
                </div>
            </g:if>

        </div>
    </div>

</g:else>

<r:script>
    $(document).ready(function () {
        $('.tabular.menu .item').tab()
    });
</r:script>

</body>
</html>
