<laser:serviceInjection/>

<semui:subNav actionName="${actionName}">

    <g:set var="evalutionsViews"
           value="['evaluationParticipant', 'surveyEvaluation']"/>

    <g:set var="subNavDisable" value="${surveyConfig ? null : true}"/>

    <g:if test="${subNavDisable}"><g:set var="disableTooltip" value="${message(code: 'surveyConfigs.nav.propertiesNotExists')}"/></g:if>

    <g:if test="${!surveyConfig?.pickAndChoose}">

        <g:if test="${surveyWithManyConfigs}">

            <semui:menuDropdownItems actionName="show" message="surveyShow.label">
                <g:each in="${surveyInfo?.surveyConfigs.sort { it?.getConfigNameShort() }}" var="surveyConfig">
                    <semui:menuDropdownItem controller="survey" action="show"
                                            params="${[id: params.id, surveyConfigID: surveyConfig?.id]}"
                                            text="${surveyConfig.getConfigNameShort()}"/>
                </g:each>
            </semui:menuDropdownItems>


            <semui:menuDropdownItems actionName="surveyConfigDocs" message="surveyConfigDocs.label">
                <g:each in="${surveyInfo?.surveyConfigs.sort { it?.getConfigNameShort() }}" var="surveyConfig">
                    <semui:menuDropdownItem controller="survey" action="surveyConfigDocs"
                                            params="${[id: params.id, surveyConfigID: surveyConfig?.id]}"
                                            text="${surveyConfig.getConfigNameShort()}"/>
                </g:each>
            </semui:menuDropdownItems>

            <semui:menuDropdownItems actionName="surveyParticipants" message="surveyParticipants.label">
                <g:each in="${surveyInfo?.surveyConfigs.sort { it?.getConfigNameShort() }}" var="surveyConfig">
                    <semui:menuDropdownItem controller="survey" action="surveyParticipants"
                                            params="${[id: params.id, surveyConfigID: surveyConfig?.id]}"
                                            text="${surveyConfig.getConfigNameShort()}"/>
                </g:each>
            </semui:menuDropdownItems>

            <g:if test="${surveyInfo.type.id == de.laser.helper.RDStore.SURVEY_TYPE_RENEWAL.id}">
                <semui:menuDropdownItems actionName="surveyCostItems" message="surveyCostItems.label">
                    <g:each in="${surveyInfo?.surveyConfigs.sort { it?.getConfigNameShort() }}" var="surveyConfig">
                        <semui:menuDropdownItem controller="survey" action="surveyCostItems"
                                                params="${[id: params.id, surveyConfigID: surveyConfig?.id]}"
                                                text="${surveyConfig.getConfigNameShort()}"/>
                    </g:each>
                </semui:menuDropdownItems>
            </g:if>

            <semui:menuDropdownItems actionName="surveyEvaluation" message="surveyEvaluation.label">
                <g:each in="${surveyInfo?.surveyConfigs.sort { it?.getConfigNameShort() }}" var="surveyConfig">
                    <semui:menuDropdownItem controller="survey" action="surveyEvaluation"
                                            params="${[id: params.id, surveyConfigID: surveyConfig?.id]}"
                                            text="${surveyConfig.getConfigNameShort()}"/>
                </g:each>
            </semui:menuDropdownItems>

        </g:if>
        <g:else>

            <semui:subNavItem controller="survey" action="show" params="${[id: params.id]}" message="surveyShow.label"/>

            <semui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="surveyConfigDocs"
                              params="${[id: params.id, surveyConfigID: surveyConfig?.id]}"
                              message="surveyConfigDocs.label"/>

            <semui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="surveyParticipants"
                              params="${[id: params.id, surveyConfigID: surveyConfig?.id]}"
                              message="surveyParticipants.label"/>

            <g:if test="${surveyInfo?.type.id == de.laser.helper.RDStore.SURVEY_TYPE_RENEWAL.id}">
                <semui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="surveyCostItems"
                              params="${[id: params.id, surveyConfigID: surveyConfig?.id]}"
                              message="surveyCostItems.label"/>
            </g:if>

            <semui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="surveyEvaluation"
                              params="${[id: params.id, surveyConfigID: surveyConfig?.id]}"
                              message="surveyEvaluation.label"
                              class="${(actionName in evalutionsViews) ? "active" : ""}"/>
        </g:else>

    </g:if>
    <g:else>

        <semui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="surveyTitles"
                          params="${[id: params.id, surveyConfigID: surveyConfig?.id]}"
                          message="surveyTitles.label"/>

        <semui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="surveyConfigDocs"
                          params="${[id: params.id, surveyConfigID: surveyConfig?.id]}"
                          message="surveyConfigDocs.label"/>

        <semui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="surveyParticipants"
                          params="${[id: params.id, surveyConfigID: surveyConfig?.id]}"
                          message="surveyParticipants.label"/>

        <semui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="surveyTitlesEvaluation"
                          params="${[id: params.id, surveyConfigID: surveyConfig?.id]}"
                          message="surveyTitlesEvaluation.label"
                          class="${(actionName in evalutionsViews) ? "active" : ""}"/>

    </g:else>

</semui:subNav>

<g:javascript>
    $(document).ready(function(){
        $(document).on('click','.dropdown .item',function(e){
            $('.ui .item').removeClass('active');
            $(this).addClass('active');
        });
    });
</g:javascript>
