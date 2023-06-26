<%@ page import="de.laser.ExportClickMeService;" %>
<laser:serviceInjection/>

<!-- _individuallyExportModal.gsp -->
<g:set var="formFields" value="${exportClickMeService.getExportLicenseFieldsForUI(institution)}"/>

<ui:modal modalSize="large" id="${modalID}" text="Export" refreshModal="true" hideSubmitButton="true">

    <g:form action="${actionName}" controller="${controllerName}" params="${params}">

        <laser:render template="/templates/export/individuallyExportForm" model="${[formFields: formFields, exportFileName: message(code: 'license.plural'), csvFieldSeparator: ',', formats: [xlsx: 'XLSX', csv: 'CSV', pdf: 'PDF']]}"/>

    </g:form>

</ui:modal>
<!-- _individuallyExportModal.gsp -->

