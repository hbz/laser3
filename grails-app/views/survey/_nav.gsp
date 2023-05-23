<%@ page import="de.laser.storage.RDStore"%>
<laser:serviceInjection/>

<ui:subNav actionName="${actionName}">

    <g:set var="evalutionsViews"
           value="['evaluationParticipant', 'surveyEvaluation']"/>

    <g:set var="subNavDisable" value="${surveyConfig ? null : true}"/>

    <g:if test="${subNavDisable}"><g:set var="disableTooltip" value="${message(code: 'surveyConfigs.nav.propertiesNotExists')}"/></g:if>

    <g:if test="${!surveyConfig.pickAndChoose}">

        <g:if test="${surveyWithManyConfigs}">

            <ui:menuDropdownItems actionName="show" message="surveyShow.label">
                <g:each in="${surveyInfo.surveyConfigs.sort { it.getConfigNameShort() }}" var="surveyConfig">
                    <ui:menuDropdownItem controller="survey" action="show"
                                            params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                                            text="${surveyConfig.getConfigNameShort()}"/>
                </g:each>
            </ui:menuDropdownItems>

            <ui:menuDropdownItems actionName="surveyParticipants" message="surveyParticipants.label">
                <g:each in="${surveyInfo.surveyConfigs.sort { it.getConfigNameShort() }}" var="surveyConfig">
                    <ui:menuDropdownItem controller="survey" action="surveyParticipants"
                                            params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                                            text="${surveyConfig.getConfigNameShort()}"/>
                </g:each>
            </ui:menuDropdownItems>

            <g:if test="${surveyInfo.type.id in [RDStore.SURVEY_TYPE_RENEWAL.id, RDStore.SURVEY_TYPE_SUBSCRIPTION.id]}">
                <ui:menuDropdownItems actionName="surveyCostItems" message="surveyCostItems.label">
                    <g:each in="${surveyInfo.surveyConfigs.sort { it.getConfigNameShort() }}" var="surveyConfig">
                        <ui:menuDropdownItem controller="survey" action="surveyCostItems"
                                                params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                                                text="${surveyConfig.getConfigNameShort()}"/>
                    </g:each>
                </ui:menuDropdownItems>
            </g:if>

            <ui:menuDropdownItems actionName="surveyEvaluation" message="surveyResult.label">
                <g:each in="${surveyInfo.surveyConfigs.sort { it.getConfigNameShort() }}" var="surveyConfig">
                    <ui:menuDropdownItem controller="survey" action="surveyEvaluation"
                                            params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                                            text="${surveyConfig.getConfigNameShort()}"/>
                </g:each>
            </ui:menuDropdownItems>

            <ui:menuDropdownItems actionName="surveyTransfer" message="surveyTransfer.label">
                <g:each in="${surveyInfo.surveyConfigs.sort { it.getConfigNameShort() }}" var="surveyConfig">
                    <g:if test="${surveyConfig.subSurveyUseForTransfer}">
                        <ui:menuDropdownItem controller="survey" action="renewalEvaluation"
                                                params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                                                text="${surveyConfig.getConfigNameShort()}"/>

                        <ui:menuDropdownItem controller="survey" action="compareMembersOfTwoSubs"
                                                params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                                                text="${message(code: 'surveyInfo.renewal') +' '+surveyConfig.getConfigNameShort()}"/>

                    </g:if>
                    <g:else>
                        <ui:menuDropdownItem controller="survey" action="surveyTransfer"
                                                params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                                                text="${surveyConfig.getConfigNameShort()}"/>
                    </g:else>
                </g:each>
            </ui:menuDropdownItems>

            <ui:menuDropdownItems actionName="notes" message="default.notes.label">
                <g:each in="${surveyInfo.surveyConfigs.sort { it.getConfigNameShort() }}" var="surveyConfig">
                    <ui:menuDropdownItem controller="survey" action="notes"
                                         params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                                         text="${surveyConfig.getConfigNameShort()}"/>
                </g:each>
            </ui:menuDropdownItems>

            <ui:menuDropdownItems actionName="tasks" message="task.plural">
                <g:each in="${surveyInfo.surveyConfigs.sort { it.getConfigNameShort() }}" var="surveyConfig">
                    <ui:menuDropdownItem controller="survey" action="tasks"
                                         params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                                         text="${surveyConfig.getConfigNameShort()}"/>
                </g:each>
            </ui:menuDropdownItems>

            <ui:menuDropdownItems actionName="surveyConfigDocs" message="surveyConfigDocs.label">
                <g:each in="${surveyInfo.surveyConfigs.sort { it.getConfigNameShort() }}" var="surveyConfig">
                    <ui:menuDropdownItem controller="survey" action="surveyConfigDocs"
                                         params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                                         text="${surveyConfig.getConfigNameShort()}"/>
                </g:each>
            </ui:menuDropdownItems>

        </g:if>
        <g:else>

            <ui:subNavItem controller="survey" action="show" params="${[id: params.id]}" message="surveyShow.label"/>

            <ui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="surveyParticipants"
                              params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                              message="surveyParticipants.label"/>

            <g:if test="${surveyInfo.type.id in [RDStore.SURVEY_TYPE_RENEWAL.id, RDStore.SURVEY_TYPE_SUBSCRIPTION.id]}">
                <ui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="surveyCostItems"
                              params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                              message="surveyCostItems.label"/>
            </g:if>

            <ui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="surveyEvaluation"
                              params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                              message="surveyResult.label"
                              class="${(actionName in evalutionsViews) ? "active" : ""}"/>

            <g:if test="${surveyConfig.subSurveyUseForTransfer}">
                <ui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="renewalEvaluation"
                                  params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                                  message="surveyInfo.evaluation"/>

                <ui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="compareMembersOfTwoSubs"
                                  class="${actionName in ['copyProperties', 'copySurveyCostItems'] ? 'active' : ''}"
                                  params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                                  message="surveyInfo.renewal"/>
            </g:if>
            <g:else>
                <ui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="surveyTransfer"
                              params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                              message="surveyTransfer.label"/>
            </g:else>

            <ui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="notes"
                           params="${[id: params.id, surveyConfigID: surveyConfig.id]}" counts="${notesCount}"
                           message="default.notes.label"/>

            <ui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="tasks"
                           params="${[id: params.id, surveyConfigID: surveyConfig.id]}" counts="${tasksCount}"
                           message="task.plural"/>

            <ui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="surveyConfigDocs"
                           params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                           message="surveyConfigDocs.label"/>
        </g:else>

    </g:if>
    <g:else>

        <ui:subNavItem controller="survey" action="show" params="${[id: params.id]}" message="surveyShow.label"/>

        <ui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="surveyTitles"
                          params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                          message="title.plural"/>

        <ui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="surveyParticipants"
                          params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                          message="surveyParticipants.label"/>

        <ui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="surveyEvaluation"
                          params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                          message="surveyEvaluation.titles.label"
                          class="${(actionName in evalutionsViews) ? "active" : ""}"/>

        <ui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="notes"
                       params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                       message="default.notes.label"/>

        <ui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="tasks"
                       params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                       message="task.plural"/>

        <ui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="surveyConfigDocs"
                       params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                       message="surveyConfigDocs.label"/>

    </g:else>

</ui:subNav>

<laser:script file="${this.getGroovyPageFileName()}">
        $(document).on('click','.dropdown .item',function(e){
            $('.ui .item').removeClass('active');
            $(this).addClass('active');
        });
</laser:script>
