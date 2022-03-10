<%@ page import="de.laser.ExportClickMeService;" %>
<laser:serviceInjection/>
<g:set var="exportClickMeService" bean="exportClickMeService"/>
<!-- _individuallyExportModal.gsp -->
<g:set var="formFields" value="${exportClickMeService.getExportCostItemFieldsForUI()}"/>

<semui:modal modalSize="large" id="${modalID}" text="Excel-Export" hideSubmitButton="true">

    <g:form action="financialsExport" controller="finance" params="${params+[id:params.id, exportClickMeExcel: true]}">

        <g:render template="/templates/export/individuallyExportForm" model="${[formFields: formFields, exportFileName: (subscription ? (escapeService.escapeString(subscription.name) + "_" + message(code:'subscription.details.financials.label')) : message(code:'subscription.details.financials.label'))]}"/>

    </g:form>

</semui:modal>
<!-- _individuallyExportModal.gsp -->

