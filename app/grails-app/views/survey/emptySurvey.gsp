<%@ page import="com.k_int.kbplus.RefdataCategory;" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'createSurvey.label')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}"/>
    <semui:crumb controller="survey" action="currentSurveys" message="currentSurveys.label"/>
    <semui:crumb message="createSurvey.label" class="active"/>
</semui:breadcrumbs>

<h1 class="ui left aligned icon header"><semui:headerIcon/>${institution?.name} - ${message(code: 'createSurvey.label')}</h1>

<semui:messages data="${flash}"/>


<div class="ui four top attached steps">
    <div class="step ${params.steps == 1 ? 'active' : ''}">
        <i class="info icon"></i>

        <div class="content">
            <div class="title">${message(code: 'emptySurvey.step.first.title')}</div>

            <div class="description">${message(code: 'emptySurvey.step.first.description')}</div>
        </div>
    </div>

    <div class="step ${params.steps == 2 ? 'active' : ''}">
        <i class="tasks icon"></i>

        <div class="content">
            <div class="title">${message(code: 'emptySurvey.step.second.title')}</div>

            <div class="description">${message(code: 'emptySurvey.step.second.description')}</div>
        </div>
    </div>

    <div class="step ${params.steps == 3 ? 'active' : ''}">
        <i class="tasks icon"></i>

        <div class="content">
            <div class="title">${message(code: 'emptySurvey.step.third.title')}</div>

            <div class="description">${message(code: 'emptySurvey.step.third.description')}</div>
        </div>
    </div>

    <div class="step ${params.steps == 4 ? 'active' : ''}">
        <i class="chart pie icon"></i>

        <div class="content">
            <div class="title">${message(code: 'emptySurvey.step.fourthly.title')}</div>

            <div class="description">${message(code: 'emptySurvey.step.fourthly.description')}</div>
        </div>
    </div>
</div>
<br>
<br>
<br>


<div class="ui grid">

    <div class="twelve wide column">

        <g:if test="${params.steps == 1}">
            <g:render template="firstStep"/>
        </g:if>

        <g:if test="${params.steps == 2}">
            <g:render template="secondStep"/>
        </g:if>

    </div>

    <aside class="four wide column la-sidekick">
        <div class="la-inline-lists">

            <g:if test="${params.steps >= 2}">
                <semui:card message="emptySurvey.step.first.title" class=" ">
                    <div class="content">
                        <dl>
                            ${message(code: 'surveyInfo.name.label', default: 'New Survey Name')}
                            <dd>${params.newEmptySurveyName}</dd>
                        </dl>
                        <dl>
                            ${message(code: 'surveyInfo.startDate.label')}
                            <dd><g:formatDate formatName="default.date.format.notime"
                                              date="${params.valid_from ?: null}"/></dd>
                        </dl>
                        <dl>
                            ${message(code: 'surveyInfo.endDate.label')}
                            <dd><g:formatDate formatName="default.date.format.notime"
                                              date="${params.valid_to ?: null}"/></dd>
                        </dl>
                        <dl>
                            ${message(code: 'surveyInfo.endDate.label')}
                            <dd><g:formatDate formatName="default.date.format.notime"
                                              date="${params.valid_to ?: null}"/></dd>
                        </dl>

                        <dl>
                            ${message(code: 'surveyInfo.type.label')}
                            <dd>${com.k_int.kbplus.RefdataValue.get(params.surveyType).getI10n('value')}</dd>
                        </dl>

                    </div>

                </semui:card>
            </g:if>
        </div>
    </aside>

</div>

</body>
</html>
