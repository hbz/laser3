<%@ page import="de.laser.Person; de.laser.RefdataValue; de.laser.SubscriptionController; de.laser.CopyElementsService" %>
<laser:htmlStart message="subscription.details.copyMyElements.label" serviceInjection="true" />

<laser:render template="breadcrumb" model="${[params: params]}"/>

<ui:h1HeaderWithIcon message="subscription.details.copyMyElements.label" />

<ui:messages data="${flash}"/>

<% Map params = [:];
if (sourceObjectId) params << [sourceObjectId: genericOIDService.getOID(sourceObject)];
if (targetObjectId) params << [targetObjectId: genericOIDService.getOID(targetObject)];
%>

<ui:subNav>

    <ui:complexSubNavItem class="${workFlowPart == CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS ? 'active' : ''}"
                             controller="subscription" action="copyMyElements"
                             params="${params << [workFlowPart: CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS]}">
        <div class="content">
            <div class="title">${message(code: 'copyElementsIntoObject.attachements.label')}</div>

            <div class="description">
                <i class="file outline icon"></i>${message(code: 'default.documents.label')}
                <i class="sticky note outline icon"></i>${message(code: 'default.notes.label')}
                <i class="checked calendar icon"></i>${message(code: 'menu.institutions.tasks')}
            </div>
        </div>
    </ui:complexSubNavItem>

    <ui:complexSubNavItem class="${workFlowPart == CopyElementsService.WORKFLOW_PROPERTIES ? 'active' : ''}" controller="subscription"
                             action="copyMyElements" params="${params << [workFlowPart: CopyElementsService.WORKFLOW_PROPERTIES]}">
        <div class="content">
            <div class="title">${message(code: 'properties')}</div>

            <div class="description">
                <i class="tags icon"></i>${message(code: 'properties')}
            </div>

        </div>
    </ui:complexSubNavItem>
</ui:subNav>

<br />

<div class="la-legend">
    <span class="la-key"><strong>${message(code: 'copyElementsIntoObject.legend.key')}:</strong>
    </span>
    <span class="la-added">${message(code: 'copyElementsIntoObject.legend.willStay')}</span>
    <span class="la-removed">${message(code: 'copyElementsIntoObject.legend.willBeReplaced')}</span>
</div>
<g:if test="${workFlowPart == CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS}">
    <laser:render template="/templates/copyElements/copyDocsAndTasks"/>
</g:if>
<g:elseif test="${workFlowPart == CopyElementsService.WORKFLOW_PROPERTIES}">
    <laser:render template="/templates/copyElements/copyPropertiesCompare"/>
</g:elseif>
<laser:render template="/templates/copyElements/copyElementsJS"/>

<laser:htmlEnd />
