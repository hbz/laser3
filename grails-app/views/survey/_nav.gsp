<%@ page import="de.laser.storage.RDStore" %>
<laser:serviceInjection/>

<ui:subNav actionName="${actionName}">

    <g:set var="evalutionsViews"
           value="['evaluationParticipant', 'surveyEvaluation']"/>

    <g:set var="subNavDisable" value="${surveyConfig ? null : true}"/>

    <g:if test="${subNavDisable}"><g:set var="disableTooltip" value="${message(code: 'surveyConfigs.nav.propertiesNotExists')}"/></g:if>

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

        <ui:menuDropdownItems actionName="surveyCostItems" message="surveyCostItems.label">
            <g:each in="${surveyInfo.surveyConfigs.sort { it.getConfigNameShort() }}" var="surveyConfig">
                <ui:menuDropdownItem controller="survey" action="surveyCostItems"
                                     params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                                     text="${surveyConfig.getConfigNameShort()}"/>
            </g:each>
        </ui:menuDropdownItems>


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
                                         text="${message(code: 'surveyInfo.renewal') + ' ' + surveyConfig.getConfigNameShort()}"/>

                </g:if>
                <ui:menuDropdownItem controller="survey" action="surveyTransfer"
                                         params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                                         text="${surveyConfig.getConfigNameShort()}"/>

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

        <g:if test="${surveyConfig.pickAndChoose}">
            <ui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="surveyTitles"
                           params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                           message="title.plural"/>
        </g:if>

        <ui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="surveyParticipants"
                       params="${[id: params.id, surveyConfigID: surveyConfig.id]}" counts="${participantsCount}"
                       message="surveyParticipants.label"/>

        <g:if test="${surveyConfig.packageSurvey}">
            <ui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="surveyPackages"
                           params="${[id: params.id, surveyConfigID: surveyConfig.id, initial: true]}" counts="${surveyPackagesCount}"
                           class="${(actionName in ['surveyPackages', 'linkSurveyPackage'] ? 'active' : '')}"
                           message="surveyPackages.label"/>
        </g:if>

        <g:if test="${surveyConfig.vendorSurvey}">
            <ui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="surveyVendors"
                           params="${[id: params.id, surveyConfigID: surveyConfig.id, initial: true]}" counts="${surveyVendorsCount}"
                           class="${(actionName in ['surveyVendors', 'linkSurveyVendor'] ? 'active' : '')}"
                           message="surveyVendors.label"/>
        </g:if>


        <ui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="surveyCostItems"
                       params="${[id: params.id, surveyConfigID: surveyConfig.id]}" counts="${surveyCostItemsCount}"
                       message="surveyCostItems.label"/>


        <g:if test="${surveyConfig.packageSurvey}">
            <ui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="surveyCostItemsPackages"
                           params="${[id: params.id, surveyConfigID: surveyConfig.id]}" counts="${surveyCostItemsPackagesCount}"
                           message="surveyCostItemsPackages.label"/>
        </g:if>

        <ui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="surveyEvaluation"
                       params="${[id: params.id, surveyConfigID: surveyConfig.id]}" counts="${evaluationCount}"
                       message="surveyResult.label"
                       class="${(actionName in evalutionsViews) ? "active" : ""}"/>

        <g:if test="${surveyConfig.packageSurvey}">
            <ui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="surveyPackagesEvaluation"
                           params="${[id: params.id, surveyConfigID: surveyConfig.id]}" counts="${evaluationCount}"
                           message="surveyPackagesEvaluation.label"/>
        </g:if>

        <g:if test="${surveyConfig.subSurveyUseForTransfer}">
            <ui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="renewalEvaluation"
                           params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                           message="surveyInfo.evaluation"/>

            <ui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="compareMembersOfTwoSubs"
                           class="${actionName in ['copyProperties', 'copySurveyCostItems', 'copySurveyPackages', 'copySurveyCostItemPackage', 'copySurveyVendors'] ? 'active' : ''}"
                           params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                           message="surveyInfo.renewal"/>
        </g:if>
        <ui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="surveyTransfer"
                           params="${[id: params.id, surveyConfigID: surveyConfig.id]}"
                           message="surveyTransfer.label"/>


        <ui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="notes"
                       params="${[id: params.id, surveyConfigID: surveyConfig.id]}" counts="${notesCount}"
                       message="default.notes.label"/>

        <ui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="tasks"
                       params="${[id: params.id, surveyConfigID: surveyConfig.id]}" counts="${tasksCount}"
                       message="task.plural"/>

        <ui:subNavItem controller="survey" disabled="${subNavDisable}" tooltip="${disableTooltip}" action="surveyConfigDocs"
                       params="${[id: params.id, surveyConfigID: surveyConfig.id]}" counts="${docsCount}"
                       message="surveyConfigDocs.label"/>
    </g:else>

</ui:subNav>

<laser:script file="${this.getGroovyPageFileName()}">
    $(document).on('click','.dropdown .item',function(e){
        $('.ui .item').removeClass('active');
        $(this).addClass('active');
    });
</laser:script>
