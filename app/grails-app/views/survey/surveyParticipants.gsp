<%@ page import="com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.SurveyProperty;com.k_int.kbplus.SurveyConfig" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'survey.label')}</title>
</head>

<body>

<g:render template="breadcrumb" model="${[ params:params ]}"/>

<semui:controlButtons>
    <g:render template="actions" />
</semui:controlButtons>

<h1 class="ui icon header"><semui:headerTitleIcon type="Survey"/>
<semui:xEditable owner="${surveyInfo}" field="name"/>
</h1>



<g:render template="nav" />

<semui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}" />

<semui:messages data="${flash}" />

<br>

<div class="ui icon info message">
    <i class="info icon"></i>

    ${message(code: 'surveyParticipants.info')}
</div>

<br>

<h2 class="ui left aligned icon header">${message(code: 'surveyConfigs.list')} <semui:totalNumber
        total="${surveyConfigs.size()}"/></h2>

<br>



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


                        <div class="ui floating circular label">${config?.orgs?.size() ?: 0}</div>
                    </g:link>
                </g:each>
            </div>
        </div>

        <div class="twelve wide stretched column">
            <div class="ui top attached tabular menu">

                <g:if test="${surveyConfig?.type == 'Subscription'}">
                <a class="item ${params.tab == 'selectedSubParticipants' ? 'active' : ''}"
                   data-tab="selectedSubParticipants">${message(code: 'surveyParticipants.selectedSubParticipants')}
                    <div class="ui floating circular label">${selectedSubParticipants.size() ?: 0}</div>
                </a>
                </g:if>

                <a class="item ${params.tab == 'selectedParticipants' ? 'active' : ''}"
                   data-tab="selectedParticipants">${message(code: 'surveyParticipants.selectedParticipants')}
                    <div class="ui floating circular label">${selectedParticipants.size() ?: 0}</div></a>

                <g:if test="${editable}">
                    <a class="item ${params.tab == 'consortiaMembers' ? 'active' : ''}"
                       data-tab="consortiaMembers">${message(code: 'surveyParticipants.consortiaMembers')}
                        <div class="ui floating circular label">${consortiaMembers.size() ?: 0}</div></a>
                </g:if>
            </div>

            <div class="ui bottom attached tab segment ${params.tab == 'selectedSubParticipants' ? 'active' : ''}"
                 data-tab="selectedSubParticipants">

                <div>
                    <g:render template="selectedSubParticipants"/>
                </div>

            </div>


            <div class="ui bottom attached tab segment ${params.tab == 'selectedParticipants' ? 'active' : ''}"
                 data-tab="selectedParticipants">

                <div>
                    <g:render template="selectedParticipants"/>
                </div>

            </div>


            <div class="ui bottom attached tab segment ${params.tab == 'consortiaMembers' ? 'active' : ''}"
                 data-tab="consortiaMembers">
                <div>
                    <g:render template="consortiaMembers"
                              model="${[showAddSubMembers: (SurveyConfig.get(params.surveyConfigID)?.type == 'Subscription') ? true : false]}"/>

                </div>
            </div>
        </div>
    </div>
</g:if>
<g:else>
    <p><b>${message(code: 'surveyConfigs.noConfigList')}</b></p>
</g:else>

<r:script>
    $(document).ready(function () {
        $('.tabular.menu .item').tab()
    });
</r:script>

</body>
</html>
