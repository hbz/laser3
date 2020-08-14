<%@ page import="com.k_int.kbplus.Person; com.k_int.kbplus.RefdataValue; com.k_int.kbplus.SubscriptionController" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
        <title>${message(code: 'laser')} : ${message(code: 'copyElementsIntoObject.label')}</title>
</head>
<body>

    <g:render template="breadcrumb" model="${[params: params]}"/>
        <br>
        <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code: 'copyElementsIntoObject.label')} </h1>

    <semui:messages data="${flash}"/>

    <% Map params = [id: params.id];
        if (sourceObjectId)   params << [sourceObjectId: sourceObjectId]
        if (targetObjectId)   params << [targetObjectId: targetObjectId]
    %>
        <semui:subNav>
            <semui:complexSubNavItem class="${workFlowPart == CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS ? 'active' : ''}" controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS]}" >
                <div class="content" >
                    <div class="title">${message(code: 'copyElementsIntoObject.general_data.label')}</div>
                    <div class="description">
                        <i class="calendar alternate outline icon"></i>${message(code: 'subscription.periodOfValidity.label')}
                        <i class="ellipsis vertical icon"></i>${message(code:'subscription.status.label')}
                        <br>
                        <i class="image outline icon"></i>${message(code:'subscription.kind.label')}
                        <i class="dolly icon"></i>${message(code:'subscription.form.label')}
                        <i class="box icon"></i>${message(code:'subscription.resource.label')}
                        <br>
                        <i class="shipping fast icon"></i>${message(code:'subscription.isPublicForApi.label')}
                        <i class="flag outline icon"></i>${message(code:'subscription.hasPerpetualAccess.label')}
                        <br>
                        <i class="balance scale icon"></i>${message(code: 'license.label')}
                        <i class="university icon"></i>${message(code: 'subscription.organisations.label')}
                        <i class="address card icon"></i>${message(code: 'subscription.specificSubscriptionEditors')}
                        <br>
                        <i class="barcode icon"></i>${message(code: 'default.identifiers.label')}

                    </div>
                </div>
            </semui:complexSubNavItem>

            <semui:complexSubNavItem class="${workFlowPart == CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS ? 'active' : ''}" controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS]}" >
                <div class="content">
                    <div class="title">${message(code: 'copyElementsIntoObject.attachements.label')}</div>
                    <div class="description">
                        <i class="file outline icon"></i>${message(code: 'default.documents.label')}
                        <i class="sticky note outline icon"></i>${message(code: 'default.notes.label')}
                        <i class="checked calendar icon"></i>${message(code: 'menu.institutions.tasks')}
                    </div>
                </div>
            </semui:complexSubNavItem>

            <g:if test="${isSubscriberVisible && accessService.checkPermAffiliation("ORG_CONSORTIUM,ORG_INST_COLLECTIVE", "INST_EDITOR")}">
                <semui:complexSubNavItem class="${workFlowPart == CopyElementsService.WORKFLOW_SUBSCRIBER ? 'active' : ''}" controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: CopyElementsService.WORKFLOW_SUBSCRIBER]}" >
                    <div class="content">
                        <div class="title">
                            ${message(code: 'consortium.subscriber')}
                        </div>
                        <div class="description">
                            <i class="university icon"></i>${message(code: 'consortium.subscriber')}
                        </div>
                    </div>
                </semui:complexSubNavItem>
            </g:if>

            <semui:complexSubNavItem class="${workFlowPart == CopyElementsService.WORKFLOW_PROPERTIES ? 'active' : ''}" controller="subscription" action="copyElementsIntoSubscription" params="${params << [workFlowPart: CopyElementsService.WORKFLOW_PROPERTIES]}" >
                <div class="content">
                    <div class="title">${message(code: 'properties')}</div>
                    <div class="description">
                        <i class="tags icon"></i>${message(code: 'properties')}
                    </div>

                </div>
            </semui:complexSubNavItem>
        </semui:subNav>
    </g:else>
    <br>
    <div class="la-legend">
        <span class="la-key"><strong>${message(code: 'copyElementsIntoObject.legend.key')}: </strong></span>
        <span class="la-added">${message(code: 'copyElementsIntoObject.legend.willStay')}</span>
        <span class="la-removed">${message(code: 'copyElementsIntoObject.legend.willBeReplaced')}</span>
    </div>
    <g:if test="${workFlowPart == CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS}">
        <g:render template="/templates/copyElements/copyDocsAndTasks" />
    </g:if>
    <g:elseif test="${workFlowPart == CopyElementsService.WORKFLOW_SUBSCRIBER && isSubscriberVisible && accessService.checkPermAffiliation("ORG_CONSORTIUM,ORG_INST_COLLECTIVE", "INST_EDITOR")}">
        <g:render template="/templates/copyElements/copySubscriber" />
    </g:elseif>
    <g:elseif test="${workFlowPart == CopyElementsService.WORKFLOW_PROPERTIES}">
        <g:render template="/templates/copyElements/copyPropertiesCompare" />
    </g:elseif>
    <g:else>
        <g:render template="/templates/copyElements/copyElements" />
    </g:else>
    <g:javascript src="copyPropertiesCompare.js"/>
</body>
</html>
