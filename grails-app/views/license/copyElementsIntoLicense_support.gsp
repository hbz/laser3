<%@ page import="de.laser.Person; de.laser.RefdataValue; de.laser.SubscriptionController; de.laser.CopyElementsService" %>

<laser:htmlStart message="copyElementsIntoObject.license" serviceInjection="true"/>

[DEBUG: Support-Template]

<ui:breadcrumbs>
    <ui:crumb text="${message(code:'license.current')}" controller="myInstitution" action="currentLicenses" />

    <g:if test="${sourceObject}">
        <ui:crumb class="active" controller="license" id="${sourceObject.id}" action="show" text="${sourceObject.dropdownNamingConvention()}" />
    </g:if>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon message="copyElementsIntoObject.license" />

<ui:messages data="${flash}"/>

<% Map params = [:];
if (sourceObjectId) params << [sourceObjectId: genericOIDService.getOID(sourceObject)]
if (targetObjectId) params << [targetObjectId: genericOIDService.getOID(targetObject)]
%>
<ui:subNav showInTabular="true">
    <ui:complexSubNavItem
            class="${workFlowPart == CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS ? 'active' : ''}"
            controller="license" action="copyElementsIntoLicense"
            params="${params << [workFlowPart: CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS]}">
        <div class="content">
            <div class="title">
                ${message(code: 'copyElementsIntoObject.general_data.label')}
            </div>
            <div class="description">
                <i class="calendar alternate outline icon"></i> ${message(code: 'subscription.periodOfValidity.label')} <br />
                <i class="ellipsis vertical icon"></i>          ${message(code: 'license.status.label')} <br />
%{--                <i class="cloud icon"></i>                      ${message(code: 'default.url.label')} <br />--}%
                <i class="clipboard list icon"></i>             ${message(code: 'license.licenseCategory.label')} <br />
                <i class="key icon"></i>                    ${message(code: 'license.openEnded.label')} <br />
                <i class="barcode icon"></i>                    ${message(code: 'default.identifiers.label')} <br />
                <i class="exchange icon"></i>                   ${message(code: 'license.linkedObjects')}
            </div>
        </div>
    </ui:complexSubNavItem>

    <ui:complexSubNavItem
            class="${workFlowPart == CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS ? 'active' : ''}"
            controller="license" action="copyElementsIntoLicense"
            params="${params << [workFlowPart: CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS]}">
        <div class="content">
            <div class="title">
                ${message(code: 'copyElementsIntoObject.attachements.label')}
            </div>
            <div class="description">
                <i class="sticky note outline icon"></i>    ${message(code: 'default.notes.label')} <br />
                <i class="calendar check outline icon"></i> ${message(code: 'menu.institutions.tasks')} <br />
                <i class="file outline icon"></i>           ${message(code: 'default.documents.label')} <br />
                <i class="tasks icon"></i>                  ${message(code: 'workflow.plural')}
            </div>
        </div>
    </ui:complexSubNavItem>

%{--    <g:if test="${isSubscriberVisible && contextService.isInstEditor_or_ROLEADMIN(CustomerTypeService.ORG_CONSORTIUM_BASIC)}">
        <ui:complexSubNavItem class="${workFlowPart == CopyElementsService.WORKFLOW_SUBSCRIBER ? 'active' : ''}"
                                 controller="license" action="copyElementsIntoLicense"
                                 params="${params << [workFlowPart: CopyElementsService.WORKFLOW_SUBSCRIBER]}">
            <div class="content">
                <div class="title">
                    ${message(code: 'consortium.subscriber')}
                </div>
                <div class="description">
                    <i class="university icon"></i> ${message(code: 'consortium.subscriber')}
                </div>
            </div>
        </ui:complexSubNavItem>
    </g:if>--}%

    <ui:complexSubNavItem class="${workFlowPart == CopyElementsService.WORKFLOW_PROPERTIES ? 'active' : ''}"
                             controller="license" action="copyElementsIntoLicense"
                             params="${params << [workFlowPart: CopyElementsService.WORKFLOW_PROPERTIES]}">
        <div class="content">
            <div class="title">
                ${message(code: 'properties')}
            </div>
            <div class="description">
                <i class="tags icon"></i> ${message(code: 'properties')}
            </div>
        </div>
    </ui:complexSubNavItem>
</ui:subNav>

<div class="ui bottom attached segment">

    <g:set var="validWorkflow" value="${workFlowPart in [CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS, CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS, CopyElementsService.WORKFLOW_PROPERTIES]}" />

    <g:if test="${validWorkflow}">
        <laser:render template="/templates/copyElements/legend"/>

        <g:if test="${workFlowPart == CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS}">
            <laser:render template="/templates/copyElements/copyElements"/>
        </g:if>
        <g:elseif test="${workFlowPart == CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS}">
            <laser:render template="/templates/copyElements/copyDocsAndTasksAndWorkflows"/>
        </g:elseif>
        %{--<g:elseif test="${workFlowPart == CopyElementsService.WORKFLOW_SUBSCRIBER && isSubscriberVisible && contextService.isInstEditor_or_ROLEADMIN(CustomerTypeService.ORG_CONSORTIUM_BASIC)}">
            <laser:render template="/templates/copyElements/copySubscriber"/>
        </g:elseif>--}%
        <g:elseif test="${workFlowPart == CopyElementsService.WORKFLOW_PROPERTIES}">
            <laser:render template="/templates/copyElements/copyPropertiesCompare"/>
        </g:elseif>

        <laser:render template="/templates/copyElements/copyElementsJS"/>
    </g:if>
    <g:else>
        <ui:msg class="error" text="Ungültiger Seitenaufruf" />
    </g:else>
</div>

<laser:htmlEnd />
