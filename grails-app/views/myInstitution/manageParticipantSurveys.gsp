<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.survey.SurveyOrg; de.laser.survey.SurveyConfig; de.laser.Org;de.laser.RefdataCategory;de.laser.survey.SurveyInfo;de.laser.storage.RDStore; de.laser.OrgRole;de.laser.RefdataValue;de.laser.properties.PropertyDefinition;de.laser.Subscription;de.laser.finance.CostItem;de.laser.survey.SurveyResult" %>
<laser:htmlStart message="manageParticipantSurveys.header" />

<ui:breadcrumbs>
    <ui:crumb message="manageParticipantSurveys.header" class="active"/>
</ui:breadcrumbs>
<ui:controlButtons>
    <ui:actionsDropdown>
            <ui:actionsDropdownItem controller="myInstitution" action="manageParticipantSurveys" params="${params + [reminder: true]}" class="item" message="participantsReminder.button"/>
    </ui:actionsDropdown>
%{--    <ui:exportDropdown>
        <ui:exportDropdownItem>
            <g:link class="item" controller="myInstitution" action="manageParticipantSurveys"
                    params="${params + [exportXLSX: true]}">${message(code: 'survey.exportSurveys')}</g:link>
        </ui:exportDropdownItem>
    </ui:exportDropdown>--}%
</ui:controlButtons>

<ui:h1HeaderWithIcon message="manageParticipantSurveys.header" total="${surveyResultsCount}" floated="true" />

<ui:messages data="${flash}"/>

<g:render template="/survey/participantInfos"/>

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
                <ui:select class="ui dropdown clearable" name="type"
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
                            <label for="checkMandatory">${message(code: 'surveyInfo.isMandatory.filter')}</label>
                            <input id="checkMandatory" name="mandatory" type="checkbox"
                                   <g:if test="${params.mandatory}">checked=""</g:if>
                                   tabindex="0">
                        </div>
                    </div>

                    <div class="inline field">
                        <div class="ui checkbox">
                            <label for="checkNoMandatory">${message(code: 'surveyInfo.isNotMandatory.filter')}</label>
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

                    <div class="inline field">
                        <div class="ui checkbox">
                            <label for="checkPackageSurvey">${message(code: 'surveyconfig.packageSurvey.short')}</label>
                            <input id="checkPackageSurvey" name="checkPackageSurvey" type="checkbox"
                                   <g:if test="${params.checkPackageSurvey}">checked=""</g:if>
                                   tabindex="0">
                        </div>
                    </div>

                    <div class="inline field">
                        <div class="ui checkbox">
                            <label for="checkVendorSurvey">${message(code: 'surveyconfig.vendorSurvey.short')}</label>
                            <input id="checkVendorSurvey" name="checkVendorSurvey" type="checkbox"
                                   <g:if test="${params.checkVendorSurvey}">checked=""</g:if>
                                   tabindex="0">
                        </div>
                    </div>

                    <div class="inline field">
                        <div class="ui checkbox">
                            <label for="checkInvoicingInformation">${message(code: 'surveyconfig.invoicingInformation.short')}</label>
                            <input id="checkInvoicingInformation" name="checkInvoicingInformation" type="checkbox"
                                   <g:if test="${params.checkInvoicingInformation}">checked=""</g:if>
                                   tabindex="0">
                        </div>
                    </div>

                    <div class="inline field">
                        <div class="ui checkbox">
                            <label for="checkSubscriptionSurvey">${message(code: 'surveyconfig.subscriptionSurvey.label')}</label>
                            <input id="checkSubscriptionSurvey" name="checkSubscriptionSurvey" type="checkbox"
                                   <g:if test="${params.checkSubscriptionSurvey}">checked=""</g:if>
                                   tabindex="0">
                        </div>
                    </div>

                </div>
            </div>

        </div>

        <div class="field la-field-right-aligned">

            <div class="field la-field-right-aligned">
                <a href="${request.forwardURI}" class="${Btn.SECONDARY} reset">${message(code: 'default.button.reset.label')}</a>
                <input type="submit" class="${Btn.PRIMARY}" value="${message(code: 'default.button.filter.label')}">
            </div>

        </div>
    </g:form>
</ui:filter>





<div>

        <ui:tabs actionName="${actionName}">
            <ui:tabsItem controller="myInstitution" action="manageParticipantSurveys"
                         params="${[id: params.id, tab: 'open', name: params.name, validOn: params.validOn, validOnYear: params.validOnYear, type: params.type, mandatory: params.mandatory, noMandatory: params.noMandatory, checkSubSurveyUseForTransfer: params.checkSubSurveyUseForTransfer, checkPackageSurvey: params.checkPackageSurvey]}" text="${message(code: "surveys.tabs.open")}" tab="open"
                         counts="${countSurveys?.open}"/>
           %{-- <ui:tabsItem controller="myInstitution" action="manageParticipantSurveys"
                            params="${[id: params.id, tab: 'new', name: params.name, validOn: params.validOn, validOnYear: params.validOnYear, type: params.type, mandatory: params.mandatory, noMandatory: params.noMandatory, checkSubSurveyUseForTransfer: params.checkSubSurveyUseForTransfer, checkPackageSurvey: params.checkPackageSurvey]}" text="${message(code: "surveys.tabs.new")}" tab="new"
                            counts="${countSurveys?.new}"/>
            <ui:tabsItem controller="myInstitution" action="manageParticipantSurveys"
                            params="${[id: params.id, tab: 'processed', name: params.name, validOn: params.validOn, validOnYear: params.validOnYear, type: params.type, mandatory: params.mandatory, noMandatory: params.noMandatory, checkSubSurveyUseForTransfer: params.checkSubSurveyUseForTransfer, checkPackageSurvey: params.checkPackageSurvey]}" text="${message(code: "surveys.tabs.processed")}" tab="processed"
                            counts="${countSurveys?.processed}"/>--}%
            <ui:tabsItem controller="myInstitution" action="manageParticipantSurveys"
                            params="${[id: params.id, tab: 'finish', name: params.name, validOn: params.validOn, validOnYear: params.validOnYear, type: params.type, mandatory: params.mandatory, noMandatory: params.noMandatory, checkSubSurveyUseForTransfer: params.checkSubSurveyUseForTransfer, checkPackageSurvey: params.checkPackageSurvey]}" text="${message(code: "surveys.tabs.finish")}" tab="finish"
                            counts="${countSurveys?.finish}"/>
            <ui:tabsItem controller="myInstitution" action="manageParticipantSurveys" class="ui red"
                            countsClass="red"
                            params="${[id: params.id, tab: 'termination', name: params.name, validOn: params.validOn, validOnYear: params.validOnYear, type: params.type, mandatory: params.mandatory, noMandatory: params.noMandatory, checkSubSurveyUseForTransfer: params.checkSubSurveyUseForTransfer, checkPackageSurvey: params.checkPackageSurvey]}" text="${message(code: "surveys.tabs.termination")}"
                            tab="termination"
                            counts="${countSurveys?.termination}"/>
            <ui:tabsItem controller="myInstitution" action="manageParticipantSurveys" class="ui orange" countsClass="orange"
                            params="${[id: params.id, tab: 'notFinish', name: params.name, validOn: params.validOn, validOnYear: params.validOnYear, type: params.type, mandatory: params.mandatory, noMandatory: params.noMandatory, checkSubSurveyUseForTransfer: params.checkSubSurveyUseForTransfer, checkPackageSurvey: params.checkPackageSurvey]}" text="${message(code: "surveys.tabs.notFinish")}" tab="notFinish"
                            counts="${countSurveys?.notFinish}"/>
        </ui:tabs>

<g:form action="createOwnMail" controller="mail" method="post" class="ui form"
        params="[id: params.id]">

    <g:hiddenField name="objectType" value="${participant.class.name}"/>
    <g:hiddenField name="originalAction" value="${actionName}"/>

    <div class="ui bottom attached tab segment active">
        <table class="ui celled sortable table la-js-responsive-table la-table">
            <thead>
            <tr>
                <g:if test="${editable && params.tab == 'open' && reminder}">
                <th>
                        <g:checkBox name="surveyListToggler" id="surveyListToggler" checked="false"/>
                </th>
                </g:if>
                <th rowspan="2" class="center aligned">
                    ${message(code: 'sidewide.number')}
                </th>
                <g:sortableColumn params="${params}" property="surInfo.name"
                                  title="${message(code: 'surveyInfo.slash.name')}"/>
                <g:sortableColumn params="${params}" property="surInfo.type"
                                  title="${message(code: 'surveyInfo.type.label')}"/>
                <g:sortableColumn params="${params}" property="surInfo.endDate"
                                  title="${message(code: 'default.endDate.label.shy')}"/>
                <th><g:message code="surveyInfo.finished"/></th>
                <g:if test="${params.tab == 'finish'}">
                    <th><g:message code="surveyInfo.finishedDate"/></th>
                </g:if>
                <g:if test="${params.tab == 'open'}">
                    <th>
                        ${message(code: 'surveyOrg.reminderMailDate')}
                    </th>
                </g:if>
                <th class="center aligned">
                    <ui:optionsIcon />
                </th>
            </tr>

            </thead>
            <g:each in="${surveyResults}" var="surveyResult" status="i">

                <g:set var="surveyConfig"
                       value="${SurveyConfig.get(surveyResult.key)}"/>

                <g:set var="surveyInfo"
                       value="${surveyConfig.surveyInfo}"/>

                <tr>
                    <g:if test="${editable && params.tab == 'open' && reminder}">
                        <td>
                            <g:checkBox name="selectedSurveys" value="${surveyInfo.id}" checked="false"/>
                        </td>
                    </g:if>
                    <td class="center aligned">
                        ${(params.int('offset') ?: 0) + i + 1}
                    </td>
                    <td>
                        <div class="la-flexbox">
                            <g:if test="${surveyConfig?.subSurveyUseForTransfer}">
                                <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                                      data-content="${message(code: "surveyconfig.subSurveyUseForTransfer.label.info2")}">
                                    <i class="${Icon.SURVEY} la-list-icon"></i>
                                </span>
                            </g:if>
                            <g:link controller="survey" action="show" id="${surveyInfo.id}" class="ui">
                                ${surveyConfig?.getSurveyName()}
                            </g:link>
                        </div>
                    </td>
                    <td>
                        <div class="ui label survey-${surveyInfo.type.value}">
                            ${surveyInfo.type.getI10n('value')}
                        </div>

                        <g:if test="${surveyInfo.isMandatory}">
                            <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                                  data-content="${message(code: "surveyInfo.isMandatory.label.info2")}">
                                <i class="${Icon.TOOLTIP.IMPORTANT} yellow"></i>
                            </span>
                        </g:if>
                    </td>
                    <td>
                        <g:formatDate formatName="default.date.format.notime" date="${surveyInfo.endDate}"/>
                    </td>

                    <td class="center aligned">
                        <uiSurvey:finishIcon participant="${participant}" surveyConfig="${surveyConfig}" surveyOwnerView="${true}"/>
                    </td>
                    <g:if test="${params.tab == 'finish'}">
                        <td class="center aligned">
                            <uiSurvey:finishDate participant="${participant}" surveyConfig="${surveyConfig}"/>
                        </td>
                    </g:if>
                    <g:if test="${params.tab == 'open'}">
                        <td>
                            <g:set var="surveyOrg" value="${SurveyOrg.findByOrgAndSurveyConfig(participant, surveyConfig)}"/>
                            <ui:xEditable owner="${surveyOrg}" type="date" field="reminderMailDate"/>
                        </td>
                    </g:if>
                    <td>
                        <span class="la-popup-tooltip"
                              data-content="${message(code: 'surveyInfo.toSurveyInfos')}">
                            <g:link controller="survey" action="evaluationParticipant"
                                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participant.id]"
                                    class="${Btn.MODERN.SIMPLE}"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                <i aria-hidden="true" class="${Icon.CMD.EDIT}"></i>
                            </g:link>
                        </span>
                    </td>

                </tr>

            </g:each>
        </table>
        <g:if test="${editable && params.tab == 'open' && reminder}">
            <div class="eight wide field" style="text-align: left;">
                <button name="openOption" type="submit" value="ReminderMail" class="${Btn.SIMPLE}">
                    ${message(code: 'openParticipantsAgain.reminder')}
                </button>
            </div>
        </g:if>
    </div>
</g:form>
</div>

<laser:script file="${this.getGroovyPageFileName()}">
    $('#surveyListToggler').click(function () {
        if ($(this).prop('checked')) {
            $("tr[class!=disabled] input[name=selectedSurveys]").prop('checked', true)
        } else {
            $("tr[class!=disabled] input[name=selectedSurveys]").prop('checked', false)
        }
    })
</laser:script>

<laser:htmlEnd />
