<%@ page import="de.laser.ExportClickMeService; de.laser.IdentifierNamespace; de.laser.Org;" %>
<laser:serviceInjection/>

<!-- _individuallyExportModal.gsp -->
<g:set var="formFields" value="${exportClickMeService.getExportRenewalFieldsForUI(surveyConfig)}"/>

<ui:modal id="${modalID}" text="Excel-Export" refreshModal="true" hideSubmitButton="true">

    <g:form controller="survey" action="renewalEvaluation" id="${surveyInfo.id}"
            params="[surveyConfigID: surveyConfig.id]">

        <laser:render template="/templates/export/individuallyExportForm"
                  model="${[formFields: formFields, exportFileName: escapeService.escapeString(surveyConfig.getSurveyName()) + "_" + message(code:'renewalexport.renewals'),
                            exportExcelButtonName: message(code: 'renewalEvaluation.exportExcelRenewal'), exportCSVButtonName: message(code: 'renewalEvaluation.exportCSVRenewal'), contactSwitch: true, csvFieldSeparator: '|']}"/>

    </g:form>

</ui:modal>
<!-- _individuallyExportModal.gsp -->

