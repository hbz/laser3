<%@ page import="de.laser.Person; de.laser.RefdataValue; de.laser.SubscriptionController; de.laser.CopyElementsService;de.laser.storage.RDStore;de.laser.PendingChangeConfiguration;" %>
<laser:serviceInjection />
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'myinst.copySubscription')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="currentSubscriptions" text="${message(code:'myinst.currentSubscriptions.label')}" />

    <g:if test="${sourceObject}">
        <semui:crumb action="show" controller="subscription" id="${sourceObject.id}" text="${sourceObject.name}" />
        <semui:crumb class="active" text="${message(code: 'myinst.copySubscription')}" />
    </g:if>
</semui:breadcrumbs>

<semui:headerWithIcon text="${message(code: 'myinst.copySubscription')}: ${sourceObject.name}" />

<semui:messages data="${flash}"/>

<% Map params = [:]
if (sourceObjectId) params << [sourceObjectId: genericOIDService.getOID(sourceObject)]
if (targetObjectId)   params << [targetObjectId: genericOIDService.getOID(targetObject)]
%>

<div class="ui tablet stackable steps la-clear-before">
    <div class="${workFlowPart == CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS ? 'active' : (workFlowPart in [CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS , CopyElementsService.WORKFLOW_PROPERTIES, CopyElementsService.WORKFLOW_PACKAGES_ENTITLEMENTS, CopyElementsService.WORKFLOW_SUBSCRIBER] ? 'completed' : '')} step">
        <i class=" icon"></i>
            <div class="content" >
                <div class="title">
                        ${message(code: 'copyElementsIntoObject.general_data.label')}
                </div>
                <div class="description">
                    <i class="image outline icon"></i>${message(code:'subscription.kind.label')}
                    <i class="dolly icon"></i>${message(code:'subscription.form.label')}
                    <i class="box icon"></i>${message(code:'subscription.resource.label')}
                    <br />
                    <i class="shipping fast icon"></i>${message(code:'subscription.isPublicForApi.label')}
                    <i class="flag outline icon"></i>${message(code:'subscription.hasPerpetualAccess.label')}
                    <i class="comment icon"></i>${message(code:'subscription.hasPublishComponent.label')}
                    <br />
                    <i class="balance scale icon"></i>${message(code: 'license.label')}
                    <i class="university icon"></i>${message(code: 'subscription.organisations.label')}
                    <i class="address card icon"></i>${message(code: 'subscription.specificSubscriptionEditors')}
                    <br />
                    <i class="barcode icon"></i>${message(code: 'default.identifiers.label')}
                    <i class="exchange icon"></i>${message(code: 'subscription.linkedObjects')}
                </div>
        </div>
    </div>
    <div class="${workFlowPart == CopyElementsService.WORKFLOW_PACKAGES_ENTITLEMENTS ? 'active' : (workFlowPart in [CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS, CopyElementsService.WORKFLOW_PROPERTIES, CopyElementsService.WORKFLOW_SUBSCRIBER] ? 'completed' : '')} step">
        <i class=" icon"></i>
        <div class="content" >
            <div class="title">${message(code: 'copyElementsIntoObject.inventory.label')}</div>
            <div class="description">
                <i class="gift icon"></i>${message(code: 'package.label')}
                <i class="book icon"></i>${message(code: 'title')}
                <br />
                <i class="icon object group"></i>${message(code: 'subscription.details.ieGroups')}
            </div>
        </div>
    </div>
    <div class="${workFlowPart == CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS ? 'active' : (workFlowPart in [CopyElementsService.WORKFLOW_PROPERTIES, CopyElementsService.WORKFLOW_SUBSCRIBER] ? 'completed' : '')} step">
        <i class=" icon"></i>
        <div class="content">
            <div class="title">
                    ${message(code: 'copyElementsIntoObject.attachements.label')}
            </div>
            <div class="description">
                <i class="file outline icon"></i>${message(code: 'default.documents.label')}
                <i class="sticky note outline icon"></i>${message(code: 'default.notes.label')}
                <i class="checked calendar icon"></i>${message(code: 'menu.institutions.tasks')}
            </div>
        </div>
    </div>
    <g:if test="${isConsortialObjects && accessService.checkPermAffiliation("ORG_CONSORTIUM", "INST_USER")}">
        <div class="${workFlowPart == CopyElementsService.WORKFLOW_SUBSCRIBER ? 'active' : (workFlowPart in [CopyElementsService.WORKFLOW_PROPERTIES] ? 'completed' : '')} step">
            <i class=" icon"></i>
            <div class="content">
                <div class="title">
                        ${message(code: 'consortium.subscriber')}
                </div>
                <div class="description">
                    <i class="university icon"></i>${message(code: 'consortium.subscriber')}
                </div>
            </div>
        </div>
    </g:if>
    <div class="${workFlowPart == CopyElementsService.WORKFLOW_PROPERTIES ? 'active' : ''} step">
        <i class=" icon"></i>
        <div class="content">
            <div class="title">
                    ${message(code: 'properties')}
            </div>
            <div class="description">
                <i class="tags icon"></i>${message(code: 'properties')}
            </div>
        </div>
    </div>
</div>

<g:if test="${workFlowPart == CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS}">
    <laser:render template="/templates/copyElements/copyDocsAndTasks" />
</g:if>
<g:elseif test="${workFlowPart == CopyElementsService.WORKFLOW_PROPERTIES}">
    <laser:render template="/templates/copyElements/copyPropertiesCompare" />
</g:elseif>
<g:elseif test="${workFlowPart == CopyElementsService.WORKFLOW_PACKAGES_ENTITLEMENTS}">
    <laser:render template="/templates/copyElements/copyPackagesAndIEs" />
</g:elseif>
<g:elseif test="${workFlowPart == CopyElementsService.WORKFLOW_SUBSCRIBER && isConsortialObjects && accessService.checkPermAffiliation("ORG_CONSORTIUM", "INST_EDITOR")}">
    <laser:render template="/templates/copyElements/copySubscriber" />
</g:elseif>
<g:else>
    <laser:render template="/templates/copyElements/copyElements" />
</g:else>
<laser:render template="/templates/copyElements/copyElementsJS"/>

</body>
</html>