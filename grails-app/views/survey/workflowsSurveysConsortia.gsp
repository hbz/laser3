<%@ page import="de.laser.helper.Icons; de.laser.helper.Params; de.laser.survey.SurveyOrg; de.laser.RefdataCategory; de.laser.survey.SurveyResult; de.laser.storage.RDStore; de.laser.OrgRole;de.laser.RefdataValue;de.laser.properties.PropertyDefinition;de.laser.Subscription;de.laser.finance.CostItem" %>
<laser:htmlStart message="currentSurveys.label" serviceInjection="true" />

<ui:breadcrumbs>
    <ui:crumb message="currentSurveys.label" class="active"/>
</ui:breadcrumbs>

<ui:controlButtons>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon message="currentSurveys.label" type="Survey" total="${surveysCount}" floated="true" />

<ui:messages data="${flash}"/>

<ui:filter>
    <g:form action="workflowsSurveysConsortia" controller="survey" method="post" class="ui small form"
            params="[tab: params.tab]">
        <div class="four fields">
            <div class="field">
                <label for="name">${message(code: 'surveyInfo.name.label')}
                </label>

                <div class="ui input">
                    <input type="text" id="name" name="name"
                           placeholder="${message(code: 'default.search.ph')}"
                           value="${params.name}"/>
                </div>
            </div>


            <div class="field">
                <ui:datepicker label="surveyInfo.startDate.label" id="startDate" name="startDate"
                                  placeholder="filter.placeholder" value="${params.startDate}"/>
            </div>


            <div class="field">
                <ui:datepicker label="surveyInfo.endDate.label" id="endDate" name="endDate"
                                  placeholder="filter.placeholder" value="${params.endDate}"/>
            </div>

            <div class="field">
                <label for="validOnYear">${message(code: 'default.valid_onYear.label')}</label>

                <select id="validOnYear" name="validOnYear" multiple="" class="ui search selection fluid dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>
                    <option value="all" <%=("all" in params.list('validOnYear')) ? 'selected="selected"' : ''%>>
                        ${message(code: 'default.select.all.label')}
                    </option>

                    <g:each in="${surveyYears}" var="surveyYear">
                        <option <%=(params.list('validOnYear').contains(surveyYear.toString())) ? 'selected="selected"' : ''%>
                                value="${surveyYear}" title="${surveyYear}">
                            ${surveyYear}
                        </option>
                    </g:each>
                </select>

            </div>

        </div>

        <div class="three fields">
            <div class="field">
                <label for="filterPvd">${message(code: 'menu.my.providers')}</label>
                <select id="filterPvd" name="filterPvd" multiple="" class="ui search selection fluid dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${providers.sort { it.name }}" var="provider">
                        <option <%=Params.getLongList(params, 'filterPvd').contains(provider.id) ? 'selected="selected"' : ''%>
                        value="${provider.id}">
                        ${provider.name}
                        </option>
                    </g:each>
                </select>

            </div>

            <div class="field">
                <label for="filterSub">${message(code: 'menu.my.subscriptions')}</label>
                <select id="filterSub" name="filterSub" multiple="" class="ui search selection fluid dropdown">
                    <option value="">${message(code: 'default.select.choose.label')}</option>

                    <g:each in="${subscriptions.sort { it }}" var="sub">
                        <option <%=(params.list('filterSub').contains(sub)) ? 'selected="selected"' : ''%>
                        value="${sub}">
                        ${sub}
                        </option>
                    </g:each>
                </select>

            </div>

            <laser:render template="/templates/properties/genericFilter" model="[propList: propList, hideFilterProp: true, label:message(code: 'subscription.property.search')]"/>

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

                    <div class="inline field">
                        <div class="ui checkbox">
                            <label for="checkPackageSurvey">${message(code: 'surveyconfig.packageSurvey.label')}</label>
                            <input id="checkPackageSurvey" name="checkPackageSurvey" type="checkbox"
                                   <g:if test="${params.checkPackageSurvey}">checked=""</g:if>
                                   tabindex="0">
                        </div>
                    </div>

                    <div class="inline field">
                        <div class="ui checkbox">
                            <label for="checkVendorSurvey">${message(code: 'surveyconfig.vendorSurvey.label')}</label>
                            <input id="checkVendorSurvey" name="checkVendorSurvey" type="checkbox"
                                   <g:if test="${params.checkVendorSurvey}">checked=""</g:if>
                                   tabindex="0">
                        </div>
                    </div>

                    <div class="inline field">
                        <div class="ui checkbox">
                            <label for="checkInvoicingInformation">${message(code: 'surveyconfig.invoicingInformation.label')}</label>
                            <input id="checkInvoicingInformation" name="checkInvoicingInformation" type="checkbox"
                                   <g:if test="${params.checkInvoicingInformation}">checked=""</g:if>
                                   tabindex="0">
                        </div>
                    </div>

                </div>
            </div>

        </div>

        <div class="field la-field-right-aligned">

            <div class="field la-field-right-aligned">
                <g:link controller="survey" action="workflowsSurveysConsortia" params="[tab: params.tab]"
                        class="ui reset secondary button">${message(code: 'default.button.reset.label')}</g:link>
                <input type="submit" class="ui primary button"
                       value="${message(code: 'default.button.filter.label')}">
            </div>

        </div>
    </g:form>
</ui:filter>


<%
    def tmpParams = params.clone()
    tmpParams.remove("tab")
%>

<ui:tabs actionName="${actionName}">
    <ui:tabsItem controller="survey" action="workflowsSurveysConsortia"
                    params="${tmpParams + [tab: 'created']}" text="Erstellt" tab="created"
                    counts="${countSurveyConfigs.created}"/>
    <ui:tabsItem controller="survey" action="workflowsSurveysConsortia"
                    params="${tmpParams + [tab: 'active']}" text="Aktiv" tab="active"
                    counts="${countSurveyConfigs.active}"/>
    <ui:tabsItem controller="survey" action="workflowsSurveysConsortia"
                    params="${tmpParams + [tab: 'finish']}" text="Beendet" tab="finish"
                    counts="${countSurveyConfigs.finish}"/>
    <ui:tabsItem controller="survey" action="workflowsSurveysConsortia"
                    params="${tmpParams + [tab: 'inEvaluation']}" text="In Auswertung" tab="inEvaluation"
                    counts="${countSurveyConfigs.inEvaluation}"/>

    <ui:tabsItem controller="survey" action="workflowsSurveysConsortia"
                    params="${tmpParams + [tab: 'completed']}" text="Abgeschlossen" tab="completed"
                    counts="${countSurveyConfigs.completed}"/>
</ui:tabs>

<div class="ui bottom attached tab segment active">

    <g:if test="${surveys}">

        <table class="ui celled sortable table la-js-responsive-table la-table">
            <thead>
            <tr>

                <th rowspan="2" class="center aligned">
                    ${message(code: 'sidewide.number')}
                </th>

                <g:sortableColumn params="${params}" property="surInfo.name"
                                  title="${message(code: 'surveyInfo.slash.name')}" rowspan="2" scope="col"/>
                <th rowspan="2" scope="col">${message(code: 'surveyInfo.type.label')}</th>
                <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}"
                                  property="surInfo.startDate"
                                  title="${message(code: 'default.startDate.label.shy')}"/>
                <th rowspan="2" scope="col">${message(code: 'default.status.label')}</th>
                <th rowspan="2" scope="col">${message(code: 'surveyProperty.plural.label')}</th>


                <th rowspan="2" scope="col">
                    <a href="#" class="la-popup-tooltip la-delay" data-content="${message(code: 'surveyConfigDocs.label')}" data-position="top center">
                        <i class="${Icons.DOCUMENT} large"></i>
                    </a>
                </th>

                <th scope="col" rowspan="2">
                    <a href="#" class="la-popup-tooltip la-delay" data-content="${message(code: 'surveyParticipants.label')}" data-position="top center">
                        <i class="users large icon"></i>
                    </a>
                </th>

                <th scope="col" rowspan="2">
                    <a href="#" class="la-popup-tooltip la-delay" data-content="${message(code: 'surveyCostItems.label')}" data-position="top center">
                        <i class="money bill large icon"></i>
                    </a>
                </th>

                <th rowspan="2" scope="col">${message(code: 'surveyInfo.finished')}</th>
                <th rowspan="2" scope="col">${message(code:'default.actions.label')}</th>

            </tr>
            <tr>
                <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}"
                                  property="surInfo.endDate"
                                  title="${message(code: 'default.endDate.label.shy')}"/>
            </tr>
            </thead>
            <g:each in="${surveys}" var="survey" status="i">

                <g:set var="surveyInfo"
                       value="${survey[0]}"/>

                <g:set var="surveyConfig"
                       value="${survey[1]}"/>


                <g:set var="participantsFinish"
                       value="${SurveyOrg.findAllBySurveyConfigAndFinishDateIsNotNull(surveyConfig)}"/>

                <g:set var="participantsTotal"
                       value="${surveyConfig.orgs ?: []}"/>

                <tr>
                    <td class="center aligned">
                        ${(params.int('offset') ?: 0) + i + 1}
                    </td>

                    <td>
                        <div class="la-flexbox">
                            <g:link controller="survey" action="show" id="${surveyInfo.id}" class="ui ">
                                ${surveyConfig.getSurveyName()}
                            </g:link>
                        </div>
                    </td>

                    <td class="center aligned">
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
                        <g:formatDate formatName="default.date.format.notime"
                                      date="${surveyInfo.startDate}"/>
                        <br />
                        <span class="la-secondHeaderRow" data-label="${message(code: 'default.endDate.label.shy')}:">
                            <g:formatDate formatName="default.date.format.notime"
                                          date="${surveyInfo.endDate}"/>
                        </span>

                    </td>

                    <td class="center aligned">
                        ${surveyInfo.status.getI10n('value')}
                    </td>

                    <td class="center aligned">

                        <g:link controller="survey" action="show" id="${surveyInfo.id}"
                                params="[surveyConfigID: surveyConfig.id]" class="ui icon">
                            <div class="ui circular ${surveyConfig.configFinish ? "green" : ""} label">
                                %{--Titel-Umfrage kann keine Umfrage-Merkmale haben--}%
                                ${surveyConfig.surveyProperties?.size() ?: 0}
                            </div>
                        </g:link>

                    </td>

                    <td class="center aligned">

                        <g:link controller="survey" action="surveyConfigDocs" id="${surveyInfo.id}"
                                params="[surveyConfigID: surveyConfig.id]" class="ui icon">
                            <div class="ui blue circular label">
                                ${surveyConfig.getCurrentDocs().size() ?: 0}
                            </div>
                        </g:link>

                    </td>


                    <td class="center aligned">
                        <g:link controller="survey" action="surveyParticipants" id="${surveyInfo.id}"
                                params="[surveyConfigID: surveyConfig.id]" class="ui icon">
                            <div class="ui circular ${participantsFinish.size() == participantsTotal.size() ? "green" : surveyConfig.configFinish ? "yellow" : ""} label">
                                ${participantsFinish.size() ?: 0} / ${surveyConfig.orgs?.size() ?: 0}
                            </div>
                        </g:link>
                    </td>


                    <td class="center aligned">
                        <g:if test="${surveyInfo.type.id != RDStore.SURVEY_TYPE_TITLE_SELECTION.id}">
                            <g:link controller="survey" action="surveyCostItems" id="${surveyInfo.id}"
                                    params="[surveyConfigID: surveyConfig.id]" class="ui icon">
                                <div class="ui circular ${surveyConfig.costItemsFinish ? "green" : ""} label">
                                    ${surveyConfig.getSurveyConfigCostItems().size() ?: 0}
                                </div>
                            </g:link>
                        </g:if>
                    </td>

                    <td class="center aligned">
                            <g:link controller="survey" action="surveyEvaluation" id="${surveyInfo.id}"
                                    params="[surveyConfigID: surveyConfig.id]"
                                    class="ui icon">
                                <div class="ui circular ${(participantsFinish.size() == participantsTotal.size()) ? "green" : (participantsFinish.size() > 0) ? "yellow" : ""} label">
                                    <g:if
                                            test="${participantsFinish && participantsTotal}">
                                        <g:formatNumber
                                                number="${(participantsFinish.size() / participantsTotal.size()) * 100}"
                                                minFractionDigits="2"
                                                maxFractionDigits="2"/>%
                                    </g:if>
                                    <g:else>
                                        0%
                                    </g:else>
                                </div>
                            </g:link>
                    </td>
                    <td>
                        <g:link controller="survey" action="show" id="${surveyInfo.id}"
                                class="ui button blue icon la-modern-button"
                                role="button"
                                aria-label="${message(code: 'ariaLabel.change.universal')}">
                            <i class="pencil icon"></i>
                        </g:link>

                        <g:if test="${editable}">
                            <g:link controller="survey" action="copySurvey" id="${surveyInfo.id}"
                                    params="[surveyConfigID: surveyConfig.id, q: surveyConfig?.subscription?.providers ? surveyConfig.subscription.providers[0].name : '']"
                                    class="ui icon blue button la-modern-button">
                                <i class="icon copy"></i>
                                </div>
                            </g:link>
                        </g:if>
                    </td>

                </tr>

            </g:each>
        </table>

    </g:if>
    <g:else>
        <g:if test="${filterSet}">
            <br /><strong><g:message code="filter.result.empty.object"
                                   args="${[message(code: "survey.plural")]}"/></strong>
        </g:if>
        <g:else>
            <br /><strong><g:message code="result.empty.object" args="${[message(code: "survey.plural")]}"/></strong>
        </g:else>
    </g:else>
</div>


<g:if test="${surveysCount}">
    <ui:paginate action="${actionName}" controller="${controllerName}" params="${params}"
                    next="${message(code: 'default.paginate.next')}"
                    prev="${message(code: 'default.paginate.prev')}" max="${max}"
                    total="${surveysCount}"/>
</g:if>

<laser:htmlEnd />
