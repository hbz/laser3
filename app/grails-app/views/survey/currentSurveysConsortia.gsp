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
                                  title="${message(code: 'surveyInfo.name.label')}"/>
                <g:sortableColumn params="${params}" property="surInfo.startDate"
                                  title="${message(code: 'default.startDate.label', default: 'Start Date')}"/>
                <g:sortableColumn params="${params}" property="surInfo.endDate"
                                  title="${message(code: 'default.endDate.label', default: 'End Date')}"/>
                <th>${message(code: 'surveyProperty.plural.label')}</th>
                <th>${message(code: 'surveyConfigDocs.label')}</th>
                <th>${message(code: 'surveyParticipants.label')}</th>
                <th>${message(code: 'surveyCostItems.label')}</th>
                <th>${message(code: 'surveyInfo.evaluation')}</th>

            </tr>

            </thead>
            <g:each in="${surveyConfigs}" var="surveyConfig" status="i">
                <tr>
                    <td class="center aligned">
                        ${(params.int('offset') ?: 0) + i + 1}
                    </td>
                    <td><g:if test="${editable}">
                        <g:if test="${surveyConfig?.type == 'Subscription'}">
                            <i class="icon clipboard outline la-list-icon"></i>
                            <g:link controller="survey" action="surveyConfigsInfo" id="${surveyConfig.surveyInfo?.id}"
                                    params="[surveyConfigID: surveyConfig?.id]" class="ui ">
                                ${surveyConfig?.subscription?.name}
                            </g:link>
                        </g:if>
                        <g:else>
                            <g:link controller="survey" action="show" id="${surveyConfig.surveyInfo?.id}" class="ui ">
                                ${surveyConfig.surveyInfo?.name}
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
                                ${surveyConfig.surveyInfo?.name}
                            </g:else>
                        </g:else>
                    </td>
                    <td>
                        <g:formatDate formatName="default.date.format.notime"
                                      date="${surveyConfig.surveyInfo?.startDate}"/>

                    </td>
                    <td>

                        <g:formatDate formatName="default.date.format.notime"
                                      date="${surveyConfig.surveyInfo?.endDate}"/>
                    </td>

                    <td class="center aligned">
                        <g:if test="${surveyConfig?.type == 'Subscription'}">
                            <g:link controller="survey" action="surveyConfigsInfo" id="${surveyConfig.surveyInfo?.id}"
                                    params="[surveyConfigID: surveyConfig?.id]" class="ui icon">
                                <div class="ui circular ${surveyConfig?.configFinish ? "green" : ""} label">${surveyConfig?.surveyProperties?.size()}</div>
                            </g:link>
                        </g:if>

                    </td>
                    <td class="center aligned">
                        <g:link controller="survey" action="surveyConfigDocs" id="${surveyConfig.surveyInfo?.id}"
                                params="[surveyConfigID: surveyConfig?.id]" class="ui icon">
                            <div class="ui circular label">${surveyConfig?.getCurrentDocs()?.size()}</div>
                        </g:link>
                    </td>

                    <g:set var="finish"
                           value="${com.k_int.kbplus.SurveyResult.findAllBySurveyConfigAndFinishDateIsNotNull(surveyConfig)}"/>

                    <td class="center aligned">
                        <g:link controller="survey" action="surveyParticipants" id="${surveyConfig.surveyInfo?.id}"
                                params="[surveyConfigID: surveyConfig?.id]" class="ui icon">
                            <div class="ui circular ${surveyConfig?.configFinish ? "green" : ""} label">${finish?.participant?.flatten()?.unique { a, b -> a.id <=> b.id }?.size()}/${surveyConfig?.orgs?.org?.flatten()?.unique { a, b -> a.id <=> b.id }?.size()}</div>
                        </g:link>
                    </td>


                    <td class="center aligned">
                        <g:link controller="survey" action="surveyCostItems" id="${surveyConfig.surveyInfo?.id}"
                                params="[surveyConfigID: surveyConfig?.id]" class="ui icon">
                            <div class="ui circular ${surveyConfig?.costItemsFinish ? "green" : ""} label">${surveyConfig?.getSurveyConfigCostItems()?.size()}</div>
                        </g:link>
                    </td>

                    <td class="center aligned">

                        <g:set var="total"
                               value="${com.k_int.kbplus.SurveyResult.findAllBySurveyConfig(surveyConfig)}"/>
                        <g:link controller="survey" action="evaluationConfigsInfo" id="${surveyConfig.surveyInfo?.id}" params="[surveyConfigID: surveyConfig?.id]"
                                class="ui icon">
                            <div class="ui circular ${surveyConfig?.evaluationFinish ? "green" : ""} label"><g:if test="${finish && total}">
                                <g:formatNumber number="${(finish.size() / total.size()) * 100}" minFractionDigits="2"
                                                maxFractionDigits="2"/>%
                            </g:if>
                            <g:else>
                                0%
                            </g:else>
                            </div>
                        </g:link>
                    </td>
                </tr>

            </g:each>
        </table>
    </div>
</semui:form>

<g:if test="${surveyConfigs}">
    <semui:paginate action="${actionName}" controller="${controllerName}" params="${params}"
                    next="${message(code: 'default.paginate.next', default: 'Next')}"
                    prev="${message(code: 'default.paginate.prev', default: 'Prev')}" max="${max}"
                    total="${countSurveyConfigs."${params.tab}"}"/>
</g:if>

</body>
</html>
