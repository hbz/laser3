<%@ page import="de.laser.ui.Icon; de.laser.CopyElementsService;" %>
<laser:htmlStart message="myinst.copyLicense" />

<ui:breadcrumbs>
    <ui:crumb controller="myInstitution" action="currentLicenses" message="license.current" />

    <g:if test="${sourceObject}">
        <ui:crumb action="show" controller="license" id="${sourceObject.id}" text="${sourceObject.reference}" />
        <ui:crumb class="active" message="myinst.copyLicense" />
    </g:if>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon text="${message(code: 'myinst.copyLicense')}: ${sourceObject.reference}" />

<ui:messages data="${flash}"/>

<% Map params = [:]
if (sourceObjectId) params << [sourceObjectId: genericOIDService.getOID(sourceObject)]
if (targetObjectId)   params << [targetObjectId: genericOIDService.getOID(targetObject)]
%>

<div class="ui tablet stackable steps la-clear-before">
    <div class="${workFlowPart == CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS ? 'active' : (workFlowPart in [CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS , CopyElementsService.WORKFLOW_PROPERTIES] ? 'completed' : '')} step">
        <div class="content">
            <div class="title">
                ${message(code: 'copyElementsIntoObject.general_data.label')}
            </div>
            <div class="description">
                <i class="${Icon.SYM.DATE}"></i>                ${message(code: 'subscription.periodOfValidity.label')} <br />
                <i class="${Icon.SYM.STATUS}"></i>              ${message(code: 'license.status.label')} <br />
%{--                <i class="${Icon.SYM.URL}"></i>                      ${message(code: 'default.url.label')} <br />--}%
                <i class="${Icon.ATTR.LICENSE_CATEGORY}"></i>   ${message(code: 'license.licenseCategory.label')} <br />
                <i class="key icon"></i>                        ${message(code: 'license.openEnded.label')} <br />
                <i class="${Icon.IDENTIFIER}"></i>              ${message(code: 'default.identifiers.label')} <br />
                <i class="${Icon.SYM.LINKED_OBJECTS}"></i>      ${message(code: 'license.linkedObjects')}
            </div>
        </div>
    </div>
    <div class="${workFlowPart == CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS ? 'active' : (workFlowPart in [CopyElementsService.WORKFLOW_PROPERTIES] ? 'completed' : '')} step">
        <div class="content">
            <div class="title">
                ${message(code: 'copyElementsIntoObject.attachements.label')}
            </div>
            <div class="description">
                <i class="${Icon.SYM.NOTE}"></i>      ${message(code: 'default.notes.label')} <br />
                <i class="${Icon.TASK}"></i>          ${message(code: 'menu.institutions.tasks')} <br />
                <i class="${Icon.DOCUMENT}"></i>      ${message(code: 'default.documents.label')} <br />
                <i class="${Icon.WORKFLOW}"></i>      ${message(code: 'workflow.plural')}
            </div>
        </div>
    </div>
    <div class="${workFlowPart == CopyElementsService.WORKFLOW_PROPERTIES ? 'active' : ''} step">
        <div class="content">
            <div class="title">
                ${message(code: 'properties')}
            </div>
            <div class="description">
                <i class="${Icon.SYM.PROPERTIES}"></i> ${message(code: 'properties')}
            </div>
        </div>
    </div>
</div>

<ui:greySegment>
    <g:if test="${workFlowPart == CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS}">
        <laser:render template="/templates/copyElements/copyElements" />
    </g:if>
    <g:elseif test="${workFlowPart == CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS}">
        <laser:render template="/templates/copyElements/copyDocsAndTasksAndWorkflows" />
    </g:elseif>
    <g:elseif test="${workFlowPart == CopyElementsService.WORKFLOW_PROPERTIES}">
        <laser:render template="/templates/copyElements/copyPropertiesCompare" />
    </g:elseif>

    <laser:render template="/templates/copyElements/copyElementsJS"/>
</ui:greySegment>

<laser:htmlEnd />