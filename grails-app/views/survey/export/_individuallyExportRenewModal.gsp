<%@ page import="de.laser.ExportClickMeService; de.laser.IdentifierNamespace; de.laser.Org;" %>
<laser:serviceInjection/>
<g:set var="exportClickMeService" bean="exportClickMeService"/>
<!-- _individuallyExportModal.gsp -->
<g:set var="formFields" value="${exportClickMeService.getExportRenewalFieldsForUI(surveyConfig)}"/>

<semui:modal id="${modalID}" text="Excel-Export" hideSubmitButton="true">

    <g:form controller="survey" action="renewalEvaluation" id="${surveyInfo.id}"
            params="[surveyConfigID: surveyConfig.id, exportClickMeExcel: true]">

        <g:render template="/templates/export/individuallyExportForm"
                  model="${[formFields: formFields, exportFileName: escapeService.escapeString(surveyConfig.getSurveyName()) + "_" + message(code:'renewalexport.renewals'),
                            exportButtonName: message(code: 'renewalEvaluation.exportRenewal')]}"/>

    </g:form>

</semui:modal>
<!-- _individuallyExportModal.gsp -->

