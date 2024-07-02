<%@ page import="de.laser.helper.Icons" %>
<br />

<ui:filter>
    <g:form action="surveyParticipants" method="post" class="ui form"
            params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: params.tab]">
        <laser:render template="/templates/filter/orgFilter"
                  model="[
                          tmplConfigShow      : [['name', 'libraryType', 'subjectGroup'], ['country&region', 'libraryNetwork', 'property&value'], ['discoverySystemsFrontend', 'discoverySystemsIndex'], ['subStatus']],
                          tmplConfigFormFilter: true
                  ]"/>
    </g:form>
</ui:filter>

<g:if test="${selectedParticipants.size() == 0 && selectedParticipantsCount > 0}">
    <strong><g:message code="default.search.empty"/></strong>
</g:if>
<g:else>
    <br/>

    <h3 class="ui icon header la-clear-before la-noMargin-top">
        <span class="ui circular label">${selectedParticipants.size()}</span> <g:message code="surveyParticipants.selectedParticipants"/>
    </h3>

    <br/><br/>
    <g:form action="actionSurveyParticipants" data-confirm-id="deleteSurveyParticipants_form" controller="survey"
            method="post" class="ui form"
            params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: params.tab, actionSurveyParticipants: 'deleteSurveyParticipants']">

        <h3 class="ui header"><g:message code="surveyParticipants.hasAccess"/></h3>

        <g:set var="surveyParticipantsHasAccess"
               value="${selectedParticipants?.findAll { it?.hasInstAdmin() }?.sort { it?.sortname }}"/>

        <div class="four wide column">
            <g:if test="${surveyParticipantsHasAccess}">
                <a data-ui="modal" class="ui icon button right floated"
                   data-orgIdList="${(surveyParticipantsHasAccess.id)?.join(',')}" href="#copyEmailaddresses_static">
                    <g:message code="survey.copyEmailaddresses.participantsHasAccess"/>
                </a>
            </g:if>
        </div>
        <br/>
        <br/>

        <laser:render template="/templates/filter/orgFilterTable"
                      model="[orgList         : surveyParticipantsHasAccess,
                              tmplShowCheckbox: editable,
                              tmplConfigShow  : ['lineNumber', 'sortname', 'name', 'libraryType']
                      ]"/>


        <h3 class="ui header"><g:message code="surveyParticipants.hasNotAccess"/></h3>

        <g:set var="surveyParticipantsHasNotAccess"
               value="${selectedParticipants.findAll { !it?.hasInstAdmin() }.sort { it?.sortname }}"/>

        <div class="four wide column">
            <g:if test="${surveyParticipantsHasNotAccess}">
                <a data-ui="modal" class="ui icon button right floated"
                   data-orgIdList="${(surveyParticipantsHasNotAccess.id)?.join(',')}" href="#copyEmailaddresses_static">
                    <g:message code="survey.copyEmailaddresses.participantsHasNoAccess"/>
                </a>
            </g:if>
        </div>

        <br/>
        <br/>

        <laser:render template="/templates/filter/orgFilterTable"
                      model="[orgList         : surveyParticipantsHasNotAccess,
                              tmplShowCheckbox: editable,
                              tmplConfigShow  : ['lineNumber', 'sortname', 'name', 'libraryType']
                      ]"/>

        <br/>

        <g:if test="${selectedParticipants && editable}">
            <button type="submit" data-confirm-id="deleteSurveyParticipants"
                    class="ui icon negative button js-open-confirm-modal"
                    data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.surveyParticipants")}"
                    data-confirm-term-how="unlink"
                    role="button"
                    aria-label="${message(code: 'ariaLabel.unlink.universal')}">
                <i class="${Icons.CMD_UNLINK} icon"></i> ${message(code: 'default.button.unlink.label')}
            </button>
        </g:if>

    </g:form>
</g:else>