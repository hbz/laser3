<%@ page import="de.laser.ExportClickMeService; de.laser.IdentifierNamespace; de.laser.Org;" %>
<laser:serviceInjection/>

<!-- _individuallyExportModal.gsp -->
<g:set var="formFields" value="${exportClickMeService.getExportSurveyEvaluationFieldsForUI(surveyConfig)}"/>

<semui:modal id="${modalID}" text="Excel-Export" hideSubmitButton="true">

    <g:form controller="survey" action="surveyEvaluation" id="${surveyInfo.id}"
            params="[surveyConfigID: surveyConfig.id, exportClickMeExcel: true]">

        <laser:render template="/templates/export/individuallyExportForm" model="${[formFields: formFields, exportFileName: escapeService.escapeString(surveyConfig.getSurveyName()) + "_" + message(code:'surveyResult.label')]}"/>

    </g:form>

</semui:modal>
<!-- _individuallyExportModal.gsp -->

