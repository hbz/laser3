<%@ page import="com.k_int.kbplus.CostItem; com.k_int.kbplus.Person; de.laser.helper.RDStore; de.laser.interfaces.TemplateSupport" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser')} : ${message(code: 'subscription.details.surveys.label')}</title>
</head>

<body>

<g:render template="breadcrumb" model="${[params: params]}"/>

<semui:controlButtons>
    <g:render template="actions"/>
</semui:controlButtons>

<h1 class="ui icon header la-noMargin-top"><semui:headerIcon/>
<semui:xEditable owner="${subscriptionInstance}" field="name"/>
<semui:totalNumber total="${surveys.size() ?: 0}"/>
</h1>
<semui:anualRings object="${subscriptionInstance}" controller="subscription" action="surveysConsortia"
                  navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>


<g:render template="nav"/>


<semui:messages data="${flash}"/>

<g:if test="${surveys}">
    <table class="ui celled sortable table la-table">
        <thead>
        <tr>

            <th rowspan="2" class="center aligned">
                ${message(code: 'sidewide.number')}
            </th>

            <g:sortableColumn params="${params}" property="subscription.name"
                              title="${message(code: 'surveyInfo.slash.name')}" rowspan="2" scope="col"/>

            <th>${message(code: 'default.status.label')}</th>

            <g:sortableColumn params="${params}" property="surveyInfo.startDate"
                              title="${message(code: 'default.startDate.label')}"/>
            <g:sortableColumn params="${params}" property="surveyInfo.endDate"
                              title="${message(code: 'default.endDate.label')}"/>
            <th>${message(code: 'surveyProperty.plural.label')}</th>
            <th>${message(code: 'surveyConfigDocs.label')}</th>
            <th>${message(code: 'surveyParticipants.label')}</th>
            <th>${message(code: 'surveyCostItems.label')}</th>
            <th>${message(code: 'surveyInfo.finished')}</th>
            <th class="la-action-info">${message(code: 'default.actions.label')}</th>

        </tr>

        </thead>
        <g:each in="${surveys}" var="surveyConfig" status="i">

            <g:set var="surveyInfo"
                   value="${surveyConfig?.surveyInfo}"/>


            <g:set var="participantsFinish"
                   value="${surveyConfig.pickAndChoose ? com.k_int.kbplus.SurveyOrg.findAllBySurveyConfigAndFinishDateIsNotNull(surveyConfig) : com.k_int.kbplus.SurveyResult.findAllBySurveyConfigAndFinishDateIsNotNull(surveyConfig)?.participant?.flatten()?.unique { a, b -> a.id <=> b.id }}"/>

            <g:set var="participantsTotal"
                   value="${surveyConfig.pickAndChoose ?  com.k_int.kbplus.SurveyOrg.findAllBySurveyConfig(surveyConfig) : com.k_int.kbplus.SurveyResult.findAllBySurveyConfig(surveyConfig)?.participant?.flatten()?.unique { a, b -> a.id <=> b.id }}"/>

            <tr>
                <td class="center aligned">
                    ${(params.int('offset') ?: 0) + i + 1}
                </td>
                <td><g:if test="${editable}">
                    <g:if test="${surveyConfig?.type == 'Subscription'}">
                        <i class="icon clipboard outline la-list-icon"></i>
                        <g:link controller="survey" action="surveyConfigsInfo" id="${surveyInfo?.id}"
                                params="[surveyConfigID: surveyConfig?.id]" class="ui ">
                            ${surveyConfig?.subscription?.name}
                        </g:link>
                    </g:if>
                    <g:else>
                        <g:link controller="survey" action="show" id="${surveyInfo?.id}" class="ui ">
                            ${surveyConfig?.getConfigNameShort()}
                        </g:link>
                    </g:else>
                </g:if>
                    <g:else>
                        <g:if test="${surveyConfig?.type == 'Subscription'}">
                            <i class="icon clipboard outline la-list-icon"></i>
                            ${surveyConfig?.subscription?.name}
                        </g:if>
                        <g:else>
                            <i class="icon chart pie la-list-icon"></i>
                            ${surveyConfig?.getConfigNameShort()}
                        </g:else>
                    </g:else>
                    <div class="la-flexbox">

                        <g:if test="${surveyConfig?.subSurveyUseForTransfer}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                  data-content="${message(code: "surveyConfig.subSurveyUseForTransfer.label.info2")}">
                                <i class="yellow icon envelope large "></i>
                            </span>
                        </g:if>
                        <i class="icon chart pie la-list-icon"></i>
                        <g:link controller="survey" action="show" id="${surveyInfo?.id}" class="ui ">
                            ${surveyInfo?.name}
                        </g:link>

                    </div>
                </td>
                <td>
                    ${surveyInfo?.status.getI10n('value')}
                </td>
                <td>
                    <g:formatDate formatName="default.date.format.notime"
                                  date="${surveyInfo?.startDate}"/>

                </td>
                <td>

                    <g:formatDate formatName="default.date.format.notime"
                                  date="${surveyInfo?.endDate}"/>
                </td>

                <td class="center aligned">

                    <g:if test="${surveyConfig && !surveyConfig.pickAndChoose}">
                        <g:if test="${surveyConfig?.type == 'Subscription'}">
                            <g:link controller="survey" action="surveyConfigsInfo" id="${surveyInfo?.id}"
                                    params="[surveyConfigID: surveyConfig?.id]" class="ui icon">
                                <div class="ui circular ${surveyConfig?.configFinish ? "green" : ""} label">
                                    ${surveyConfig?.surveyProperties?.size() ?: 0}
                                </div>
                            </g:link>
                        </g:if>
                    </g:if>

                </td>
                <td class="center aligned">
                    <g:if test="${surveyConfig}">
                        <g:link controller="survey" action="surveyConfigDocs" id="${surveyInfo?.id}"
                                params="[surveyConfigID: surveyConfig?.id]" class="ui icon">
                            <div class="ui circular label">
                                ${surveyConfig?.getCurrentDocs()?.size() ?: 0}
                            </div>
                        </g:link>
                    </g:if>
                </td>

                <td class="center aligned">
                    <g:if test="${surveyConfig}">
                        <g:link controller="survey" action="surveyParticipants" id="${surveyInfo?.id}"
                                params="[surveyConfigID: surveyConfig?.id]" class="ui icon">
                            <div class="ui circular ${participantsFinish?.size() == participantsTotal?.size() ? "green" : surveyConfig?.configFinish ? "yellow" : ""} label">
                                ${participantsFinish?.size() ?: 0} / ${surveyConfig?.orgs?.org?.flatten()?.unique { a, b -> a.id <=> b.id }?.size() ?: 0}
                            </div>
                        </g:link>
                    </g:if>
                </td>


                <td class="center aligned">
                    <g:if test="${surveyConfig && !surveyConfig.pickAndChoose}">
                        <g:link controller="survey" action="surveyCostItems" id="${surveyInfo?.id}"
                                params="[surveyConfigID: surveyConfig?.id]" class="ui icon">
                            <div class="ui circular ${surveyConfig?.costItemsFinish ? "green" : ""} label">
                                ${surveyConfig?.getSurveyConfigCostItems()?.size() ?: 0}
                            </div>
                        </g:link>
                    </g:if>
                </td>

                <td class="center aligned">
                    <g:if test="${surveyConfig && !surveyConfig.pickAndChoose}">
                        <g:link controller="survey" action="evaluationConfigsInfo" id="${surveyInfo?.id}"
                                params="[surveyConfigID: surveyConfig?.id]"
                                class="ui icon">
                            <div class="ui circular ${(participantsFinish?.size() == participantsTotal?.size()) ? "green" : (participantsFinish?.size() > 0) ? "yellow" : ""} label">
                                <g:if
                                        test="${participantsFinish && participantsTotal}">
                                    <g:formatNumber
                                            number="${(participantsFinish?.size() / participantsTotal?.size()) * 100}"
                                            minFractionDigits="2"
                                            maxFractionDigits="2"/>%
                                </g:if>
                                <g:else>
                                    0%
                                </g:else>
                            </div>
                        </g:link>
                    </g:if>

                    <g:if test="${surveyConfig && surveyConfig.pickAndChoose}">
                        <g:link controller="survey" action="surveyTitlesEvaluation" id="${surveyInfo?.id}"
                                params="[surveyConfigID: surveyConfig?.id]"
                                class="ui icon">
                            <div class="ui circular ${(participantsFinish?.size() == participantsTotal?.size()) ? "green" : (participantsFinish?.size() > 0) ? "yellow" : ""} label">
                                <g:if
                                        test="${participantsFinish && participantsTotal}">
                                    <g:formatNumber
                                            number="${(participantsFinish?.size() / participantsTotal?.size()) * 100}"
                                            minFractionDigits="2"
                                            maxFractionDigits="2"/>%
                                </g:if>
                                <g:else>
                                    0%
                                </g:else>
                            </div>
                        </g:link>
                    </g:if>

                </td>
                <td>
                    <g:if test="${surveyConfig && !surveyConfig.pickAndChoose}">
                        <span class="la-popup-tooltip la-delay"
                              data-content="${message(code: 'surveyInfo.toSurveyInfos')}">
                            <g:link controller="survey" action="evaluationConfigsInfo" id="${surveyInfo?.id}"
                                    params="[surveyConfigID: surveyConfig?.id]"
                                    class="ui button icon">
                                <i class="write icon"></i>
                            </g:link>
                        </span>
                    </g:if>
                    <g:if test="${surveyConfig && surveyConfig.pickAndChoose}">
                        <span class="la-popup-tooltip la-delay"
                              data-content="${message(code: 'surveyInfo.toSurveyInfos')}">
                            <g:link controller="survey" action="show" id="${surveyInfo?.id}"
                                    params="[surveyConfigID: surveyConfig?.id]"
                                    class="ui button icon">
                                <i class="write icon"></i>
                            </g:link>
                        </span>
                    </g:if>
                </td>
            </tr>

        </g:each>
    </table>
</g:if>
<g:else>

    <semui:form>
        <h3>
            <g:message code="survey.notExist.plural"/>
        </h3>
    </semui:form>
</g:else>
</body>
</html>

