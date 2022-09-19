<%@ page import="de.laser.Person; de.laser.RefdataValue; de.laser.SubscriptionController; de.laser.CopyElementsService" %>

<laser:htmlStart text="${message(code: 'copyElementsIntoObject.label', args: [message(code: "${sourceObject.getClass().getSimpleName().toLowerCase()}.label")])}" serviceInjection="true"/>

<ui:breadcrumbs>
    <ui:crumb text="${message(code:'license.current')}" controller="myInstitution" action="currentLicenses" />

    <g:if test="${sourceObject}">
        <ui:crumb class="active" controller="license" id="${sourceObject.id}" action="show" text="${sourceObject.dropdownNamingConvention()}" />
    </g:if>
</ui:breadcrumbs>

<ui:h1HeaderWithIcon text="${message(code: 'copyElementsIntoObject.label', args: [message(code: "${sourceObject.getClass().getSimpleName().toLowerCase()}.label")])}" />

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
            <div class="title">${message(code: 'copyElementsIntoObject.general_data.label')}</div>

            <div class="description">
                <i class="calendar alternate outline icon"></i>${message(code: 'subscription.periodOfValidity.label')}
                <i class="ellipsis vertical icon"></i>${message(code: 'license.status.label')}
                <br />
                <i class="cloud icon"></i>${message(code: 'default.url.label')}
                <i class="clipboard list icon"></i>${message(code: 'license.licenseCategory.label')}
                <br />
                <i class="shipping fast icon"></i>${message(code: 'license.isPublicForApi.label')}
                <br />
                <i class="university icon"></i>${message(code: 'subscription.organisations.label')}
                <i class="barcode icon"></i>${message(code: 'default.identifiers.label')}

            </div>
        </div>
    </ui:complexSubNavItem>

    <ui:complexSubNavItem
            class="${workFlowPart == CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS ? 'active' : ''}"
            controller="license" action="copyElementsIntoLicense"
            params="${params << [workFlowPart: CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS]}">
        <div class="content">
            <div class="title">${message(code: 'copyElementsIntoObject.attachements.label')}</div>

            <div class="description">
                <i class="file outline icon"></i>${message(code: 'default.documents.label')}
                <i class="sticky note outline icon"></i>${message(code: 'default.notes.label')}
                <i class="calendar check outline icon"></i>${message(code: 'menu.institutions.tasks')}
            </div>
        </div>
    </ui:complexSubNavItem>

%{--    <g:if test="${isSubscriberVisible && accessService.checkPermAffiliation("ORG_CONSORTIUM", "INST_EDITOR")}">
        <ui:complexSubNavItem class="${workFlowPart == CopyElementsService.WORKFLOW_SUBSCRIBER ? 'active' : ''}"
                                 controller="license" action="copyElementsIntoLicense"
                                 params="${params << [workFlowPart: CopyElementsService.WORKFLOW_SUBSCRIBER]}">
            <div class="content">
                <div class="title">
                    ${message(code: 'consortium.subscriber')}
                </div>

                <div class="description">
                    <i class="university icon"></i>${message(code: 'consortium.subscriber')}
                </div>
            </div>
        </ui:complexSubNavItem>
    </g:if>--}%

    <ui:complexSubNavItem class="${workFlowPart == CopyElementsService.WORKFLOW_PROPERTIES ? 'active' : ''}"
                             controller="license" action="copyElementsIntoLicense"
                             params="${params << [workFlowPart: CopyElementsService.WORKFLOW_PROPERTIES]}">
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
    <span class="la-key"><strong>${message(code: 'copyElementsIntoObject.legend.key')}:</strong></span>
    <span class="la-added">${message(code: 'copyElementsIntoObject.legend.willStay')}</span>
    <span class="la-removed">${message(code: 'copyElementsIntoObject.legend.willBeReplaced')}</span>
</div>
<g:if test="${workFlowPart == CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS}">
    <laser:render template="/templates/copyElements/copyDocsAndTasks"/>
</g:if>
%{--<g:elseif test="${workFlowPart == CopyElementsService.WORKFLOW_SUBSCRIBER && isSubscriberVisible && accessService.checkPermAffiliation("ORG_CONSORTIUM", "INST_EDITOR")}">
    <laser:render template="/templates/copyElements/copySubscriber"/>
</g:elseif>--}%
<g:elseif test="${workFlowPart == CopyElementsService.WORKFLOW_PROPERTIES}">
    <laser:render template="/templates/copyElements/copyPropertiesCompare"/>
</g:elseif>
<g:else>
    <laser:render template="/templates/copyElements/copyElements"/>
</g:else>
<laser:render template="/templates/copyElements/copyElementsJS"/>

<laser:htmlEnd />
