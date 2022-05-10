<%@ page import="de.laser.storage.RDStore"%>
<laser:serviceInjection/>

<semui:subNav actionName="${actionName}">

    <g:set var="evalutionsViews"
           value="['evaluationParticipant', 'surveyEvaluation']"/>

    <g:set var="subNavDisable" value="${surveyConfig ? null : true}"/>

    <g:if test="${subNavDisable}"><g:set var="disableTooltip" value="${message(code: 'surveyConfigs.nav.propertiesNotExists')}"/></g:if>

    <g:if test="${!surveyConfig.pickAndChoose}">

        <g:if test="${surveyWithManyConfigs}">

            <semui:menuDropdownItems actionName="show" message="surveyShow.label">
                <g:each in="${surveyInfo.surveyConfigs.sort { it.getConfigNameShort() }}" var="surveyConfig">
                    <semui:menuDropdownItem controller="survey" action="show"
                                            params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                                            text="${surveyConfig.getConfigNameShort()}"/>
                </g:each>
            </semui:menuDropdownItems>


            <semui:menuDropdownItems actionName="surveyConfigDocs" message="surveyConfigDocs.label">
                <g:each in="${surveyInfo.surveyConfigs.sort { it.getConfigNameShort() }}" var="surveyConfig">
                    <semui:menuDropdownItem controller="survey" action="surveyConfigDocs"
                                            params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                                            text="${surveyConfig.getConfigNameShort()}"/>
                </g:each>
            </semui:menuDropdownItems>

            <semui:menuDropdownItems actionName="tasks" message="task.plural">
                <g:each in="${surveyInfo.surveyConfigs.sort { it.getConfigNameShort() }}" var="surveyConfig">
                    <semui:menuDropdownItem controller="survey" action="tasks"
                                            params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                                            text="${surveyConfig.getConfigNameShort()}"/>
                </g:each>
            </semui:menuDropdownItems>

            <semui:menuDropdownItems actionName="notes" message="default.notes.label">
                <g:each in="${surveyInfo.surveyConfigs.sort { it.getConfigNameShort() }}" var="surveyConfig">
                    <semui:menuDropdownItem controller="survey" action="notes"
                                            params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                                            text="${surveyConfig.getConfigNameShort()}"/>
                </g:each>
            </semui:menuDropdownItems>

            <semui:menuDropdownItems actionName="surveyParticipants" message="surveyParticipants.label">
                <g:each in="${surveyInfo.surveyConfigs.sort { it.getConfigNameShort() }}" var="surveyConfig">
                    <semui:menuDropdownItem controller="survey" action="surveyParticipants"
                                            params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                                            text="${surveyConfig.getConfigNameShort()}"/>
                </g:each>
            </semui:menuDropdownItems>

            <g:if test="${surveyInfo.type.id in [RDStore.SURVEY_TYPE_RENEWAL.id, RDStore.SURVEY_TYPE_SUBSCRIPTION.id]}">
                <semui:menuDropdownItems actionName="surveyCostItems" message="surveyCostItems.label">
                    <g:each in="${surveyInfo.surveyConfigs.sort { it.getConfigNameShort() }}" var="surveyConfig">
                        <semui:menuDropdownItem controller="survey" action="surveyCostItems"
                                                params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                                                text="${surveyConfig.getConfigNameShort()}"/>
                    </g:each>
                </semui:menuDropdownItems>
            </g:if>

            <semui:menuDropdownItems actionName="surveyEvaluation" message="surveyResult.label">
                <g:each in="${surveyInfo.surveyConfigs.sort { it.getConfigNameShort() }}" var="surveyConfig">
                    <semui:menuDropdownItem controller="survey" action="surveyEvaluation"
                                            params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                                            text="${surveyConfig.getConfigNameShort()}"/>
                </g:each>
            </semui:menuDropdownItems>

            <semui:menuDropdownItems actionName="surveyTransfer" message="surveyTransfer.label">
                <g:each in="${surveyInfo.surveyConfigs.sort { it.getConfigNameShort() }}" var="surveyConfig">
                    <g:if test="${surveyConfig.subSurveyUseForTransfer}">
                        <semui:menuDropdownItem controller="survey" action="renewalEvaluation"
                                                params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                                                text="${surveyConfig.getConfigNameShort()}"/>

                        <semui:menuDropdownItem controller="survey" action="compareMembersOfTwoSubs"
                                                params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                                                text="${message(code: 'surveyInfo.renewal') +' '+surveyConfig.getConfigNameShort()}"/>

                    </g:if>
                    <g:else>
                        <semui:menuDropdownItem controller="survey" action="surveyTransfer"
                                                params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                                                text="${surveyConfig.getConfigNameShort()}"/>
                    </g:else>
                </g:each>
            </semui:menuDropdownItems>

        </g:if>
        <g:else>

            <semui:subNavItem controller="survey" action="show" params="${[id: params.id]}" message="surveyShow.label"/>

            <semui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="surveyConfigDocs"
                              params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                              message="surveyConfigDocs.label"/>

            <semui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="tasks"
                              params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                              message="task.plural"/>

            <semui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="notes"
                              params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                              message="default.notes.label"/>

            <semui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="surveyParticipants"
                              params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                              message="surveyParticipants.label"/>

            <g:if test="${surveyInfo.type.id in [RDStore.SURVEY_TYPE_RENEWAL.id, RDStore.SURVEY_TYPE_SUBSCRIPTION.id]}">
                <semui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="surveyCostItems"
                              params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                              message="surveyCostItems.label"/>
            </g:if>

            <semui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="surveyEvaluation"
                              params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                              message="surveyResult.label"
                              class="${(actionName in evalutionsViews) ? "active" : ""}"/>

            <g:if test="${surveyConfig.subSurveyUseForTransfer}">
                <semui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="renewalEvaluation"
                                  params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                                  message="surveyInfo.evaluation"/>

                <semui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="compareMembersOfTwoSubs"
                                  class="${actionName in ['copyProperties', 'copySurveyCostItems'] ? 'active' : ''}"
                                  params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                                  message="surveyInfo.renewal"/>
            </g:if>
            <g:else>
                <semui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="surveyTransfer"
                              params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                              message="surveyTransfer.label"/>
            </g:else>

        </g:else>

    </g:if>
    <g:else>

        <semui:subNavItem controller="survey" action="show" params="${[id: params.id]}" message="surveyShow.label"/>

        <semui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="surveyTitles"
                          params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                          message="title.plural"/>

        <semui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="surveyConfigDocs"
                          params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                          message="surveyConfigDocs.label"/>

        <semui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="tasks"
                          params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                          message="task.plural"/>

        <semui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="notes"
                          params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                          message="default.notes.label"/>

        <semui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="surveyParticipants"
                          params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                          message="surveyParticipants.label"/>

        <semui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="surveyEvaluation"
                          params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                          message="surveyEvaluation.titles.label"
                          class="${(actionName in evalutionsViews) ? "active" : ""}"/>

    </g:else>

</semui:subNav>

<laser:script file="${this.getGroovyPageFileName()}">
        $(document).on('click','.dropdown .item',function(e){
            $('.ui .item').removeClass('active');
            $(this).addClass('active');
        });
</laser:script>
