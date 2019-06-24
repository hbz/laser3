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
            <g:link class="item" action="exportParticipantResult"
                    params="${params + [exportXLS: true]}">${message(code: 'default.button.exports.xls')}</g:link>
        </semui:exportDropdownItem>
    </semui:exportDropdown>
    </g:if>

    <g:render template="actions"/>
</semui:controlButtons>

<h1 class="ui icon header"><semui:headerTitleIcon type="Survey"/>
<semui:xEditable owner="${surveyInfo}" field="name"/>
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
                    <a class="item ${params.tab == 'surveyConfigsView' ? 'active' : ''}"
                       data-tab="surveyConfigsView">${message(code: 'surveyEvaluation.surveyConfigsView')}
                        <div class="ui floating circular label">${surveyConfigs?.size() ?: 0}</div>
                    </a>

                    <a class="item ${params.tab == 'participantsView' ? 'active' : ''}"
                       data-tab="participantsView">${message(code: 'surveyEvaluation.participantsView')}
                        <div class="ui floating circular label">${participants?.size() ?: 0}</div></a>

                </div>

                <div class="ui bottom attached tab segment ${params.tab == 'surveyConfigsView' ? 'active' : ''}"
                     data-tab="surveyConfigsView">

                    <div>
                        <g:render template="evaluationSurveyConfigsView"/>
                    </div>

                </div>


                <div class="ui bottom attached tab segment ${params.tab == 'participantsView' ? 'active' : ''}"
                     data-tab="participantsView">

                    <div>
                        <g:render template="evaluationParticipantsView"/>
                    </div>

                </div>

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
