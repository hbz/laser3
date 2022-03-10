<%@ page import="de.laser.SurveyConfig;" %>
<br />
%{--<g:if test="${surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION}">
    <h3 class="ui icon header"><semui:headerIcon/>
    <g:link controller="subscription" action="show" id="${surveyConfig.subscription?.id}">
        ${surveyConfig.subscription?.name}
    </g:link>
    </h3>
</g:if>
<g:else>
<h3 class="ui left aligned">${surveyConfig.getConfigNameShort()}</h3>
</g:else>--}%

<div class="four wide column">

        <g:link action="addSubMembersToSurvey" params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: params.tab]"
                class="ui icon button right floated">
            <g:message code="surveyParticipants.addSubMembersToSurvey"/>
        </g:link>
    <br />
    <br />


</div>


<g:render template="/templates/filter/javascript" />

<semui:filter showFilterButton="true">
    <g:form action="surveyParticipants" method="post" class="ui form"
            params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: params.tab]">
        <g:render template="/templates/filter/orgFilter"
                  model="[
                          tmplConfigShow      : [['name', 'libraryType', 'subjectGroup'], ['country&region', 'libraryNetwork', 'property&value']],
                          tmplConfigFormFilter: true
                  ]"/>
    </g:form>
</semui:filter>

<br><br>
<g:form action="deleteSurveyParticipants" data-confirm-id="deleteSurveyParticipants_form" controller="survey" method="post" class="ui form"
        params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: params.tab]">

    <h3 class="ui header"><g:message code="surveyParticipants.hasAccess"/></h3>

    <g:set var="surveyParticipantsHasAccess"
           value="${selectedSubParticipants?.findAll{ it?.hasAccessOrg() }?.sort{ it?.sortname }}"/>

    <div class="four wide column">
    <g:if test="${surveyParticipantsHasAccess}">

        <a data-semui="modal" class="ui icon button right floated" data-orgIdList="${(surveyParticipantsHasAccess.id)?.join(',')}" href="#copyEmailaddresses_static">
            <g:message code="survey.copyEmailaddresses.participantsHasAccess"/>
        </a>

    </g:if>
    </div>
    <br />
    <br />


    <g:render template="/templates/filter/orgFilterTable"
              model="[orgList         : surveyParticipantsHasAccess,
                      tmplShowCheckbox: editable,
                      tmplConfigShow  : ['lineNumber', 'sortname', 'name', 'libraryType', 'surveySubInfo']
              ]"/>


    <h3 class="ui header"><g:message code="surveyParticipants.hasNotAccess"/></h3>


    <g:set var="surveyParticipantsHasNotAccess"
           value="${selectedSubParticipants.findAll{ !it?.hasAccessOrg() }.sort{ it?.sortname }}"/>

    <div class="four wide column">
    <g:if test="${surveyParticipantsHasNotAccess}">
        <a data-semui="modal" class="ui icon button right floated" data-orgIdList="${(surveyParticipantsHasNotAccess.id)?.join(',')}" href="#copyEmailaddresses_static">
            <g:message code="survey.copyEmailaddresses.participantsHasNoAccess"/>
        </a>
    </g:if>
    </div>

    <br />
    <br />
    <g:render template="/templates/filter/orgFilterTable"
              model="[orgList         : surveyParticipantsHasNotAccess,
                      tmplShowCheckbox: editable,
                      tmplConfigShow  : ['lineNumber', 'sortname', 'name', 'libraryType', 'surveySubInfo']
              ]"/>

    <br />

    <g:if test="${selectedSubParticipants && editable}">
        <button type="submit" data-confirm-id="deleteSurveyParticipants" class="ui icon negative button js-open-confirm-modal"
                        data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.surveyParticipants")}"
                        data-confirm-term-how="delete"
                        role="button"
                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
        <i class="trash alternate outline icon"></i> ${message(code: 'default.button.delete.label')}
        </button>
    </g:if>

</g:form>
