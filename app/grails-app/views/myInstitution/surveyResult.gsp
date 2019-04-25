<%@ page import="com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.SurveyProperty;" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'survey.label')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}"/>
    <semui:crumb controller="survey" action="currentSurveysConsortia" message="currentSurveys.label"/>
    <semui:crumb message="survey.label" class="active"/>
</semui:breadcrumbs>

<h1 class="ui left aligned icon header"><semui:headerIcon/>${institution?.name} - ${message(code: 'survey.label')}</h1>

<br>

<semui:messages data="${flash}"/>

<br>


<div class="la-inline-lists">
    <div class="ui card">
        <div class="content">

            <div class="header">
                <div class="ui grid">
                    <div class="twelve wide column">
                        ${message(code: 'showSurveyInfo.step.first.title')}
                    </div>
                </div>
            </div>
            <dl>
                <dt>${message(code: 'surveyInfo.status.label', default: 'Survey Status')}</dt>
                <dd>${surveyInfo.status?.getI10n('value')}</dd>
            </dl>
            <dl>
                <dt>${message(code: 'surveyInfo.name.label', default: 'New Survey Name')}</dt>
                <dd>${surveyInfo.name}</dd>
            </dl>
            <dl>
                <dt>${message(code: 'surveyInfo.startDate.label')}</dt>
                <dd><g:formatDate formatName="default.date.format.notime"
                                  date="${surveyInfo.startDate ?: null}"/></dd>
            </dl>
            <dl>
                <dt>${message(code: 'surveyInfo.endDate.label')}</dt>
                <dd><g:formatDate formatName="default.date.format.notime"
                                  date="${surveyInfo.endDate ?: null}"/></dd>
            </dl>

            <dl>
                <dt>${message(code: 'surveyInfo.type.label')}</dt>
                <dd>${com.k_int.kbplus.RefdataValue.get(surveyInfo?.type?.id)?.getI10n('value')}</dd>
            </dl>

            <dl>
                <dt>${message(code: 'surveyInfo.owner.label')}</dt>
                <dd>${surveyInfo?.owner}</dd>
            </dl>
            <dl>
                <dt>${message(code: 'surveyInfo.comment.label')}</dt>
                <dd>${surveyInfo?.comment}</dd>
            </dl>

        </div>
    </div>
</div>

<br>

<h3>
<p>
    <g:message code="surveyResult.info" args="[surveyInfo?.owner]"/>
</p>
</h3>

<br>

<h2 class="ui left aligned icon header">${message(code: 'surveyResult.list')} <semui:totalNumber
        total="${surveyResults.size()}"/></h2>

<div class="ui grid">
    <table class="ui celled sortable table la-table">
        <thead>
        <tr>
            <th class="center aligned">
                ${message(code: 'surveyConfig.configOrder.label')}
            </th>
            <th>${message(code: 'surveyProperty.name.label')}</th>
            <th>${message(code: 'surveyProperty.type.label')}</th>
            <th></th>
            <th></th>

        </tr>

        </thead>

        <g:each in="${surveyResults}" var="surveyResult" status="i">


            <tr>
                <td class="center aligned">
                    ${i+1}
                </td>
                <td>
                    <g:if test="${surveyResult.surveyConfig?.type == 'Subscription'}">

                        <g:set var="childSub" value="${surveyResult.surveyConfig?.subscription?.getDerivedSubscriptionBySubscribers(institution)}"/>

                        <g:link controller="subscription" action="show"
                                id="${childSub.id}">${childSub.dropdownNamingConvention()}</g:link>
                    </g:if>

                    <g:if test="${surveyResult.surveyConfig?.type == 'SurveyProperty'}">
                        ${surveyResult.surveyConfig?.surveyProperty?.getI10n('name')}
                    </g:if>

                </td>
                <td>
                    ${com.k_int.kbplus.SurveyConfig.getLocalizedValue(surveyResult.surveyConfig?.type)}

                    <g:if test="${surveyResult.surveyConfig?.surveyProperty}">
                        <br>
                        <b>${message(code: 'surveyProperty.type.label')}: ${com.k_int.kbplus.SurveyProperty.getLocalizedValue(surveyResult.surveyConfig?.surveyProperty?.type)}</b>

                        <g:if test="${surveyResult.surveyConfig?.surveyProperty?.type == 'class com.k_int.kbplus.RefdataValue'}">
                            <g:set var="refdataValues" value="${[]}"/>
                            <g:each in="${com.k_int.kbplus.RefdataCategory.getAllRefdataValues(surveyResult.surveyConfig?.surveyProperty?.refdataCategory)}"
                                    var="refdataValue">
                                <g:set var="refdataValues"
                                       value="${refdataValues + refdataValue?.getI10n('value')}"/>
                            </g:each>
                            <br>
                            (${refdataValues.join('/')})
                        </g:if>
                    </g:if>

                </td>
                <td>
                </td>
            </tr>


        </g:each>
    </table>
</div>

<br>
<br>

</div>

</body>
</html>
