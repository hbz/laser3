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
    <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}"/>
    <semui:crumb controller="survey" action="currentSurveys" message="currentSurveys.label"/>
    <semui:crumb message="survey" class="active"/>
</semui:breadcrumbs>

<h1 class="ui left aligned icon header"><semui:headerIcon/>${institution?.name} - ${message(code: 'survey.label')}</h1>


<g:render template="steps"/>
<br>

<semui:messages data="${flash}"/>

<br>
<semui:form>
    <div class="ui grid">
        <div class="middle aligned row">
            <div class="two wide column">

                <g:link controller="survey" action="showSurveyConfigDocs" id="${surveyInfo.id}"
                        class="ui huge button"><i class="angle left aligned icon"></i></g:link>

            </div>

            <div class="twelve wide column">

                <div class="la-inline-lists">
                    <div class="ui card">
                        <div class="content">

                            <div class="header">
                                <div class="ui grid">
                                    <div class="twelve wide column">
                                        ${message(code: 'showSurveyInfo.step.first.title')}
                                    </div>
                                </div>
                            </div>
                            <dl>
                                <dt>${message(code: 'surveyInfo.status.label', default: 'Survey Status')}</dt>
                                <dd>${surveyInfo.status?.getI10n('value')}</dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'surveyInfo.name.label', default: 'New Survey Name')}</dt>
                                <dd>${surveyInfo.name}</dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'surveyInfo.startDate.label')}</dt>
                                <dd><g:formatDate formatName="default.date.format.notime"
                                                  date="${surveyInfo.startDate ?: null}"/></dd>
                            </dl>
                            <dl>
                                <dt>${message(code: 'surveyInfo.endDate.label')}</dt>
                                <dd><g:formatDate formatName="default.date.format.notime"
                                                  date="${surveyInfo.endDate ?: null}"/></dd>
                            </dl>

                            <dl>
                                <dt>${message(code: 'surveyInfo.type.label')}</dt>
                                <dd>${com.k_int.kbplus.RefdataValue.get(surveyInfo?.type?.id)?.getI10n('value')}</dd>
                            </dl>

                        </div>
                    </div>
                </div>
            </div>

            <div class="two wide column">
                <g:if test="${surveyConfigs.size() > 0}">

                    <g:link controller="survey" action="openSurvey" id="${surveyInfo.id}"
                            class="ui huge button"><i class="angle right icon"></i></g:link>

                </g:if>
            </div>
        </div>

    </div>

</semui:form>

<br>

<h2 class="ui left aligned icon header">${message(code: 'showSurveyConfig.list')} <semui:totalNumber
        total="${surveyConfigs.size()}"/></h2>

<br>

<g:if test="${surveyConfigs}">
    <div class="ui grid">
        <div class="four wide column">
            <div class="ui vertical fluid menu">
                <g:each in="${surveyConfigs.sort { it.configOrder }}" var="config" status="i">

                    <g:link class="item ${params.surveyConfigID == config?.id.toString() ? 'active' : ''}"
                            controller="survey" action="showSurveyParticipants"
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
                   data-tab="selectedSubParticipants">${message(code: 'showSurveyParticipants.selectedSubParticipants')}
                    <div class="ui floating circular label">${selectedSubParticipants.size() ?: 0}</div>
                </a>

                <a class="item ${params.tab == 'selectedParticipants' ? 'active' : ''}"
                   data-tab="selectedParticipants">${message(code: 'showSurveyParticipants.selectedParticipants')}
                    <div class="ui floating circular label">${selectedParticipants.size() ?: 0}</div></a>

                <g:if test="${editable}">
                    <a class="item ${params.tab == 'consortiaMembers' ? 'active' : ''}"
                       data-tab="consortiaMembers">${message(code: 'showSurveyParticipants.consortiaMembers')}
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
    <p><b>${message(code: 'showSurveyConfig.noConfigList')}</b></p>
</g:else>

<r:script>
    $(document).ready(function () {
        $('.tabular.menu .item').tab()
    });
</r:script>

</body>
</html>
