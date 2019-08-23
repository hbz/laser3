<%@ page import="com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.SurveyProperty;com.k_int.kbplus.SurveyConfig" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'survey.label')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${contextService.getOrg()?.getDesignation()}" />
    <semui:crumb controller="survey" action="currentSurveysConsortia" text="${message(code:'menu.my.surveys')}" />
    <g:if test="${surveyInfo}">
        <semui:crumb controller="survey" action="show" id="${surveyInfo.id}" text="${surveyInfo.name}" />
    </g:if>
    <g:if test="${surveyInfo}">
        <semui:crumb controller="survey" action="surveyConfigsInfo" id="${surveyInfo.id}" params="[surveyConfigID: surveyConfig]" text="${surveyConfig?.getConfigNameShort()}" />
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

<h2 class="ui left aligned icon header">
    <g:if test="${surveyConfig?.type == 'Subscription'}">
        <i class="icon clipboard outline la-list-icon"></i>
        <g:link controller="subscription" action="show" id="${surveyConfig?.subscription?.id}">
            ${surveyConfig?.subscription?.name}
        </g:link>
    </g:if>
    <g:else>
        ${surveyConfig?.getConfigNameShort()}
    </g:else>
    : ${message(code: 'surveyParticipants.label')}
</h2>

<br>

<g:if test="${surveyConfigs}">
    <div class="ui grid">
        %{-- <div class="four wide column">
             <div class="ui vertical fluid menu">
                 <g:each in="${surveyConfigs.sort { it.configOrder }}" var="config" status="i">

                     <g:link class="item ${params.surveyConfigID == config?.id.toString() ? 'active' : ''}"
                             controller="survey" action="surveyParticipants"
                             id="${config?.surveyInfo?.id}" params="[surveyConfigID: config?.id]">

                         <h5 class="ui header">${config?.getConfigNameShort()}</h5>
                         ${com.k_int.kbplus.SurveyConfig.getLocalizedValue(config?.type)}


                         <div class="ui floating circular label">${config?.orgs?.size() ?: 0}</div>
                     </g:link>
                 </g:each>
             </div>
         </div>--}%

        <div class="sixteen wide stretched column">
            <div class="ui top attached tabular menu">

                <g:if test="${surveyConfig?.type == 'Subscription'}">
                    <g:link class="item ${params.tab == 'selectedSubParticipants' ? 'active' : ''}"
                            controller="survey" action="surveyParticipants"
                            id="${surveyConfig?.surveyInfo?.id}"
                            params="[surveyConfigID: surveyConfig?.id, tab: 'selectedSubParticipants']">
                        ${message(code: 'surveyParticipants.selectedSubParticipants')}
                        <div class="ui floating circular label">${selectedSubParticipants.size() ?: 0}</div>
                    </g:link>
                </g:if>

                <g:link class="item ${params.tab == 'selectedParticipants' ? 'active' : ''}"
                        controller="survey" action="surveyParticipants"
                        id="${surveyConfig?.surveyInfo?.id}"
                        params="[surveyConfigID: surveyConfig?.id, tab: 'selectedParticipants']">
                    ${message(code: 'surveyParticipants.selectedParticipants')}
                    <div class="ui floating circular label">${selectedParticipants.size() ?: 0}</div></g:link>

                <g:if test="${editable}">
                    <g:link class="item ${params.tab == 'consortiaMembers' ? 'active' : ''}"
                            controller="survey" action="surveyParticipants"
                            id="${surveyConfig?.surveyInfo?.id}"
                            params="[surveyConfigID: surveyConfig?.id, tab: 'consortiaMembers']">
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
                    <g:render template="consortiaMembers"
                              model="${[showAddSubMembers: (SurveyConfig.get(params.surveyConfigID)?.type == 'Subscription') ? true : false]}"/>

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
