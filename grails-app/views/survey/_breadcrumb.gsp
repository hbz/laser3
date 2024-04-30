<ui:breadcrumbs>
    <ui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code:'menu.my.surveys')}" />

    <g:if test="${surveyInfo}">
        <g:if test="${actionName == 'show'}">
            <ui:crumb class="active" text="${surveyInfo.name}" />
        </g:if>
        <g:else>
            <ui:crumb controller="survey" action="show" id="${surveyInfo.id}" params="[surveyConfigID: surveyConfig.id]" text="${surveyInfo.name} (${surveyInfo.type.getI10n('value')})" />
        </g:else>

    </g:if>
</ui:breadcrumbs>

