<semui:filter>
    <g:form action="showSurveyParticipants" method="post" class="ui form" params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: 'consortiaMembers']">
        <g:render template="/templates/filter/orgFilter"
                  model="[
                          tmplConfigShow: [['name', 'libraryType'], ['federalState', 'libraryNetwork','property']],
                          tmplConfigFormFilter: true,
                          useNewLayouter: true
                  ]"/>
    </g:form>
</semui:filter>


<g:form action="addSurveyParticipants" controller="survey" method="post" class="ui form"  params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: params.tab]">

    <g:render template="/templates/filter/orgFilterTable"
              model="[orgList: consortiaMembers,
                      tmplShowCheckbox: true,
                      tmplConfigShow: ['sortname', 'name', 'libraryType']
              ]"/>

</g:form>