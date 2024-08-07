<%@ page import="de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.Person; de.laser.RefdataValue; de.laser.SubscriptionController; de.laser.CopyElementsService;de.laser.storage.RDStore;de.laser.PendingChangeConfiguration;" %>
<laser:htmlStart message="myinst.copySubscription" serviceInjection="true" />

<ui:breadcrumbs>
    <ui:crumb controller="myInstitution" action="currentSubscriptions" text="${message(code:'myinst.currentSubscriptions.label')}" />

    <g:if test="${sourceObject}">
        <ui:crumb action="show" controller="subscription" id="${sourceObject.id}" text="${sourceObject.name}" />
        <ui:crumb class="active" text="${message(code: 'myinst.copySubscription')}" />
    </g:if>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon text="${message(code: 'myinst.copySubscription')}: ${sourceObject.name}" />

<ui:messages data="${flash}"/>

<% Map params = [:]
if (sourceObjectId) params << [sourceObjectId: genericOIDService.getOID(sourceObject)]
if (targetObjectId)   params << [targetObjectId: genericOIDService.getOID(targetObject)]
%>

<div class="ui tablet stackable steps la-clear-before">
    <div class="${workFlowPart == CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS ? 'active' : (workFlowPart in [CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS , CopyElementsService.WORKFLOW_PROPERTIES, CopyElementsService.WORKFLOW_PACKAGES_ENTITLEMENTS, CopyElementsService.WORKFLOW_SUBSCRIBER] ? 'completed' : '')} step">
            <div class="content" >
                <div class="title">
                    ${message(code: 'copyElementsIntoObject.general_data.label')}
                </div>
                <div class="description">
                    <i class="${Icon.SYM.ALTNAME}"></i>                 ${message(code: 'altname.plural')} <br />
                    <i class="${Icon.ATTR.SUBSCRIPTION_KIND}"></i>      ${message(code:'subscription.kind.label')} <br />
                    <i class="${Icon.ATTR.SUBSCRIPTION_FORM}"></i>      ${message(code:'subscription.form.label')} <br />
                    <i class="${Icon.ATTR.SUBSCRIPTION_RESOURCE}"></i>  ${message(code:'subscription.resource.label')} <br />
                    <i class="${Icon.SYM.IS_PUBLIC}"></i>               ${message(code: 'subscription.isPublicForApi.label')} <br />
                    <i class="${Icon.ATTR.SUBSCRIPTION_HAS_PERPETUAL_ACCESS}"></i>  ${message(code:'subscription.hasPerpetualAccess.label')} <br />
                    <i class="${Icon.ATTR.SUBSCRIPTION_HAS_PUBLISH_COMPONENT}"></i> ${message(code:'subscription.hasPublishComponent.label')} <br />
                    <i class="${Icon.LICENSE}"></i>             ${message(code: 'license.label')} <br />
                    <i class="${Icon.ORG}"></i>                 ${message(code: 'subscription.organisations.label')} <br />
                    <i class="${Icon.ACP_PUBLIC}"></i>          ${message(code: 'subscription.specificSubscriptionEditors')} <br />
                    <i class="${Icon.IDENTIFIER}"></i>          ${message(code: 'default.identifiers.label')} <br />
                    <i class="${Icon.SYM.LINKED_OBJECTS}"></i>  ${message(code: 'subscription.linkedObjects')}
                </div>
            </div>
    </div>
    <div class="${workFlowPart == CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS ? 'active' : (workFlowPart in [CopyElementsService.WORKFLOW_PROPERTIES, CopyElementsService.WORKFLOW_SUBSCRIBER] ? 'completed' : '')} step">
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
    <g:if test="${isConsortialObjects && contextService.isInstUser_or_ROLEADMIN(CustomerTypeService.ORG_CONSORTIUM_BASIC)}">
        <div class="${workFlowPart == CopyElementsService.WORKFLOW_SUBSCRIBER ? 'active' : (workFlowPart in [CopyElementsService.WORKFLOW_PROPERTIES, CopyElementsService.WORKFLOW_PACKAGES_ENTITLEMENTS] ? 'completed' : '')} step">
            <div class="content">
                <div class="title">
                    ${message(code: 'consortium.subscriber')}
                </div>
                <div class="description">
                    <i class="${Icon.ORG}"></i> ${message(code: 'consortium.subscriber')}
                </div>
            </div>
        </div>
    </g:if>
    <div class="${workFlowPart == CopyElementsService.WORKFLOW_PACKAGES_ENTITLEMENTS ? 'active' : (workFlowPart in [CopyElementsService.WORKFLOW_PROPERTIES] ? 'completed' : '')} step">
        <div class="content" >
            <div class="title">${message(code: 'copyElementsIntoObject.inventory.label')}</div>
            <div class="description">
                <i class="${Icon.PACKAGE}"></i>    ${message(code: 'package.label')} <br />
                <i class="${Icon.TIPP}"></i>       ${message(code: 'title')} <br />
                <i class="${Icon.IE_GROUP}"></i>   ${message(code: 'subscription.details.ieGroups')}
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
    <g:if test="${workFlowPart == CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS}">
        <laser:render template="/templates/copyElements/copyDocsAndTasksAndWorkflows" />
    </g:if>
    <g:elseif test="${workFlowPart == CopyElementsService.WORKFLOW_PROPERTIES}">
        <laser:render template="/templates/copyElements/copyPropertiesCompare" />
    </g:elseif>
    <g:elseif test="${workFlowPart == CopyElementsService.WORKFLOW_PACKAGES_ENTITLEMENTS}">
        <laser:render template="/templates/copyElements/copyPackagesAndIEs" />
    </g:elseif>
    <g:elseif test="${workFlowPart == CopyElementsService.WORKFLOW_SUBSCRIBER && isConsortialObjects && contextService.isInstEditor_or_ROLEADMIN(CustomerTypeService.ORG_CONSORTIUM_BASIC)}">
        <laser:render template="/templates/copyElements/copySubscriber" />
    </g:elseif>
    <g:else>
        <laser:render template="/templates/copyElements/copyElements" />
    </g:else>

    <laser:render template="/templates/copyElements/copyElementsJS"/>
</ui:greySegment>

<laser:htmlEnd />