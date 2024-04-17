<%@ page import="de.laser.ExportClickMeService;" %>
<laser:serviceInjection/>

<!-- _individuallyExportModalVendors.gsp -->
<g:set var="formFields" value="${exportClickMeService.getExportVendorFieldsForUI()}"/>

<ui:modal id="${modalID}" text="Excel-Export" refreshModal="true" hideSubmitButton="true">

    <g:form action="${actionName}" controller="${controllerName}" params="${params}">

        <laser:render template="/templates/export/individuallyExportForm" model="${[formFields: formFields, exportFileName: exportFileName, contactSwitch: contactSwitch, csvFieldSeparator: '|']}"/>

    </g:form>

</ui:modal>
<!-- _individuallyExportModalOrgs.gsp -->

