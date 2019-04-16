<h2 class="ui left aligned icon header">${message(code: 'showSurveyParticipants.consortiaMembers')}<semui:totalNumber
        total="${consortiaMembers?.size()}"/></h2>
<br>



<semui:filter>
    <g:form action="showSurveyParticipants" method="post" class="ui form"
            params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: 'consortiaMembers']">
        <g:render template="/templates/filter/orgFilter"
                  model="[
                          tmplConfigShow      : [['name', 'libraryType'], ['federalState', 'libraryNetwork', 'property']],
                          tmplConfigFormFilter: true,
                          useNewLayouter      : true
                  ]"/>
    </g:form>
</semui:filter>


<g:form action="addSurveyParticipants" controller="survey" method="post" class="ui form"
        params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: params.tab]">

    <g:render template="/templates/filter/orgFilterTable"
              model="[orgList          : consortiaMembers,
                      tmplShowCheckbox : true,
                      tmplDisableOrgIds: surveyConfigOrgs?.id,
                      tmplConfigShow   : ['sortname', 'name', 'libraryType']
              ]"/>

    <br/>
    <g:if test="${editable}">
        <input type="submit" class="ui button" value="${message(code: 'default.button.add.label', default: 'Add')}"/>
    </g:if>

    <g:if test="${showAddSubMembers && (selectedSubParticipants.size() != surveyConfigSubOrgs.size()) && editable}">
        <br/>
        <br/>
        <g:link class="ui button" controller="survey" action="addSubMembers"
                id="${surveyConfig?.surveyInfo?.id}"
                params="[surveyConfigID: surveyConfig?.id]">${message(code: 'showAddSubMembers.button.addSubMembers')}</g:link>
    </g:if>

</g:form>