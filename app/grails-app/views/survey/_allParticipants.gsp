<%@ page import="com.k_int.kbplus.Org;" %>

<g:set var="surveyConfigOrgs" value="${Org.findAllByIdInList(surveyConfig?.orgIDs) ?: null}" />

<h2 class="ui left aligned icon header">${message(code: 'showSurveyParticipants.selectedSubParticipants')}<semui:totalNumber
        total="${surveyConfigOrgs?.size()}"/></h2>
<br>

${surveyConfigSubOrgs?.id}
    <g:render template="/templates/filter/orgFilterTable"
              model="[orgList         : surveyConfigOrgs,
                      tmplConfigShow  : ['sortname', 'name', 'libraryType', 'surveySubInfo'],
                      surveyConfig: surveyConfig
              ]"/>

    <br/>
