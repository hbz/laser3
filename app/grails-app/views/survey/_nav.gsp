<laser:serviceInjection />

<semui:subNav actionName="${actionName}">

    <g:set var="evalutionsViews" value="['evaluationConfigsInfo', 'evaluationConfigResult', 'evaluationParticipantInfo']"/>
    <g:set var="surveyConfigsViews" value="['surveyConfigsInfo']"/>

    <g:set var="subNavDisable" value="${surveyConfig ? null : true}"/>

    <semui:subNavItem controller="survey" action="show" params="${[id:params.id, surveyConfigID: surveyConfig?.id]}" message="surveyShow.label" />

    <semui:subNavItem controller="survey" action="surveyConfigs" params="${[id:params.id, surveyConfigID: surveyConfig?.id]}" message="surveyConfigs.label"
                      class="${(actionName in surveyConfigsViews) ? "active" : ""}"/>

    <g:if test="${surveyInfo?.surveyConfigs?.size() > 1}">

        <semui:menuDropdownItems message="surveyConfigDocs.label">
            <g:each in="${surveyInfo?.surveyConfigs.sort{it?.getConfigNameShort()}}" var="surveyConfig">
                <semui:menuDropdownItem controller="survey" action="surveyConfigDocs" params="${[id:params.id, surveyConfigID: surveyConfig?.id]}" text="${surveyConfig.getConfigNameShort()}" />
            </g:each>
        </semui:menuDropdownItems>

        <semui:menuDropdownItems message="surveyParticipants.label">
            <g:each in="${surveyInfo?.surveyConfigs}" var="surveyConfig">
                <semui:menuDropdownItem controller="survey" action="surveyParticipants" params="${[id:params.id, surveyConfigID: surveyConfig?.id]}" text="${surveyConfig.getConfigNameShort()}" />
            </g:each>
        </semui:menuDropdownItems>

        <semui:menuDropdownItems message="surveyCostItems.label">
            <g:each in="${surveyInfo?.surveyConfigs}" var="surveyConfig">
                <semui:menuDropdownItem controller="survey" action="surveyCostItems" params="${[id:params.id, surveyConfigID: surveyConfig?.id]}" text="${surveyConfig.getConfigNameShort()}" />
            </g:each>
        </semui:menuDropdownItems>

        <semui:menuDropdownItems message="surveyEvaluation.label">
            <g:each in="${surveyInfo?.surveyConfigs}" var="surveyConfig">
                <semui:menuDropdownItem controller="survey" action="surveyEvaluation" params="${[id:params.id, surveyConfigID: surveyConfig?.id]}" text="${surveyConfig.getConfigNameShort()}" />
            </g:each>
        </semui:menuDropdownItems>

    </g:if>
    <g:else>

        <semui:subNavItem controller="survey" disabled="${subNavDisable}" action="surveyConfigDocs" params="${[id:params.id, surveyConfigID: surveyConfig?.id]}" message="surveyConfigDocs.label" />

        <semui:subNavItem controller="survey" disabled="${subNavDisable}" action="surveyParticipants" params="${[id:params.id, surveyConfigID: surveyConfig?.id]}" message="surveyParticipants.label" />

        <semui:subNavItem controller="survey" disabled="${subNavDisable}" action="surveyCostItems" params="${[id:params.id, surveyConfigID: surveyConfig?.id]}" message="surveyCostItems.label" />

        <semui:subNavItem controller="survey" disabled="${subNavDisable}" action="surveyEvaluation" params="${[id:params.id, surveyConfigID: surveyConfig?.id]}" message="surveyEvaluation.label"
                          class="${(actionName in evalutionsViews) ? "active" : ""}"/>
    </g:else>

</semui:subNav>
