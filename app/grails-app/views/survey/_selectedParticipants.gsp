<h2 class="ui left aligned icon header">${message(code: 'showSurveyParticipants.selectedParticipants')}<semui:totalNumber
        total="${selectedParticipants?.size()}"/></h2>
<br>

<semui:filter>
    <g:form action="showSurveyParticipants" method="post" class="ui form" params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: 'selectedParticipants']">
        <g:render template="/templates/filter/orgFilter"
                  model="[
                          tmplConfigShow: [['name', 'libraryType'], ['federalState', 'libraryNetwork','property']],
                          tmplConfigFormFilter: true,
                          useNewLayouter: true
                  ]"/>
    </g:form>
</semui:filter>

<g:form action="deleteSurveyParticipants" controller="survey" method="post" class="ui form"
        params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: params.tab]">

    <g:render template="/templates/filter/orgFilterTable"
              model="[orgList         : selectedParticipants,
                      tmplShowCheckbox: true,
                      tmplConfigShow  : ['sortname', 'name', 'libraryType']
              ]"/>

    <br/>

    <g:if test="${selectedParticipants && editable}">
        <input type="submit" class="ui button"
               value="${message(code: 'default.button.delete.label', default: 'Delete')}"/>
    </g:if>

</g:form>