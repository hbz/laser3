<%@ page import="de.laser.CustomerTypeService; de.laser.survey.SurveyConfig; de.laser.Person; de.laser.RefdataValue; de.laser.SubscriptionController; de.laser.CopyElementsService;" %>

    <g:if test="${isRenewSub}">
        <g:set var="pageTitle" value="${message(code: 'subscription.details.renewals.renew_sub.label')}" />
    </g:if>
    <g:else>
        <g:set var="pageTitle" value="${message(code: 'copyElementsIntoObject.subscription')}" />
    </g:else>

<laser:htmlStart text="${pageTitle}" serviceInjection="true" />

    <g:if test="${fromSurvey}">
        <ui:breadcrumbs>
            <ui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code:'menu.my.surveys')}" />

            <g:if test="${sourceObject}">
                <g:set var="surveyConfig" value="${SurveyConfig.findBySubscriptionAndSubSurveyUseForTransfer(sourceObject, true)}"/>
                <ui:crumb controller="survey" action="renewalEvaluation" id="${surveyConfig.surveyInfo.id}" params="[surveyConfigID: surveyConfig.id]" text="${surveyConfig.surveyInfo.name}" />
            </g:if>

            <ui:crumb class="active" message="subscription.details.renewals.renew_sub.label" />
        </ui:breadcrumbs>
    </g:if>
    <g:else>
        <ui:breadcrumbs>
            <ui:crumb controller="myInstitution" action="currentSubscriptions" text="${message(code:'myinst.currentSubscriptions.label')}" />

            <g:if test="${sourceObject}">
                <ui:crumb class="active" controller="subscription" action="show" id="${sourceObject.id}" text="${sourceObject.name}" />
            </g:if>
        </ui:breadcrumbs>
    </g:else>

    <ui:h1HeaderWithIcon>
    <g:if test="${isRenewSub}">
        ${message(code: 'subscription.details.renewals.renew_sub.label')}: <g:if test="${sourceObject}">${sourceObject.name}</g:if>
    </g:if>
    <g:else>
        ${message(code: 'copyElementsIntoObject.subscription')}
    </g:else>
    </ui:h1HeaderWithIcon>

    <ui:messages data="${flash}"/>

    <% Map params = [:]
        if (sourceObjectId)   params << [sourceObjectId: genericOIDService.getOID(sourceObject)]
        if (targetObjectId)   params << [targetObjectId: genericOIDService.getOID(targetObject)]
        if (isRenewSub)             params << [isRenewSub: isRenewSub]
    %>
    <g:if test="${isRenewSub}">
        <div class="ui tablet stackable steps la-clear-before">
            <div class="${workFlowPart == CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS ? 'active' : (workFlowPart in [CopyElementsService.WORKFLOW_PACKAGES_ENTITLEMENTS, CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS , CopyElementsService.WORKFLOW_SUBSCRIBER, CopyElementsService.WORKFLOW_PROPERTIES] ? 'completed' : '')} step">
                <div class="content">
                    <div class="title">
                        <g:link controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: null]}">
                            ${message(code: 'copyElementsIntoObject.general_data.label')}
                        </g:link>
                    </div>
                    <div class="description">
                        <i class="image outline icon"></i>  ${message(code:'subscription.kind.label')}
                        <i class="dolly icon"></i>          ${message(code:'subscription.form.label')}
                        <i class="box icon"></i>            ${message(code:'subscription.resource.label')} <br />
                        <i class="shipping fast icon"></i>  ${message(code:'subscription.isPublicForApi.label')}
                        <i class="flag outline icon"></i>   ${message(code:'subscription.hasPerpetualAccess.label')}
                        <i class="comment icon"></i>        ${message(code:'subscription.hasPublishComponent.label')} <br />
                        <i class="pizza slice icon"></i>    ${message(code:'subscription.holdingSelection.label')}
                        <i class="balance scale icon"></i>  ${message(code: 'license.label')}
                        <i class="university icon"></i>     ${message(code: 'subscription.organisations.label')} <br />
                        <i class="address card icon"></i>   ${message(code: 'subscription.specificSubscriptionEditors')}
                        <i class="barcode icon"></i>        ${message(code: 'default.identifiers.label')}
                        <i class="exchange icon"></i>       ${message(code: 'subscription.linkedObjects')}
                    </div>
                </div>
            </div>
            <div class="${workFlowPart == CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS ? 'active' : (workFlowPart in [CopyElementsService.WORKFLOW_PACKAGES_ENTITLEMENTS, CopyElementsService.WORKFLOW_SUBSCRIBER, CopyElementsService.WORKFLOW_PROPERTIES] ? 'completed' : '')} step">
                <div class="content">
                    <div class="title">
                        <g:link controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS]}">
                            ${message(code: 'copyElementsIntoObject.attachements.label')}
                        </g:link>
                    </div>
                    <div class="description">
                        <i class="sticky note outline icon"></i>    ${message(code: 'default.notes.label')} <br />
                        <i class="calendar check outline icon"></i> ${message(code: 'menu.institutions.tasks')} <br />
                        <i class="file outline icon"></i>           ${message(code: 'default.documents.label')} <br />
%{--                        <i class="tasks icon"></i>                  ${message(code: 'workflow.plural')}--}%
                    </div>
                </div>
            </div>

            <g:if test="${!fromSurvey && isSubscriberVisible && contextService.hasPermAsInstUser_or_ROLEADMIN(CustomerTypeService.ORG_CONSORTIUM_BASIC)}">
                <div class="${workFlowPart == CopyElementsService.WORKFLOW_SUBSCRIBER ? 'active' : (workFlowPart in [CopyElementsService.WORKFLOW_PACKAGES_ENTITLEMENTS, CopyElementsService.WORKFLOW_PROPERTIES] ? 'completed' : '')} step">
                    <div class="content">
                        <div class="title">
                            <g:link controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS]}">
                                ${message(code: 'consortium.subscriber')}
                            </g:link>
                        </div>
                        <div class="description">
                            <i class="university icon"></i> ${message(code: 'consortium.subscriber')}
                        </div>
                    </div>
                </div>
            </g:if>
            <div class="${workFlowPart == CopyElementsService.WORKFLOW_PACKAGES_ENTITLEMENTS ? 'active' : (workFlowPart in [CopyElementsService.WORKFLOW_PROPERTIES] ? 'completed' : '')} step">
                <div class="content">
                    <div class="title">
                        <g:link controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: CopyElementsService.WORKFLOW_SUBSCRIBER]}">
                        %{--<g:link controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: CopyElementsService.WORKFLOW_IDENTIFIERS]}">--}%
                            ${message(code: 'copyElementsIntoObject.inventory.label')}
                        </g:link>
                    </div>
                    <div class="description">
                        <i class="gift icon"></i>           ${message(code: 'package.label')} <br />
                        <i class="book icon"></i>           ${message(code: 'title')} <br />
                        <i class="icon object group"></i>   ${message(code: 'subscription.details.ieGroups')}
                    </div>
                </div>
            </div>

            <div class="${workFlowPart == CopyElementsService.WORKFLOW_PROPERTIES ? 'active' : ''} step">
                <div class="content">
                    <div class="title">
                        <g:link controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: CopyElementsService.WORKFLOW_PACKAGES_ENTITLEMENTS]}">
                            ${message(code: 'properties')}
                        </g:link>
                    </div>
                    <div class="description">
                        <i class="tags icon"></i> ${message(code: 'properties')}
                    </div>
                </div>
            </div>
        </div>
    </g:if>
    <g:else>
        <ui:subNav showInTabular="true">
            <ui:complexSubNavItem class="${workFlowPart == CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS ? 'active' : ''}" disabled="${transferIntoMember}" controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS]}" >
                <div class="content">
                    <div class="title">
                        ${message(code: 'copyElementsIntoObject.general_data.label')}
                    </div>
                    <div class="description">
                        <i class="calendar alternate outline icon"></i>     ${message(code: 'subscription.periodOfValidity.label')}
                        <i class="ellipsis vertical icon"></i>              ${message(code:'subscription.status.label')}
                        <i class="image outline icon"></i>                  ${message(code:'subscription.kind.label')} <br />
                        <i class="dolly icon"></i>                          ${message(code:'subscription.form.label')}
                        <i class="box icon"></i>                            ${message(code:'subscription.resource.label')}
                        <i class="shipping fast icon"></i>                  ${message(code:'subscription.isPublicForApi.label')} <br />
                        <i class="flag outline icon"></i>                   ${message(code:'subscription.hasPerpetualAccess.label')}
                        <i class="comment icon"></i>                        ${message(code:'subscription.hasPublishComponent.label')}
                        <g:if test="${sourceObject?.packages}">
                            <i class="pizza slice icon"></i>                ${message(code:'subscription.holdingSelection.label')} <br />
                        </g:if>
                        <i class="balance scale icon"></i>                  ${message(code: 'license.label')}
                        <i class="university icon"></i>                     ${message(code: 'subscription.organisations.label')}
                        <i class="address card icon"></i>                   ${message(code: 'subscription.specificSubscriptionEditors')} <br />
                        <i class="barcode icon"></i>                        ${message(code: 'default.identifiers.label')}
                    </div>
                </div>
            </ui:complexSubNavItem>

            <ui:complexSubNavItem class="${workFlowPart == CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS ? 'active' : ''}" controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS]}" >
                <div class="content">
                    <div class="title">
                        ${message(code: 'copyElementsIntoObject.attachements.label')}
                    </div>
                    <div class="description">
                        <i class="sticky note outline icon"></i>    ${message(code: 'default.notes.label')} <br />
                        <i class="calendar check outline icon"></i> ${message(code: 'menu.institutions.tasks')} <br />
                        <i class="file outline icon"></i>           ${message(code: 'default.documents.label')} <br />
%{--                        <i class="tasks icon"></i>                  ${message(code: 'workflow.plural')}--}%
                    </div>
                </div>
            </ui:complexSubNavItem>

            <g:if test="${isSubscriberVisible && contextService.hasPermAsInstEditor_or_ROLEADMIN(CustomerTypeService.ORG_CONSORTIUM_BASIC)}">
                <ui:complexSubNavItem class="${workFlowPart == CopyElementsService.WORKFLOW_SUBSCRIBER ? 'active' : ''}" controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: CopyElementsService.WORKFLOW_SUBSCRIBER]}" >
                    <div class="content">
                        <div class="title">
                            ${message(code: 'consortium.subscriber')}
                        </div>
                        <div class="description">
                            <i class="university icon"></i> ${message(code: 'consortium.subscriber')}
                        </div>
                    </div>
                </ui:complexSubNavItem>
            </g:if>

            <ui:complexSubNavItem class="${workFlowPart == CopyElementsService.WORKFLOW_PACKAGES_ENTITLEMENTS ? 'active' : ''}" disabled="${transferIntoMember}" controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: CopyElementsService.WORKFLOW_PACKAGES_ENTITLEMENTS]}" >
                <div class="content">
                    <div class="title">
                        ${message(code: 'copyElementsIntoObject.inventory.label')}
                    </div>
                    <div class="description">
                        <i class="gift icon"></i>           ${message(code: 'package.label')} <br />
                        <i class="book icon"></i>           ${message(code: 'title')} <br />
                        <i class="icon object group"></i>   ${message(code: 'subscription.details.ieGroups')}
                    </div>
                </div>
            </ui:complexSubNavItem>

            <ui:complexSubNavItem class="${workFlowPart == CopyElementsService.WORKFLOW_PROPERTIES ? 'active' : ''}" controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: CopyElementsService.WORKFLOW_PROPERTIES]}" >
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
    </g:else>

<div class="ui bottom attached segment">
    <laser:render template="/templates/copyElements/legend"/>

    <g:if test="${workFlowPart == CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS}">
        <laser:render template="/templates/copyElements/copyDocsAndTasks" />
    </g:if>
    <g:elseif test="${workFlowPart == CopyElementsService.WORKFLOW_SUBSCRIBER && isSubscriberVisible && contextService.hasPermAsInstEditor_or_ROLEADMIN(CustomerTypeService.ORG_CONSORTIUM_BASIC)}">
        <laser:render template="/templates/copyElements/copySubscriber" />
    </g:elseif>
    <g:elseif test="${workFlowPart == CopyElementsService.WORKFLOW_PROPERTIES}">
        <laser:render template="/templates/copyElements/copyPropertiesCompare" />
    </g:elseif>
    <g:elseif test="${workFlowPart == CopyElementsService.WORKFLOW_PACKAGES_ENTITLEMENTS}">
        <laser:render template="/templates/copyElements/copyPackagesAndIEs" />
    </g:elseif>
    %{--<g:elseif test="${workFlowPart == CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS}">--}%
    <g:else>
        <laser:render template="/templates/copyElements/copyElements" />
    </g:else>
    <laser:render template="/templates/copyElements/copyElementsJS"/>
</div>

<laser:htmlEnd />
