<h2 class="ui left aligned icon header">${message(code: 'surveyParticipants.selectedSubParticipants')}<semui:totalNumber
        total="${selectedSubParticipants?.size()}"/></h2>
<br>

<h3 class="ui left aligned">${surveyConfig?.getConfigName()}</h3>
<br>

<semui:filter>
    <g:form action="surveyParticipants" method="post" class="ui form"
            params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: 'selectedSubParticipants']">
        <g:render template="/templates/filter/orgFilter"
                  model="[
                          tmplConfigShow      : [['name', 'libraryType'], ['federalState', 'libraryNetwork', 'property'], ['customerType']],
                          tmplConfigFormFilter: true,
                          useNewLayouter      : true
                  ]"/>
    </g:form>
</semui:filter>


<g:form action="deleteSurveyParticipants" controller="survey" method="post" class="ui form"
        params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: params.tab]">

    <h3><g:message code="surveyParticipants.hasAccess"/></h3>

    <g:render template="/templates/filter/orgFilterTable"
              model="[orgList         : selectedSubParticipants.findAll { it?.hasAccessOrg() }.sort { it?.sortname },
                      tmplShowCheckbox: editable,
                      tmplConfigShow  : ['lineNumber', 'sortname', 'name', 'libraryType', 'surveySubInfo']
              ]"/>


    <h3><g:message code="surveyParticipants.hasNotAccess"/></h3>


    <g:set var="surveyParticipantsHasNotAccess" value="${selectedSubParticipants.findAll { !it?.hasAccessOrg() }.sort { it?.sortname }}"/>

    <div class="four wide column">
        <button type="button" class="ui icon button right floated" data-semui="modal"
                data-href="#copyEmailaddresses_selectedSubParticipants"><g:message code="survey.copyEmailaddresses.participantsHasNoAccess"/></button>
    </div>

    <g:render template="../templates/copyEmailaddresses"
              model="[orgList: surveyParticipantsHasNotAccess ?: null, modalID: 'copyEmailaddresses_selectedSubParticipants']"/>

    <br>
    <br>
    <g:render template="/templates/filter/orgFilterTable"
              model="[orgList         : surveyParticipantsHasNotAccess,
                      tmplShowCheckbox: editable,
                      tmplConfigShow  : ['lineNumber', 'sortname', 'name', 'libraryType', 'surveySubInfo']
              ]"/>

    <br/>

    <g:if test="${selectedSubParticipants && editable}">
        <input type="submit" class="ui button"
               value="${message(code: 'default.button.delete.label', default: 'Delete')}"/>
    </g:if>

</g:form>