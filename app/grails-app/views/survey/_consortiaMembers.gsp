<%@ page import="de.laser.helper.RDStore;" %>
<br>

<semui:filter>
    <g:form action="surveyParticipants" method="post" class="ui form"
            params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: params.tab]">
        <g:render template="/templates/filter/orgFilter"
                  model="[
                          tmplConfigShow      : [['name', 'libraryType'], ['region', 'libraryNetwork', 'property']],
                          tmplConfigFormFilter: true,
                          useNewLayouter      : true
                  ]"/>
    </g:form>
</semui:filter>


<g:form action="addSurveyParticipants" controller="survey" method="post" class="ui form"
        params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: params.tab]">

    <g:render template="/templates/filter/orgFilterTable"
              model="[orgList          : consortiaMembers,
                      tmplShowCheckbox : (surveyInfo.status in [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY, RDStore.SURVEY_SURVEY_STARTED]),
                      tmplDisableOrgIds: surveyConfig.orgs?.org?.id,
                      tmplConfigShow   : ['lineNumber', 'sortname', 'name', 'libraryType', (surveyConfig.type == 'Subscription' ? 'surveySubInfo' : '')]
              ]"/>

    <br/>
    <g:if test="${surveyInfo.status in [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY, RDStore.SURVEY_SURVEY_STARTED]}">
        <input type="submit" class="ui button" value="${message(code: 'default.button.add.label')}"/>
    </g:if>

</g:form>
