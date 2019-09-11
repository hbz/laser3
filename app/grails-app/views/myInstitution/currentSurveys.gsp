<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.SurveyResult; com.k_int.kbplus.OrgRole;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.properties.PropertyDefinition;com.k_int.kbplus.Subscription;com.k_int.kbplus.CostItem" %>
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


<h1 class="ui left aligned icon header"><semui:headerIcon/>${institution?.name} - ${message(code: 'currentSurveys.label', default: 'Current Surveys')}
<semui:totalNumber total="${countSurveys.values().sum { it }}"/>
</h1>

<semui:messages data="${flash}"/>

<semui:filter>
    <g:form action="currentSurveys" controller="myInstitution" method="get" class="form-inline ui small form">
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
        <semui:tabsItem controller="myInstitution" action="currentSurveys"
                        params="${[id: params.id, tab: 'new']}" text="neu" tab="new"
                        counts="${countSurveys?.new}"/>
        <semui:tabsItem controller="myInstitution" action="currentSurveys"
                        params="${[id: params.id, tab: 'processed']}" text="bearbeitet" tab="processed"
                        counts="${countSurveys?.processed}"/>
        <semui:tabsItem controller="myInstitution" action="currentSurveys"
                        params="${[id: params.id, tab: 'finish']}" text="abgeschlossen" tab="finish"
                        counts="${countSurveys?.finish}"/>
    </semui:tabs>

    <table class="ui celled sortable table la-table">
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
                              title="${message(code: 'default.endDate.label', default: 'End Date')}"/>
            <g:sortableColumn params="${params}" property="surveyInfo.owner"
                              title="${message(code: 'surveyInfo.owner.label')}"/>
            <th><g:message code="surveyInfo.finished"/></th>
            <th class="la-action-info">${message(code:'default.actions')}</th>
        </tr>

        </thead>
        <g:each in="${surveyResults}" var="surveyResult" status="i">

            <g:set var="surveyConfig"
                   value="${com.k_int.kbplus.SurveyConfig.get(surveyResult.key)}"/>

            <g:set var="surveyInfo"
                   value="${surveyConfig.surveyInfo}"/>

            <tr>
                <td class="center aligned">
                    ${(params.int('offset') ?: 0) + i + 1}
                </td>
                <td>
                    <div class="la-flexbox">
                        <g:if test="${surveyConfig?.isSubscriptionSurveyFix}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                  data-content="${message(code: "surveyConfig.isSubscriptionSurveyFix.label.info2")}">
                                <i class="yellow icon envelope large "></i>
                            </span>
                        </g:if>
                        <i class="icon chart pie la-list-icon"></i>
                        <g:link controller="survey" action="show" id="${surveyInfo?.id}"
                                 class="ui ">
                            ${surveyInfo.isSubscriptionSurvey ? surveyConfig?.getSurveyName() : surveyInfo?.name}
                        </g:link>
                    </div>
                </td>
                <td>
                    ${surveyInfo.type.getI10n('value')} (${surveyInfo.isSubscriptionSurvey ? message(code: 'subscriptionSurvey.label') : message(code: 'generalSurvey.label')})
                </td>
                <td>
                    <g:formatDate formatName="default.date.format.notime" date="${surveyInfo?.endDate}"/>
                </td>

                <td class="center aligned">

                    ${surveyInfo?.owner}

                </td>

                <td class="center aligned">
                    <g:set var="surveyResults"
                           value="${SurveyResult.findAllByParticipantAndSurveyConfig(institution, surveyConfig)}"/>

                    <g:if test="${surveyResults}">

                        <g:link action="surveyConfigsInfo" id="${surveyInfo?.id}" params="[surveyConfigID: surveyConfig?.id]"
                                class="ui icon mini button">
                            <g:if test="${surveyResults?.finishDate?.contains(null)}">
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="top right"
                                      data-variation="tiny"
                                      data-content="Nicht abgeschlossen">
                                    <i class="circle red icon"></i>
                                </span>
                            </g:if>
                            <g:else>

                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="top right"
                                      data-variation="tiny"
                                      data-content="${message(code: 'surveyResult.finish.info.consortia')}">
                                    <i class="check big green icon"></i>
                                </span>
                            </g:else>
                        </g:link>
                    </g:if>
                </td>

                <td class="x">

                    <g:if test="${editable}">
                        <span class="la-popup-tooltip la-delay"
                              data-content="${message(code: 'surveyInfo.toSurveyInfos')}">
                            <g:link controller="myInstitution" action="surveyInfos" id="${surveyInfo.id}"
                                    class="ui icon button">
                                <i class="write icon"></i>
                            </g:link>
                        </span>
                    </g:if>
                </td>

            </tr>

        </g:each>
    </table>
</semui:form>


%{--<g:if test="${countSurveys."${params.tab}"}">
    <semui:paginate action="${actionName}" controller="${controllerName}" params="${params}"
                    next="${message(code: 'default.paginate.next', default: 'Next')}"
                    prev="${message(code: 'default.paginate.prev', default: 'Prev')}" max="${max}"
                    total="${countSurveys."${params.tab}"}"/>
</g:if>--}%

</body>
</html>
