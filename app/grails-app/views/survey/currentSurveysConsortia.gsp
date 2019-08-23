<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.OrgRole;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.properties.PropertyDefinition;com.k_int.kbplus.Subscription;com.k_int.kbplus.CostItem" %>
<laser:serviceInjection/>
<!doctype html>

<r:require module="annotations"/>

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

                <g:sortableColumn params="${params}" property="surInfo.startDate"
                                  title="${message(code: 'default.startDate.label', default: 'Start Date')}"/>
                <g:sortableColumn params="${params}" property="surInfo.endDate"
                                  title="${message(code: 'default.endDate.label', default: 'End Date')}"/>
                <th>${message(code: 'surveyProperty.plural.label')}</th>
                <th>${message(code: 'surveyConfigDocs.label')}</th>
                <th>${message(code: 'surveyParticipants.label')}</th>
                <th>${message(code: 'surveyCostItems.label')}</th>
                <th>${message(code: 'surveyInfo.finished')}</th>

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
                                <i class="icon chart bar la-list-icon"></i>
                                ${surveyConfig?.getConfigNameShort()}
                            </g:else>
                        </g:else>
                        <div class="la-flexbox">
                            <i class="icon chart bar la-list-icon"></i>
                            ${surveyInfo?.name}
                        </div>
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

                        <g:if test="${surveyConfig}">
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
                                <div class="ui circular ${participantsFinish == participantsTotal ? "green" : surveyConfig?.configFinish ? "yellow" : ""} label">
                                    ${participantsFinish?.participant?.flatten()?.unique { a, b -> a.id <=> b.id }?.size() ?: 0} / ${surveyConfig?.orgs?.org?.flatten()?.unique { a, b -> a.id <=> b.id }?.size() ?: 0}
                                </div>
                            </g:link>
                        </g:if>
                    </td>


                    <td class="center aligned">
                        <g:if test="${surveyConfig}">
                            <g:link controller="survey" action="surveyCostItems" id="${surveyInfo?.id}"
                                    params="[surveyConfigID: surveyConfig?.id]" class="ui icon">
                                <div class="ui circular ${surveyConfig?.costItemsFinish ? "green" : ""} label">
                                    ${surveyConfig?.getSurveyConfigCostItems()?.size() ?: 0}
                                </div>
                            </g:link>
                        </g:if>
                    </td>

                    <td class="center aligned">
                        <g:if test="${surveyConfig}">
                            <g:link controller="survey" action="evaluationConfigsInfo" id="${surveyInfo?.id}"
                                    params="[surveyConfigID: surveyConfig?.id]"
                                    class="ui icon">
                                <div class="ui circular ${surveyConfig?.evaluationFinish ? "green" : (participantsFinish.size() == participantsTotal.size()) ? "yellow" :""} label">
                                    <g:if
                                        test="${participantsFinish && participantsTotal}">
                                    <g:formatNumber number="${(participantsFinish.size() / participantsTotal.size()) * 100}" minFractionDigits="2"
                                                    maxFractionDigits="2"/>%
                                </g:if>
                                <g:else>
                                    0%
                                </g:else>
                                </div>
                            </g:link>
                        </g:if>
                    </td>
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
