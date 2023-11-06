<%@ page import="de.laser.survey.SurveyConfig; de.laser.Org;de.laser.RefdataCategory;de.laser.survey.SurveyInfo;de.laser.storage.RDStore; de.laser.OrgRole;de.laser.RefdataValue;de.laser.properties.PropertyDefinition;de.laser.Subscription;de.laser.finance.CostItem;de.laser.survey.SurveyResult" %>
<laser:htmlStart message="manageParticipantSurveys.header" serviceInjection="true"/>

<ui:breadcrumbs>
    <ui:crumb message="manageParticipantSurveys.header" class="active"/>
</ui:breadcrumbs>
<ui:controlButtons>
    <ui:exportDropdown>
        <ui:exportDropdownItem>
            <g:link class="item" controller="myInstitution" action="manageParticipantSurveys"
                    params="${params + [exportXLSX: true]}">${message(code: 'survey.exportSurveys')}</g:link>
        </ui:exportDropdownItem>
    </ui:exportDropdown>
</ui:controlButtons>

<ui:h1HeaderWithIcon message="manageParticipantSurveys.header" total="${countSurveys.values().sum { it }}" floated="true" />

<ui:messages data="${flash}"/>

<ui:filter>
    <g:form action="manageParticipantSurveys" controller="myInstitution" method="post" id="${params.id}"
            params="[tab: params.tab]" class="ui small form">

        <div class="three fields">
            <div class="field">
                <label for="name">${message(code: 'surveyInfo.name.label')}</label>

                <div class="ui input">
                    <input type="text" id="name" name="name"
                           placeholder="${message(code: 'default.search.ph')}"
                           value="${params.name}"/>
                </div>
            </div>

            <div class="field">
                <ui:datepicker label="default.valid_on.label" id="validOn" name="validOn" placeholder="filter.placeholder" value="${params.validOn}" />
            </div>

            <div class="field">
                <label>${message(code: 'default.valid_onYear.label')}</label>
                <g:select name="validOnYear"
                          from="${surveyYears}"
                          class="ui fluid search selection dropdown"
                          value="${params.validOnYear}"
                          noSelection="${['': message(code: 'default.select.choose.label')]}"/>

            </div>

        </div>

        <div class="two fields">

            <div class="field">
                <label>${message(code: 'surveyInfo.type.label')}</label>
                <ui:select class="ui dropdown" name="type"
                              from="${RefdataCategory.getAllRefdataValues(de.laser.storage.RDConstants.SURVEY_TYPE)}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.type}"
                              noSelection="${['': message(code: 'default.select.choose.label')]}"/>
            </div>

            <div class="field">
                <label>${message(code: 'surveyInfo.options')}</label>

                <div class="inline fields la-filter-inline">
                    <div class="inline field">
                        <div class="ui checkbox">
                            <label for="checkMandatory">${message(code: 'surveyInfo.isMandatory.label')}</label>
                            <input id="checkMandatory" name="mandatory" type="checkbox"
                                   <g:if test="${params.mandatory}">checked=""</g:if>
                                   tabindex="0">
                        </div>
                    </div>

                    <div class="inline field">
                        <div class="ui checkbox">
                            <label for="checkNoMandatory">${message(code: 'surveyInfo.isNotMandatory.label')}</label>
                            <input id="checkNoMandatory" name="noMandatory" type="checkbox"
                                   <g:if test="${params.noMandatory}">checked=""</g:if>
                                   tabindex="0">
                        </div>
                    </div>

                    <div class="inline field">
                        <div class="ui checkbox">
                            <label for="checkSubSurveyUseForTransfer">${message(code: 'surveyconfig.subSurveyUseForTransfer.label')}</label>
                            <input id="checkSubSurveyUseForTransfer" name="checkSubSurveyUseForTransfer" type="checkbox"
                                   <g:if test="${params.checkSubSurveyUseForTransfer}">checked=""</g:if>
                                   tabindex="0">
                        </div>
                    </div>
                </div>
            </div>

        </div>

        <div class="field la-field-right-aligned">

            <div class="field la-field-right-aligned">
                <a href="${request.forwardURI}"
                   class="ui reset secondary button">${message(code: 'default.button.reset.label')}</a>
                <input type="submit" class="ui primary button"
                       value="${message(code: 'default.button.filter.label')}">
            </div>

        </div>
    </g:form>
</ui:filter>

<g:if test="${participant}">
    <g:set var="choosenOrg" value="${Org.findById(participant.id)}"/>
    <g:set var="choosenOrgCPAs" value="${choosenOrg?.getGeneralContactPersons(false)}"/>

    <table class="ui table la-js-responsive-table la-table compact">
        <tbody>
        <tr>
            <td>
                <p><strong><g:link controller="organisation" action="show" id="${choosenOrg.id}">${choosenOrg.name} (${choosenOrg.sortname})</g:link></strong></p>

                ${choosenOrg?.libraryType?.getI10n('value')}
            </td>
            <td>
                <g:if test="${choosenOrgCPAs}">
                    <g:set var="oldEditable" value="${editable}"/>
                    <g:set var="editable" value="${false}" scope="request"/>
                    <g:each in="${choosenOrgCPAs}" var="gcp">
                        <laser:render template="/templates/cpa/person_details"
                                  model="${[person: gcp, tmplHideLinkToAddressbook: true]}"/>
                    </g:each>
                    <g:set var="editable" value="${oldEditable ?: false}" scope="request"/>
                </g:if>
            </td>
        </tr>
        </tbody>
    </table>
</g:if>


<div>

        <ui:tabs actionName="${actionName}">
            <ui:tabsItem controller="myInstitution" action="manageParticipantSurveys"
                         params="${[id: params.id, tab: 'open', name: params.name, validOn: params.validOn, validOnYear: params.validOnYear, type: params.type, mandatory: params.mandatory, noMandatory: params.noMandatory, checkSubSurveyUseForTransfer: params.checkSubSurveyUseForTransfer]}" text="${message(code: "surveys.tabs.open")}" tab="open"
                         counts="${countSurveys?.open}"/>
           %{-- <ui:tabsItem controller="myInstitution" action="manageParticipantSurveys"
                            params="${[id: params.id, tab: 'new', name: params.name, validOn: params.validOn, validOnYear: params.validOnYear, type: params.type, mandatory: params.mandatory, noMandatory: params.noMandatory, checkSubSurveyUseForTransfer: params.checkSubSurveyUseForTransfer]}" text="${message(code: "surveys.tabs.new")}" tab="new"
                            counts="${countSurveys?.new}"/>
            <ui:tabsItem controller="myInstitution" action="manageParticipantSurveys"
                            params="${[id: params.id, tab: 'processed', name: params.name, validOn: params.validOn, validOnYear: params.validOnYear, type: params.type, mandatory: params.mandatory, noMandatory: params.noMandatory, checkSubSurveyUseForTransfer: params.checkSubSurveyUseForTransfer]}" text="${message(code: "surveys.tabs.processed")}" tab="processed"
                            counts="${countSurveys?.processed}"/>--}%
            <ui:tabsItem controller="myInstitution" action="manageParticipantSurveys"
                            params="${[id: params.id, tab: 'finish', name: params.name, validOn: params.validOn, validOnYear: params.validOnYear, type: params.type, mandatory: params.mandatory, noMandatory: params.noMandatory, checkSubSurveyUseForTransfer: params.checkSubSurveyUseForTransfer]}" text="${message(code: "surveys.tabs.finish")}" tab="finish"
                            counts="${countSurveys?.finish}"/>
            <ui:tabsItem controller="myInstitution" action="manageParticipantSurveys" class="ui red"
                            countsClass="red"
                            params="${[id: params.id, tab: 'termination', name: params.name, validOn: params.validOn, validOnYear: params.validOnYear, type: params.type, mandatory: params.mandatory, noMandatory: params.noMandatory, checkSubSurveyUseForTransfer: params.checkSubSurveyUseForTransfer]}" text="${message(code: "surveys.tabs.termination")}"
                            tab="termination"
                            counts="${countSurveys?.termination}"/>
            <ui:tabsItem controller="myInstitution" action="manageParticipantSurveys" class="ui orange" countsClass="orange"
                            params="${[id: params.id, tab: 'notFinish', name: params.name, validOn: params.validOn, validOnYear: params.validOnYear, type: params.type, mandatory: params.mandatory, noMandatory: params.noMandatory, checkSubSurveyUseForTransfer: params.checkSubSurveyUseForTransfer]}" text="${message(code: "surveys.tabs.notFinish")}" tab="notFinish"
                            counts="${countSurveys?.notFinish}"/>
        </ui:tabs>


    <div class="ui bottom attached tab segment active">
        <table class="ui celled sortable table la-js-responsive-table la-table">
            <thead>
            <tr>
                <th rowspan="2" class="center aligned">
                    ${message(code: 'sidewide.number')}
                </th>
                <g:sortableColumn params="${params}" property="surveyInfo.name"
                                  title="${message(code: 'surveyInfo.slash.name')}"/>
                <g:sortableColumn params="${params}" property="surveyInfo.type"
                                  title="${message(code: 'surveyInfo.type.label')}"/>
                <g:sortableColumn params="${params}" property="surveyInfo.endDate"
                                  title="${message(code: 'default.endDate.label')}"/>
                <th><g:message code="surveyInfo.finished"/></th>
                <g:if test="${params.tab == 'finish'}">
                    <th><g:message code="surveyInfo.finishedDate"/></th>
                </g:if>
                <th class="la-action-info">${message(code: 'default.actions.label')}</th>
            </tr>

            </thead>
            <g:each in="${surveyResults}" var="surveyResult" status="i">

                <g:set var="surveyConfig"
                       value="${SurveyConfig.get(surveyResult.key)}"/>

                <g:set var="surveyInfo"
                       value="${surveyConfig.surveyInfo}"/>

                <tr>
                    <td class="center aligned">
                        ${(params.int('offset') ?: 0) + i + 1}
                    </td>
                    <td>
                        <div class="la-flexbox">
                            <g:if test="${surveyConfig?.subSurveyUseForTransfer}">
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                      data-content="${message(code: "surveyconfig.subSurveyUseForTransfer.label.info2")}">
                                    <i class="icon pie chart la-list-icon"></i>
                                </span>
                            </g:if>
                            <g:link controller="survey" action="show" id="${surveyInfo.id}" class="ui ">
                                ${surveyConfig?.getSurveyName()}
                            </g:link>
                        </div>
                    </td>
                    <td>
                        <div class="ui label survey-${surveyInfo.type.value}">
                            ${surveyInfo.type.getI10n('value')}
                        </div>

                        <g:if test="${surveyInfo.isMandatory}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                  data-content="${message(code: "surveyInfo.isMandatory.label.info2")}">
                                <i class="yellow icon exclamation triangle"></i>
                            </span>
                        </g:if>
                    </td>
                    <td>
                        <g:formatDate formatName="default.date.format.notime" date="${surveyInfo.endDate}"/>
                    </td>

                    <td class="center aligned">
                        <uiSurvey:finishIcon participant="${Org.get(params.id)}" surveyConfig="${surveyConfig}" surveyOwnerView="${true}"/>
                    </td>
                    <g:if test="${params.tab == 'finish'}">
                        <td class="center aligned">
                            <uiSurvey:finishDate participant="${Org.get(params.id)}" surveyConfig="${surveyConfig}"/>
                        </td>
                    </g:if>
                    <td>
                        <span class="la-popup-tooltip la-delay"
                              data-content="${message(code: 'surveyInfo.toSurveyInfos')}">
                            <g:link controller="survey" action="evaluationParticipant"
                                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participant.id]"
                                    class="ui icon button blue la-modern-button"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                <i aria-hidden="true" class="write icon"></i>
                            </g:link>
                        </span>
                    </td>

                </tr>

            </g:each>
        </table>
    </div>
</div>

<laser:htmlEnd />
