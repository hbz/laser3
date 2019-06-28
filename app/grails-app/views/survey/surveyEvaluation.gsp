<%@ page import="com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.SurveyProperty;com.k_int.kbplus.SurveyConfig;com.k_int.kbplus.RefdataValue" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'survey.label')}</title>
</head>

<body>

<g:render template="breadcrumb" model="${[params: params]}"/>

<semui:controlButtons>
    <g:if test="${surveyInfo.status != RefdataValue.loc('Survey Status', [en: 'In Processing', de: 'In Bearbeitung'])}">
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

<g:if test="${surveyInfo.status == RefdataValue.loc('Survey Status', [en: 'In Processing', de: 'In Bearbeitung'])}">
    <b>${message(code: 'surveyEvaluation.notOpenSurvey')}</b>
</g:if>
<g:else>
    <div class="ui grid">

        <div class="sixteen wide stretched column">
            <div class="ui top attached tabular menu">

                <g:link class="item ${params.tab == 'surveyConfigsView' ? 'active' : ''}"
                        controller="survey" action="surveyEvaluation"
                        id="${surveyInfo?.id}"
                        params="[tab: 'surveyConfigsView']">
                    ${message(code: 'surveyEvaluation.surveyConfigsView')}
                    <div class="ui floating circular label">${surveyConfigs?.size() ?: 0}</div>
                </g:link>

                <g:link class="item ${params.tab == 'participantsViewAllFinish' ? 'active' : ''}"
                        controller="survey" action="surveyEvaluation"
                        id="${surveyInfo?.id}"
                        params="[tab: 'participantsViewAllFinish']">
                    ${message(code: 'surveyEvaluation.participantsViewAllFinish')}
                    <div class="ui floating circular label">${participantsFinish?.size() ?: 0}</div>
                </g:link>

                <g:link class="item ${params.tab == 'participantsViewAllNotFinish' ? 'active' : ''}"
                        controller="survey" action="surveyEvaluation"
                        id="${surveyInfo?.id}"
                        params="[tab: 'participantsViewAllNotFinish']">
                    ${message(code: 'surveyEvaluation.participantsViewAllNotFinish')}
                    <div class="ui floating circular label">${participantsNotFinish?.size() ?: 0}</div>
                </g:link>

                <g:link class="item ${params.tab == 'participantsView' ? 'active' : ''}"
                        controller="survey" action="surveyEvaluation"
                        id="${surveyInfo?.id}"
                        params="[tab: 'participantsView']">
                    ${message(code: 'surveyEvaluation.participantsView')}
                    <div class="ui floating circular label">${participants?.size() ?: 0}</div>
                </g:link>

            </div>

            <g:if test="${params.tab == 'surveyConfigsView'}">
                <div class="ui bottom attached tab segment active">
                    <g:render template="evaluationSurveyConfigsView"/>
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
