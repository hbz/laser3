
<h2 class="ui left aligned icon header">${message(code: 'showSurveyParticipants.selectedParticipants')}<semui:totalNumber
        total="${surveyConfigOrgs?.size()}"/></h2>
<br>

<g:render template="/templates/filter/orgFilterTable"
          model="[orgList: surveyConfigOrgs,
                  tmplShowCheckbox: true,
                  tmplConfigShow: ['sortname', 'name', 'libraryType']
          ]"/>