<%@ page import="de.laser.ExportClickMeService; de.laser.IdentifierNamespace; de.laser.Org;" %>
<laser:serviceInjection/>

<!-- _individuallyExportModal.gsp -->
<g:set var="formFields" value="${exportClickMeService.getExportSurveyEvaluationFieldsForUI(surveyConfig)}"/>

<ui:modal id="${modalID}" text="Excel-Export" hideSubmitButton="true">

    <g:form controller="survey" action="surveyEvaluation" refreshModal="true" id="${surveyInfo.id}"
            params="[surveyConfigID: surveyConfig.id]">

        <laser:render template="/templates/export/individuallyExportForm" model="${[formFields: formFields, exportFileName: escapeService.escapeString(surveyConfig.getSurveyName()) + "_" + message(code:'surveyResult.label'), contactSwitch: true]}"/>

    </g:form>

</ui:modal>
<!-- _individuallyExportModal.gsp -->

