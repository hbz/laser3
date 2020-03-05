<laser:serviceInjection />

<semui:breadcrumbs>
    <semui:crumb controller="survey" action="currentSurveysConsortia" text="${message(code:'menu.my.surveys')}" />

    <g:if test="${surveyInfo}">
        <semui:crumb class="active" controller="survey" action="show" id="${surveyInfo.id}" text="${surveyInfo.name}" />
    </g:if>
</semui:breadcrumbs>

