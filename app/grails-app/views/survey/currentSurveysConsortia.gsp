<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.OrgRole;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.properties.PropertyDefinition;com.k_int.kbplus.Subscription;com.k_int.kbplus.CostItem" %>
<laser:serviceInjection/>
<!doctype html>



<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'currentSurveys.label', default: 'Current Surveys')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}"/>
    <semui:crumb message="currentSurveys.label" class="active"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <g:render template="actions"/>
</semui:controlButtons>



<h1 class="ui left aligned icon header"><semui:headerIcon/>${message(code: 'currentSurveys.label', default: 'Current Surveys')}
<semui:totalNumber total="${countSurveyConfigs.values().sum { it }}"/>
</h1>

<semui:messages data="${flash}"/>

<semui:filter>
    <g:form action="currentSurveysConsortia" controller="survey" method="post" class="ui small form" params="[tab: params.tab]">
        <div class="three fields">
            <div class="field">
                <label for="name">${message(code: 'surveyInfo.name.label')}
                </label>

                <div class="ui input">
                    <input type="text" id="name" name="name"
                           placeholder="${message(code: 'default.search.ph', default: 'enter search term...')}"
                           value="${params.name}"/>
                </div>
            </div>


            <div class="field fieldcontain">
                <semui:datepicker label="surveyInfo.startDate.label" id="startDate" name="startDate"
                                  placeholder="filter.placeholder" value="${params.startDate}"/>
            </div>


            <div class="field fieldcontain">
                <semui:datepicker label="surveyInfo.endDate.label" id="endDate" name="endDate"
                                  placeholder="filter.placeholder" value="${params.endDate}"/>
            </div>

        </div>

        <div class="four fields">

            <div class="field">
                <label>${message(code: 'surveyInfo.type.label')}</label>
                <laser:select class="ui dropdown" name="type"
                              from="${RefdataCategory.getAllRefdataValues('Survey Type')}"
                              optionKey="id"
                              optionValue="value"
                              value="${params.type}"
                              noSelection="${['': message(code: 'default.select.choose.label')]}"/>
            </div>

        </div>

        <div class="field la-field-right-aligned">

            <div class="field la-field-right-aligned">
                <a href="${request.forwardURI}"
                   class="ui reset primary button">${message(code: 'default.button.reset.label')}</a>
                <input type="submit" class="ui secondary button"
                       value="${message(code: 'default.button.filter.label', default: 'Filter')}">
            </div>

        </div>
    </g:form>
</semui:filter>

<semui:form>

    <semui:tabs actionName="${actionName}">
        <semui:tabsItem controller="survey" action="currentSurveysConsortia"
                        params="${[id: params.id, tab: 'created']}" text="Erstellt" tab="created"
                        counts="${countSurveyConfigs?.created}"/>
        <semui:tabsItem controller="survey" action="currentSurveysConsortia"
                        params="${[id: params.id, tab: 'active']}" text="Aktiv" tab="active"
                        counts="${countSurveyConfigs?.active}"/>
        <semui:tabsItem controller="survey" action="currentSurveysConsortia"
                        params="${[id: params.id, tab: 'finish']}" text="Beendet" tab="finish"
                        counts="${countSurveyConfigs?.finish}"/>
        <semui:tabsItem controller="survey" action="currentSurveysConsortia"
                        params="${[id: params.id, tab: 'inEvaluation']}" text="In Auswertung" tab="inEvaluation"
                        counts="${countSurveyConfigs?.inEvaluation}"/>
        <semui:tabsItem controller="survey" action="currentSurveysConsortia"
                        params="${[id: params.id, tab: 'completed']}" text="Abgeschlossen" tab="completed"
                        counts="${countSurveyConfigs?.completed}"/>
    </semui:tabs>

    <div class="ui bottom attached tab segment active">

        <table class="ui celled sortable table la-table">
            <thead>
            <tr>

                <th rowspan="2" class="center aligned">
                    ${message(code: 'sidewide.number')}
                </th>

                <g:sortableColumn params="${params}" property="surInfo.name"
                                  title="${message(code: 'surveyInfo.slash.name')}" rowspan="2" scope="col"/>
                <th rowspan="2" scope="col">${message(code: 'surveyInfo.type.label')}</th>
                <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}" property="surInfo.startDate"
                                  title="${message(code: 'default.startDate.label', default: 'Start Date')}"/>
                <th rowspan="2" scope="col">${message(code: 'surveyProperty.plural.label')}</th>

                <g:if test="${params.tab in ["created", "active"]}">
                    <th rowspan="2" scope="col">${message(code: 'surveyConfigDocs.label')}</th>
                </g:if>
                <th rowspan="2" scope="col">${message(code: 'surveyParticipants.label')}</th>

                <g:if test="${params.tab in ["created", "active"]}">
                <th rowspan="2" scope="col">${message(code: 'surveyCostItems.label')}</th>
                </g:if>

                <g:if test="${params.tab != "created"}">
                <th rowspan="2" scope="col">${message(code: 'surveyInfo.finished')}</th>
                </g:if>

                <g:if test="${params.tab == "finish"}">
                    <th rowspan="2" scope="col">${message(code: 'surveyInfo.evaluation.action')}</th>
                </g:if>

                <g:if test="${params.tab == "inEvaluation"}">
                    <th rowspan="2" scope="col">${message(code: 'surveyInfo.renewal.action')}</th>
                </g:if>

            </tr>
            <tr>
                <g:sortableColumn scope="col" class="la-smaller-table-head" params="${params}" property="surInfo.endDate"
                                  title="${message(code: 'default.endDate.label', default: 'End Date')}"/>
            </tr>
            </thead>
            <g:each in="${surveys}" var="survey" status="i">

                <g:set var="surveyInfo"
                       value="${survey[0]}"/>

                <g:set var="surveyConfig"
                       value="${survey[1]}"/>


                <g:set var="participantsFinish"
                       value="${com.k_int.kbplus.SurveyResult.findAllBySurveyConfigAndFinishDateIsNotNull(surveyConfig)}"/>

                <g:set var="participantsTotal"
                       value="${com.k_int.kbplus.SurveyResult.findAllBySurveyConfig(surveyConfig)}"/>

                <tr>
                    <td class="center aligned">
                        ${(params.int('offset') ?: 0) + i + 1}
                    </td>

                    <td>%{--<g:if test="${editable}">
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
                        </g:else>--}%
                        <div class="la-flexbox">
                            <g:if test="${surveyConfig?.isSubscriptionSurveyFix}">
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                      data-content="${message(code: "surveyConfig.isSubscriptionSurveyFix.label.info2")}">
                                    <i class="yellow icon envelope large "></i>
                                </span>
                            </g:if>
                            <i class="icon chart pie la-list-icon"></i>
                            <g:link controller="survey" action="show" id="${surveyInfo?.id}" class="ui ">
                                ${surveyConfig?.getSurveyName()}
                            </g:link>
                        </div>
                    </td>

                    <td class="center aligned">
                        ${surveyInfo.type.getI10n('value')} (${surveyInfo.isSubscriptionSurvey ? message(code: 'subscriptionSurvey.label') : message(code: 'generalSurvey.label')})
                    </td>
                    <td>
                        <g:formatDate formatName="default.date.format.notime"
                                      date="${surveyInfo?.startDate}"/>
                        <br>
                        <g:formatDate formatName="default.date.format.notime"
                                      date="${surveyInfo?.endDate}"/>
                    </td>

                    <td class="center aligned">

                        <g:if test="${surveyConfig}">
                            <g:if test="${surveyConfig?.type == 'Subscription' && !surveyConfig?.pickAndChoose}">
                                <g:link controller="survey" action="surveyConfigsInfo" id="${surveyInfo?.id}"
                                        params="[surveyConfigID: surveyConfig?.id]" class="ui icon">
                                    <div class="ui circular ${surveyConfig?.configFinish ? "green" : ""} label">
                                        ${surveyConfig?.surveyProperties?.size() ?: 0}
                                    </div>
                                </g:link>
                            </g:if>
                        </g:if>

                    </td>
                    <g:if test="${params.tab in ["created", "active"]}">
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
                    </g:if>

                    <td class="center aligned">
                        <g:if test="${surveyConfig}">
                            <g:link controller="survey" action="surveyParticipants" id="${surveyInfo?.id}"
                                    params="[surveyConfigID: surveyConfig?.id]" class="ui icon">
                                <div class="ui circular ${participantsFinish?.size() == participantsTotal?.size() ? "green" : surveyConfig?.configFinish ? "yellow" : ""} label">
                                    ${participantsFinish?.participant?.flatten()?.unique { a, b -> a.id <=> b.id }?.size() ?: 0} / ${surveyConfig?.orgs?.org?.flatten()?.unique { a, b -> a.id <=> b.id }?.size() ?: 0}
                                </div>
                            </g:link>
                        </g:if>
                    </td>

                    <g:if test="${params.tab in ["created", "active"]}">
                        <td class="center aligned">
                            <g:if test="${surveyConfig && surveyConfig?.type == 'Subscription' && !surveyConfig?.pickAndChoose}">
                                <g:link controller="survey" action="surveyCostItems" id="${surveyInfo?.id}"
                                        params="[surveyConfigID: surveyConfig?.id]" class="ui icon">
                                    <div class="ui circular ${surveyConfig?.costItemsFinish ? "green" : ""} label">
                                        ${surveyConfig?.getSurveyConfigCostItems()?.size() ?: 0}
                                    </div>
                                </g:link>
                            </g:if>
                        </td>
                    </g:if>
                    <g:if test="${params.tab != "created"}">
                        <td class="center aligned">
                            <g:if test="${surveyConfig && surveyConfig?.type == 'Subscription' && !surveyConfig?.pickAndChoose}">
                                <g:link controller="survey" action="evaluationConfigsInfo" id="${surveyInfo?.id}"
                                        params="[surveyConfigID: surveyConfig?.id]"
                                        class="ui icon">
                                    <div class="ui circular ${(participantsFinish?.size() == participantsTotal?.size()) ? "green" : (participantsFinish?.size() > 0) ? "yellow" :""} label">
                                        <g:if
                                            test="${participantsFinish && participantsTotal}">
                                        <g:formatNumber number="${(participantsFinish?.size() / participantsTotal?.size()) * 100}" minFractionDigits="2"
                                                        maxFractionDigits="2"/>%
                                    </g:if>
                                    <g:else>
                                        0%
                                    </g:else>
                                    </div>
                                </g:link>
                            </g:if>
                            <g:if test="${surveyConfig && surveyConfig?.type == 'Subscription' && surveyConfig?.pickAndChoose}">

                                <g:set var="participantsTitleSurveyFinish"
                                       value="${com.k_int.kbplus.SurveyOrg.findAllBySurveyConfigAndFinishDateIsNotNull(surveyConfig)}"/>

                                <g:set var="participantsTitleSurveyTotal"
                                       value="${com.k_int.kbplus.SurveyOrg.findAllBySurveyConfig(surveyConfig)}"/>
                                <g:link controller="survey" action="surveyTitlesEvaluation" id="${surveyInfo?.id}"
                                        params="[surveyConfigID: surveyConfig?.id]"
                                        class="ui icon">
                                    <div class="ui circular ${(participantsTitleSurveyFinish.size() == participantsTitleSurveyTotal.size()) ? "green" : (participantsTitleSurveyFinish.size() > 0) ? "yellow" :""} label">
                                        <g:if
                                                test="${participantsTitleSurveyFinish && participantsTitleSurveyTotal}">
                                            <g:formatNumber number="${(participantsTitleSurveyFinish.size() / participantsTitleSurveyTotal.size()) * 100}" minFractionDigits="2"
                                                            maxFractionDigits="2"/>%
                                        </g:if>
                                        <g:else>
                                            0%
                                        </g:else>
                                    </div>
                                </g:link>
                            </g:if>
                        </td>
                    </g:if>
                    <g:if test="${params.tab == "finish"}">
                        <td>
                            <g:if test="${surveyInfo.isSubscriptionSurvey && surveyConfig && surveyConfig?.type == 'Subscription' && !surveyConfig?.pickAndChoose}">
                                <g:link class="ui button "
                                        data-content=""
                                        controller="survey" action="setInEvaluation" id="${surveyInfo.id}">
                                    <g:message code="surveyInfo.evaluation.action"/>
                                </g:link>
                            </g:if>
                        </td>
                    </g:if>

                    <g:if test="${params.tab == "inEvaluation"}">
                        <td>
                        <g:if test="${surveyInfo.isSubscriptionSurvey && surveyConfig && surveyConfig?.type == 'Subscription' && !surveyConfig?.pickAndChoose}">
                            <g:link controller="survey" action="renewalwithSurvey" id="${surveyInfo?.id}"
                                    params="[surveyConfigID: surveyConfig?.id]"
                                    class="ui button ">
                                <g:message code="surveyInfo.renewal.action"/>
                            </g:link>
                        </g:if>
                        </td>
                    </g:if>
                </tr>

            </g:each>
        </table>
    </div>
</semui:form>

<g:if test="${countSurveyConfigs}">
    <semui:paginate action="${actionName}" controller="${controllerName}" params="${params}"
                    next="${message(code: 'default.paginate.next', default: 'Next')}"
                    prev="${message(code: 'default.paginate.prev', default: 'Prev')}" max="${max}"
                    total="${countSurveyConfigs."${params.tab}"}"/>
</g:if>

</body>
</html>
