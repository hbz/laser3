<%@ page import="de.laser.CopyElementsService;" %>
<laser:serviceInjection/>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'myinst.copyLicense')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="currentLicenses" message="license.current" />

    <g:if test="${sourceObject}">
        <semui:crumb action="show" controller="license" id="${sourceObject.id}" text="${sourceObject.reference}" />
        <semui:crumb class="active" message="myinst.copyLicense" />
    </g:if>
</semui:breadcrumbs>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />${message(code: 'myinst.copyLicense')}: ${sourceObject.reference}</h1>

<semui:messages data="${flash}"/>


<% Map params = [:]
if (sourceObjectId) params << [sourceObjectId: genericOIDService.getOID(sourceObject)]
if (targetObjectId)   params << [targetObjectId: genericOIDService.getOID(targetObject)]
%>

<div class="ui tablet stackable steps la-clear-before">
    <div class="${workFlowPart == CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS ? 'active' : (workFlowPart in [CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS , CopyElementsService.WORKFLOW_PROPERTIES] ? 'completed' : '')} step">
        <i class=" icon"></i>
        <div class="content" >
            <div class="title">
                ${message(code: 'copyElementsIntoObject.general_data.label')}
            </div>
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
                <i class="exchange icon"></i>${message(code: 'license.linkedObjects')}
            </div>
        </div>
    </div>
    <div class="${workFlowPart == CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS ? 'active' : (workFlowPart in [CopyElementsService.WORKFLOW_PROPERTIES] ? 'completed' : '')} step">
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
    <div class="${workFlowPart == CopyElementsService.WORKFLOW_PROPERTIES ? 'active' : ''} step">
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
<g:else>
    <laser:render template="/templates/copyElements/copyElements" />
</g:else>
<laser:render template="/templates/copyElements/copyElementsJS"/>

</body>
</html>