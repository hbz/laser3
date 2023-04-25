<%@ page import="de.laser.ExportClickMeService;" %>
<laser:serviceInjection/>

<!-- _individuallyExportModal.gsp -->
<g:set var="formFields" value="${exportClickMeService.getExportCostItemFieldsForUI(subscription)}"/>

<ui:modal modalSize="large" id="${modalID}" text="Excel-Export" refreshModal="true" hideSubmitButton="true">

    <g:form action="financialsExport" controller="finance" params="${params+[id:params.id]}">

        <laser:render template="/templates/export/individuallyExportForm" model="${[formFields: formFields, exportFileName: (subscription ? (escapeService.escapeString(subscription.name) + "_" + message(code:'subscription.details.financials.label')) : message(code:'subscription.details.financials.label'))]}"/>

    </g:form>

</ui:modal>
<!-- _individuallyExportModal.gsp -->

