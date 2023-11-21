<%@ page import="de.laser.ExportClickMeService;" %>
<laser:serviceInjection/>

<!-- _individuallyExportModalOrgs.gsp -->
<g:set var="formFields" value="${exportClickMeService.getExportOrgFieldsForUI(orgType)}"/>
<g:if test="${actionName in ['currentProviders','listProvider']}">
    <g:set var="exportFileName" value="${message(code: 'default.ProviderAgency.export.label')}"/>
</g:if>
<g:elseif test="${actionName in ['currentConsortia','listConsortia']}">
    <g:set var="exportFileName" value="${message(code: 'consortium.plural.label')}"/>
</g:elseif>
<g:elseif test="${actionName in ['listInstitution']}">
    <g:set var="exportFileName" value="${message(code: 'menu.institutions')}"/>
</g:elseif>
<g:else>
    <g:set var="exportFileName" value="${message(code: 'subscription.details.consortiaMembers.label')}"/>
</g:else>

<ui:modal id="${modalID}" text="Excel-Export" refreshModal="true" hideSubmitButton="true">

    <g:form action="${actionName}" controller="${controllerName}" params="${params}">

        <laser:render template="/templates/export/individuallyExportForm" model="${[accessPointNotice: true, formFields: formFields, exportFileName: exportFileName, contactSwitch: contactSwitch, csvFieldSeparator: '|']}"/>

    </g:form>

</ui:modal>
<!-- _individuallyExportModalOrgs.gsp -->

