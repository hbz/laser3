<%@ page import="de.laser.RefdataValue; de.laser.CopyElementsService;de.laser.helper.RDStore;" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
        <title>${message(code: 'laser')} : ${message(code: 'copyElementsIntoObject.label', args: [message(code: "${sourceObject.getClass().getSimpleName().toLowerCase()}.label")])}</title>
</head>
<body>
<semui:breadcrumbs>
    <semui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code:'menu.my.surveys')}" />

    <g:if test="${sourceObject}">
        <semui:crumb class="active" controller="survey" action="show" id="${sourceObject.surveyInfo.id}"
                     params="[surveyConfigID: sourceObject.id]" text="${sourceObject.surveyInfo.name}" />
    </g:if>
</semui:breadcrumbs>

        <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code: 'copyElementsIntoObject.label', args: [message(code: "${sourceObject.getClass().getSimpleName().toLowerCase()}.label")])} </h1>
    <semui:messages data="${flash}"/>

    <% Map params = [:];
        if (sourceObjectId)   params << [sourceObjectId: genericOIDService.getOID(sourceObject)];
        if (targetObjectId)   params << [targetObjectId: genericOIDService.getOID(targetObject)];
    %>
        <semui:subNav showInTabular="true">
            <semui:complexSubNavItem class="${workFlowPart == CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS ? 'active' : ''}" controller="survey" action="copyElementsIntoSurvey" params="${params << [workFlowPart: CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS]}" >
                <div class="content" >
                    <div class="title">${message(code: 'copyElementsIntoObject.general_data.label')}</div>
                    <div class="description">
                        <i class="calendar alternate outline icon"></i>${message(code: 'subscription.periodOfValidity.label')}
                    </div>
                </div>
            </semui:complexSubNavItem>

            <semui:complexSubNavItem class="${workFlowPart == CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS ? 'active' : ''}" controller="survey" action="copyElementsIntoSurvey" params="${params << [workFlowPart: CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS]}" >
                <div class="content">
                    <div class="title">${message(code: 'copyElementsIntoObject.attachements.label')}</div>
                    <div class="description">
                        <i class="file outline icon"></i>${message(code: 'default.documents.label')}
                        <i class="sticky note outline icon"></i>${message(code: 'default.notes.label')}
                        <i class="checked calendar icon"></i>${message(code: 'menu.institutions.tasks')}
                    </div>
                </div>
            </semui:complexSubNavItem>

            <semui:complexSubNavItem class="${workFlowPart == CopyElementsService.WORKFLOW_SUBSCRIBER ? 'active' : ''}" controller="survey" action="copyElementsIntoSurvey" params="${params << [workFlowPart: CopyElementsService.WORKFLOW_SUBSCRIBER]}" >
                    <div class="content">
                        <div class="title">
                            ${message(code: 'consortium.subscriber')}
                        </div>
                        <div class="description">
                            <i class="university icon"></i>${message(code: 'consortium.subscriber')}
                        </div>
                    </div>
            </semui:complexSubNavItem>

            <semui:complexSubNavItem class="${workFlowPart == CopyElementsService.WORKFLOW_PROPERTIES ? 'active' : ''}" controller="survey" action="copyElementsIntoSurvey" params="${params << [workFlowPart: CopyElementsService.WORKFLOW_PROPERTIES]}" >
                <div class="content">
                    <div class="title">${message(code: 'properties')}</div>
                    <div class="description">
                        <i class="tags icon"></i>${message(code: 'properties')}
                    </div>

                </div>
            </semui:complexSubNavItem>
        </semui:subNav>
    <br />
    <div class="la-legend">
        <span class="la-key"><strong>${message(code: 'copyElementsIntoObject.legend.key')}: </strong></span>
        <span class="la-added">${message(code: 'copyElementsIntoObject.legend.willStay')}</span>
        <span class="la-removed">${message(code: 'copyElementsIntoObject.legend.willBeReplaced')}</span>
    </div>
    <g:if test="${workFlowPart == CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS}">
        <g:render template="/templates/copyElements/copyDocsAndTasks" />
    </g:if>
    <g:elseif test="${workFlowPart == CopyElementsService.WORKFLOW_SUBSCRIBER && targetObject && targetObject.surveyInfo.status == RDStore.SURVEY_IN_PROCESSING}">
        <g:render template="/templates/copyElements/copySurveyParticipants" />
    </g:elseif>
    <g:elseif test="${workFlowPart == CopyElementsService.WORKFLOW_PROPERTIES}">
        <g:render template="/templates/copyElements/copyPropertiesCompare" />
    </g:elseif>
    <g:else>
        <g:render template="/templates/copyElements/copyElements" />
    </g:else>
    <g:render template="/templates/copyElements/copyElementsJS"/>
</body>
</html>
