<%@ page import="de.laser.survey.SurveyConfig; de.laser.storage.RDStore;" %>
<br />

<ui:filter>
    <g:form action="surveyParticipants" method="post" class="ui form"
            params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: params.tab]">
        <laser:render template="/templates/filter/orgFilter"
                  model="[
                          tmplConfigShow      : [['name', 'libraryType', 'subjectGroup'], ['country&region', 'libraryNetwork', 'property&value'], ['discoverySystemsFrontend', 'discoverySystemsIndex'], ['subStatus'], surveyConfig.subscription ? ['hasSubscription'] : []],
                          tmplConfigFormFilter: true
                  ]"/>
    </g:form>
</ui:filter>
<g:if test="${consortiaMembers.size() == 0 && consortiaMembersCount > 0}">
    <strong><g:message code="default.search.empty"/></strong>
</g:if>
<g:else>
    <br/>

    <h3 class="ui icon header la-clear-before la-noMargin-top">
        <span class="ui circular label">${consortiaMembers.size()}</span> <g:message code="surveyParticipants.consortiaMembers"/>
    </h3>

    <br/><br/>
    <g:form action="actionSurveyParticipants" controller="survey" method="post" class="ui form"
            params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: params.tab, actionSurveyParticipants: addSurveyParticipants]">

        <laser:render template="/templates/filter/orgFilterTable"
                      model="[orgList          : consortiaMembers,
                              tmplShowCheckbox : (surveyInfo.status in [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY, RDStore.SURVEY_SURVEY_STARTED]),
                              tmplDisableOrgIds: surveyConfig.orgs?.org?.id,
                              tmplConfigShow   : ['lineNumber', 'sortname', 'name', 'libraryType', (surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION ? 'surveySubInfo' : '')]
                      ]"/>

        <br/>
        <g:if test="${surveyInfo.status in [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY, RDStore.SURVEY_SURVEY_STARTED]}">
            <input type="submit" class="ui button" value="${message(code: 'default.button.add.label')}"/>
        </g:if>

    </g:form>
</g:else>
