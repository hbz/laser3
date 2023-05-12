<ui:breadcrumbs>
    <ui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code:'menu.my.surveys')}" />

    <g:if test="${surveyInfo}">
        <ui:crumb class="active" text="${surveyInfo.name}" />
%{--        <ui:crumb class="active" controller="survey" action="show" id="${surveyInfo.id}"--}%
%{--                     params="[surveyConfigID: surveyConfig.id]" text="${surveyInfo.name}" />--}%
    </g:if>
</ui:breadcrumbs>

