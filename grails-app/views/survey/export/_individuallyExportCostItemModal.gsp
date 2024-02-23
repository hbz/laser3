<%@ page import="de.laser.ExportClickMeService; de.laser.IdentifierNamespace; de.laser.Org;" %>
<laser:serviceInjection/>

<!-- __individuallyExportCostItemModal.gsp -->
<g:set var="formFields" value="${exportClickMeService.getExportSurveyCostItemFieldsForUI()}"/>

<ui:modal id="${modalID}" text="Excel-Export" hideSubmitButton="true">
    <g:form controller="survey" action="exportSurCostItems" refreshModal="true" id="${surveyInfo.id}"
            params="[exportXLSX: true, surveyConfigID: surveyConfig.id]">

        <laser:render template="/templates/export/individuallyExportForm"
                      model="${[formFields: formFields, exportFileName: escapeService.escapeString(surveyConfig.getSurveyName()) + "_" + message(code: 'financials.costItem'), contactSwitch: true, csvFieldSeparator: '|', overrideFormat: [xlsx: 'XLSX', csv: 'CSV']]}"/>

    </g:form>

</ui:modal>
<!-- __individuallyExportCostItemModal.gsp -->

