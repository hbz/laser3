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

    <g:if test="${surveyConfigs}">
        <div class="ui grid">
            <div class="four wide column">
                <div class="ui vertical fluid menu">
                    <g:each in="${surveyConfigs.sort { it.configOrder }}" var="config" status="i">

                        <g:link class="item ${params.surveyConfigID == config?.id.toString() ? 'active' : ''}"
                                controller="survey" action="surveyParticipants"
                                id="${config?.surveyInfo?.id}" params="[surveyConfigID: config?.id]">

                            <h5 class="ui header">${config?.getConfigName()}</h5>
                            ${com.k_int.kbplus.SurveyConfig.getLocalizedValue(config?.type)}


                            <div class="ui floating circular label">${config?.orgIDs?.size() ?: 0}</div>
                        </g:link>
                    </g:each>
                </div>
            </div>

            <div class="twelve wide stretched column">
                <div class="ui top attached tabular menu">
                    <a class="item ${params.tab == 'selectedSubParticipants' ? 'active' : ''}"
                       data-tab="selectedSubParticipants">${message(code: 'surveyParticipants.selectedSubParticipants')}
                        <div class="ui floating circular label">${selectedSubParticipants.size() ?: 0}</div>
                    </a>

                    <a class="item ${params.tab == 'selectedParticipants' ? 'active' : ''}"
                       data-tab="selectedParticipants">${message(code: 'surveyParticipants.selectedParticipants')}
                        <div class="ui floating circular label">${selectedParticipants.size() ?: 0}</div></a>

                </div>

                <div class="ui bottom attached tab segment ${params.tab == 'selectedSubParticipants' ? 'active' : ''}"
                     data-tab="selectedSubParticipants">

                    <div>

                    </div>

                </div>


                <div class="ui bottom attached tab segment ${params.tab == 'selectedParticipants' ? 'active' : ''}"
                     data-tab="selectedParticipants">

                    <div>

                    </div>

                </div>

            </div>
        </div>
    </g:if>
    <g:else>
        <p><b>${message(code: 'surveyConfigs.noConfigList')}</b></p>
    </g:else>
</g:else>

<r:script>
    $(document).ready(function () {
        $('.tabular.menu .item').tab()
    });
</r:script>

</body>
</html>
