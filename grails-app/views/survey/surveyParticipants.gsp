<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.survey.SurveyConfig;de.laser.RefdataCategory;de.laser.properties.PropertyDefinition;de.laser.storage.RDStore;" %>
<laser:htmlStart text="${message(code: 'survey.label')} (${message(code: 'surveyParticipants.label')})" />

<ui:breadcrumbs>
    <ui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code:'menu.my.surveys')}" />
    <g:if test="${surveyInfo}">
%{--        <ui:crumb controller="survey" action="show" id="${surveyInfo.id}" params="[surveyConfigID: surveyConfig.id]" text="${surveyConfig.getConfigNameShort()}" />--}%
        <ui:crumb class="active" text="${surveyConfig.getConfigNameShort()}" />
    </g:if>
%{--    <ui:crumb message="surveyParticipants.label" class="active"/>--}%
</ui:breadcrumbs>

<ui:controlButtons>
    <laser:render template="exports"/>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon text="${surveyInfo.name}" type="Survey"/>

<uiSurvey:statusWithRings object="${surveyInfo}" surveyConfig="${surveyConfig}" controller="survey" action="${actionName}"/>

<g:if test="${surveyConfig.subscription}">
 <ui:buttonWithIcon style="vertical-align: super;" message="${message(code: 'button.message.showLicense')}" variation="tiny" icon="${Icon.SUBSCRIPTION}" href="${createLink(action: 'show', controller: 'subscription', id: surveyConfig.subscription.id)}"/>
</g:if>

<laser:render template="nav"/>

<ui:objectStatus object="${surveyInfo}" />

<ui:messages data="${flash}"/>

<br />

<g:if test="${surveyConfig}">
    <div class="ui grid">
        <div class="sixteen wide stretched column">
            <br />

            <ui:filter>
                <g:form action="surveyParticipants" method="post" class="ui form"
                        params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: params.tab]">
                    <laser:render template="/templates/filter/orgFilter"
                                  model="[
                                          tmplConfigShow      : [['name', 'libraryType', 'subjectGroup'], ['country&region', 'libraryNetwork', 'property&value'], ['discoverySystemsFrontend', 'discoverySystemsIndex'], ['hasSubscription', 'subRunTimeMultiYear'], ['subscriptionAdjustDropdown']],
                                          tmplConfigFormFilter: true
                                  ]"/>
                </g:form>
            </ui:filter>

            <g:if test="${participantsTotal == 0}">
                <strong><g:message code="surveyParticipants.addParticipantsOverPencil"/></strong>
            </g:if>
            <g:elseif test="${participants.size() == 0 && participantsTotal > 0}">
                <strong><g:message code="default.search.empty"/></strong>
            </g:elseif>
            <g:else>
                <br/>

                <h3 class="ui icon header la-clear-before la-noMargin-top">
                    <ui:bubble count="${participants.size()}" grey="true"/> <g:message code="surveyParticipants.selectedParticipants"/>
                </h3>

                <br/><br/>
                <g:form action="actionSurveyParticipants" data-confirm-id="deleteSurveyParticipants_form" controller="survey"
                        method="post" class="ui form"
                        params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: params.tab, actionSurveyParticipants: 'deleteSurveyParticipants']">

                    <h3 class="ui header"><g:message code="surveyParticipants.hasAccess"/></h3>

                    <g:set var="surveyParticipantsHasAccess"
                           value="${participants?.findAll { it?.hasInstAdmin() }?.sort { it?.sortname }}"/>

                    <div class="four wide column">
                        <g:if test="${surveyParticipantsHasAccess}">
                            <a data-ui="modal" class="${Btn.SIMPLE} right floated"
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
                                          tmplConfigShow  : ['lineNumber', 'sortname', 'name', 'libraryType', (surveyConfig.subscription || params.subs ? 'surveySubInfo' : '')]
                                  ]"/>


                    <g:set var="surveyParticipantsHasNotAccess"
                           value="${participants.findAll { !it?.hasInstAdmin() }.sort { it?.sortname }}"/>
                    <g:if test="${surveyParticipantsHasNotAccess}">

                        <h3 class="ui header"><g:message code="surveyParticipants.hasNotAccess"/></h3>

                        <div class="four wide column">

                            <a data-ui="modal" class="${Btn.SIMPLE} right floated"
                               data-orgIdList="${(surveyParticipantsHasNotAccess.id)?.join(',')}" href="#copyEmailaddresses_static">
                                <g:message code="survey.copyEmailaddresses.participantsHasNoAccess"/>
                            </a>

                        </div>

                        <br/>
                        <br/>

                        <laser:render template="/templates/filter/orgFilterTable"
                                      model="[orgList         : surveyParticipantsHasNotAccess,
                                              tmplShowCheckbox: editable,
                                              tmplConfigShow  : ['lineNumber', 'sortname', 'name', 'libraryType', (surveyConfig.subscription || params.subs ? 'surveySubInfo' : '')]
                                      ]"/>

                        <br/>

                    </g:if>

                    <g:if test="${participants && editable}">
                        <button type="submit" data-confirm-id="deleteSurveyParticipants"
                                class="${Btn.NEGATIVE_CONFIRM}"
                                data-confirm-tokenMsg="${message(code: "confirm.dialog.unlink.surveyParticipants")}"
                                data-confirm-term-how="unlink"
                                role="button"
                                aria-label="${message(code: 'ariaLabel.unlink.universal')}">
                            <i class="${Icon.CMD.UNLINK}"></i> ${message(code: 'default.button.unlink.label')}
                        </button>
                    </g:if>

                </g:form>
            </g:else>
        </div>
    </div>
</g:if>
<g:else>
    <p><strong>${message(code: 'surveyConfigs.noConfigList')}</strong></p>
</g:else>

<laser:htmlEnd />
