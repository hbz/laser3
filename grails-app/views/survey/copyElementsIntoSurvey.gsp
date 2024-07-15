<%@ page import="de.laser.helper.Icons; de.laser.RefdataValue; de.laser.CopyElementsService;de.laser.storage.RDStore;" %>

<laser:htmlStart message="copyElementsIntoObject.survey" serviceInjection="true"/>

<ui:breadcrumbs>
    <ui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code:'menu.my.surveys')}" />

    <g:if test="${sourceObject}">
        <ui:crumb class="active" controller="survey" action="show" id="${sourceObject.surveyInfo.id}"
                     params="[surveyConfigID: sourceObject.id]" text="${sourceObject.surveyInfo.name}" />
    </g:if>
</ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="copyElementsIntoObject.survey" type="survey"/>

    <ui:messages data="${flash}"/>

    <% Map params = [:];
        if (sourceObjectId)   params << [sourceObjectId: genericOIDService.getOID(sourceObject)];
        if (targetObjectId)   params << [targetObjectId: genericOIDService.getOID(targetObject)];
    %>
        <ui:subNav showInTabular="true">
            <ui:complexSubNavItem class="${workFlowPart == CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS ? 'active' : ''}" controller="survey" action="copyElementsIntoSurvey" params="${params << [workFlowPart: CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS]}" >
                <div class="content" >
                    <div class="title">
                        ${message(code: 'copyElementsIntoObject.general_data.label')}
                    </div>
                    <div class="description">
                        <i class="${Icons.SYM.DATE}"></i> ${message(code: 'subscription.periodOfValidity.label')}
                    </div>
                </div>
            </ui:complexSubNavItem>

            <ui:complexSubNavItem class="${workFlowPart == CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS ? 'active' : ''}" controller="survey" action="copyElementsIntoSurvey" params="${params << [workFlowPart: CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS]}" >
                <div class="content">
                    <div class="title">
                        ${message(code: 'copyElementsIntoObject.attachements.label')}
                    </div>
                    <div class="description">
                        <i class="sticky note outline icon"></i>    ${message(code: 'default.notes.label')}<br />
                        <i class="${Icons.TASK}"></i>          ${message(code: 'menu.institutions.tasks')}<br />
                        <i class="${Icons.DOCUMENT}"></i>      ${message(code: 'default.documents.label')}<br />
                    %{--                        <i class="${Icons.WORKFLOW}"></i>                  ${message(code: 'workflow.plural')}--}%
                    </div>
                </div>
            </ui:complexSubNavItem>

            <ui:complexSubNavItem class="${workFlowPart == CopyElementsService.WORKFLOW_SUBSCRIBER ? 'active' : ''}" controller="survey" action="copyElementsIntoSurvey" params="${params << [workFlowPart: CopyElementsService.WORKFLOW_SUBSCRIBER]}" >
                    <div class="content">
                        <div class="title">
                            ${message(code: 'consortium.subscriber')}
                        </div>
                        <div class="description">
                            <i class="${Icons.ORG}"></i> ${message(code: 'consortium.subscriber')}
                        </div>
                    </div>
            </ui:complexSubNavItem>

            <ui:complexSubNavItem class="${workFlowPart == CopyElementsService.WORKFLOW_PROPERTIES ? 'active' : ''}" controller="survey" action="copyElementsIntoSurvey" params="${params << [workFlowPart: CopyElementsService.WORKFLOW_PROPERTIES]}" >
                <div class="content">
                    <div class="title">
                        ${message(code: 'properties')}
                    </div>
                    <div class="description">
                        <i class="${Icons.SYM.PROPERTIES}"></i> ${message(code: 'properties')}
                    </div>
                </div>
            </ui:complexSubNavItem>
        </ui:subNav>

<div class="ui bottom attached segment">
    <laser:render template="/templates/copyElements/legend"/>

    <g:if test="${workFlowPart == CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS}">
        <laser:render template="/templates/copyElements/copyDocsAndTasksAndWorkflows" />
    </g:if>
    <g:elseif test="${workFlowPart == CopyElementsService.WORKFLOW_SUBSCRIBER && targetObject && targetObject.surveyInfo.status == RDStore.SURVEY_IN_PROCESSING}">
        <laser:render template="/templates/copyElements/copySurveyParticipants" />
    </g:elseif>
    <g:elseif test="${workFlowPart == CopyElementsService.WORKFLOW_PROPERTIES}">
        <laser:render template="/templates/copyElements/copyPropertiesCompare" />
    </g:elseif>
    <g:else>
        <laser:render template="/templates/copyElements/copyElements" />
    </g:else>

    <laser:render template="/templates/copyElements/copyElementsJS"/>
</div>

<laser:htmlEnd />
