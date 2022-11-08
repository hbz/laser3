<%@ page import="de.laser.survey.SurveyOrg; de.laser.finance.CostItem; de.laser.Person; de.laser.storage.RDStore; de.laser.interfaces.CalculatedType; de.laser.survey.SurveyResult" %>
<laser:htmlStart message="subscription.details.surveys.label" serviceInjection="true"/>

<laser:render template="breadcrumb" model="${[params: params]}"/>

<ui:controlButtons>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon>
<ui:xEditable owner="${subscription}" field="name"/>
<ui:totalNumber total="${surveys.size()}"/>
</ui:h1HeaderWithIcon>
<ui:anualRings object="${subscription}" controller="subscription" action="surveysConsortia"
                  navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>


<laser:render template="nav"/>


<ui:messages data="${flash}"/>

<g:if test="${surveys}">
    <table class="ui celled sortable table la-js-responsive-table la-table">
        <thead>
        <tr>

            <th rowspan="2" class="center aligned">
                ${message(code: 'sidewide.number')}
            </th>

            <g:sortableColumn params="${params}" property="surveyInfo.name"
                              title="${message(code: 'surveyInfo.slash.name')}" rowspan="2" scope="col"/>

            <th>${message(code: 'default.status.label')}</th>

            <g:sortableColumn params="${params}" property="surveyInfo.startDate"
                              title="${message(code: 'default.startDate.label')}"/>
            <g:sortableColumn params="${params}" property="surveyInfo.endDate"
                              title="${message(code: 'default.endDate.label')}"/>
            <th>${message(code: 'surveyProperty.plural.label')}</th>
            <th>
                <a href="#" class="la-popup-tooltip la-delay" data-content="${message(code: 'surveyConfigDocs.label')}" data-position="top center">
                <i class="file alternate large icon"></i>
                </a>
            </th>
            <th>
                <a href="#" class="la-popup-tooltip la-delay" data-content="${message(code: 'surveyParticipants.label')}" data-position="top center">
                    <i class="users large icon"></i>
                </a>
            </th>

            <th>
                <a href="#" class="la-popup-tooltip la-delay" data-content="${message(code: 'surveyCostItems.label')}" data-position="top center">
                    <i class="money bill large icon"></i>
                </a>
            </th>
            <th>${message(code: 'surveyInfo.finished')}</th>
            <th class="la-action-info">${message(code: 'default.actions.label')}</th>

        </tr>

        </thead>
        <g:each in="${surveys}" var="surveyConfig" status="i">

            <g:set var="surveyInfo"
                   value="${surveyConfig.surveyInfo}"/>


            <g:set var="participantsFinish"
                   value="${SurveyOrg.findAllBySurveyConfigAndFinishDateIsNotNull(surveyConfig)}"/>

            <g:set var="participantsTotal"
                   value="${SurveyOrg.findAllBySurveyConfig(surveyConfig)}"/>

            <tr>
                <td class="center aligned">
                    ${(params.int('offset') ?: 0) + i + 1}
                </td>
                <td>
                    <div class="la-flexbox">
                        <g:if test="${surveyConfig.subSurveyUseForTransfer}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                  data-content="${message(code: "surveyconfig.subSurveyUseForTransfer.label.info2")}">
                                <i class="icon pie chart la-list-icon"></i>
                            </span>
                        </g:if>

                        <g:link controller="survey" action="show" id="${surveyInfo.id}" class="ui ">
                            ${surveyConfig.getConfigNameShort()}
                        </g:link>
                    </div>
                </td>
                <td>
                    ${surveyInfo.status.getI10n('value')}
                </td>
                <td>
                    <g:formatDate formatName="default.date.format.notime"
                                  date="${surveyInfo.startDate}"/>

                </td>
                <td>

                    <g:formatDate formatName="default.date.format.notime"
                                  date="${surveyInfo.endDate}"/>
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
                                ${participantsFinish.size() ?: 0} / ${surveyConfig.orgs?.org?.flatten()?.unique { a, b -> a.id <=> b.id }?.size() ?: 0}
                            </div>
                        </g:link>
                </td>


                <td class="center aligned">
                    <g:if test="${surveyInfo.type.id in [RDStore.SURVEY_TYPE_RENEWAL.id, RDStore.SURVEY_TYPE_SUBSCRIPTION.id]}">
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
                    <span class="la-popup-tooltip la-delay"
                          data-content="${message(code: 'surveyInfo.toSurveyInfos')}">
                        <g:link controller="survey" action="show" id="${surveyInfo.id}"
                                params="[surveyConfigID: surveyConfig.id]"
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
</g:if>
<g:else>

    <ui:greySegment>
        <h3 class="ui header">
            <g:message code="survey.notExist.plural"/>
        </h3>
    </ui:greySegment>
</g:else>
<laser:htmlEnd />

