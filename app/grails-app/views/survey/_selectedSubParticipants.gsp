
<br>
<g:if test="${surveyConfig?.type == 'Subscription'}">
    <h3 class="ui icon header"><semui:headerIcon/>
    <g:link controller="subscription" action="show" id="${surveyConfig?.subscription?.id}">
        ${surveyConfig?.subscription?.name}
    </g:link>
    </h3>
</g:if>
<g:else>
<h3 class="ui left aligned">${surveyConfig?.getConfigNameShort()}</h3>
</g:else>

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

    <g:set var="surveyParticipantsHasAccess"
           value="${selectedSubParticipants?.findAll{ it?.hasAccessOrg() }?.sort{ it?.sortname }}"/>

    <div class="four wide column">
        <g:link data-orgIdList="${(surveyParticipantsHasAccess.id)?.join(',')}"
                data-targetId="copyEmailaddresses_ajaxModal2"
                class="ui icon button right floated trigger-modal">
            <g:message code="survey.copyEmailaddresses.participantsHasAccess"/>
        </g:link>
    </div>
    <br>
    <br>


    <g:render template="/templates/filter/orgFilterTable"
              model="[orgList         : selectedSubParticipants.findAll { it?.hasAccessOrg() }.sort { it?.sortname },
                      tmplShowCheckbox: editable,
                      tmplConfigShow  : ['lineNumber', 'sortname', 'name', 'libraryType', 'surveySubInfo']
              ]"/>


    <h3><g:message code="surveyParticipants.hasNotAccess"/></h3>


    <g:set var="surveyParticipantsHasNotAccess"
           value="${selectedSubParticipants.findAll{ !it?.hasAccessOrg() }.sort{ it?.sortname }}"/>

    <div class="four wide column">
        <g:link data-orgIdList="${(surveyParticipantsHasNotAccess.id)?.join(',')}"
                data-targetId="copyEmailaddresses_ajaxModal3"
                class="ui icon button right floated trigger-modal">
            <g:message code="survey.copyEmailaddresses.participantsHasNoAccess"/>
        </g:link>
    </div>

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